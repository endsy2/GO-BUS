import { useState, useEffect } from 'react';
import { reportService } from 'services';
import { Icon } from 'shared/components/common/Icon';

export const useStats = () => {
  const [stats, setStats] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const result = await reportService.getDashboardStats();
        const data = result.data || result;
        
        // Transform API data to stats format
        const statsData = [
          { 
            title: 'Total Bookings', 
            value: data.totalBookings || '1,234', 
            icon: <Icon name="ticket" size={24} color="#FFFFFF" />, 
            change: data.bookingsChange || '+12%' 
          },
          { 
            title: 'Active Buses', 
            value: data.activeBuses || '45', 
            icon: <Icon name="bus" size={24} color="#FFFFFF" />, 
            change: data.busesChange || '+3%' 
          },
          { 
            title: 'Total Revenue', 
            value: data.totalRevenue || '$45,678', 
            icon: <Icon name="dollarSign" size={24} color="#FFFFFF" />, 
            change: data.revenueChange || '+18%' 
          },
          { 
            title: 'Customers', 
            value: data.totalCustomers || '892', 
            icon: <Icon name="users" size={24} color="#FFFFFF" />, 
            change: data.customersChange || '+8%' 
          },
        ];
        setStats(statsData);
      } catch (error) {
        console.error('Error fetching stats:', error);
        // Fallback to mock data if API fails
        const mockStats = [
          { 
            title: 'Total Bookings', 
            value: '1,234', 
            icon: <Icon name="ticket" size={24} color="#FFFFFF" />, 
            change: '+12%' 
          },
          { 
            title: 'Active Buses', 
            value: '45', 
            icon: <Icon name="bus" size={24} color="#FFFFFF" />, 
            change: '+3%' 
          },
          { 
            title: 'Total Revenue', 
            value: '$45,678', 
            icon: <Icon name="dollarSign" size={24} color="#FFFFFF" />, 
            change: '+18%' 
          },
          { 
            title: 'Customers', 
            value: '892', 
            icon: <Icon name="users" size={24} color="#FFFFFF" />, 
            change: '+8%' 
          },
        ];
        setStats(mockStats);
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, []);

  return { stats, loading };
};
