// ═══════════════════════════════════════════════════════════
//  エージェント (Agent モード)
//
//  Ask との違いは「道具を持つか」だけである。Ask はプロジェクトの中身を渡して
//  文章で答えるだけ。Agent は選んでいる演習のファイルを読み書きする。
//
//  ループはプロバイダに依存しない。モデルが道具の呼び出しを返す →
//  ここで実行する → 結果を返す → モデルが続きを考える、の繰り返しである。
//  3 社の違い (道具の宣言と結果の返し方の JSON の形) は llm/ 配下に閉じている。
//
//  ■ できることの境界 (プロンプトではなく、ここのコードで縛る)
//
//  1. 触れるのは「いま開いている演習のプロジェクトディレクトリの中」だけ。
//     パスは相対パスのみ受け付け、.. / ドライブ文字 / UNC / シンボリックリンク
//     による外向きの抜け道をすべて閉じる (resolveInExercise)。
//  2. 生成物のディレクトリ (node_modules / build / .git など) は読み書きしない。
//  3. プロセスの実行はしない。道具は「一覧・読む・書く」の 3 つだけで、
//     実行はこれまでどおり受講者が「実行」ボタンで行う。
//
//  モデルの言うことを信用して境界を守らせるのではなく、道具の側で弾く。
//  プロンプトに書いた約束は破られうるが、ここで弾けば破れない。
//
//  書き込みは即座に適用し、変更前の内容を持っておいて画面から「元に戻す」で
//  戻せるようにする (人の承認を毎回待つとエージェントとして働けないため)。
//  ただし勝手に消えたように見えないよう、変更は必ず差分として画面に出す。
// ═══════════════════════════════════════════════════════════

const fs   = require('fs');
const path = require('path');

const llm       = require('../llm');
const { buildContextMessage } = require('../llm/prompt');
const workspace = require('./workspace');
const { walkTree, isTextFile, safeJoin, SKIP_DIRS } = require('./util');

// 1 回の依頼で回す上限。届いたら打ち切って、そこまでの結果を伝える
const MAX_STEPS = 12;

const MAX_READ_CHARS  = 60_000;
const MAX_WRITE_CHARS = 400_000;

// ── 道具 ───────────────────────────────────────────────────
//
// 引数の形は JSON Schema で書く。3 社が共通して解釈できる範囲
// (type / properties / required / description / items / enum) に収めておく。
//
// 実行の道具は置かない。動かして確かめるのは受講者の仕事であり、
// エージェントにプロセスを起こさせないことで「何が動いているか」が
// 常に受講者の操作と一致する。

const TOOLS = [
  {
    name: 'list_files',
    description: 'いま開いている演習のファイル一覧を返す。生成物 (build / node_modules など) は含まない。',
    schema: { type: 'object', properties: {} },
  },
  {
    name: 'read_file',
    description: 'いま開いている演習の中のファイルを読む。参考情報に無いファイルを見たいときに使う。',
    schema: {
      type: 'object',
      properties: { path: { type: 'string', description: '演習のディレクトリからの相対パス' } },
      required: ['path'],
    },
  },
  {
    name: 'write_file',
    description: 'いま開いている演習の中のファイルを書き換える (新規作成もできる)。内容はファイル全文を渡す。抜粋や省略を渡すとそのファイルは壊れる。演習の外は書き換えられない。',
    schema: {
      type: 'object',
      properties: {
        path:    { type: 'string', description: '演習のディレクトリからの相対パス' },
        content: { type: 'string', description: '変更後のファイル全文' },
      },
      required: ['path', 'content'],
    },
  },
];

// ── 演習の外へ出さないための解決 ────────────────────────────

/** 境界を越えようとしたときに投げる。モデルへはこの文言をそのまま返す */
class OutsideExerciseError extends Error {}

/**
 * 演習のディレクトリの中に収まる実パスを返す。収まらなければ投げる。
 *
 * safeJoin だけでも .. やドライブ文字は弾けるが、シンボリックリンクで
 * 外を指していると通ってしまう。書き込み先の親までさかのぼって実体を見て、
 * 演習の外を指していないことまで確かめる。
 *
 * @param {string} projectDir 演習のプロジェクトディレクトリ (絶対パス)
 * @param {string} relPath    受け取った相対パス
 */
function resolveInExercise(projectDir, relPath) {
  const raw = String(relPath ?? '').trim();
  if (!raw) throw new OutsideExerciseError('パスが空です。演習の中の相対パスを渡してください。');

  const normalized = raw.replace(/\\/g, '/');
  if (/^[a-zA-Z]:/.test(normalized) || normalized.startsWith('/') || normalized.startsWith('//')) {
    throw new OutsideExerciseError(`絶対パスは扱えません。演習の中の相対パスを渡してください: ${raw}`);
  }
  const segments = normalized.split('/').filter(s => s !== '' && s !== '.');
  if (segments.includes('..')) {
    throw new OutsideExerciseError(`演習の外には出られません: ${raw}`);
  }
  if (segments.some(s => SKIP_DIRS.has(s))) {
    throw new OutsideExerciseError(`生成物のディレクトリは触れません: ${raw}`);
  }

  const full = safeJoin(projectDir, segments.join('/'));
  if (!full) throw new OutsideExerciseError(`演習の外には出られません: ${raw}`);

  // シンボリックリンク対策。存在する一番近い親までの実体が演習の中にあるか見る
  const rootReal = realPathOf(projectDir);
  let probe = full;
  while (!fs.existsSync(probe)) {
    const parent = path.dirname(probe);
    if (parent === probe) break;
    probe = parent;
  }
  const probeReal = realPathOf(probe);
  if (probeReal !== rootReal && !probeReal.startsWith(rootReal + path.sep)) {
    throw new OutsideExerciseError(`演習の外を指しているため扱えません: ${raw}`);
  }

  return full;
}

function realPathOf(target) {
  try { return fs.realpathSync.native(target); }
  catch { return path.resolve(target); }
}

// ── 道具の実装 ─────────────────────────────────────────────

function toolListFiles({ projectDir }) {
  const entries = walkTree(projectDir).filter(e => !e.dir).map(e => e.path);
  return entries.length ? entries.join('\n') : 'ファイルがありません。';
}

function toolReadFile({ projectDir, input }) {
  const rel  = String(input.path || '');
  const full = resolveInExercise(projectDir, rel);
  if (!fs.existsSync(full)) return `ファイルがありません: ${rel}`;
  if (!fs.statSync(full).isFile()) return `ファイルではありません: ${rel}`;
  if (!isTextFile(rel))     return `テキストとして読めないファイルです: ${rel}`;

  const content = fs.readFileSync(full, 'utf8');
  return content.length > MAX_READ_CHARS
    ? `${content.slice(0, MAX_READ_CHARS)}\n… (以下省略。${content.length} 文字のうち先頭 ${MAX_READ_CHARS} 文字)`
    : content;
}

/**
 * ファイルを書き換える。
 * 変更前の内容を戻り値の edit に入れて呼び出し側へ渡し、画面で差分と
 * 「元に戻す」を出せるようにする。
 */
function toolWriteFile({ projectDir, input }) {
  const rel  = String(input.path || '');
  const full = resolveInExercise(projectDir, rel);
  if (!isTextFile(rel)) return { output: `テキストファイル以外は書き換えられません: ${rel}` };

  const after = String(input.content ?? '');
  if (after.length > MAX_WRITE_CHARS) {
    return { output: `大きすぎて書き込めません (${after.length} 文字): ${rel}` };
  }

  const existed = fs.existsSync(full);
  if (existed && !fs.statSync(full).isFile()) return { output: `ファイルではありません: ${rel}` };

  const before = existed ? fs.readFileSync(full, 'utf8') : null;
  if (existed && before === after) return { output: `変更はありません: ${rel}` };

  fs.mkdirSync(path.dirname(full), { recursive: true });
  fs.writeFileSync(full, after, 'utf8');

  return {
    output: existed ? `書き換えました: ${rel}` : `作成しました: ${rel}`,
    edit:   { path: rel, before, after, isNew: !existed },
  };
}

// ── システムプロンプト ─────────────────────────────────────

function buildAgentSystemPrompt({ uiLang, project, kinds, runTargets, courseName }) {
  const lang = uiLang === 'en' ? 'English' : '日本語';
  const lines = [
    'あなたは Codinable というデスクトップ開発環境の中で動く、プログラミング学習者の相棒です。',
    `回答は${lang}で書いてください。`,
    '',
    '## できること',
    '- list_files / read_file で、いま開いている演習の中を調べる',
    '- write_file で、いま開いている演習の中のファイルを書き換える (全文を渡す)',
    '',
    '## できないこと',
    '- 演習のディレクトリの外を読むこと・書くことはできません (道具の側で弾かれます)。',
    '- プログラムを動かすことはできません。実行は受講者が「実行」ボタンで行います。',
    '  動かして確かめてほしいときは、どの実行対象を選んで押すかを伝えてください。',
    '',
    '## 進め方',
    '- まず何をするつもりかを 1〜2 行で書いてから道具を使ってください。',
    '- 推測で書き換えないでください。関係するファイルは read_file で読んでから直します。',
    '- 直したら、受講者が何を実行して確かめればよいかを書いてください。',
    '- 相手は学習者です。最後に「何をなぜ変えたか」を短くまとめてください。',
    '- 質問されただけのときは、書き換えずに答えてください。',
    '- 書き換えは受講者の画面に差分として出ます。取り消せるので、必要な変更は遠慮せず行ってください。',
  ];

  if (courseName) lines.push('', '## 受講中の講座', `- ${courseName}`);

  lines.push('', '## 開いている演習', `- プロジェクト名: ${project}`);
  if (kinds?.length)      lines.push(`- 種別: ${kinds.join(', ')}`);
  if (runTargets?.length) lines.push(`- 受講者が実行できる対象: ${runTargets.join(' / ')}`);

  return lines.join('\n');
}

/** 受講者が「実行」で選べる対象。エージェントが案内に使う (自分では動かせない) */
function describeRunTargets(info) {
  const targets = [];
  for (const task of info.gradleTasks || []) targets.push(`gradle:${task}`);
  for (const script of info.npmScripts || []) targets.push(`npm:${script}`);
  for (const file of info.runnableFiles || []) targets.push(file);
  return targets;
}

// ── ループ ─────────────────────────────────────────────────

/**
 * 1 回の依頼を、道具を使いながら終わるまで回す。
 *
 * @param {object} opts
 * @param {object} opts.event      IPC イベント (画面へ経過を送る)
 * @param {Array}  opts.messages   これまでの会話 [{ role, content }]
 * @param {object} opts.context    { project, kinds, files, log, courseName }
 * @param {object} opts.selection  { modelId, override }
 * @param {object} opts.apiKeys
 * @param {string} opts.uiLang
 * @param {AbortSignal} opts.signal
 */
async function runAgent({ event, messages, context, selection, apiKeys, uiLang, signal }) {
  const send = (channel, payload) => {
    if (!event.sender.isDestroyed()) event.sender.send(channel, payload);
  };

  // 対象は「画面で開いている演習」1 つに固定する。道具の引数では変えられない
  const project    = context?.project;
  const projectDir = workspace.resolveProjectDir(project);
  if (!projectDir || !fs.existsSync(projectDir)) {
    throw new Error('プロジェクトが見つかりません。演習を選んでから依頼してください。');
  }

  const info   = workspace.detectProject(projectDir);
  const system = buildAgentSystemPrompt({
    uiLang, project,
    kinds:      info.kinds,
    runTargets: describeRunTargets(info),
    courseName: context?.courseName,
  });

  // プロジェクトの中身は最初から渡しておく。read_file で取りにも行けるが、
  // 教材のファイルは短いので、往復を 1 回減らしたほうが速い
  const history   = [...(messages || [])];
  const reference = buildContextMessage(context || {});
  if (reference) {
    history.splice(Math.max(0, history.length - 1), 0, { role: 'user', content: reference });
  }

  for (let step = 1; step <= MAX_STEPS; step++) {
    if (signal?.aborted) return;

    const reply = await llm.callWithTools({
      modelId:       selection.modelId,
      modelOverride: selection.override,
      apiKeys,
      messages:      history,
      system,
      tools:         TOOLS,
      signal,
    });
    if (signal?.aborted) return;

    if (reply.text) send('agent-text', reply.text);

    if (!reply.toolCalls.length) {
      history.push({ role: 'assistant', content: reply.text });
      return { history };
    }

    history.push({ role: 'assistant', content: reply.text, toolCalls: reply.toolCalls });

    const results = [];
    for (const call of reply.toolCalls) {
      if (signal?.aborted) return;
      send('agent-tool', { name: call.name, input: call.input, state: 'start' });

      let output = '';
      let edit   = null;
      try {
        const res = execTool(call, { projectDir });
        if (typeof res === 'string') output = res;
        else { output = res.output; edit = res.edit || null; }
      } catch (err) {
        // 境界を越えようとした場合もここに来る。理由を返して続けさせる
        output = err instanceof OutsideExerciseError ? err.message : `エラー: ${err.message}`;
      }

      send('agent-tool', { name: call.name, input: call.input, state: 'end', output });
      if (edit) send('agent-edit', edit);
      results.push({ id: call.id, name: call.name, output: String(output).slice(0, 20_000) });
    }

    history.push({ role: 'tool', results });
  }

  send('agent-limit', { steps: MAX_STEPS });
  return { history };
}

function execTool(call, ctx) {
  const input = call.input || {};
  switch (call.name) {
    case 'list_files': return toolListFiles(ctx);
    case 'read_file':  return toolReadFile({ ...ctx, input });
    case 'write_file': return toolWriteFile({ ...ctx, input });
    // 知らない道具は実行しない (増やすときはここと TOOLS の両方に足す)
    default:           return `使えない道具です: ${call.name}`;
  }
}

module.exports = {
  runAgent, TOOLS, MAX_STEPS,
  // テスト用に公開
  resolveInExercise, OutsideExerciseError, describeRunTargets, buildAgentSystemPrompt,
};
