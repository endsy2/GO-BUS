/**
 * Shared test utilities
 *
 * Wraps components with all required context providers so individual test
 * files don't have to repeat boilerplate.
 */
import React from 'react';
import { render } from '@testing-library/react';
import { LocaleProvider } from 'shared/context/LocaleContext';
import { ThemeProvider } from 'shared/context/ThemeContext';

/**
 * Renders a component with LocaleProvider + ThemeProvider.
 * Accepts the same options as @testing-library/react render().
 */
export const renderWithProviders = (ui, options = {}) => {
  const Wrapper = ({ children }) => (
    <ThemeProvider>
      <LocaleProvider>
        {children}
      </LocaleProvider>
    </ThemeProvider>
  );

  return render(ui, { wrapper: Wrapper, ...options });
};

// Re-export everything from testing-library so tests can import from one place
export * from '@testing-library/react';
export { renderWithProviders as render };
