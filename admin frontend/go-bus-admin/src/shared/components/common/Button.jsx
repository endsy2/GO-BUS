import React from 'react';
import { Button as ShadcnButton } from '../ui/button';
import { cn } from 'lib/utils';

const Button = ({ children, variant = 'primary', onClick, className = '', type = 'button', disabled = false, ...props }) => {
  const variantMap = {
    primary: 'default',
    secondary: 'secondary',
    danger: 'destructive'
  };

  return (
    <ShadcnButton 
      type={type}
      variant={variantMap[variant] || 'default'}
      onClick={onClick}
      disabled={disabled}
      className={cn(className)}
      {...props}
    >
      {children}
    </ShadcnButton>
  );
};

export default Button;
export { Button };
