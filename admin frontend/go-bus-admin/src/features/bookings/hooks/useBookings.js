import { useState, useEffect, useCallback, useRef } from 'react';
import bookingService from '../services/bookingService';
import { DEFAULT_PAGE_SIZE } from 'shared/hooks/usePagination';

export const useBookings = (initialFilters = {}, initialPage = 0, initialSize = DEFAULT_PAGE_SIZE) => {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [pageSize, setPageSize] = useState(initialSize);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [filters, setFilters] = useState(initialFilters);

  // `silent` skips the loading flag so a catch-up refetch (e.g. on WebSocket
  // reconnect) refreshes the rows in place without flashing the skeleton.
  const fetchBookings = useCallback(async (silent = false) => {
    try {
      if (!silent) setLoading(true);
      setError(null);
      // Backend uses 0-based pagination — send currentPage directly.
      const response = await bookingService.filterBookings(filters, currentPage, pageSize);

      const pagedData = response.data || {};
      setBookings(pagedData.content || []);
      setTotalPages(pagedData.totalPages || 0);
      setTotalElements(pagedData.totalElements || 0);
      // Do NOT sync currentPage from the response — that would mutate a dependency
      // of this callback and trigger a second fetch on every page change.
    } catch (err) {
      console.error('Error fetching bookings:', err);
      setError(err.message || 'Failed to fetch bookings');
      setBookings([]);
    } finally {
      if (!silent) setLoading(false);
    }
  }, [filters, currentPage, pageSize]);

  useEffect(() => {
    fetchBookings();
  }, [fetchBookings]);

  // Keep a ref to the latest list so applyBookingEvent can check membership
  // without being re-created (and re-subscribing the WebSocket) on every change.
  const bookingsRef = useRef(bookings);
  useEffect(() => {
    bookingsRef.current = bookings;
  }, [bookings]);

  /**
   * Apply a real-time booking event (from the /topic/admin/bookings WebSocket).
   *
   * - If the booking is already on the current page, patch its row in place so
   *   status/payment badges update instantly without a refetch or page jump.
   * - A brand-new booking (CREATED) that isn't in view is only pulled in when the
   *   admin is on the first page (newest-first sort), so paging through older
   *   records isn't disrupted.
   */
  const applyBookingEvent = useCallback((event) => {
    if (!event) return;
    const { action, booking, bookingId } = event;
    const id = booking?.id ?? bookingId;
    const inView = bookingsRef.current.some((b) => b.id === id);

    if (inView && booking) {
      setBookings((prev) =>
        prev.map((b) => (b.id === id ? { ...b, ...booking } : b))
      );
      return;
    }

    if (action === 'CREATED' && currentPage === 0) {
      fetchBookings();
    }
  }, [currentPage, fetchBookings]);

  const updateFilters = (newFilters) => {
    setCurrentPage(0);
    setFilters(prev => ({ ...prev, ...newFilters }));
  };

  const resetFilters = () => {
    setCurrentPage(0);
    setFilters({});
  };

  const goToPage = (page) => {
    setCurrentPage(page);
  };

  const changePageSize = (size) => {
    setCurrentPage(0);
    setPageSize(size);
  };

  return {
    bookings,
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
    refetch: fetchBookings,
    applyBookingEvent,
  };
};
