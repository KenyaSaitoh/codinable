// ═══════════════════════════════════════════════════════════
//  コースの組み合わせごとの画面の確認
//
//  受講者の手元は「講座ごとのインストーラで入れたコース」が 1 つ以上並ぶ
//  npm start と同じ CODINABLE_COURSES で読み込むコースを絞り、次を確かめる
//    - 1 コースだけ: ヘッダーのコース選択は講座名の表示だけで、選べない
//    - 2 コース:     選べて、切り替えると演習一覧がそのコースのものに入れ替わる
//    - 全コース:     courses/ のコースがすべて並ぶ
//
//    node test/check-course-filter.js
// ═══════════════════════════════════════════════════════════

const { spawn }  = require('child_process');
const http       = require('http');
const fs         = require('fs');
const os         = require('os');
const path       = require('path');
const yaml       = require('js-yaml');
const WebSocket  = require('ws');

const COURSES_DIR = path.resolve(__dirname, '../../courses');
const problems = [];
const check = (cond, message) => { if (!cond) problems.push(message); return !!cond; };
const sleep = ms => new Promise(r => setTimeout(r, ms));

/** courses/ のコース id と演習数 (order 順) */
function bundledCourses() {
  return fs.readdirSync(COURSES_DIR, { withFileTypes: true })
    .filter(e => e.isDirectory() && fs.existsSync(path.join(COURSES_DIR, e.name, 'course.yaml')))
    .map(e => yaml.load(fs.readFileSync(path.join(COURSES_DIR, e.name, 'course.yaml'), 'utf8')))
    .sort((a, b) => (a.order ?? 999) - (b.order ?? 999) || String(a.id).localeCompare(String(b.id)))
    .map(m => ({ id: String(m.id), exercises: (m.exercises || []).length }));
}

main().then(() => {
  for (const p of problems) console.log('NG   ' + p);
  console.log(problems.length ? `NG ${problems.length} 件` : 'すべて期待どおり');
  process.exit(problems.length ? 1 : 0);
}).catch(err => {
  console.log('NG   検査そのものが失敗: ' + err.message);
  process.exit(1);
});

async function main() {
  const all = bundledCourses();
  if (!check(all.length >= 2, 'courses/ にコースが 2 つ以上ない')) return;
  const [a, b] = [all[all.length - 1], all[all.length - 2]];

  await withApp(a.id, async cdp => {
    const state = await selectState(cdp);
    check(state.options.join() === a.id, `1 コース: 選択肢が ${a.id} だけになっていない (${state.options})`);
    check(state.disabled, '1 コース: コース選択が選べる状態になっている');
    check(state.exerciseCount === a.exercises, `1 コース: 演習数が ${a.exercises} でない (${state.exerciseCount})`);
    console.log(`OK  1 コース (${a.id}): 表示だけで選べない`);
  });

  await withApp(`${a.id},${b.id}`, async cdp => {
    const state = await selectState(cdp);
    check(state.options.join() === [b.id, a.id].join(), `2 コース: 選択肢が order 順の 2 つでない (${state.options})`);
    check(!state.disabled, '2 コース: コース選択が選べない');
    for (const course of [a, b]) {
      await cdp.eval(`(() => { const s = document.getElementById('active-course-select');
        s.value = ${JSON.stringify(course.id)}; s.dispatchEvent(new Event('change')); })()`);
      const count = await cdp.eval(`Number(document.getElementById('exercise-count').textContent)`);
      check(count === course.exercises, `2 コース: ${course.id} に切り替えた演習数が ${course.exercises} でない (${count})`);
    }
    console.log(`OK  2 コース (${b.id}, ${a.id}): 選べて、切り替えで演習一覧が入れ替わる`);
  });

  await withApp('', async cdp => {
    const state = await selectState(cdp);
    check(state.options.join() === all.map(c => c.id).join(), `全コース: courses/ と並びが一致しない (${state.options})`);
    check(!state.disabled, '全コース: コース選択が選べない');
    console.log(`OK  全コース (${all.length} 件)`);
  });
}

async function selectState(cdp) {
  await waitFor(cdp, `document.getElementById('active-course-select').options.length > 0 &&
    document.getElementById('active-course-select').value !== ''`, 20000);
  return cdp.eval(`(() => {
    const s = document.getElementById('active-course-select');
    return { options: [...s.options].map(o => o.value), disabled: s.disabled,
             exerciseCount: Number(document.getElementById('exercise-count').textContent) };
  })()`);
}

/** CODINABLE_COURSES を付けて起動し、画面につないで fn を実行する */
async function withApp(filter, fn) {
  const tmp      = fs.mkdtempSync(path.join(os.tmpdir(), 'codinable-filter-'));
  const userData = path.join(tmp, 'ud');
  fs.mkdirSync(userData, { recursive: true });
  fs.writeFileSync(path.join(userData, 'codinable-config.json'),
    JSON.stringify({ workspaceRoot: path.join(tmp, 'ws'), uiLang: 'ja' }, null, 2), 'utf8');

  const port = await freePort();
  const env = { ...process.env };
  if (filter) env.CODINABLE_COURSES = filter; else delete env.CODINABLE_COURSES;
  const child = spawn(path.resolve(__dirname, '../node_modules/electron/dist/electron.exe'), [
    path.resolve(__dirname, '..'), `--remote-debugging-port=${port}`, `--user-data-dir=${userData}`,
  ], { stdio: 'ignore', env });

  let cdp;
  try {
    cdp = await connect(port);
    await fn(cdp);
  } catch (err) {
    problems.push(`${filter || '全コース'}: ${err.message}`);
  } finally {
    if (cdp) cdp.close();
    child.kill();
    await sleep(500);
    try { fs.rmSync(tmp, { recursive: true, force: true }); } catch {}
  }
}

async function waitFor(cdp, condition, timeoutMs) {
  const until = Date.now() + timeoutMs;
  while (Date.now() < until) {
    try { if (await cdp.eval(`!!(${condition})`)) return; } catch {}
    await sleep(300);
  }
  throw new Error(`待っても条件が満たされない: ${condition.replace(/\s+/g, ' ').slice(0, 80)}`);
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
      if (msg.error) return waiter.rej(new Error(msg.error.message));
      if (msg.result?.exceptionDetails) return waiter.rej(new Error(msg.result.exceptionDetails.text));
      waiter.res(msg.result?.result?.value);
    });
  });
}

function get(url) {
  return new Promise((resolve, reject) => {
    http.get(url, res => {
      let body = '';
      res.on('data', d => body += d);
      res.on('end', () => resolve(body));
    }).on('error', reject);
  });
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
