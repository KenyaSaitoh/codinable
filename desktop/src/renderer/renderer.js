// ═══════════════════════════════════════════════════════════
//  Codinable — レンダラー
//
//  画面の構成は index.html のとおり:
//    ヘッダー (プロジェクト選択 / モデル選択 / 設定)
//    プロジェクト情報バー
//    メイン = ファイルツリー | エディタ + 出力タブ | チャット
//
//  main プロセスとは preload.js の window.api だけでやり取りする。
//  この層に fs / child_process は無いので、ファイルもプロセスも
//  すべて window.api 経由で扱う。
//
//  状態の持ち方:
//    - 見た目 (テーマ・フォント・キーバインド・ペイン幅・最後に開いたプロジェクト)
//      → localStorage。アプリを再起動しても同じ画面で始まる。
//    - 設定の本体 (表示言語・API キー・ワークスペース・モデル選択)
//      → main プロセスの設定ファイル (window.api 経由)。
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
let courses     = [];     // コースパック (演習一覧と新規プロジェクトダイアログで使う)

// ═══════════════════════════════════════════
//  テーマ / フォント / キーバインド
//
//  ワークベンチの配色は style.css の body[data-theme='<id>']、
//  エディタの配色は editor/themes.js が同じ id で持つ。
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
//  自前のモーダルで置き換える。いずれも Promise を返す。
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
//  閉じるまで捨てずに CSS で出し入れする。
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

  const entry = { host, cm, dirty: false, mode };
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
  mdPreviewOn = false;

  for (const [key, entry] of openFiles) {
    entry.host.classList.toggle('hidden', key !== relPath);
  }
  $('editor-empty').classList.toggle('hidden', openFiles.size > 0);
  $('md-preview').classList.add('hidden');

  const entry = openFiles.get(relPath);
  $('editor-filepath').textContent = relPath || '';
  $('btn-md-preview').classList.toggle('hidden', !entry || entry.mode !== 'text/markdown');
  $('btn-md-preview').textContent = t('btnMdPreview');

  renderTabs();
  renderTree();
  updateHistoryButtons();
  updateRunTargets();
  applyCoverageToEditor();
  if (focus && entry) setTimeout(() => entry.cm.focus(), 0);
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
  const select = $('project-select');
  select.innerHTML = '';

  if (!projects.length) {
    const opt = document.createElement('option');
    opt.value = '';
    opt.textContent = t('noProject');
    select.appendChild(opt);
  }
  for (const p of projects) {
    const opt = document.createElement('option');
    opt.value = p.name;
    opt.textContent = p.name;
    select.appendChild(opt);
  }
  if (project) select.value = project;

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
  $('project-select').value = project || '';
  // どの演習を開いているかの ▶ 印を付け替える
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
    $('project-icon').textContent = '📁';
    $('project-name').textContent = t('noProject');
    $('project-kinds').innerHTML  = '';
    $('project-path').textContent = '';
    $('btn-reset-exercise').classList.add('hidden');
    $('hsqldb-badge').classList.add('hidden');
    updateRunTargets();
    return;
  }

  const info = await window.api.wsProjectInfo(project);
  projectInfo = info.ok ? info : null;

  $('project-icon').textContent = '📁';
  $('project-name').textContent = project;
  $('project-path').textContent = info.ok ? info.path : '';
  $('project-kinds').innerHTML = (info.kinds || [])
    .map(k => `<span class="project-kind">${escapeHtml(k)}</span>`).join('');
  $('btn-reset-exercise').classList.toggle('hidden', !(info.ok && info.template));
  $('hsqldb-badge').classList.toggle('hidden', !(info.kinds || []).includes('sql'));

  updateRunTargets();
}

/**
 * 演習を配布時の状態に戻す。
 *
 * 試して壊したコードをいつでも捨てられるようにするための機能なので、
 * 編集中のタブも disk の内容に入れ替える (開いたままだと戻したのに古い内容が
 * 見え続け、保存した瞬間に書き戻ってしまう)。
 */
async function resetExercise() {
  if (!project) return;
  if (!projectInfo?.template) { await alertDialog(t('resetNoTemplate')); return; }
  if (!await confirmDialog(tf('confirmResetExercise', { name: project }))) return;

  const res = await window.api.wsResetTemplate(project, getLang());
  if (!res.ok) {
    await alertDialog(res.error === 'no-template' ? t('resetNoTemplate') : (res.error || ''));
    return;
  }
  for (const relPath of [...openFiles.keys()]) await reopenFileFromDisk(relPath);
  await reloadTree();
  await refreshProjectInfo();
  await alertDialog(tf('resetDone', { n: res.written }));
}

// ═══════════════════════════════════════════
//  演習一覧
//
//  演習 = 講座のレッスンに対応する「動かして確かめる 1 単位」。
//  問題を出して解かせるものではないので、正解・不正解や採点は持たない。
//
//  受講者の操作を「演習を選ぶ → 実行を押す」の 2 手に収めるため、
//  選んだ時点で作業用プロジェクトの用意・ファイルを開く・実行対象の選択
//  (= 実行環境の切り替え) までを済ませる。
// ═══════════════════════════════════════════

/** runtime → 一覧に出すアイコン。course.yaml の runtime と対応させる */
const RUNTIME_ICONS = {
  java: '☕', spring: '🌱', node: '🟩', react: '⚛️',
  python: '🐍', static: '🌐', sql: '🗄', shell: '🖥', other: '📦',
};

async function reloadExercises({ reload = false } = {}) {
  courses = reload ? await window.api.coursesReload(getLang())
                   : await window.api.loadCourses(getLang());

  // コースの切り替えはヘッダー左で行う。講座が 1 つだけのときも、
  // 今どの講座を見ているのかが分かるように出したままにして、選べなくする。
  const select = $('active-course-select');
  const previous = select.value || localStorage.getItem('lastCourse') || '';
  select.innerHTML = '';
  for (const course of courses) {
    const opt = document.createElement('option');
    opt.value = course.id;
    opt.textContent = course.name;
    opt.title = course.description || course.name;
    select.appendChild(opt);
  }
  if (!courses.length) {
    const opt = document.createElement('option');
    opt.value = '';
    opt.textContent = t('coursesEmpty');
    select.appendChild(opt);
  }
  select.value = courses.some(c => c.id === previous) ? previous : (courses[0]?.id || '');
  select.disabled = courses.length <= 1;

  renderExercises();
}

function currentCourse() {
  return courses.find(c => c.id === $('active-course-select').value) || null;
}

/** 演習に対応する作業用プロジェクト (まだ作っていなければ null) */
function projectForExercise(course, exercise) {
  return projects.find(p => p.courseId === course.id && p.template === exercise.id) || null;
}

function renderExercises() {
  const list    = $('exercise-list');
  const course  = currentCourse();
  const entries = course?.exercises || [];

  $('exercise-count').textContent = String(entries.length);
  $('exercise-course-name').textContent = course?.name || '';
  list.innerHTML = '';
  if (!entries.length) {
    list.innerHTML = `<div class="tree-placeholder">${escapeHtml(t('exerciseEmpty'))}</div>`;
    return;
  }

  let lastChapter = null;
  for (const exercise of entries) {
    // 一覧そのものはフラットに並べる。どのチャプターのものかが分かるよう、
    // チャプターが変わるところにだけ細い見出しを挟む
    if (exercise.chapter && exercise.chapter !== lastChapter) {
      const heading = document.createElement('div');
      heading.className = 'exercise-chapter';
      heading.textContent = tf('exerciseChapter', { n: exercise.chapter });
      list.appendChild(heading);
      lastChapter = exercise.chapter;
    }

    const created = projectForExercise(course, exercise);
    const item = document.createElement('button');
    item.type  = 'button';
    item.className = 'q-item exercise-item';
    // created = 作業用プロジェクトが既にある (進捗ではなく、作ったかどうか)
    item.classList.toggle('created', !!created);
    item.classList.toggle('active', !!created && created.name === project);
    item.title = exercise.description || exercise.name;
    item.innerHTML =
      '<span class="q-status"></span>' +
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
    list.appendChild(item);
  }
}

/**
 * 演習を開く。
 * 作業用プロジェクトが無ければ雛形から作り、選んで、実行対象まで合わせる。
 */
async function openExercise(course, exercise) {
  let target = projectForExercise(course, exercise);

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
 * 演習が宣言している実行対象を選ぶ。これが「実行環境の自動切り替え」にあたる。
 *
 * course.yaml の run は実行対象セレクトと同じ書式。ただし file: のときは、
 * 実行対象 'file' が「いま開いているファイル」を指すため、先にそのファイルを開く。
 */
async function applyExerciseRunTarget(exercise) {
  if (!exercise.run) return;
  const [kind, arg = ''] = exercise.run.split(/:(.*)/s);

  // file: / sql: は対象がファイルなので、何を動かすのか見えるように開いておく
  if ((kind === 'file' || kind === 'sql') && arg) await openFile(arg);
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
//  新規プロジェクトダイアログ
// ═══════════════════════════════════════════

let selectedTemplate = null;   // { courseId, templateId, openFiles } / null = 空プロジェクト

async function openNewProjectDialog() {
  courses = await window.api.loadCourses(getLang());

  const select = $('course-select');
  select.innerHTML = '';
  const none = document.createElement('option');
  none.value = '';
  none.textContent = t('courseNone');
  select.appendChild(none);
  for (const course of courses) {
    const opt = document.createElement('option');
    opt.value = course.id;
    opt.textContent = course.name;
    select.appendChild(opt);
  }
  // ヘッダーで選んでいる講座を既定にする (講座を使う人のほうが多いため)
  const active = $('active-course-select').value;
  select.value = courses.some(c => c.id === active) ? active : (courses[0]?.id || '');

  $('new-project-name').value = '';
  $('new-project-error').classList.add('hidden');
  renderTemplateList();
  $('new-project-overlay').classList.remove('hidden');
}

function renderTemplateList() {
  const courseId = $('course-select').value;
  const list = $('template-list');
  list.innerHTML = '';
  selectedTemplate = null;

  const cards = [];
  if (!courseId) {
    cards.push({ id: null, name: t('templateBlank'), description: t('templateBlankDesc'), tags: [] });
  } else {
    const course = courses.find(c => c.id === courseId);
    for (const exercise of course?.exercises || []) {
      cards.push({
        id: exercise.id, name: exercise.name, description: exercise.description,
        suggestName: exercise.suggestName, openFiles: exercise.openFiles,
        tags: [exercise.lesson, t(`runtime_${exercise.runtime}`)].filter(Boolean),
      });
    }
    if (!cards.length) {
      list.innerHTML = `<div class="tree-placeholder">${escapeHtml(t('templateEmpty'))}</div>`;
    }
    // 講座を選んでいても空から始めたい人はいるので、最後に必ず置く
    cards.push({ id: null, name: t('templateBlank'), description: t('templateBlankDesc'), tags: [] });
  }

  cards.forEach((card, index) => {
    const el = document.createElement('button');
    el.type = 'button';
    el.className = 'template-card';
    el.innerHTML =
      `<div class="template-card-title">${escapeHtml(card.name)}</div>` +
      (card.description ? `<div class="template-card-desc">${escapeHtml(card.description)}</div>` : '') +
      (card.tags.length
        ? `<div class="template-card-tags">${card.tags
            .map(tag => `<span class="template-card-tag">${escapeHtml(tag)}</span>`).join('')}</div>`
        : '');
    el.addEventListener('click', () => {
      list.querySelectorAll('.template-card').forEach(c => c.classList.remove('selected'));
      el.classList.add('selected');
      selectedTemplate = card.id
        ? { courseId, templateId: card.id, openFiles: card.openFiles || [] }
        : null;
      if (!$('new-project-name').value.trim()) {
        $('new-project-name').value = card.suggestName || 'my-project';
      }
      $('btn-create-project').disabled = false;
    });
    list.appendChild(el);
    if (index === 0) el.click();
  });
}

async function createProject() {
  const name = $('new-project-name').value.trim();
  const error = $('new-project-error');
  error.classList.add('hidden');

  if (!/^[A-Za-z0-9][A-Za-z0-9._-]{0,63}$/.test(name)) {
    error.textContent = t('errNameInvalid');
    error.classList.remove('hidden');
    return;
  }

  const res = await window.api.wsCreateProject({
    name,
    courseId:   selectedTemplate?.courseId,
    templateId: selectedTemplate?.templateId,
    lang:       getLang(),
  });
  if (!res.ok) {
    error.textContent = res.error === 'already-exists' ? t('errNameExists')
                      : res.error === 'invalid-name'   ? t('errNameInvalid')
                      : tf('errCreateFailed', { error: res.error || '' });
    error.classList.remove('hidden');
    return;
  }

  $('new-project-overlay').classList.add('hidden');
  await reloadProjects();
  await selectProject(name, { openInitial: selectedTemplate?.openFiles || [] });
}

// ═══════════════════════════════════════════
//  実行
// ═══════════════════════════════════════════

let running = false;

/**
 * 実行できるものを select に並べる。値は 'kind' または 'kind:引数'。
 *
 * 並ぶ内容はプロジェクトの中身だけで決まり、エディタで選んでいるファイルには
 * 依存しない。README を開いているあいだ実行できなくなる、といったことを避ける。
 * 先頭に来るものが既定になるので、そのプロジェクトの「本命」から並べる。
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
  // あっても dev サーバー越しでないと動かないので、並べると壊れた選択肢になる。
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

function setRunning(state, interactive = false) {
  running = state;
  $('btn-run').disabled      = state || !project || !$('run-target-select').value;
  $('btn-run-stop').disabled = !state;
  // 標準入力欄は「実際に届く実行」のときだけ出す。gradlew は shell 経由で
  // 起動するため届かず、出しておくと押しても何も起きない UI になる。
  $('run-stdin-row').classList.toggle('hidden', !state || !interactive);
}

function clearRunOutput() {
  $('output-result').textContent = t('outputPlaceholder');
  runLog = '';
}

let runLog = '';   // チャットへ渡すための実行ログ (画面表示とは別に保持する)

function appendRunOutput(text) {
  const pre = $('output-result');
  if (runLog === '') pre.textContent = '';
  runLog += text;
  pre.textContent += text;
  pre.scrollTop = pre.scrollHeight;
}

async function runSelected() {
  if (!project) { await alertDialog(t('runNoProject')); return; }
  const value = $('run-target-select').value;
  if (!value) { await alertDialog(t('runNoTarget')); return; }

  const [kind, arg = ''] = value.split(/:(.*)/s);

  // 静的ページは子プロセスを起こさず、内蔵の静的サーバーで配信する
  if (kind === 'static') { await servePreview(arg); return; }

  // SQL は子プロセスではなく常駐の HSQLDB へ流す。
  // DB が止まっていればここで起こしてから流す (「DB起動」を先に押させない)。
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
  setRunning(true, res.interactive);
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

function showRunPane(paneId) {
  document.querySelectorAll('.run-tab').forEach(tab =>
    tab.classList.toggle('active', tab.dataset.pane === paneId));
  document.querySelectorAll('.run-pane').forEach(pane =>
    pane.classList.toggle('active', pane.id === paneId));
  // 実行ログをチャットに渡すボタンは、実行結果タブでだけ意味がある
  $('btn-add-log-context').classList.toggle('hidden', paneId !== 'tab-result');

  if (paneId === 'tab-terminal') startTerminal();
  if (paneId === 'tab-browser')  fitPreview();
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

let sqlRunning = false;

function setSqlRunning(state) {
  sqlRunning = state;
  $('btn-sql-start').disabled = state;
  $('btn-sql-stop').disabled  = !state;
  $('btn-sql-run').disabled   = !state;
}

function setSqlMessage(html, className = 'sql-message') {
  $('sql-result-wrap').innerHTML = `<div class="${className}">${html}</div>`;
}

async function startSql() {
  setSqlMessage(escapeHtml(t('sqlStarting')));
  const res = await window.api.sqlStart('');
  if (!res.ok) { setSqlMessage(escapeHtml(res.error || ''), 'sql-error'); return; }
  setSqlRunning(true);
  setSqlMessage(escapeHtml(t('sqlStarted')));
}

async function stopSql() {
  await window.api.sqlStop();
  setSqlRunning(false);
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

async function runSql() {
  const sql = currentSqlText().trim();
  if (!sql) { setSqlMessage(escapeHtml(t('sqlNoSql')), 'sql-error'); return; }
  const res = await window.api.sqlRun(sql);
  renderSqlResult(res);
}

/**
 * 「実行」ボタンから SQL を流す。
 *
 * 対象ファイルをエディタで開いてから、その内容 (選択範囲があればそこだけ) を
 * HSQLDB へ流す。DB が止まっていれば起こすので、編集 → 実行がボタン 1 つで回る。
 */
async function runSqlFromEditor(relPath) {
  // 何が流れたのかが見えるように、対象は必ず画面に出しておく
  if (relPath) await openFile(relPath);
  for (const openPath of openFiles.keys()) await saveFile(openPath);
  showRunPane('tab-sql');

  const entry = relPath ? openFiles.get(relPath) : null;
  const sql = (relPath && relPath !== activeFile && entry ? entry.cm.getValue()
                                                          : currentSqlText()).trim();
  if (!sql) { setSqlMessage(escapeHtml(t('sqlNoSql')), 'sql-error'); return; }

  if (!sqlRunning) {
    await startSql();
    if (!sqlRunning) return;   // 起動に失敗した (理由は startSql が出している)
  }
  renderSqlResult(await window.api.sqlRun(sql));
}

function renderSqlResult(res) {
  if (!res || res.error) {
    setSqlMessage(escapeHtml(res?.error || ''), 'sql-error');
    return;
  }
  // SELECT 以外 (INSERT / UPDATE / DDL) は更新件数だけが返る
  if (!Array.isArray(res.columns) || !res.columns.length) {
    const message = typeof res.affected === 'number' && res.affected >= 0
      ? tf('sqlUpdated', { n: res.affected })
      : t('sqlOk');
    setSqlMessage(escapeHtml(message));
    return;
  }

  const rows = res.rows || [];
  const head = res.columns.map(c => `<th>${escapeHtml(c)}</th>`).join('');
  const body = rows.map(row =>
    `<tr>${row.map(cell => `<td>${cell === null || cell === undefined
      ? `<em>${escapeHtml(t('sqlNull'))}</em>` : escapeHtml(cell)}</td>`).join('')}</tr>`).join('');

  $('sql-result-wrap').innerHTML =
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

// プレビューできる先があるか。
// 「ボタンはあるが押しても何も起きない」を無くすため、実際に待ち受けている
// サーバーが見つかるまではプレビューを触れない状態にしておく。
let previewAvailable = false;

function setPreviewAvailable(state) {
  previewAvailable = !!state;
  $('btn-preview').disabled     = !previewAvailable;
  $('run-tab-browser').disabled = !previewAvailable;
  if (!previewAvailable) {
    // <webview> の src は触らない (about:blank を入れ直すと ERR_ABORTED になる)。
    // 触れないタブなので、次にプレビューできたとき previewUrl が入れ替える。
    $('browser-url').value = '';
    // 見えなくなるタブを開いたままにしない
    if (document.querySelector('.run-tab.active')?.id === 'run-tab-browser') {
      showRunPane('tab-result');
    }
  }
}

/**
 * 実行が終わったあとにプレビューの可否を見直す。
 * 静的ページの内蔵サーバーは実行プロセスとは別に生き続けるので、
 * プロセスが終わったことだけを理由に落とさない。
 */
async function refreshPreviewAvailability() {
  const status = await window.api.previewStatus();
  setPreviewAvailable(!!(status.ok && status.url));
}

function previewUrl(url) {
  $('browser-url').value = url;
  setPreviewAvailable(true);
  $('mini-browser').src  = url;
  showRunPane('tab-browser');
}

function fitPreview() {
  // <webview> は非表示のあいだ 0x0 で描画されるため、表示時に読み直す
  const view = $('mini-browser');
  if (view.src && view.src !== 'about:blank') {
    try { view.reload(); } catch { /* まだ読み込まれていない */ }
  }
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

/** 🌐 プレビューボタン: 直前に見つかった URL → 内蔵サーバー の順に当たってみる */
async function openPreview() {
  const current = $('browser-url').value.trim();
  if (current && current !== 'about:blank') { previewUrl(current); return; }
  const status = await window.api.previewStatus();
  if (status.ok && status.url) { previewUrl(status.url); return; }
  await alertDialog(t('previewNoTarget'));
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
let chatEditMode  = false;  // 応答を変更案として解釈するか
const attachments = [];     // [{ kind: 'file' | 'log', path?, content }]

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
    `<div class="chat-bubble${streaming ? ' streaming' : ''}"></div>`;
  const bubble = msg.querySelector('.chat-bubble');
  // ユーザーの発言は入力どおり、AI の応答は Markdown として描く
  if (role === 'user') bubble.textContent = content;
  else bubble.innerHTML = renderMarkdown(content);

  history.appendChild(msg);
  history.scrollTop = history.scrollHeight;
  return bubble;
}

function setChatStreaming(state) {
  chatStreaming = state;
  $('btn-chat-send').disabled = state;
  $('btn-chat-edit').disabled = state;
  $('btn-chat-abort').classList.toggle('hidden', !state);
  $('chat-actions').classList.toggle('hidden', state);
}

function renderAttachments() {
  const wrap = $('ai-context-chips');
  wrap.innerHTML = '';
  wrap.classList.toggle('hidden', !attachments.length);
  attachments.forEach((item, index) => {
    const chip = document.createElement('span');
    chip.className = 'ai-context-chip';
    chip.innerHTML =
      escapeHtml(item.kind === 'file' ? tf('chipFile', { name: item.path }) : t('chipLog')) +
      `<button class="ai-context-chip-remove" title="${escapeHtml(t('chipRemove'))}">✕</button>`;
    chip.querySelector('button').addEventListener('click', () => {
      attachments.splice(index, 1);
      renderAttachments();
    });
    wrap.appendChild(chip);
  });
}

function attachCurrentFile() {
  if (!activeFile) return;
  const entry = openFiles.get(activeFile);
  if (!entry) return;
  const existing = attachments.find(a => a.kind === 'file' && a.path === activeFile);
  if (existing) existing.content = entry.cm.getValue();
  else attachments.push({ kind: 'file', path: activeFile, content: entry.cm.getValue() });
  renderAttachments();
}

function attachRunLog() {
  if (!runLog) return;
  const existing = attachments.find(a => a.kind === 'log');
  if (existing) existing.content = runLog;
  else attachments.push({ kind: 'log', content: runLog });
  renderAttachments();
}

async function sendChat({ editMode = false } = {}) {
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

  // 書き換えは「いま開いているファイル」が対象。中身を渡さないと書き換えられないので、
  // 添付し忘れても成立するようにここで開いているファイルを全部積む。
  if (editMode) {
    if (!project)     { await alertDialog(t('editNoProject')); return; }
    if (!openFiles.size) { await alertDialog(t('editNoFile')); return; }
    for (const relPath of openFiles.keys()) await saveFile(relPath);
    for (const [relPath, entry] of openFiles) {
      const existing = attachments.find(a => a.kind === 'file' && a.path === relPath);
      if (existing) existing.content = entry.cm.getValue();
      else attachments.push({ kind: 'file', path: relPath, content: entry.cm.getValue() });
    }
    renderAttachments();
  }

  input.value = '';
  addChatMessage('user', text);
  chatMessages.push({ role: 'user', content: text });

  chatBuffer = '';
  chatEditMode = editMode;
  chatBubble = addChatMessage('assistant', '', { streaming: true });
  setChatStreaming(true);

  window.api.chatSend({
    messages: chatMessages,
    context: {
      project,
      kinds: projectInfo?.kinds || [],
      files: attachments.filter(a => a.kind === 'file').map(a => ({ path: a.path, content: a.content })),
      log:   attachments.find(a => a.kind === 'log')?.content || null,
      editMode,
    },
  });
}

// ═══════════════════════════════════════════
//  コードの書き換え (AI駆動開発)
//
//  応答に含まれる ```codinable-edit path=… ブロックを変更案として取り出し、
//  差分を見せてから適用する。勝手に書き換えることはしない。
// ═══════════════════════════════════════════

const EDIT_BLOCK_RE = /^[ \t]*```+[ \t]*codinable-edit[ \t]+path=([^\n`]+)\n([\s\S]*?)\n[ \t]*```+[ \t]*$/gm;

/** 応答テキストから変更案を取り出す。取り出した部分は本文から取り除く。 */
function extractEditProposals(text) {
  const proposals = [];
  const body = text.replace(EDIT_BLOCK_RE, (_all, rawPath, content) => {
    const relPath = rawPath.trim().replace(/^["'`]|["'`]$/g, '').replace(/\\/g, '/');
    if (relPath) proposals.push({ relPath, content });
    return '';
  });
  return { body: body.replace(/\n{3,}/g, '\n\n').trim(), proposals };
}

/** 行単位の差分 (LCS)。戻りは [{ kind: 'keep'|'add'|'del', text }] */
function diffLines(before, after) {
  const a = before.split('\n');
  const b = after.split('\n');

  // 教材のファイルは短いので素直な DP で足りる。念のため上限を置き、
  // 超えたときは「全置換」として見せる (計算で固まらせないため)。
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

/** 変更案 1 件をカードとして描く */
async function renderEditProposal(proposal, container) {
  const current = await window.api.wsReadFile(project, proposal.relPath);
  const before  = current.ok ? String(current.content ?? '') : null;
  const after   = proposal.content;

  const card = document.createElement('div');
  card.className = 'edit-proposal';

  if (before !== null && before === after) {
    card.innerHTML =
      `<div class="edit-proposal-head"><span class="edit-proposal-path">` +
      `${escapeHtml(proposal.relPath)}</span>` +
      `<span class="edit-proposal-stat">${escapeHtml(t('editNoChange'))}</span></div>`;
    container.appendChild(card);
    return;
  }

  const rows    = before === null
    ? after.split('\n').map(text => ({ kind: 'add', text }))
    : diffLines(before, after);
  const added   = rows.filter(r => r.kind === 'add').length;
  const removed = rows.filter(r => r.kind === 'del').length;

  const diffHtml = collapseDiff(rows).map(row => row.kind === 'skip'
    ? `<div class="diff-row skip">${escapeHtml(tf('editDiffSkipped', { n: row.count }))}</div>`
    : `<div class="diff-row ${row.kind}"><span class="diff-mark">${DIFF_MARK[row.kind]}</span>` +
      `<span class="diff-text">${escapeHtml(row.text)}</span></div>`).join('');

  card.innerHTML =
    '<div class="edit-proposal-head">' +
      `<span class="edit-proposal-path">${escapeHtml(proposal.relPath)}</span>` +
      `<span class="edit-proposal-stat">${before === null ? escapeHtml(t('editNewFile')) + ' ' : ''}` +
        `<span class="diff-added">+${added}</span> <span class="diff-removed">-${removed}</span></span>` +
    '</div>' +
    `<div class="edit-diff">${diffHtml}</div>` +
    '<div class="edit-proposal-actions">' +
      `<button class="btn btn-apply" data-act="apply">${escapeHtml(t('editApply'))}</button>` +
      `<button class="btn" data-act="discard">${escapeHtml(t('editDiscard'))}</button>` +
    '</div>';

  const actions = card.querySelector('.edit-proposal-actions');
  card.querySelector('[data-act="apply"]').addEventListener('click', async () => {
    const res = await window.api.wsWriteFile(project, proposal.relPath, after);
    if (!res.ok) { await alertDialog(tf('saveFailed', { error: res.error || '' })); return; }
    await reopenFileFromDisk(proposal.relPath);
    await reloadTree();
    actions.innerHTML = `<span class="edit-proposal-done">${escapeHtml(t('editApplied'))}</span>`;
  });
  card.querySelector('[data-act="discard"]').addEventListener('click', () => {
    actions.innerHTML = `<span class="edit-proposal-done">${escapeHtml(t('editDiscarded'))}</span>`;
  });

  container.appendChild(card);
}

/** 適用後、開いているタブを disk の内容に入れ替える */
async function reopenFileFromDisk(relPath) {
  if (!openFiles.has(relPath)) { await openFile(relPath); return; }
  const res = await window.api.wsReadFile(project, relPath);
  if (!res.ok) return;
  const entry = openFiles.get(relPath);
  entry.cm.setValue(String(res.content ?? ''));
  entry.dirty = false;
  renderTabs();
}

/** 応答の描画。書き換えモードなら変更案をカードにして本文の下に並べる */
async function renderChatResponse(bubble, text, { editMode }) {
  if (!editMode) { bubble.innerHTML = renderMarkdown(text); return; }

  const { body, proposals } = extractEditProposals(text);
  bubble.innerHTML = renderMarkdown(body || text);
  if (!proposals.length) return;

  const wrap = document.createElement('div');
  wrap.className = 'edit-proposals';
  bubble.appendChild(wrap);
  for (const proposal of proposals) await renderEditProposal(proposal, wrap);
}

function clearChat() {
  chatMessages = [];
  attachments.length = 0;
  renderAttachments();
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
  $('version-badge').textContent = `v${appInfo.version}`;
  // 入力欄には既存のキーを出さない (伏せ字でも読み出せてしまうため)。
  // 空のまま保存したときは変更しない扱いにする。
  document.querySelectorAll('[data-key-field]').forEach(input => { input.value = ''; });
  updateModelHint();
  updateKeyBadges();
  renderCoursesInfo();
  renderRuntimeInfo();

  $('settings-overlay').classList.remove('hidden');
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

  // 観測に失敗しても「読み込み中」で固まらせない。
  // どれが取れなかったのかが分かるほうが原因に近づける。
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
  ];
  wrap.innerHTML = rows.map(([label, value]) =>
    `<div class="runtime-info-row"><span>${escapeHtml(label)}</span>` +
    `<span>${escapeHtml(value || t('runtimeMissing'))}</span></div>`).join('');
}

/**
 * インストールされている講座と、それがどこから読まれたかを出す。
 * 講座を足したのに出ないときの切り分け (置き場が違う / course.yaml が壊れている) に使う。
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
  updateLlmBadge();
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
  // ── ヘッダー ──
  // コース切り替え。開いているプロジェクトはそのままにして、演習一覧だけ入れ替える
  // (別の講座を見ながら今の作業を続けられるようにするため)
  $('active-course-select').addEventListener('change', ev => {
    localStorage.setItem('lastCourse', ev.target.value);
    renderExercises();
  });
  $('project-select').addEventListener('change', ev => selectProject(ev.target.value));
  $('btn-new-project').addEventListener('click', openNewProjectDialog);
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

  // ── 新規プロジェクト ──
  $('new-project-close').addEventListener('click', () =>
    $('new-project-overlay').classList.add('hidden'));
  $('new-project-overlay').addEventListener('click', ev => {
    if (ev.target === $('new-project-overlay')) $('new-project-overlay').classList.add('hidden');
  });
  $('course-select').addEventListener('change', renderTemplateList);
  $('btn-create-project').addEventListener('click', createProject);
  $('new-project-name').addEventListener('keydown', ev => {
    if (ev.key === 'Enter') createProject();
  });

  // ── ファイルツリー ──
  $('btn-new-file').addEventListener('click', () => createEntry('file'));
  $('btn-new-dir').addEventListener('click',  () => createEntry('dir'));
  $('btn-refresh-tree').addEventListener('click', reloadTree);
  document.addEventListener('click', closeTreeMenu);

  // ── エディタ ──
  $('btn-editor-undo').addEventListener('click', () => { activeEditor()?.undo(); updateHistoryButtons(); });
  $('btn-editor-redo').addEventListener('click', () => { activeEditor()?.redo(); updateHistoryButtons(); });
  $('btn-md-preview').addEventListener('click', toggleMdPreview);

  // ── 実行 ──
  $('btn-run').addEventListener('click', runSelected);
  $('btn-run-stop').addEventListener('click', stopRun);
  $('btn-preview').addEventListener('click', openPreview);
  $('run-target-select').addEventListener('change', () => {
    $('btn-run').disabled = running || !project || !$('run-target-select').value;
  });
  $('run-stdin-send').addEventListener('click', sendStdin);
  $('run-stdin-input').addEventListener('keydown', ev => {
    if (ev.key === 'Enter') sendStdin();
  });
  document.querySelectorAll('.run-tab').forEach(tab =>
    tab.addEventListener('click', () => showRunPane(tab.dataset.pane)));
  $('btn-add-log-context').addEventListener('click', attachRunLog);

  // ── SQL ──
  $('btn-sql-start').addEventListener('click', startSql);
  $('btn-sql-stop').addEventListener('click', stopSql);
  $('btn-sql-run').addEventListener('click', runSql);

  // ── プレビュー ──
  const view = $('mini-browser');
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
  $('btn-chat-send').addEventListener('click', () => sendChat());
  $('btn-chat-edit').addEventListener('click', () => sendChat({ editMode: true }));
  $('btn-chat-abort').addEventListener('click', () => window.api.chatAbort());
  $('btn-chat-clear').addEventListener('click', clearChat);
  $('btn-attach-file').addEventListener('click', attachCurrentFile);
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
      else if (!$('new-project-overlay').classList.contains('hidden')) {
        $('new-project-overlay').classList.add('hidden');
      } else if (!$('settings-overlay').classList.contains('hidden')) {
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

function sendStdin() {
  const input = $('run-stdin-input');
  const text  = input.value;
  if (!running) return;
  window.api.runStdin(`${text}\n`);
  appendRunOutput(`${text}\n`);
  input.value = '';
}

// ═══════════════════════════════════════════
//  main プロセスからの通知
// ═══════════════════════════════════════════

function wireIpc() {
  window.api.onRunOutput(text => appendRunOutput(text));
  window.api.onRunExit(async ({ code }) => {
    setRunning(false);
    appendRunOutput(code === 0 ? t('runExitOk') : tf('runExitNg', { code }));
    await refreshPreviewAvailability();
  });
  window.api.onRunUrl(({ url }) => previewUrl(url));
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
    // 途中では本文だけ描く (書き換えブロックは閉じるまで差分を出せない)
    chatBubble.innerHTML = renderMarkdown(
      chatEditMode ? extractEditProposals(chatBuffer).body || chatBuffer : chatBuffer);
    const history = $('chat-history');
    history.scrollTop = history.scrollHeight;
  });
  window.api.onChatEnd(async () => {
    const bubble = chatBubble;
    const text   = chatBuffer;
    const editMode = chatEditMode;
    chatBubble = null;
    setChatStreaming(false);
    if (!bubble) return;
    bubble.classList.remove('streaming');
    chatMessages.push({ role: 'assistant', content: text });
    await renderChatResponse(bubble, text, { editMode });
    const history = $('chat-history');
    history.scrollTop = history.scrollHeight;
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
  clearChat();
  clearTestResults();
  clearRunOutput();
  setRunning(false);
  setSqlRunning(false);
  setPreviewAvailable(false);
  wireEvents();
  wireIpc();

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
