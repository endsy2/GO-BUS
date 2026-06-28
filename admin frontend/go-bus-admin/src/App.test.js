/**
 * App smoke test
 *
 * Ensures the app shell can mount without crashing.  Providers (LocaleProvider,
 * ThemeProvider) live in index.js, so we supply them here via renderWithProviders.
 *
 * The full auth flow needs a live backend; the tests only assert that the
 * correct screen is shown when localStorage is empty (= the login page).
 */
import React from 'react';
import { render as renderWithProviders } from './test-utils';
import App from './App';

// localStorage starts empty for each test
beforeEach(() => {
  localStorage.clear();
});

test('renders the login page when no user is stored', async () => {
  const { findByText } = renderWithProviders(<App />);
  expect(await findByText(/Go Bus Booking System/i)).toBeInTheDocument();
});

test('renders the sign-in button on the login page', async () => {
  const { findByRole } = renderWithProviders(<App />);
  expect(
    await findByRole('button', { name: /sign in/i })
  ).toBeInTheDocument();
});
