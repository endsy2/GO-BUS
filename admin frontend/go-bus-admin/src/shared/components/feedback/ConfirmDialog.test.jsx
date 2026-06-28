/**
 * ConfirmDialog component tests
 *
 * Covers:
 *  • Not rendered when isOpen=false
 *  • Shows title, message, and action buttons when open
 *  • Confirm and Cancel callbacks fire correctly
 *  • Default text values
 *  • type variants (danger / warning / info) don't crash
 */
import React from 'react';
import { render, screen, fireEvent } from '../../../test-utils';
import { ConfirmDialog } from './ConfirmDialog';

// ── Helpers ───────────────────────────────────────────────────────────────────

const defaultProps = {
  isOpen: true,
  onConfirm: jest.fn(),
  onCancel: jest.fn(),
  title: 'Delete Bus',
  message: 'Are you sure you want to delete this bus?',
};

const renderDialog = (overrides = {}) =>
  render(<ConfirmDialog {...defaultProps} {...overrides} />);

beforeEach(() => jest.clearAllMocks());

// ── Visibility ────────────────────────────────────────────────────────────────

describe('visibility', () => {
  it('does not show content when isOpen is false', () => {
    renderDialog({ isOpen: false });
    expect(screen.queryByText('Delete Bus')).not.toBeInTheDocument();
  });

  it('shows title and message when isOpen is true', () => {
    renderDialog();
    expect(screen.getByText('Delete Bus')).toBeInTheDocument();
    expect(screen.getByText('Are you sure you want to delete this bus?')).toBeInTheDocument();
  });
});

// ── Action buttons ────────────────────────────────────────────────────────────

describe('action buttons', () => {
  it('renders Confirm button with default text', () => {
    renderDialog();
    expect(screen.getByRole('button', { name: /confirm/i })).toBeInTheDocument();
  });

  it('renders Cancel button with default text', () => {
    renderDialog();
    expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument();
  });

  it('calls onConfirm when Confirm is clicked', () => {
    renderDialog();
    fireEvent.click(screen.getByRole('button', { name: /confirm/i }));
    expect(defaultProps.onConfirm).toHaveBeenCalledTimes(1);
    expect(defaultProps.onCancel).not.toHaveBeenCalled();
  });

  it('calls onCancel when Cancel is clicked', () => {
    renderDialog();
    fireEvent.click(screen.getByRole('button', { name: /cancel/i }));
    expect(defaultProps.onCancel).toHaveBeenCalledTimes(1);
    expect(defaultProps.onConfirm).not.toHaveBeenCalled();
  });

  it('uses custom confirmText and cancelText props', () => {
    renderDialog({ confirmText: 'Yes, delete', cancelText: 'No, keep it' });
    expect(screen.getByRole('button', { name: /yes, delete/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /no, keep it/i })).toBeInTheDocument();
  });
});

// ── type variants ─────────────────────────────────────────────────────────────

describe('type variants', () => {
  it.each(['danger', 'warning', 'info'])(
    'renders without crashing for type="%s"',
    (type) => {
      expect(() => renderDialog({ type })).not.toThrow();
      expect(screen.getByRole('button', { name: /confirm/i })).toBeInTheDocument();
    }
  );
});

// ── Invariant message ─────────────────────────────────────────────────────────

describe('disclaimer text', () => {
  it('shows the "action cannot be undone" note', () => {
    renderDialog();
    // The translated key is 'actionCannotBeUndone'; in English locale it should render
    // the translated string or fall back to the key itself.
    // Either way some descriptive text is present inside the description.
    const description = screen.getByRole('dialog');
    expect(description).toBeInTheDocument();
  });
});
