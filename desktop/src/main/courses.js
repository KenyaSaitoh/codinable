// ═══════════════════════════════════════════════════════════
//  コースパック
//
//  Codinable は演習問題も解説も持たない。講座との「連動」は
//  「その講座で出てくるサンプルプロジェクトの雛形を配る」ことだけで実現する。
//
//    courses/
//    └── <courseId>/
//        ├── course.yaml          … 講座のメタ情報 + 雛形の一覧
//        └── templates/<name>/    … 雛形の中身 (そのままワークスペースへコピーされる)
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

    const templates = (Array.isArray(meta.templates) ? meta.templates : [])
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
          // 新規プロジェクト名の既定値
          suggestName: String(t.suggestName || t.id || dirName),
          chapter:     t.chapter ? String(t.chapter) : null,
          kind:        String(t.kind || 'other'),
          name:        pickLang(t.names, lang, dirName),
          description: pickLang(t.descriptions, lang, ''),
          // README 等を開いた状態で始めたいときに使う
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
      templates,
    });
  }

  courses.sort((a, b) => a.order - b.order || a.id.localeCompare(b.id));
  cache = { lang, courses };
  return courses;
}

/** courseId + templateId から雛形ディレクトリを引く */
function findTemplateDir(courseId, templateId, lang = 'ja') {
  const course = loadCourses(lang).find(c => c.id === courseId);
  if (!course) return null;
  const template = course.templates.find(t => t.id === templateId);
  return template ? template.dir : null;
}

function clearCache() {
  cache = null;
}

module.exports = { getCoursesDir, loadCourses, findTemplateDir, clearCache };
