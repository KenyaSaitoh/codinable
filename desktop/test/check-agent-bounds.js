// Agent モードの境界を、コードとして守れているか確かめる。
//
// 「プロンプトにそう書いた」では守られたことにならない。演習の外へ出る経路を
// ひとつずつ渡して、道具の側で断られることを確認する。
//
//   node test/check-agent-bounds.js

const fs   = require('fs');
const os   = require('os');
const path = require('path');

const agent = require('../src/main/agent');

let ng = 0;

function ok(label, condition, detail = '') {
  console.log(`${condition ? 'OK ' : 'NG '} ${label}${detail ? ` — ${detail}` : ''}`);
  if (!condition) ng++;
}

// ── 準備: 演習ひとつ分と、その外に置くファイル ──
const root     = fs.mkdtempSync(path.join(os.tmpdir(), 'codinable-bounds-'));
const exercise = path.join(root, 'exercise');
const outside  = path.join(root, 'outside');
fs.mkdirSync(path.join(exercise, 'src'), { recursive: true });
fs.mkdirSync(path.join(exercise, 'node_modules'), { recursive: true });
fs.mkdirSync(outside, { recursive: true });
fs.writeFileSync(path.join(exercise, 'src', 'app.js'), 'console.log(1);\n', 'utf8');
fs.writeFileSync(path.join(outside, 'secret.txt'), 'himitsu\n', 'utf8');

// ── 道具の一覧 ──
const toolNames = agent.TOOLS.map(t => t.name).sort();
ok('道具は一覧・読む・書くの 3 つだけ',
   toolNames.join(',') === 'list_files,read_file,write_file', toolNames.join(','));
ok('実行の道具は持たない', !toolNames.some(n => /run|exec|shell|command|sql/.test(n)));

// ── 演習の中は通る ──
try {
  const full = agent.resolveInExercise(exercise, 'src/app.js');
  ok('演習の中の相対パスは通る', full === path.join(exercise, 'src', 'app.js'), full);
} catch (err) {
  ok('演習の中の相対パスは通る', false, err.message);
}
try {
  const full = agent.resolveInExercise(exercise, 'src/new/deep.txt');
  ok('まだ無いファイルも (中なら) 通る', full.startsWith(exercise + path.sep), full);
} catch (err) {
  ok('まだ無いファイルも (中なら) 通る', false, err.message);
}

// ── 外へ出る経路は全部断る ──
const escapes = [
  ['親をさかのぼる',           '../outside/secret.txt'],
  ['途中で親へ戻る',           'src/../../outside/secret.txt'],
  ['区切りが円記号',           '..\\outside\\secret.txt'],
  ['ドライブ文字の絶対パス',   'C:/Windows/System32/drivers/etc/hosts'],
  ['ルートからの絶対パス',     '/etc/passwd'],
  ['UNC パス',                 '//server/share/x.txt'],
  ['ワークスペースの別演習',   '../exercise2/app.js'],
  ['空のパス',                 '   '],
  ['生成物 (node_modules)',    'node_modules/pkg/index.js'],
  ['生成物 (build)',           'build/classes/Main.class'],
  ['隠しディレクトリ (.git)',  '.git/config'],
];

for (const [label, relPath] of escapes) {
  let refused = false;
  let message = '';
  try { agent.resolveInExercise(exercise, relPath); }
  catch (err) { refused = err instanceof agent.OutsideExerciseError; message = err.message; }
  ok(`断る: ${label}`, refused, refused ? message : '通ってしまった');
}

// ── シンボリックリンクで外を指していても断る ──
// (Windows では管理者権限が無いと作れないことがあるので、作れたときだけ見る)
let linked = false;
try {
  fs.symlinkSync(outside, path.join(exercise, 'link'), 'junction');
  linked = true;
} catch { /* 作れない環境ではこの検査は飛ばす */ }

if (linked) {
  let refused = false;
  try { agent.resolveInExercise(exercise, 'link/secret.txt'); }
  catch (err) { refused = err instanceof agent.OutsideExerciseError; }
  ok('断る: シンボリックリンク越しの外', refused);
} else {
  console.log('-- シンボリックリンクの検査は飛ばした (この環境では作れない)');
}

// ── システムプロンプトが「実行しない」と言っているか ──
const prompt = agent.buildAgentSystemPrompt({
  uiLang: 'ja', project: 'exercise', kinds: ['node'], runTargets: ['npm:dev'],
});
ok('プロンプトも実行できないと伝えている', /動かすことはできません/.test(prompt));
ok('プロンプトも演習の外は触れないと伝えている', /外を読むこと・書くことはできません/.test(prompt));

fs.rmSync(root, { recursive: true, force: true });

console.log(ng ? `\n${ng} 件が期待どおりではありません` : '\nすべて期待どおり');
process.exit(ng ? 1 : 0);
