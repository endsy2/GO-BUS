import React from 'react';
import { Search, X } from 'lucide-react';
import { cn } from 'lib/utils';

/**
 * Shared search input used across all list/filter pages.
 *
 * Standardises the search experience: a left magnifier icon, a rounded
 * bordered field, and a clear (X) button that appears once there is text.
 *
 * Props:
 *  - value:        current search string (controlled)
 *  - onChange:     called with the new string value (NOT the event)
 *  - onClear:      optional; defaults to onChange('')
 *  - onSubmit:     optional; called when the user presses Enter
 *  - placeholder:  input placeholder
 *  - className:    extra classes for the wrapper (e.g. "flex-1")
 *  - inputClassName: extra classes for the <input> itself
 */
export const SearchInput = ({
  value = '',
  onChange,
  onClear,
  onSubmit,
  placeholder = 'Search...',
  className,
  inputClassName,
  disabled = false,
  autoFocus = false,
  ...props
}) => {
  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && onSubmit) {
      onSubmit(e);
    }
  };

  const handleClear = () => {
    if (onClear) {
      onClear();
    } else if (onChange) {
      onChange('');
    }
  };

  return (
    <div className={cn('relative w-full', className)}>
      <Search className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
      <input
        type="text"
        value={value}
        onChange={(e) => onChange?.(e.target.value)}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        disabled={disabled}
        autoFocus={autoFocus}
        className={cn(
          'h-10 w-full rounded-lg border border-input bg-background px-3 py-2 pl-10 pr-10 text-sm text-foreground',
          'focus:outline-none focus:ring-2 focus:ring-ring',
          'disabled:cursor-not-allowed disabled:opacity-50',
          inputClassName
        )}
        {...props}
      />
      {value && (
        <button
          type="button"
          onClick={handleClear}
          aria-label="Clear search"
          className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
        >
          <X className="h-4 w-4" />
        </button>
      )}
    </div>
  );
};

export default SearchInput;
