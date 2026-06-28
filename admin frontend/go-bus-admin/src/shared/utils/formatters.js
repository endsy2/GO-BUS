/**
 * Converts UPPER_SNAKE_CASE enum values to Title Case for display.
 * e.g. "REFUND_REQUESTED" → "Refund Requested", "PENDING" → "Pending"
 */
export const formatStatus = (value) =>
  value
    ? value
        .split('_')
        .map((w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
        .join(' ')
    : '';
