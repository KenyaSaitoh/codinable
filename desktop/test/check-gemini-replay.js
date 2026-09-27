// Gemini の道具呼び出しで、thoughtSignature を送り返しているかを確かめる
//
// Gemini は functionCall の部分に thoughtSignature を付けて返し、次の呼び出しで
// それをそのまま送り返すことを求める (無いと 400: "Function call is missing a
// thought_signature")。本物の API は呼ばず、fetch を差し替えて送った中身を見る
//
//   node test/check-gemini-replay.js

const google = require('../src/llm/google');

let ng = 0;
function ok(label, condition, detail = '') {
  console.log(`${condition ? 'OK ' : 'NG '} ${label}${detail ? ` — ${detail}` : ''}`);
  if (!condition) ng++;
}

const SIGNATURE = 'c2lnbmF0dXJlLWZvci10ZXN0';
const sent = [];
global.fetch = async (_url, init) => {
  const body = JSON.parse(init.body);
  sent.push(body);
  const parts = sent.length === 1
    ? [{ text: 'index.html を読みます。' },
       { functionCall: { name: 'read_file', args: { path: 'index.html' } }, thoughtSignature: SIGNATURE }]
    : [{ text: '読みました。' }];
  return { ok: true, json: async () => ({ candidates: [{ content: { role: 'model', parts } }] }) };
};

(async () => {
  const tools = [{ name: 'read_file', description: 'read', schema: { type: 'object', properties: { path: { type: 'string' } } } }];
  const history = [{ role: 'user', content: '月給を時給に変えて' }];

  // 1 手目: 道具の呼び出しが返る
  const first = await google.callWithTools({ apiKey: 'k', model: 'm', messages: history, tools });
  ok('道具の呼び出しを中立な形で返す', first.toolCalls.length === 1 && first.toolCalls[0].name === 'read_file');
  ok('送り返すための parts を持っている', first.replay?.provider === 'google' && first.replay.parts.length === 2);

  // Agent のループと同じ形で履歴に積む
  history.push({ role: 'assistant', content: first.text, toolCalls: first.toolCalls, replay: first.replay });
  history.push({ role: 'tool', results: [{ id: first.toolCalls[0].id, name: 'read_file', output: '<html></html>' }] });

  // 2 手目: 受け取った parts が thoughtSignature ごと送られていること
  await google.callWithTools({ apiKey: 'k', model: 'm', messages: history, tools });
  const model = sent[1].contents.find(c => c.role === 'model');
  const call  = model?.parts.find(p => p.functionCall);
  ok('2 手目に thoughtSignature を送り返している', call?.thoughtSignature === SIGNATURE,
     JSON.stringify(model?.parts));
  ok('道具の結果を functionResponse で送っている',
     sent[1].contents.some(c => c.parts?.some(p => p.functionResponse?.name === 'read_file')));

  console.log(ng ? `\n${ng} 件が期待どおりではありません` : '\nすべて期待どおり');
  process.exit(ng ? 1 : 0);
})().catch(err => {
  console.error('検査そのものが失敗:', err);
  process.exit(1);
});
