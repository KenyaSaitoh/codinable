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

module.exports = { streamChat, LABEL };
