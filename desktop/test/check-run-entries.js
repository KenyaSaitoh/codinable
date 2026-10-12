// 全講座の演習が、ツールバーの「▶ アプリ実行」とエディタの「▶ 実行」で
// 動かせるかを確かめる
//
// course.yaml の run は次のどれかに当たっていなければならない (renderer.js の runEntries)
//   - ▶ アプリ実行が動かすもの (workspace.js の pickRunEntries の app)
//   - テスト全体 (test)。テストは 🧪 のボタンではなく、テストクラスを開いてエディタの ▶ 実行で流す
//   - app と同じ種類の gradle: / npm: (gradle:build の演習のように ▶ アプリ実行を差し替える)
//   - sql: / file: なら、そのファイルが単独で動かせる (listFileRuns に載る = ツリーに ▶ が付く)
// あわせて、説明文が無くなったセレクトや古いボタン名を案内していないかも見る
//
// workspace.js は electron を読むので、electron で動かす
//
//   npx electron desktop/test/check-run-entries.js

const { app } = require('electron');
const fs   = require('fs');
const os   = require('os');
const path = require('path');
const yaml = require('js-yaml');

app.on('window-all-closed', () => {});

app.whenReady().then(() => {
  app.setPath('userData', fs.mkdtempSync(path.join(os.tmpdir(), 'codinable-run-entries-')));
  const { detectProject, listFileRuns } = require('../src/main/workspace');

  const coursesDir = path.resolve(__dirname, '../../courses');
  const problems = [];
  let count = 0;

  for (const id of fs.readdirSync(coursesDir)) {
    const yamlPath = path.join(coursesDir, id, 'course.yaml');
    if (!fs.existsSync(yamlPath)) continue;
    const course = yaml.load(fs.readFileSync(yamlPath, 'utf8'));

    for (const ex of course.exercises || []) {
      count++;
      const where = `${id}/${ex.id}`;
      const dir = path.join(coursesDir, id, 'templates', ex.dir || ex.id);
      if (!fs.existsSync(dir)) { problems.push(`${where}: 雛形がありません`); continue; }
      const info = detectProject(dir);
      const { run } = info;
      const fileRuns = listFileRuns(dir, info);

      if (!run.app && !run.test && !Object.keys(fileRuns).length) problems.push(`${where}: 動かせるものがありません`);
      if (ex.run) {
        const [kind, arg] = ex.run.split(/:(.*)/s);
        const sameKind = /^(gradle|npm)$/.test(kind) && run.app?.split(':')[0] === kind;
        const ok = ex.run === run.app || ex.run === run.test || sameKind ||
                   (/^(sql|file)$/.test(kind) && fileRuns[arg] === ex.run);
        if (!ok) problems.push(`${where}: run ${ex.run} はどのボタンにも当たりません (app=${run.app} test=${run.test})`);
      }

      // 説明文はボタンの名前で案内する (「Gradle: test を選んで」「実行対象を切り替えて」はもう画面に無い)
      for (const [lang, text] of Object.entries(ex.descriptions || {})) {
        if (/Gradle: ?\w|npm: ?\w|実行対象|run target|🧪|テスト実行|Run tests|ターミナルタブ|terminal tab|の左のファイル|to the left of/i.test(text)) {
          problems.push(`${where} (${lang}): 説明文が古い実行の操作を案内しています`);
        }
      }
    }
  }

  if (problems.length) {
    console.log(problems.map(p => `NG ${p}`).join('\n'));
    console.log(`\n${problems.length} 件の問題 (${count} 演習)`);
    app.exit(1);
  } else {
    console.log(`すべて期待どおり (${count} 演習)`);
    app.exit(0);
  }
});
