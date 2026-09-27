// ═══════════════════════════════════════════════════════════
//  Codinable のアプリ定義
//
//  Codinable は「Udemy 講座の受講中にそのまま使える開発環境」である
//  演習問題・解説・音声読み上げは持たず、次の 3 つだけを提供する
//
//    ① コードを書く      … ファイルツリー + エディタ (LSP 付き) + ターミナル
//    ② 動かして確かめる  … Java / Spring Boot / JUnit / Python / Node.js /
//                           静的 Web ページ / SQL の実行と Web プレビュー
//    ③ 困ったら聞く      … LLM チャット (BYOK。使わなくてもよい)
//
//  講座との連動は courses/ 配下の「コースパック」で行う (src/main/courses.js)
//  コースパックは講座ごとのサンプルプロジェクト集にすぎないため、
//  別の Udemy 講座を足すときも courses/<id>/ を 1 つ増やすだけでよい
// ═══════════════════════════════════════════════════════════

const PRODUCT = {
  id:               'codinable',
  // exe 名・成果物名に使われるため ASCII
  productName:      'Codinable',
  displayName:      'Codinable',
  appId:            'pro.kensait.codinable',
  // app.setName() に渡す名前 (= userData ディレクトリ名)
  dataDirName:      'Codinable',
  // ワークスペース (受講者の作業ファイル) の既定ディレクトリ名。~/ 直下に作る
  workspaceDirName: 'codinable',
  // 選択できる UI 言語。renderer/i18n.js の UI_STRINGS と同じ並びにしておくこと
  languages:        ['ja', 'en'],
  // 同梱を前提とするランタイム (scripts/setup-runtimes.ps1 / setup-jdtls.ps1 が用意する)
  runtimes:         ['java', 'node', 'python', 'bash', 'hsqldb', 'gradle-wrapper', 'jdtls',
                     'kafka', 'rabbitmq', 'erlang'],
};

// ── 更新の配信先 ───────────────────────────────────────────
//
// アプリ本体と講座は別々に更新する。配信先はこのリポジトリ (公開) の
// GitHub Releases で、次のように置く (npm run release:app / release:courses)
//
//   アプリ本体: タグ v<version> のリリース (「最新」にする)
//     latest.yml / Codinable-setup-<version>.exe (+ .blockmap)
//     → https://github.com/<owner>/<repo>/releases/latest/download/latest.yml
//   講座: タグ courses のリリース 1 つに上書きで置く (「最新」にはしない)
//     index.json / <id>-<version>.codpack
//     → https://github.com/<owner>/<repo>/releases/download/courses/index.json
//
// github を空にすると更新を確認しない (すべて同梱の版のまま動く)
// 環境変数 CODINABLE_UPDATE_URL を与えると、<url>/app と <url>/courses を
// 見るように差し替わる (手元に配信先を立てて確かめるとき)
const UPDATES = {
  github:     { owner: 'KenyaSaitoh', repo: 'codinable' },
  coursesTag: 'courses',
};

/** アプリ本体 (electron-updater の generic フィード) と講座の配信先。未設定なら '' */
function getUpdateUrls() {
  const override = String(process.env.CODINABLE_UPDATE_URL || '').trim().replace(/\/+$/, '');
  if (override) return { app: `${override}/app`, courses: `${override}/courses` };
  const { owner, repo } = UPDATES.github || {};
  if (!owner || !repo) return { app: '', courses: '' };
  const releases = `https://github.com/${owner}/${repo}/releases`;
  return {
    app:     `${releases}/latest/download`,
    courses: `${releases}/download/${UPDATES.coursesTag}`,
  };
}

// ── LLM (BYOK) ─────────────────────────────────────────────
//
// チャットは「あってもよい補助」であり、Codinable の必須機能ではない
// キーが未設定ならチャット欄だけが使えないだけで、開発環境としては完全に動く
//
// キーはユーザー自身が用意する (BYOK)。3 社のうち使いたいものだけ登録すればよい
// モデルは各社の「最新を指すエイリアス」を既定にしてあるので、
// 新しい版が出ても Codinable 側の更新は要らない
// 固定版を使いたい場合は設定パネルからモデル ID を上書きできる
const LLM_MODELS = [
  {
    id:       'claude-haiku',
    provider: 'anthropic',
    label:    'Claude Haiku',
    // claude-haiku-4-5 は最新 Haiku スナップショットを指すエイリアス
    model:    'claude-haiku-4-5',
    keyField: 'anthropicApiKey',
    keyLabel: 'Anthropic API Key',
    keyUrl:   'https://console.anthropic.com/settings/keys',
  },
  {
    id:       'gpt-luna',
    provider: 'openai',
    label:    'GPT Luna',
    model:    'gpt-5.6-luna',
    keyField: 'openaiApiKey',
    keyLabel: 'OpenAI API Key',
    keyUrl:   'https://platform.openai.com/api-keys',
  },
  {
    id:       'gemini-flash',
    provider: 'google',
    label:    'Gemini Flash',
    // gemini-flash-latest はその時点の最新 Flash を指すエイリアス
    model:    'gemini-flash-latest',
    keyField: 'geminiApiKey',
    keyLabel: 'Google AI Studio API Key',
    keyUrl:   'https://aistudio.google.com/apikey',
  },
];

const DEFAULT_LLM_ID = 'claude-haiku';

/** LLM 定義を id で引く (未知の id は既定モデル) */
function getLlmModel(id) {
  return LLM_MODELS.find(m => m.id === id) ||
         LLM_MODELS.find(m => m.id === DEFAULT_LLM_ID);
}

/** API キーを保存する設定フィールド名の一覧 (暗号化対象) */
const API_KEY_FIELDS = LLM_MODELS.map(m => m.keyField);

module.exports = { PRODUCT, UPDATES, getUpdateUrls, LLM_MODELS, DEFAULT_LLM_ID, getLlmModel, API_KEY_FIELDS };
