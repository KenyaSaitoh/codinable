// ═══════════════════════════════════════════════════════════
//  Codinable — レンダラー
//
//  画面の構成は index.html のとおり:
//    ヘッダー (プロジェクト選択 / モデル選択 / 設定)
//    プロジェクト情報バー
//    メイン = ファイルツリー | エディタ + 出力タブ | チャット
//
//  main プロセスとは preload.js の window.api だけでやり取りする
//  この層に fs / child_process は無いので、ファイルもプロセスも
//  すべて window.api 経由で扱う
//
//  状態の持ち方:
//    - 見た目 (テーマ・フォント・キーバインド・ペイン幅・最後に開いたプロジェクト)
//      → localStorage。アプリを再起動しても同じ画面で始まる
//    - 設定の本体 (表示言語・API キー・ワークスペース・モデル選択)
//      → main プロセスの設定ファイル (window.api 経由)
// ═══════════════════════════════════════════════════════════

/* global CM6, marked, DOMPurify, t, tf, setLang, getLang, applyI18nDom */

const $ = id => document.getElementById(id);

function escapeHtml(s) {
  return String(s ?? '').replace(/[&<>"']/g, c =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
}

/** 末尾の呼び出しだけを実行する (入力のたびに保存しないための間引き) */
function debounce(fn, ms) {
  let timer = null;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => fn(...args), ms);
  };
}

// ═══════════════════════════════════════════
//  アプリ全体の状態
// ═══════════════════════════════════════════

let appInfo     = null;   // get-app-info の結果 (バージョン・モデル一覧・キーの有無)
let projects    = [];     // ワークスペース直下のプロジェクト一覧
let project     = null;   // 選択中のプロジェクト名
let projectInfo = null;   // detectProject の結果 (kinds / gradleTasks / npmScripts / staticRoot)
let courses     = [];     // コースパック (演習一覧と新規コース開始・再開ダイアログで使う)
// 受講中の講座。選べるのは「新規コース開始・再開」ダイアログだけ (openCourseDialog)
let activeCourseId = localStorage.getItem('lastCourse') || '';

// ═══════════════════════════════════════════
//  テーマ / フォント / キーバインド
//
//  ワークベンチの配色は style.css の body[data-theme='<id>']、
//  エディタの配色は editor/themes.js が同じ id で持つ
// ═══════════════════════════════════════════

const THEMES = [
  { id: 'github-dark',     dark: true  },
  { id: 'one-dark-pro',    dark: true  },
  { id: 'tokyo-night',     dark: true  },
  { id: 'dracula',         dark: true  },
  { id: 'github-light',    dark: false },
  { id: 'solarized-light', dark: false },
];
const KEYMAPS  = ['default', 'vim', 'emacs', 'sublime'];
const DEFAULTS = {
  theme: 'github-dark', fontSize: 15, keymap: 'default',
  fontFamily: "'JetBrains Mono', monospace",
};

function resolveTheme(id) {
  return THEMES.find(x => x.id === id) || THEMES.find(x => x.id === DEFAULTS.theme);
}

function applyTheme(id) {
  const def = resolveTheme(id);
  document.body.dataset.theme = def.id;
  // ダーク系に共通の構造ルール (body.theme-dark …) 用
  document.body.classList.toggle('theme-dark', def.dark);
  forEachEditor(cm => cm.setOption('theme', def.id));
  document.querySelectorAll('#theme-grid .theme-btn')
    .forEach(b => b.classList.toggle('active', b.dataset.theme === def.id));
  localStorage.setItem('theme', def.id);
  applyTerminalTheme();
}

function applyFontSize(size) {
  document.documentElement.style.setProperty('--editor-font-size', `${size}px`);
  $('font-size-display').textContent = `${size}px`;
  forEachEditor(cm => cm.refresh());
  localStorage.setItem('fontSize', size);
  applyTerminalFont();
}

function applyFontFamily(family) {
  document.documentElement.style.setProperty('--editor-font-family', family);
  forEachEditor(cm => cm.refresh());
  localStorage.setItem('fontFamily', family);
  applyTerminalFont();
}

function applyKeymap(keymap) {
  const id = KEYMAPS.includes(keymap) ? keymap : DEFAULTS.keymap;
  forEachEditor(cm => cm.setOption('keyMap', id));
  $('keymap-select').value = id;
  localStorage.setItem('keymap', id);
}

function loadLocalSettings() {
  const fontSize   = parseInt(localStorage.getItem('fontSize'), 10) || DEFAULTS.fontSize;
  const fontFamily = localStorage.getItem('fontFamily') || DEFAULTS.fontFamily;
  applyTheme(localStorage.getItem('theme') || DEFAULTS.theme);
  applyFontSize(fontSize);
  applyFontFamily(fontFamily);
  applyKeymap(localStorage.getItem('keymap') || DEFAULTS.keymap);
  $('font-size-range').value    = fontSize;
  $('font-family-select').value = fontFamily;

  // ペイン幅・出力欄の高さ
  const sidebarW = parseInt(localStorage.getItem('sidebarWidth'), 10);
  if (sidebarW) $('sidebar').style.width = `${sidebarW}px`;
  const aiW = parseInt(localStorage.getItem('aiWidth'), 10);
  if (aiW) $('ai-panel').style.width = `${aiW}px`;
  const runH = parseInt(localStorage.getItem('runHeight'), 10);
  if (runH) $('run-output-wrap').style.height = `${runH}px`;
  const exerciseH = parseInt(localStorage.getItem('exerciseHeight'), 10);
  if (exerciseH) $('exercise-list').style.height = `${exerciseH}px`;
}

// ═══════════════════════════════════════════
//  汎用ダイアログ (alert / confirm / prompt)
//
//  Electron では window.alert / confirm がメインプロセスを止めてしまうため、
//  自前のモーダルで置き換える。いずれも Promise を返す
// ═══════════════════════════════════════════

let dialogResolve = null;

function closeSimpleDialog(value) {
  $('simple-dialog-overlay').classList.add('hidden');
  const resolve = dialogResolve;
  dialogResolve = null;
  if (resolve) resolve(value);
}

function showDialog({ message, kind = 'alert', defaultValue = '' }) {
  const overlay = $('simple-dialog-overlay');
  $('simple-dialog-message').textContent = message;
  const input  = $('simple-dialog-input');
  const cancel = $('simple-dialog-cancel');
  input.classList.toggle('hidden', kind !== 'prompt');
  cancel.classList.toggle('hidden', kind === 'alert');
  input.value = defaultValue;
  overlay.classList.remove('hidden');
  setTimeout(() => (kind === 'prompt' ? input.focus() : $('simple-dialog-ok').focus()), 0);

  return new Promise(resolve => {
    dialogResolve = resolve;
    $('simple-dialog-ok').onclick = () =>
      closeSimpleDialog(kind === 'prompt' ? input.value.trim() : true);
    cancel.onclick = () => closeSimpleDialog(kind === 'prompt' ? null : false);
  });
}

/** 処理中の表示 (講座の更新確認・アプリのダウンロード)。null で閉じる */
function showBusy(message) {
  $('busy-message').textContent = message || '';
  $('busy-overlay').classList.toggle('hidden', message === null);
}

/** '202609.1.0' を講座で使っている '202609.01.00' の形で見せる (semver では 0 埋めできない) */
function formatVersion(version) {
  const parts = String(version || '').split('.');
  return parts.map((part, i) => (i > 0 && /^\d$/.test(part) ? `0${part}` : part)).join('.');
}

const alertDialog   = message => showDialog({ message });
const confirmDialog = message => showDialog({ message, kind: 'confirm' });
const promptDialog  = (message, defaultValue = '') =>
  showDialog({ message, kind: 'prompt', defaultValue });

// ═══════════════════════════════════════════
//  ファイル種別
// ═══════════════════════════════════════════

/** 拡張子 → CodeMirror の mode (editor/languages.js の FACTORY のキー) */
function cmModeFromFilename(filename) {
  const ext = filename && filename.includes('.') ? filename.split('.').pop().toLowerCase() : '';
  return ({
    java: 'text/x-java', kt: 'text/x-java',
    groovy: 'text/x-groovy', gradle: 'text/x-groovy',
    xml: 'xml', html: 'text/html', htm: 'text/html', jsp: 'text/html', vue: 'text/html',
    css: 'text/css', scss: 'text/css', less: 'text/css',
    sql: 'text/x-sql', py: 'python',
    ts: 'text/typescript', tsx: 'text/typescript',
    js: 'text/javascript', mjs: 'text/javascript', cjs: 'text/javascript', jsx: 'text/javascript',
    json: 'application/json',
    sh: 'text/x-sh', bash: 'text/x-sh',
    c: 'text/x-csrc', h: 'text/x-csrc',
    md: 'text/markdown', markdown: 'text/markdown',
    yaml: 'text/x-yaml', yml: 'text/x-yaml',
    properties: 'text/x-properties',
  })[ext] || 'text/plain';
}

const FILE_TYPE_MAP = {
  java: { label: 'java', bg: '#b07219', fg: '#fff' },
  py:   { label: 'py',   bg: '#3572a5', fg: '#fff' },
  js:   { label: 'js',   bg: '#d4ba00', fg: '#000' },
  mjs:  { label: 'js',   bg: '#d4ba00', fg: '#000' },
  ts:   { label: 'ts',   bg: '#3178c6', fg: '#fff' },
  tsx:  { label: 'tsx',  bg: '#3178c6', fg: '#fff' },
  jsx:  { label: 'jsx',  bg: '#61dafb', fg: '#000' },
  sql:  { label: 'sql',  bg: '#e38c00', fg: '#fff' },
  yaml: { label: 'yaml', bg: '#cc1c1c', fg: '#fff' },
  yml:  { label: 'yml',  bg: '#cc1c1c', fg: '#fff' },
  json: { label: 'json', bg: '#8aaf3a', fg: '#fff' },
  xml:  { label: 'xml',  bg: '#e07030', fg: '#fff' },
  html: { label: 'html', bg: '#e34c26', fg: '#fff' },
  css:  { label: 'css',  bg: '#563d7c', fg: '#fff' },
  properties: { label: 'prop', bg: '#4b8c4b', fg: '#fff' },
  gradle: { label: 'grd', bg: '#02303a', fg: '#fff' },
  kt:   { label: 'kt',   bg: '#7f52ff', fg: '#fff' },
  sh:   { label: 'sh',   bg: '#4eaa25', fg: '#fff' },
  md:   { label: 'md',   bg: '#2d59b0', fg: '#fff' },
  txt:  { label: 'txt',  bg: '#707070', fg: '#fff' },
};

function fileTypeIcon(filename) {
  const ext  = filename.includes('.') ? filename.split('.').pop().toLowerCase() : '';
  const info = FILE_TYPE_MAP[ext] || { label: 'file', bg: '#555', fg: '#fff' };
  return `<span class="file-type-badge" style="background:${info.bg};color:${info.fg}">` +
         `${escapeHtml(info.label)}</span>`;
}

/** Markdown を安全に HTML へ (描画前に必ず DOMPurify を通す) */
function renderMarkdown(text) {
  const html = window.marked ? marked.parse(String(text ?? '')) : escapeHtml(text);
  return window.DOMPurify ? DOMPurify.sanitize(html) : html;
}

// ═══════════════════════════════════════════
//  エディタ (開いたファイルごとに CodeMirror を 1 つ持つ)
//
//  タブを切り替えても取り消し履歴とスクロール位置が残るよう、インスタンスは
//  閉じるまで捨てずに CSS で出し入れする
// ═══════════════════════════════════════════

/** relPath -> { host, cm, dirty, mode } */
const openFiles = new Map();
let activeFile  = null;
let mdPreviewOn = false;

function forEachEditor(fn) {
  for (const entry of openFiles.values()) fn(entry.cm);
}

function activeEditor() {
  return activeFile ? openFiles.get(activeFile)?.cm || null : null;
}

const autoSave = debounce(relPath => saveFile(relPath), 900);

async function saveFile(relPath) {
  const entry = openFiles.get(relPath);
  if (!entry || !entry.dirty || !project) return;
  const res = await window.api.wsWriteFile(project, relPath, entry.cm.getValue());
  if (res.ok) {
    entry.dirty = false;
    renderTabs();
  } else {
    await alertDialog(tf('saveFailed', { error: res.error || '' }));
  }
}

async function openFile(relPath, { focus = true } = {}) {
  if (!project) return;

  if (openFiles.has(relPath)) {
    activateFile(relPath, { focus });
    return;
  }

  const res = await window.api.wsReadFile(project, relPath);
  if (!res.ok) {
    if (res.error === 'binary')        await alertDialog(t('openFailedBinary'));
    else if (res.error === 'too-large') await alertDialog(t('openFailedTooLarge'));
    else await alertDialog(tf('openFailed', { error: res.error || '' }));
    return;
  }

  const host = document.createElement('div');
  host.className = 'cm-host hidden';
  $('editor-hosts').appendChild(host);

  const mode = cmModeFromFilename(relPath);
  const cm = CM6.create(host, {
    value:        res.content,
    mode,
    theme:        document.body.dataset.theme,
    keyMap:       localStorage.getItem('keymap') || DEFAULTS.keymap,
    lineNumbers:  true,
    lineWrapping: mode === 'text/markdown',
    extraKeys: {
      // 自動保存もするが、明示的に保存したい人のために Ctrl+S も受ける
      'Ctrl-S': () => saveFile(relPath),
      'Cmd-S':  () => saveFile(relPath),
    },
  });

  // Markdown (README や SCHEMA.md) は読むためのものなので、最初はビューアで見せる
  // 「編集」に切り替えれば書き換えられる。どちらで見ているかはタブごとに覚える
  const entry = { host, cm, dirty: false, mode, preview: mode === 'text/markdown' };
  openFiles.set(relPath, entry);

  cm.on('change', () => {
    entry.dirty = true;
    renderTabs();
    autoSave(relPath);
  });
  cm.on('changes', () => updateHistoryButtons());
  cm.on('focus',   () => updateHistoryButtons());

  attachLspTo(relPath, entry);
  activateFile(relPath, { focus });
}

function activateFile(relPath, { focus = true } = {}) {
  activeFile = relPath;
  const entry = openFiles.get(relPath);
  mdPreviewOn = !!(entry && entry.mode === 'text/markdown' && entry.preview);

  for (const [key, e] of openFiles) {
    e.host.classList.toggle('hidden', key !== relPath || mdPreviewOn);
  }
  $('editor-empty').classList.toggle('hidden', openFiles.size > 0);
  $('md-preview').classList.toggle('hidden', !mdPreviewOn);
  if (mdPreviewOn) $('md-preview').innerHTML = renderMarkdown(entry.cm.getValue());

  $('editor-filepath').textContent = relPath || '';
  $('btn-md-preview').classList.toggle('hidden', !entry || entry.mode !== 'text/markdown');
  $('btn-md-preview').textContent = mdPreviewOn ? t('btnMdEdit') : t('btnMdPreview');

  renderTabs();
  renderTree();
  updateHistoryButtons();
  updateRunTargets();
  applyCoverageToEditor();
  if (focus && entry && !mdPreviewOn) setTimeout(() => entry.cm.focus(), 0);
}

async function closeFile(relPath) {
  const entry = openFiles.get(relPath);
  if (!entry) return;
  if (entry.dirty) {
    const name = relPath.split('/').pop();
    if (!await confirmDialog(tf('confirmCloseDirty', { name }))) return;
  }
  entry.cm.destroy();
  entry.host.remove();
  openFiles.delete(relPath);

  if (activeFile === relPath) {
    const next = [...openFiles.keys()].pop() || null;
    if (next) activateFile(next);
    else {
      activeFile = null;
      $('editor-filepath').textContent = '';
      $('editor-empty').classList.remove('hidden');
      $('btn-md-preview').classList.add('hidden');
      renderTabs();
      updateRunTargets();
    }
  } else {
    renderTabs();
  }
}

function closeAllFiles() {
  for (const entry of openFiles.values()) {
    entry.cm.destroy();
    entry.host.remove();
  }
  openFiles.clear();
  activeFile = null;
  $('editor-filepath').textContent = '';
  $('editor-empty').classList.remove('hidden');
  $('md-preview').classList.add('hidden');
  $('btn-md-preview').classList.add('hidden');
  renderTabs();
}

function renderTabs() {
  const bar = $('editor-tabs');
  bar.innerHTML = '';
  for (const [relPath, entry] of openFiles) {
    const name = relPath.split('/').pop();
    const tab  = document.createElement('div');
    tab.className = 'editor-tab' + (relPath === activeFile ? ' active' : '');
    tab.title = relPath;
    tab.innerHTML =
      `<span class="editor-tab-icon">${fileTypeIcon(name)}</span>` +
      `<span class="editor-tab-label">${escapeHtml(name)}${entry.dirty ? ' •' : ''}</span>` +
      '<span class="editor-tab-close">✕</span>';
    tab.addEventListener('click', ev => {
      if (ev.target.classList.contains('editor-tab-close')) closeFile(relPath);
      else activateFile(relPath);
    });
    bar.appendChild(tab);
  }
}

function updateHistoryButtons() {
  const cm = activeEditor();
  const size = cm ? cm.historySize() : { undo: 0, redo: 0 };
  $('btn-editor-undo').disabled = !cm || size.undo === 0;
  $('btn-editor-redo').disabled = !cm || size.redo === 0;
}

function toggleMdPreview() {
  const entry = activeFile ? openFiles.get(activeFile) : null;
  if (!entry || entry.mode !== 'text/markdown') return;
  mdPreviewOn = !mdPreviewOn;
  entry.preview = mdPreviewOn;
  const preview = $('md-preview');
  preview.classList.toggle('hidden', !mdPreviewOn);
  entry.host.classList.toggle('hidden', mdPreviewOn);
  $('btn-md-preview').textContent = mdPreviewOn ? t('btnMdEdit') : t('btnMdPreview');
  if (mdPreviewOn) preview.innerHTML = renderMarkdown(entry.cm.getValue());
}

// ═══════════════════════════════════════════
//  ファイルツリー
// ═══════════════════════════════════════════

let treeEntries  = [];
const collapsed  = new Set();   // 折りたたんでいるディレクトリの相対パス

async function reloadTree() {
  if (!project) { treeEntries = []; renderTree(); return; }
  const res = await window.api.wsTree(project);
  treeEntries = res.ok ? res.entries : [];
  renderTree();
}

function renderTree() {
  const root = $('file-tree');
  root.innerHTML = '';

  if (!project) {
    root.innerHTML = `<div class="tree-placeholder">${escapeHtml(t('explorerEmpty'))}</div>`;
    return;
  }

  // 折りたたんだディレクトリの中身は描かない
  const isHidden = relPath => {
    for (const dir of collapsed) {
      if (relPath.startsWith(`${dir}/`)) return true;
    }
    return false;
  };

  for (const entry of treeEntries) {
    if (isHidden(entry.path)) continue;
    const depth = entry.path.split('/').length - 1;
    const name  = entry.path.split('/').pop();
    const el    = document.createElement('div');
    el.style.paddingLeft = `${4 + depth * 12}px`;

    if (entry.dir) {
      const isCollapsed = collapsed.has(entry.path);
      el.className = 'tree-dir';
      el.innerHTML = `<span class="tree-icon">${isCollapsed ? '▸' : '▾'}</span>` +
                     `<span>${escapeHtml(name)}</span>`;
      el.addEventListener('click', () => {
        if (collapsed.has(entry.path)) collapsed.delete(entry.path);
        else collapsed.add(entry.path);
        renderTree();
      });
    } else {
      el.className = 'tree-file' + (entry.path === activeFile ? ' current' : '') +
                     (entry.text ? ' editable' : '');
      el.innerHTML = `<span class="tree-icon">${fileTypeIcon(name)}</span>` +
                     `<span>${escapeHtml(name)}</span>`;
      el.addEventListener('click', () => openFile(entry.path));
    }

    el.addEventListener('contextmenu', ev => {
      ev.preventDefault();
      showTreeMenu(ev, entry);
    });
    root.appendChild(el);
  }
}

// ── ツリーの右クリックメニュー ──

function closeTreeMenu() {
  document.querySelectorAll('.tree-context-menu').forEach(el => el.remove());
}

function showTreeMenu(ev, entry) {
  closeTreeMenu();
  const menu = document.createElement('div');
  menu.className = 'tree-context-menu';
  menu.style.left = `${ev.clientX}px`;
  menu.style.top  = `${ev.clientY}px`;

  const items = [];
  if (!entry.dir && entry.text) items.push([t('ctxOpen'), () => openFile(entry.path)]);
  if (!entry.dir && /\.(java|py|ts|js|mjs|cjs|sh|bash)$/i.test(entry.path)) {
    items.push([t('ctxRun'), () => runFile(entry.path)]);
  }
  if (!entry.dir && /\.html?$/i.test(entry.path)) {
    const dir = entry.path.includes('/') ? entry.path.replace(/\/[^/]+$/, '') : '';
    items.push([t('ctxPreview'), () => servePreview(dir)]);
  }
  items.push([t('ctxRename'), () => renameEntry(entry.path)]);
  items.push([t('ctxDelete'), () => deleteEntry(entry.path)]);
  items.push([t('ctxReveal'), () => window.api.wsReveal(project, entry.path)]);

  for (const [label, action] of items) {
    const item = document.createElement('div');
    item.className = 'tree-context-item';
    item.textContent = label;
    item.addEventListener('click', () => { closeTreeMenu(); action(); });
    menu.appendChild(item);
  }
  document.body.appendChild(menu);
}

async function createEntry(kind) {
  if (!project) return;
  const relPath = await promptDialog(t(kind === 'dir' ? 'promptNewDir' : 'promptNewFile'));
  if (!relPath) return;
  const res = await window.api.wsCreateEntry(project, relPath, kind);
  if (!res.ok) { await alertDialog(res.error || ''); return; }
  await reloadTree();
  await refreshProjectInfo();
  if (kind === 'file') openFile(relPath);
}

async function renameEntry(relPath) {
  const next = await promptDialog(t('promptRename'), relPath);
  if (!next || next === relPath) return;
  const res = await window.api.wsRenameEntry(project, relPath, next);
  if (!res.ok) { await alertDialog(res.error || ''); return; }
  if (openFiles.has(relPath)) {
    // 開いたままだと古いパスに保存してしまうので、いったん閉じて開き直す
    const entry = openFiles.get(relPath);
    entry.dirty = false;
    await closeFile(relPath);
    await openFile(next);
  }
  await reloadTree();
  await refreshProjectInfo();
}

async function deleteEntry(relPath) {
  const name = relPath.split('/').pop();
  if (!await confirmDialog(tf('confirmDeleteEntry', { name }))) return;
  const res = await window.api.wsDeleteEntry(project, relPath);
  if (!res.ok) { await alertDialog(res.error || ''); return; }
  if (openFiles.has(relPath)) {
    openFiles.get(relPath).dirty = false;
    await closeFile(relPath);
  }
  await reloadTree();
  await refreshProjectInfo();
}

// ═══════════════════════════════════════════
//  プロジェクト
// ═══════════════════════════════════════════

async function reloadProjects() {
  projects = await window.api.wsListProjects();
  // 「作成済み」の印が付く演習が変わるので、一覧を描き直す
  if (courses.length) renderExercises();
}

async function selectProject(name, { openInitial = [] } = {}) {
  // 別プロジェクトへ移る前に、未保存の変更を書き出しておく
  for (const relPath of openFiles.keys()) await saveFile(relPath);

  await window.api.runStop();
  await window.api.previewStop();
  setPreviewAvailable(false);
  disposeLsp();
  closeAllFiles();
  collapsed.clear();
  clearRunOutput();
  clearTestResults();

  project = name || null;
  localStorage.setItem('lastProject', project || '');
  rememberCourseProgress(projects.find(p => p.name === project));
  // どの演習を開いているかの選択背景を付け替える
  if (courses.length) renderExercises();

  await refreshProjectInfo();
  await reloadTree();

  for (const relPath of openInitial) {
    if (treeEntries.some(e => !e.dir && e.path === relPath)) await openFile(relPath);
  }

  startLspIfNeeded();
  restartTerminalIfVisible();
}

async function refreshProjectInfo() {
  if (!project) {
    projectInfo = null;
    applyExerciseTabs(null);
    $('project-path').textContent = '';
    $('btn-reset-exercise').classList.add('hidden');
    renderExerciseBadge();
    updateRunTargets();
    renderServiceControls();
    return;
  }

  const info = await window.api.wsProjectInfo(project);
  projectInfo = info.ok ? info : null;

  renderExerciseBadge();
  $('project-path').textContent = info.ok ? info.path : '';
  $('project-path').title = info.ok ? info.path : '';
  $('btn-reset-exercise').classList.toggle('hidden', !(info.ok && info.template));
  applyExerciseTabs(exerciseForProject(projectInfo));

  updateRunTargets();
  renderServiceControls();
}

/**
 * 演習を配布時の状態に戻す
 *
 * 試して壊したコードをいつでも捨てられるようにするための機能なので、
 * 編集中のタブも disk の内容に入れ替える (開いたままだと戻したのに古い内容が
 * 見え続け、保存した瞬間に書き戻ってしまう)
 */
async function resetExercise() {
  if (!project) return;
  if (!projectInfo?.template) { await alertDialog(t('resetNoTemplate')); return; }
  const displayName = exerciseForProject()?.name || project;
  if (!await confirmDialog(tf('confirmResetExercise', { name: displayName }))) return;

  // 自分で足したファイルも消すので、掴んでいるものを先に手放す
  // (Windows では実行中のプロセスや言語サーバーが開いているファイルを消せない)
  if (running) await stopRun();
  await window.api.previewStop();
  setPreviewAvailable(false);
  disposeLsp();

  const res = await window.api.wsResetTemplate(project, getLang());
  if (!res.ok) {
    startLspIfNeeded();
    await alertDialog(res.error === 'no-template' ? t('resetNoTemplate') : (res.error || ''));
    return;
  }
  // 残っていないファイル (自分で足したもの) のタブは閉じ、残ったものは読み直す
  for (const relPath of [...openFiles.keys()]) await reopenFileFromDisk(relPath, { closeIfMissing: true });
  await reloadTree();
  await refreshProjectInfo();
  startLspIfNeeded();
  const done = tf('resetDone', { n: res.written });
  await alertDialog(res.failed?.length
    ? `${done}\n${tf('resetPartlyFailed', { names: res.failed.join(', ') })}`
    : done);
}

// ═══════════════════════════════════════════
//  演習一覧
//
//  演習 = 講座のレッスンに対応する「動かして確かめる 1 単位」
//  問題を出して解かせるものではないので、正解・不正解や採点は持たない
//
//  受講者の操作を「演習を選ぶ → 実行を押す」の 2 手に収めるため、
//  選んだ時点で作業用プロジェクトの用意・ファイルを開く・実行対象の選択
//  (= 実行環境の切り替え) までを済ませる
// ═══════════════════════════════════════════

/** runtime → 一覧に出すアイコン。course.yaml の runtime と対応させる */
// Node.js には合う絵文字が無いので、ロゴと同じ緑の六角形を描く (大きさは絵文字にそろえる)
const NODE_ICON = '<svg class="runtime-svg" viewBox="0 0 24 24" aria-hidden="true">' +
  '<path d="M12 1.8 21 7v10l-9 5.2L3 17V7z" fill="#5fa04e"/>' +
  '<path d="M12 6.2 16.9 9v6L12 17.8 7.1 15V9z" fill="none" stroke="#fff" stroke-width="1.6" opacity=".85"/></svg>';

const RUNTIME_ICONS = {
  java: '☕', spring: '🌱', node: NODE_ICON, react: '⚛️',
  python: '🐍', static: '🌐', sql: '🗄', shell: '🖥', other: '📦',
};

async function reloadExercises({ reload = false } = {}) {
  courses = reload ? await window.api.coursesReload(getLang())
                   : await window.api.loadCourses(getLang());
  // 前回の講座が無くなっていたら (アンインストール・絞り込み) 先頭の講座にする
  if (!courses.some(c => c.id === activeCourseId)) activeCourseId = courses[0]?.id || '';
  renderCurrentCourse();
  renderExercises();
}

function currentCourse() {
  return courses.find(c => c.id === activeCourseId) || null;
}

/**
 * 受講中の講座を切り替える
 * 開いているプロジェクトはそのままにして、演習一覧だけ入れ替える
 * (別の講座を見ながら今の作業を続けられるようにするため)
 */
function setActiveCourse(id) {
  activeCourseId = id || '';
  localStorage.setItem('lastCourse', activeCourseId);
  renderCurrentCourse();
  renderExercises();
}

/** タイトル下の帯に、受講中の講座名を出す */
function renderCurrentCourse() {
  const course = currentCourse();
  $('current-course-name').textContent = course?.name || t('coursesEmpty');
  $('current-course-name').title = course?.description || course?.name || '';
}

// ═══════════════════════════════════════════
//  新規コース開始・再開ダイアログ
//
//  新規: インストールされている全講座。選ぶと最初の演習を開く
//  再開: 作業用プロジェクトが 1 つ以上ある講座。選ぶと最後に開いていた演習を開く
//  取り組んだ状態は作業用プロジェクトそのもの (ワークスペースのフォルダ) なので、
//  別に保存はしない。最後に開いた演習と日時だけ localStorage に控える
// ═══════════════════════════════════════════

function readCourseProgress() {
  try { return JSON.parse(localStorage.getItem('courseProgress') || '{}') || {}; }
  catch { return {}; }
}

function rememberCourseProgress(target) {
  if (!target?.courseId) return;
  const progress = readCourseProgress();
  progress[target.courseId] = { project: target.name, at: new Date().toISOString() };
  localStorage.setItem('courseProgress', JSON.stringify(progress));
}

/** 取り組んだことのある講座 (最後に触った順) */
function resumableCourses() {
  const progress = readCourseProgress();
  const result = [];
  for (const course of courses) {
    const exerciseIds = new Set(course.exercises.map(e => e.id));
    const own = projects.filter(p => p.courseId === course.id && exerciseIds.has(p.template));
    if (!own.length) continue;
    // 最後に開いた演習。控えが無い・消えているときはフォルダの更新日時が新しいもの
    const saved  = progress[course.id];
    const last   = own.find(p => p.name === saved?.project) || own[0];
    const at     = saved?.project === last.name ? saved.at : last.mtime;
    const worked = new Set(own.map(p => p.template)).size;
    result.push({ course, last, at, worked,
                  exercise: course.exercises.find(e => e.id === last.template) });
  }
  return result.sort((a, b) => new Date(b.at || 0) - new Date(a.at || 0));
}

function formatDateTime(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '';
  return d.toLocaleString(getLang() === 'ja' ? 'ja-JP' : 'en-US',
    { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
}

async function openCourseDialog() {
  projects = await window.api.wsListProjects();
  const body = $('course-dialog-body');
  body.innerHTML = '';

  const section = (titleKey, hintKey) => {
    const head = document.createElement('div');
    head.className = 'course-dialog-section';
    head.innerHTML = `<span class="course-dialog-section-title">${escapeHtml(t(titleKey))}</span>` +
                     `<span class="course-dialog-section-hint">${escapeHtml(t(hintKey))}</span>`;
    body.appendChild(head);
  };
  const empty = text => {
    const div = document.createElement('div');
    div.className = 'course-dialog-empty';
    div.textContent = text;
    body.appendChild(div);
  };
  const card = ({ kind, id, title, meta, desc, badge, active, onClick }) => {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = `course-card course-card-${kind}`;
    btn.dataset.courseId = id;
    btn.classList.toggle('active', !!active);
    btn.innerHTML =
      '<span class="course-card-head">' +
        `<span class="course-card-title">${escapeHtml(title)}</span>` +
        (badge ? `<span class="course-card-badge">${escapeHtml(badge)}</span>` : '') +
      '</span>' +
      (meta ? `<span class="course-card-meta">${escapeHtml(meta)}</span>` : '') +
      (desc ? `<span class="course-card-desc">${escapeHtml(desc)}</span>` : '');
    btn.addEventListener('click', onClick);
    body.appendChild(btn);
  };

  // ── 再開 ──
  const resumable = resumableCourses();
  section('courseResumeSection', 'courseResumeHint');
  if (!resumable.length) empty(t('courseResumeEmpty'));
  for (const r of resumable) {
    card({
      kind: 'resume',
      id: r.course.id,
      title: r.course.name,
      active: r.course.id === activeCourseId,
      badge: tf('courseProgressCount', { n: r.worked, total: r.course.exercises.length }),
      meta: tf('courseResumeLast', { name: r.exercise?.name || r.last.name, at: formatDateTime(r.at) }),
      onClick: () => resumeCourse(r),
    });
  }

  // ── 新規 ──
  section('courseNewSection', 'courseNewHint');
  if (!courses.length) empty(t('coursesEmpty'));
  const inProgress = new Set(resumable.map(r => r.course.id));
  for (const course of courses) {
    card({
      kind: 'new',
      id: course.id,
      title: course.name,
      badge: inProgress.has(course.id) ? t('courseInProgress') : '',
      meta: tf('coursesExercises', { n: course.exercises.length }),
      desc: course.description || '',
      onClick: () => startCourse(course),
    });
  }

  $('course-dialog-overlay').classList.remove('hidden');
}

function closeCourseDialog() {
  $('course-dialog-overlay').classList.add('hidden');
}

/** その講座の作業用プロジェクトがあるか (= 取り組み中か) */
function isCourseStarted(courseId) {
  return projects.some(p => p.courseId === courseId);
}

/**
 * 講座を新しく始める準備。配信先に新しい版があれば取り込み、その版に固定する
 * 取り組み中の講座は開始したときの版のまま動かすので、ここは通らない
 * 配信先に届かないときも、手元の版で始められる
 *
 * @returns 準備したあとの講座 (見つからなければ null)
 */
async function prepareCourseStart(course) {
  showBusy(tf('courseChecking', { name: course.name }));
  let res;
  try {
    res = await window.api.coursesPrepareStart(course.id, getLang());
  } finally {
    showBusy(null);
  }
  courses = await window.api.loadCourses(getLang());
  const prepared = courses.find(c => c.id === course.id) || null;
  renderCurrentCourse();
  renderExercises();
  if (res?.updated && prepared) {
    await alertDialog(tf('courseUpdated', { name: prepared.name, version: res.version }));
  }
  return prepared;
}

/** 新規: 講座を切り替えて、最初の演習を開く */
async function startCourse(course) {
  closeCourseDialog();
  setActiveCourse(course.id);
  const started = isCourseStarted(course.id);
  const target  = started ? course : await prepareCourseStart(course);
  const first   = target?.exercises[0];
  if (first) await openExercise(target, first, { prepared: !started });
}

/** 再開: 講座を切り替えて、最後に開いていた演習を開く */
async function resumeCourse({ course, last, exercise }) {
  closeCourseDialog();
  setActiveCourse(course.id);
  if (exercise) await openExercise(course, exercise, { preferProject: last.name });
  else await selectProject(last.name);
}

/** 演習に対応する作業用プロジェクト (まだ作っていなければ null) */
function projectForExercise(course, exercise) {
  const matches = projects.filter(p => p.courseId === course.id && p.template === exercise.id);
  return matches.find(p => p.name === project) || matches[0] || null;
}

/** 内部の作業フォルダ名ではなく、講座で見えている演習名を引く */
function exerciseForProject(info = projectInfo) {
  if (!info?.courseId || !info?.template) return null;
  const course = courses.find(c => c.id === info.courseId);
  return course?.exercises.find(e => e.id === info.template) || null;
}

function renderExercises() {
  const list    = $('exercise-list');
  const course  = currentCourse();
  const entries = course?.exercises || [];

  list.innerHTML = '';
  if (!entries.length) {
    list.innerHTML = `<div class="tree-placeholder">${escapeHtml(t('exerciseEmpty'))}</div>`;
    return;
  }

  // チャプターごとに折りたためるようにする (見出しの三角形で開閉)
  // 開いているチャプターは講座ごとに覚えておく。初めて見る講座では、
  // いま開いている演習のチャプター (無ければ最初のチャプター) だけを開く
  const activeExercise = entries.find(e => projectForExercise(course, e)?.name === project);
  const open = openChaptersFor(course, activeExercise?.chapter ?? entries[0]?.chapter);
  // 開いている演習のチャプターは必ず開く。初めて見る講座でも、この状態を覚えておく
  // (覚えずにおくと、次に描き直したとき直前のチャプターが閉じてしまう)
  if (activeExercise?.chapter) open.add(activeExercise.chapter);
  saveOpenChapters(course, open);

  let group = list;
  let lastChapter = null;
  for (const exercise of entries) {
    if (exercise.chapter && exercise.chapter !== lastChapter) {
      const chapter  = exercise.chapter;
      const expanded = open.has(chapter);
      const heading = document.createElement('button');
      heading.type = 'button';
      heading.className = 'exercise-chapter';
      heading.dataset.chapter = chapter;
      heading.setAttribute('aria-expanded', String(expanded));
      const title = course.chapters?.[chapter];
      heading.innerHTML =
        '<span class="exercise-chapter-caret" aria-hidden="true"></span>' +
        `<span class="exercise-chapter-title">${escapeHtml(tf('exerciseChapter', { n: chapter }))}` +
        (title ? ` ${escapeHtml(title)}` : '') + '</span>';
      heading.title = heading.textContent;
      list.appendChild(heading);

      group = document.createElement('div');
      group.className = 'exercise-chapter-items';
      group.classList.toggle('collapsed', !expanded);
      list.appendChild(group);

      const items = group;
      heading.addEventListener('click', () => {
        const nowOpen = heading.getAttribute('aria-expanded') !== 'true';
        heading.setAttribute('aria-expanded', String(nowOpen));
        items.classList.toggle('collapsed', !nowOpen);
        const state = openChaptersFor(course);
        if (nowOpen) state.add(chapter); else state.delete(chapter);
        saveOpenChapters(course, state);
      });
      lastChapter = chapter;
    }

    const created = projectForExercise(course, exercise);
    const item = document.createElement('button');
    item.type  = 'button';
    item.className = 'q-item exercise-item';
    item.dataset.exerciseId = exercise.id;
    item.classList.toggle('active', !!created && created.name === project);
    item.title = exercise.description || exercise.name;
    item.innerHTML =
      '<span class="exercise-runtime-icon" aria-hidden="true">' +
        `${RUNTIME_ICONS[exercise.runtime] || RUNTIME_ICONS.other}</span>` +
      '<span class="exercise-body">' +
        `<span class="exercise-title">${escapeHtml(exercise.name)}</span>` +
        (exercise.lesson
          ? `<span class="exercise-lesson">${escapeHtml(exercise.lesson)}</span>`
          : '') +
      '</span>' +
      `<span class="exercise-tag">${escapeHtml(t(`runtime_${exercise.runtime}`))}</span>`;
    item.addEventListener('click', () => openExercise(course, exercise));
    group.appendChild(item);
  }
  renderExerciseBadge();
}

/** コース名の右に、いま開いている演習をバッジで出す (演習一覧をスクロールしても見失わないように) */
function renderExerciseBadge() {
  const badge = $('current-exercise-badge');
  const course = currentCourse();
  const exercise = (course?.exercises || []).find(e => projectForExercise(course, e)?.name === project);
  badge.classList.toggle('hidden', !exercise);
  if (!exercise) { badge.textContent = ''; return; }
  badge.textContent = exercise.name;
  badge.title = tf('exerciseBadgeTitle', { name: exercise.lesson ? `${exercise.name}（${exercise.lesson}）` : exercise.name });
}

/** 講座ごとに開いているチャプター。まだ何も覚えていなければ fallback だけを開く */
function openChaptersFor(course, fallback = null) {
  try {
    const saved = JSON.parse(localStorage.getItem('openChapters') || '{}')[course.id];
    if (Array.isArray(saved)) return new Set(saved.map(String));
  } catch { /* 壊れていたら初期状態から */ }
  return new Set(fallback ? [String(fallback)] : []);
}

function saveOpenChapters(course, open) {
  let all = {};
  try { all = JSON.parse(localStorage.getItem('openChapters') || '{}') || {}; } catch { /* 作り直す */ }
  all[course.id] = [...open];
  localStorage.setItem('openChapters', JSON.stringify(all));
}

/**
 * 演習を開く
 * 作業用プロジェクトが無ければ雛形から作り、選んで、実行対象まで合わせる
 */
async function openExercise(course, exercise, { preferProject = null, prepared = false } = {}) {
  // 一覧から直接選んだ演習で講座を始めるときも、新しい版を確かめてから作る
  if (!prepared && !isCourseStarted(course.id)) {
    const prepared = await prepareCourseStart(course);
    if (!prepared) return;
    course = prepared;
    exercise = course.exercises.find(e => e.id === exercise.id) || course.exercises[0];
    if (!exercise) return;
  }
  let target = projects.find(p => p.name === preferProject) || projectForExercise(course, exercise);

  if (!target) {
    const name = uniqueProjectName(exercise.suggestName || exercise.id);
    const res  = await window.api.wsCreateProject({
      name, courseId: course.id, templateId: exercise.id, lang: getLang(),
    });
    if (!res.ok) {
      await alertDialog(tf('errCreateFailed', { error: res.error || '' }));
      return;
    }
    await reloadProjects();
    target = projects.find(p => p.name === name);
    if (!target) return;
  } else {
    // 以前に作ったプロジェクトには、あとから雛形に足したファイル (SCHEMA.md など) が無い
    // 最初に見せるファイルと、SQL を流す前に使う reset.sql だけは補っておく
    // (自分で書き換えたファイルは上書きしない。雛形に無いものは何もしない)
    await window.api.wsAddMissingFiles(target.name, [...(exercise.openFiles || []), SQL_RESET_FILE], getLang());
  }

  await selectProject(target.name, { openInitial: exercise.openFiles });
  await applyExerciseRunTarget(exercise);
  renderExercises();
}

/** 既にあるプロジェクトと名前がぶつからないようにする */
function uniqueProjectName(base) {
  const safe = String(base).replace(/[^A-Za-z0-9._-]/g, '-').replace(/^[^A-Za-z0-9]+/, '')
               || 'exercise';
  if (!projects.some(p => p.name === safe)) return safe;
  for (let i = 2; i < 100; i++) {
    if (!projects.some(p => p.name === `${safe}-${i}`)) return `${safe}-${i}`;
  }
  return `${safe}-${Date.now()}`;
}

/**
 * 演習が宣言している実行対象を選ぶ。これが「実行環境の自動切り替え」にあたる
 *
 * course.yaml の run は実行対象セレクトと同じ書式
 */
async function applyExerciseRunTarget(exercise) {
  if (!exercise.run) return;
  // 実行対象のファイルはここでは開かない。最初に見せるファイルは演習ごとに
  // openFiles で決めてあり (1 つが原則)、ここで開くとそれを押しのけてしまう
  // 実行対象は 'kind:パス' でファイルを指すので、開いていなくても動かせる
  selectRunTarget(exercise.run);
}

/** 実行対象セレクトにその選択肢があれば選ぶ (無ければ触らない) */
function selectRunTarget(value) {
  const select = $('run-target-select');
  if (![...select.options].some(opt => opt.value === value)) return;
  select.value = value;
  $('btn-run').disabled = running || !project;
}

// ═══════════════════════════════════════════
//  実行
// ═══════════════════════════════════════════

let running = false;

/**
 * 実行できるものを select に並べる。値は 'kind' または 'kind:引数'
 *
 * 並ぶ内容はプロジェクトの中身だけで決まり、エディタで選んでいるファイルには
 * 依存しない。README を開いているあいだ実行できなくなる、といったことを避ける
 * 先頭に来るものが既定になるので、そのプロジェクトの「本命」から並べる
 */
function updateRunTargets() {
  const select = $('run-target-select');
  const previous = select.value;
  select.innerHTML = '';

  const kinds   = projectInfo?.kinds || [];
  const options = [];

  for (const task of projectInfo?.gradleTasks || []) {
    options.push([`gradle:${task}`, tf('runTargetGradle', { task })]);
  }
  for (const script of projectInfo?.npmScripts || []) {
    options.push([`npm:${script}`, tf('runTargetNpm', { script })]);
  }
  // 静的配信は npm スクリプトが無いときだけ。Vite などは index.html が
  // あっても dev サーバー越しでないと動かないので、並べると壊れた選択肢になる
  if (projectInfo?.staticRoot !== null && projectInfo?.staticRoot !== undefined &&
      !(projectInfo?.npmScripts || []).length) {
    options.push([`static:${projectInfo.staticRoot}`, t('runTargetStatic')]);
  }
  for (const entry of projectInfo?.runnableFiles || []) {
    // .sql は子プロセスではなく常駐の HSQLDB へ流す (SQL タブに結果が出る)
    const key = entry.kind === 'sql' ? 'runTargetSql' : 'runTargetFile';
    options.push([`${entry.kind}:${entry.relPath}`, tf(key, { name: entry.relPath })]);
  }
  // main を持つ .java が拾えなかったときの保険 (自動でエントリポイントを探す)
  if (kinds.includes('java') && !kinds.includes('gradle') &&
      !(projectInfo?.runnableFiles || []).some(e => e.relPath.endsWith('.java'))) {
    options.push(['java', t('runTargetJava')]);
  }

  if (!options.length) options.push(['', t('runTargetNone')]);
  for (const [value, label] of options) {
    const opt = document.createElement('option');
    opt.value = value;
    opt.textContent = label;
    select.appendChild(opt);
  }
  if (options.some(([value]) => value === previous)) select.value = previous;

  $('btn-run').disabled = !project || !select.value;
}

function setRunning(state) {
  running = state;
  $('btn-run').disabled      = state || !project || !$('run-target-select').value;
  $('btn-run-stop').disabled = !state;
}

function clearRunOutput() {
  $('output-result').textContent = t('outputPlaceholder');
  runLog = '';
  runOutputFollow = true;
}

let runLog = '';   // チャットへ渡すための実行ログ (画面表示とは別に保持する)

// 実行結果は末尾を追いかける。スクロールしているのは <pre> ではなく外側の .run-pane
// 受講者が上へ戻って読んでいるあいだは動かさず、末尾まで戻したらまた追いかける
let runOutputFollow = true;

function scrollRunOutputToEnd() {
  const pane = $('tab-result');
  pane.scrollTop = pane.scrollHeight;
}

function appendRunOutput(text) {
  const pre = $('output-result');
  if (runLog === '') pre.textContent = '';
  runLog += text;
  pre.textContent += text;
  if (runOutputFollow) scrollRunOutputToEnd();
}

async function runSelected() {
  if (!project) { await alertDialog(t('runNoProject')); return; }
  const value = $('run-target-select').value;
  if (!value) { await alertDialog(t('runNoTarget')); return; }

  const [kind, arg = ''] = value.split(/:(.*)/s);

  // 静的ページは子プロセスを起こさず、内蔵の静的サーバーで配信する
  if (kind === 'static') { await servePreview(arg); return; }

  // SQL は子プロセスではなく常駐の HSQLDB へ流す
  // DB が止まっていればここで起こしてから流す (「DB起動」を先に押させない)
  if (kind === 'sql') { await runSqlFromEditor(arg); return; }

  // 保存していない内容で動かして混乱しないよう、先に全部書き出す
  for (const relPath of openFiles.keys()) await saveFile(relPath);

  showRunPane('tab-result');
  clearRunOutput();
  if (kind === 'gradle' && arg.split(/\s+/).includes('test')) renderTestResultsRunning();

  setRunning(true);
  const res = await window.api.runStart({
    project,
    kind,
    relPath: kind === 'file' ? arg : undefined,
    task:    kind === 'gradle' || kind === 'npm' ? arg : undefined,
  });
  if (!res.ok) {
    setRunning(false);
    appendRunOutput(`\n${tf('runFailed', { error: res.error || '' })}\n`);
    return;
  }
  setRunning(true);
}

/** ツリーの右クリックから直接ファイルを実行する */
async function runFile(relPath) {
  await openFile(relPath);
  // 実行対象に無いファイル (実行できないもの) なら選択は変えずに何もしない
  const value = /\.sql$/i.test(relPath) ? `sql:${relPath}` : `file:${relPath}`;
  selectRunTarget(value);
  if ($('run-target-select').value !== value) {
    await alertDialog(tf('runNotRunnable', { name: relPath }));
    return;
  }
  await runSelected();
}

async function stopRun() {
  await window.api.runStop();
  appendRunOutput(t('runStopped'));
  setRunning(false);
}

// ═══════════════════════════════════════════
//  出力タブの切り替え
// ═══════════════════════════════════════════

// ═══════════════════════════════════════════
//  演習ごとの出力タブ
//
//  静的ページの演習に SQL やメッセージングのタブが並んでいても迷うだけなので、
//  演習が持つ tabs (courses.js の exerciseTabs) に載っているタブだけを出す
//  演習から作っていないプロジェクトでは全部出す
// ═══════════════════════════════════════════

const RUN_TAB_IDS = {
  output: 'run-tab-output', tests: 'run-tab-tests', sql: 'run-tab-sql',
  terminal: 'run-tab-terminal', messaging: 'run-tab-messaging', preview: 'run-tab-browser',
};

function applyExerciseTabs(exercise) {
  const tabs = new Set(exercise?.tabs?.length ? exercise.tabs : Object.keys(RUN_TAB_IDS));
  tabs.add('output');
  for (const [tab, id] of Object.entries(RUN_TAB_IDS)) {
    $(id).classList.toggle('hidden', !tabs.has(tab));
  }
  // 見えなくなったタブを開いたままにしない
  const active = document.querySelector('.run-tab.active');
  if (active?.classList.contains('hidden')) showRunPane('tab-result');
}

function showRunPane(paneId, { reloadPreview = true } = {}) {
  // 演習では隠しているタブでも、結果を出す必要があれば出す
  // (ツリーの右クリックで .sql を流したとき など)
  document.querySelector(`.run-tab[data-pane="${paneId}"]`)?.classList.remove('hidden');
  document.querySelectorAll('.run-tab').forEach(tab =>
    tab.classList.toggle('active', tab.dataset.pane === paneId));
  document.querySelectorAll('.run-pane').forEach(pane =>
    pane.classList.toggle('active', pane.id === paneId));
  // 実行ログをチャットに渡すボタンは、実行結果タブでだけ意味がある

  if (paneId === 'tab-terminal') startTerminal();
  // 隠れているあいだに届いた出力のぶん、表示したときに末尾へ合わせる
  if (paneId === 'tab-result' && runOutputFollow) scrollRunOutputToEnd();
  if (paneId === 'tab-browser' && reloadPreview) fitPreview();
  if (paneId === 'tab-messaging') refreshMessaging();
}

let messagingStates = [];

async function refreshMessaging() {
  try { messagingStates = await window.api.messagingStatus(); renderMessaging(); }
  catch (err) { $('messaging-log').textContent = err.message; }
}

function renderMessaging() {
  $('messaging-services').innerHTML = messagingStates.map(s => {
    const idle = ['stopped', 'error'].includes(s.state);
    const name = s.id === 'kafka' ? 'Apache Kafka' : 'RabbitMQ';
    return `<div class="messaging-service" data-service="${s.id}">` +
      `<div class="messaging-service-title"><strong>${name} ${escapeHtml(s.version)}</strong>` +
      `<span class="messaging-state" data-state="${s.state}">${escapeHtml(t(s.available ? `messaging_${s.state}` : 'runtimeMissing'))}</span></div>` +
      `<code>${escapeHtml(s.endpoint)}</code>` +
      `<div class="messaging-actions">` +
      `<button class="btn" data-action="reset" ${!idle ? 'disabled' : ''}>${escapeHtml(t('messagingReset'))}</button>` +
      (s.managementUrl ? `<button class="btn" data-action="management" ${s.state !== 'running' ? 'disabled' : ''}>${escapeHtml(t('messagingManagement'))}</button>` : '') +
      '</div>' + (s.managementUrl ? `<small>${escapeHtml(t('messagingCredentials'))}</small>` : '') +
      (s.error ? `<p class="messaging-error">${escapeHtml(s.error)}</p>` : '') + '</div>';
  }).join('');
  renderMessagingLog();
  renderServiceControls();
}

function renderMessagingLog() {
  const pre = $('messaging-log');
  pre.textContent = messagingStates.find(s => s.id === $('messaging-log-select').value)?.log || '';
  pre.scrollTop = pre.scrollHeight;
}

async function messagingAction(event) {
  const button = event.target.closest('button[data-action]');
  if (!button || button.disabled) return;
  const id = button.closest('[data-service]').dataset.service;
  const action = button.dataset.action;
  if (action === 'management') {
    await window.api.openBrowser(messagingStates.find(s => s.id === id).managementUrl);
    return;
  }
  if (action === 'reset' && !await confirmDialog(tf('messagingConfirmReset', { name: id === 'kafka' ? 'Kafka' : 'RabbitMQ' }))) return;
  button.disabled = true;
  await callMessaging(id, action);
}

async function callMessaging(id, action) {
  try {
    const call = { start: 'messagingStart', stop: 'messagingStop', reset: 'messagingReset' }[action];
    const result = await window.api[call](id);
    if (!result.ok) await alertDialog(result.error);
  } catch (err) { await alertDialog(err.message); }
  await refreshMessaging();
}

// ═══════════════════════════════════════════
//  サーバーの起動・停止 (出力タブの行の右端)
//
//  HSQLDB も Kafka / RabbitMQ も customer-hub のような付き添いのプロセスも
//  「実行」で自動的に起動するので、起動を先に押させない。
//  ここは手で止めたい・先に起こしておきたいときの口で、演習が使うサーバー
//  (SQL の実行対象がある / codinable.services.json に書いてある) と、
//  いま動いているサーバーだけを出す (動いているものは止められるようにしておく)
// ═══════════════════════════════════════════

const SERVICE_NAMES = { kafka: 'Kafka', rabbitmq: 'RabbitMQ' };
let companionStates = [];   // [{ id, port, projectDir, state }]

function renderServiceControls() {
  const live = state => ['running', 'starting', 'stopping'].includes(state);
  const groups = [];

  const usesSql = (projectInfo?.runnableFiles || []).some(f => f.kind === 'sql');
  if (usesSql || sqlState !== 'stopped') {
    groups.push({ id: 'hsqldb', name: t('serviceDb'), title: 'HSQLDB', state: sqlState, available: true });
  }
  for (const s of messagingStates) {
    if (!(projectInfo?.services || []).includes(s.id) && !live(s.state)) continue;
    groups.push({ id: s.id, name: SERVICE_NAMES[s.id], title: `${SERVICE_NAMES[s.id]} ${s.version || ''}`.trim(),
                  state: s.state, available: s.available });
  }
  const declared = projectInfo?.processes || [];
  const liveCompanions = companionStates.filter(c => live(c.state));
  for (const id of new Set([...declared.map(p => p.id), ...liveCompanions.map(c => c.id)])) {
    const state = companionStates.find(c => c.id === id)?.state || 'stopped';
    const port = declared.find(p => p.id === id)?.port || companionStates.find(c => c.id === id)?.port;
    // 宣言していないプロジェクトからは起こせない (止めるだけ)
    groups.push({ id, kind: 'companion', name: id, title: `${id} :${port}`, state,
                  available: declared.some(p => p.id === id) || live(state) });
  }

  $('service-controls').innerHTML = groups.map(g => {
    const stateText = t(g.available ? `messaging_${g.state}` : 'runtimeMissing');
    const canStart = g.available && ['stopped', 'error'].includes(g.state);
    // Kafka / RabbitMQ は起動待ちの途中でも止められる (HSQLDB は起動が一瞬なので待つ)
    const canStop = g.state === 'running' || (g.state === 'starting' && g.id !== 'hsqldb');
    return `<span class="service-group" data-service="${g.id}" data-kind="${g.kind || ''}" title="${escapeHtml(`${g.title}: ${stateText}`)}">` +
      `<span class="service-dot" data-state="${escapeHtml(g.state)}"></span>` +
      `<button class="btn btn-run" data-action="start" ${canStart ? '' : 'disabled'}>` +
        `${escapeHtml(tf('serviceStart', { name: g.name }))}</button>` +
      `<button class="btn btn-stop" data-action="stop" ${canStop ? '' : 'disabled'}>` +
        `${escapeHtml(tf('serviceStop', { name: g.name }))}</button>` +
      '</span>';
  }).join('');
}

async function serviceAction(event) {
  const button = event.target.closest('button[data-action]');
  if (!button || button.disabled) return;
  const id = button.closest('[data-service]').dataset.service;
  const action = button.dataset.action;
  button.disabled = true;
  if (id === 'hsqldb') {
    if (action === 'start') await startSql(); else await stopSql();
    return;
  }
  if (button.closest('[data-kind="companion"]')) {
    const result = action === 'start' ? await window.api.companionStart(project, id) : await window.api.companionStop(id);
    if (!result.ok) await alertDialog(result.error);
    return;
  }
  await callMessaging(id, action);
}

// 付き添いのプロセスの出力は、行の頭に [id] を付けて実行結果へ流す
const companionAtLineStart = {};
function appendCompanionOutput(id, text) {
  let out = '';
  for (const ch of text.split(/(?<=\n)/)) {
    if (companionAtLineStart[id] !== false) out += `[${id}] `;
    out += ch;
    companionAtLineStart[id] = ch.endsWith('\n');
  }
  appendRunOutput(out);
}

// ═══════════════════════════════════════════
//  テスト結果 (JUnit XML + JaCoCo)
// ═══════════════════════════════════════════

let coverage = null;

function clearTestResults() {
  coverage = null;
  clearEditorCoverage();
  $('test-results-wrap').innerHTML =
    `<div class="test-results-placeholder">${escapeHtml(t('testResultsPlaceholder'))}</div>`;
}

function renderTestResultsRunning() {
  $('test-results-wrap').innerHTML =
    `<div class="test-results-placeholder">${escapeHtml(t('testsRunning'))}</div>`;
}

/** 「expected: <X> but was: <Y>」から期待値と実測値を取り出す */
function parseExpectedActual(message) {
  const m = /expected:\s*(.+?)\s+but was:\s*(.+)/s.exec(message || '');
  if (!m) return null;
  const strip = s => {
    const trimmed = s.trim();
    const inner = /^<([\s\S]*)>$/.exec(trimmed);
    return inner ? inner[1] : trimmed;
  };
  return { expected: strip(m[1]), actual: strip(m[2]) };
}

function renderTestResults(data) {
  const wrap = $('test-results-wrap');
  wrap.innerHTML = '';
  coverage = data?.coverage || null;

  const tests = data?.tests || [];
  if (!tests.length) {
    wrap.innerHTML = `<div class="test-results-placeholder">${escapeHtml(t('testsNoResults'))}</div>`;
    return;
  }

  const s = data.summary;
  const allGreen = s.failed === 0;
  const covTotal = coverage ? coverage.covered + coverage.missed : 0;
  const covPct   = covTotal > 0 ? Math.round((100 * coverage.covered) / covTotal) : null;

  const summary = document.createElement('div');
  summary.className = 'tr-summary';
  summary.innerHTML =
    `<span class="tr-verdict ${allGreen ? 'pass' : 'fail'}">` +
      (allGreen ? `✅ ${escapeHtml(t('testsAllPassed'))}`
                : `❌ ${escapeHtml(tf('testsFailedCount', { n: s.failed }))}`) +
    '</span>' +
    `<span class="tr-chip pass">✔ ${s.passed}</span>` +
    `<span class="tr-chip fail">✘ ${s.failed}</span>` +
    (s.skipped ? `<span class="tr-chip skip">⏭ ${s.skipped}</span>` : '') +
    `<span class="tr-chip time">⏱ ${(s.time || 0).toFixed(2)}s</span>` +
    (covPct !== null
      ? `<span class="tr-chip cov">🎯 ${escapeHtml(t('testsCoverage'))} ${covPct}%</span>` : '');
  wrap.appendChild(summary);

  // Eclipse 風のグリーンバー / レッドバー
  const bar = document.createElement('div');
  bar.className = 'tr-bar';
  const pct = s.total ? Math.round((100 * s.passed) / s.total) : 0;
  bar.innerHTML = `<div class="tr-bar-fill ${allGreen ? 'pass' : 'fail'}" ` +
                  `style="width:${allGreen ? 100 : Math.max(pct, 4)}%"></div>`;
  wrap.appendChild(bar);

  const byClass = new Map();
  for (const test of tests) {
    const key = test.classname || '(default)';
    if (!byClass.has(key)) byClass.set(key, []);
    byClass.get(key).push(test);
  }

  for (const [className, cases] of byClass) {
    const group = document.createElement('div');
    group.className = 'tr-class';
    const failed = cases.filter(c => c.status === 'failed').length;

    const head = document.createElement('div');
    head.className = 'tr-class-header' + (failed ? ' has-fail' : '');
    head.innerHTML =
      `<span class="tr-class-icon">${failed ? '❌' : '✅'}</span>` +
      `<span class="tr-class-name">${escapeHtml(className)}</span>` +
      `<span class="tr-class-stat">${cases.length - failed}/${cases.length}</span>`;
    group.appendChild(head);

    for (const test of cases) {
      const row = document.createElement('div');
      row.className = `tr-case ${test.status}`;
      const icon = test.status === 'passed' ? '✅' : test.status === 'failed' ? '❌' : '⏭';
      row.innerHTML =
        `<span class="tr-case-icon">${icon}</span>` +
        `<span class="tr-case-name">${escapeHtml(test.name)}</span>` +
        `<span class="tr-case-time">${(test.time || 0).toFixed(3)}s</span>`;
      group.appendChild(row);

      if (test.status === 'failed') {
        const detail = document.createElement('div');
        // 失敗の内容は最初から開いておく (まず読んでほしいので畳まない)
        detail.className = 'tr-fail-detail open';
        const ea = parseExpectedActual(test.message);
        detail.innerHTML =
          (test.message ? `<div class="tr-msg">${escapeHtml(test.message)}</div>` : '') +
          (ea
            ? '<div class="tr-ea">' +
                `<span class="tr-ea-exp">${escapeHtml(t('testsExpected'))}: ` +
                  `<code>${escapeHtml(ea.expected)}</code></span>` +
                `<span class="tr-ea-act">${escapeHtml(t('testsActual'))}: ` +
                  `<code>${escapeHtml(ea.actual)}</code></span>` +
              '</div>'
            : '') +
          (test.type ? `<div class="tr-type">${escapeHtml(test.type)}</div>` : '') +
          (test.detail
            ? `<details class="tr-stack"><summary>${escapeHtml(t('testsStackTrace'))}</summary>` +
              `<pre>${escapeHtml(test.detail)}</pre></details>`
            : '');
        row.classList.add('clickable');
        row.addEventListener('click', () => detail.classList.toggle('open'));
        group.appendChild(detail);
      }
    }
    wrap.appendChild(group);
  }

  if (coverage && Object.keys(coverage.files).length) {
    const covWrap = document.createElement('div');
    covWrap.className = 'tr-cov-files';
    covWrap.innerHTML =
      `<div class="tr-cov-title">🎯 ${escapeHtml(t('testsCoverageByFile'))}</div>` +
      `<div class="tr-cov-legend">${escapeHtml(t('testsCovLegend'))}</div>`;
    for (const [key, entry] of Object.entries(coverage.files)) {
      const total = entry.missed + entry.covered;
      const filePct = total ? Math.round((100 * entry.covered) / total) : 0;
      const row = document.createElement('div');
      row.className = 'tr-cov-file';
      row.title = t('testsCoverageOpenHint');
      row.innerHTML =
        `<span class="tr-cov-name">${escapeHtml(key)}</span>` +
        `<span class="tr-cov-barwrap"><span class="tr-cov-barfill" ` +
          `style="width:${filePct}%"></span></span>` +
        `<span class="tr-cov-pct">${filePct}%</span>`;
      row.addEventListener('click', () => openCoverageFile(key));
      covWrap.appendChild(row);
    }
    wrap.appendChild(covWrap);
  }

  applyCoverageToEditor();
}

/** JaCoCo のキー (pkg/path/Foo.java) に対応するファイルをツリーから探して開く */
function openCoverageFile(key) {
  const hit = treeEntries.find(e => !e.dir && e.path.endsWith(`/${key}`));
  if (hit) openFile(hit.path);
}

// ── エディタの行カバレッジ (緑 = 実行 / 黄 = 分岐の一部 / 赤 = 未実行) ──

const COV_CLASSES = ['cm-cov-full', 'cm-cov-part', 'cm-cov-miss'];

function clearEditorCoverage() {
  for (const entry of openFiles.values()) {
    const lines = entry.cm.lineCount();
    entry.cm.operation(() => {
      for (let i = 0; i < lines; i++) {
        for (const cls of COV_CLASSES) entry.cm.removeLineClass(i, 'background', cls);
      }
    });
  }
}

function applyCoverageToEditor() {
  if (!coverage || !activeFile) return;
  const entry = openFiles.get(activeFile);
  if (!entry) return;

  const key = Object.keys(coverage.files).find(k => activeFile.endsWith(`/${k}`));
  if (!key) return;

  const lines = coverage.files[key].lines;
  entry.cm.operation(() => {
    for (const [lineNo, counters] of Object.entries(lines)) {
      const line = parseInt(lineNo, 10) - 1;
      if (line < 0 || line >= entry.cm.lineCount()) continue;
      const cls = counters.ci === 0 && counters.mi > 0 ? 'cm-cov-miss'
                : counters.mb > 0                      ? 'cm-cov-part'
                : 'cm-cov-full';
      entry.cm.addLineClass(line, 'background', cls);
    }
  });
}

// ═══════════════════════════════════════════
//  SQL (HSQLDB インメモリ)
// ═══════════════════════════════════════════

// stopped / starting / running / stopping (表示はメッセージングと同じ語を使う)
let sqlState = 'stopped';
// 演習ごとの初期化 SQL (テーブルを作り直して初期データを入れる)。ファイルを流す前に毎回流す
const SQL_RESET_FILE = 'reset.sql';
// いまの DB の中身がどのプロジェクトの reset.sql から始まっているか (null = 分からない)
let sqlPreparedFor = null;

function setSqlState(state) {
  sqlState = state;
  renderServiceControls();
}

function setSqlMessage(html, className = 'sql-message') {
  $('sql-result-wrap').innerHTML = `<div class="${className}">${html}</div>`;
}

async function startSql() {
  if (sqlState === 'running') return;
  sqlPreparedFor = null;
  setSqlState('starting');
  setSqlMessage(escapeHtml(t('sqlStarting')));
  const res = await window.api.sqlStart('');
  if (!res.ok) {
    setSqlState('stopped');
    setSqlMessage(escapeHtml(res.error || ''), 'sql-error');
    return;
  }
  setSqlState('running');
  setSqlMessage(escapeHtml(t('sqlStarted')));
}

async function stopSql() {
  setSqlState('stopping');
  sqlPreparedFor = null;
  await window.api.sqlStop();
  setSqlState('stopped');
  setSqlMessage(escapeHtml(t('sqlStopped')));
}

/** エディタの選択範囲、なければファイル全体を SQL として取り出す */
function currentSqlText() {
  const cm = activeEditor();
  if (!cm) return '';
  const state = cm.view.state;
  const range = state.selection.main;
  if (!range.empty) return state.sliceDoc(range.from, range.to);
  return cm.getValue();
}

/**
 * 「実行」ボタンから SQL を流す
 *
 * 対象ファイルをエディタで開いてから、その内容 (選択範囲があればそこだけ) を
 * HSQLDB へ流す。DB が止まっていれば起こすので、編集 → 実行がボタン 1 つで回る
 */
async function runSqlFromEditor(relPath) {
  // 何が流れたのかが見えるように、対象は必ず画面に出しておく
  if (relPath) await openFile(relPath);
  for (const openPath of openFiles.keys()) await saveFile(openPath);
  showRunPane('tab-sql');

  const entry = relPath ? openFiles.get(relPath) : null;
  const whole = relPath && relPath !== activeFile && entry;
  const selected = !whole && !!activeEditor() && !activeEditor().view.state.selection.main.empty;
  const sql = (whole ? entry.cm.getValue() : currentSqlText()).trim();
  if (!sql) { setSqlMessage(escapeHtml(t('sqlNoSql')), 'sql-error'); return; }

  // ファイルを流すときは、その演習の reset.sql で毎回初期状態に戻してから流す
  // (更新系の SQL を流すたびにデータが変わり、2 回目で主キー違反になる、を起こさない)
  // 選択範囲だけを流すときは戻さない (1 文ずつ順に試せるように)。ただし別の演習の
  // 状態が残っているとき (演習を切り替えた直後・DB を起こし直した直後) は戻す
  const reset = await window.api.wsReadFile(project, SQL_RESET_FILE);
  const doReset = reset.ok && (!selected || sqlPreparedFor !== project);
  let note = '';
  if (doReset) {
    setSqlState('starting');
    // 動いていれば PUBLIC スキーマを消して流し直し、止まっていれば起こしてから流す
    const res = await window.api.sqlStart(reset.content);
    if (!res.ok) {
      setSqlState(res.running ? 'running' : 'stopped');
      sqlPreparedFor = null;
      setSqlMessage(escapeHtml(tf('sqlResetFailed', { file: SQL_RESET_FILE, error: res.error || '' })), 'sql-error');
      return;
    }
    setSqlState('running');
    sqlPreparedFor = project;
    note = tf('sqlResetDone', { file: SQL_RESET_FILE });
  } else if (sqlState !== 'running') {
    await startSql();
    if (sqlState !== 'running') return;   // 起動に失敗した (理由は startSql が出している)
  }
  renderSqlResult(await window.api.sqlRun(sql), note);
}

function renderSqlResult(res, note = '') {
  const noteHtml = note ? `<div class="sql-note">${escapeHtml(note)}</div>` : '';
  if (!res || res.error) {
    // 何文目で止まったかを出す (それより前の文は流れている)
    const where = res?.statementIndex
      ? `<div class="sql-error-where">${escapeHtml(tf('sqlErrorAt', { n: res.statementIndex }))}</div>` +
        `<pre class="sql-error-statement">${escapeHtml(res.statement || '')}</pre>`
      : '';
    $('sql-result-wrap').innerHTML = noteHtml +
      `<div class="sql-error">${where}${escapeHtml(res?.error || '')}</div>`;
    return;
  }
  // SELECT 以外 (INSERT / UPDATE / DDL) は更新件数だけが返る
  if (!Array.isArray(res.columns) || !res.columns.length) {
    const message = typeof res.affected === 'number' && res.affected >= 0
      ? tf('sqlUpdated', { n: res.affected })
      : t('sqlOk');
    $('sql-result-wrap').innerHTML = noteHtml + `<div class="sql-message">${escapeHtml(message)}</div>`;
    return;
  }

  const rows = res.rows || [];
  const head = res.columns.map(c => `<th>${escapeHtml(c)}</th>`).join('');
  const body = rows.map(row =>
    `<tr>${row.map(cell => `<td>${cell === null || cell === undefined
      ? `<em>${escapeHtml(t('sqlNull'))}</em>` : escapeHtml(cell)}</td>`).join('')}</tr>`).join('');

  $('sql-result-wrap').innerHTML = noteHtml +
    `<div class="sql-rowcount">${escapeHtml(tf('sqlRowCount', { n: rows.length }))}</div>` +
    `<div class="sql-table-wrap"><table class="sql-table">` +
      `<thead><tr>${head}</tr></thead><tbody>${body}</tbody></table></div>`;
}

// ═══════════════════════════════════════════
//  ターミナル (node-pty + xterm.js)
// ═══════════════════════════════════════════

// ANSI 16 色はテーマごとに持つ (元になった VS Code テーマの terminal.ansi* と同じ値)
const XTERM_PALETTES = {
  'github-light': {
    black: '#24292f', red: '#cf222e', green: '#116329', yellow: '#4d2d00', blue: '#0969da',
    magenta: '#8250df', cyan: '#1b7c83', white: '#6e7781', brightBlack: '#57606a',
    brightRed: '#a40e26', brightGreen: '#1a7f37', brightYellow: '#633c01',
    brightBlue: '#218bff', brightMagenta: '#a475f9', brightCyan: '#3192aa', brightWhite: '#8c959f',
  },
  'github-dark': {
    black: '#484f58', red: '#ff7b72', green: '#3fb950', yellow: '#d29922', blue: '#58a6ff',
    magenta: '#bc8cff', cyan: '#39c5cf', white: '#b1bac4', brightBlack: '#6e7681',
    brightRed: '#ffa198', brightGreen: '#56d364', brightYellow: '#e3b341',
    brightBlue: '#79c0ff', brightMagenta: '#d2a8ff', brightCyan: '#56d4dd', brightWhite: '#f0f6fc',
  },
  'one-dark-pro': {
    black: '#3f4451', red: '#e05561', green: '#8cc265', yellow: '#d18f52', blue: '#4aa5f0',
    magenta: '#c162de', cyan: '#42b3c2', white: '#d7dae0', brightBlack: '#4f5666',
    brightRed: '#ff616e', brightGreen: '#a5e075', brightYellow: '#f0a45d',
    brightBlue: '#4dc4ff', brightMagenta: '#de73ff', brightCyan: '#4cd1e0', brightWhite: '#e6e6e6',
  },
  'tokyo-night': {
    black: '#15161e', red: '#f7768e', green: '#9ece6a', yellow: '#e0af68', blue: '#7aa2f7',
    magenta: '#bb9af7', cyan: '#7dcfff', white: '#a9b1d6', brightBlack: '#414868',
    brightRed: '#f7768e', brightGreen: '#9ece6a', brightYellow: '#e0af68',
    brightBlue: '#7aa2f7', brightMagenta: '#bb9af7', brightCyan: '#7dcfff', brightWhite: '#c0caf5',
  },
  dracula: {
    black: '#21222c', red: '#ff5555', green: '#50fa7b', yellow: '#f1fa8c', blue: '#bd93f9',
    magenta: '#ff79c6', cyan: '#8be9fd', white: '#f8f8f2', brightBlack: '#6272a4',
    brightRed: '#ff6e6e', brightGreen: '#69ff94', brightYellow: '#ffffa5',
    brightBlue: '#d6acff', brightMagenta: '#ff92df', brightCyan: '#a4ffff', brightWhite: '#ffffff',
  },
  'solarized-light': {
    black: '#073642', red: '#dc322f', green: '#859900', yellow: '#b58900', blue: '#268bd2',
    magenta: '#d33682', cyan: '#2aa198', white: '#eee8d5', brightBlack: '#002b36',
    brightRed: '#cb4b16', brightGreen: '#586e75', brightYellow: '#657b83',
    brightBlue: '#839496', brightMagenta: '#6c71c4', brightCyan: '#93a1a1', brightWhite: '#fdf6e3',
  },
};

let xterm    = null;
let xtermFit = null;

function xtermThemeFromCss() {
  // テーマの CSS 変数は body[data-theme] に定義されているので body から読む
  const style = getComputedStyle(document.body);
  const read = name => style.getPropertyValue(name).trim();
  const bg = read('--bg-base')  || '#0d1117';
  const fg = read('--text-main') || '#e6edf3';
  const def = resolveTheme(document.body.dataset.theme);
  return {
    background: bg, foreground: fg, cursor: fg, cursorAccent: bg,
    selectionBackground: `${read('--accent-blue') || '#2f81f7'}55`,
    ...(XTERM_PALETTES[def.id] || XTERM_PALETTES[def.dark ? 'github-dark' : 'github-light']),
  };
}

function xtermFontFromCss() {
  const style = getComputedStyle(document.documentElement);
  const size  = parseFloat(style.getPropertyValue('--editor-font-size'));
  return {
    fontFamily: style.getPropertyValue('--editor-font-family').trim() ||
                "'JetBrains Mono', Consolas, monospace",
    fontSize:   Number.isFinite(size) && size > 0 ? size : 13,
  };
}

function applyTerminalTheme() {
  if (xterm) xterm.options.theme = xtermThemeFromCss();
}

function applyTerminalFont() {
  if (!xterm) return;
  const font = xtermFontFromCss();
  xterm.options.fontFamily = font.fontFamily;
  xterm.options.fontSize   = font.fontSize;
  fitTerminal();
}

function fitTerminal() {
  if (!xtermFit) return;
  try { xtermFit.fit(); } catch { /* 表示されていないときは測れない */ }
}

async function startTerminal() {
  if (!project) {
    if (xterm) xterm.write(t('termNoProject'));
    return;
  }
  if (!xterm) {
    if (!window.Terminal) return;
    xterm = new window.Terminal({
      ...xtermFontFromCss(),
      cursorBlink: true,
      scrollback: 5000,
      // xterm は自前のスクロールバーを描くので、幅は他の UI と同じ --scrollbar-size にそろえる
      overviewRuler: { width: parseInt(getComputedStyle(document.documentElement).getPropertyValue('--scrollbar-size'), 10) || 12 },
      theme: xtermThemeFromCss(),
    });
    xtermFit = new window.FitAddon.FitAddon();
    xterm.loadAddon(xtermFit);
    xterm.open($('xterm-wrap'));
    // キー入力はそのまま PTY へ渡す (Ctrl+C も SIGINT として届く)
    xterm.onData(data => window.api.termInput(data));
    xterm.onResize(({ cols, rows }) => window.api.termResize({ cols, rows }));
    if (window.ResizeObserver) {
      const refit = debounce(() => fitTerminal(), 80);
      new ResizeObserver(refit).observe($('xterm-wrap'));
    }
  }

  fitTerminal();
  await window.api.termStart({ cols: xterm.cols, rows: xterm.rows, project });
}

/** プロジェクトを切り替えたとき、ターミナルタブを見ていれば開き直す */
function restartTerminalIfVisible() {
  if (!xterm) return;
  xterm.clear();
  if ($('tab-terminal').classList.contains('active')) startTerminal();
  else window.api.termStop();
}

// ═══════════════════════════════════════════
//  Web プレビュー
// ═══════════════════════════════════════════

// プレビューできる先があるか
// 「ボタンはあるが押しても何も起きない」を無くすため、実際に待ち受けている
// サーバーが見つかるまではプレビューを触れない状態にしておく
let previewAvailable = false;

function setPreviewAvailable(state) {
  previewAvailable = !!state;
  $('run-tab-browser').disabled = !previewAvailable;
  if (!previewAvailable) {
    // <webview> の src は触らない (about:blank を入れ直すと ERR_ABORTED になる)
    // 触れないタブなので、次にプレビューできたとき previewUrl が入れ替える
    $('browser-url').value = '';
    // 見えなくなるタブを開いたままにしない
    if (document.querySelector('.run-tab.active')?.id === 'run-tab-browser') {
      showRunPane('tab-result');
    }
  }
}

/**
 * 実行が終わったあとにプレビューの可否を見直す
 * 静的ページの内蔵サーバーは実行プロセスとは別に生き続けるので、
 * プロセスが終わったことだけを理由に落とさない
 */
async function refreshPreviewAvailability() {
  const status = await window.api.previewStatus();
  setPreviewAvailable(!!(status.ok && status.url));
}

// <webview> の中身 (guest) が用意できたか。用意できる前は loadURL を呼べない
let previewReady = false;

function previewUrl(url) {
  $('browser-url').value = url;
  setPreviewAvailable(true);
  // 先にタブを出してから読み込ませる
  // 非表示のあいだに src を差し替えて直後に reload すると、読み込みが
  // about:blank の ERR_ABORTED で打ち切られてプレビューが真っ白のままになる
  showRunPane('tab-browser', { reloadPreview: false });
  const view = $('mini-browser');
  if (previewReady) view.loadURL(url).catch(() => { /* 別の読み込みで上書きされた */ });
  else view.setAttribute('src', url);
}

/**
 * 演習が preview にパスを書いていれば、検知した URL のその場所を開く
 * (/ に何も無いアプリで、プレビューが 404 から始まらないように)
 */
function withExercisePreviewPath(url) {
  const path = exerciseForProject()?.preview;
  if (!path) return url;
  try {
    const u = new URL(url);
    if (u.pathname !== '/' || u.search) return url;   // アプリ自身が場所を示したときはそちらを使う
    return u.origin + path;
  } catch { return url; }
}

function fitPreview() {
  // <webview> は非表示のあいだ 0x0 で描画されるため、表示時に読み直す
  if (!previewReady) return;
  const view = $('mini-browser');
  try {
    if (view.getURL() && view.getURL() !== 'about:blank') view.reload();
  } catch { /* まだ読み込まれていない */ }
}

/** 静的ページを内蔵サーバーで配信してプレビューする */
async function servePreview(relDir) {
  if (!project) { await alertDialog(t('runNoProject')); return; }
  const res = await window.api.previewServe(project, relDir || '');
  if (!res.ok) {
    await alertDialog(tf('previewServeFailed', { error: res.error || '' }));
    return;
  }
  previewUrl(res.url);
}


// ═══════════════════════════════════════════
//  言語サーバー (Java / jdtls)
// ═══════════════════════════════════════════

let lsp = null;

function setLspBadge(status) {
  const badge = $('lsp-badge');
  badge.classList.remove('hidden', 'starting', 'ready', 'error');
  if (!status || status === 'off') { badge.classList.add('hidden'); return; }
  badge.classList.add(status);
  badge.title = status === 'ready' ? t('lspReady')
              : status === 'error' ? t('lspError')
              : t('lspStarting');
}

function disposeLsp() {
  if (lsp) { lsp.dispose(); lsp = null; }
  setLspBadge('off');
}

function startLspIfNeeded() {
  disposeLsp();
  if (!project || !appInfo?.lspAvailable) return;
  const kinds = projectInfo?.kinds || [];
  if (!kinds.includes('java') && !kinds.includes('gradle')) return;

  lsp = CM6.createLspConnection({
    language: 'java',
    project,
    onStatus: status => {
      setLspBadge(status);
      // 接続できた時点で、すでに開いている Java ファイルへ後付けする
      if (status === 'ready') {
        for (const [relPath, entry] of openFiles) attachLspTo(relPath, entry);
      }
    },
  });
}

function attachLspTo(relPath, entry) {
  if (!lsp || !lsp.isReady()) return;
  const languageId = entry.cm.lspLanguageId();
  if (!languageId) return;
  entry.cm.setLspExtension(lsp.pluginFor(relPath, languageId));
}

// ═══════════════════════════════════════════
//  LLM チャット (BYOK / 任意機能)
// ═══════════════════════════════════════════

let chatMessages  = [];     // [{ role, content }] API へ送る履歴
let chatStreaming = false;
let chatBubble    = null;   // ストリーミング中の吹き出し
let chatBuffer    = '';
let chatMode      = 'ask';  // 'ask' (読むだけ) / 'agent' (書き換えと実行までする)
let agentCard     = null;   // Agent の経過を出しているカード

function currentModel() {
  const id = appInfo?.llmSelection?.modelId;
  return appInfo?.llmModels?.find(m => m.id === id) || appInfo?.llmModels?.[0] || null;
}

function updateLlmBadge() {
  const model = currentModel();
  const badge = $('llm-key-badge');
  const has   = model ? !!appInfo?.apiKeyStatus?.[model.keyField] : false;
  badge.textContent = t(has ? 'apiKeySet' : 'apiKeyUnset');
  badge.classList.toggle('set', has);
  badge.classList.toggle('unset', !has);
  $('chat-title').textContent = model ? `🤖 ${model.label}` : t('chatTitle');
}

function addChatMessage(role, content, { streaming = false } = {}) {
  const history = $('chat-history');
  history.querySelector('.chat-welcome')?.remove();

  const model = currentModel();
  const msg = document.createElement('div');
  msg.className = `chat-msg ${role}`;
  const label = role === 'user' ? t('roleUser')
                                : tf('roleAssistant', { model: model?.label || 'AI' });
  msg.innerHTML =
    `<div class="chat-role-label">${escapeHtml(label)}</div>` +
    `<div class="chat-bubble${role === 'user' ? '' : ' md-body'}${streaming ? ' streaming' : ''}"></div>`;
  const bubble = msg.querySelector('.chat-bubble');
  // ユーザーの発言は入力どおり、AI の応答は Markdown として描く
  if (role === 'user') bubble.textContent = content;
  else {
    bubble.innerHTML = renderMarkdown(content);
    if (!streaming) addCopyButtons(bubble, content);
  }

  history.appendChild(msg);
  history.scrollTop = history.scrollHeight;
  return bubble;
}

/**
 * 送信中は、送信ボタンそのものを「中断」に変える (回転する印つき)
 * 別の場所に中断ボタンを出すと、押す場所を探させることになるため
 */
function setChatStreaming(state) {
  document.querySelectorAll('input[name="chat-mode"]').forEach(input => { input.disabled = state; });
  chatStreaming = state;
  const btn = $('btn-chat-send');
  btn.classList.toggle('is-streaming', state);
  btn.innerHTML = state
    ? `<span class="btn-spinner" aria-hidden="true"></span>${escapeHtml(t('btnAbort'))}`
    : escapeHtml(t('btnSend'));
  btn.title = t(state ? 'btnAbortTitle' : 'btnSendTitle');
}

function abortChat() {
  if (chatMode === 'agent') window.api.agentAbort();
  else window.api.chatAbort();
}

// ── コピー ─────────────────────────────────────────────────
//
// AI の応答は丸ごと (Markdown のまま)、コードブロックは中身だけをコピーできるようにする

async function copyText(text, button) {
  try {
    await navigator.clipboard.writeText(text);
  } catch {
    // クリップボード API が使えないときの保険
    const area = document.createElement('textarea');
    area.value = text;
    document.body.appendChild(area);
    area.select();
    document.execCommand('copy');
    area.remove();
  }
  if (!button) return;
  const label = button.textContent;
  button.textContent = t('copied');
  button.classList.add('copied');
  setTimeout(() => { button.textContent = label; button.classList.remove('copied'); }, 1400);
}

function makeCopyButton(getText, className) {
  const btn = document.createElement('button');
  btn.type = 'button';
  btn.className = className;
  btn.textContent = t('btnCopy');
  btn.title = t('btnCopyTitle');
  btn.addEventListener('click', ev => { ev.stopPropagation(); copyText(getText(), btn); });
  return btn;
}

/** 応答が出そろったあとに、応答全体とコードブロックのコピーボタンを付ける */
function addCopyButtons(bubble, rawText) {
  if (!bubble || !String(rawText || '').trim()) return;
  for (const pre of bubble.querySelectorAll('pre')) {
    if (pre.querySelector('.chat-code-copy')) continue;
    pre.appendChild(makeCopyButton(() => pre.querySelector('code')?.innerText ?? pre.innerText,
                                   'chat-code-copy'));
  }
  const msg = bubble.closest('.chat-msg');
  if (msg && !msg.querySelector('.chat-copy')) {
    msg.appendChild(makeCopyButton(() => rawText, 'chat-copy'));
  }
}

// ── Ask / Agent の切り替え ─────────────────────────────────
//
// Ask は読むだけ。Agent はファイルを書き換える (実行は受講者が行う)
// どちらを使うかは覚えておく (毎回選び直させない)

function setChatMode(mode, { persist = true } = {}) {
  chatMode = mode === 'agent' ? 'agent' : 'ask';
  for (const input of document.querySelectorAll('input[name="chat-mode"]')) {
    input.checked = input.value === chatMode;
  }
  $('chat-input').placeholder = t(chatMode === 'agent' ? 'chatInputAgent' : 'chatInputAsk');
  if (persist) window.api.setLlmSelection({ chatMode });
}

/**
 * 開いているプロジェクトのファイルを、送信のたびに集め直す
 *
 * 受講者に「どれを渡すか」を選ばせない。演習のファイルは短く、
 * 相談したい内容もプロジェクト全体にまたがるため、まとめて渡すほうが早い
 * 編集中の内容を渡すので、先に保存する
 */
async function collectProjectContext() {
  if (!project) return { files: [], skipped: 0 };
  for (const relPath of openFiles.keys()) await saveFile(relPath);

  const res = await window.api.wsProjectContext(project);
  const files = res.ok ? res.files : [];
  return { files, skipped: res.skipped || 0 };
}

async function sendChat() {
  const input = $('chat-input');
  const text  = input.value.trim();
  if (!text || chatStreaming) return;

  const model = currentModel();
  if (!model) return;
  if (!appInfo?.apiKeyStatus?.[model.keyField]) {
    await alertDialog(tf('chatNoKey', { label: model.keyLabel }));
    openSettings();
    return;
  }

  // Agent はプロジェクトのファイルを書き換えて実行する。対象が無いと何もできない
  if (chatMode === 'agent' && !project) { await alertDialog(t('agentNoProject')); return; }

  // どちらのモードでも、開いているプロジェクトの中身はそのまま渡す
  const { files } = await collectProjectContext();

  input.value = '';
  addChatMessage('user', text);
  chatMessages.push({ role: 'user', content: text });

  const context = {
    project,
    kinds: projectInfo?.kinds || [],
    files,
    // 直近の実行結果も自動で渡す (長ければ末尾だけ。llm/prompt.js)
    log:   runLog || null,
    courseName: currentCourse()?.name || null,
  };

  if (chatMode === 'agent') {
    startAgent(context);
    return;
  }

  chatBuffer = '';
  chatBubble = addChatMessage('assistant', '', { streaming: true });
  setChatStreaming(true);
  window.api.chatSend({ messages: chatMessages, context });
}

// ═══════════════════════════════════════════
//  Agent モード
//
//  道具 (ファイルを読む / 書く / 実行する) を使いながら、モデルが自分で
//  数手すすめる。何をしたかは経過カードに残し、書き換えは差分で見せて
//  「元に戻す」で戻せるようにする (勝手に消えたように見せないため)
// ═══════════════════════════════════════════

function startAgent(context) {
  chatBuffer = '';
  chatBubble = addChatMessage('assistant', '', { streaming: true });

  agentCard = document.createElement('div');
  agentCard.className = 'agent-steps';
  chatBubble.appendChild(agentCard);

  setChatStreaming(true);
  window.api.agentSend({ messages: chatMessages, context });
}

// Agent が持つ道具はこの 3 つだけ (実行はしない。main/agent.js の TOOLS と対応)
const TOOL_LABEL = {
  list_files: 'agentToolList',
  read_file:  'agentToolRead',
  write_file: 'agentToolWrite',
};

/** 道具 1 回分の行を出す (開始で出して、終了で結果を足す) */
function renderAgentTool({ name, input, state, output }) {
  if (!agentCard) return;

  if (state === 'start') {
    const row = document.createElement('div');
    row.className = 'agent-step running';
    row.dataset.tool = name;
    const arg = input?.path || input?.target || '';
    row.innerHTML =
      '<span class="agent-step-spinner">⏳</span>' +
      `<span class="agent-step-label">${escapeHtml(t(TOOL_LABEL[name] || 'agentToolOther'))}</span>` +
      (arg ? `<code class="agent-step-arg">${escapeHtml(arg)}</code>` : '');
    agentCard.appendChild(row);
    scrollChatToBottom();
    return;
  }

  const row = [...agentCard.querySelectorAll('.agent-step.running')].pop();
  if (!row) return;
  row.classList.remove('running');

  // 断られた (演習の外を触ろうとした等) ときは、その理由を出す
  // 書き換えの結果は差分カードで見えるので、ここには出さない
  const refused = /^(演習|生成物|絶対パス|パスが空|使えない道具|エラー)/.test(String(output || ''));
  row.querySelector('.agent-step-spinner').textContent = refused ? '⚠️' : '✓';
  if (refused) {
    row.classList.add('is-refused');
    const note = document.createElement('span');
    note.className = 'agent-step-note';
    note.textContent = String(output);
    row.appendChild(note);
  }
  scrollChatToBottom();
}

/** Agent が書き換えたファイルを差分カードで見せる (元に戻せる) */
function renderAgentEdit(edit) {
  if (!agentCard) return;

  const rows = edit.before === null
    ? String(edit.after).split('\n').map(text => ({ kind: 'add', text }))
    : diffLines(edit.before, edit.after);
  const added   = rows.filter(r => r.kind === 'add').length;
  const removed = rows.filter(r => r.kind === 'del').length;

  const diffHtml = collapseDiff(rows).map(row => row.kind === 'skip'
    ? `<div class="diff-row skip">${escapeHtml(tf('editDiffSkipped', { n: row.count }))}</div>`
    : `<div class="diff-row ${row.kind}"><span class="diff-mark">${DIFF_MARK[row.kind]}</span>` +
      `<span class="diff-text">${escapeHtml(row.text)}</span></div>`).join('');

  const card = document.createElement('div');
  card.className = 'edit-proposal is-applied';
  card.innerHTML =
    '<div class="edit-proposal-head">' +
      `<span class="edit-proposal-path">${escapeHtml(edit.path)}</span>` +
      `<span class="edit-proposal-stat">${edit.isNew ? escapeHtml(t('editNewFile')) + ' ' : ''}` +
        `<span class="diff-added">+${added}</span> <span class="diff-removed">-${removed}</span></span>` +
    '</div>' +
    `<div class="edit-diff">${diffHtml}</div>` +
    '<div class="edit-proposal-actions">' +
      `<span class="edit-proposal-done">${escapeHtml(t('editApplied'))}</span>` +
      `<button class="btn" data-act="undo">${escapeHtml(t('editUndo'))}</button>` +
    '</div>';

  const actions = card.querySelector('.edit-proposal-actions');
  card.querySelector('[data-act="undo"]').addEventListener('click', async () => {
    // 新規作成だったものは空にはできないので、消して元の状態へ戻す
    const res = edit.isNew
      ? await window.api.wsDeleteEntry(project, edit.path)
      : await window.api.wsWriteFile(project, edit.path, edit.before);
    if (!res.ok) { await alertDialog(tf('saveFailed', { error: res.error || '' })); return; }
    if (edit.isNew) await closeFile(edit.path);
    else await reopenFileFromDisk(edit.path);
    await reloadTree();
    actions.innerHTML = `<span class="edit-proposal-done">${escapeHtml(t('editUndone'))}</span>`;
  });

  agentCard.appendChild(card);
  scrollChatToBottom();

  // エディタに開いているものは、書き換わった内容に入れ替える
  reopenFileFromDisk(edit.path).then(() => reloadTree());
}

function scrollChatToBottom() {
  const history = $('chat-history');
  history.scrollTop = history.scrollHeight;
}

// ═══════════════════════════════════════════
//  差分の表示 (Agent が書き換えたものを見せる)
//
//  Agent はファイルを直接書き換える。勝手に消えたように見えないよう、
//  変更は必ず差分として出し、「元に戻す」で戻せるようにする
// ═══════════════════════════════════════════

/** 行単位の差分 (LCS)。戻りは [{ kind: 'keep'|'add'|'del', text }] */
function diffLines(before, after) {
  // 改行コードの違い (CRLF / LF) は差分として見せない
  const a = before.split(/\r?\n/);
  const b = after.split(/\r?\n/);

  // 教材のファイルは短いので素直な DP で足りる。念のため上限を置き、
  // 超えたときは「全置換」として見せる (計算で固まらせないため)
  if (a.length * b.length > 4_000_000) {
    return [...a.map(text => ({ kind: 'del', text })),
            ...b.map(text => ({ kind: 'add', text }))];
  }

  const lcs = Array.from({ length: a.length + 1 }, () => new Uint32Array(b.length + 1));
  for (let i = a.length - 1; i >= 0; i--) {
    for (let j = b.length - 1; j >= 0; j--) {
      lcs[i][j] = a[i] === b[j] ? lcs[i + 1][j + 1] + 1
                                : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
    }
  }

  const rows = [];
  let i = 0, j = 0;
  while (i < a.length && j < b.length) {
    if (a[i] === b[j])                     { rows.push({ kind: 'keep', text: a[i] }); i++; j++; }
    else if (lcs[i + 1][j] >= lcs[i][j + 1]) { rows.push({ kind: 'del', text: a[i] }); i++; }
    else                                     { rows.push({ kind: 'add', text: b[j] }); j++; }
  }
  while (i < a.length) rows.push({ kind: 'del', text: a[i++] });
  while (j < b.length) rows.push({ kind: 'add', text: b[j++] });
  return rows;
}

/** keep が続くところを畳んで、変更の周辺だけ見せる */
function collapseDiff(rows, context = 2) {
  const keep = new Array(rows.length).fill(false);
  rows.forEach((row, index) => {
    if (row.kind === 'keep') return;
    for (let k = index - context; k <= index + context; k++) {
      if (k >= 0 && k < rows.length) keep[k] = true;
    }
  });

  const out = [];
  let skipped = 0;
  rows.forEach((row, index) => {
    if (keep[index]) {
      if (skipped) { out.push({ kind: 'skip', count: skipped }); skipped = 0; }
      out.push(row);
    } else {
      skipped++;
    }
  });
  if (skipped) out.push({ kind: 'skip', count: skipped });
  return out;
}

const DIFF_MARK = { add: '+', del: '-', keep: ' ' };

/** 書き換えのあと、開いているタブを disk の内容に入れ替える */
async function reopenFileFromDisk(relPath, { closeIfMissing = false } = {}) {
  if (!openFiles.has(relPath)) { await openFile(relPath); return; }
  const res = await window.api.wsReadFile(project, relPath);
  if (!res.ok) {
    if (closeIfMissing) {
      openFiles.get(relPath).dirty = false;
      await closeFile(relPath);
    }
    return;
  }
  const entry = openFiles.get(relPath);
  entry.cm.setValue(String(res.content ?? ''));
  entry.dirty = false;
  if (relPath === activeFile && mdPreviewOn) $('md-preview').innerHTML = renderMarkdown(entry.cm.getValue());
  renderTabs();
}

function clearChat() {
  chatMessages = [];
  $('chat-history').innerHTML =
    '<div class="chat-welcome">' +
    `<p>${escapeHtml(t('chatWelcome1'))}</p>` +
    `<p>${escapeHtml(t('chatWelcome2'))}</p>` +
    `<p>${escapeHtml(t('chatWelcome3'))}</p>` +
    '</div>';
}

// ═══════════════════════════════════════════
//  設定ダイアログ
// ═══════════════════════════════════════════

// ═══════════════════════════════════════════
//  アプリ本体の更新
//
//  確認からインストールまでは main (main/updater.js) が進め、画面は
//  ダイアログを描いて押されたボタンを返すだけ。起動時は黙って確認し、
//  新しい版があるときだけ「今すぐ更新 / あとで」を聞く
//  講座の版はこれでは変わらない (講座は新しく始めるときに別に確かめる)
// ═══════════════════════════════════════════

let updDialogId = null;

/** main から来る文言 ({ i18n, vars } か素の文字列) を表示言語で引く */
function updText(value) {
  if (value && typeof value === 'object' && value.i18n) {
    const vars = { ...(value.vars || {}) };
    for (const key of ['version', 'current']) if (vars[key]) vars[key] = formatVersion(vars[key]);
    return tf(value.i18n, vars);
  }
  return String(value ?? '');
}

function renderUpdateDialog(spec) {
  updDialogId = spec.id || null;
  $('upd-title').textContent   = updText(spec.title);
  $('upd-message').textContent = updText(spec.message);
  $('upd-detail').textContent  = updText(spec.detail);
  const isProgress = spec.kind === 'progress';
  $('upd-progress').classList.toggle('hidden', !isProgress);
  if (isProgress) setUpdateDialogProgress(0);

  // ボタンは spec.buttons の順に並べ、defaultIndex を主ボタンにする
  const actions = $('upd-actions');
  actions.innerHTML = '';
  (spec.buttons || []).forEach((label, index) => {
    const btn = document.createElement('button');
    btn.className = `btn ${index === (spec.defaultIndex || 0) ? 'btn-apply' : 'btn-answer'}`;
    btn.textContent = updText(label);
    btn.addEventListener('click', () => closeUpdateDialog(index));
    actions.appendChild(btn);
  });
  $('update-modal').classList.remove('hidden');
  actions.querySelector('.btn-apply, button')?.focus();
  if (spec.kind === 'info' || spec.kind === 'error') renderAppUpdateStatus(spec);
}

function setUpdateDialogProgress(percent) {
  const pct = Math.max(0, Math.min(100, Math.round(Number(percent) || 0)));
  $('upd-bar-fill').style.width = `${pct}%`;
  $('upd-pct').textContent = `${pct}%`;
}

/** buttonIndex を渡すと main へ返す。渡さなければ閉じるだけ */
function closeUpdateDialog(buttonIndex) {
  $('update-modal').classList.add('hidden');
  const id = updDialogId;
  updDialogId = null;
  if (id != null && buttonIndex != null) window.api.replyUpdaterDialog(id, buttonIndex);
}

/** 設定画面のバージョン欄の横に、確認の結果を短く出す */
function renderAppUpdateStatus(spec) {
  const status = $('app-update-status');
  status.classList.remove('is-error');
  if (!spec) {
    const state = appInfo?.appUpdate;
    status.textContent = state && !state.enabled
      ? t(state.reason === 'dev' ? 'appUpdateDev' : 'appUpdateNotConfigured')
      : '';
    return;
  }
  status.textContent = spec.kind === 'error' ? updText(spec.title) : updText(spec.message);
  status.classList.toggle('is-error', spec.kind === 'error');
}

async function checkAppUpdate() {
  const btn = $('btn-app-update-check');
  btn.disabled = true;
  $('app-update-status').textContent = t('appUpdateChecking');
  try {
    const res = await window.api.checkForUpdates();
    if (res?.status === 'skipped' && res.reason !== 'checking') renderAppUpdateStatus(null);
    else if (res?.status === 'postponed') $('app-update-status').textContent =
      tf('appUpdateFound', { version: formatVersion(res.version) });
  } finally {
    btn.disabled = !!(appInfo?.appUpdate && !appInfo.appUpdate.enabled);
  }
}

let settingsSnapshot = null;   // キャンセル時に戻すための見た目の控え

/** LLM ごとの API キー入力欄を LLM_MODELS から組み立てる */
function buildLlmSettings() {
  const select = $('llm-model-select');
  select.innerHTML = '';
  for (const model of appInfo.llmModels) {
    const opt = document.createElement('option');
    opt.value = model.id;
    opt.textContent = model.label;
    select.appendChild(opt);
  }
  select.value = appInfo.llmSelection.modelId || appInfo.llmModels[0]?.id || '';

  const groups = $('llm-key-groups');
  groups.innerHTML = '';
  for (const model of appInfo.llmModels) {
    const row = document.createElement('div');
    row.className = 'api-key-group';
    row.innerHTML =
      `<div class="settings-label">` +
        `<span>${escapeHtml(model.keyLabel)}</span>` +
        `<span class="api-key-badge" data-key-badge="${escapeHtml(model.keyField)}"></span>` +
      '</div>' +
      '<div class="api-key-wrap">' +
        `<input type="password" class="api-key-input" data-key-field="${escapeHtml(model.keyField)}" ` +
               'autocomplete="off" spellcheck="false" placeholder="">' +
      '</div>' +
      (model.keyUrl
        ? `<div class="api-key-hint"><a href="#" data-key-url="${escapeHtml(model.keyUrl)}">` +
          `${escapeHtml(t('apiKeyGet'))} →</a></div>`
        : '');
    groups.appendChild(row);
  }
  groups.querySelectorAll('[data-key-url]').forEach(link => {
    link.addEventListener('click', ev => {
      ev.preventDefault();
      window.api.openBrowser(link.dataset.keyUrl);
    });
  });

  updateKeyBadges();
  updateModelHint();
}

function updateKeyBadges() {
  document.querySelectorAll('[data-key-badge]').forEach(badge => {
    const has = !!appInfo.apiKeyStatus[badge.dataset.keyBadge];
    badge.textContent = t(has ? 'apiKeySet' : 'apiKeyUnset');
    badge.classList.toggle('set', has);
    badge.classList.toggle('unset', !has);
  });
  updateLlmBadge();
}

function updateModelHint() {
  const model = appInfo.llmModels.find(m => m.id === $('llm-model-select').value);
  $('llm-model-default-hint').textContent =
    model ? tf('llmDefaultModel', { model: model.defaultModel }) : '';
  $('llm-model-override').placeholder = model ? model.defaultModel : '';
}

async function openSettings() {
  settingsSnapshot = {
    theme:      localStorage.getItem('theme')      || DEFAULTS.theme,
    fontSize:   parseInt(localStorage.getItem('fontSize'), 10) || DEFAULTS.fontSize,
    fontFamily: localStorage.getItem('fontFamily') || DEFAULTS.fontFamily,
    keymap:     localStorage.getItem('keymap')     || DEFAULTS.keymap,
  };

  $('ui-lang-select').value      = getLang();
  $('llm-model-select').value    = appInfo.llmSelection.modelId;
  $('llm-model-override').value  = appInfo.llmSelection.override || '';
  $('ws-root-input').value       = appInfo.workspaceRoot;
  $('version-badge').textContent = `v${formatVersion(appInfo.version)}`;
  $('btn-app-update-check').disabled = !!(appInfo.appUpdate && !appInfo.appUpdate.enabled);
  renderAppUpdateStatus(null);
  // 入力欄には既存のキーを出さない (伏せ字でも読み出せてしまうため)
  // 空のまま保存したときは変更しない扱いにする
  document.querySelectorAll('[data-key-field]').forEach(input => { input.value = ''; });
  updateModelHint();
  updateKeyBadges();
  renderCoursesInfo();
  renderRuntimeInfo();

  // 開くたびに中央へ戻す (前回ドラッグした位置は持ち越さない)
  $('settings-panel').style.transform = '';
  $('settings-overlay').classList.remove('hidden');
}

/**
 * ダイアログを見出しの帯でつかんで動かせるようにする
 * 位置は translate で持つ (中央寄せのレイアウトはそのまま)。画面の外へは出さない
 */
function makeDialogDraggable(panel, handle) {
  handle.classList.add('dialog-drag-handle');
  handle.addEventListener('pointerdown', event => {
    if (event.button !== 0 || event.target.closest('button, input, select, textarea, a')) return;
    const start = { x: event.clientX, y: event.clientY };
    const [, tx = 0, ty = 0] = (panel.style.transform.match(/translate\((-?[\d.]+)px, (-?[\d.]+)px\)/) || []).map(Number);
    const rect = panel.getBoundingClientRect();
    try { handle.setPointerCapture(event.pointerId); } catch { /* 捕まえられなくてもドラッグはできる */ }
    const move = ev => {
      // 見出しの帯が少なくとも 40px は画面に残るようにする
      const dx = Math.min(Math.max(ev.clientX - start.x, 40 - rect.right), window.innerWidth - 40 - rect.left);
      const dy = Math.min(Math.max(ev.clientY - start.y, -rect.top), window.innerHeight - 40 - rect.top);
      panel.style.transform = `translate(${tx + dx}px, ${ty + dy}px)`;
    };
    const up = () => {
      handle.removeEventListener('pointermove', move);
      handle.removeEventListener('pointerup', up);
      handle.removeEventListener('pointercancel', up);
    };
    handle.addEventListener('pointermove', move);
    handle.addEventListener('pointerup', up);
    handle.addEventListener('pointercancel', up);
    event.preventDefault();
  });
}

function closeSettings({ revert = false } = {}) {
  if (revert && settingsSnapshot) {
    applyTheme(settingsSnapshot.theme);
    applyFontSize(settingsSnapshot.fontSize);
    applyFontFamily(settingsSnapshot.fontFamily);
    applyKeymap(settingsSnapshot.keymap);
    $('font-size-range').value    = settingsSnapshot.fontSize;
    $('font-family-select').value = settingsSnapshot.fontFamily;
  }
  settingsSnapshot = null;
  $('settings-overlay').classList.add('hidden');
}

async function renderRuntimeInfo() {
  const wrap = $('runtime-info');
  wrap.innerHTML = `<div class="runtime-info-row">${escapeHtml(t('loading'))}</div>`;

  // 観測に失敗しても「読み込み中」で固まらせない
  // どれが取れなかったのかが分かるほうが原因に近づける
  let status;
  try {
    status = await window.api.runtimeStatus();
  } catch (err) {
    wrap.innerHTML =
      `<div class="runtime-info-row"><span>${escapeHtml(t('runtimeLabel'))}</span>` +
      `<span>${escapeHtml(err?.message || String(err))}</span></div>`;
    return;
  }
  status = status || {};

  const rows = [
    ['Java',    status.java],
    ['Node.js', status.node],
    ['npm',     status.npm],
    ['Python',  status.python],
    ['bash',    status.bash],
    ['HSQLDB',  status.hsqldb ? 'OK' : null],
    ['Gradle Wrapper', status.gradleWrapper ? 'OK' : null],
    ['Apache Kafka', status.kafka],
    ['RabbitMQ', status.rabbitmq],
    ['Erlang/OTP', status.erlang],
  ];
  wrap.innerHTML = rows.map(([label, value]) =>
    `<div class="runtime-info-row"><span>${escapeHtml(label)}</span>` +
    `<span>${escapeHtml(value || t('runtimeMissing'))}</span></div>`).join('');
}

/**
 * インストールされている講座と、それがどこから読まれたかを出す
 * 講座を足したのに出ないときの切り分け (置き場が違う / course.yaml が壊れている) に使う
 */
async function renderCoursesInfo() {
  const wrap = $('courses-info');
  wrap.innerHTML = `<div class="runtime-info-row">${escapeHtml(t('loading'))}</div>`;

  let info;
  try {
    info = await window.api.coursesInfo(getLang());
  } catch (err) {
    wrap.innerHTML = `<div class="runtime-info-row"><span>${escapeHtml(t('coursesLabel'))}</span>` +
                     `<span>${escapeHtml(err?.message || String(err))}</span></div>`;
    return;
  }

  const rows = (info.courses || []).map(c =>
    `<div class="runtime-info-row" title="${escapeHtml(c.path || '')}">` +
    `<span>${escapeHtml(c.name)}</span><span>` +
    `${escapeHtml(tf('coursesExercises', { n: c.exerciseCount }))} / ` +
    `${escapeHtml(t(`courseSource_${c.source}`))}` +
    (c.version && c.version !== '0' ? ` / v${escapeHtml(c.version)}` : '') +
    '</span></div>');

  // 置き場そのものも出す (どこへ置けばよいかが分かるように)
  for (const root of info.roots || []) {
    rows.push(`<div class="runtime-info-row" title="${escapeHtml(root.dir)}">` +
      `<span>${escapeHtml(t(`courseSource_${root.source}`))}</span>` +
      `<span>${escapeHtml(root.exists ? root.dir : t('coursesRootMissing'))}</span></div>`);
  }
  if (info.devFilter) {
    rows.push(`<div class="runtime-info-row"><span>${escapeHtml(t('coursesDevFilter'))}</span>` +
      `<span>${escapeHtml(info.devFilter.join(', '))}</span></div>`);
  }
  wrap.innerHTML = rows.join('') ||
    `<div class="runtime-info-row"><span>${escapeHtml(t('coursesEmpty'))}</span><span></span></div>`;
}

async function saveSettings() {
  // 表示言語
  const lang = $('ui-lang-select').value;
  if (lang !== getLang()) {
    await window.api.setUiLang(lang);
    setLang(lang);
    applyI18nDom();
    retranslateDynamicUi();
  }

  // API キー (空欄は「変更しない」)
  for (const input of document.querySelectorAll('[data-key-field]')) {
    const key = input.value.trim();
    if (!key) continue;
    await window.api.setApiKey(input.dataset.keyField, key);
    input.value = '';
  }
  appInfo.apiKeyStatus = await window.api.getApiKeyStatus();

  // モデル選択
  appInfo.llmSelection = await window.api.setLlmSelection({
    modelId:  $('llm-model-select').value,
    override: $('llm-model-override').value.trim(),
  });

  updateKeyBadges();
  closeSettings();
}

async function changeWorkspaceRoot() {
  const picked = await window.api.wsPickDir();
  if (!picked.ok) return;
  await window.api.wsSetRoot(picked.path);
  appInfo.workspaceRoot = await window.api.wsGetRoot();
  $('ws-root-input').value = appInfo.workspaceRoot;

  await selectProject(null);
  await reloadProjects();
  await alertDialog(t('wsChangedReload'));
  const next = projects[0]?.name || null;
  if (next) await selectProject(next);
}

/** 言語を切り替えたとき、JS が作った部分を作り直す */
function retranslateDynamicUi() {
  renderMessaging();
  updateLlmBadge();
  setChatMode(chatMode, { persist: false });
  updateModelHint();
  renderTabs();
  renderTree();
  updateRunTargets();
  refreshProjectInfo();
  // 演習名・説明はコースパックが言語ごとに持っているので読み直す
  reloadExercises();
  if (!chatMessages.length) clearChat();
  if (!coverage) clearTestResults();
}

// ═══════════════════════════════════════════
//  ペインのリサイズ
// ═══════════════════════════════════════════

function setupResize(handleId, targetId, { axis, invert = false, storageKey, min, max }) {
  const handle = $(handleId);
  const target = $(targetId);
  let startPos = 0;
  let startSize = 0;

  const onMove = ev => {
    const delta = (axis === 'x' ? ev.clientX - startPos : ev.clientY - startPos) * (invert ? -1 : 1);
    const size  = Math.min(max, Math.max(min, startSize + delta));
    target.style[axis === 'x' ? 'width' : 'height'] = `${size}px`;
    localStorage.setItem(storageKey, String(Math.round(size)));
  };
  const onUp = () => {
    handle.classList.remove('dragging');
    document.removeEventListener('mousemove', onMove);
    document.removeEventListener('mouseup', onUp);
    fitTerminal();
    forEachEditor(cm => cm.refresh());
  };

  handle.addEventListener('mousedown', ev => {
    ev.preventDefault();
    startPos  = axis === 'x' ? ev.clientX : ev.clientY;
    startSize = axis === 'x' ? target.offsetWidth : target.offsetHeight;
    handle.classList.add('dragging');
    document.addEventListener('mousemove', onMove);
    document.addEventListener('mouseup', onUp);
  });
}

// ═══════════════════════════════════════════
//  イベント配線
// ═══════════════════════════════════════════

function wireEvents() {
  makeDialogDraggable($('settings-panel'), document.querySelector('#settings-panel .settings-header'));
  // ── ヘッダー ──
  // コース切り替え。開いているプロジェクトはそのままにして、演習一覧だけ入れ替える
  // (別の講座を見ながら今の作業を続けられるようにするため)
  $('btn-course-dialog').addEventListener('click', openCourseDialog);
  $('course-dialog-close').addEventListener('click', closeCourseDialog);
  $('course-dialog-overlay').addEventListener('click', ev => {
    if (ev.target === $('course-dialog-overlay')) closeCourseDialog();
  });
  $('btn-reset-exercise').addEventListener('click', resetExercise);
  $('llm-model-select').addEventListener('change', async ev => {
    appInfo.llmSelection = await window.api.setLlmSelection({
      modelId: ev.target.value, override: appInfo.llmSelection.override,
    });
    updateKeyBadges();
    updateModelHint();
  });

  // ── 設定 ──
  $('btn-settings').addEventListener('click', openSettings);
  $('settings-close').addEventListener('click', () => closeSettings({ revert: true }));
  $('settings-cancel').addEventListener('click', () => closeSettings({ revert: true }));
  $('settings-save').addEventListener('click', saveSettings);
  $('settings-overlay').addEventListener('click', ev => {
    if (ev.target === $('settings-overlay')) closeSettings({ revert: true });
  });
  $('btn-ws-browse').addEventListener('click', changeWorkspaceRoot);
  $('btn-app-update-check').addEventListener('click', checkAppUpdate);
  $('btn-open-courses-dir').addEventListener('click', () => window.api.coursesOpenDir());
  // 講座を足した直後に、アプリを再起動させずに反映する
  $('btn-reload-courses').addEventListener('click', async () => {
    await reloadExercises({ reload: true });
    await renderCoursesInfo();
  });
  // テーマ・フォントは選んだ瞬間に反映して見た目を確かめられるようにする
  document.querySelectorAll('#theme-grid .theme-btn').forEach(btn =>
    btn.addEventListener('click', () => applyTheme(btn.dataset.theme)));
  $('font-size-range').addEventListener('input', ev => applyFontSize(parseInt(ev.target.value, 10)));
  $('font-family-select').addEventListener('change', ev => applyFontFamily(ev.target.value));
  $('keymap-select').addEventListener('change', ev => applyKeymap(ev.target.value));
  $('llm-model-override').addEventListener('input', updateModelHint);
  $('llm-model-select').addEventListener('change', updateModelHint);

  // ── ファイルツリー ──
  document.addEventListener('click', closeTreeMenu);

  // ── エディタ ──
  $('btn-editor-undo').addEventListener('click', () => { activeEditor()?.undo(); updateHistoryButtons(); });
  $('btn-editor-redo').addEventListener('click', () => { activeEditor()?.redo(); updateHistoryButtons(); });
  $('btn-md-preview').addEventListener('click', toggleMdPreview);

  // ── 実行 ──
  $('btn-run').addEventListener('click', runSelected);
  $('btn-run-stop').addEventListener('click', stopRun);
  $('run-target-select').addEventListener('change', () => {
    $('btn-run').disabled = running || !project || !$('run-target-select').value;
  });
  document.querySelectorAll('.run-tab').forEach(tab =>
    tab.addEventListener('click', () => showRunPane(tab.dataset.pane)));

  // ── SQL ──
  $('service-controls').addEventListener('click', serviceAction);
  $('messaging-services').addEventListener('click', messagingAction);
  $('messaging-log-select').addEventListener('change', renderMessagingLog);
  // 末尾付近にいれば追いかけ、上へ戻したら止める (隠れているあいだの高さ 0 は判定に使わない)
  $('tab-result').addEventListener('scroll', () => {
    const pane = $('tab-result');
    if (!pane.clientHeight) return;
    runOutputFollow = pane.scrollHeight - pane.scrollTop - pane.clientHeight < 40;
  });

  // ── プレビュー ──
  const view = $('mini-browser');
  view.addEventListener('dom-ready', () => { previewReady = true; });
  $('browser-back').addEventListener('click', () => { try { view.goBack(); } catch {} });
  $('browser-forward').addEventListener('click', () => { try { view.goForward(); } catch {} });
  $('browser-reload').addEventListener('click', () => { try { view.reload(); } catch {} });
  $('browser-go').addEventListener('click', () => previewUrl(normalizeUrl($('browser-url').value)));
  $('browser-url').addEventListener('keydown', ev => {
    if (ev.key === 'Enter') previewUrl(normalizeUrl($('browser-url').value));
  });
  $('browser-external').addEventListener('click', () =>
    window.api.openBrowser($('browser-url').value));

  // ── チャット ──
  $('btn-chat-send').addEventListener('click', () => (chatStreaming ? abortChat() : sendChat()));
  for (const input of document.querySelectorAll('input[name="chat-mode"]')) {
    input.addEventListener('change', () => { if (input.checked && !chatStreaming) setChatMode(input.value); });
  }
  $('btn-chat-clear').addEventListener('click', clearChat);
  $('chat-input').addEventListener('keydown', ev => {
    if (ev.key === 'Enter' && !ev.shiftKey && !ev.isComposing) {
      ev.preventDefault();
      sendChat();
    }
  });

  // ── リサイズ ──
  setupResize('sidebar-resize-handle', 'sidebar', {
    axis: 'x', storageKey: 'sidebarWidth', min: 150, max: 560,
  });
  setupResize('ai-resize-handle', 'ai-panel', {
    axis: 'x', invert: true, storageKey: 'aiWidth', min: 240, max: 640,
  });
  setupResize('run-log-handle', 'run-output-wrap', {
    axis: 'y', invert: true, storageKey: 'runHeight', min: 60, max: 700,
  });
  setupResize('exercise-resize-handle', 'exercise-list', {
    axis: 'y', storageKey: 'exerciseHeight', min: 60, max: 620,
  });

  // ── キーボード ──
  document.addEventListener('keydown', ev => {
    if (ev.key === 'Escape') {
      if (!$('simple-dialog-overlay').classList.contains('hidden')) closeSimpleDialog(false);
      else if (!$('course-dialog-overlay').classList.contains('hidden')) closeCourseDialog();
      else if (!$('settings-overlay').classList.contains('hidden')) {
        closeSettings({ revert: true });
      }
      return;
    }
    if ((ev.ctrlKey || ev.metaKey) && ev.key.toLowerCase() === 's') {
      ev.preventDefault();
      if (activeFile) saveFile(activeFile);
    }
    if ((ev.ctrlKey || ev.metaKey) && ev.key === 'Enter') {
      ev.preventDefault();
      if (!running) runSelected();
    }
  });

  window.addEventListener('resize', debounce(() => fitTerminal(), 120));
  // 閉じるときに書き損じないよう、未保存の変更を同期的に投げておく
  window.addEventListener('beforeunload', () => {
    for (const [relPath, entry] of openFiles) {
      if (entry.dirty && project) window.api.wsWriteFile(project, relPath, entry.cm.getValue());
    }
  });
}

function normalizeUrl(value) {
  const url = String(value || '').trim();
  if (!url) return 'about:blank';
  return /^[a-z]+:\/\//i.test(url) ? url : `http://${url}`;
}

// ═══════════════════════════════════════════
//  main プロセスからの通知
// ═══════════════════════════════════════════

function wireIpc() {
  window.api.onUpdaterDialog(spec => renderUpdateDialog(spec));
  window.api.onUpdaterDialogProgress(({ percent } = {}) => setUpdateDialogProgress(percent));
  window.api.onUpdaterDialogClose(() => closeUpdateDialog());
  // 再起動してインストールする前に、未保存の変更を書き出す
  window.api.onUpdaterBeforeInstall(async ({ id }) => {
    try { for (const relPath of openFiles.keys()) await saveFile(relPath); }
    finally { window.api.replyUpdaterDialog(id, 0); }
  });
  window.api.onMessagingStatus(states => { messagingStates = states; renderMessaging(); });
  window.api.onCompanionStatus(states => {
    // preview を持つプロセス (React の画面など) が待ち受けを始めたら、プレビューでそこを開く
    for (const s of states) {
      const before = companionStates.find(c => c.id === s.id)?.state;
      if (s.state === 'running' && before !== 'running' && s.preview) {
        previewUrl(`http://localhost:${s.port}${s.preview}`);
      }
    }
    companionStates = states;
    renderServiceControls();
  });
  window.api.onCompanionLog(({ id, text }) => appendCompanionOutput(id, text));
  window.api.onMessagingLog(({ id, text }) => {
    const state = messagingStates.find(s => s.id === id);
    if (state) state.log = (state.log + text).slice(-64000);
    if ($('tab-messaging').classList.contains('active')) renderMessagingLog();
  });
  window.api.onRunOutput(text => appendRunOutput(text));
  window.api.onRunExit(async ({ code }) => {
    setRunning(false);
    appendRunOutput(code === 0 ? t('runExitOk') : tf('runExitNg', { code }));
    await refreshPreviewAvailability();
  });
  window.api.onRunUrl(({ url }) => {
    // 一緒に起動した画面 (React など) が先に待ち受けを始めていれば、プレビューはそちらのまま
    const shown = companionStates.find(c => c.preview && c.state === 'running');
    previewUrl(shown ? `http://localhost:${shown.port}${shown.preview}` : withExercisePreviewPath(url));
  });
  window.api.onRunTestResults(data => {
    renderTestResults(data);
    showRunPane('tab-test-results');
  });

  window.api.onTermOutput(data => xterm?.write(data));
  window.api.onTermExit(({ code }) => xterm?.write(tf('termExited', { code })));

  window.api.onChatStart(() => { /* 吹き出しは送信時に用意してある */ });
  window.api.onChatChunk(text => {
    if (!chatBubble) return;
    chatBuffer += text;
    chatBubble.innerHTML = renderMarkdown(chatBuffer);
    scrollChatToBottom();
  });
  window.api.onChatEnd(() => {
    const bubble = chatBubble;
    const text   = chatBuffer;
    chatBubble = null;
    setChatStreaming(false);
    if (!bubble) return;
    bubble.classList.remove('streaming');
    chatMessages.push({ role: 'assistant', content: text });
    bubble.innerHTML = renderMarkdown(text);
    addCopyButtons(bubble, text);
    scrollChatToBottom();
  });
  window.api.onChatError(async (message, info) => {
    if (chatBubble) {
      chatBubble.classList.remove('streaming');
      chatBubble.textContent = tf('chatError', { error: message });
    }
    chatBubble = null;
    setChatStreaming(false);
    // 直前のユーザー発言は履歴から外す (同じ内容でもう一度送れるようにする)
    if (chatMessages.at(-1)?.role === 'user') chatMessages.pop();
    if (info?.code === 'API_KEY_NOT_CONFIGURED') openSettings();
  });

  // ── Agent モード ──
  window.api.onAgentStart(() => { /* 吹き出しは送信時に用意してある */ });
  window.api.onAgentText(text => {
    if (!chatBubble || !text.trim()) return;
    // 経過カードより前に本文を積む (何をするつもりかが上に並ぶ)
    const block = document.createElement('div');
    block.className = 'agent-say';
    block.innerHTML = renderMarkdown(text);
    chatBubble.insertBefore(block, agentCard);
    chatBuffer += `${text}\n`;
    scrollChatToBottom();
  });
  window.api.onAgentTool(info => renderAgentTool(info));
  window.api.onAgentEdit(edit => renderAgentEdit(edit));
  window.api.onAgentLimit(({ steps }) => {
    if (agentCard) {
      const note = document.createElement('div');
      note.className = 'agent-note';
      note.textContent = tf('agentLimit', { n: steps });
      agentCard.appendChild(note);
    }
  });
  window.api.onAgentEnd(() => {
    chatBubble?.classList.remove('streaming');
    addCopyButtons(chatBubble, chatBuffer);
    if (chatBuffer.trim()) chatMessages.push({ role: 'assistant', content: chatBuffer });
    chatBubble = null;
    agentCard  = null;
    setChatStreaming(false);
    scrollChatToBottom();
  });
  window.api.onAgentError(async (message, info) => {
    if (chatBubble) {
      chatBubble.classList.remove('streaming');
      const note = document.createElement('div');
      note.className = 'agent-note is-error';
      note.textContent = tf('chatError', { error: message });
      chatBubble.appendChild(note);
    }
    chatBubble = null;
    agentCard  = null;
    setChatStreaming(false);
    if (chatMessages.at(-1)?.role === 'user') chatMessages.pop();
    if (info?.code === 'API_KEY_NOT_CONFIGURED') openSettings();
  });
}

// ═══════════════════════════════════════════
//  起動
// ═══════════════════════════════════════════

async function boot() {
  appInfo = await window.api.getAppInfo();

  setLang(appInfo.lang);
  applyI18nDom();
  loadLocalSettings();
  buildLlmSettings();
  updateLlmBadge();
  setChatMode(appInfo.llmSelection?.chatMode || 'ask', { persist: false });
  clearChat();
  clearTestResults();
  clearRunOutput();
  setRunning(false);
  setPreviewAvailable(false);
  wireEvents();
  wireIpc();
  // タブの行の右端に、動いているサーバーを出すため (演習の実行前でも止められるように)
  refreshMessaging();

  // 演習一覧を先に読む (プロジェクト一覧の描画で「作成済み」の照合に使う)
  await reloadExercises();
  await reloadProjects();
  const last = localStorage.getItem('lastProject');
  const initial = projects.some(p => p.name === last) ? last : projects[0]?.name || null;
  if (initial) await selectProject(initial, { focus: false });
  else await refreshProjectInfo();

  showRunPane('tab-result');
}

window.addEventListener('DOMContentLoaded', () => {
  boot()
    .catch(err => console.error('[boot] failed:', err))
    // 初期化が転んでも空の IDE は見せる (真っ暗のまま待たせない)
    .finally(() => window.api.rendererReady());
});
