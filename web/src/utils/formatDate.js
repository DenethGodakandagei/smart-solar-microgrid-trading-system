import { format, formatDistanceToNow, isValid, parseISO } from 'date-fns';

/**
 * Format a date string or Date object to a readable format.
 * @param {string|Date} date
 * @param {string} formatStr - date-fns format string (default: 'MMM dd, yyyy')
 * @returns {string}
 */
export function formatDate(date, formatStr = 'MMM dd, yyyy') {
  if (!date) return '—';
  const parsed = typeof date === 'string' ? parseISO(date) : date;
  if (!isValid(parsed)) return '—';
  return format(parsed, formatStr);
}

/**
 * Format a date to include time (e.g., "Sep 28, 2026 2:30 PM")
 * @param {string|Date} date
 * @returns {string}
 */
export function formatDateTime(date) {
  return formatDate(date, 'MMM dd, yyyy h:mm a');
}

/**
 * Format a date as relative time (e.g., "3 hours ago")
 * @param {string|Date} date
 * @returns {string}
 */
export function formatRelativeTime(date) {
  if (!date) return '—';
  const parsed = typeof date === 'string' ? parseISO(date) : date;
  if (!isValid(parsed)) return '—';
  return formatDistanceToNow(parsed, { addSuffix: true });
}
