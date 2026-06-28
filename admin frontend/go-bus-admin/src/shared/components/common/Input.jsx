import React from 'react';
import { Input as ShadcnInput } from 'shared/components/ui/input';
import { Label } from 'shared/components/ui/label';
import { cn } from 'lib/utils';

export const Input = ({ 
  type = 'text', 
  name, 
  value, 
  onChange, 
  placeholder, 
  required = false,
  error,
  prefix,
  label
}) => {
  return (
    <div className="space-y-2">
      {label && <Label htmlFor={name}>{label}</Label>}
      <div className={cn("relative flex items-center", prefix && "border rounded-md")}>
        {prefix && (
          <span className="flex items-center justify-center px-3 bg-muted border-r min-w-[50px] text-muted-foreground">
            {prefix}
          </span>
        )}
        <ShadcnInput
          id={name}
          type={type}
          name={name}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          required={required}
          className={cn(
            error && "border-destructive focus-visible:ring-destructive",
            prefix && "border-0 rounded-l-none"
          )}
        />
      </div>
      {error && <p className="text-sm text-destructive">{error}</p>}
    </div>
  );
};

export default Input;
