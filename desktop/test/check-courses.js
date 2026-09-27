// ═══════════════════════════════════════════════════════════
//  courses/*/course.yaml の設計ルールを確かめる (アプリは起動しない)
//
//  演習を足したり直したりしたときに、次が崩れていないかを見る
//    - openFiles (演習を選んだときに開くファイル) は 1 つが原則。2 つにしてよいのは
//        ・動かす前に読む手順が README.md にしか無いとき … README.md + 主役 (主役が表に出る)
//        ・DB を題材にする演習 … 主役 + SCHEMA.md (テーブル構成が表に出る)。
//          どの演習が該当するかは scripts/build-schema-docs.js の TARGETS
//    - openFiles のファイルが雛形に実在する
//    - SQL の演習は SCHEMA.md、静的ページの演習は index.html を開く
//    - 演習が使うチャプター番号には、すべて chapters: に名前がある
//
//    node test/check-courses.js
// ═══════════════════════════════════════════════════════════

const fs   = require('fs');
const path = require('path');
const yaml = require('js-yaml');
const { TARGETS: DB_TARGETS } = require('../scripts/build-schema-docs');

/** DB を題材にする演習か (テーブル構成の SCHEMA.md を最初に見せる演習) */
function isDbExercise(course, id) {
  const spec = DB_TARGETS[course];
  if (!spec) return false;
  return Array.isArray(spec) ? spec.includes(id) : !spec.except.includes(id);
}

const COURSES_DIR = path.resolve(__dirname, '../../courses');
const problems = [];
const ng = (where, message) => problems.push(`${where}: ${message}`);

let exercises = 0;
for (const entry of fs.readdirSync(COURSES_DIR, { withFileTypes: true })) {
  const file = path.join(COURSES_DIR, entry.name, 'course.yaml');
  if (!entry.isDirectory() || !fs.existsSync(file)) continue;
  const course = yaml.load(fs.readFileSync(file, 'utf8'));
  const chapters = course.chapters || {};

  for (const e of course.exercises || []) {
    exercises++;
    const where = `${entry.name}/${e.id}`;
    const dir = path.join(COURSES_DIR, entry.name, 'templates', e.dir || e.id);
    const open = e.openFiles || [];

    if (e.chapter !== undefined && e.chapter !== null) {
      const names = chapters[e.chapter] || chapters[String(e.chapter)];
      if (!names?.ja || !names?.en) ng(where, `チャプター ${e.chapter} の名前 (ja / en) が chapters: に無い`);
    }

    if (open.length === 0) ng(where, 'openFiles が無い (最初に見せるファイルを 1 つ決める)');
    if (open.length > 2) ng(where, `openFiles が ${open.length} 個ある (1 つが原則)`);
    if (open.length === 2 && open[0] !== 'README.md' && open[1] !== 'SCHEMA.md') {
      ng(where, `2 つ開くのは「README.md + 主役」「主役 + SCHEMA.md」のときだけ: ${open.join(', ')}`);
    }
    if (isDbExercise(entry.name, e.id) && (open.length !== 2 || open[1] !== 'SCHEMA.md')) {
      ng(where, `DB の演習は「主役 + SCHEMA.md」(テーブル構成を表に出す): ${open.join(', ')}`);
    }
    for (const f of open) {
      if (!fs.existsSync(path.join(dir, f))) ng(where, `openFiles のファイルが無い: ${f}`);
    }
    if (e.runtime === 'sql' && open.join() !== 'SCHEMA.md') ng(where, `SQL の演習は SCHEMA.md だけを開く: ${open.join(', ')}`);
    if (e.runtime === 'static' && open.join() !== 'index.html') ng(where, `静的ページの演習は index.html だけを開く: ${open.join(', ')}`);
  }
}

for (const p of problems) console.log('NG   ' + p);
console.log(problems.length ? `NG ${problems.length} 件` : `すべて期待どおり (${exercises} 演習)`);
process.exit(problems.length ? 1 : 0);
