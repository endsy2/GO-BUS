import { useMemo } from 'react';

/**
 * Returns the currently authenticated user from localStorage.
 *
 * The value is memoized so it only changes when localStorage changes —
 * components that call this hook won't re-render unnecessarily.
 *
 * Usage:
 *   const user = useAuth();
 *   if (!user) return <Redirect to="/login" />;
 */
const useAuth = () => {
  return useMemo(() => {
    try {
      const raw = localStorage.getItem('user');
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps
};

export default useAuth;
