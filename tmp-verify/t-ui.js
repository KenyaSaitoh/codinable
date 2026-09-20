// 「実行対象は選択ファイルに依存しない」「プレビューはサーバーが立つまで非活性」を画面で確かめる。
const { connect } = require('./cdp');

const sleep = ms => new Promise(r => setTimeout(r, ms));

const snapshot = `(() => {
  const sel = document.getElementById('run-target-select');
  return {
    project: document.getElementById('project-select').value,
    targets: [...sel.options].map(o => o.value),
    selected: sel.value,
    activeFile: document.querySelector('.editor-tab.active')?.dataset.path || null,
    runDisabled: document.getElementById('btn-run').disabled,
    previewBtnDisabled: document.getElementById('btn-preview').disabled,
    previewTabDisabled: document.getElementById('run-tab-browser').disabled,
    activePane: document.querySelector('.run-pane.active')?.id || null,
    url: document.getElementById('browser-url').value,
  };
})()`;

// 演習一覧から名前で選んで開く
const openExercise = name => `(async () => {
  const item = [...document.querySelectorAll('.exercise-item')]
    .find(el => el.textContent.includes(${JSON.stringify(name)}));
  if (!item) return 'not found: ' + ${JSON.stringify(name)};
  item.click();
  return 'clicked';
})()`;

const openTreeFile = name => `(async () => {
  const node = [...document.querySelectorAll('.tree-item')]
    .find(el => el.textContent.trim().endsWith(${JSON.stringify(name)}));
  if (!node) return 'not found: ' + ${JSON.stringify(name)};
  node.click();
  return 'clicked';
})()`;

(async () => {
  const cdp = await connect();
  const show = (label, s) => {
    console.log(`\n── ${label}`);
    console.log(`   プロジェクト = ${s.project}   開いているファイル = ${s.activeFile}`);
    console.log(`   実行対象(${s.targets.length}) = ${s.targets.join(' , ')}`);
    console.log(`   選択中 = ${s.selected}   実行ボタン = ${s.runDisabled ? '非活性' : '活性'}`);
    console.log(`   プレビュー: ボタン = ${s.previewBtnDisabled ? '非活性' : '活性'}` +
                ` / タブ = ${s.previewTabDisabled ? '非活性' : '活性'}   url = ${s.url || '(空)'}`);
    console.log(`   表示中ペイン = ${s.activePane}`);
  };

  // ① Spring の演習: gradle タスクが並び、プレビューは非活性のはず
  console.log(await cdp.evaluate(openExercise('計算アプリ')));
  await sleep(6000);
  const spring = await cdp.evaluate(snapshot);
  show('① Spring MVC の演習を開いた直後', spring);

  // ② 実行できないファイル (テンプレート HTML) を開いても実行対象が変わらないこと
  console.log(await cdp.evaluate(openTreeFile('build.gradle')));
  await sleep(1500);
  const afterFile = await cdp.evaluate(snapshot);
  show('② build.gradle を開いたあと', afterFile);
  console.log(`   => 実行対象は ${JSON.stringify(spring.targets) === JSON.stringify(afterFile.targets) ? '変化なし (OK)' : '変わってしまった (NG)'}`);

  // ③ SQL の演習: sql: が 3 本並ぶ
  console.log(await cdp.evaluate(openExercise('SQL')));
  await sleep(6000);
  show('③ SQL の演習', await cdp.evaluate(snapshot));

  // ④ 静的ページの演習 → 実行するとプレビューが活性になる
  console.log(await cdp.evaluate(openExercise('静的')));
  await sleep(6000);
  show('④ 静的ページの演習 (実行前)', await cdp.evaluate(snapshot));

  await cdp.evaluate(`document.getElementById('btn-run').click(); 'run'`);
  await sleep(5000);
  show('⑤ 静的ページを実行した後', await cdp.evaluate(snapshot));

  cdp.close();
})().catch(err => { console.error('FAILED:', err.message); process.exit(1); });
