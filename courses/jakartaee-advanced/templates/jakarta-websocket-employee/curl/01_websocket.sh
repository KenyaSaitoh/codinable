#!/usr/bin/env bash
# WebSocket のエンドポイントへ 2 本の接続を張り、配信と入力エラーの戻り先を確かめる
# （curl は WebSocket を話せないので、同梱の Node.js の WebSocket で接続する）
# 前提: 先に ▶ アプリ実行で jakarta-websocket-employee をデプロイしておく
# 期待する結果:
#   [A] と [B] の両方に、A が送った Alice の通知（occurredAt はサーバーの時刻）が届く
#   社員名が空の通知と、JSON として読めないメッセージには、A だけに {"error":"..."} が返る（B には届かない）
#   Origin が http://evil.example の接続は拒否される（OriginCheckConfigurator が false を返す）
set -x
node - <<'JS'
const url = 'ws://localhost:8080/jakarta-websocket-employee/ws/employees';
const origin = 'http://localhost:8080';
const open = (name, o) => new Promise((resolve, reject) => {
  const ws = new WebSocket(url, { headers: { Origin: o } });
  ws.addEventListener('message', (e) => console.log(`[${name}] 受信 ${e.data}`));
  ws.addEventListener('open', () => resolve(ws));
  ws.addEventListener('error', () => reject(new Error(`[${name}] 接続できません`)));
});
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
(async () => {
  const a = await open('A', origin);
  const b = await open('B', origin);
  console.log('A: 正しい通知を送る');
  a.send(JSON.stringify({ employeeId: 10001, employeeName: 'Alice', message: 'SALES から PLANNING へ異動します' }));
  await sleep(500);
  console.log('A: 社員名が空の通知を送る（Bean Validation の違反）');
  a.send(JSON.stringify({ employeeId: 10001, employeeName: '', message: 'test' }));
  await sleep(500);
  console.log('A: JSON として読めないメッセージを送る（DecodeException）');
  a.send('{broken');
  await sleep(500);
  a.close(); b.close();
  console.log('Origin が http://evil.example の接続を試す');
  try { (await open('C', 'http://evil.example')).close(); console.log('[C] 接続できてしまいました'); }
  catch (e) { console.log(`[C] 拒否されました`); }
})().catch((e) => { console.error(e.message); process.exit(1); });
JS
