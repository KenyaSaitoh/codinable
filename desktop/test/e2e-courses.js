// 講座の E2E 検査。アプリを起動し、画面を操作して演習を 1 つずつ動かす
//
// 演習ごとに確かめること
//   1. 一覧から選ぶと、作業用プロジェクトが雛形から作られる
//   2. openFiles がエディタに開き、実行対象が run の値になる
//   3. 「実行」の結果が種類どおりになる
//      - Web サーバー (bootRun / npm dev / Django …) … URL を検知し、HTTP で 5xx 以外が返る
//      - gradle test … BUILD SUCCESSFUL で、テスト結果が 1 件以上・失敗 0 件
//      - それ以外 (gradle run / build / スクリプト) … 終了コード 0
//      - 静的ページ … プレビューが有効になり、200 が返る
//      - SQL … 並んでいる .sql をすべて流し、エラーにならない (わざと失敗させる教材は除く)
//      - スクリプトの演習は、並んでいるファイルをすべて動かす
//   4. 付き添いのプロセス (customer-hub) やメッセージングを使う演習は、それが起動する
//
//   node test/e2e-courses.js <course-id>[,<course-id>…] [--only=<exercise-id>,…]
//   結果は E2E_OUT (既定は %TEMP%/codinable-e2e) の <course>.json と、失敗時のスクリーンショット
//
// ワークスペースと設定は一時ディレクトリに向ける (Gradle の依存キャッシュはふだんのものを使う)

const { spawn } = require('child_process');
const http  = require('http');
const fs    = require('fs');
const os    = require('os');
const path  = require('path');
const net   = require('net');
const yaml  = require('js-yaml');
const WebSocket = require('ws');

const REPO = path.resolve(__dirname, '..', '..');
const OUT  = process.env.E2E_OUT || path.join(os.tmpdir(), 'codinable-e2e');
fs.mkdirSync(OUT, { recursive: true });

// 画面を持たないサーバー (画面は別プロセスの受信側や React が受け持つ)。/ の 404 は想定どおり
const NO_PAGE_SERVERS = new Set(['spring-kafka-employee', 'spring-amqp-employee', 'spring-websocket-employee']);

// わざとエラーにする SQL (制約違反を見せる教材)
const SQL_EXPECTED_ERRORS = new Set(['sql-ddl:02_constraints.sql']);

const args = process.argv.slice(2);
const courseIds = (args.find(a => !a.startsWith('--')) || '').split(',').filter(Boolean);
const only = new Set((args.find(a => a.startsWith('--only=')) || '').slice(7).split(',').filter(Boolean));
if (!courseIds.length) { console.error('usage: node test/e2e-courses.js <course-id>[,…] [--only=a,b]'); process.exit(2); }

const sleep = ms => new Promise(r => setTimeout(r, ms));
const log = (...a) => console.log(new Date().toISOString().slice(11, 19), ...a);

main().then(code => process.exit(code), err => { console.error(err); process.exit(1); });

async function main() {
  let failed = 0;
  for (const id of courseIds) failed += await runCourse(id);
  return failed ? 1 : 0;
}

async function runCourse(courseId) {
  const course = yaml.load(fs.readFileSync(path.join(REPO, 'courses', courseId, 'course.yaml'), 'utf8'));
  const exercises = course.exercises.filter(e => !only.size || only.has(e.id));
  const tmp = fs.mkdtempSync(path.join(os.tmpdir(), `codinable-e2e-${courseId}-`));
  const userData = path.join(tmp, 'ud');
  const wsRoot = path.join(tmp, 'ws');
  fs.mkdirSync(userData, { recursive: true });
  fs.mkdirSync(wsRoot, { recursive: true });
  fs.writeFileSync(path.join(userData, 'codinable-config.json'),
                   JSON.stringify({ workspaceRoot: wsRoot, uiLang: 'ja' }, null, 2), 'utf8');

  const port = await freePort();
  const electron = path.resolve(__dirname, '../node_modules/electron/dist/electron.exe');
  const child = spawn(electron, [path.resolve(__dirname, '..'), `--remote-debugging-port=${port}`, `--user-data-dir=${userData}`],
                      { stdio: ['ignore', 'pipe', 'pipe'], env: { ...process.env, CODINABLE_COURSES: courseId } });
  const appLog = [];
  child.stdout.on('data', d => appLog.push(String(d)));
  child.stderr.on('data', d => appLog.push(String(d)));

  const results = [];
  const reportFile = path.join(OUT, `${courseId}${only.size ? '.only' : ''}.json`);
  const save = () => fs.writeFileSync(reportFile, JSON.stringify({ course: courseId, results }, null, 2));
  let cdp;
  try {
    cdp = await connect(port);
    await waitFor(cdp, `document.querySelectorAll('.exercise-item').length > 0`, 30000, '演習一覧が出ない');
    // 画面の出来事を拾う (実行の終了・URL・テスト結果)
    await cdp.eval(`(() => {
      globalThis.__e2e = { exit: null, url: null, tests: null };
      window.api.onRunExit(p => { __e2e.exit = p; });
      window.api.onRunUrl(p => { __e2e.url = p.url; });
      window.api.onRunTestResults(d => { __e2e.tests = d.summary; });
      return true; })()`);
    for (const ex of exercises) {
      const started = Date.now();
      log(`[${courseId}] ▶ ${ex.id} (${ex.run || '-'})`);
      const r = { id: ex.id, run: ex.run, problems: [], notes: [] };
      try { await checkExercise(cdp, courseId, ex, r); }
      catch (err) { r.problems.push(`検査が中断: ${err.message}`); }
      r.seconds = Math.round((Date.now() - started) / 1000);
      r.ok = !r.problems.length;
      if (process.env.E2E_SHOTS && r.ok) {
        await cdp.eval(`[...document.querySelectorAll('.exercise-item')].find(e => e.dataset.exerciseId === ${JSON.stringify(ex.id)})?.scrollIntoView({ block: 'center' }); true`).catch(() => {});
        try { fs.writeFileSync(path.join(OUT, `${courseId}__${ex.id}.png`), Buffer.from(await cdp.screenshot(), 'base64')); } catch {}
      }
      if (!r.ok) {
        r.output = String(await cdp.eval(`runLog.slice(-6000)`).catch(() => '')).trim();
        r.sqlPane = String(await cdp.eval(`document.getElementById('sql-result-wrap').textContent.slice(0, 1500)`).catch(() => ''));
        try { fs.writeFileSync(path.join(OUT, `${courseId}__${ex.id}.png`), Buffer.from(await cdp.screenshot(), 'base64')); } catch {}
      }
      log(`[${courseId}] ${r.ok ? 'OK ' : 'NG '} ${ex.id} ${r.seconds}s ${r.problems.join(' / ')}`);
      results.push(r);
      save();
      await stopEverything(cdp);
    }
  } catch (err) {
    results.push({ id: '(course)', ok: false, problems: [err.message], appLog: appLog.join('').slice(-3000) });
    save();
  } finally {
    if (cdp) {
      for (const id of ['kafka', 'rabbitmq']) await cdp.eval(`window.api.messagingStop('${id}')`).catch(() => {});
      await cdp.eval(`Promise.all(companionStates.map(c => window.api.companionStop(c.id)))`).catch(() => {});
      await cdp.eval('setTimeout(() => window.close(), 100); true').catch(() => {});
      cdp.close();
    }
    await sleep(8000);
    child.kill();
    try { fs.rmSync(tmp, { recursive: true, force: true }); } catch {}
  }
  const bad = results.filter(r => !r.ok);
  log(`[${courseId}] 完了: ${results.length - bad.length}/${results.length} OK`);
  return bad.length;
}

// ── 1 演習 ──────────────────────────────────────────────

async function checkExercise(cdp, courseId, ex, r) {
  const templateName = ex.dir || ex.id;
  await clickExercise(cdp, ex.id);
  // .codinable/project.json の template には演習の id が入る (雛形のフォルダ名ではない)
  await waitFor(cdp, `projectInfo?.template === ${JSON.stringify(ex.id)} && !!project`, 60000,
                `演習を選んでもプロジェクトが開かない (${ex.id})`);
  // 実行対象の反映と openFiles を待つ
  await sleep(1500);
  if (ex.run) {
    const ok = await waitFor(cdp, `document.getElementById('run-target-select').value === ${JSON.stringify(ex.run)}`, 15000, null);
    if (!ok) {
      const options = await cdp.eval(`[...document.getElementById('run-target-select').options].map(o => o.value).join(', ')`);
      r.problems.push(`実行対象が ${ex.run} にならない (並んでいるもの: ${options})`);
      return;
    }
  }
  for (const f of ex.openFiles || []) {
    if (!await cdp.eval(`openFiles.has(${JSON.stringify(f)})`)) r.problems.push(`openFiles の ${f} が開いていない`);
  }
  const active = await cdp.eval(`activeFile`);
  if ((ex.openFiles || []).length && !(ex.openFiles || []).includes(active)) {
    r.problems.push(`表のタブが openFiles に無いファイル: ${active}`);
  }
  if (!ex.run) return;

  const [kind] = ex.run.split(':');
  if (kind === 'sql') return checkSql(cdp, templateName, r);
  if (kind === 'static') return checkStatic(cdp, r);

  // スクリプトの演習は並んでいるファイルをすべて動かす (01_… 02_… の連番)
  let targets = [ex.run];
  if (kind === 'file') {
    targets = await cdp.eval(`[...document.getElementById('run-target-select').options].map(o => o.value).filter(v => v.startsWith('file:'))`);
    if (!targets.includes(ex.run)) targets.unshift(ex.run);
  }
  for (const target of targets) {
    await runTarget(cdp, target, ex, r);
    await stopRun(cdp);
  }
}

async function runTarget(cdp, target, ex, r) {
  await cdp.eval(`(() => { const s = document.getElementById('run-target-select'); s.value = ${JSON.stringify(target)};
                   s.dispatchEvent(new Event('change')); __e2e.exit = null; __e2e.url = null; __e2e.tests = null;
                   document.getElementById('btn-run').click(); return true; })()`);
  const isGradle = target.startsWith('gradle:');
  const limit = isGradle ? 15 * 60_000 : 5 * 60_000;
  const until = Date.now() + limit;
  let state = null;
  while (Date.now() < until) {
    state = await cdp.eval(`({ exit: __e2e.exit, url: __e2e.url, tests: __e2e.tests, running })`);
    if (state.exit || state.url) break;
    await sleep(1500);
  }
  const label = target;
  if (!state.exit && !state.url) { r.problems.push(`${label}: ${limit / 60000} 分たっても終わらず、URL も出ない`); return; }

  if (state.url && !state.exit) {
    // サーバーとして起動した。HTTP で答えるか (5xx 以外)
    // プレビューが開いた URL (演習の preview パスが付く) を確かめる。404 は受講者にはエラー画面に見える
    await sleep(1500);
    const shownUrl = await cdp.eval(`document.getElementById('browser-url').value`) || state.url;
    const res = await httpStatus(shownUrl);
    if (res.error) r.problems.push(`${label}: ${shownUrl} に繋がらない (${res.error})`);
    else if (res.status >= 500 || (res.status === 404 && !NO_PAGE_SERVERS.has(ex.id))) r.problems.push(`${label}: プレビューの ${shownUrl} が ${res.status} を返した`);
    else r.notes.push(`${label}: ${shownUrl} → ${res.status}`);
    if (process.env.E2E_SHOTS) {
      await cdp.eval(`showRunPane('tab-browser'); true`);
      await sleep(3000);
      try { fs.writeFileSync(path.join(OUT, `${ex.id}__preview.png`), Buffer.from(await cdp.screenshot(), 'base64')); } catch {}
    }
    const failedStart = await cdp.eval(`/APPLICATION FAILED TO START|BUILD FAILED/.test(runLog)`);
    if (failedStart) r.problems.push(`${label}: 出力に起動失敗がある`);
    // 付き添いのプロセスを使う演習は、それが動くまで待つ
    const processes = await cdp.eval(`(projectInfo?.processes || []).map(p => p.id)`);
    for (const id of processes) {
      const ok = await waitFor(cdp, `companionStates.find(c => c.id === ${JSON.stringify(id)})?.state === 'running'`, 8 * 60_000, null);
      if (!ok) r.problems.push(`${label}: 一緒に起動する ${id} が動かない (${await cdp.eval(`companionStates.find(c => c.id === ${JSON.stringify(id)})?.state`)})`);
      else r.notes.push(`${id} running`);
    }
    // preview を持つプロセス (React の画面など) は、動き出したらプレビューがそこへ移る
    const withPreview = await cdp.eval(`companionStates.filter(c => c.preview && c.state === 'running').map(c => 'http://localhost:' + c.port + c.preview)`);
    for (const url of withPreview) {
      const moved = await waitFor(cdp, `document.getElementById('browser-url').value === ${JSON.stringify(url)}`, 10000, null);
      if (!moved) { r.problems.push(`${label}: プレビューが ${url} に移らない`); continue; }
      const res2 = await httpStatus(url);
      if (res2.error || res2.status >= 400) r.problems.push(`${label}: ${url} → ${res2.status || res2.error}`);
      else r.notes.push(`preview ${url} → ${res2.status}`);
    }
    const services = await cdp.eval(`projectInfo?.services || []`);
    for (const id of services) {
      const st = await cdp.eval(`messagingStates.find(s => s.id === ${JSON.stringify(id)})?.state`);
      if (st !== 'running') r.problems.push(`${label}: ${id} が起動していない (${st})`);
    }
    return;
  }

  // 終わった
  const code = state.exit.code;
  if (code !== 0) { r.problems.push(`${label}: 終了コード ${code}${state.exit.phase ? ` (${state.exit.phase})` : ''}`); return; }
  if (target.startsWith('gradle:') && target.split(/[:\s]/).includes('test')) {
    // テスト結果は run-exit の直前に届く
    const tests = state.tests || await cdp.eval(`__e2e.tests`);
    if (!tests || !tests.total) r.problems.push(`${label}: テスト結果が 0 件`);
    else if (tests.failed) r.problems.push(`${label}: テスト失敗 ${tests.failed}/${tests.total}`);
    else r.notes.push(`${label}: tests ${tests.passed}/${tests.total}`);
    const shown = await cdp.eval(`document.querySelectorAll('.tr-case').length`);
    if (tests?.total && !shown) r.problems.push(`${label}: テスト結果タブに出ていない`);
  } else {
    r.notes.push(`${label}: exit 0`);
  }
  // java-db-access の SampleLauncher は、main が例外で終わっても終了コード 0 で最後に件数と一覧を出す
  // わざと例外にする main (ロックの衝突など) は、mains.txt のその行の直前のコメントに例外名か「例外」と書いてある
  const failures = await cdp.eval(`(() => {
    const m = runLog.match(/例外で終わった main: (\\d+) =====\\r?\\n((?:[ \\t]+- .*\\r?\\n?)*)/);
    return m ? m[2].split(/\\r?\\n/).map(s => s.trim().replace(/^- /, '')).filter(Boolean) : [];
  })()`);
  if (failures.length) {
    const mains = (await cdp.eval(`window.api.wsReadFile(project, 'mains.txt').then(r => r.content || '')`)).split(/\r?\n/);
    const unexpected = failures.filter(f => {
      const m = f.match(/^(\S+) \((\w+)\)/);
      if (!m) return true;
      const i = mains.findIndex(line => !line.trim().startsWith('#') && line.includes(m[1]));
      if (i < 0) return true;
      const comments = [];
      for (let j = i - 1; j >= 0 && mains[j].trim().startsWith('#'); j--) comments.push(mains[j]);
      const text = comments.join('\n');
      return !(text.includes(m[2]) || text.includes('例外'));
    });
    if (unexpected.length) r.problems.push(`${label}: 想定していない例外で終わった main: ${unexpected.join(', ')}`);
    else r.notes.push(`${label}: 想定どおり例外で終わった main: ${failures.join(', ')}`);
  }
}

async function checkSql(cdp, templateName, r) {
  const targets = await cdp.eval(`[...document.getElementById('run-target-select').options].map(o => o.value).filter(v => v.startsWith('sql:'))`);
  if (!targets.length) { r.problems.push('SQL の実行対象が無い'); return; }
  // 2 周流す (2 周目も初期データから始まること)
  for (const round of [1, 2]) {
    for (const target of targets) {
      await cdp.eval(`(() => { const s = document.getElementById('run-target-select'); s.value = ${JSON.stringify(target)};
                       s.dispatchEvent(new Event('change')); document.getElementById('sql-result-wrap').innerHTML = '';
                       document.getElementById('btn-run').click(); return true; })()`);
      const ok = await waitFor(cdp, `!!document.querySelector('#sql-result-wrap table, #sql-result-wrap .sql-error, #sql-result-wrap .sql-message')`, 60000, null);
      if (!ok) { r.problems.push(`${target}: 結果が出ない`); continue; }
      const error = await cdp.eval(`document.querySelector('#sql-result-wrap .sql-error')?.textContent || ''`);
      const expected = SQL_EXPECTED_ERRORS.has(`${templateName}:${target.slice(4)}`);
      if (error && !expected) r.problems.push(`${target} (${round} 周目): ${error.slice(0, 300)}`);
      if (!error && expected) r.problems.push(`${target}: わざと失敗させる SQL がエラーにならない`);
      if (round === 1) r.notes.push(`${target}: ${error ? 'error (expected)' : 'ok'}`);
    }
  }
}

async function checkStatic(cdp, r) {
  await cdp.eval(`document.getElementById('btn-run').click()`);
  const ok = await waitFor(cdp, `previewAvailable && !!document.getElementById('browser-url').value`, 30000, null);
  if (!ok) { r.problems.push('静的ページのプレビューが有効にならない'); return; }
  const url = await cdp.eval(`document.getElementById('browser-url').value`);
  const res = await httpStatus(url);
  if (res.status !== 200) r.problems.push(`${url} → ${res.status || res.error}`);
  else r.notes.push(`${url} → 200`);
}

async function stopRun(cdp) {
  if (await cdp.eval(`running`)) {
    await cdp.eval(`document.getElementById('btn-run-stop').click()`);
    await waitFor(cdp, `!running`, 60000, null);
  }
}

async function stopEverything(cdp) {
  await stopRun(cdp);
  await cdp.eval(`window.api.previewStop ? window.api.previewStop() : null`).catch(() => {});
  await cdp.eval(`Promise.all(companionStates.map(c => window.api.companionStop(c.id)))`).catch(() => {});
  await sleep(1000);
}

async function clickExercise(cdp, id) {
  // チャプターのアコーディオンが閉じていても押せるよう、要素を直接押す
  const ok = await cdp.eval(`(() => {
    const item = [...document.querySelectorAll('.exercise-item')].find(el => el.dataset.exerciseId === ${JSON.stringify(id)} || el.dataset.id === ${JSON.stringify(id)});
    if (!item) return false;
    item.click();
    return true; })()`);
  if (!ok) throw new Error(`演習 ${id} が一覧に無い`);
  for (let i = 0; i < 10; i++) {
    await sleep(400);
    if (await cdp.eval(`!document.getElementById('simple-dialog-overlay').classList.contains('hidden')`)) {
      await cdp.eval(`document.getElementById('simple-dialog-ok').click()`);
      break;
    }
  }
}

// ── 道具 ────────────────────────────────────────────────

async function waitFor(cdp, expr, ms, message) {
  const until = Date.now() + ms;
  while (Date.now() < until) {
    try { if (await cdp.eval(expr)) return true; } catch { /* 画面の読み込み中 */ }
    await sleep(500);
  }
  if (message) throw new Error(message);
  return false;
}

function httpStatus(url) {
  return new Promise(resolve => {
    const req = http.get(url, { timeout: 30000 }, res => { res.resume(); resolve({ status: res.statusCode }); });
    req.on('error', err => resolve({ error: err.message }));
    req.on('timeout', () => { req.destroy(); resolve({ error: 'timeout' }); });
  });
}

function freePort() {
  return new Promise(resolve => {
    const s = net.createServer();
    s.listen(0, '127.0.0.1', () => { const { port } = s.address(); s.close(() => resolve(port)); });
  });
}

function get(url) {
  return new Promise((resolve, reject) => {
    http.get(url, res => { let b = ''; res.on('data', c => { b += c; }); res.on('end', () => resolve(b)); }).on('error', reject);
  });
}

async function connect(port, timeoutMs = 60000) {
  const until = Date.now() + timeoutMs;
  let last = '';
  while (Date.now() < until) {
    await sleep(500);
    try {
      const targets = JSON.parse(await get(`http://127.0.0.1:${port}/json/list`));
      const page = targets.find(t => t.type === 'page' && /index\.html/.test(t.url || ''));
      if (!page) { last = 'no page'; continue; }
      return await open(page.webSocketDebuggerUrl);
    } catch (e) { last = e.message; }
  }
  throw new Error(`アプリに接続できない (${last})`);
}

function open(url) {
  return new Promise((resolve, reject) => {
    const ws = new WebSocket(url, { maxPayload: 256 * 1024 * 1024 });
    const pending = new Map();
    let id = 0;
    const send = (method, params, timeout = 60000) => new Promise((res, rej) => {
      const mid = ++id;
      pending.set(mid, { res, rej });
      ws.send(JSON.stringify({ id: mid, method, params }));
      setTimeout(() => { if (pending.delete(mid)) rej(new Error('CDP timeout: ' + method)); }, timeout);
    });
    ws.on('open', () => resolve({
      eval: async expression => {
        const r = await send('Runtime.evaluate', { expression, awaitPromise: true, returnByValue: true });
        if (r.exceptionDetails) throw new Error((r.exceptionDetails.exception?.description || r.exceptionDetails.text).slice(0, 300));
        return r.result.value;
      },
      screenshot: async () => (await send('Page.captureScreenshot', { format: 'png' })).data,
      close: () => ws.close(),
    }));
    ws.on('error', reject);
    ws.on('message', data => {
      const msg = JSON.parse(data);
      const w = pending.get(msg.id);
      if (!w) return;
      pending.delete(msg.id);
      if (msg.error) w.rej(new Error(msg.error.message)); else w.res(msg.result);
    });
  });
}
