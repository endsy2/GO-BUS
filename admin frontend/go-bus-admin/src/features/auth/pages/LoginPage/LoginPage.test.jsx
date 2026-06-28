/**
 * LoginPage tests
 *
 * Covers:
 *  • Form renders correctly
 *  • Client-side validation (empty fields, invalid email)
 *  • Password visibility toggle
 *  • Successful login flow (mocked services)
 *  • API error display
 */
import React from 'react';
import { render, screen, fireEvent, waitFor } from 'test-utils';
import LoginPage from './LoginPage';

// ── Service mocks ──────────────────────────────────────────────────────────────

// Resolve mock paths relative to the test file; jest maps 'features/...' to src/features/...
jest.mock('../../services/authService', () => ({
  __esModule: true,
  default: {
    login: jest.fn(),
  },
}));

jest.mock('features/team/services/userService', () => ({
  __esModule: true,
  default: {
    getProfile: jest.fn(),
  },
}));

import authService from '../../services/authService';
import userService from 'features/team/services/teamService';

// ── Helpers ───────────────────────────────────────────────────────────────────

const onLoginSuccess = jest.fn();

const renderLogin = () => render(<LoginPage onLoginSuccess={onLoginSuccess} />);

const fillEmail = (value) =>
  fireEvent.change(screen.getByPlaceholderText(/admin@example.com/i), {
    target: { value },
  });

const fillPassword = (value) =>
  fireEvent.change(screen.getByPlaceholderText(/••••••••/), {
    target: { value },
  });

const submit = () => fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

// ── Tests ─────────────────────────────────────────────────────────────────────

beforeEach(() => {
  jest.clearAllMocks();
  localStorage.clear();
});

describe('LoginPage rendering', () => {
  it('shows the app title', () => {
    renderLogin();
    expect(screen.getByText(/Go Bus Booking System/i)).toBeInTheDocument();
  });

  it('renders email and password fields', () => {
    renderLogin();
    expect(screen.getByPlaceholderText(/admin@example.com/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/••••••••/)).toBeInTheDocument();
  });

  it('renders the Sign In submit button', () => {
    renderLogin();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });
});

describe('password visibility toggle', () => {
  it('password field starts as type=password', () => {
    renderLogin();
    expect(screen.getByPlaceholderText(/••••••••/)).toHaveAttribute('type', 'password');
  });

  it('clicking the eye icon toggles to type=text and back', () => {
    renderLogin();
    const passwordInput = screen.getByPlaceholderText(/••••••••/);
    // There is exactly one toggle button (the eye icon button)
    const toggleButton = screen.getByRole('button', { name: '' });
    // Not all buttons have text — find the one next to the password field
    // It is the only <button type="button"> on the page
    const allButtons = screen.getAllByRole('button');
    const eyeBtn = allButtons.find(
      (b) => b.getAttribute('type') === 'button'
    );

    fireEvent.click(eyeBtn);
    expect(passwordInput).toHaveAttribute('type', 'text');

    fireEvent.click(eyeBtn);
    expect(passwordInput).toHaveAttribute('type', 'password');
  });
});

describe('client-side validation', () => {
  it('shows error when email is empty', async () => {
    renderLogin();
    submit();
    expect(await screen.findByText(/email is required/i)).toBeInTheDocument();
  });

  it('shows error when email format is invalid', async () => {
    renderLogin();
    fillEmail('not-an-email');
    submit();
    expect(await screen.findByText(/email is invalid/i)).toBeInTheDocument();
  });

  it('shows error when password is empty', async () => {
    renderLogin();
    fillEmail('admin@example.com');
    submit();
    expect(await screen.findByText(/password is required/i)).toBeInTheDocument();
  });

  it('does not call authService when validation fails', () => {
    renderLogin();
    submit();
    expect(authService.login).not.toHaveBeenCalled();
  });

  it('clears email error when user starts typing', async () => {
    renderLogin();
    submit();
    await screen.findByText(/email is required/i);
    fillEmail('a');
    expect(screen.queryByText(/email is required/i)).not.toBeInTheDocument();
  });
});

describe('successful login', () => {
  const tokenData = { token: 'tok123', refreshToken: 'ref456' };
  const profileData = { fullName: 'Admin User', userName: 'admin' };

  beforeEach(() => {
    authService.login.mockResolvedValue({ data: tokenData });
    userService.getProfile.mockResolvedValue({ data: profileData });
  });

  it('calls authService.login with credentials', async () => {
    renderLogin();
    fillEmail('admin@example.com');
    fillPassword('secret123');
    submit();

    await waitFor(() =>
      expect(authService.login).toHaveBeenCalledWith({
        email: 'admin@example.com',
        password: 'secret123',
      })
    );
  });

  it('calls onLoginSuccess with merged user profile', async () => {
    renderLogin();
    fillEmail('admin@example.com');
    fillPassword('secret123');
    submit();

    await waitFor(() => expect(onLoginSuccess).toHaveBeenCalled());
    const calledWith = onLoginSuccess.mock.calls[0][0];
    expect(calledWith.fullName).toBe('Admin User');
    expect(calledWith.token).toBe('tok123');
  });

  it('stores the user profile in localStorage', async () => {
    renderLogin();
    fillEmail('admin@example.com');
    fillPassword('secret123');
    submit();

    await waitFor(() => expect(onLoginSuccess).toHaveBeenCalled());
    const stored = JSON.parse(localStorage.getItem('user'));
    expect(stored.token).toBe('tok123');
  });
});

describe('API error display', () => {
  it('shows the API error message when login fails', async () => {
    authService.login.mockRejectedValue({
      response: { data: { message: 'Invalid credentials' } },
    });

    renderLogin();
    fillEmail('admin@example.com');
    fillPassword('wrong');
    submit();

    expect(await screen.findByText(/invalid credentials/i)).toBeInTheDocument();
  });

  it('shows fallback error message when response has no message', async () => {
    authService.login.mockRejectedValue(new Error('Network error'));

    renderLogin();
    fillEmail('admin@example.com');
    fillPassword('wrong');
    submit();

    expect(await screen.findByText(/login failed/i)).toBeInTheDocument();
  });

  it('still calls onLoginSuccess when profile fetch fails (graceful fallback)', async () => {
    authService.login.mockResolvedValue({ data: { token: 'tok', refreshToken: 'ref' } });
    userService.getProfile.mockRejectedValue(new Error('profile 500'));

    renderLogin();
    fillEmail('admin@example.com');
    fillPassword('pass');
    submit();

    await waitFor(() => expect(onLoginSuccess).toHaveBeenCalled());
    expect(onLoginSuccess.mock.calls[0][0].token).toBe('tok');
  });
});
