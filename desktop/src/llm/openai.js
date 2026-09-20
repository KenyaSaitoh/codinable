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

// ── 道具つきの呼び出し (Agent モード) ──────────────────────

/** 中立な履歴 → Responses API の input 項目 */
function toToolInput(messages) {
  const out = [];
  for (const m of messages || []) {
    if (m.role === 'tool') {
      for (const r of m.results || []) {
        out.push({ type: 'function_call_output', call_id: r.id, output: String(r.output ?? '') });
      }
      continue;
    }
    if (m.role === 'assistant' && m.toolCalls?.length) {
      if (String(m.content ?? '').trim()) {
        out.push({ role: 'assistant', content: [{ type: 'output_text', text: m.content }] });
      }
      for (const c of m.toolCalls) {
        out.push({
          type: 'function_call', call_id: c.id, name: c.name,
          arguments: JSON.stringify(c.input || {}),
        });
      }
      continue;
    }
    const text = String(m.content ?? '');
    if (text.trim() === '') continue;
    const role = m.role === 'assistant' ? 'assistant' : 'user';
    out.push({
      role,
      content: [{ type: role === 'assistant' ? 'output_text' : 'input_text', text }],
    });
  }
  return out;
}

async function callWithTools({ apiKey, model, messages, system, tools, signal, maxTokens = 8192 }) {
  const body = {
    model,
    input: toToolInput(messages),
    max_output_tokens: maxTokens,
    reasoning: { effort: 'low' },
    tools: (tools || []).map(t => ({
      type: 'function', name: t.name, description: t.description, parameters: t.schema,
    })),
  };
  if (system) body.instructions = system;

  const response = await fetch(API_URL, {
    method: 'POST',
    headers: { 'content-type': 'application/json', 'authorization': `Bearer ${apiKey}` },
    body: JSON.stringify(body),
    signal,
  });

  if (!response.ok) {
    throw toFriendlyHttpError(response.status, await readErrorDetail(response), LABEL);
  }

  const data  = await response.json();
  const items = data.output || [];

  const text = items
    .filter(i => i.type === 'message')
    .flatMap(i => (i.content || []).filter(c => c.type === 'output_text').map(c => c.text))
    .join('');

  const toolCalls = items
    .filter(i => i.type === 'function_call')
    .map(i => {
      let input = {};
      try { input = JSON.parse(i.arguments || '{}'); } catch { /* 壊れた引数は空扱い */ }
      return { id: i.call_id || i.id, name: i.name, input };
    });

  return { text, toolCalls };
}

module.exports = { streamChat, callWithTools, LABEL };
