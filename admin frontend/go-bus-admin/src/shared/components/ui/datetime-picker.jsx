import * as React from "react";
import { Calendar as CalendarIcon, Clock, X, ChevronUp, ChevronDown } from "lucide-react";
import { cn } from "lib/utils";
import { Button } from "./button";
import { Calendar } from "./calendar";
import { Popover, PopoverContent, PopoverTrigger } from "./popover";

export function DateTimePicker({ value, onChange, placeholder = "Pick date and time", className, disabled }) {
  // Convert a 24-hour value (0-23) to the 12-hour display hour (1-12).
  const to12Hour = (h) => (h % 12 === 0 ? 12 : h % 12);

  const [date, setDate] = React.useState(value ? new Date(value) : undefined);
  const [hours, setHours] = React.useState(value ? to12Hour(new Date(value).getHours()) : 12);
  const [minutes, setMinutes] = React.useState(value ? new Date(value).getMinutes() : 0);
  const [period, setPeriod] = React.useState(value ? (new Date(value).getHours() >= 12 ? 'PM' : 'AM') : 'PM');

  React.useEffect(() => {
    if (value) {
      const d = new Date(value);
      setDate(d);
      const h = d.getHours();
      setHours(to12Hour(h));
      setMinutes(d.getMinutes());
      setPeriod(h >= 12 ? 'PM' : 'AM');
    } else {
      // Reset when the value is cleared/empty so the picker doesn't keep showing
      // a stale date when the parent form is reset or a different record loads.
      setDate(undefined);
      setHours(12);
      setMinutes(0);
      setPeriod('PM');
    }
  }, [value]);

  const handleDateSelect = (selectedDate) => {
    if (selectedDate) {
      setDate(selectedDate);
      updateDateTime(selectedDate, hours, minutes, period);
    }
  };

  const handleHoursChange = (newHours) => {
    setHours(newHours);
    if (date) {
      updateDateTime(date, newHours, minutes, period);
    }
  };

  const handleMinutesChange = (newMinutes) => {
    setMinutes(newMinutes);
    if (date) {
      updateDateTime(date, hours, newMinutes, period);
    }
  };

  const handlePeriodToggle = () => {
    const newPeriod = period === 'AM' ? 'PM' : 'AM';
    setPeriod(newPeriod);
    if (date) {
      updateDateTime(date, hours, minutes, newPeriod);
    }
  };

  const updateDateTime = (selectedDate, h, m, p) => {
    if (selectedDate && onChange) {
      const newDate = new Date(selectedDate);
      let hour24 = h;
      if (p === 'PM' && h !== 12) hour24 = h + 12;
      if (p === 'AM' && h === 12) hour24 = 0;

      newDate.setHours(hour24, m, 0, 0);

      // Format as YYYY-MM-DDTHH:mm for datetime-local input compatibility
      const year = newDate.getFullYear();
      const month = String(newDate.getMonth() + 1).padStart(2, '0');
      const day = String(newDate.getDate()).padStart(2, '0');
      const hour = String(newDate.getHours()).padStart(2, '0');
      const minute = String(newDate.getMinutes()).padStart(2, '0');

      onChange(`${year}-${month}-${day}T${hour}:${minute}`);
    }
  };

  const handleClear = (e) => {
    e.stopPropagation();
    setDate(undefined);
    setHours(12);
    setMinutes(0);
    setPeriod('PM');
    if (onChange) {
      onChange('');
    }
  };

  const formatDateTime = () => {
    if (!date) return null;

    const dateStr = date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });

    const timeStr = `${hours}:${String(minutes).padStart(2, '0')} ${period}`;

    return `${dateStr}, ${timeStr}`;
  };

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button
          type="button"
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
            {date ? formatDateTime() : placeholder}
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
        <div className="flex">
          {/* Calendar Section */}
          <div className="border-r border-slate-200 dark:border-slate-700/50">
            <Calendar
              mode="single"
              selected={date}
              onSelect={handleDateSelect}
              initialFocus
              className="bg-transparent"
            />
          </div>

          {/* Time Picker Section */}
          <div className="p-4 space-y-3 bg-gradient-to-b from-slate-50 to-slate-100 dark:from-slate-900/50 dark:to-slate-900 w-52">
            <div className="flex items-center gap-2 pb-2 border-b border-slate-200 dark:border-slate-700/50">
              <Clock className="h-4 w-4 text-blue-500 dark:text-blue-400" />
              <span className="text-sm font-semibold text-slate-900 dark:text-white">Select Time</span>
            </div>

            <div className="flex items-center justify-center gap-2">
              {/* Hours */}
              <div className="flex flex-col items-center gap-1">
                <button
                  type="button"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleHoursChange(hours === 12 ? 1 : hours + 1);
                  }}
                  className="p-1 hover:bg-slate-200 dark:hover:bg-slate-800/50 rounded-lg transition-all duration-200 active:scale-95"
                >
                  <ChevronUp className="w-4 h-4 text-slate-500 dark:text-slate-400 hover:text-blue-500 dark:hover:text-blue-400 transition-colors" />
                </button>
                <div className="w-14 h-12 flex items-center justify-center bg-slate-100 dark:bg-slate-800/80 rounded-xl border border-slate-200 dark:border-slate-700/50 shadow-lg">
                  <span className="text-xl font-bold text-slate-900 dark:text-white tabular-nums">{String(hours).padStart(2, '0')}</span>
                </div>
                <button
                  type="button"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleHoursChange(hours === 1 ? 12 : hours - 1);
                  }}
                  className="p-1 hover:bg-slate-200 dark:hover:bg-slate-800/50 rounded-lg transition-all duration-200 active:scale-95"
                >
                  <ChevronDown className="w-4 h-4 text-slate-500 dark:text-slate-400 hover:text-blue-500 dark:hover:text-blue-400 transition-colors" />
                </button>
              </div>

              <span className="text-xl font-bold text-slate-400 dark:text-slate-500 mb-1">:</span>

              {/* Minutes */}
              <div className="flex flex-col items-center gap-1">
                <button
                  type="button"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleMinutesChange(minutes === 59 ? 0 : minutes + 1);
                  }}
                  className="p-1 hover:bg-slate-200 dark:hover:bg-slate-800/50 rounded-lg transition-all duration-200 active:scale-95"
                >
                  <ChevronUp className="w-4 h-4 text-slate-500 dark:text-slate-400 hover:text-blue-500 dark:hover:text-blue-400 transition-colors" />
                </button>
                <div className="w-14 h-12 flex items-center justify-center bg-slate-100 dark:bg-slate-800/80 rounded-xl border border-slate-200 dark:border-slate-700/50 shadow-lg">
                  <span className="text-xl font-bold text-slate-900 dark:text-white tabular-nums">{String(minutes).padStart(2, '0')}</span>
                </div>
                <button
                  type="button"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleMinutesChange(minutes === 0 ? 59 : minutes - 1);
                  }}
                  className="p-1 hover:bg-slate-200 dark:hover:bg-slate-800/50 rounded-lg transition-all duration-200 active:scale-95"
                >
                  <ChevronDown className="w-4 h-4 text-slate-500 dark:text-slate-400 hover:text-blue-500 dark:hover:text-blue-400 transition-colors" />
                </button>
              </div>
            </div>

            {/* AM/PM */}
            <div className="flex gap-2">
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  if (period !== 'AM') handlePeriodToggle();
                }}
                className={cn(
                  "px-4 py-2 rounded-lg text-sm font-bold transition-all duration-200 flex-1",
                  period === 'AM'
                    ? "bg-blue-500 text-white shadow-lg shadow-blue-500/30"
                    : "bg-slate-200 dark:bg-slate-800/50 text-slate-600 dark:text-slate-400 hover:bg-slate-300 dark:hover:bg-slate-700/50 active:scale-95"
                )}
              >
                AM
              </button>
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  if (period !== 'PM') handlePeriodToggle();
                }}
                className={cn(
                  "px-4 py-2 rounded-lg text-sm font-bold transition-all duration-200 flex-1",
                  period === 'PM'
                    ? "bg-blue-500 text-white shadow-lg shadow-blue-500/30"
                    : "bg-slate-200 dark:bg-slate-800/50 text-slate-600 dark:text-slate-400 hover:bg-slate-300 dark:hover:bg-slate-700/50 active:scale-95"
                )}
              >
                PM
              </button>
            </div>

            {/* Quick time buttons */}
            <div className="grid grid-cols-2 gap-2 pt-2 border-t border-slate-200 dark:border-slate-700/50">
              {[
                { label: '9 AM', h: 9, m: 0, p: 'AM' },
                { label: '12 PM', h: 12, m: 0, p: 'PM' },
                { label: '3 PM', h: 3, m: 0, p: 'PM' },
                { label: '6 PM', h: 6, m: 0, p: 'PM' },
              ].map((preset) => (
                <button
                  key={preset.label}
                  type="button"
                  onClick={(e) => {
                    e.stopPropagation();
                    setHours(preset.h);
                    setMinutes(preset.m);
                    setPeriod(preset.p);
                    if (date) {
                      updateDateTime(date, preset.h, preset.m, preset.p);
                    }
                  }}
                  className="px-3 py-1.5 text-xs font-medium bg-slate-200 dark:bg-slate-800/50 hover:bg-slate-300 dark:hover:bg-slate-700/50 hover:text-blue-500 dark:hover:text-blue-400 text-slate-700 dark:text-slate-300 rounded-lg transition-all duration-200 active:scale-95"
                >
                  {preset.label}
                </button>
              ))}
            </div>
          </div>
        </div>
      </PopoverContent>
    </Popover>
  );
}

