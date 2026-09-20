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

// ── 道具つきの呼び出し (Agent モード) ──────────────────────
//
// ストリーミングは使わない。道具呼び出しは引数が細切れで届くため組み立てが
// 面倒な割に、1 ステップの待ちは短いので得るものが少ない。

/** 中立な履歴 → Anthropic の messages */
function toToolMessages(messages) {
  const out = [];
  for (const m of messages || []) {
    if (m.role === 'tool') {
      out.push({
        role: 'user',
        content: (m.results || []).map(r => ({
          type: 'tool_result', tool_use_id: r.id, content: String(r.output ?? ''),
        })),
      });
      continue;
    }
    if (m.role === 'assistant' && m.toolCalls?.length) {
      const content = [];
      if (String(m.content ?? '').trim()) content.push({ type: 'text', text: m.content });
      for (const c of m.toolCalls) {
        content.push({ type: 'tool_use', id: c.id, name: c.name, input: c.input || {} });
      }
      out.push({ role: 'assistant', content });
      continue;
    }
    const text = String(m.content ?? '');
    if (text.trim() === '') continue;
    out.push({ role: m.role === 'assistant' ? 'assistant' : 'user', content: text });
  }
  return out;
}

async function callWithTools({ apiKey, model, messages, system, tools, signal, maxTokens = 8192 }) {
  const body = {
    model,
    max_tokens: maxTokens,
    messages: toToolMessages(messages),
    tools: (tools || []).map(t => ({
      name: t.name, description: t.description, input_schema: t.schema,
    })),
  };
  if (system) body.system = system;

  const response = await fetch(API_URL, {
    method: 'POST',
    headers: {
      'content-type':      'application/json',
      'x-api-key':         apiKey,
      'anthropic-version': VERSION,
      'anthropic-dangerous-direct-browser-access': 'true',
    },
    body: JSON.stringify(body),
    signal,
  });

  if (!response.ok) {
    throw toFriendlyHttpError(response.status, await readErrorDetail(response), LABEL);
  }

  const data = await response.json();
  const text = (data.content || [])
    .filter(b => b.type === 'text').map(b => b.text).join('');
  const toolCalls = (data.content || [])
    .filter(b => b.type === 'tool_use')
    .map(b => ({ id: b.id, name: b.name, input: b.input || {} }));

  return { text, toolCalls };
}

module.exports = { streamChat, callWithTools, LABEL };
