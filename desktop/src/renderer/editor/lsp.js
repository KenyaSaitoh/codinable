// ═══════════════════════════════════════════════════════════
//  LSP クライアント (renderer 側)
//
//  言語サーバー本体 (Java = Eclipse jdtls) は main プロセスが子プロセスとして起動し、
//  stdio の Content-Length フレーミングを外した JSON-RPC 本文だけを IPC で流す
//  (src/lsp-server.js / preload.js の lspStart / lspSend / onLspMessage)
//  ここはその IPC を @codemirror/lsp-client の Transport に見せるだけの層
//
//  サーバー → クライアント方向のリクエスト (workspace/configuration など) には
//  この層で応答する。応答しないと jdtls は初期化の途中で止まってしまう
// ═══════════════════════════════════════════════════════════

import { LSPClient, languageServerExtensions } from '@codemirror/lsp-client';

/** jdtls に渡す設定。フォーマッタ・補完まわりだけ学習用に寄せる */
function languageSettings(language) {
  if (language !== 'java') return {};
  return {
    java: {
      completion: { enabled: true, guessMethodArguments: true },
      // 学習中のコードは未使用 import などが当たり前に出るので、警告どまりにする
      errors: { incompleteClasspath: { severity: 'ignore' } },
      configuration: { updateBuildConfiguration: 'automatic' },
      autobuild: { enabled: true },
      maxConcurrentBuilds: 1,
      format: { enabled: true },
      signatureHelp: { enabled: true },
      // 学習環境では import 整理の提案までは要らない
      saveActions: { organizeImports: false },
      // build.gradle が無いプロジェクト (フォルダに .java を直置きしただけのもの) では、
      // jdtls は「見えないプロジェクト」として扱い、ソースルートが分からないまま
      //   The declared package "pro.kensait.sample"
      //     does not match the expected package ""
      // という誤った診断を出す。ソースルートを明示して防ぐ
      project: {
        sourcePaths: ['src/main/java', 'src/test/java'],
        outputPath: 'bin',
      },
      // Gradle 取り込みの既定は「切」。build.gradle があるプロジェクトだけ
      // createLspConnection() が main 側の判定 (gradleImport) を見て有効にする
      // 取り込みは依存解決のためにネットワークアクセスと数分の待ちを伴うので、
      // 必要のないプロジェクトで走らせたくない
      import: {
        gradle: { enabled: false },
        maven:  { enabled: false },
      },
    },
  };
}

class ElectronLspTransport {
  constructor(id, { rootUri, settings }) {
    this.id = id;
    this.rootUri = rootUri;
    this.settings = settings;
    this.handlers = new Set();
    this.closed = false;
  }

  rawSend(message) {
    if (this.closed) return;
    window.api.lspSend({ id: this.id, body: message }).catch(() => {});
  }

  /** @codemirror/lsp-client → サーバー */
  send(message) {
    let parsed;
    try {
      parsed = JSON.parse(message);
    } catch {
      this.rawSend(message);
      return;
    }
    if (parsed.method === 'initialize') {
      parsed.params.workspaceFolders = [{ uri: this.rootUri, name: 'Codinable' }];
      parsed.params.initializationOptions = { settings: this.settings };
    }
    this.rawSend(JSON.stringify(parsed));
  }

  subscribe(handler)   { this.handlers.add(handler); }
  unsubscribe(handler) { this.handlers.delete(handler); }

  /** サーバー → @codemirror/lsp-client */
  receive(message) {
    let parsed;
    try {
      parsed = JSON.parse(message);
    } catch {
      return;
    }
    // id と method の両方を持つ = サーバーからのリクエスト。ここで返す
    if (Object.prototype.hasOwnProperty.call(parsed, 'id') && parsed.method) {
      this.respondToServerRequest(parsed);
      return;
    }
    for (const handler of this.handlers) handler(message);
  }

  respondToServerRequest(request) {
    let result = null;
    let error  = null;
    switch (request.method) {
      case 'workspace/configuration':
        result = (request.params?.items || []).map(item => this.configurationValue(item.section));
        break;
      case 'workspace/workspaceFolders':
        result = [{ uri: this.rootUri, name: 'Codinable' }];
        break;
      case 'workspace/applyEdit':
        // 学習者のファイルをサーバー主導で書き換えることはしない
        result = { applied: false, failureReason: 'Server-side edits are disabled.' };
        break;
      case 'window/showDocument':
        result = { success: false };
        break;
      case 'window/showMessageRequest':
      case 'client/registerCapability':
      case 'client/unregisterCapability':
      case 'window/workDoneProgress/create':
      case 'workspace/codeLens/refresh':
      case 'workspace/semanticTokens/refresh':
      case 'workspace/inlayHint/refresh':
      case 'workspace/diagnostic/refresh':
        result = null;
        break;
      default:
        error = { code: -32601, message: `Unsupported client request: ${request.method}` };
    }
    const response = { jsonrpc: '2.0', id: request.id };
    if (error) response.error = error;
    else response.result = result;
    this.rawSend(JSON.stringify(response));
  }

  configurationValue(section) {
    if (!section) return this.settings || {};
    let value = this.settings || {};
    for (const part of section.split('.')) {
      value = value?.[part];
      if (value === undefined) return null;
    }
    return value;
  }
}

/** OS パス → file:// URI (Windows のドライブレターとバックスラッシュを吸収する) */
export function pathToUri(absPath) {
  let p = String(absPath || '').replace(/\\/g, '/');
  if (!p.startsWith('/')) p = `/${p}`;
  return `file://${encodeURI(p).replace(/#/g, '%23').replace(/\?/g, '%3F')}`;
}

/**
 * 言語サーバーへ接続する。1 プロジェクト = 1 接続
 * onStatus: 'starting' | 'ready' | 'error' | 'off'
 */
export function createLspConnection({ language, project, onStatus }) {
  const settings = languageSettings(language);
  const notify   = onStatus || (() => {});

  let disposed = false;
  let ready    = false;
  let id       = null;
  let rootUri  = null;
  let transport = null;
  let client   = null;
  let offMessage = null;

  notify('starting');

  const started = (async () => {
    // プロジェクトディレクトリの絶対パスは main 側が解決して返す
    const res = await window.api.lspStart({ language, project });
    if (!res?.ok) throw new Error(res?.error || 'failed to start language server');
    if (disposed) { window.api.lspStop({ id: res.id }).catch(() => {}); return; }

    id = res.id;
    rootUri = pathToUri(res.rootPath);
    if (language === 'java') {
      // build.gradle があるなら Gradle 取り込みに任せたほうが依存解決が正しい
      // 無いプロジェクトは referencedLibraries (Gradle キャッシュ) で補う
      if (res.gradleImport) settings.java.import.gradle.enabled = true;
      else if (Array.isArray(res.libraryGlobs) && res.libraryGlobs.length) {
        settings.java.project.referencedLibraries = res.libraryGlobs;
      }
    }
    transport = new ElectronLspTransport(id, { rootUri, settings });
    offMessage = window.api.onLspMessage(payload => {
      if (disposed || !payload || payload.id !== id) return;
      if (payload.event === 'exit') { notify('error'); return; }
      transport.receive(payload.body);
    });

    client = new LSPClient({
      rootUri,
      // jdtls は初回の起動・インデックスに時間がかかる
      timeout: language === 'java' ? 120000 : 20000,
      sanitizeHTML: html => (window.DOMPurify ? window.DOMPurify.sanitize(html) : html),
      extensions: languageServerExtensions(),
    });
    client.connect(transport);

    await client.initializing;
    if (disposed) return;
    client.notification('workspace/didChangeConfiguration', { settings });
    ready = true;
    notify('ready');
  })().catch(err => {
    if (!disposed) {
      console.warn('[lsp] start failed:', err?.message || err);
      notify('error');
    }
  });

  return {
    get language() { return language; },
    get project()  { return project; },
    isReady: () => ready,
    whenStarted: () => started,

    /** プロジェクト相対パスのファイルに対する CodeMirror 拡張 */
    pluginFor(relPath, languageId) {
      if (!ready || !client) return [];
      const uri = `${rootUri}/${String(relPath).replace(/\\/g, '/').replace(/^\/+/, '')}`;
      return client.plugin(uri, languageId || language);
    },

    dispose() {
      disposed = true;
      ready = false;
      if (offMessage) { try { offMessage(); } catch { /* already removed */ } }
      if (client) { try { client.disconnect(); } catch { /* already disconnected */ } }
      if (id) window.api.lspStop({ id }).catch(() => {});
      notify('off');
    },
  };
}
