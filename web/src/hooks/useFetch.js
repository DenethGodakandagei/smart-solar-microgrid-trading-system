import { useState, useEffect, useCallback } from 'react';

/**
 * useFetch — generic data-fetching hook
 *
 * @param {Function} apiFn - Async API function to call (e.g., userService.getUsers)
 * @param {object} params - Parameters to pass to apiFn. Re-fetches when params change.
 * @param {object} options - { immediate: boolean } — if false, won't fetch on mount
 *
 * @returns {{ data, loading, error, refetch }}
 */
export function useFetch(apiFn, params = {}, options = { immediate: true }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(options.immediate);
  const [error, setError] = useState(null);

  const paramsKey = JSON.stringify(params);

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await apiFn(params);
      setData(result);
    } catch (err) {
      const message =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'An unexpected error occurred';
      setError(message);
    } finally {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [apiFn, paramsKey]);

  useEffect(() => {
    if (options.immediate) {
      fetchData();
    }
  }, [fetchData, options.immediate]);

  return { data, loading, error, refetch: fetchData };
}
