// ═══════════════════════════════════════════════════════════
//  アプリ本体の更新の確認 (パッケージ後のアプリでだけ動く)
//
//  手元に配信先 (latest.yml と setup.exe) を立て、CODINABLE_UPDATE_URL で向けて起動する
//  setup.exe の中身は何もしない exe (whoami.exe) にしてあるので、インストールは起きない
//  確かめること
//    0. まだ 1 度も公開していない (latest.yml が 404) ときは、起動時に何も出さず、
//       「更新を確認」では「最新です」と出す
//    1. 起動して少し経つと「新しいバージョンがあります」が出る (版は 0 埋めで見せる)
//    2. 「あとで」を選ぶと何も起きない
//    3. 設定の「更新を確認」から同じ確認ができ、「今すぐ更新」でダウンロードの進捗が出て、
//       終わるとアプリが閉じる (インストーラが起動される)
//
//    npm run pack
//    node test/check-app-update.js
// ═══════════════════════════════════════════════════════════

const { spawn }  = require('child_process');
const http       = require('http');
const fs         = require('fs');
const os         = require('os');
const path       = require('path');
const crypto     = require('crypto');
const WebSocket  = require('ws');

const APP_EXE = process.env.PACKAGED_APP ||
  path.resolve(__dirname, '../dist-installer/win-unpacked/Codinable.exe');
const NEW_VERSION = '202609.9.0';
const SETUP_NAME  = `Codinable-setup-${NEW_VERSION}.exe`;

const problems = [];
const check = (cond, message) => { if (!cond) problems.push(message); return !!cond; };
const sleep = ms => new Promise(r => setTimeout(r, ms));

main().then(() => {
  for (const p of problems) console.log('NG   ' + p);
  console.log(problems.length ? `NG ${problems.length} 件` : 'すべて期待どおり');
  process.exit(problems.length ? 1 : 0);
}).catch(err => {
  console.log('NG   検査そのものが失敗: ' + err.message);
  process.exit(1);
});

async function main() {
  if (!fs.existsSync(APP_EXE)) throw new Error(`パッケージ後のアプリが無い: ${APP_EXE}（npm run pack）`);

  // ── 配信先 ──
  const setup = fs.readFileSync(path.join(process.env.SystemRoot || 'C:\\Windows', 'System32', 'whoami.exe'));
  const sha512 = crypto.createHash('sha512').update(setup).digest('base64');
  const latestYml = [
    `version: ${NEW_VERSION}`,
    'files:',
    `  - url: ${SETUP_NAME}`,
    `    sha512: ${sha512}`,
    `    size: ${setup.length}`,
    `path: ${SETUP_NAME}`,
    `sha512: ${sha512}`,
    `releaseDate: '${new Date().toISOString()}'`,
    '',
  ].join('\n');
  const requests = [];
  let published = false;   // まだ 1 度も公開していない状態 (latest.yml が 404) から始める
  // GitHub Releases と同じく、ダウンロードの URL は別の場所へ 302 で飛ばす
  const server = http.createServer((req, res) => {
    requests.push(req.url.split('?')[0]);
    if (req.url.startsWith('/app/')) {
      res.writeHead(302, { location: `/storage${req.url}` });
      res.end();
      return;
    }
    if (!published) { res.statusCode = 404; res.end(); return; }
    if (req.url.startsWith('/storage/app/latest.yml')) { res.end(latestYml); return; }
    if (req.url.startsWith(`/storage/app/${SETUP_NAME}`)) {
      res.setHeader('content-length', setup.length);
      res.end(setup);
      return;
    }
    res.statusCode = 404; res.end();
  });
  await new Promise(r => server.listen(0, '127.0.0.1', r));

  const tmp      = fs.mkdtempSync(path.join(os.tmpdir(), 'codinable-appupd-'));
  const userData = path.join(tmp, 'ud');
  fs.mkdirSync(userData, { recursive: true });
  fs.writeFileSync(path.join(userData, 'codinable-config.json'),
    JSON.stringify({ workspaceRoot: path.join(tmp, 'ws'), uiLang: 'ja' }, null, 2), 'utf8');

  const env = { ...process.env, CODINABLE_UPDATE_URL: `http://127.0.0.1:${server.address().port}` };

  // ── 0. まだ 1 度も公開していないとき: 起動時は黙り、「更新を確認」は「最新です」 ──
  {
    const port0 = await freePort();
    const first = spawn(APP_EXE, [`--remote-debugging-port=${port0}`, `--user-data-dir=${userData}`],
                        { stdio: 'ignore', env });
    const gone = new Promise(r => first.once('exit', r));
    const cdp0 = await connect(port0);
    await sleep(9000);   // 起動時の確認 (5 秒後) が終わるのを待つ
    check(await cdp0.eval(`document.getElementById('update-modal').classList.contains('hidden')`),
          '公開前なのに起動時にダイアログが出た');
    await cdp0.eval(`document.getElementById('btn-settings').click()`);
    await sleep(800);
    await cdp0.eval(`document.getElementById('btn-app-update-check').click()`);
    await waitFor(cdp0, `!document.getElementById('update-modal').classList.contains('hidden')`, 20000,
                  '公開前に「更新を確認」してもダイアログが出ない');
    const msg0 = await cdp0.eval(`document.getElementById('upd-message').textContent`);
    check(/最新です/.test(msg0), `公開前の「更新を確認」が「最新です」にならない: ${msg0}`);
    if (/最新です/.test(msg0)) console.log(`OK  公開前: ${msg0}`);
    cdp0.close();
    first.kill();
    await gone;
    await sleep(1500);
  }
  published = true;

  const port = await freePort();
  const child = spawn(APP_EXE, [`--remote-debugging-port=${port}`, `--user-data-dir=${userData}`], {
    stdio: ['ignore', 'pipe', 'pipe'],
    env,
  });
  let exited = false;
  child.once('exit', () => { exited = true; });
  const log = [];
  child.stdout.on('data', d => log.push(String(d)));
  child.stderr.on('data', d => log.push(String(d)));

  let cdp;
  try {
    cdp = await connect(port);
    const modalShown = `!document.getElementById('update-modal').classList.contains('hidden')`;

    // ── 1. 起動時の知らせ ──
    await waitFor(cdp, modalShown, 30000, '起動しても「新しいバージョンがあります」が出ない');
    const title = await cdp.eval(`document.getElementById('upd-title').textContent`);
    const message = await cdp.eval(`document.getElementById('upd-message').textContent`);
    check(title === '新しいバージョンがあります', `起動時のダイアログの題が違う: ${title}`);
    check(message.includes('v202609.09.00') && message.includes('v202609.01.00'),
          `版の見せ方が違う (0 埋めで新旧を出す): ${message}`);
    const buttons = await cdp.eval(`[...document.querySelectorAll('#upd-actions button')].map(b => b.textContent).join(',')`);
    check(buttons === '今すぐ更新,あとで', `ボタンが違う: ${buttons}`);
    console.log(`OK  起動時: ${message}`);

    // ── 2. あとで ──
    await cdp.eval(`document.querySelectorAll('#upd-actions button')[1].click()`);
    await sleep(1500);
    check(await cdp.eval(`document.getElementById('update-modal').classList.contains('hidden')`), '「あとで」で閉じない');
    check(!requests.includes(`/storage/app/${SETUP_NAME}`), '「あとで」なのにダウンロードしている');
    check(!exited, '「あとで」なのにアプリが閉じた');
    console.log('OK  あとで: 何もしない');

    // ── 3. 設定の「更新を確認」→ 今すぐ更新 ──
    await cdp.eval(`document.getElementById('btn-settings').click()`);
    await sleep(800);
    const badge = await cdp.eval(`document.getElementById('version-badge').textContent`);
    check(badge === 'v202609.01.00', `設定のバージョン表示が違う: ${badge}`);
    await cdp.eval(`document.getElementById('btn-app-update-check').click()`);
    await waitFor(cdp, modalShown, 20000, '「更新を確認」でダイアログが出ない');
    const again = await cdp.eval(`document.getElementById('upd-title').textContent + ' / ' + document.getElementById('upd-message').textContent`);
    check(/新しいバージョンがあります/.test(again), `「更新を確認」のダイアログが違う: ${again}`);
    await cdp.eval(`document.querySelectorAll('#upd-actions button')[0].click()`);
    // 進捗ダイアログ (ダウンロードが小さいので一瞬で終わることもある)
    const until = Date.now() + 60000;
    while (!exited && Date.now() < until) await sleep(300);
    const downloadedOk = check(requests.includes(`/storage/app/${SETUP_NAME}`), 'インストーラをダウンロードしていない');
    const exitedOk = check(exited, 'ダウンロードが終わってもアプリが閉じない (インストールに進まない)');
    if (!exited) {
      const shown = await cdp.eval(`document.getElementById('update-modal').classList.contains('hidden') ? '(閉じている)' : document.getElementById('upd-title').textContent + ' / ' + document.getElementById('upd-message').textContent`).catch(() => '?');
      problems.push(`そのときのダイアログ: ${shown}`);
    }
    if (downloadedOk && exitedOk) console.log('OK  今すぐ更新: ダウンロードしてアプリを閉じた');
  } catch (err) {
    problems.push(err.message);
  } finally {
    if (problems.length) {
      console.log('--- アプリの出力 ---');
      console.log(log.join('').split('\n').filter(l => /updater|rror/.test(l)).slice(-20).join('\n'));
      console.log('--- 配信先への要求 --- ' + requests.join(', '));
    }
    try { cdp?.close(); } catch {}
    if (!exited) child.kill();
    server.close();
    await sleep(1000);
    try { fs.rmSync(tmp, { recursive: true, force: true }); } catch {}
    // electron-updater がダウンロードを置く場所を片づける
    const pending = path.join(process.env.LOCALAPPDATA || '', 'codinable-updater');
    try { fs.rmSync(pending, { recursive: true, force: true }); } catch {}
  }
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

async function connect(port, timeoutMs = 60000) {
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
