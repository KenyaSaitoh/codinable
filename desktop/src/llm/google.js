// Gemini API (streamGenerateContent / SSE)。
// Codinable では Gemini Flash を選んだときに使う。
//
// SDK (@google/genai) は使わず REST を直接叩く。Codinable が必要とするのは
// 「テキストを投げてテキストを受け取る」だけで、SDK の版差に追随する手間に
// 見合わないため。

const { consumeSse, readErrorDetail, toFriendlyHttpError } = require('./sse');

const BASE_URL = 'https://generativelanguage.googleapis.com/v1beta';
const LABEL    = 'Google AI';

/** { role: 'user' | 'assistant' } → Gemini の contents ('user' | 'model') */
function toContents(messages) {
  return (messages || [])
    .map(m => ({
      role:  m.role === 'assistant' ? 'model' : 'user',
      parts: [{ text: String(m.content ?? '') }],
    }))
    .filter(c => c.parts[0].text.trim() !== '');
}

/** 混雑時の 503 は待てば通ることが多いので数回だけ再試行する */
const RETRY_ATTEMPTS = 3;
const RETRY_DELAY_MS = 2000;
const RETRYABLE = new Set([500, 502, 503, 504]);

function delay(ms, signal) {
  return new Promise(resolve => {
    const done = () => { clearTimeout(timer); signal?.removeEventListener?.('abort', done); resolve(); };
    const timer = setTimeout(done, ms);
    signal?.addEventListener?.('abort', done, { once: true });
  });
}

async function streamOnce({ apiKey, model, messages, system, signal, onText, maxTokens, progress }) {
  const body = {
    contents: toContents(messages),
    generationConfig: { maxOutputTokens: maxTokens },
  };
  if (system) body.systemInstruction = { parts: [{ text: system }] };

  const url = `${BASE_URL}/models/${encodeURIComponent(model)}:streamGenerateContent?alt=sse`;
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'content-type': 'application/json', 'x-goog-api-key': apiKey },
    body: JSON.stringify(body),
    signal,
  });

  if (!response.ok) {
    const detail = await readErrorDetail(response);
    const err = toFriendlyHttpError(response.status, detail, LABEL);
    err.status = response.status;
    throw err;
  }

  await consumeSse(response, event => {
    if (event.error) throw new Error(event.error.message || 'ストリーミング中にエラーが発生しました');
    for (const part of event.candidates?.[0]?.content?.parts || []) {
      if (typeof part.text === 'string' && part.text) { progress.emitted = true; onText(part.text); }
    }
  });
}

async function streamChat(opts) {
  const { signal } = opts;
  for (let attempt = 0; ; attempt++) {
    // 本文を出し始めたあとにやり直すと同じ文が二重に出るため、
    // 再試行できるのは「まだ 1 文字も出していない」あいだだけ
    const progress = { emitted: false };
    try {
      await streamOnce({ ...opts, maxTokens: opts.maxTokens || 8192, progress });
      return;
    } catch (err) {
      if (signal?.aborted || err.name === 'AbortError') return;
      const retryable = RETRYABLE.has(err.status) ||
                        /fetch failed|ECONNRESET|ETIMEDOUT|EAI_AGAIN/i.test(err.message || '');
      if (progress.emitted || attempt >= RETRY_ATTEMPTS || !retryable) throw err;
      console.warn(`[gemini] 呼び出しに失敗 (${attempt + 1}/${RETRY_ATTEMPTS + 1})。再試行します: ${err.message}`);
      await delay(RETRY_DELAY_MS, signal);
      if (signal?.aborted) return;
    }
  }
}

// ── 道具つきの呼び出し (Agent モード) ──────────────────────
//
// Gemini の functionCall には ID が無いので、こちら側で名前から作る
// (結果は functionResponse の name で対応づけられる)。

/** 中立な履歴 → Gemini の contents */
function toToolContents(messages) {
  const out = [];
  for (const m of messages || []) {
    if (m.role === 'tool') {
      out.push({
        role: 'user',
        parts: (m.results || []).map(r => ({
          functionResponse: { name: r.name, response: { output: String(r.output ?? '') } },
        })),
      });
      continue;
    }
    if (m.role === 'assistant' && m.toolCalls?.length) {
      const parts = [];
      if (String(m.content ?? '').trim()) parts.push({ text: m.content });
      for (const c of m.toolCalls) {
        parts.push({ functionCall: { name: c.name, args: c.input || {} } });
      }
      out.push({ role: 'model', parts });
      continue;
    }
    const text = String(m.content ?? '');
    if (text.trim() === '') continue;
    out.push({ role: m.role === 'assistant' ? 'model' : 'user', parts: [{ text }] });
  }
  return out;
}

async function callWithTools({ apiKey, model, messages, system, tools, signal, maxTokens = 8192 }) {
  const body = {
    contents: toToolContents(messages),
    generationConfig: { maxOutputTokens: maxTokens },
    tools: [{
      functionDeclarations: (tools || []).map(t => {
        const declaration = { name: t.name, description: t.description };
        // 引数の無い道具に空の parameters を渡すと Gemini が受け付けないので落とす
        if (Object.keys(t.schema?.properties || {}).length) declaration.parameters = t.schema;
        return declaration;
      }),
    }],
  };
  if (system) body.systemInstruction = { parts: [{ text: system }] };

  const url = `${BASE_URL}/models/${encodeURIComponent(model)}:generateContent`;
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'content-type': 'application/json', 'x-goog-api-key': apiKey },
    body: JSON.stringify(body),
    signal,
  });

  if (!response.ok) {
    throw toFriendlyHttpError(response.status, await readErrorDetail(response), LABEL);
  }

  const data  = await response.json();
  const parts = data.candidates?.[0]?.content?.parts || [];

  const text = parts.filter(p => typeof p.text === 'string').map(p => p.text).join('');
  const toolCalls = parts
    .filter(p => p.functionCall)
    .map((p, index) => ({
      id:    `${p.functionCall.name}-${index}`,
      name:  p.functionCall.name,
      input: p.functionCall.args || {},
    }));

  return { text, toolCalls };
}

module.exports = { streamChat, callWithTools, LABEL };
