import React, { useEffect } from 'react';
import { CheckCircle2, XCircle, AlertTriangle, Info, X } from 'lucide-react';
import { cn } from 'lib/utils';

export const Snackbar = ({ message, type = 'success', isOpen, onClose, duration = 3000 }) => {
  useEffect(() => {
    if (isOpen && duration > 0) {
      const timer = setTimeout(() => {
        onClose();
      }, duration);

      return () => clearTimeout(timer);
    }
  }, [isOpen, duration, onClose]);

  if (!isOpen) return null;

  const getIcon = () => {
    switch (type) {
      case 'success':
        return <CheckCircle2 className="h-5 w-5" />;
      case 'error':
        return <XCircle className="h-5 w-5" />;
      case 'warning':
        return <AlertTriangle className="h-5 w-5" />;
      case 'info':
        return <Info className="h-5 w-5" />;
      default:
        return null;
    }
  };

  const typeStyles = {
    success: 'bg-green-500 text-white',
    error: 'bg-red-500 text-white',
    warning: 'bg-orange-500 text-white',
    info: 'bg-blue-500 text-white'
  };

  return (
    <div className={cn(
      "fixed bottom-6 left-1/2 -translate-x-1/2 flex items-center gap-3 px-5 py-3.5 rounded-lg shadow-lg z-[10000] min-w-[300px] max-w-[500px] animate-in slide-in-from-bottom-full",
      typeStyles[type]
    )}>
      <div className="flex items-center justify-center flex-shrink-0">
        {getIcon()}
      </div>
      <div className="flex-1 text-sm font-medium leading-snug">{message}</div>
      <button 
        className="flex items-center justify-center p-1 rounded hover:bg-white/20 transition-colors flex-shrink-0"
        onClick={onClose}
      >
        <X className="h-4 w-4" />
      </button>
    </div>
  );
};

export default Snackbar;
