// ═══════════════════════════════════════════════════════════
//  preload — renderer に公開する API
//
//  contextIsolation: true / nodeIntegration: false のため、renderer は
//  ここで公開したものしか触れない。IPC のチャンネル名を renderer 側に
//  漏らさず、呼べる操作をこの一覧に限定する。
// ═══════════════════════════════════════════════════════════

const { contextBridge, ipcRenderer } = require('electron');

/** on 系の共通ヘルパー: 解除用の関数を返す */
function subscribe(channel, handler) {
  const listener = (_event, ...args) => handler(...args);
  ipcRenderer.on(channel, listener);
  return () => ipcRenderer.removeListener(channel, listener);
}

contextBridge.exposeInMainWorld('api', {
  // ── アプリ情報 / 設定 ──
  getAppInfo:       ()            => ipcRenderer.invoke('get-app-info'),
  getDefaultLang:   ()            => ipcRenderer.invoke('get-default-lang'),
  setUiLang:        lang          => ipcRenderer.invoke('set-ui-lang', lang),
  getApiKeyStatus:  ()            => ipcRenderer.invoke('get-api-key-status'),
  setApiKey:        (field, key)  => ipcRenderer.invoke('set-api-key', { field, key }),
  getLlmSelection:  ()            => ipcRenderer.invoke('get-llm-selection'),
  setLlmSelection:  selection     => ipcRenderer.invoke('set-llm-selection', selection),
  runtimeStatus:    ()            => ipcRenderer.invoke('runtime-status'),
  openBrowser:      url           => ipcRenderer.invoke('open-browser', url),
  rendererReady:    ()            => ipcRenderer.send('renderer-ready'),

  // ── コースパック ──
  loadCourses:      lang          => ipcRenderer.invoke('load-courses', { lang }),
  coursesInfo:      lang          => ipcRenderer.invoke('courses-info', { lang }),
  coursesReload:    lang          => ipcRenderer.invoke('courses-reload', { lang }),
  coursesOpenDir:   ()            => ipcRenderer.invoke('courses-open-dir'),

  // ── ワークスペース ──
  wsGetRoot:        ()            => ipcRenderer.invoke('ws-get-root'),
  wsSetRoot:        dir           => ipcRenderer.invoke('ws-set-root', dir),
  wsPickDir:        ()            => ipcRenderer.invoke('ws-pick-dir'),
  wsListProjects:   ()            => ipcRenderer.invoke('ws-list-projects'),
  wsCreateProject:  payload       => ipcRenderer.invoke('ws-create-project', payload),
  wsResetTemplate:  (name, lang)  => ipcRenderer.invoke('ws-reset-template', { name, lang }),
  wsProjectInfo:    name          => ipcRenderer.invoke('ws-project-info', { name }),
  wsProjectContext: name          => ipcRenderer.invoke('ws-project-context', { name }),
  wsTree:           name          => ipcRenderer.invoke('ws-tree', { name }),
  wsReadFile:       (name, relPath) => ipcRenderer.invoke('ws-read-file', { name, relPath }),
  wsWriteFile:      (name, relPath, content) =>
                                     ipcRenderer.invoke('ws-write-file', { name, relPath, content }),
  wsCreateEntry:    (name, relPath, kind) =>
                                     ipcRenderer.invoke('ws-create-entry', { name, relPath, kind }),
  wsDeleteEntry:    (name, relPath) => ipcRenderer.invoke('ws-delete-entry', { name, relPath }),
  wsRenameEntry:    (name, relPath, newRelPath) =>
                                     ipcRenderer.invoke('ws-rename-entry', { name, relPath, newRelPath }),
  wsReveal:         (name, relPath) => ipcRenderer.invoke('ws-reveal', { name, relPath }),

  // ── 実行 ──
  runStart:         payload       => ipcRenderer.invoke('run-start', payload),
  runStop:          ()            => ipcRenderer.invoke('run-stop'),
  runStatus:        ()            => ipcRenderer.invoke('run-status'),
  runStdin:         text          => ipcRenderer.send('run-stdin', text),
  onRunOutput:      handler       => subscribe('run-output', handler),
  onRunExit:        handler       => subscribe('run-exit', handler),
  onRunUrl:         handler       => subscribe('run-url', handler),
  onRunTestResults: handler       => subscribe('run-test-results', handler),

  // ── ターミナル ──
  termStart:        opts          => ipcRenderer.invoke('term-start', opts),
  termStop:         ()            => ipcRenderer.invoke('term-stop'),
  termInput:        data          => ipcRenderer.send('term-input', data),
  termResize:       size          => ipcRenderer.send('term-resize', size),
  onTermOutput:     handler       => subscribe('term-output', handler),
  onTermExit:       handler       => subscribe('term-exit', handler),

  // ── Web プレビュー ──
  previewServe:     (name, relPath) => ipcRenderer.invoke('preview-serve', { name, relPath }),
  previewStatus:    ()            => ipcRenderer.invoke('preview-status'),
  previewStop:      ()            => ipcRenderer.invoke('preview-stop'),

  // ── SQL ──
  sqlStart:         schemaSQL     => ipcRenderer.invoke('sql-start', { schemaSQL }),
  sqlRun:           sql           => ipcRenderer.invoke('sql-run', { sql }),
  sqlStop:          ()            => ipcRenderer.invoke('sql-stop'),

  // ── 言語サーバー ──
  lspAvailable:     language      => ipcRenderer.invoke('lsp-available', { language }),
  lspStart:         payload       => ipcRenderer.invoke('lsp-start', payload),
  lspSend:          payload       => ipcRenderer.invoke('lsp-send', payload),
  lspStop:          payload       => ipcRenderer.invoke('lsp-stop', payload),
  onLspMessage:     handler       => subscribe('lsp-message', handler),

  // ── LLM チャット ──
  chatSend:         payload       => ipcRenderer.send('chat-send', payload),
  chatAbort:        ()            => ipcRenderer.invoke('chat-abort'),
  onChatStart:      handler       => subscribe('chat-start', handler),
  onChatChunk:      handler       => subscribe('chat-chunk', handler),
  onChatEnd:        handler       => subscribe('chat-end', handler),
  onChatError:      handler       => subscribe('chat-error', handler),
});
