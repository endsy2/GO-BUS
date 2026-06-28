import { useState, useEffect, useCallback } from 'react';
import dashboardService from '../services/dashboardService';

export const useBookingVelocity = (fromDate = null, toDate = null) => {
  const [velocityData, setVelocityData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // `silent` skips the loading flag so real-time refreshes don't flash the loader.
  const fetchVelocity = useCallback(async (silent = false) => {
    if (!silent) setLoading(true);
    setError(null);

    try {
      const params = {};
      if (fromDate) params.fromDate = fromDate;
      if (toDate) params.toDate = toDate;
      
      const response = await dashboardService.getBookingVelocity(params);
      const data = response.data?.data || response.data || [];
      
      setVelocityData(data);
    } catch (err) {
      console.error('Failed to fetch booking velocity:', err);
      setError(err.response?.data?.message || 'Failed to load booking velocity data');
      setVelocityData([]);
    } finally {
      if (!silent) setLoading(false);
    }
  }, [fromDate, toDate]);

  useEffect(() => {
    fetchVelocity();
  }, [fetchVelocity]);

  return { velocityData, loading, error, refetch: fetchVelocity };
};
