import { useState, useEffect } from 'react';

/**
 * useDebounce — delays updating a value
 * Useful for search inputs to prevent API calls on every keystroke.
 *
 * @param {*} value - The value to debounce
 * @param {number} delay - Delay in milliseconds (default: 400)
 * @returns {*} The debounced value
 */
export function useDebounce(value, delay = 400) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => clearTimeout(timer);
  }, [value, delay]);

  return debouncedValue;
}
