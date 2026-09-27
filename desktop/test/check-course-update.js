// ═══════════════════════════════════════════════════════════
//  講座の単独更新と、演習ごとの出力タブの確認
//
//  手元に配信先 (index.json と .codpack) を立て、CODINABLE_UPDATE_URL で向けて起動する
//  確かめること
//    1. まだ始めていない講座を「新規」で始めると、新しい版を取り込んでから始まる
//       (個人の置き場に <id>@<version>/ ができ、その版に固定される)
//    2. 取り組み中の講座は、新しい版が配信されていても「新規」で変わらない
//       (配信先へ問い合わせもしない)
//    3. 配信先に届かないときも、手元の版で始められる
//    4. 演習の tabs に応じて出力タブが出し分けられる
//       (静的ページ: SQL / メッセージングなどは出ない、SQL の演習: SQL が出る)
//
//    node test/check-course-update.js
// ═══════════════════════════════════════════════════════════

const { spawn }  = require('child_process');
const http       = require('http');
const fs         = require('fs');
const os         = require('os');
const path       = require('path');
const zlib       = require('zlib');
const crypto     = require('crypto');
const yaml       = require('js-yaml');
const WebSocket  = require('ws');

const COURSES_DIR = path.resolve(__dirname, '../../courses');
const problems = [];
const check = (cond, message) => { if (!cond) problems.push(message); return !!cond; };
const sleep = ms => new Promise(r => setTimeout(r, ms));

const NEW_VERSION = '9.9.0';
const RENAMED     = '（更新版）HTMLの基本構造を読む';

main().then(() => {
  for (const p of problems) console.log('NG   ' + p);
  console.log(problems.length ? `NG ${problems.length} 件` : 'すべて期待どおり');
  process.exit(problems.length ? 1 : 0);
}).catch(err => {
  console.log('NG   検査そのものが失敗: ' + err.message);
  process.exit(1);
});

/** courses/<id> を読み、course.yaml を書き換えた codpack を作る */
function makePack(id, edit) {
  const dir = path.join(COURSES_DIR, id);
  const files = [];
  const walk = (d, base) => {
    for (const e of fs.readdirSync(d, { withFileTypes: true })) {
      const full = path.join(d, e.name);
      if (e.isDirectory()) { if (!['build', '.gradle', 'bin', 'node_modules'].includes(e.name)) walk(full, base); }
      else files.push(path.relative(base, full).split(path.sep).join('/'));
    }
  };
  walk(dir, dir);
  const meta = yaml.load(fs.readFileSync(path.join(dir, 'course.yaml'), 'utf8'));
  edit(meta);
  const packFiles = files.map(rel => ({
    path: rel,
    data: rel === 'course.yaml'
      ? Buffer.from(yaml.dump(meta)).toString('base64')
      : fs.readFileSync(path.join(dir, ...rel.split('/'))).toString('base64'),
  }));
  const buffer = zlib.gzipSync(Buffer.from(JSON.stringify({ format: 1, id, version: meta.version, files: packFiles })));
  return { id, version: meta.version, file: `${id}-${meta.version}.codpack`, buffer,
           sha256: crypto.createHash('sha256').update(buffer).digest('hex') };
}

async function main() {
  // ── 配信先 ──
  const packs = [
    makePack('webapp-archi-overview', m => { m.version = NEW_VERSION; m.exercises[0].names.ja = RENAMED; }),
    makePack('spring-boot-cicd',      m => { m.version = NEW_VERSION; }),
  ];
  const requests = [];
  // GitHub Releases と同じく、ダウンロードの URL は別の場所へ 302 で飛ばす
  const server = http.createServer((req, res) => {
    requests.push(req.url);
    if (req.url.startsWith('/courses/')) {
      res.writeHead(302, { location: `/storage${req.url}` });
      res.end();
      return;
    }
    if (req.url === '/storage/courses/index.json') {
      res.setHeader('content-type', 'application/json');
      res.end(JSON.stringify({ format: 1, courses: packs.map(({ buffer, ...e }) => e) }));
      return;
    }
    const pack = packs.find(p => req.url === `/storage/courses/${p.file}`);
    if (pack) { res.end(pack.buffer); return; }
    res.statusCode = 404; res.end();
  });
  await new Promise(r => server.listen(0, '127.0.0.1', r));
  const baseUrl = `http://127.0.0.1:${server.address().port}`;

  // ── 受講者の環境: spring-boot-cicd はすでに取り組み中 ──
  const tmp      = fs.mkdtempSync(path.join(os.tmpdir(), 'codinable-update-'));
  const userData = path.join(tmp, 'ud');
  const wsRoot   = path.join(tmp, 'ws');
  fs.mkdirSync(userData, { recursive: true });
  fs.writeFileSync(path.join(userData, 'codinable-config.json'),
    JSON.stringify({ workspaceRoot: wsRoot, uiLang: 'ja' }, null, 2), 'utf8');
  const started = path.join(wsRoot, 'junit-shipping');
  fs.mkdirSync(path.join(started, '.codinable'), { recursive: true });
  fs.writeFileSync(path.join(started, '.codinable', 'project.json'),
    JSON.stringify({ courseId: 'spring-boot-cicd', template: 'junit-shipping' }), 'utf8');

  const port = await freePort();
  const env  = { ...process.env, CODINABLE_UPDATE_URL: baseUrl };
  delete env.CODINABLE_COURSES;
  const child = spawn(path.resolve(__dirname, '../node_modules/electron/dist/electron.exe'), [
    path.resolve(__dirname, '..'), `--remote-debugging-port=${port}`, `--user-data-dir=${userData}`,
  ], { stdio: 'ignore', env });

  let cdp;
  try {
    cdp = await connect(port);
    await waitFor(cdp, `document.querySelectorAll('.exercise-item').length > 0`, 20000, '演習一覧が出ない');

    const readConfig = () => JSON.parse(fs.readFileSync(path.join(userData, 'codinable-config.json'), 'utf8'));
    // 起動時に、取り組み中の講座は今の版に固定される
    check(readConfig().coursePins?.['spring-boot-cicd'] === '1.0.0',
          `取り組み中の講座が今の版に固定されていない: ${JSON.stringify(readConfig().coursePins)}`);

    // ── 1. まだ始めていない講座を新規で始める → 新しい版を取り込む ──
    await startFromDialog(cdp, 'webapp-archi-overview');
    await waitFor(cdp, `/9\\.9\\.0/.test(document.getElementById('simple-dialog-message').textContent) &&
      !document.getElementById('simple-dialog-overlay').classList.contains('hidden')`, 20000,
      '新しい版を取り込んだ知らせが出ない');
    await cdp.eval(`document.getElementById('simple-dialog-ok').click()`);
    await waitFor(cdp, `document.querySelector('.exercise-item.active .exercise-title')?.textContent === ${JSON.stringify(RENAMED)}`,
                  20000, '取り込んだ版の演習で始まっていない');
    check(fs.existsSync(path.join(userData, 'courses', `webapp-archi-overview@${NEW_VERSION}`, 'course.yaml')),
          '個人の置き場に取り込んだ版が無い');
    check(readConfig().coursePins?.['webapp-archi-overview'] === NEW_VERSION,
          `取り込んだ版に固定されていない: ${JSON.stringify(readConfig().coursePins)}`);
    console.log('OK  新規で始めると新しい版を取り込んでから始まる');

    // ── 4. 出力タブの出し分け (静的ページ) ──
    const visibleTabs = () => cdp.eval(`[...document.querySelectorAll('.run-tab')]
      .filter(t => !t.classList.contains('hidden')).map(t => t.id).join(',')`);
    const staticTabs = await visibleTabs();
    check(staticTabs === 'run-tab-output,run-tab-browser', `静的ページの演習のタブが想定と違う: ${staticTabs}`);
    await clickExercise(cdp, 'sql-basics');
    await waitFor(cdp, `document.getElementById('run-target-select').value.startsWith('sql:')`, 20000, 'SQL の演習が開かない');
    const sqlTabs = await visibleTabs();
    check(sqlTabs === 'run-tab-output,run-tab-sql', `SQL の演習のタブが想定と違う: ${sqlTabs}`);
    console.log(`OK  出力タブの出し分け (静的: ${staticTabs} / SQL: ${sqlTabs})`);

    // ── 2. 取り組み中の講座は新規で選んでも変わらない ──
    requests.length = 0;
    await startFromDialog(cdp, 'spring-boot-cicd');
    await waitFor(cdp, `projectInfo?.courseId === 'spring-boot-cicd'`, 20000, '取り組み中の講座が開かない');
    check(!requests.length, `取り組み中の講座で配信先へ問い合わせている: ${requests.join(', ')}`);
    check(!fs.existsSync(path.join(userData, 'courses', `spring-boot-cicd@${NEW_VERSION}`)),
          '取り組み中の講座に新しい版を取り込んでいる');
    const cicdTabs = await visibleTabs();
    check(cicdTabs.includes('run-tab-tests') && !cicdTabs.includes('run-tab-sql'),
          `Gradle の演習のタブが想定と違う: ${cicdTabs}`);
    console.log('OK  取り組み中の講座は開始したときの版のまま');

    // ── 3. 配信先に届かなくても、手元の版で始められる ──
    await new Promise(r => server.close(r));
    await startFromDialog(cdp, 'java-db-access');
    await waitFor(cdp, `projectInfo?.courseId === 'java-db-access'`, 30000, '配信先に届かないと講座を始められない');
    check(await cdp.eval(`document.getElementById('simple-dialog-overlay').classList.contains('hidden')`),
          '配信先に届かないときにダイアログが出ている');
    check(readConfig().coursePins?.['java-db-access'] === '1.0.0', '配信先に届かないとき、手元の版に固定されていない');
    console.log('OK  配信先に届かなくても手元の版で始まる');
  } catch (err) {
    problems.push(err.message);
  } finally {
    if (cdp) cdp.close();
    child.kill();
    server.close();
    await sleep(500);
    try { fs.rmSync(tmp, { recursive: true, force: true }); } catch {}
  }
}

async function startFromDialog(cdp, id) {
  await cdp.eval(`document.getElementById('btn-course-dialog').click()`);
  await waitFor(cdp, `document.querySelector('.course-card-new[data-course-id="${id}"]')`, 10000, `新規に ${id} が無い`);
  await cdp.eval(`document.querySelector('.course-card-new[data-course-id="${id}"]').click()`);
}

async function clickExercise(cdp, id) {
  await cdp.eval(`document.querySelector('.exercise-item[data-exercise-id="${id}"]').click()`);
}

// ── CDP ─────────────────────────────────────────────────

async function waitFor(cdp, condition, timeoutMs, message) {
  const until = Date.now() + timeoutMs;
  while (Date.now() < until) {
    try { if (await cdp.eval(`!!(${condition})`)) return true; } catch {}
    await sleep(300);
  }
  problems.push(`${message}（${timeoutMs / 1000} 秒待った）`);
  return false;
}

async function connect(port, timeoutMs = 40000) {
  const until = Date.now() + timeoutMs;
  while (Date.now() < until) {
    await sleep(500);
    try {
      const targets = JSON.parse(await get(`http://127.0.0.1:${port}/json/list`));
      const page = targets.find(t => t.type === 'page' && /index\.html/.test(t.url || ''));
      if (page) return await open(page.webSocketDebuggerUrl);
    } catch { /* 起動待ち */ }
  }
  throw new Error('アプリの画面に接続できなかった');
}

function open(url) {
  return new Promise((resolve, reject) => {
    const ws = new WebSocket(url);
    const pending = new Map();
    let id = 0;
    ws.on('open', () => resolve({
      eval: expression => new Promise((res, rej) => {
        const mid = ++id;
        pending.set(mid, { res, rej });
        ws.send(JSON.stringify({ id: mid, method: 'Runtime.evaluate',
          params: { expression, awaitPromise: true, returnByValue: true } }));
        setTimeout(() => { if (pending.delete(mid)) rej(new Error('返ってこない')); }, 20000);
      }),
      close: () => ws.close(),
    }));
    ws.on('error', reject);
    ws.on('message', data => {
      const msg = JSON.parse(data);
      const waiter = pending.get(msg.id);
      if (!waiter) return;
      pending.delete(msg.id);
      if (msg.error || msg.result?.exceptionDetails) {
        return waiter.rej(new Error(msg.error?.message || msg.result.exceptionDetails.text));
      }
      waiter.res(msg.result?.result?.value);
    });
  });
}

function freePort() {
  return new Promise((resolve, reject) => {
    const s = require('net').createServer();
    s.once('error', reject);
    s.listen(0, '127.0.0.1', () => { const { port } = s.address(); s.close(() => resolve(port)); });
  });
}

function get(url) {
  return new Promise((resolve, reject) => {
    const req = http.get(url, res => {
      let body = '';
      res.on('data', d => (body += d));
      res.on('end', () => resolve(body));
    });
    req.setTimeout(3000, () => req.destroy(new Error('timeout')));
    req.on('error', reject);
  });
}
