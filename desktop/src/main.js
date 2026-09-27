// ═══════════════════════════════════════════════════════════
//  Codinable — メインプロセス
//
//  役割は「renderer からの依頼を、OS 上の実体 (ファイル・プロセス・ポート) に
//  つなぐこと」だけ。機能ごとの実装は src/main/ 配下に分けてある
//
//    main/config.js        設定の永続化 (API キーは safeStorage で暗号化)
//    main/runtimes.js      同梱ランタイム (java / node / python / bash …) の解決
//    main/util.js          出力デコード・プロセス停止・ディレクトリ走査
//    main/workspace.js     ワークスペースとプロジェクト (実ファイル操作)
//    main/courses.js       コースパック (講座ごとのサンプル雛形・講座の単独更新)
//    main/updater.js       アプリ本体の更新 (electron-updater)
//    main/runner.js        実行エンジン (Gradle / npm / java / python / node / bash)
//    main/terminal.js      ターミナル (node-pty)
//    main/static-server.js 静的 Web ページ配信
//    main/sql.js           HSQLDB インメモリ SQL 実行
//    lsp-server.js         Java 言語サーバー (jdtls) の起動と中継
//    llm/                  LLM チャット (BYOK)
// ═══════════════════════════════════════════════════════════

const fs   = require('fs');
const os   = require('os');
const path = require('path');
const { app, BrowserWindow, Menu, ipcMain, shell } = require('electron');

const { PRODUCT, LLM_MODELS } = require('./app-config');

// userData のディレクトリ名を決めるため、他のモジュールを読む前に呼ぶ
app.setName(PRODUCT.dataDirName);
// A profile owns one set of broker data and listening ports.
if (!app.requestSingleInstanceLock()) app.exit(0);
app.on('second-instance', () => {
  if (mainWindow && !mainWindow.isDestroyed()) {
    if (mainWindow.isMinimized()) mainWindow.restore();
    mainWindow.show();
    mainWindow.focus();
  }
});

const config     = require('./main/config');
const runtimes   = require('./main/runtimes');
const util       = require('./main/util');
const workspace  = require('./main/workspace');
const courses    = require('./main/courses');
const runner     = require('./main/runner');
const terminal   = require('./main/terminal');
const staticSrv  = require('./main/static-server');
const sqlEngine  = require('./main/sql');
const messaging  = require('./main/messaging');
const companions = require('./main/companions');
const agent      = require('./main/agent');
const updater    = require('./main/updater');
const lspServer  = require('./lsp-server');
const llm        = require('./llm');
const { buildSystemPrompt, buildContextMessage } = require('./llm/prompt');

const APP_NAME = PRODUCT.displayName;

// ═══════════════════════════════════════════════════════════
//  アプリ情報 / 設定
// ═══════════════════════════════════════════════════════════

ipcMain.handle('get-app-info', () => {
  const selection = config.getLlmSelection();
  return {
    product:     PRODUCT.displayName,
    version:     app.getVersion(),
    languages:   PRODUCT.languages,
    lang:        config.getUiLang(),
    workspaceRoot: config.getWorkspaceRoot(),
    llmModels:   LLM_MODELS.map(m => ({
      id: m.id, label: m.label, provider: m.provider,
      defaultModel: m.model, keyField: m.keyField, keyLabel: m.keyLabel, keyUrl: m.keyUrl,
    })),
    llmSelection: selection,
    apiKeyStatus: config.getApiKeyStatus(),
    lspAvailable: lspServer.isAvailable(app, 'java'),
    appUpdate:   updater.availability(),
  };
});

// ═══════════════════════════════════════════════════════════
//  アプリ本体の更新 (講座の更新は courses-prepare-start)
// ═══════════════════════════════════════════════════════════

// 確認・進捗は OS 標準ダイアログではなく画面の #update-modal に出す
// main は表示の依頼と、押されたボタン (index) の受け取りだけを行う
let updDialogSeq = 0;
const updDialogPending = new Map();

function updaterWindow() {
  if (mainWindow && !mainWindow.isDestroyed()) return mainWindow;
  return BrowserWindow.getAllWindows().find(w => !w.isDestroyed()) || null;
}

ipcMain.on('updater-dialog-reply', (_event, payload) => {
  const resolve = payload && updDialogPending.get(payload.id);
  if (!resolve) return;
  updDialogPending.delete(payload.id);
  resolve(Number(payload.buttonIndex) || 0);
});

/** 画面へ送って応答を待つ。ウィンドウが閉じたら fallback で解放する (待ちっぱなしにしない) */
function askRenderer(channel, spec, fallback, timeoutMs = 0) {
  const win = updaterWindow();
  if (!win) return Promise.resolve(fallback);
  const id = `upd-${++updDialogSeq}`;
  return new Promise(resolve => {
    const done = value => { if (updDialogPending.delete(id)) resolve(value); };
    updDialogPending.set(id, resolve);
    win.webContents.once('destroyed', () => done(fallback));
    if (timeoutMs) setTimeout(() => done(fallback), timeoutMs);
    win.webContents.send(channel, { id, ...spec });
  });
}

const updaterDeps = {
  // 文言は画面側で表示言語に合わせて引く。ここではキーと差し込む値だけを渡す
  t: (key, vars) => ({ i18n: key, vars: vars || {} }),
  prompt: spec => askRenderer('updater-dialog', spec, spec.cancelIndex || 0),
  showProgress: spec => {
    const win = updaterWindow();
    if (win) win.webContents.send('updater-dialog', { id: `upd-${++updDialogSeq}`, ...spec });
  },
  progress: percent => updaterWindow()?.webContents.send('updater-dialog-progress', { percent }),
  dismiss: () => updaterWindow()?.webContents.send('updater-dialog-close'),
  // 再起動する前に、開いているファイルの未保存の変更を書き出してもらう
  beforeInstall: () => askRenderer('updater-before-install', {}, 0, 5000),
};

ipcMain.handle('updater-check', () => updater.checkForUpdates(updaterDeps, { silent: false }));

/** 起動して落ち着いたころに一度だけ黙って確認する (新しい版があるときだけ聞く) */
function checkAppUpdateOnStartup() {
  if (!updater.availability().enabled) return;
  setTimeout(() => {
    updater.checkForUpdates(updaterDeps, { silent: true }).catch(() => {});
  }, 5000);
}

ipcMain.handle('get-default-lang', () => config.getUiLang());
ipcMain.handle('set-ui-lang', (_event, lang) => config.setUiLang(lang));

ipcMain.handle('get-api-key-status', () => config.getApiKeyStatus());
ipcMain.handle('set-api-key', (_event, { field, key }) => config.saveApiKey(field, key));

ipcMain.handle('get-llm-selection', () => config.getLlmSelection());
ipcMain.handle('set-llm-selection', (_event, selection) => config.setLlmSelection(selection || {}));

ipcMain.handle('runtime-status', () => runtimes.probeRuntimes(util.decodeOutput));

ipcMain.handle('messaging-status', () => messaging.getManager().status());
for (const action of ['start', 'stop', 'reset']) {
  ipcMain.handle(`messaging-${action}`, async (_event, id) => {
    try { await messaging.getManager()[action](id); return { ok: true }; }
    catch (err) { return { ok: false, error: err.message }; }
  });
}

// 付き添いのプロセス (codinable.services.json の processes)
ipcMain.handle('companion-status', () => companions.getManager().status());
ipcMain.handle('companion-start', async (_event, { name, id } = {}) => {
  try {
    const dir = workspace.resolveProjectDir(name);
    const def = dir && companions.requirements(dir).find(d => d.id === id);
    if (!def) return { ok: false, error: 'not-found' };
    await companions.getManager().stopOthers(dir);
    await companions.getManager().start(dir, def, config.getUiLang());
    return { ok: true };
  } catch (err) { return { ok: false, error: err.message }; }
});
ipcMain.handle('companion-stop', async (_event, { id } = {}) => {
  await companions.getManager().stop(id);
  return { ok: true };
});

ipcMain.handle('open-browser', (_event, url) => {
  if (/^https?:\/\//i.test(String(url || ''))) shell.openExternal(url);
  return true;
});

// ═══════════════════════════════════════════════════════════
//  コースパック
// ═══════════════════════════════════════════════════════════

ipcMain.handle('load-courses', (_event, { lang } = {}) => {
  const uiLang = lang || config.getUiLang();
  // 取り組み中の講座は、今の版に固定しておく (版の固定を入れる前からの環境向け)
  courses.pinStartedCourses(workspace.listProjects().map(p => p.courseId), uiLang);
  return courses.loadCourses(uiLang);
});

// 講座を新しく始める前に、配信先の新しい版を取り込んでその版に固定する
ipcMain.handle('courses-prepare-start', (_event, { id, lang } = {}) =>
  courses.prepareCourseStart(String(id || ''), lang || config.getUiLang()));

// インストールされているコースと置き場の一覧 (設定画面に出す)
ipcMain.handle('courses-info', (_event, { lang } = {}) =>
  courses.describeCourses(lang || config.getUiLang()));

// コースを足したあとに読み直す (アプリの再起動を要らなくする)
ipcMain.handle('courses-reload', (_event, { lang } = {}) => {
  courses.clearCache();
  return courses.loadCourses(lang || config.getUiLang());
});

// 手でコースを足せるように、個人用のコース置き場を開く
ipcMain.handle('courses-open-dir', () => {
  const dir = courses.ensureUserCoursesDir();
  shell.openPath(dir);
  return { ok: true, dir };
});

// ═══════════════════════════════════════════════════════════
//  ワークスペース
// ═══════════════════════════════════════════════════════════

ipcMain.handle('ws-get-root', () => config.getWorkspaceRoot());
ipcMain.handle('ws-set-root', (_event, dir) => config.setWorkspaceRoot(dir));
ipcMain.handle('ws-pick-dir', () => workspace.pickDirectory());

ipcMain.handle('ws-list-projects', () => workspace.listProjects());

ipcMain.handle('ws-create-project', (_event, { name, courseId, templateId, lang } = {}) => {
  const templateDir = courseId && templateId
    ? courses.findTemplateDir(courseId, templateId, lang || config.getUiLang())
    : null;
  if (courseId && templateId && !templateDir) return { ok: false, error: 'template-not-found' };
  return workspace.createProject({ name, templateDir, courseId, template: templateId });
});


// 演習を配布時の状態に戻す。汚したコードをいつでも捨てられるようにするための口
ipcMain.handle('ws-reset-template', (_event, { name, lang } = {}) => {
  const dir = workspace.resolveProjectDir(name);
  if (!dir) return { ok: false, error: 'not-found' };
  const meta = workspace.readProjectMeta(dir);
  if (!meta.courseId || !meta.template) return { ok: false, error: 'no-template' };
  const templateDir = courses.findTemplateDir(meta.courseId, meta.template, lang || config.getUiLang());
  return workspace.resetToTemplate({ name, templateDir });
});

// 以前に作ったプロジェクトに、あとから雛形へ足された「最初に見せるファイル」を補う
ipcMain.handle('ws-add-missing-files', (_event, { name, files, lang } = {}) => {
  const dir = workspace.resolveProjectDir(name);
  if (!dir) return { ok: false, error: 'not-found' };
  const meta = workspace.readProjectMeta(dir);
  if (!meta.courseId || !meta.template) return { ok: false, error: 'no-template' };
  const templateDir = courses.findTemplateDir(meta.courseId, meta.template, lang || config.getUiLang());
  return workspace.addMissingFromTemplate({ name, templateDir, files: Array.isArray(files) ? files : [] });
});

ipcMain.handle('ws-project-info', (_event, { name } = {}) => {
  const dir = workspace.resolveProjectDir(name);
  if (!dir || !fs.existsSync(dir)) return { ok: false, error: 'not-found' };
  // services: 実行前に自動起動するサーバー。画面はそれを起動・停止ボタンとして出す
  // processes: 実行のたびに一緒に起こすプロセス (customer-hub など)
  let services = [], processes = [];
  try { services = messaging.requirements(dir); } catch { /* 書き方の誤りは実行時に出す */ }
  try { processes = companions.requirements(dir).map(({ id, port }) => ({ id, port })); } catch { /* 同上 */ }
  return { ok: true, name, path: dir, ...workspace.detectProject(dir),
           ...workspace.readProjectMeta(dir), services, processes };
});

// チャットへ渡すプロジェクトの中身 (添付操作の代わりに、送信時にまとめて渡す)
ipcMain.handle('ws-project-context', (_event, { name } = {}) =>
  workspace.collectContextFiles(name));

ipcMain.handle('ws-tree',         (_event, { name } = {}) => workspace.listTree(name));
ipcMain.handle('ws-read-file',    (_event, { name, relPath } = {}) => workspace.readFile(name, relPath));
ipcMain.handle('ws-write-file',   (_event, { name, relPath, content } = {}) =>
  workspace.writeFile(name, relPath, content));
ipcMain.handle('ws-create-entry', (_event, { name, relPath, kind } = {}) =>
  workspace.createEntry(name, relPath, kind));
ipcMain.handle('ws-delete-entry', (_event, { name, relPath } = {}) =>
  workspace.deleteEntry(name, relPath));
ipcMain.handle('ws-rename-entry', (_event, { name, relPath, newRelPath } = {}) =>
  workspace.renameEntry(name, relPath, newRelPath));
ipcMain.handle('ws-reveal',       (_event, { name, relPath } = {}) =>
  workspace.revealPath(name, relPath));

// ═══════════════════════════════════════════════════════════
//  実行
// ═══════════════════════════════════════════════════════════

ipcMain.handle('run-start', (event, payload = {}) =>
  runner.start(event, { uiLang: config.getUiLang(), ...payload }));
ipcMain.handle('run-stop',  () => runner.stop());
ipcMain.handle('run-status', () => ({ running: runner.isRunning() }));

// ═══════════════════════════════════════════════════════════
//  ターミナル
// ═══════════════════════════════════════════════════════════

ipcMain.handle('term-start', (event, opts = {}) => terminal.start(event, opts));
ipcMain.handle('term-stop',  () => terminal.stop());
ipcMain.on('term-input',  (_event, data) => terminal.write(data));
ipcMain.on('term-resize', (_event, size) => terminal.resize(size));

// ═══════════════════════════════════════════════════════════
//  Web プレビュー (静的ページ配信)
// ═══════════════════════════════════════════════════════════

ipcMain.handle('preview-serve', async (_event, { name, relPath } = {}) => {
  const dir = workspace.resolveInProject(name, relPath || '');
  if (!dir || !fs.existsSync(dir)) return { ok: false, error: 'not-found' };
  return staticSrv.serve(dir);
});
ipcMain.handle('preview-status', () => staticSrv.status());
ipcMain.handle('preview-stop',   () => staticSrv.stop());

// ═══════════════════════════════════════════════════════════
//  SQL (HSQLDB)
// ═══════════════════════════════════════════════════════════

ipcMain.handle('sql-start', (_event, { schemaSQL } = {}) =>
  sqlEngine.start({ schemaSQL, uiLang: config.getUiLang() }));
ipcMain.handle('sql-run',   (_event, { sql } = {}) => sqlEngine.run(sql));
ipcMain.handle('sql-stop',  () => sqlEngine.stop());

// ═══════════════════════════════════════════════════════════
//  言語サーバー (jdtls)
// ═══════════════════════════════════════════════════════════

/**
 * jdtls に渡す依存ライブラリの glob
 * Gradle 取り込みを使わない場合 (build.gradle が無いプロジェクト) の補助
 */
function libraryGlobs() {
  const globs = [];
  const gradleCache = path.join(os.homedir(), '.gradle', 'caches', 'modules-2', 'files-2.1');
  if (fs.existsSync(gradleCache)) globs.push(`${gradleCache.replace(/\\/g, '/')}/**/*.jar`);
  const hsqldb = runtimes.resolveHsqldb();
  if (hsqldb) globs.push(hsqldb.replace(/\\/g, '/'));
  return globs;
}

ipcMain.handle('lsp-available', (_event, { language } = {}) =>
  lspServer.isAvailable(app, language || 'java'));

ipcMain.handle('lsp-start', (event, { language, project } = {}) => {
  const rootPath = workspace.resolveProjectDir(project);
  if (!rootPath || !fs.existsSync(rootPath)) return { ok: false, error: 'project not found' };

  const result = lspServer.start(app, event.sender, { language: language || 'java', rootPath });
  if (!result.ok) return result;

  // build.gradle があるなら Gradle 取り込みに任せたほうが依存解決が正しい
  // 無いプロジェクト (フォルダ直置きの .java) は sourcePaths + referencedLibraries で補う
  const detected = workspace.detectProject(rootPath);
  return {
    ...result,
    rootPath,
    gradleImport: detected.kinds.includes('gradle'),
    libraryGlobs: libraryGlobs(),
  };
});

ipcMain.handle('lsp-send', (_event, payload) => lspServer.send(payload || {}));
ipcMain.handle('lsp-stop', (_event, { id } = {}) => lspServer.stop(id));

// ═══════════════════════════════════════════════════════════
//  LLM チャット
// ═══════════════════════════════════════════════════════════

let chatAbort = null;

ipcMain.handle('chat-abort', () => {
  try { chatAbort?.abort(); } catch { /* すでに終了している */ }
  return true;
});

ipcMain.on('chat-send', async (event, { messages, context } = {}) => {
  const send = (channel, ...args) => {
    if (!event.sender.isDestroyed()) event.sender.send(channel, ...args);
  };

  const selection = config.getLlmSelection();
  const history   = Array.isArray(messages) ? [...messages] : [];

  // 添付ファイルと実行ログは、最後のユーザー発言の直前に参考情報として挿し込む
  const contextMessage = buildContextMessage(context || {});
  if (contextMessage) {
    const insertAt = Math.max(0, history.length - 1);
    history.splice(insertAt, 0, { role: 'user', content: contextMessage });
  }

  chatAbort = new AbortController();
  try {
    send('chat-start');
    await llm.streamChat({
      modelId:       selection.modelId,
      modelOverride: selection.override,
      apiKeys:       config.loadApiKeys(),
      messages:      history,
      system:        buildSystemPrompt({ uiLang: config.getUiLang(), ...(context || {}) }),
      signal:        chatAbort.signal,
      onText:        text => send('chat-chunk', text),
    });
    send('chat-end');
  } catch (err) {
    if (err.name === 'AbortError') send('chat-end');
    else send('chat-error', err.message, { code: err.code || null, keyField: err.keyField || null });
  } finally {
    chatAbort = null;
  }
});

// ═══════════════════════════════════════════════════════════
//  Agent (道具を使う書き換え)
//
//  Ask との違いは道具を持つかどうかだけなので、キーの扱いも選択中モデルも
//  チャットと同じものを使う。経過は agent-* で画面へ流す
// ═══════════════════════════════════════════════════════════

let agentAbort = null;

ipcMain.handle('agent-abort', () => {
  try { agentAbort?.abort(); } catch { /* すでに終了している */ }
  return true;
});

ipcMain.on('agent-send', async (event, { messages, context } = {}) => {
  const send = (channel, ...args) => {
    if (!event.sender.isDestroyed()) event.sender.send(channel, ...args);
  };

  agentAbort = new AbortController();
  try {
    send('agent-start');
    await agent.runAgent({
      event,
      messages:  Array.isArray(messages) ? messages : [],
      context:   context || {},
      selection: config.getLlmSelection(),
      apiKeys:   config.loadApiKeys(),
      uiLang:    config.getUiLang(),
      signal:    agentAbort.signal,
    });
    send('agent-end');
  } catch (err) {
    if (err.name === 'AbortError') send('agent-end');
    else send('agent-error', err.message, { code: err.code || null, keyField: err.keyField || null });
  } finally {
    agentAbort = null;
  }
});

// ═══════════════════════════════════════════════════════════
//  ウィンドウ
// ═══════════════════════════════════════════════════════════

let splashWindow = null;
let mainWindow   = null;
let revealed     = false;

function createSplash() {
  splashWindow = new BrowserWindow({
    width: 460, height: 300,
    frame: false, resizable: false, maximizable: false, fullscreenable: false,
    center: true, show: false,
    title: APP_NAME,
    backgroundColor: '#181818',
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
    },
  });
  splashWindow.setMenuBarVisibility(false);
  splashWindow.removeMenu();
  splashWindow.once('ready-to-show', () => splashWindow?.show());
  splashWindow.loadFile(path.join(__dirname, 'renderer', 'splash.html'), {
    query: { lang: config.getUiLang(), product: APP_NAME },
  });
}

function closeSplash() {
  if (splashWindow && !splashWindow.isDestroyed()) {
    const w = splashWindow;
    splashWindow = null;
    try { w.close(); } catch { /* すでに閉じている */ }
  }
  splashWindow = null;
}

function createWindow() {
  revealed = false;

  // メニューバーは隠すが editMenu (コピー / 貼り付け / 元に戻す) は残す
  // setApplicationMenu(null) にすると環境によってエディタ上の Ctrl+C / Ctrl+V が効かなくなる
  Menu.setApplicationMenu(Menu.buildFromTemplate([{ role: 'editMenu' }]));

  mainWindow = new BrowserWindow({
    width: 1500, height: 950,
    minWidth: 1100, minHeight: 680,
    title: APP_NAME,
    backgroundColor: '#181818',
    // renderer の初期化が終わってから表示する (空の IDE を見せないため)
    show: false,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
      webviewTag: true,   // Web プレビュータブ (<webview>) 用
    },
  });

  mainWindow.setMenuBarVisibility(false);
  mainWindow.maximize();

  // 初期化が例外で止まっても永久に真っ暗にならないよう保険をかける
  mainWindow.once('ready-to-show', () => setTimeout(() => reveal('timeout'), 10_000));
  mainWindow.loadFile(path.join(__dirname, 'renderer', 'index.html'));
}

function reveal(reason = 'ready') {
  if (revealed) return;
  revealed = true;
  if (reason !== 'ready') {
    console.warn(`[boot] revealing main window (${reason}) — renderer-ready was not received`);
  }
  if (mainWindow && !mainWindow.isDestroyed()) {
    if (!mainWindow.isMaximized()) mainWindow.maximize();
    mainWindow.show();
    mainWindow.focus();
  }
  closeSplash();
}

let startupUpdateChecked = false;
ipcMain.on('renderer-ready', () => {
  reveal('ready');
  if (!startupUpdateChecked) { startupUpdateChecked = true; checkAppUpdateOnStartup(); }
});

// プレビュー (<webview>) 内のリンクは新規ウィンドウを開かず同じ webview 内で開く
app.on('web-contents-created', (_event, contents) => {
  if (contents.getType() === 'webview') {
    contents.setWindowOpenHandler(({ url }) => {
      if (/^https?:\/\//i.test(url)) contents.loadURL(url);
      return { action: 'deny' };
    });
  }
});

// ═══════════════════════════════════════════════════════════
//  ライフサイクル
// ═══════════════════════════════════════════════════════════

app.whenReady().then(() => {
  const brokerManager = messaging.getManager();
  for (const channel of ['status', 'log']) brokerManager.on(channel, payload => {
    for (const win of BrowserWindow.getAllWindows()) {
      if (!win.webContents.isDestroyed()) win.webContents.send(`messaging-${channel}`, payload);
    }
  });
  for (const channel of ['status', 'log']) companions.getManager().on(channel, payload => {
    for (const win of BrowserWindow.getAllWindows()) {
      if (!win.webContents.isDestroyed()) win.webContents.send(`companion-${channel}`, payload);
    }
  });
  // ワークスペースが無ければ作っておく (初回起動でファイルツリーが空でも迷わないように)
  try { fs.mkdirSync(config.getWorkspaceRoot(), { recursive: true }); } catch { /* 権限が無い環境では諦める */ }
  createSplash();
  createWindow();
});

app.on('window-all-closed', () => { if (process.platform !== 'darwin') app.quit(); });
app.on('activate', () => { if (BrowserWindow.getAllWindows().length === 0) createWindow(); });

// 終了時に子プロセス (実行・ターミナル・HSQLDB・言語サーバー・静的サーバー) を残さない
let cleanupComplete = false;
let cleanupPending = false;
app.on('before-quit', event => {
  if (cleanupComplete) return;
  event.preventDefault();
  if (cleanupPending) return;
  cleanupPending = true;
  runner.disposeAll();
  terminal.stop();
  sqlEngine.stop();
  staticSrv.stop();
  lspServer.stopAll();
  Promise.all([
    companions.getManager().stopAll().catch(err => console.error('[companions] shutdown:', err)),
    messaging.getManager().dispose().catch(err => console.error('[messaging] shutdown:', err)),
  ])
    .finally(() => { cleanupComplete = true; app.quit(); });
});
