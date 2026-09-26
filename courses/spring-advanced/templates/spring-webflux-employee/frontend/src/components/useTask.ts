import { useState } from 'react';
export function useTask() {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  async function run(action: () => Promise<void>) {
    setBusy(true); setError(''); setMessage('');
    try { await action(); }
    catch (failure) { setError(failure instanceof Error ? failure.message : String(failure)); }
    finally { setBusy(false); }
  }
  return { busy, error, message, setMessage, run };
}
