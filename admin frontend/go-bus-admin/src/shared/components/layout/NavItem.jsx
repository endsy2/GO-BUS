import React from 'react';
import { cn } from 'lib/utils';

export const NavItem = ({ icon, label, active, onClick }) => {
  return (
    <button
      onClick={onClick}
      className={cn(
        "w-full flex items-center gap-3 px-5 py-3 text-sm font-medium transition-all duration-200 relative group",
        active 
          ? "bg-blue-50 dark:bg-blue-500/10 text-blue-600 dark:text-blue-400" 
          : "text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-50 dark:hover:bg-gray-800/50"
      )}
    >
      {/* Active indicator */}
      {active && (
        <div className="absolute left-0 top-0 bottom-0 w-1 bg-blue-600 dark:bg-blue-500 rounded-r-full" />
      )}
      
      {/* Icon */}
      <span className={cn(
        "flex items-center justify-center w-5 h-5 transition-all duration-200",
        active ? "text-blue-600 dark:text-blue-400 scale-110" : "text-gray-500 dark:text-gray-400 group-hover:text-gray-900 dark:group-hover:text-white group-hover:scale-105"
      )}>
        {icon}
      </span>
      
      {/* Label */}
      <span className={cn(
        "flex-1 text-left transition-all duration-200",
        active ? "font-semibold" : "font-medium"
      )}>
        {label}
      </span>

    </button>
  );
};

export default NavItem;
