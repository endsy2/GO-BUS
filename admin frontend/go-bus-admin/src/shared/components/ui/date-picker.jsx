import * as React from "react";
import { Calendar as CalendarIcon, X } from "lucide-react";
import { cn } from "lib/utils";
import { Button } from "./button";
import { Calendar } from "./calendar";
import { Popover, PopoverContent, PopoverTrigger } from "./popover";

export function DatePicker({ value, onChange, placeholder = "Pick a date", className, disabled, minDate }) {
  const parseDate = (dateString) => {
    if (!dateString) return undefined;
    // Parse YYYY-MM-DD as local date (no timezone conversion)
    const [year, month, day] = dateString.split('T')[0].split('-').map(Number);
    return new Date(year, month - 1, day);
  };

  const [date, setDate] = React.useState(value ? parseDate(value) : undefined);

  React.useEffect(() => {
    if (value) {
      setDate(parseDate(value));
    } else {
      setDate(undefined);
    }
  }, [value]);

  const handleSelect = (selectedDate) => {
    setDate(selectedDate);
    if (onChange) {
      // Format as YYYY-MM-DD using local date (no timezone conversion)
      if (selectedDate) {
        const year = selectedDate.getFullYear();
        const month = String(selectedDate.getMonth() + 1).padStart(2, '0');
        const day = String(selectedDate.getDate()).padStart(2, '0');
        const formatted = `${year}-${month}-${day}`;
        onChange(formatted);
      } else {
        onChange('');
      }
    }
  };

  const handleClear = (e) => {
    e.stopPropagation();
    setDate(undefined);
    if (onChange) {
      onChange('');
    }
  };

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button
          variant="outline"
          disabled={disabled}
          className={cn(
            "w-full justify-start text-left font-normal bg-white dark:bg-slate-800/50 border-slate-300 dark:border-slate-700/50 text-slate-900 dark:text-white hover:bg-slate-50 dark:hover:bg-slate-800 hover:border-slate-400 dark:hover:border-slate-600 relative transition-all duration-200",
            !date && "text-slate-400 dark:text-slate-400",
            className
          )}
        >
          <CalendarIcon className="mr-2 h-4 w-4 flex-shrink-0 text-slate-400 dark:text-slate-400" />
          <span className="flex-1 truncate">
            {date ? (
              date.toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'long',
                day: 'numeric'
              })
            ) : (
              placeholder
            )}
          </span>
          {date && (
            <X 
              className="h-4 w-4 ml-2 flex-shrink-0 text-slate-400 dark:text-slate-400 hover:text-red-500 dark:hover:text-red-400 transition-colors" 
              onClick={handleClear}
            />
          )}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-auto p-0 bg-white dark:bg-slate-900/95 backdrop-blur-xl border-slate-200 dark:border-slate-700/50 shadow-2xl" align="start">
        <Calendar
          mode="single"
          selected={date}
          onSelect={handleSelect}
          minDate={minDate}
          initialFocus
          className="bg-transparent"
        />
      </PopoverContent>
    </Popover>
  );
}
