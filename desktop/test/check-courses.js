// ═══════════════════════════════════════════════════════════
//  courses/*/course.yaml の設計ルールを確かめる (アプリは起動しない)
//
//  演習を足したり直したりしたときに、次が崩れていないかを見る
//    - openFiles (演習を選んだときに開くファイル) は雛形直下の README.md (演習の概要) 1 つだけ
//      DB・SQL の演習のテーブル構成も README.md に書く (SCHEMA.md は置かない)
//    - openFiles のファイルが雛形に実在する
//    - 演習が使うチャプター番号には、すべて chapters: に名前がある
//
//    node test/check-courses.js
// ═══════════════════════════════════════════════════════════

const fs   = require('fs');
const path = require('path');
const yaml = require('js-yaml');

function readText(file) {
  try { return fs.readFileSync(file, 'utf8'); } catch { return ''; }
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

    if (open.length !== 1 || open[0] !== 'README.md') {
      ng(where, `openFiles は README.md (演習の概要) 1 つだけ: ${open.join(', ') || '(なし)'}`);
    }
    if (fs.existsSync(path.join(dir, 'SCHEMA.md'))) ng(where, 'SCHEMA.md は置かない (テーブル構成は README.md に書く)');
    if (e.runtime === 'sql' && !/この演習で使うテーブル|テーブル/.test(readText(path.join(dir, 'README.md')))) {
      ng(where, 'SQL の演習の README.md にテーブル構成が無い');
    }
    for (const f of open) {
      if (!fs.existsSync(path.join(dir, f))) ng(where, `openFiles のファイルが無い: ${f}`);
    }
  }
}

for (const p of problems) console.log('NG   ' + p);
console.log(problems.length ? `NG ${problems.length} 件` : `すべて期待どおり (${exercises} 演習)`);
process.exit(problems.length ? 1 : 0);
