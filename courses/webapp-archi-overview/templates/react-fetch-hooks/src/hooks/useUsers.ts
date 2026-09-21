import { useCallback, useEffect, useState } from 'react';

export type User = { id: number; name: string; email: string };

// データ取得に必要なStateと副作用を、UIから再利用可能な関数へ分離する。
const useUsers = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [requestId, setRequestId] = useState(0);

  useEffect(() => {
    const controller = new AbortController();

    const fetchUsers = async () => {
      setLoading(true);
      setError('');
      try {
        const response = await fetch('/users.json', { signal: controller.signal });
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        setUsers(await response.json() as User[]);
      } catch (reason) {
        if (reason instanceof DOMException && reason.name === 'AbortError') return;
        setError(reason instanceof Error ? reason.message : '取得に失敗しました');
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    };

    void fetchUsers();
    return () => controller.abort();
  }, [requestId]);

  const reload = useCallback(() => setRequestId((id) => id + 1), []);
  return { users, loading, error, reload };
};

export default useUsers;
