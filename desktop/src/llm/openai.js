// OpenAI Responses API (SSE ストリーミング)。
// Codinable では GPT Luna を選んだときに使う。
//
// GPT-5.6 世代は Responses API (/v1/responses) が正路で、
// 推論つきモデルの扱いも Chat Completions より素直なためこちらを使う。

const { consumeSse, readErrorDetail, toFriendlyHttpError } = require('./sse');

const API_URL = 'https://api.openai.com/v1/responses';
const LABEL   = 'OpenAI';

/** { role, content } → Responses API の input */
function toInput(messages) {
  return (messages || [])
    .map(m => ({
      role: m.role === 'assistant' ? 'assistant' : 'user',
      content: [{
        type: m.role === 'assistant' ? 'output_text' : 'input_text',
        text: String(m.content ?? ''),
      }],
    }))
    .filter(m => m.content[0].text.trim() !== '');
}

async function streamChat({ apiKey, model, messages, system, signal, onText, maxTokens = 4096 }) {
  const body = {
    model,
    stream: true,
    input: toInput(messages),
    max_output_tokens: maxTokens,
    // コーディングの相談は短い往復が中心なので、待ち時間と課金を抑える
    reasoning: { effort: 'low' },
  };
  if (system) body.instructions = system;

  const response = await fetch(API_URL, {
    method: 'POST',
    headers: {
      'content-type':  'application/json',
      'authorization': `Bearer ${apiKey}`,
    },
    body: JSON.stringify(body),
    signal,
  });

  if (!response.ok) {
    throw toFriendlyHttpError(response.status, await readErrorDetail(response), LABEL);
  }

  await consumeSse(response, event => {
    switch (event.type) {
      case 'response.output_text.delta':
        if (typeof event.delta === 'string') onText(event.delta);
        break;
      case 'response.refusal.delta':
        if (typeof event.delta === 'string') onText(event.delta);
        break;
      case 'error':
      case 'response.failed':
        throw new Error(event.error?.message || event.response?.error?.message ||
                        'ストリーミング中にエラーが発生しました');
      default:
        // response.created / .in_progress / .completed などは読み捨てる
        break;
    }
  });
}

module.exports = { streamChat, LABEL };
