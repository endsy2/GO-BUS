import { useState, useEffect, useCallback } from 'react';
import refundService from '../services/refundService';
import { DEFAULT_PAGE_SIZE } from 'shared/hooks/usePagination';

/**
 * useRefunds — server-side paginated refund data with filters.
 *
 * Pattern matches useBookings: all state lives as separate useState entries;
 * fetchRefunds depends on all of them via useCallback, and useEffect([fetchRefunds])
 * fires exactly once per dependency change — no stale-closure issues.
 */
export const useRefunds = (initialFilters = {}, initialPage = 0, initialSize = DEFAULT_PAGE_SIZE) => {
  const [refunds, setRefunds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [pageSize, setPageSize] = useState(initialSize);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [filters, setFilters] = useState(initialFilters);

  const fetchRefunds = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      let response;
      if (filters.fromDate && filters.toDate && filters.status) {
        response = await refundService.getRefundsByDateRange(
          filters.status,
          filters.fromDate,
          filters.toDate,
          currentPage,
          pageSize
        );
      } else {
        response = await refundService.getAllRefunds(filters.status, currentPage, pageSize);
      }

      const pagedData = response.data || {};
      setRefunds(pagedData.content || []);
      setTotalPages(pagedData.totalPages || 0);
      setTotalElements(pagedData.totalElements || 0);
      // Do NOT sync currentPage from response — that causes a second fetch on every page change.
    } catch (err) {
      console.error('Error fetching refunds:', err);
      setError(err.message || 'Failed to fetch refunds');
      setRefunds([]);
    } finally {
      setLoading(false);
    }
  }, [filters, currentPage, pageSize]);

  useEffect(() => {
    fetchRefunds();
  }, [fetchRefunds]);

  const updateFilters = (newFilters) => {
    setCurrentPage(0);
    setFilters(prev => ({ ...prev, ...newFilters }));
  };

  const resetFilters = () => {
    setCurrentPage(0);
    setFilters({});
  };

  const goToPage = (page) => setCurrentPage(page);

  const changePageSize = (size) => {
    setCurrentPage(0);
    setPageSize(size);
  };

  return {
    refunds,
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
    resetFilters,
    goToPage,
    changePageSize,
    refetch: fetchRefunds,
  };
};
