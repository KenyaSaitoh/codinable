// ═══════════════════════════════════════════════════════════
//  LLM チャット (BYOK)
//
//  Codinable のチャットは「使いたければ使える補助」であり、必須機能ではない
//  キーが未設定なら、そのことを伝えるだけで他の機能には影響しない
//
//  接続先はユーザーが選んだモデル (app-config.js の LLM_MODELS) で決まる
//  3 社の差はこの配下 (anthropic.js / openai.js / google.js) に閉じている
// ═══════════════════════════════════════════════════════════

const { getLlmModel } = require('../app-config');

const PROVIDERS = {
  anthropic: require('./anthropic'),
  openai:    require('./openai'),
  google:    require('./google'),
};

/**
 * 選択中のモデルでチャットをストリーミングする
 *
 * @param {object} opts
 * @param {string} opts.modelId    LLM_MODELS の id
 * @param {string} [opts.modelOverride] モデル ID の手動上書き (空なら既定値)
 * @param {object} opts.apiKeys    { anthropicApiKey, openaiApiKey, geminiApiKey } (復号済み)
 * @param {Array}  opts.messages   [{ role, content }]
 * @param {string} [opts.system]
 * @param {AbortSignal} opts.signal
 * @param {(text: string) => void} opts.onText
 */
async function streamChat({ modelId, modelOverride, apiKeys, messages, system, signal, onText }) {
  const { provider, apiKey, model } = resolve(modelId, modelOverride, apiKeys);
  await provider.streamChat({ apiKey, model, messages, system, signal, onText });
}

/**
 * 道具つきで 1 回呼ぶ (Agent モードのループ 1 周分)
 *
 * 道具の宣言と結果の返し方の JSON の形は 3 社で違うが、ここから上は
 * 中立な形 ({ text, toolCalls: [{ id, name, input }] }) だけを扱う
 *
 * @param {Array}  opts.messages [{ role, content }] / { role:'assistant', toolCalls }
 *                               / { role:'tool', results: [{ id, name, output }] }
 * @param {Array}  opts.tools    [{ name, description, schema }]
 * @returns {Promise<{ text: string, toolCalls: Array }>}
 */
async function callWithTools({ modelId, modelOverride, apiKeys, messages, system, tools, signal }) {
  const { provider, apiKey, model } = resolve(modelId, modelOverride, apiKeys);
  if (typeof provider.callWithTools !== 'function') {
    throw new Error(`${provider.LABEL} は道具つきの呼び出しに対応していません`);
  }
  return provider.callWithTools({ apiKey, model, messages, system, tools, signal });
}

/** モデル ID からプロバイダとキーを決める (未設定なら分かる形で失敗させる) */
function resolve(modelId, modelOverride, apiKeys) {
  const spec     = getLlmModel(modelId);
  const provider = PROVIDERS[spec.provider];
  if (!provider) throw new Error(`未対応の LLM プロバイダ: ${spec.provider}`);

  const apiKey = String((apiKeys || {})[spec.keyField] || '').trim();
  if (!apiKey) {
    throw Object.assign(
      new Error(`${spec.keyLabel} が未設定です。設定パネルから登録してください。`),
      { code: 'API_KEY_NOT_CONFIGURED', keyField: spec.keyField },
    );
  }
  return { provider, apiKey, model: String(modelOverride || '').trim() || spec.model };
}

/** 選択中モデルの表示用情報 (キー本体は含めない) */
function describeModel(modelId, modelOverride) {
  const spec = getLlmModel(modelId);
  return {
    id:       spec.id,
    label:    spec.label,
    provider: spec.provider,
    model:    String(modelOverride || '').trim() || spec.model,
    keyField: spec.keyField,
    keyLabel: spec.keyLabel,
  };
}

module.exports = { streamChat, callWithTools, describeModel };
