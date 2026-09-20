// ═══════════════════════════════════════════════════════════
//  コースパック
//
//  Codinable は問題を出して解かせることをしない。講座との「連動」は
//  「講義で出てくるサンプルを、開いてすぐ動かせる形で配る」ことだけで実現する。
//
//    courses/
//    └── <courseId>/
//        ├── course.yaml          … 講座のメタ情報 + 演習の一覧
//        └── templates/<name>/    … 雛形の中身 (そのままワークスペースへコピーされる)
//
//  **演習 (exercise)** = 動かして確かめる 1 単位。講座のレッスンと 1 対 1 で対応し、
//  「どのレッスンのものか (chapter / lesson)」「何で動くか (runtime)」
//  「どう動かすか (run)」を自分で持つ。受講者が演習を選ぶだけで実行環境まで
//  決まるようにするためで、これが雛形との違いである。
//
//  雛形は「開いてすぐ動く最小構成」に徹する。解説は Udemy の動画側にあるため、
//  ここに講義内容を持ち込まない。
//
//  別の Udemy 講座を足すときは courses/ にディレクトリを 1 つ増やすだけでよい
//  (アプリ側の変更は不要)。
// ═══════════════════════════════════════════════════════════

const fs   = require('fs');
const path = require('path');
const yaml = require('js-yaml');
const { app } = require('electron');

const { getRepoRoot } = require('./runtimes');

/**
 * コースパックの置き場所。
 * パッケージ後は asar の中 (resources/app.asar/courses)。雛形は読むだけなので
 * Electron の asar 透過読み込みでそのまま扱える。
 */
function getCoursesDir() {
  return app.isPackaged
    ? path.join(__dirname, '..', '..', 'courses')
    : path.join(getRepoRoot(), 'courses');
}

/** names / descriptions のような多言語マップから 1 言語を取り出す */
function pickLang(map, lang, fallback = '') {
  if (!map) return fallback;
  if (typeof map === 'string') return map;
  return map[lang] || map.ja || map.en || Object.values(map)[0] || fallback;
}

let cache = null;

/**
 * courses/ 配下を読み込む。
 * @param {string} lang UI 言語 ('ja' | 'en')
 */
function loadCourses(lang = 'ja') {
  if (cache && cache.lang === lang) return cache.courses;

  const coursesDir = getCoursesDir();
  const courses = [];

  let dirents = [];
  try {
    dirents = fs.readdirSync(coursesDir, { withFileTypes: true })
      .filter(e => e.isDirectory())
      .sort((a, b) => a.name.localeCompare(b.name));
  } catch {
    console.warn('[courses] not found:', coursesDir);
    return courses;
  }

  for (const entry of dirents) {
    const courseDir = path.join(coursesDir, entry.name);
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
          // 実行環境 (java / spring / node / react / python / static / sql / shell)。
          // 一覧のアイコンとタグに使う
          runtime:     String(t.runtime || 'other'),
          // 実行対象の既定値。renderer の実行対象 select と同じ書式で、
          //   file:<相対パス> / gradle:<タスク> / npm:<スクリプト> / static:<ルート> / java
          // を受け付ける。演習を選んだだけで「実行」が押せる状態にするためのもの。
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
      name:        pickLang(meta.names, lang, entry.name),
      description: pickLang(meta.descriptions, lang, ''),
      udemyUrl:    meta.udemy && meta.udemy.url ? String(meta.udemy.url) : null,
      exercises,
    });
  }

  courses.sort((a, b) => a.order - b.order || a.id.localeCompare(b.id));
  cache = { lang, courses };
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

module.exports = { getCoursesDir, loadCourses, findTemplateDir, clearCache };
