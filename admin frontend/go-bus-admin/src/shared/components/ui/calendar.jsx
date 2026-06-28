import * as React from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { cn } from "lib/utils";

export function Calendar({ mode = "single", selected, onSelect, className, initialFocus, minDate }) {
  const [currentMonth, setCurrentMonth] = React.useState(
    selected ? new Date(selected) : new Date()
  );

  const daysInMonth = (date) => {
    return new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
  };

  const firstDayOfMonth = (date) => {
    return new Date(date.getFullYear(), date.getMonth(), 1).getDay();
  };

  const monthNames = [
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
  ];

  const dayNames = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

  const previousMonth = () => {
    setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1));
  };

  const nextMonth = () => {
    setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1));
  };

  const handleDayClick = (day) => {
    const newDate = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), day);
    if (onSelect) {
      onSelect(newDate);
    }
  };

  // A day is disabled when it falls before minDate (compared at day granularity).
  const isDisabled = (day) => {
    if (!minDate) return false;
    const checkDate = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), day);
    checkDate.setHours(0, 0, 0, 0);
    const min = new Date(minDate);
    min.setHours(0, 0, 0, 0);
    return checkDate < min;
  };

  const handleNavClick = (e, action) => {
    e.stopPropagation();
    action();
  };

  const isSelected = (day) => {
    if (!selected) return false;
    const checkDate = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), day);
    const selectedDate = new Date(selected);
    return (
      checkDate.getDate() === selectedDate.getDate() &&
      checkDate.getMonth() === selectedDate.getMonth() &&
      checkDate.getFullYear() === selectedDate.getFullYear()
    );
  };

  const isToday = (day) => {
    const today = new Date();
    const checkDate = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), day);
    return (
      checkDate.getDate() === today.getDate() &&
      checkDate.getMonth() === today.getMonth() &&
      checkDate.getFullYear() === today.getFullYear()
    );
  };

  const days = [];
  const totalDays = daysInMonth(currentMonth);
  const startDay = firstDayOfMonth(currentMonth);

  // Empty cells for days before month starts
  for (let i = 0; i < startDay; i++) {
    days.push(<div key={`empty-${i}`} className="w-9 h-9" />);
  }

  // Days of the month
  for (let day = 1; day <= totalDays; day++) {
    const disabled = isDisabled(day);
    days.push(
      <button
        key={day}
        type="button"
        disabled={disabled}
        onClick={(e) => {
          e.stopPropagation();
          if (!disabled) handleDayClick(day);
        }}
        className={cn(
          "w-9 h-9 flex items-center justify-center text-sm font-medium rounded-lg transition-all duration-200 relative",
          disabled
            ? "text-slate-300 dark:text-slate-700 cursor-not-allowed opacity-50"
            : "hover:bg-slate-200 dark:hover:bg-slate-800/50 active:scale-95",
          !disabled && isSelected(day) && "bg-blue-500 text-white hover:bg-blue-600 shadow-lg shadow-blue-500/30",
          !disabled && isToday(day) && !isSelected(day) && "border-2 border-blue-500/50 text-blue-500 dark:text-blue-400 font-bold",
          !disabled && !isSelected(day) && !isToday(day) && "text-slate-700 dark:text-slate-300 hover:text-slate-900 dark:hover:text-white"
        )}
      >
        {day}
      </button>
    );
  }

  return (
    <div className={cn("p-3 min-w-[280px]", className)}>
      <div className="flex items-center justify-between mb-3">
        <button
          type="button"
          onClick={(e) => handleNavClick(e, previousMonth)}
          className="p-1.5 hover:bg-slate-200 dark:hover:bg-slate-800/50 rounded-lg transition-all duration-200 active:scale-95"
        >
          <ChevronLeft className="h-4 w-4 text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white transition-colors" />
        </button>
        <div className="text-sm font-bold text-slate-900 dark:text-white">
          {monthNames[currentMonth.getMonth()]} {currentMonth.getFullYear()}
        </div>
        <button
          type="button"
          onClick={(e) => handleNavClick(e, nextMonth)}
          className="p-1.5 hover:bg-slate-200 dark:hover:bg-slate-800/50 rounded-lg transition-all duration-200 active:scale-95"
        >
          <ChevronRight className="h-4 w-4 text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white transition-colors" />
        </button>
      </div>
      <div className="grid grid-cols-7 gap-1 mb-2">
        {dayNames.map(name => (
          <div key={name} className="text-xs text-center text-slate-500 dark:text-slate-500 font-semibold p-1.5 w-9">
            {name}
          </div>
        ))}
      </div>
      <div className="grid grid-cols-7 gap-1">
        {days}
      </div>
    </div>
  );
}
