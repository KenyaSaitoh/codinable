// 画面を実際に触って確かめるための CDP ヘルパー。検証後に tmp-verify ごと消す。
const http = require('http');

const PORT = process.env.CDP_PORT || 9444;

function list() {
  return new Promise((resolve, reject) => {
    http.get({ host: '127.0.0.1', port: PORT, path: '/json/list', timeout: 5000 }, res => {
      let d = ''; res.on('data', c => d += c); res.on('end', () => resolve(JSON.parse(d)));
    }).on('error', reject);
  });
}

async function connect() {
  const target = (await list()).find(t => t.type === 'page' && t.title === 'Codinable');
  if (!target) throw new Error('Codinable のウィンドウが見つからない');
  // Node 22 の組み込み WebSocket を使う (追加依存を入れない)
  const ws = new WebSocket(target.webSocketDebuggerUrl);
  await new Promise((res, rej) => {
    ws.addEventListener('open', res, { once: true });
    ws.addEventListener('error', rej, { once: true });
  });
  let id = 0;
  const pending = new Map();
  ws.addEventListener('message', ev => {
    const msg = JSON.parse(ev.data);
    if (msg.id && pending.has(msg.id)) { pending.get(msg.id)(msg); pending.delete(msg.id); }
  });
  const send = (method, params = {}) => new Promise(res => {
    const mid = ++id;
    pending.set(mid, res);
    ws.send(JSON.stringify({ id: mid, method, params }));
  });
  const evaluate = async expression => {
    const r = await send('Runtime.evaluate', { expression, awaitPromise: true, returnByValue: true });
    if (r.result?.exceptionDetails) throw new Error(JSON.stringify(r.result.exceptionDetails.exception));
    return r.result?.result?.value;
  };
  return { send, evaluate, close: () => ws.close() };
}

module.exports = { connect };
