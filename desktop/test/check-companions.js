// 付き添いのプロセス (codinable.services.json の processes) の検査
//
// 確かめること
//   1. processes の書き方の誤りを弾く / processes だけのファイルでメッセージングを起こさない
//   2. 実践編の書店 (leaf-books-mvc) から customer-hub を起動でき、8081 で API が答える
//   3. 起こし直すと customer-hub の DB が初期データに戻る
//   4. 別のプロジェクトを実行すると止まる (同じポートを取り合わない)
//
// 2〜4 は同梱の Java と Gradle で実際に起動する (初回は依存の取得に数分かかる)
//
//   node desktop/test/check-companions.js

const assert = require('node:assert/strict');
const fs   = require('fs');
const os   = require('os');
const path = require('path');

const root = fs.mkdtempSync(path.join(os.tmpdir(), 'codinable-companions-'));
// Electron の外で動かすので、ランタイムの解決に要る app だけを差し替える (開発時と同じ扱い)
require.cache[require.resolve('electron')] = {
  exports: { app: { isPackaged: false, getPath: () => root, getAppPath: () => path.resolve(__dirname, '..') },
             shell: {}, dialog: {}, safeStorage: {} },
};

const companions = require('../src/main/companions');
const messaging  = require('../src/main/messaging');

const repo = path.resolve(__dirname, '../..');
const template = path.join(repo, 'courses', 'spring-boot-webapp-practical', 'templates', 'leaf-books-mvc');

const results = [];
async function step(name, fn) {
  try { await fn(); results.push(`PASS ${name}`); }
  catch (err) { results.push(`FAIL ${name}: ${err.message}`); throw err; }
}

const waitFor = async (cond, ms, message) => {
  const until = Date.now() + ms;
  while (Date.now() < until) { if (await cond()) return; await new Promise(r => setTimeout(r, 1000)); }
  throw new Error(message);
};

function writeServices(dir, json) {
  fs.mkdirSync(dir, { recursive: true });
  fs.writeFileSync(path.join(dir, 'codinable.services.json'), JSON.stringify(json));
}

async function main() {
  await step('validation', async () => {
    const bad = path.join(root, 'bad');
    for (const def of [
      { id: 'x', dir: '../outside', run: 'gradle:bootRun', port: 8081 },
      { id: 'x', dir: 'C:/Windows', run: 'gradle:bootRun', port: 8081 },
      { id: 'x', dir: 'sub', run: 'file:evil.sh', port: 8081 },
      { id: 'x', dir: 'sub', run: 'gradle:bootRun & calc', port: 8081 },
      { id: 'X Y', dir: 'sub', run: 'gradle:bootRun', port: 8081 },
      { id: 'x', dir: 'sub', run: 'gradle:bootRun', port: 80 },
    ]) {
      writeServices(bad, { processes: [def] });
      assert.throws(() => companions.requirements(bad), undefined, JSON.stringify(def));
    }
    writeServices(bad, { processes: [{ id: 'hub', dir: 'sub', run: 'gradle:bootRun', port: 8081, when: ['gradle:bootRun'] }] });
    const [def] = companions.requirements(bad);
    assert.equal(def.id, 'hub');
    assert.ok(companions.appliesTo(def, 'gradle:bootRun'));
    assert.ok(!companions.appliesTo(def, 'gradle:test'));
    // processes だけのファイルは Kafka / RabbitMQ を要求しない
    assert.deepEqual(messaging.requirements(bad), []);
  });

  if (process.env.SKIP_GRADLE) return;

  const project = path.join(root, 'leaf-books-mvc');
  fs.cpSync(template, project, {
    recursive: true,
    filter: src => !/[\\/](build|\.gradle)([\\/]|$)/.test(src.slice(template.length)),
  });
  const manager = companions.getManager();
  let log = '';
  manager.on('log', ({ text }) => { log += text; });
  const state = () => manager.status().find(s => s.id === 'customer-hub')?.state;
  const defs = companions.requirements(project);

  await step('customer-hub starts with the bookstore', async () => {
    await manager.restartFor(project, defs, 'ja');
    await waitFor(() => state() === 'running' || state() === 'error', 600_000, 'customer-hub が 8081 で待ち受けない');
    assert.equal(state(), 'running', log.slice(-2000));
    const res = await fetch('http://127.0.0.1:8081/customers/1');
    assert.equal(res.status, 200);
    assert.match(await res.text(), /alice@gmail\.com/);
  });

  await step('restart resets the customer-hub data', async () => {
    const query = 'http://127.0.0.1:8081/customers/query_email?email=zz@example.com';
    const created = await fetch('http://127.0.0.1:8081/customers/', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ customerName: 'Zed', password: 'password', email: 'zz@example.com',
                             birthday: '2000-01-01', address: 'Tokyo' }),
    });
    assert.equal(created.status, 200, await created.text());
    assert.equal((await fetch(query)).status, 200, '登録した顧客が引けない');
    await manager.restartFor(project, defs, 'ja');
    await waitFor(() => state() === 'running' || state() === 'error', 300_000, '起こし直した customer-hub が待ち受けない');
    assert.equal(state(), 'running', log.slice(-2000));
    assert.equal((await fetch(query)).status, 404, '起こし直しても登録した顧客が残っている');
  });

  await step('running another project stops it', async () => {
    await manager.stopOthers(path.join(root, 'other'));
    assert.equal(state(), 'stopped');
    assert.equal(await messaging.portOpen(8081), false, '止めたあとも 8081 が開いている');
  });
}

main()
  .catch(() => {})
  .finally(async () => {
    await companions.getManager().stopAll().catch(() => {});
    for (const line of results) console.log(line);
    try { fs.rmSync(root, { recursive: true, force: true }); } catch { /* Gradle が掴んでいることがある */ }
    process.exit(results.some(r => r.startsWith('FAIL')) ? 1 : 0);
  });
