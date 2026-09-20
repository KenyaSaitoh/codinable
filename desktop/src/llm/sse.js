// Server-Sent Events (SSE) の共通読み取り。
// 3 社とも「行頭 data: の後ろに JSON」という形は共通なので、
// フレーム分解だけここに寄せ、差分は各プロバイダの onEvent に閉じる。

/**
 * @param {Response} response fetch のレスポンス (body は ReadableStream)
 * @param {(payload: object, raw: string) => void} onEvent data: 行ごとに呼ばれる
 */
async function consumeSse(response, onEvent) {
  const reader  = response.body.getReader();
  const decoder = new TextDecoder();
  let   buffer  = '';

  for (;;) {
    const { done, value } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });

    const lines = buffer.split('\n');
    buffer = lines.pop();

    for (const line of lines) {
      const trimmed = line.trimEnd();
      if (!trimmed.startsWith('data:')) continue;
      const data = trimmed.slice(5).trim();
      if (!data || data === '[DONE]') continue;
      let parsed;
      try { parsed = JSON.parse(data); } catch { continue; }
      onEvent(parsed, data);
    }
  }
}

/** エラーレスポンスの本文から、人が読める 1 行を取り出す */
async function readErrorDetail(response) {
  let text = '';
  try { text = await response.text(); } catch { /* 本文が読めないこともある */ }
  try {
    const parsed = JSON.parse(text);
    return parsed.error?.message || parsed.message || parsed.detail || text;
  } catch {
    return text;
  }
}

/** 認証・レート制限を、設定パネルへ誘導する文言に変換する */
function toFriendlyHttpError(status, detail, providerLabel) {
  if (status === 401 || status === 403) {
    return Object.assign(
      new Error(`${providerLabel} の API キーが正しくないようです。設定パネルで登録し直してください。`),
      { code: 'API_KEY_INVALID' },
    );
  }
  if (status === 429) {
    return Object.assign(
      new Error(`${providerLabel} の利用上限に達しました。しばらく待ってからお試しください。`),
      { code: 'RATE_LIMITED' },
    );
  }
  return new Error(`${providerLabel} API error ${status}: ${detail}`);
}

module.exports = { consumeSse, readErrorDetail, toFriendlyHttpError };
