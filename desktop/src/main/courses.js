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
//  あればバージョンの新しいほうを採る (同じなら後の置き場が勝つ)
//
//    同梱: アプリの中 (asar)                     … そのインストーラが持ってきたコース
//    共有: %PROGRAMDATA%\Codinable\courses       … コース単位のインストーラが足す場所
//    個人: <userData>\courses                    … 手で足す・差し替える場所
//
//  受講者が 2 つ目の講座を受けるときは、アプリを入れ直さずコースだけが増える
//  どのコースを見ているかは画面左上のコース選択で切り替える
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

const fs   = require('fs');
const path = require('path');
const yaml = require('js-yaml');
const { app } = require('electron');

const { getRepoRoot } = require('./runtimes');
const { PRODUCT } = require('../app-config');

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
 * コース単位のインストーラはここへ course.yaml と templates/ を置く
 * アプリ本体を入れ直さずにコースだけ増やせるようにするための領域
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

let cache = null;

/**
 * コース置き場すべてを読み込む
 *
 * 同じ id のコースが複数の置き場にあるときは、バージョンが新しいほうを採る
 * (同じなら後の置き場が勝つ)。コース単位のインストーラが古いパックを
 * 置いても、新しいものが選ばれている状態を保つため
 *
 * @param {string} lang UI 言語 ('ja' | 'en')
 */
function loadCourses(lang = 'ja') {
  if (cache && cache.lang === lang) return cache.courses;

  const byId = new Map();
  for (const root of getCourseRoots()) {
    for (const course of readCourseRoot(root, lang)) {
      const current = byId.get(course.id);
      if (current && compareVersions(course.version, current.version) < 0) continue;
      byId.set(course.id, course);
    }
  }

  const courses = [...byId.values()]
    .sort((a, b) => a.order - b.order || a.id.localeCompare(b.id));
  cache = { lang, courses };
  return courses;
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
};
