/**
 * Pagination component tests
 *
 * Covers:
 *  • Item count text display
 *  • Previous / Next button disabled states
 *  • Navigation callbacks
 *  • Page label display
 */
import React from 'react';
import { render, screen, fireEvent } from '../../../test-utils';
import { Pagination } from './Pagination';

// ── Default props helper ───────────────────────────────────────────────────────

const defaultProps = {
  currentPage: 0,
  totalPages: 5,
  pageSize: 15,
  totalElements: 73,
  onPageChange: jest.fn(),
  onPageSizeChange: jest.fn(),
};

const renderPagination = (overrides = {}) =>
  render(<Pagination {...defaultProps} {...overrides} />);

beforeEach(() => jest.clearAllMocks());

// ── Item count label ──────────────────────────────────────────────────────────

describe('item count label', () => {
  it('shows "Showing 1–15 of 73" on the first page', () => {
    renderPagination();
    expect(screen.getByText(/showing 1–15 of 73/i)).toBeInTheDocument();
  });

  it('shows correct range on an intermediate page', () => {
    // page index 2, size 15 → items 31–45
    renderPagination({ currentPage: 2 });
    expect(screen.getByText(/showing 31–45 of 73/i)).toBeInTheDocument();
  });

  it('caps the end item at totalElements on the last page', () => {
    // page index 4, size 15, 73 total → items 61–73 (not 75)
    renderPagination({ currentPage: 4 });
    expect(screen.getByText(/showing 61–73 of 73/i)).toBeInTheDocument();
  });
});

// ── Page label ────────────────────────────────────────────────────────────────

describe('page label', () => {
  it('shows "Page 1 of 5" on the first page', () => {
    renderPagination();
    expect(screen.getByText(/page 1 of 5/i)).toBeInTheDocument();
  });

  it('shows the correct page number on any page', () => {
    renderPagination({ currentPage: 3 });
    expect(screen.getByText(/page 4 of 5/i)).toBeInTheDocument();
  });
});

// ── Previous button ───────────────────────────────────────────────────────────

describe('Previous button', () => {
  it('is disabled on the first page', () => {
    renderPagination({ currentPage: 0 });
    const prevBtn = screen.getAllByRole('button')[0];
    expect(prevBtn).toBeDisabled();
  });

  it('is enabled when not on the first page', () => {
    renderPagination({ currentPage: 1 });
    const prevBtn = screen.getAllByRole('button')[0];
    expect(prevBtn).not.toBeDisabled();
  });

  it('calls onPageChange with (currentPage - 1) when clicked', () => {
    renderPagination({ currentPage: 2 });
    const prevBtn = screen.getAllByRole('button')[0];
    fireEvent.click(prevBtn);
    expect(defaultProps.onPageChange).toHaveBeenCalledWith(1);
  });
});

// ── Next button ───────────────────────────────────────────────────────────────

describe('Next button', () => {
  it('is disabled on the last page', () => {
    renderPagination({ currentPage: 4, totalPages: 5 });
    const buttons = screen.getAllByRole('button');
    const nextBtn = buttons[buttons.length - 1];
    expect(nextBtn).toBeDisabled();
  });

  it('is enabled when not on the last page', () => {
    renderPagination({ currentPage: 0, totalPages: 5 });
    const buttons = screen.getAllByRole('button');
    const nextBtn = buttons[buttons.length - 1];
    expect(nextBtn).not.toBeDisabled();
  });

  it('calls onPageChange with (currentPage + 1) when clicked', () => {
    renderPagination({ currentPage: 1 });
    const buttons = screen.getAllByRole('button');
    const nextBtn = buttons[buttons.length - 1];
    fireEvent.click(nextBtn);
    expect(defaultProps.onPageChange).toHaveBeenCalledWith(2);
  });
});

// ── Single page edge case ─────────────────────────────────────────────────────

describe('single-page edge case', () => {
  it('disables both buttons when there is only one page', () => {
    renderPagination({ currentPage: 0, totalPages: 1, totalElements: 5 });
    const buttons = screen.getAllByRole('button');
    expect(buttons[0]).toBeDisabled(); // prev
    expect(buttons[buttons.length - 1]).toBeDisabled(); // next
  });
});
