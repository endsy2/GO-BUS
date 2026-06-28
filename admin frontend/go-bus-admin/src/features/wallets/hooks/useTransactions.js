import { useState, useEffect, useCallback } from 'react';
import walletService from '../services/walletService';
import { DEFAULT_PAGE_SIZE } from 'shared/hooks/usePagination';

/**
 * useTransactions — server-side paginated transaction data with filters.
 *
 * Pattern matches useBookings: all state lives as separate useState entries;
 * fetchTransactions depends on all of them via useCallback, and useEffect([fetchTransactions])
 * fires exactly once per dependency change — no stale-closure issues.
 */
export const useTransactions = (initialFilters = {}, initialPage = 0, initialSize = DEFAULT_PAGE_SIZE) => {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [pageSize, setPageSize] = useState(initialSize);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [filters, setFilters] = useState(initialFilters);

  const fetchTransactions = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      // Handle PagedResponse structure: { content, page, size, totalElements, totalPages }
      const response = await walletService.getTransactions(filters, currentPage, pageSize);
      setTransactions(response.content || []);
      setTotalPages(response.totalPages || 0);
      setTotalElements(response.totalElements || 0);
      // Do NOT sync currentPage from response — that causes a second fetch on every page change.
    } catch (err) {
      setError(err.message || 'Failed to fetch transactions');
      setTransactions([]);
    } finally {
      setLoading(false);
    }
  }, [filters, currentPage, pageSize]);

  useEffect(() => {
    fetchTransactions();
  }, [fetchTransactions]);

  const updateFilters = (newFilters) => {
    setCurrentPage(0);
    setFilters(prev => ({ ...prev, ...newFilters }));
  };

  const goToPage = (page) => setCurrentPage(page);

  const changePageSize = (size) => {
    setCurrentPage(0);
    setPageSize(size);
  };

  return {
    transactions,
    loading,
    error,
    pagination: {
      currentPage,
      totalPages,
      totalElements,
      size: pageSize,
    },
    filters,
    updateFilters,
    goToPage,
    changePageSize,
    refetch: fetchTransactions,
  };
};
