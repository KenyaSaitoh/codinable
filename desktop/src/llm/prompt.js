// チャットのシステムプロンプト。
//
// Codinable は演習問題も模範解答も持たないので、「採点する先生」ではなく
// 「今開いているコードの相談相手」として振る舞わせる。
// 講座の内容そのものは Udemy の動画側にあるため、ここでは扱わない。

const LANG_NAME = { ja: '日本語', en: 'English' };

// 書き換え依頼のときに足す指示。
//
// 形式はファイル全体の差し替えに絞る。部分差分 (行番号つきの unified diff や
// 検索置換) はモデルが少しずれるだけで適用できなくなるが、教材のファイルは
// どれも短いので、全文を返してもらって差分は Codinable 側で計算するのが確実。
const EDIT_BLOCK_MARKER = 'codinable-edit';

const EDIT_INSTRUCTIONS = [
  '',
  '## コードを書き換えるとき',
  '',
  'ファイルを変更する提案は、**必ず**次の形式のコードブロックで返してください。',
  'この形式で返されたものだけを、Codinable が「適用」ボタン付きの差分として見せます。',
  '',
  '````',
  '```' + EDIT_BLOCK_MARKER + ' path=src/main/java/Example.java',
  '(変更後のファイル全体)',
  '```',
  '````',
  '',
  '- `path` は参考情報に出ているプロジェクト内の相対パスをそのまま使ってください。',
  '- 抜粋や「…省略…」は書かず、**そのファイルの全文**を書いてください。',
  '  途中を省いたものを適用すると、そのファイルは壊れます。',
  '- 変更するファイルごとに 1 ブロックにしてください。',
  '- 変更が要らないファイルのブロックは作らないでください。',
  '- ブロックの前に、何をなぜ変えるのかを数行で書いてください。',
  '- 参考情報に中身が無いファイルは書き換えられません。中身を添付するよう促してください。',
].join('\n');

/**
 * @param {object} context
 * @param {string} [context.uiLang]     'ja' | 'en'
 * @param {boolean} [context.editMode]  コード書き換えモード
 * @param {string} [context.courseName] 受講中の講座名 (任意)
 * @param {string} [context.project]    プロジェクト名
 * @param {string[]} [context.kinds]    プロジェクト種別 ('spring' / 'node' など)
 * @param {Array}  [context.files]      [{ path, content }] 添付されたファイル
 * @param {string} [context.log]        直近の実行ログ
 */
function buildSystemPrompt(context = {}) {
  const lang = LANG_NAME[context.uiLang] || LANG_NAME.ja;

  const lines = [
    'あなたは Codinable というデスクトップ開発環境に組み込まれた、プログラミング学習者の相談相手です。',
    `回答は${lang}で書いてください。`,
    '',
    '## ふるまい',
    '- 相手は学習者です。結論を先に述べ、そのあとに理由を短く補ってください。',
    '- コードを示すときは言語を明記したコードブロックを使い、そのまま動くものを書いてください。',
    '- エラーメッセージを見せられたら、まず原因を 1 行で言い当ててから直し方を示してください。',
    '- 分からないことは推測で埋めず、確認したい点を質問してください。',
    '- 環境構築の手順を案内するときは、この環境に Java / Node.js / Python / bash / HSQLDB が',
    '  同梱されていること、Gradle は Wrapper で自動取得されることを前提にしてください。',
  ];

  if (context.courseName) {
    lines.push('', '## 受講中の講座', `- ${context.courseName}`);
  }

  if (context.project) {
    lines.push('', '## 開いているプロジェクト', `- 名前: ${context.project}`);
    if (Array.isArray(context.kinds) && context.kinds.length) {
      lines.push(`- 種別: ${context.kinds.join(', ')}`);
    }
  }

  if (context.editMode) lines.push(EDIT_INSTRUCTIONS);

  return lines.join('\n');
}

/** 添付ファイルと実行ログを、ユーザー発言の前に置く参考情報にまとめる */
function buildContextMessage(context = {}) {
  const blocks = [];

  for (const file of (context.files || []).slice(0, 12)) {
    const content = String(file.content ?? '');
    // 1 ファイル 12000 文字で打ち切る (長大なファイルで文脈を食いつぶさないため)
    const body = content.length > 12000
      ? `${content.slice(0, 12000)}\n… (以下省略)`
      : content;
    blocks.push(`### ${file.path}\n\`\`\`\n${body}\n\`\`\``);
  }

  if (context.log) {
    const log = String(context.log);
    // ログは末尾のほうが重要なので後ろから取る
    const tail = log.length > 6000 ? `… (前略)\n${log.slice(-6000)}` : log;
    blocks.push(`### 実行ログ\n\`\`\`\n${tail}\n\`\`\``);
  }

  if (!blocks.length) return null;
  return `参考情報 (現在の作業内容です。質問への回答に必要な範囲で使ってください):\n\n${blocks.join('\n\n')}`;
}

module.exports = { buildSystemPrompt, buildContextMessage };
