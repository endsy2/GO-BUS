import React from 'react';
import { cn } from 'lib/utils';

const variantClasses = {
  // Booking / order status
  confirmed: 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400 border-transparent',
  pending: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400 border-transparent',
  cancelled: 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400 border-transparent',
  // Generic status
  success: 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400 border-transparent',
  danger: 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400 border-transparent',
  warning: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400 border-transparent',
  info: 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400 border-transparent',
  // Neutral
  default: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300 border-transparent',
  secondary: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300 border-transparent',
  outline: 'bg-transparent text-foreground border-border',
};

const Badge = ({ children, variant = 'default', className }) => {
  const classes = variantClasses[variant?.toLowerCase()] ?? variantClasses.default;

  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors',
        classes,
        className
      )}
    >
      {children}
    </span>
  );
};

export default Badge;
export { Badge };
