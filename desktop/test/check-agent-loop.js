// Agent のループを、擬似プロバイダで一巡させて確かめる。
//
// 本物の API は呼ばない (キーも通信も要らない)。llm.callWithTools だけ
// 差し替えて、「道具を呼ぶ → こちらで実行する → 結果を返す」の流れと、
// 演習の外への書き込みが断られることを見る。
//
//   node test/check-agent-loop.js

const fs   = require('fs');
const os   = require('os');
const path = require('path');

const llm       = require('../src/llm');
const workspace = require('../src/main/workspace');
const agent     = require('../src/main/agent');

let ng = 0;
function ok(label, condition, detail = '') {
  console.log(`${condition ? 'OK ' : 'NG '} ${label}${detail ? ` — ${detail}` : ''}`);
  if (!condition) ng++;
}

// ── 準備 ──
const root     = fs.mkdtempSync(path.join(os.tmpdir(), 'codinable-loop-'));
const exercise = path.join(root, 'my-exercise');
fs.mkdirSync(exercise, { recursive: true });
fs.writeFileSync(path.join(exercise, 'app.js'), 'console.log("before");\n', 'utf8');

// ワークスペースは使わないので、演習の場所だけ教える
workspace.resolveProjectDir = name => (name === 'my-exercise' ? exercise : null);

// ── 擬似プロバイダ: 3 手で終わる ──
const asked = [];
let turn = 0;
llm.callWithTools = async ({ messages, tools, system }) => {
  asked.push({ messages: [...messages], tools, system });
  turn++;
  if (turn === 1) {
    return {
      text: 'app.js を読みます。',
      toolCalls: [{ id: 'c1', name: 'read_file', input: { path: 'app.js' } }],
    };
  }
  if (turn === 2) {
    return {
      text: '直します。ついでに外にも書いてみます。',
      toolCalls: [
        { id: 'c2', name: 'write_file', input: { path: 'app.js', content: 'console.log("after");\n' } },
        { id: 'c3', name: 'write_file', input: { path: '../escaped.txt', content: 'x' } },
        { id: 'c4', name: 'run',        input: { target: 'file:app.js' } },
      ],
    };
  }
  return { text: '直しました。「実行」を押して確かめてください。', toolCalls: [] };
};

// ── 経過イベントを集める ──
const events = [];
const event = { sender: { isDestroyed: () => false, send: (ch, payload) => events.push({ ch, payload }) } };

(async () => {
  const result = await agent.runAgent({
    event,
    messages: [{ role: 'user', content: 'app.js の出力を after にして' }],
    context:  { project: 'my-exercise', kinds: ['node'], files: [] },
    selection: { modelId: 'claude-haiku', override: '' },
    apiKeys:  {},
    uiLang:   'ja',
  });

  // 1. 道具が実際に効いたか
  ok('演習の中のファイルは書き換わった',
     fs.readFileSync(path.join(exercise, 'app.js'), 'utf8') === 'console.log("after");\n');
  ok('演習の外には書かれていない', !fs.existsSync(path.join(root, 'escaped.txt')));

  // 2. 画面へ流した経過
  const chans = events.map(e => e.ch);
  ok('本文を流した', chans.includes('agent-text'));
  ok('道具の開始と終了を流した',
     events.filter(e => e.ch === 'agent-tool' && e.payload.state === 'start').length === 4 &&
     events.filter(e => e.ch === 'agent-tool' && e.payload.state === 'end').length === 4);

  const edits = events.filter(e => e.ch === 'agent-edit').map(e => e.payload);
  ok('書き換えは 1 件だけ差分として出た', edits.length === 1, JSON.stringify(edits.map(e => e.path)));
  ok('差分に変更前と変更後が入っている',
     edits[0]?.before === 'console.log("before");\n' && edits[0]?.after === 'console.log("after");\n');

  // 3. 断り方 (モデルには理由が返り、ループは続く)
  const outputs = events.filter(e => e.ch === 'agent-tool' && e.payload.state === 'end')
                        .map(e => String(e.payload.output));
  ok('外への書き込みは理由つきで断った',
     outputs.some(o => o.startsWith('演習の外には出られません')), outputs.join(' / '));
  ok('実行の依頼は道具が無いので断った',
     outputs.some(o => o.startsWith('使えない道具です: run')), outputs.join(' / '));
  ok('断られてもループは続いた (3 手で終わった)', turn === 3, `turn=${turn}`);
  ok('最後は本文で終わった', !events.some(e => e.ch === 'agent-limit'));

  // 4. モデルへ渡した形
  const last = asked.at(-1).messages;
  ok('道具の結果を履歴に積んでいる', last.some(m => m.role === 'tool' && m.results?.length));
  ok('道具は 3 つだけ渡している', asked[0].tools.length === 3);
  ok('プロジェクト名をプロンプトに入れている', /my-exercise/.test(asked[0].system));
  ok('履歴を返している', Array.isArray(result.history) && result.history.length > 1);

  fs.rmSync(root, { recursive: true, force: true });
  console.log(ng ? `\n${ng} 件が期待どおりではありません` : '\nすべて期待どおり');
  process.exit(ng ? 1 : 0);
})().catch(err => {
  console.error('検査そのものが失敗:', err);
  process.exit(1);
});
