// ═══════════════════════════════════════════════════════════
//  DB を題材にする演習の SCHEMA.md (使うテーブルの構成) を作り直す
//
//    node scripts/build-schema-docs.js
//
//  DB アクセス (JDBC / MyBatis / JPA / トランザクション …) の演習は、コードより先に
//  「どんなテーブルを読み書きするのか」が分かっていないと読めない。そこで SQL の演習と
//  同じく、テーブル構成の SCHEMA.md を最初に開く (course.yaml の openFiles)
//
//  中身は雛形の実際の DDL / DML を HSQLDB に流して作る (scripts/SchemaDoc.java)
//  DDL を直したら、これを流し直せば説明も追従する
//  テーブルをエンティティから自動生成する演習 (JPA の drop-and-create) は DDL が無いので、
//  SCHEMA.md を手で書き、ここでは触らない (MANUAL)
// ═══════════════════════════════════════════════════════════

const fs   = require('fs');
const path = require('path');
const yaml = require('js-yaml');
const { spawnSync } = require('child_process');

const ROOT    = path.resolve(__dirname, '../..');
const COURSES = path.join(ROOT, 'courses');
const JAVA    = path.join(ROOT, 'runtime', 'java', 'bin', process.platform === 'win32' ? 'java.exe' : 'java');
const HSQLDB  = fs.readdirSync(path.join(ROOT, 'hsqldb')).find(f => /^hsqldb-.*\.jar$/.test(f));

// DB を題材にする演習 (講座 → 演習 ID)。null は「その講座の全演習」
const TARGETS = {
  'java-db-access':               { all: true, except: ['jdbc-connection'] },   // 接続だけでテーブルを使わない
  'spring-boot-webapp-basic':     ['spring-mvc-employee-jdbc', 'spring-mvc-employee-jpa',
                                   'spring-mvc-employee-mybatis', 'spring-mvc-tx', 'spring-mvc-tx-jta'],
  'spring-boot-webapp-practical': ['leaf-books-mvc-jpa'],
  'spring-boot-cicd':             ['database-rider-employee'],
};

// テーブルをエンティティから作る (DDL が無い) ので、SCHEMA.md を手で書いている雛形
const MANUAL = new Set(['java-db-access/jpa-crud', 'java-db-access/jpa-relations']);

function sourcesOf(dir) {
  const rel = p => path.relative(dir, p).split(path.sep).join('/');
  const hsqldb = path.join(dir, 'sql', 'hsqldb');
  if (fs.existsSync(hsqldb)) {
    const files = fs.readdirSync(hsqldb).filter(f => f.endsWith('.sql')).sort().map(f => path.join(hsqldb, f));
    const ddl = files.filter(f => /DDL/i.test(path.basename(f))).map(f => '`' + rel(f) + '`');
    const dml = files.filter(f => /DML/i.test(path.basename(f))).map(f => '`' + rel(f) + '`');
    return { mode: '--sql', files,
             how: `テーブルはインメモリの HSQLDB に作られます（定義: ${ddl.join('・')}` +
                  (dml.length ? `、初期データ: ${dml.join('・')}` : '') + '）。' };
  }
  for (const [schema, data] of [['src/main/resources/schema.sql', 'src/main/resources/data.sql'],
                                ['src/main/resources/db/schema-hsqldb.sql', 'src/main/resources/db/data-hsqldb.sql']]) {
    if (fs.existsSync(path.join(dir, schema))) {
      const files = [schema, data].filter(f => fs.existsSync(path.join(dir, f)));
      return { mode: '--sql', files: files.map(f => path.join(dir, f)),
               how: `テーブルはアプリの起動時に、インメモリの HSQLDB へ ${files.map(f => '`' + f + '`').join(' と ')} から作られます。` };
    }
  }
  // テストの準備で CREATE TABLE しているもの
  const tests = [];
  const walk = d => {
    for (const e of fs.readdirSync(d, { withFileTypes: true })) {
      const p = path.join(d, e.name);
      if (e.isDirectory()) walk(p);
      else if (e.name.endsWith('.java') && /CREATE TABLE/.test(fs.readFileSync(p, 'utf8'))) tests.push(p);
    }
  };
  if (fs.existsSync(path.join(dir, 'src'))) walk(path.join(dir, 'src'));
  if (tests.length) {
    return { mode: '--java', files: tests,
             how: `テーブルはテストの準備で、インメモリの HSQLDB に作られます（${tests.map(f => '`' + rel(f) + '`').join('・')}）。` };
  }
  return null;
}

function main() {
  let written = 0;
  const problems = [];
  for (const [course, spec] of Object.entries(TARGETS)) {
    const meta = yaml.load(fs.readFileSync(path.join(COURSES, course, 'course.yaml'), 'utf8'));
    const exercises = meta.exercises.filter(e => Array.isArray(spec) ? spec.includes(e.id) : !spec.except.includes(e.id));

    // 同じ雛形を複数の演習が使うことがある (dir の共有)。雛形ごとに 1 回だけ作る
    const byDir = new Map();
    for (const e of exercises) {
      const dir = e.dir || e.id;
      if (!byDir.has(dir)) byDir.set(dir, []);
      byDir.get(dir).push(e);
    }

    for (const [dirName, users] of byDir) {
      const dir = path.join(COURSES, course, 'templates', dirName);
      if (MANUAL.has(`${course}/${dirName}`)) {
        if (!fs.existsSync(path.join(dir, 'SCHEMA.md'))) problems.push(`${course}/${dirName}: 手書きの SCHEMA.md が無い`);
        continue;
      }
      const src = sourcesOf(dir);
      if (!src) { problems.push(`${course}/${dirName}: テーブルの定義が見つからない`); continue; }

      const res = spawnSync(JAVA, ['-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8', '-cp', path.join(ROOT, 'hsqldb', HSQLDB),
                                   path.join(__dirname, 'SchemaDoc.java'), src.mode, ...src.files], { encoding: 'utf8' });
      if (res.status !== 0 || !res.stdout.trim()) {
        problems.push(`${course}/${dirName}: 生成に失敗した\n${res.stderr}`);
        continue;
      }
      if (res.stderr.trim()) problems.push(`${course}/${dirName}: ${res.stderr.trim()}`);

      const names = users.map(e => `「${e.names.ja}」`).join('・');
      const doc = [
        '# この演習で使うテーブル',
        '',
        `${names}のコードが読み書きするテーブルです。`,
        src.how,
        '',
        // 講座を作る側への注意は、受講者の画面 (Markdown ビューア) には出さない
        '<!-- この説明は desktop/scripts/build-schema-docs.js が DDL から作っている。',
        '     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->',
        '',
        res.stdout.trimEnd(),
        '',
      ].join('\n');
      fs.writeFileSync(path.join(dir, 'SCHEMA.md'), doc, 'utf8');
      written++;
      console.log(`  ${course}/${dirName}  (${users.map(e => e.id).join(', ')})`);
    }
  }
  for (const p of problems) console.log('NG   ' + p);
  console.log(`\nSCHEMA.md を ${written} 件作りました。`);
  process.exit(problems.length ? 1 : 0);
}

if (require.main === module) main();

module.exports = { TARGETS };
