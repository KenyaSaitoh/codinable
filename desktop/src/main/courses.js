// ═══════════════════════════════════════════════════════════
//  コースパック
//
//  Codinable は問題を出して解かせることをしない。講座との「連動」は
//  「講義で出てくるサンプルを、開いてすぐ動かせる形で配る」ことだけで実現する
//
//    courses/
//    └── <courseId>/
//        ├── course.yaml          … 講座のメタ情報 + 演習の一覧
//        └── templates/<name>/    … 雛形の中身 (そのままワークスペースへコピーされる)
//
//  この courses/ は 3 か所にある。読む順は 同梱 → 共有 → 個人 で、同じ id が
//  あればバージョンの新しいほうを採る (同じなら後の置き場が勝つ)。
//  ただし開始した講座は、開始したときの版に固定する (config の coursePins)
//
//    同梱: アプリの中 (asar)                     … インストーラ (全講座入りの 1 本) が持ってきたもの
//    共有: %PROGRAMDATA%\Codinable\courses       … 管理者が PC 全体へ配るときの置き場
//    個人: <userData>\courses                    … 配信された新しい版 (<id>@<version>/) と、
//                                                   手で足す・差し替えるもの
//
//  講座はアプリ本体と別に更新できる。講座を新しく始めるときに配信先を確認し、
//  新しい版があれば個人の置き場へ取り込んでから始める (prepareCourseStart)
//  どのコースを見ているかは画面左上の「新規コース開始・再開」で切り替える
//
//  **演習 (exercise)** = 動かして確かめる 1 単位。講座のレッスンと 1 対 1 で対応し、
//  「どのレッスンのものか (chapter / lesson)」「何で動くか (runtime)」
//  「どう動かすか (run)」を自分で持つ。受講者が演習を選ぶだけで実行環境まで
//  決まるようにするためで、これが雛形との違いである
//
//  雛形は「開いてすぐ動く最小構成」に徹する。解説は Udemy の動画側にあるため、
//  ここに講義内容を持ち込まない
//
//  別の Udemy 講座を足すときは courses/ にディレクトリを 1 つ増やすだけでよい
//  (アプリ側の変更は不要)
// ═══════════════════════════════════════════════════════════

const fs     = require('fs');
const path   = require('path');
const zlib   = require('zlib');
const crypto = require('crypto');
const yaml   = require('js-yaml');
const { app } = require('electron');

const { getRepoRoot } = require('./runtimes');
const { PRODUCT, getUpdateUrls } = require('../app-config');
const config = require('./config');

// ── 演習ごとに出す出力タブ ─────────────────────────────────
//
// 静的ページの演習に SQL やメッセージングのタブが並んでいても迷うだけなので、
// 演習ごとに使うタブを決める。course.yaml の exercises[].tabs で明示でき、
// 書かなければ runtime と雛形の中身から決める
//   tests     … build.gradle がある (Gradle の test の結果を出す場所)
//   messaging … codinable.services.json がある (Kafka / RabbitMQ を使う)
// 実行結果 (output) はどの演習でも出す
const TAB_IDS = ['output', 'tests', 'sql', 'terminal', 'messaging', 'preview'];
const DEFAULT_TABS = {
  static: ['output', 'preview'],
  java:   ['output', 'terminal'],
  spring: ['output', 'terminal', 'preview'],
  node:   ['output', 'terminal', 'preview'],
  react:  ['output', 'terminal', 'preview'],
  python: ['output', 'terminal', 'preview'],
  sql:    ['output', 'sql'],
  shell:  ['output', 'terminal'],
};

function exerciseTabs(entry, templateDir, runtime) {
  const tabs = new Set(['output']);
  if (Array.isArray(entry.tabs)) {
    for (const tab of entry.tabs.map(String)) if (TAB_IDS.includes(tab)) tabs.add(tab);
  } else {
    for (const tab of DEFAULT_TABS[runtime] || TAB_IDS) tabs.add(tab);
    const has = name => fs.existsSync(path.join(templateDir, name));
    if (has('build.gradle') || has('build.gradle.kts')) tabs.add('tests');
    if (has('codinable.services.json')) tabs.add('messaging');
  }
  return TAB_IDS.filter(tab => tabs.has(tab));
}

/**
 * 同梱のコースパックの置き場所
 * パッケージ後は asar の中 (resources/app.asar/courses)。雛形は読むだけなので
 * Electron の asar 透過読み込みでそのまま扱える
 */
function getCoursesDir() {
  return app.isPackaged
    ? path.join(__dirname, '..', '..', 'courses')
    : path.join(getRepoRoot(), 'courses');
}

/**
 * 全ユーザー共有のコース置き場 (Windows のみ)
 * 管理者が PC 全体へ講座を配るときの置き場。受講者向けのインストーラは
 * 全講座入りの 1 本なので、ふだんは空のまま
 */
function getSharedCoursesDir() {
  const base = process.env.ProgramData || process.env.ALLUSERSPROFILE;
  return base ? path.join(base, PRODUCT.dataDirName, 'courses') : null;
}

/** 個人用のコース置き場。手で足したコースが同梱・共有より優先される */
function getUserCoursesDir() {
  return path.join(app.getPath('userData'), 'courses');
}

/**
 * 読む順に並べたコース置き場。後に来るものが優先 (同じ id は後勝ち)
 * 同梱 → 共有 → 個人 の順で、あとから足したコースで上書きできる
 */
function getCourseRoots() {
  const roots = [{ source: 'bundled', dir: getCoursesDir() }];
  const shared = getSharedCoursesDir();
  if (shared) roots.push({ source: 'shared', dir: shared });
  roots.push({ source: 'user', dir: getUserCoursesDir() });
  return roots.map(r => ({ ...r, exists: fs.existsSync(r.dir) }));
}

/**
 * '1.2.0' のようなバージョンを比べる。数値の並びとして見て、
 * 桁数が違うものは短いほうを 0 埋めして扱う (1.2 < 1.2.1)
 */
function compareVersions(a, b) {
  const pa = String(a || '0').split('.').map(n => parseInt(n, 10) || 0);
  const pb = String(b || '0').split('.').map(n => parseInt(n, 10) || 0);
  for (let i = 0; i < Math.max(pa.length, pb.length); i++) {
    const diff = (pa[i] || 0) - (pb[i] || 0);
    if (diff) return diff;
  }
  return 0;
}

/** names / descriptions のような多言語マップから 1 言語を取り出す */
function pickLang(map, lang, fallback = '') {
  if (!map) return fallback;
  if (typeof map === 'string') return map;
  return map[lang] || map.ja || map.en || Object.values(map)[0] || fallback;
}

/**
 * 開発時だけのコースの絞り込み (npm start で選んだコース。scripts/start.js が渡す)
 * 「全コース入り」「コース A だけ」「A と B」のような受講者の環境を、
 * 置き場を触らずに手元で再現して確かめるためのもの
 * パッケージ後は環境変数が残っていても効かせない (受講者の講座が消えて見えるため)
 *
 * @returns {Set<string>|null} 絞り込まないときは null
 */
function getDevCourseFilter() {
  if (app.isPackaged) return null;
  const raw = String(process.env.CODINABLE_COURSES || '').trim();
  if (!raw || raw === 'all') return null;
  return new Set(raw.split(',').map(s => s.trim()).filter(Boolean));
}

let cache = null;

/**
 * 置き場すべてから、同じ id ごとに候補 (版違い・置き場違い) を集める
 * 並びは読んだ順 (同梱 → 共有 → 個人)
 */
function collectCandidates(lang) {
  const byId = new Map();
  for (const root of getCourseRoots()) {
    for (const course of readCourseRoot(root, lang)) {
      if (!byId.has(course.id)) byId.set(course.id, []);
      byId.get(course.id).push(course);
    }
  }
  return byId;
}

/** 候補のうち最も新しい版 (同じなら後の置き場が勝つ) */
function newestOf(candidates) {
  let best = null;
  for (const course of candidates) {
    if (!best || compareVersions(course.version, best.version) >= 0) best = course;
  }
  return best;
}

/**
 * コース置き場すべてを読み込む
 *
 * 同じ id のコースが複数あるときは、次の順で 1 つに決める
 *   1. 開始した講座は、開始したときの版 (config の coursePins)。取り組み中の講座は
 *      新しい版が届いても変えない
 *   2. それ以外は版の新しいほう (同じなら後の置き場が勝つ)
 *
 * @param {string} lang UI 言語 ('ja' | 'en')
 */
function loadCourses(lang = 'ja') {
  if (cache && cache.lang === lang) return cache.courses;

  const pins = config.getCoursePins();
  const chosen = [];
  for (const [id, candidates] of collectCandidates(lang)) {
    const pinned = pins[id]
      ? candidates.filter(c => compareVersions(c.version, pins[id]) === 0)
      : [];
    chosen.push(pinned.length ? pinned[pinned.length - 1] : newestOf(candidates));
  }

  // 絞り込みは 3 つの置き場すべてに効かせる (個人フォルダに残ったコースも隠す)
  const filter = getDevCourseFilter();
  const courses = chosen
    .filter(c => !filter || filter.has(c.id))
    .sort((a, b) => a.order - b.order || a.id.localeCompare(b.id));
  cache = { lang, courses };
  return courses;
}

// ═══════════════════════════════════════════════════════════
//  講座の単独更新
//
//  アプリ本体とは別に、講座だけを新しい版へ入れ替えられるようにする
//  確認するのは「講座を新しく始めるとき」だけで、取り組み中の講座は
//  開始したときの版のまま動かす (loadCourses の coursePins)
//
//  配信物は GitHub Releases の courses タグに置いた index.json と <id>-<version>.codpack
//  (scripts/build-courses.js が作る)。codpack は講座フォルダの中身を
//  gzip した JSON 1 つで、展開に外部ツールを要らないようにしてある
//  取り込み先は個人の置き場の <id>@<version>/ で、版ごとに別フォルダにする
// ═══════════════════════════════════════════════════════════

const FETCH_TIMEOUT_MS = 8000;

async function fetchWithTimeout(url, timeoutMs) {
  const { net } = require('electron');
  const res = await net.fetch(url, { signal: AbortSignal.timeout(timeoutMs), cache: 'no-store' });
  if (!res.ok) throw new Error(`HTTP ${res.status}: ${url}`);
  return res;
}

async function fetchCourseIndex(coursesUrl) {
  const res = await fetchWithTimeout(`${coursesUrl}/index.json`, FETCH_TIMEOUT_MS);
  const index = await res.json();
  return Array.isArray(index?.courses) ? index.courses : [];
}

/** codpack のファイルパスとして受け付けるか (置き場の外へ出るものは拒む) */
function isSafeRelPath(rel) {
  if (typeof rel !== 'string' || !rel || rel.length > 400) return false;
  if (path.isAbsolute(rel) || /^[a-zA-Z]:/.test(rel) || rel.startsWith('\\\\')) return false;
  return rel.split(/[\\/]/).every(seg => seg && seg !== '.' && seg !== '..');
}

/**
 * codpack を展開して個人の置き場へ入れる
 * いったん作業用フォルダへ書き出し、揃ってから名前を付け替える
 * (途中で止まっても、壊れた講座が読み込まれないようにするため)
 */
function installCoursePack(buffer, { id, version }) {
  const pack = JSON.parse(zlib.gunzipSync(buffer).toString('utf8'));
  if (pack?.format !== 1 || pack.id !== id || compareVersions(pack.version, version) !== 0) {
    throw new Error('course pack does not match the index');
  }
  const files = Array.isArray(pack.files) ? pack.files : [];
  if (!files.some(f => f.path === 'course.yaml')) throw new Error('course pack has no course.yaml');

  const userDir = ensureUserCoursesDir();
  const staging = path.join(userDir, `.incoming-${id}-${process.pid}-${Date.now()}`);
  try {
    for (const file of files) {
      if (!isSafeRelPath(file.path)) throw new Error(`unsafe path in course pack: ${file.path}`);
      const target = path.join(staging, ...file.path.split(/[\\/]/));
      fs.mkdirSync(path.dirname(target), { recursive: true });
      fs.writeFileSync(target, Buffer.from(String(file.data || ''), 'base64'));
    }
    const meta = yaml.load(fs.readFileSync(path.join(staging, 'course.yaml'), 'utf8')) || {};
    if (String(meta.id) !== id || compareVersions(meta.version, version) !== 0) {
      throw new Error('course.yaml in the pack does not match the index');
    }
    const finalDir = path.join(userDir, `${id}@${version}`);
    fs.rmSync(finalDir, { recursive: true, force: true });
    fs.renameSync(staging, finalDir);
    return finalDir;
  } finally {
    fs.rmSync(staging, { recursive: true, force: true });
  }
}

/** 講座フォルダを丸ごと写す。asar の中からも読めるよう readFileSync で写す */
function copyCourseTree(from, to) {
  fs.mkdirSync(to, { recursive: true });
  for (const entry of fs.readdirSync(from, { withFileTypes: true })) {
    const src = path.join(from, entry.name);
    const dst = path.join(to, entry.name);
    if (entry.isDirectory()) copyCourseTree(src, dst);
    else fs.writeFileSync(dst, fs.readFileSync(src));
  }
}

/**
 * 講座の版を固定する
 * 同梱の版は、アプリを更新すると入れ替わってしまう。取り組み中の講座が
 * 変わらないよう、パッケージ後は個人の置き場へ写しを取ってから固定する
 * (開発時は写さない。courses/ を直したらすぐ反映されてほしいため)
 */
function pinCourse(course, { onlyIfAbsent = false } = {}) {
  if (!course) return;
  if (onlyIfAbsent && config.getCoursePins()[course.id]) return;
  if (app.isPackaged && course.source === 'bundled') {
    const snapshot = path.join(ensureUserCoursesDir(), `${course.id}@${course.version}`);
    if (!fs.existsSync(path.join(snapshot, 'course.yaml'))) {
      try {
        const staging = `${snapshot}.incoming-${process.pid}`;
        fs.rmSync(staging, { recursive: true, force: true });
        copyCourseTree(course.path, staging);
        fs.renameSync(staging, snapshot);
      } catch (err) {
        console.warn(`[courses] snapshot failed: ${course.id}@${course.version}:`, err.message);
      }
    }
  }
  config.setCoursePin(course.id, course.version, { onlyIfAbsent });
}

/** 個人の置き場にある、固定していない古い版を片づける */
function pruneUserVersions(courseId, keepVersion) {
  const userDir = getUserCoursesDir();
  let entries = [];
  try { entries = fs.readdirSync(userDir, { withFileTypes: true }); } catch { return; }
  for (const entry of entries) {
    if (!entry.isDirectory()) continue;
    const m = entry.name.match(/^(.+)@([^@]+)$/);
    if (!m || m[1] !== courseId || compareVersions(m[2], keepVersion) === 0) continue;
    try { fs.rmSync(path.join(userDir, entry.name), { recursive: true, force: true }); } catch { /* 使用中なら次の機会に */ }
  }
}

/**
 * 講座を新しく始める準備
 * 配信先に新しい版があれば取り込み、なければ手元の版のまま、その版に固定する
 * 配信先に届かないとき (オフライン・未設定) も、手元の版で始められるようにする
 *
 * @returns {Promise<{ok:boolean, version?:string, previous?:string, updated:boolean,
 *                    checked:boolean, offline?:boolean, error?:string}>}
 */
async function prepareCourseStart(courseId, lang = 'ja') {
  const installed = () => newestOf(collectCandidates(lang).get(courseId) || []);
  const before = installed();
  const result = { ok: true, courseId, updated: false, checked: false };

  const coursesUrl = getUpdateUrls().courses;
  if (coursesUrl) {
    try {
      const entry = (await fetchCourseIndex(coursesUrl)).find(c => c.id === courseId);
      result.checked = true;
      if (entry && (!before || compareVersions(entry.version, before.version) > 0)) {
        if (!/^[A-Za-z0-9._@-]+$/.test(String(entry.file || ''))) throw new Error('bad file name in index');
        const res = await fetchWithTimeout(`${coursesUrl}/${entry.file}`, 120_000);
        const buffer = Buffer.from(await res.arrayBuffer());
        const digest = crypto.createHash('sha256').update(buffer).digest('hex');
        if (entry.sha256 && digest !== String(entry.sha256).toLowerCase()) {
          throw new Error('checksum mismatch');
        }
        installCoursePack(buffer, { id: courseId, version: String(entry.version) });
        result.updated = true;
        result.previous = before?.version || null;
      }
    } catch (err) {
      result.offline = !result.checked;
      result.error = err.message;
      console.warn(`[courses] update check failed for ${courseId}:`, err.message);
    }
  }

  clearCache();
  const chosen = installed();
  if (!chosen) return { ...result, ok: false, error: result.error || 'course-not-found' };
  pinCourse(chosen);
  pruneUserVersions(courseId, chosen.version);
  clearCache();
  return { ...result, version: chosen.version };
}

/**
 * すでに取り組んでいる講座 (ワークスペースに作業用プロジェクトがある講座) を、
 * 今の版に固定する。講座の版の固定を入れる前から使っている環境向けの補い
 */
function pinStartedCourses(courseIds, lang = 'ja') {
  const pins = config.getCoursePins();
  const todo = [...new Set(courseIds)].filter(id => id && !pins[id]);
  if (!todo.length) return;
  const candidates = collectCandidates(lang);
  for (const id of todo) pinCourse(newestOf(candidates.get(id) || []), { onlyIfAbsent: true });
  clearCache();
}

/** 置き場 1 つぶんを読む。壊れているコースは飛ばし、他のコースは使えるようにする */
function readCourseRoot(root, lang) {
  const courses = [];

  let dirents = [];
  try {
    dirents = fs.readdirSync(root.dir, { withFileTypes: true })
      .filter(e => e.isDirectory())
      .sort((a, b) => a.name.localeCompare(b.name));
  } catch {
    // 共有・個人の置き場は無いのが普通なので、同梱だけ警告する
    if (root.source === 'bundled') console.warn('[courses] not found:', root.dir);
    return courses;
  }

  for (const entry of dirents) {
    const courseDir = path.join(root.dir, entry.name);
    const yamlPath  = path.join(courseDir, 'course.yaml');
    if (!fs.existsSync(yamlPath)) continue;

    let meta;
    try {
      meta = yaml.load(fs.readFileSync(yamlPath, 'utf8')) || {};
    } catch (err) {
      console.warn(`[courses] ${entry.name}/course.yaml parse error:`, err.message);
      continue;
    }

    const exercises = (Array.isArray(meta.exercises) ? meta.exercises : [])
      .map(t => {
        const dirName = String(t.dir || t.id || '').trim();
        if (!dirName) return null;
        const templateDir = path.join(courseDir, 'templates', dirName);
        if (!fs.existsSync(templateDir)) {
          console.warn(`[courses] template missing: ${entry.name}/templates/${dirName}`);
          return null;
        }
        return {
          id:          String(t.id || dirName),
          dir:         templateDir,
          // 作業用プロジェクト名の既定値
          suggestName: String(t.suggestName || t.id || dirName),
          // 対応する講義の位置。chapter はバッジ、lesson は副題として出る
          chapter:     t.chapter === undefined || t.chapter === null ? null : String(t.chapter),
          lesson:      t.lesson ? String(t.lesson) : null,
          // 実行環境 (java / spring / node / react / python / static / sql / shell)
          // 一覧のアイコンとタグに使う
          runtime:     String(t.runtime || 'other'),
          // 実行対象の既定値。renderer の実行対象 select と同じ書式で、
          //   file:<相対パス> / gradle:<タスク> / npm:<スクリプト> / static:<ルート> / java
          // を受け付ける。演習を選んだだけで「実行」が押せる状態にするためのもの
          run:         t.run ? String(t.run) : null,
          name:        pickLang(t.names, lang, dirName),
          description: pickLang(t.descriptions, lang, ''),
          // 演習を開いた直後に開いておくファイル
          openFiles:   Array.isArray(t.openFiles) ? t.openFiles.map(String) : [],
          // 出力欄に出すタブ (output / tests / sql / terminal / messaging / preview)
          tabs:        exerciseTabs(t, templateDir, String(t.runtime || 'other')),
        };
      })
      .filter(Boolean);

    courses.push({
      id:          String(meta.id || entry.name),
      folder:      entry.name,
      order:       Number.isFinite(meta.order) ? meta.order : 999,
      // 同じコースが複数の置き場にあるときの新旧判定に使う
      version:     meta.version ? String(meta.version) : '0',
      // どこから読んだか (設定画面に出す。トラブル時に効く)
      source:      root.source,
      path:        courseDir,
      name:        pickLang(meta.names, lang, entry.name),
      description: pickLang(meta.descriptions, lang, ''),
      udemyUrl:    meta.udemy && meta.udemy.url ? String(meta.udemy.url) : null,
      // チャプター番号 → 名前 (演習一覧の見出し「チャプター3 フロントエンドの基本技術」)
      chapters:    Object.fromEntries(Object.entries(meta.chapters || {})
        .map(([n, names]) => [String(n), pickLang(names, lang, '')])),
      exercises,
    });
  }

  return courses;
}

/** courseId + exerciseId から雛形ディレクトリを引く */
function findTemplateDir(courseId, exerciseId, lang = 'ja') {
  const course = loadCourses(lang).find(c => c.id === courseId);
  if (!course) return null;
  const exercise = course.exercises.find(e => e.id === exerciseId);
  return exercise ? exercise.dir : null;
}

function clearCache() {
  cache = null;
}

/**
 * インストールされているコースと、その置き場の一覧
 * 「今どのコースが入っていて、どこから読まれているか」を設定画面に出すためのもの
 */
function describeCourses(lang = 'ja') {
  return {
    roots: getCourseRoots(),
    userDir: getUserCoursesDir(),
    // 開発時の絞り込み中なら、その id 一覧 (「講座が足りない」と迷わないように出す)
    devFilter: getDevCourseFilter() ? [...getDevCourseFilter()] : null,
    courses: loadCourses(lang).map(c => ({
      id: c.id, name: c.name, version: c.version, source: c.source,
      path: c.path, exerciseCount: c.exercises.length,
    })),
  };
}

/** 個人用のコース置き場を作る (無ければ)。手でコースを足せるようにするため */
function ensureUserCoursesDir() {
  const dir = getUserCoursesDir();
  try { fs.mkdirSync(dir, { recursive: true }); } catch { /* 作れなくても読み込みは続けられる */ }
  return dir;
}

module.exports = {
  getCoursesDir, getSharedCoursesDir, getUserCoursesDir, getCourseRoots,
  loadCourses, findTemplateDir, clearCache, describeCourses, ensureUserCoursesDir,
  prepareCourseStart, pinStartedCourses, compareVersions, TAB_IDS,
};
