// Anthropic Messages API (SSE ストリーミング)。
// Codinable では Claude Haiku を選んだときに使う。

const { consumeSse, readErrorDetail, toFriendlyHttpError } = require('./sse');

const API_URL = 'https://api.anthropic.com/v1/messages';
const VERSION = '2023-06-01';
const LABEL   = 'Anthropic';

/** { role: 'user' | 'assistant', content } はそのまま Anthropic の形式 */
function toMessages(messages) {
  return (messages || [])
    .map(m => ({
      role:    m.role === 'assistant' ? 'assistant' : 'user',
      content: String(m.content ?? ''),
    }))
    .filter(m => m.content.trim() !== '');
}

async function streamChat({ apiKey, model, messages, system, signal, onText, maxTokens = 4096 }) {
  const body = {
    model,
    max_tokens: maxTokens,
    stream: true,
    messages: toMessages(messages),
  };
  if (system) body.system = system;

  const response = await fetch(API_URL, {
    method: 'POST',
    headers: {
      'content-type':      'application/json',
      'x-api-key':         apiKey,
      'anthropic-version': VERSION,
      // Electron の renderer 由来ではないが、fetch が Origin を付けるため
      // ブラウザ扱いされないよう明示的に許可する
      'anthropic-dangerous-direct-browser-access': 'true',
    },
    body: JSON.stringify(body),
    signal,
  });

  if (!response.ok) {
    throw toFriendlyHttpError(response.status, await readErrorDetail(response), LABEL);
  }

  await consumeSse(response, event => {
    if (event.type === 'content_block_delta' && event.delta?.type === 'text_delta') {
      onText(event.delta.text);
    } else if (event.type === 'error') {
      throw new Error(event.error?.message || 'ストリーミング中にエラーが発生しました');
    }
  });
}

module.exports = { streamChat, LABEL };
