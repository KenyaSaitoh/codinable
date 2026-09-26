export function Feedback({ error, message }: { error: string; message: string }) {
  return <div aria-live="polite">{error && <p role="alert" className="error">{error}</p>}{message && <p className="success">{message}</p>}</div>;
}
