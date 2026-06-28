import { useState, useCallback } from 'react';

/**
 * Shared pagination constants — import these everywhere instead of hard-coding.
 */
export const DEFAULT_PAGE_SIZE = 15;
export const PAGE_SIZE_OPTIONS = [15, 30, 50];

/**
 * usePagination — lightweight hook for managing 0-based pagination state.
 *
 * Usage inside a feature hook:
 *
 *   const { currentPage, pageSize, totalPages, totalElements,
 *           pagination, goToPage, changePageSize, resetPage, setTotals } = usePagination();
 *
 *   // After a successful fetch:
 *   setTotals({ totalPages: data.totalPages, totalElements: data.totalElements });
 *
 * The `pagination` object has the exact shape expected by <Pagination>:
 *   { currentPage, size, totalPages, totalElements }
 *
 * Convention: currentPage is always 0-based internally.
 * When calling 1-based backend APIs, add +1 at the service-call site.
 */
export function usePagination({ defaultPage = 0, defaultSize = DEFAULT_PAGE_SIZE } = {}) {
  const [currentPage, setCurrentPage] = useState(defaultPage);
  const [pageSize, setPageSize] = useState(defaultSize);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  /** Navigate to a specific 0-based page. */
  const goToPage = useCallback((page) => {
    setCurrentPage(page);
  }, []);

  /** Change page size and jump back to the first page. */
  const changePageSize = useCallback((size) => {
    setCurrentPage(0);
    setPageSize(size);
  }, []);

  /** Jump back to the first page (use when filters change). */
  const resetPage = useCallback(() => {
    setCurrentPage(0);
  }, []);

  /**
   * Update total counts after a successful fetch.
   * @param {{ totalPages: number, totalElements: number }} totals
   */
  const setTotals = useCallback(({ totalPages: tp = 0, totalElements: te = 0 } = {}) => {
    setTotalPages(tp);
    setTotalElements(te);
  }, []);

  return {
    currentPage,
    pageSize,
    totalPages,
    totalElements,
    /** Consistent shape consumed by <Pagination currentPage size totalPages totalElements> */
    pagination: { currentPage, size: pageSize, totalPages, totalElements },
    goToPage,
    changePageSize,
    resetPage,
    setTotals,
  };
}
