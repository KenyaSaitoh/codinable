// 「初期化」（演習を配布時の状態に戻す）が期待どおり働くかを確かめる検査用スクリプト
//
// workspace.js は electron を読むので、electron で動かす
// ワークスペースは一時ディレクトリに向けるため、実際の %USERPROFILE%\codinable は触らない
//
//   npx electron desktop/test/check-reset.js

const { app } = require('electron');
const fs      = require('fs');
const os      = require('os');
const path    = require('path');

app.on('window-all-closed', () => {});

app.whenReady().then(() => {
  // config.js は userData 配下の設定を読むので、先に差し替えておく
  const tmp = fs.mkdtempSync(path.join(os.tmpdir(), 'codinable-reset-'));
  app.setPath('userData', path.join(tmp, 'userData'));

  const config    = require('../src/main/config');
  const workspace = require('../src/main/workspace');
  config.setWorkspaceRoot(path.join(tmp, 'ws'));

  const templates = path.resolve(__dirname, '../../courses/webapp-archi-overview/templates');
  const problems  = [];
  const check     = (cond, message) => { if (!cond) problems.push(message); };

  // ── 静的ページの演習を作る ─────────────────────────
  const templateDir = path.join(templates, 'html-form');
  const created = workspace.createProject({ name: 'html-form', templateDir, courseId: 'test' });
  check(created.ok, `プロジェクトが作れない: ${created.error}`);

  const projectDir = created.path;
  const indexHtml  = path.join(projectDir, 'index.html');
  const formCss    = path.join(projectDir, 'form.css');
  const original   = fs.readFileSync(indexHtml, 'utf8');

  // ── 汚す: 書き換える / 消す / 増やす ────────────────
  fs.writeFileSync(indexHtml, '<h1>こわした</h1>\n', 'utf8');
  fs.unlinkSync(formCss);
  fs.writeFileSync(path.join(projectDir, 'memo.txt'), '自分で作ったメモ\n', 'utf8');

  // ── 初期化 ───────────────────────────────────────
  const reset = workspace.resetToTemplate({ name: 'html-form', templateDir });
  check(reset.ok, `初期化が失敗した: ${reset.error}`);
  check(reset.written >= 2, `書き戻した件数が少ない: ${reset.written}`);

  check(fs.readFileSync(indexHtml, 'utf8') === original,
        '書き換えたファイルが元に戻っていない');
  check(fs.existsSync(formCss), '消したファイルが戻っていない');
  check(fs.existsSync(path.join(projectDir, 'memo.txt')),
        '自分で作ったファイルが消えてしまった');

  // ── プロジェクトの情報（.codinable/meta.json）が残ること ──
  const meta = workspace.readProjectMeta(projectDir);
  check(meta && meta.courseId === 'test', '初期化でプロジェクト情報が消えた');

  // ── 雛形が無い演習では、何もせず理由を返すこと ────────
  const noTemplate = workspace.resetToTemplate({ name: 'html-form', templateDir: null });
  check(noTemplate.ok === false && noTemplate.error === 'no-template',
        `雛形が無いときの扱いが違う: ${JSON.stringify(noTemplate)}`);

  // ── SQL の演習でも同じこと（サブディレクトリなし・複数ファイル）──
  const sqlTemplate = path.join(templates, 'sql-crud');
  workspace.createProject({ name: 'sql-crud', templateDir: sqlTemplate, courseId: 'test' });
  const setupSql = path.join(config.getWorkspaceRoot(), 'sql-crud', '01_setup.sql');
  fs.writeFileSync(setupSql, '-- けした\n', 'utf8');
  const r2 = workspace.resetToTemplate({ name: 'sql-crud', templateDir: sqlTemplate });
  check(r2.ok && fs.readFileSync(setupSql, 'utf8') === fs.readFileSync(path.join(sqlTemplate, '01_setup.sql'), 'utf8'),
        'SQL 演習が元に戻っていない');

  fs.rmSync(tmp, { recursive: true, force: true });

  for (const p of problems) console.log('NG   ' + p);
  console.log(problems.length ? `NG ${problems.length} 件` : '初期化は期待どおり');
  app.exit(problems.length ? 1 : 0);
});
