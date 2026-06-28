import { useState, useEffect, useCallback } from 'react';
import { Icon } from 'shared/components/common/Icon';
import dashboardService from '../services/dashboardService';

export const useStats = (fromDate = null, toDate = null) => {
  const [stats, setStats] = useState([]);
  const [rawStats, setRawStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // `silent` skips the loading flag so real-time refreshes don't flash the
  // skeleton / full-page loader. Used by the dashboard WebSocket listener.
  const fetchStats = useCallback(async (silent = false) => {
    if (!silent) setLoading(true);
    setError(null);

    try {
      const params = {};
      if (fromDate) params.fromDate = fromDate;
      if (toDate) params.toDate = toDate;
      
      const response = await dashboardService.getDashboardStats(params);
      const data = response.data?.data || response.data;
      
      setRawStats(data);
      
      // Transform API data to stat cards format
      const transformedStats = [
        { 
          title: 'Active Bookings', 
          value: data.activeBookings?.toLocaleString() || '0', 
          icon: <Icon name="ticket" size={24} color="#FFFFFF" />, 
          change: data.todayBookings ? `+${data.todayBookings} today` : null,
          subtitle: `${data.confirmedBookings || 0} confirmed`
        },
        { 
          title: 'Available Fleet', 
          value: data.availableFleet?.toString() || '0', 
          icon: <Icon name="bus" size={24} color="#FFFFFF" />, 
          change: null,
          subtitle: 'Active buses'
        },
        { 
          title: 'Total Revenue', 
          value: `$${parseFloat(data.totalRevenue || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`, 
          icon: <Icon name="dollarSign" size={24} color="#FFFFFF" />, 
          change: data.todayRevenue ? `+$${parseFloat(data.todayRevenue).toLocaleString('en-US', { minimumFractionDigits: 2 })} today` : null,
          subtitle: data.pendingPayments ? `${data.pendingPayments} pending payments` : null
        },
        { 
          title: 'Pending Refunds', 
          value: data.pendingRefunds?.toLocaleString() || '0', 
          icon: <Icon name="alertCircle" size={24} color="#FFFFFF" />, 
          change: data.pendingRefundAmount ? `$${parseFloat(data.pendingRefundAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}` : null,
          subtitle: 'Refund amount',
          variant: 'warning'
        },
      ];
      
      setStats(transformedStats);
    } catch (err) {
      console.error('Failed to fetch dashboard stats:', err);
      setError(err.response?.data?.message || 'Failed to load dashboard statistics');
      
      // Fallback to mock data on error
      const mockStats = [
        { 
          title: 'Active Bookings', 
          value: '0', 
          icon: <Icon name="ticket" size={24} color="#FFFFFF" />, 
          change: null 
        },
        { 
          title: 'Available Fleet', 
          value: '0', 
          icon: <Icon name="bus" size={24} color="#FFFFFF" />, 
          change: null 
        },
        { 
          title: 'Total Revenue', 
          value: '$0.00', 
          icon: <Icon name="dollarSign" size={24} color="#FFFFFF" />, 
          change: null 
        },
        { 
          title: 'Pending Refunds', 
          value: '0', 
          icon: <Icon name="alertCircle" size={24} color="#FFFFFF" />, 
          change: null 
        },
      ];
      setStats(mockStats);
    } finally {
      if (!silent) setLoading(false);
    }
  }, [fromDate, toDate]);

  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  return { stats, rawStats, loading, error, refetch: fetchStats };
};
