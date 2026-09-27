// 画面側の動作確認。アプリを起動して、実際の UI を操作して確かめる
//
// 確かめること
//   1. 演習一覧が course.yaml の件数どおりに、チャプターごとに並ぶ
//   2. 静的ページの演習を選ぶと、プロジェクトが作られて実行対象が static: になる
//   3. 実行するとプレビューが活性になり、止めると非活性に戻る
//   4. ファイルを汚してから「初期化」で元に戻る
//   5. SQL の演習を選んで実行すると、DB が自動で起動して SQL タブに結果が出る
//      (DB の起動・停止はタブの行の右端だけにあり、SQL タブに実行ボタンは無い)
//
// ワークスペースと設定は一時ディレクトリに向けるので、実際の環境は汚さない
//
//   node desktop/test/check-app.js

const { spawn }  = require('child_process');
const http       = require('http');
const fs         = require('fs');
const os         = require('os');
const path       = require('path');
const WebSocket  = require('ws');

// 固定のポートだと、前の検査の残りが掴んでいて繋がらないことがある。毎回空きを取る
let PORT = 0;
const tmp    = fs.mkdtempSync(path.join(os.tmpdir(), 'codinable-app-'));
const userData = path.join(tmp, 'ud');
const wsRoot   = path.join(tmp, 'ws');

fs.mkdirSync(userData, { recursive: true });
fs.mkdirSync(wsRoot,   { recursive: true });

// SCHEMA.md を雛形に足す前に作った SQL の演習を再現する (SCHEMA.md が無く、SQL は自分で書き換え済み)
const OLD_SQL = path.join(wsRoot, 'sql-join');
const EDITED_SQL = '-- 自分で書き換えた内容\nSELECT 1 FROM (VALUES (0));\n';
fs.cpSync(path.resolve(__dirname, '../../courses/webapp-archi-overview/templates/sql-join'), OLD_SQL, { recursive: true });
fs.rmSync(path.join(OLD_SQL, 'SCHEMA.md'));
fs.writeFileSync(path.join(OLD_SQL, '01_setup.sql'), EDITED_SQL, 'utf8');
fs.mkdirSync(path.join(OLD_SQL, '.codinable'), { recursive: true });
fs.writeFileSync(path.join(OLD_SQL, '.codinable', 'project.json'),
                 JSON.stringify({ courseId: 'webapp-archi-overview', template: 'sql-join' }), 'utf8');
// 起動前に設定を置く。--user-data-dir を渡すので、ここが設定の置き場になる
fs.writeFileSync(path.join(userData, 'codinable-config.json'),
                 JSON.stringify({ workspaceRoot: wsRoot, uiLang: 'ja' }, null, 2), 'utf8');

const problems = [];
const check = (cond, message) => { if (!cond) problems.push(message); return !!cond; };
const sleep = ms => new Promise(r => setTimeout(r, ms));

main().then(async code => {
  for (const p of problems) console.log('NG   ' + p);
  console.log(problems.length ? `NG ${problems.length} 件` : 'すべて期待どおり');
  process.exit(problems.length ? 1 : code);
}).catch(err => {
  console.log('NG   検査そのものが失敗: ' + err.message);
  process.exit(1);
});

async function main() {
  PORT = await freePort();
  const electron = process.env.PACKAGED_APP || path.resolve(__dirname, '../node_modules/electron/dist/electron.exe');
  const child = spawn(electron, [
    ...(!process.env.PACKAGED_APP ? [path.resolve(__dirname, '..')] : []),
    `--remote-debugging-port=${PORT}`,
    `--user-data-dir=${userData}`,
  ], { stdio: ['ignore', 'pipe', 'pipe'] });

  const log = [];
  child.stdout.on('data', d => log.push(String(d)));
  child.stderr.on('data', d => log.push(String(d)));

  let cdp;
  try {
    cdp = await connect();
    await run(cdp);
    if (process.env.CHECK_MESSAGING) {
      const exited = new Promise(resolve => child.once('exit', () => resolve(true)));
      await cdp.eval('setTimeout(() => window.close(), 100); true');
      let timeout;
      const closed = await Promise.race([exited, new Promise(resolve => { timeout = setTimeout(() => resolve(false), 40000); })]);
      clearTimeout(timeout);
      check(closed, 'App shutdown did not finish');
      if (closed) {
        cdp.close(); cdp = null;
        const { portOpen } = require('../src/main/messaging');
        for (const port of [9092, 9093, 5672, 15672, 25672, 43690]) check(!await portOpen(port), 'App shutdown left port ' + port + ' open');
      }
    }
  } catch (err) {
    problems.push(err.message);
  } finally {
    if (cdp && process.env.CHECK_MESSAGING) {
      for (const id of ['kafka', 'rabbitmq']) {
        try { await cdp.eval(`window.api.messagingStop('${id}')`); } catch {}
      }
    }
    if (cdp) cdp.close();
    child.kill();
    // Electron はウィンドウを持つので、念のため子プロセスも落とす
    await sleep(500);
    try { fs.rmSync(tmp, { recursive: true, force: true }); } catch {}
  }
  if (problems.length && log.length) {
    console.log('--- アプリの出力 ---');
    console.log(log.join('').split('\n').filter(l => /error|Error|失敗/.test(l)).slice(0, 20).join('\n'));
  }
  return 0;
}

// ── 操作 ────────────────────────────────────────────────

async function run(cdp) {
  await cdp.eval('1');            // 準備できるまで待つのは connect 側で済んでいる
  await waitFor(cdp, `document.querySelectorAll('.exercise-item').length > 0`, 20000,
                '演習一覧が出ない');

  // 1. 件数とチャプター見出し
  const yaml     = require('js-yaml');
  const course   = yaml.load(fs.readFileSync(
    path.resolve(__dirname, '../../courses/webapp-archi-overview/course.yaml'), 'utf8'));
  const expected = course.exercises.length;
  const listed   = await cdp.eval(`document.querySelectorAll('.exercise-item').length`);
  check(listed === expected, `演習の数が合わない: 画面 ${listed} 件 / course.yaml ${expected} 件`);

  const chapters = new Set(course.exercises.map(e => e.chapter)).size;
  const headers  = await cdp.eval(`document.querySelectorAll('#exercise-list .q-chapter, #exercise-list .exercise-chapter').length`);
  check(headers === chapters, `チャプター見出しの数が合わない: 画面 ${headers} / 想定 ${chapters}`);

  check(await cdp.eval(`!document.getElementById('exercise-count')`), '演習の件数バッジが残っている');
  const courseName = await cdp.eval(`document.getElementById('current-course-name').textContent`);
  check(courseName === course.names.ja, `タイトル下が講座名になっていない: ${courseName}`);

  // 2. 静的ページの演習を選ぶ
  await clickExercise(cdp, 'html-form');
  await waitFor(cdp, `document.getElementById('run-target-select').value.startsWith('static:')`,
                20000, '静的ページの実行対象が static: にならない');
  const opened = await cdp.eval(`[...document.querySelectorAll('.editor-tab')].map(t => t.title || t.textContent).join(',')`);
  check(/index\.html/.test(opened), `openFiles が開かれていない: ${opened}`);
  check(await cdp.eval(`!document.getElementById('btn-reset-exercise').classList.contains('hidden')`),
        '演習を選んでも「初期化」が出ない');
  check(await cdp.eval(`document.querySelectorAll('.exercise-item .q-status').length`) === 0,
        '演習一覧に意味の分からない状態記号が残っている');
  check(await cdp.eval(`['btn-new-file','btn-new-dir','btn-refresh-tree'].some(id => document.getElementById(id))`) === false,
        'プロジェクト欄に +F / +D / 再読み込みが残っている');

  // 標準入力の欄は持たない (演習は入力を打ち込む前提のものではない)
  check(await cdp.eval(`!document.getElementById('run-stdin-row') && !document.getElementById('run-stdin-input')`),
        '標準入力の欄が残っている');

  // 3. 実行 → プレビューが活性になり、中身が実際に読み込まれる
  check(await cdp.eval(`document.getElementById('run-tab-browser').disabled`) === true,
        '実行する前からプレビューが活性になっている');
  await cdp.eval(`document.getElementById('btn-run').click()`);
  await waitFor(cdp, `document.getElementById('run-tab-browser').disabled === false`, 30000,
                '実行してもプレビューが活性にならない');
  const url = await cdp.eval(`document.getElementById('browser-url').value`);
  check(/^http:\/\/localhost:\d+/.test(url), `プレビューの URL が入らない: ${url}`);
  // <webview> が about:blank のまま (読み込みが打ち切られる) になっていないこと
  await waitFor(cdp, `(() => { try { return document.getElementById('mini-browser').getURL() === ${JSON.stringify(url)}; } catch { return false; } })()`,
                10000, 'プレビューにページが読み込まれない');

  // 4. 汚して、自分のファイルも足してから初期化
  const indexHtml = path.join(wsRoot, 'html-form', 'index.html');
  const before = fs.readFileSync(indexHtml, 'utf8');
  fs.writeFileSync(indexHtml, '<h1>こわした</h1>\n', 'utf8');
  const ownFile = path.join(wsRoot, 'html-form', 'memo.txt');
  fs.writeFileSync(ownFile, '自分で足したファイル\n', 'utf8');
  await cdp.eval(`document.getElementById('btn-reset-exercise').click()`);
  await waitFor(cdp, `!document.getElementById('simple-dialog-overlay').classList.contains('hidden')`,
                5000, '初期化の確認ダイアログが出ない');
  const confirmText = await cdp.eval(`document.getElementById('simple-dialog-message').textContent`);
  check(/^選択された演習（.+）を配布時の状態に戻します/.test(confirmText), `初期化の確認文が想定と違う: ${confirmText}`);
  await cdp.eval(`document.getElementById('simple-dialog-ok').click()`);
  await waitFor(cdp, `!document.getElementById('simple-dialog-overlay').classList.contains('hidden')`,
                10000, '初期化の結果ダイアログが出ない');
  const done = await cdp.eval(`document.getElementById('simple-dialog-message').textContent`);
  check(/初期化/.test(done), `初期化の知らせが出ない: ${done}`);
  await cdp.eval(`document.getElementById('simple-dialog-ok').click()`);
  check(fs.readFileSync(indexHtml, 'utf8') === before, '初期化してもファイルが戻っていない');
  check(!fs.existsSync(ownFile), '初期化しても自分で足したファイルが残っている');
  // エディタの中身も読み直されていること
  const inEditor = await cdp.eval(`(window.cmGetValue ? window.cmGetValue() : '')`);
  if (typeof inEditor === 'string' && inEditor.length) {
    check(!inEditor.includes('こわした'), 'エディタに壊した内容が残っている');
  }

  // 5. SQL の演習
  await clickExercise(cdp, 'sql-crud');
  await waitFor(cdp, `document.getElementById('run-target-select').value.startsWith('sql:')`,
                20000, 'SQL の実行対象が sql: にならない');
  check(await cdp.eval(`!document.querySelector('#tab-sql button')`), 'SQL タブにボタンが残っている');
  await waitFor(cdp, `!!document.querySelector('#service-controls [data-service="hsqldb"]')`, 5000,
                'タブの行の右端に DB の起動・停止が出ない');
  check(await cdp.eval(`document.querySelector('#service-controls [data-service="hsqldb"] [data-action="stop"]').disabled`),
        'DB が止まっているのに「DB停止」が押せる');
  await cdp.eval(`document.getElementById('btn-run').click()`);
  await waitFor(cdp, `document.querySelectorAll('#sql-result-wrap table tr').length > 1`, 60000,
                'SQL を実行しても結果の表が出ない');
  const cols = await cdp.eval(`[...document.querySelectorAll('#sql-result-wrap table th')].map(th => th.textContent).join(',')`);
  check(/EMPLOYEE_NAME/i.test(cols), `SQL の結果の列が想定と違う: ${cols}`);
  const activePane = await cdp.eval(`document.querySelector('.run-pane.active')?.id`);
  check(activePane === 'tab-sql', `SQL 実行後に SQL タブが出ていない: ${activePane}`);
  check(await cdp.eval(`document.querySelector('#service-controls [data-service="hsqldb"] .service-dot').dataset.state`) === 'running',
        '実行で DB が起動したことがタブの行に出ない');
  check(await cdp.eval(`!!document.querySelector('#sql-result-wrap .sql-note')`),
        'reset.sql で作り直したことが SQL タブに出ない');
  check(await cdp.eval(`![...document.getElementById('run-target-select').options].some(o => o.value === 'sql:reset.sql')`),
        'reset.sql が実行対象に並んでいる');

  // 5a. 更新系の SQL を 2 回続けて流しても、毎回初期データから始まるので主キー違反にならない
  const runSqlTarget = async target => {
    await cdp.eval(`(() => { const s = document.getElementById('run-target-select'); s.value = ${JSON.stringify(target)};
                     s.dispatchEvent(new Event('change')); document.getElementById('sql-result-wrap').innerHTML = '';
                     document.getElementById('btn-run').click(); })()`);
    await waitFor(cdp, `!!document.querySelector('#sql-result-wrap table, #sql-result-wrap .sql-error, #sql-result-wrap .sql-message')`,
                  30000, `${target} の結果が出ない`);
    return cdp.eval(`document.getElementById('sql-result-wrap').textContent`);
  };
  for (let i = 1; i <= 2; i++) {
    const text = await runSqlTarget('sql:02_insert.sql');
    check(!/violation|エラー/i.test(text), `02_insert.sql の ${i} 回目がエラーになった: ${text.slice(0, 200)}`);
    check(/10006/.test(text), `02_insert.sql の ${i} 回目に登録した行が出ない`);
  }

  // 5a'. 途中の文で失敗したら、何文目のどの文かを出す
  await cdp.eval(`window.api.wsWriteFile(project, 'zz_error.sql', 'SELECT 1 FROM (VALUES (0));\\nINSERT INTO EMPLOYEE VALUES (10001, \\'Dup\\', NULL, 1);\\nSELECT 2 FROM (VALUES (0));\\n')`);
  await cdp.eval(`refreshProjectInfo()`);
  await waitFor(cdp, `[...document.getElementById('run-target-select').options].some(o => o.value === 'sql:zz_error.sql')`,
                10000, '足した SQL が実行対象に出ない');
  const errText = await runSqlTarget('sql:zz_error.sql');
  check(/2 文目/.test(errText) && /INSERT INTO EMPLOYEE VALUES \(10001/.test(errText),
        `失敗した文の位置と中身が出ない: ${errText.slice(0, 300)}`);
  if (process.env.SHOTS) {
    fs.writeFileSync(path.join(process.env.SHOTS, 'app-sql-error.png'), Buffer.from(await cdp.screenshot(), 'base64'));
  }

  await cdp.eval(`document.querySelector('#service-controls [data-service="hsqldb"] [data-action="stop"]').click()`);
  await waitFor(cdp, `sqlState === 'stopped' && !document.querySelector('#service-controls [data-service="hsqldb"] [data-action="start"]').disabled`,
                10000, '「DB停止」で DB が止まらない');
  if (process.env.SHOTS) {
    fs.mkdirSync(process.env.SHOTS, { recursive: true });
    fs.writeFileSync(path.join(process.env.SHOTS, 'app-sql-controls.png'), Buffer.from(await cdp.screenshot(), 'base64'));
  }

  // 5b. 以前に作った SQL の演習でも、テーブル構成の SCHEMA.md が補われてビューアで開く
  await clickExercise(cdp, 'sql-join');
  await waitFor(cdp, `projectInfo?.template === 'sql-join' && activeFile === 'SCHEMA.md'`, 20000,
                '以前に作った SQL の演習で SCHEMA.md が開かない');
  check(await cdp.eval(`!document.getElementById('md-preview').classList.contains('hidden')`),
        'SCHEMA.md がビューアで表示されていない');
  check(fs.existsSync(path.join(OLD_SQL, 'SCHEMA.md')), '以前に作ったプロジェクトに SCHEMA.md が補われていない');
  check(fs.readFileSync(path.join(OLD_SQL, '01_setup.sql'), 'utf8') === EDITED_SQL,
        'SCHEMA.md を補うときに、自分で書き換えたファイルが上書きされた');

  // 5c. シェルの演習を実行しても、標準入力の欄は出ない
  await clickExercise(cdp, 'http-curl');
  await waitFor(cdp, `projectInfo?.template === 'http-curl'`, 20000, 'HTTP の演習が開かない');
  await cdp.eval(`document.getElementById('btn-run').click()`);
  await sleep(1500);
  check(await cdp.eval(`!document.querySelector('[id^="run-stdin"]')`), 'シェルの実行中に標準入力の欄が出た');
  await cdp.eval(`document.getElementById('btn-run-stop').disabled || document.getElementById('btn-run-stop').click()`);

  // 5d. 実行結果は画面を超えると末尾を追いかける。上へ戻して読んでいるあいだは動かさない
  await cdp.eval(`showRunPane('tab-result'); clearRunOutput(); true`);
  await cdp.eval(`(() => { for (let i = 0; i < 400; i++) appendRunOutput('line ' + i + '\\n'); return true; })()`);
  await sleep(300);
  const atEnd = `(() => { const p = document.getElementById('tab-result'); return p.scrollHeight > p.clientHeight && p.scrollHeight - p.scrollTop - p.clientHeight < 5; })()`;
  check(await cdp.eval(atEnd), '実行結果が画面を超えても末尾までスクロールされない');
  await cdp.eval(`document.getElementById('tab-result').scrollTop = 0; true`);
  await sleep(300);
  await cdp.eval(`appendRunOutput('more\\n'); true`);
  await sleep(300);
  check(await cdp.eval(`document.getElementById('tab-result').scrollTop === 0`), '上へ戻して読んでいるのに末尾へ動かされた');
  await cdp.eval(`(() => { const p = document.getElementById('tab-result'); p.scrollTop = p.scrollHeight; return true; })()`);
  await sleep(300);
  await cdp.eval(`(() => { for (let i = 0; i < 50; i++) appendRunOutput('again ' + i + '\\n'); return true; })()`);
  await sleep(300);
  check(await cdp.eval(atEnd), '末尾へ戻したあと、また追いかけない');

  // 5e. スクロールバーの幅はすべての UI で共通 (演習一覧・実行結果・チャット・ターミナル)
  const barWidth = id => cdp.eval(`(() => { const el = document.getElementById(${JSON.stringify(id)});
    const prev = el.style.overflowY; el.style.overflowY = 'scroll';
    const w = el.offsetWidth - el.clientWidth - parseFloat(getComputedStyle(el).borderLeftWidth) - parseFloat(getComputedStyle(el).borderRightWidth);
    el.style.overflowY = prev; return w; })()`);
  for (const id of ['exercise-list', 'tab-result', 'chat-history']) {
    const exists = await cdp.eval(`!!document.getElementById(${JSON.stringify(id)})`);
    if (!check(exists, `スクロールバーの検査対象 #${id} が無い`)) continue;
    const w = await barWidth(id);
    check(Math.abs(w - 12) <= 1, `#${id} のスクロールバーの幅が 12px でない: ${w}`);
  }
  await cdp.eval(`document.getElementById('run-tab-terminal').click(); true`);
  await waitFor(cdp, `!!document.querySelector('#xterm-wrap .xterm-scrollable-element > .scrollbar.vertical')`, 15000,
                'ターミナルのスクロールバーが出ない');
  const termBar = await cdp.eval(`document.querySelector('#xterm-wrap .xterm-scrollable-element > .scrollbar.vertical').offsetWidth`);
  check(Math.abs(termBar - 12) <= 1, `ターミナルのスクロールバーの幅が 12px でない: ${termBar}`);
  await cdp.eval(`showRunPane('tab-result'); true`);

  // 5f. コース名に続けて、選んでいる演習の名前を文字だけで出す (枠で囲まない)
  await clickExercise(cdp, 'sql-crud');
  await waitFor(cdp, `projectInfo?.template === 'sql-crud'`, 20000, 'sql-crud が開かない');
  await waitFor(cdp, `!document.getElementById('current-exercise-badge').classList.contains('hidden')`, 5000, '選んでいる演習の名前が出ない');
  const badgeText = await cdp.eval(`document.getElementById('current-exercise-badge').textContent`);
  const exName = await cdp.eval(`currentCourse().exercises.find(e => e.id === 'sql-crud').name`);
  check(badgeText === exName, `演習名だけになっていない: ${badgeText}`);
  check(await cdp.eval(`document.getElementById('current-exercise-badge').tagName`) === 'SPAN', '演習名が押せる要素になっている');
  check(await cdp.eval(`getComputedStyle(document.getElementById('current-exercise-badge')).borderTopStyle`) === 'none', '演習名が枠で囲まれている');
  if (process.env.SHOTS) {
    fs.writeFileSync(path.join(process.env.SHOTS, 'app-exercise-badge.png'), Buffer.from(await cdp.screenshot(), 'base64'));
  }

  // 5g. チャットの吹き出しは幅の 94% まで広がる (右の余白を以前の半分に)
  check(await cdp.eval(`(() => { const h = document.getElementById('chat-history'); const b = document.createElement('div');
    b.className = 'chat-msg assistant'; b.innerHTML = '<div class="chat-bubble">' + 'あ'.repeat(400) + '</div>'; h.appendChild(b);
    const inner = h.clientWidth - parseFloat(getComputedStyle(h).paddingLeft) - parseFloat(getComputedStyle(h).paddingRight);
    const ratio = b.firstChild.getBoundingClientRect().width / inner; b.remove(); return ratio > 0.93 && ratio < 0.95; })()`),
        'チャットの吹き出しが幅の 94% まで広がらない');

  // 5h. 設定ダイアログは見出しの帯でつかんで動かせる。開き直すと中央に戻る
  await cdp.eval(`document.getElementById('btn-settings').click(); true`);
  await waitFor(cdp, `!document.getElementById('settings-overlay').classList.contains('hidden')`, 5000, '設定が開かない');
  const dlgBefore = await cdp.eval(`(() => { const r = document.getElementById('settings-panel').getBoundingClientRect(); return [r.left, r.top]; })()`);
  await cdp.eval(`(() => { const h = document.querySelector('#settings-panel .settings-header'); const r = h.getBoundingClientRect();
    const x = r.left + 60, y = r.top + 10, o = { bubbles: true, pointerId: 1, button: 0, isPrimary: true };
    h.dispatchEvent(new PointerEvent('pointerdown', { ...o, clientX: x, clientY: y }));
    h.dispatchEvent(new PointerEvent('pointermove', { ...o, clientX: x - 150, clientY: y + 40 }));
    h.dispatchEvent(new PointerEvent('pointerup', { ...o, clientX: x - 150, clientY: y + 40 }));
    return true; })()`);
  const dlgAfter = await cdp.eval(`(() => { const r = document.getElementById('settings-panel').getBoundingClientRect(); return [r.left, r.top]; })()`);
  check(Math.round(dlgAfter[0] - dlgBefore[0]) === -150 && Math.round(dlgAfter[1] - dlgBefore[1]) === 40,
        `設定ダイアログをドラッグしても動かない: ${dlgBefore} → ${dlgAfter}`);
  check(await cdp.eval(`!document.getElementById('settings-overlay').classList.contains('hidden')`), 'ドラッグで設定が閉じた');
  await cdp.eval(`document.getElementById('settings-close').click(); true`);
  await cdp.eval(`document.getElementById('btn-settings').click(); true`);
  await waitFor(cdp, `!document.getElementById('settings-overlay').classList.contains('hidden')`, 5000, '設定が開き直せない');
  const reopened = await cdp.eval(`(() => { const r = document.getElementById('settings-panel').getBoundingClientRect(); return [r.left, r.top]; })()`);
  check(Math.round(reopened[0]) === Math.round(dlgBefore[0]) && Math.round(reopened[1]) === Math.round(dlgBefore[1]),
        '開き直しても中央に戻らない');
  await cdp.eval(`document.getElementById('settings-close').click(); true`);

  // 6. チャットの Ask / Agent ラジオボタン
  check(await cdp.eval(`document.querySelectorAll('input[type="radio"][name="chat-mode"]').length`) === 2, 'Ask/Agent のラジオボタンが無い');
  check(await cdp.eval(`!document.getElementById('btn-chat-edit')`),
        '「書き換え」ボタンが残っている');
  check(await cdp.eval(`document.getElementById('btn-mode-ask').checked`),
        '既定が Ask になっていない');
  check(await cdp.eval(`document.querySelector('label:has(#btn-mode-ask)').textContent.trim()`) === 'Ask', 'Ask のラベルに補足が残っている');
  await cdp.eval(`document.getElementById('btn-mode-agent').click()`);
  await waitFor(cdp, `document.getElementById('btn-mode-agent').checked`,
                5000, 'Agent に切り替わらない');
  check(await cdp.eval(`!document.getElementById('btn-mode-ask').checked`), 'Ask と Agent が同時に選択されている');
  check(await cdp.eval(`document.querySelector('label:has(#btn-mode-agent)').textContent.trim()`) === 'Agent', 'Agent のラベルに補足が残っている');
  const placeholder = await cdp.eval(`document.getElementById('chat-input').placeholder`);
  check(/演習/.test(placeholder), `Agent の入力案内が変わらない: ${placeholder}`);
  await cdp.eval(`document.getElementById('btn-mode-ask').click()`);
  check(await cdp.eval(`document.getElementById('btn-mode-ask').checked`),
        'Ask に戻せない');

  // 7. 任意の重い検査: 実ランタイムで React / Spring Boot の待受とプレビューを確認する
  // 初回は npm / Gradle の依存取得があるため、通常の画面検査では飛ばす
  if (process.env.CHECK_WEB_SERVERS) {
    await checkWebServerExercise(cdp, 'react-spa', 'npm:dev', /localhost:5173/, 'React');
    await checkWebServerExercise(cdp, 'spring-mvc-calc', 'gradle:bootRun', /localhost:8080/, 'Spring Boot');
  }

  if (process.env.CHECK_JAVA_TESTS) {
    await clickExercise(cdp, 'spring-rest-users');
    await waitFor(cdp, `projectInfo?.template === 'spring-rest-users' && !running`, 20000, 'Spring REST exercise did not open');
    await cdp.eval(`(() => { const s = document.getElementById('run-target-select'); s.value = 'gradle:test'; s.dispatchEvent(new Event('change')); document.getElementById('btn-run').click(); })()`);
    await waitFor(cdp, `/BUILD (SUCCESSFUL|FAILED)/.test(document.getElementById('output-result').textContent)`, 240000, 'JUnit did not finish');
    check(await cdp.eval(`document.getElementById('output-result').textContent.includes('BUILD SUCCESSFUL')`), 'JUnit failed');
    await waitFor(cdp, `document.querySelectorAll('.tr-case').length > 0`, 10000, 'JUnit results were not displayed');
    console.log('Spring Boot / JUnit results checked');
  }

  if (process.env.CHECK_TERMINAL) {
    await cdp.eval(`globalThis.runtimeProbe = ''; window.api.onTermOutput(text => { globalThis.runtimeProbe += text; }); document.getElementById('run-tab-terminal').click();`);
    await sleep(1000);
    await cdp.eval(`window.api.termInput('java -version\\rnode --version\\rpython --version\\r')`);
    await waitFor(cdp, `/25\\./.test(globalThis.runtimeProbe) && /v24\\./.test(globalThis.runtimeProbe) && /Python 3\\.13/.test(globalThis.runtimeProbe)`, 15000, 'Terminal bundled runtimes');
    console.log('Terminal Java / Node / Python checked');
  }

  if (process.env.CHECK_MESSAGING) await checkMessaging(cdp);

  if (process.env.SHOTS) {
    fs.mkdirSync(process.env.SHOTS, { recursive: true });
    const png = await cdp.screenshot();
    fs.writeFileSync(path.join(process.env.SHOTS, 'app-sql.png'), Buffer.from(png, 'base64'));
  }
}

async function checkMessaging(cdp) {
  await cdp.eval(`document.getElementById('run-tab-messaging').click()`);
  await waitFor(cdp, `document.querySelectorAll('.messaging-service').length === 2`, 10000, 'Messaging controls missing');
  check(await cdp.eval(`!document.querySelector('.messaging-service [data-action="start"], .messaging-service [data-action="stop"]')`),
    'メッセージングタブに起動・停止ボタンが残っている');
  check(await cdp.eval(`messagingStates.every(s => s.available)`), 'Bundled brokers not available');
  // 講座の演習 (Spring の producer / consumer) は常駐するため、ここでは同梱ブローカーの起動だけを見る
  // 「実行」の自動起動と同じ口 (main の ensure) ではなく、IPC から直接起こす
  for (const id of ['kafka', 'rabbitmq']) {
    await cdp.eval(`window.api.messagingStart('${id}')`);
    await waitFor(cdp, `document.querySelector('.messaging-service[data-service="${id}"] .messaging-state').dataset.state === 'running'`, 120000, id + ' start');
    await waitFor(cdp, `!!document.querySelector('#service-controls [data-service="${id}"] [data-action="stop"]:not(:disabled)')`,
                  5000, id + ' がタブの行の右端に出ない');
  }
  if (process.env.SHOTS) {
    fs.mkdirSync(process.env.SHOTS, { recursive: true });
    fs.writeFileSync(path.join(process.env.SHOTS, 'app-messaging.png'), Buffer.from(await cdp.screenshot(), 'base64'));
  }
  // タブの行の右端の停止ボタン。演習が使わないサーバーは、止まればそこから消える
  await cdp.eval(`document.querySelector('#service-controls [data-service="rabbitmq"] [data-action="stop"]').click()`);
  await waitFor(cdp, `document.querySelector('.messaging-service[data-service="rabbitmq"] .messaging-state').dataset.state === 'stopped'`, 30000, 'RabbitMQ stop button');
  await waitFor(cdp, `!document.querySelector('#service-controls [data-service="rabbitmq"]')`, 5000, '止めた RabbitMQ がタブの行に残っている');
  console.log('Messaging UI and both brokers checked');
}

async function checkWebServerExercise(cdp, exerciseId, target, urlPattern, label) {
  await clickExercise(cdp, exerciseId);
  await waitFor(cdp, `document.getElementById('run-target-select').value === ${JSON.stringify(target)}`,
                20000, `${label} の実行対象が ${target} にならない`);
  await cdp.eval(`document.getElementById('btn-run').click()`);
  await waitFor(cdp, `document.getElementById('btn-preview').disabled === false`, 360000,
                `${label} を実行してもプレビューが有効にならない`);
  const url = await cdp.eval(`document.getElementById('browser-url').value`);
  check(urlPattern.test(url), `${label} のプレビュー URL が想定と違う: ${url}`);
  await cdp.eval(`document.getElementById('btn-run-stop').click()`);
  await waitFor(cdp, `document.getElementById('btn-run-stop').disabled === true`, 30000,
                `${label} を停止できない`);
}

async function clickExercise(cdp, id) {
  const ok = await cdp.eval(`(() => {
    const item = [...document.querySelectorAll('.exercise-item')]
      .find(el => el.dataset.exerciseId === ${JSON.stringify(id)}
                || (el.dataset.id === ${JSON.stringify(id)}));
    if (!item) return false;
    item.click();
    return true;
  })()`);
  if (!check(ok === true, `演習 ${id} が一覧に無い（data 属性で見つからない）`)) return;
  // 「作る？」の確認が出たら進める
  await sleep(400);
  if (await cdp.eval(`!document.getElementById('simple-dialog-overlay').classList.contains('hidden')`)) {
    await cdp.eval(`document.getElementById('simple-dialog-ok').click()`);
  }
}

// ── CDP ─────────────────────────────────────────────────

async function connect(timeoutMs = 40000) {
  const until = Date.now() + timeoutMs;
  let lastError = 'まだ何も返っていない';
  while (Date.now() < until) {
    await sleep(500);
    let targets;
    try {
      targets = JSON.parse(await get(`http://127.0.0.1:${PORT}/json/list`));
    } catch (e) { lastError = `一覧が取れない: ${e.message}`; continue; }
    const page = targets.find(t => t.type === 'page' && /index\.html/.test(t.url || ''));
    if (!page) {
      lastError = `画面の target が無い: ${targets.map(t => t.type + ' ' + t.url).join(' / ') || '(空)'}`;
      continue;
    }
    try { return await open(page.webSocketDebuggerUrl); }
    catch (e) { lastError = `WebSocket に繋がらない: ${e.message}`; }
  }
  throw new Error(`アプリの画面に接続できなかった（${lastError}）`);
}

function open(url) {
  return new Promise((resolve, reject) => {
    const ws = new WebSocket(url);
    const pending = new Map();
    let id = 0;
    const send = (method, params, label) => new Promise((res, rej) => {
      const mid = ++id;
      pending.set(mid, { res, rej });
      ws.send(JSON.stringify({ id: mid, method, params }));
      setTimeout(() => {
        if (pending.delete(mid)) rej(new Error('返ってこない: ' + label));
      }, 20000);
    });
    ws.on('open', () => resolve({
      eval: expression => send('Runtime.evaluate',
        { expression, awaitPromise: true, returnByValue: true },
        expression.slice(0, 60)),
      screenshot: async () => (await send('Page.captureScreenshot', { format: 'png', fromSurface: false },
                                          'screenshot')).data,
      close: () => ws.close(),
    }));
    ws.on('error', reject);
    ws.on('message', data => {
      const msg = JSON.parse(data);
      const waiter = pending.get(msg.id);
      if (!waiter) return;
      pending.delete(msg.id);
      if (msg.error) return waiter.rej(new Error(msg.error.message));
      if (msg.result?.exceptionDetails) {
        return waiter.rej(new Error(msg.result.exceptionDetails.text + ' ' +
                                    (msg.result.result?.description || '')));
      }
      // Runtime.evaluate は result.result.value、それ以外はそのまま返す
      waiter.res('result' in (msg.result || {}) && msg.result.result?.type
        ? msg.result.result.value
        : msg.result);
    });
  });
}

async function waitFor(cdp, condition, timeoutMs, message) {
  const until = Date.now() + timeoutMs;
  while (Date.now() < until) {
    try { if (await cdp.eval(`!!(${condition})`)) return true; } catch {}
    await sleep(300);
  }
  problems.push(`${message}（${timeoutMs / 1000} 秒待った）`);
  return false;
}

function freePort() {
  return new Promise((resolve, reject) => {
    const server = require('net').createServer();
    server.once('error', reject);
    server.listen(0, '127.0.0.1', () => {
      const { port } = server.address();
      server.close(() => resolve(port));
    });
  });
}

function get(url) {
  return new Promise((resolve, reject) => {
    const req = http.get(url, res => {
      let body = '';
      res.on('data', d => (body += d));
      res.on('end', () => resolve(body));
    });
    req.setTimeout(3000, () => { req.destroy(new Error('timeout')); });
    req.on('error', reject);
  });
}
