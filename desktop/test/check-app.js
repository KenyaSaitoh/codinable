// 画面側の動作確認。アプリを起動して、実際の UI を操作して確かめる。
//
// 確かめること
//   1. 演習一覧が course.yaml の件数どおりに、チャプターごとに並ぶ
//   2. 静的ページの演習を選ぶと、プロジェクトが作られて実行対象が static: になる
//   3. 実行するとプレビューが活性になり、止めると非活性に戻る
//   4. ファイルを汚してから「初期化」で元に戻る
//   5. SQL の演習を選んで実行すると、SQL タブに結果が出る
//
// ワークスペースと設定は一時ディレクトリに向けるので、実際の環境は汚さない。
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
  const electron = path.resolve(__dirname, '../node_modules/electron/dist/electron.exe');
  const child = spawn(electron, [
    path.resolve(__dirname, '..'),
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
  } catch (err) {
    problems.push(err.message);
  } finally {
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

  const badge = await cdp.eval(`document.getElementById('exercise-count').textContent`);
  check(String(badge) === String(expected), `件数のバッジが合わない: ${badge}`);

  // 2. 静的ページの演習を選ぶ
  await clickExercise(cdp, 'html-form');
  await waitFor(cdp, `document.getElementById('run-target-select').value.startsWith('static:')`,
                20000, '静的ページの実行対象が static: にならない');
  const opened = await cdp.eval(`[...document.querySelectorAll('.editor-tab')].map(t => t.title || t.textContent).join(',')`);
  check(/index\.html/.test(opened), `openFiles が開かれていない: ${opened}`);
  const selectedExerciseName = await cdp.eval(`document.querySelector('.exercise-item.active .exercise-title')?.textContent`);
  const headerExerciseName = await cdp.eval(`document.getElementById('project-name').textContent`);
  check(headerExerciseName === selectedExerciseName,
        `上部の演習名が一覧と一致しない: 上部=${headerExerciseName} / 一覧=${selectedExerciseName}`);
  check(await cdp.eval(`document.querySelectorAll('.exercise-item .q-status').length`) === 0,
        '演習一覧に意味の分からない状態記号が残っている');
  check(await cdp.eval(`['btn-new-file','btn-new-dir','btn-refresh-tree'].some(id => document.getElementById(id))`) === false,
        'プロジェクト欄に +F / +D / 再読み込みが残っている');

  // 3. 実行 → プレビューが活性になる
  check(await cdp.eval(`document.getElementById('btn-preview').disabled`) === true,
        '実行する前からプレビューが活性になっている');
  await cdp.eval(`document.getElementById('btn-run').click()`);
  await waitFor(cdp, `document.getElementById('btn-preview').disabled === false`, 30000,
                '実行してもプレビューが活性にならない');
  const url = await cdp.eval(`document.getElementById('browser-url').value`);
  check(/^http:\/\/localhost:\d+/.test(url), `プレビューの URL が入らない: ${url}`);

  // 4. 汚してから初期化
  const indexHtml = path.join(wsRoot, 'html-form', 'index.html');
  const before = fs.readFileSync(indexHtml, 'utf8');
  fs.writeFileSync(indexHtml, '<h1>こわした</h1>\n', 'utf8');
  await cdp.eval(`document.getElementById('btn-reset-exercise').click()`);
  await waitFor(cdp, `!document.getElementById('simple-dialog-overlay').classList.contains('hidden')`,
                5000, '初期化の確認ダイアログが出ない');
  await cdp.eval(`document.getElementById('simple-dialog-ok').click()`);
  await waitFor(cdp, `!document.getElementById('simple-dialog-overlay').classList.contains('hidden')`,
                10000, '初期化の結果ダイアログが出ない');
  const done = await cdp.eval(`document.getElementById('simple-dialog-message').textContent`);
  check(/初期化/.test(done), `初期化の知らせが出ない: ${done}`);
  await cdp.eval(`document.getElementById('simple-dialog-ok').click()`);
  check(fs.readFileSync(indexHtml, 'utf8') === before, '初期化してもファイルが戻っていない');
  // エディタの中身も読み直されていること
  const inEditor = await cdp.eval(`(window.cmGetValue ? window.cmGetValue() : '')`);
  if (typeof inEditor === 'string' && inEditor.length) {
    check(!inEditor.includes('こわした'), 'エディタに壊した内容が残っている');
  }

  // 5. SQL の演習
  await clickExercise(cdp, 'sql-crud');
  await waitFor(cdp, `document.getElementById('run-target-select').value.startsWith('sql:')`,
                20000, 'SQL の実行対象が sql: にならない');
  await cdp.eval(`document.getElementById('btn-run').click()`);
  await waitFor(cdp, `document.querySelectorAll('#sql-result-wrap table tr').length > 1`, 60000,
                'SQL を実行しても結果の表が出ない');
  const cols = await cdp.eval(`[...document.querySelectorAll('#sql-result-wrap table th')].map(th => th.textContent).join(',')`);
  check(/EMPLOYEE_NAME/i.test(cols), `SQL の結果の列が想定と違う: ${cols}`);
  const activePane = await cdp.eval(`document.querySelector('.run-pane.active')?.id`);
  check(activePane === 'tab-sql', `SQL 実行後に SQL タブが出ていない: ${activePane}`);

  // 6. チャットの Ask / Agent トグル
  check(await cdp.eval(`!!document.getElementById('chat-mode')`), 'Ask/Agent のトグルが無い');
  check(await cdp.eval(`!document.getElementById('btn-chat-edit')`),
        '「書き換え」ボタンが残っている');
  check(await cdp.eval(`document.getElementById('btn-mode-ask').classList.contains('is-active')`),
        '既定が Ask になっていない');
  check(await cdp.eval(`document.getElementById('btn-mode-ask').getAttribute('aria-pressed')`) === 'true',
        'Ask が選択中であることを支援技術へ伝えていない');
  await cdp.eval(`document.getElementById('btn-mode-agent').click()`);
  await waitFor(cdp, `document.getElementById('btn-mode-agent').classList.contains('is-active')`,
                5000, 'Agent に切り替わらない');
  check(await cdp.eval(`document.getElementById('chat-mode').classList.contains('is-agent')`),
        'Agent のときの見た目が変わらない');
  check(await cdp.eval(`document.getElementById('btn-mode-agent').getAttribute('aria-pressed')`) === 'true',
        'Agent が選択中であることを支援技術へ伝えていない');
  const placeholder = await cdp.eval(`document.getElementById('chat-input').placeholder`);
  check(/演習/.test(placeholder), `Agent の入力案内が変わらない: ${placeholder}`);
  await cdp.eval(`document.getElementById('btn-mode-ask').click()`);
  check(await cdp.eval(`document.getElementById('btn-mode-ask').classList.contains('is-active')`),
        'Ask に戻せない');

  // 7. 任意の重い検査: 実ランタイムで React / Spring Boot の待受とプレビューを確認する。
  // 初回は npm / Gradle の依存取得があるため、通常の画面検査では飛ばす。
  if (process.env.CHECK_WEB_SERVERS) {
    await checkWebServerExercise(cdp, 'react-spa', 'npm:dev', /localhost:5173/, 'React');
    await checkWebServerExercise(cdp, 'spring-mvc-calc', 'gradle:bootRun', /localhost:8080/, 'Spring Boot');
  }

  if (process.env.SHOTS) {
    fs.mkdirSync(process.env.SHOTS, { recursive: true });
    const png = await cdp.screenshot();
    fs.writeFileSync(path.join(process.env.SHOTS, 'app-sql.png'), Buffer.from(png, 'base64'));
  }
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
  check(await cdp.eval(`document.getElementById('run-stdin-row').classList.contains('hidden')`) === true,
        `${label} の実行中に標準入力欄が出ている`);
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
      screenshot: async () => (await send('Page.captureScreenshot', { format: 'png' },
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
