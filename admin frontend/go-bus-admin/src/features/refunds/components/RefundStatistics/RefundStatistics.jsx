import React, { useState, useEffect } from 'react';
import { TrendingUp, DollarSign, CheckCircle, XCircle, Clock } from 'lucide-react';
import { Skeleton } from 'shared/components/ui/skeleton';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import refundService from '../../services/refundService';

const RefundStatistics = ({ fromDate, toDate }) => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;

  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      if (!fromDate || !toDate) return;
      
      try {
        setLoading(true);
        const response = await refundService.getRefundStatistics(fromDate, toDate);
        setStats(response.data);
      } catch (error) {
        console.error('Error fetching refund statistics:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, [fromDate, toDate]);

  if (!fromDate || !toDate) return null;

  if (loading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-5 gap-4 mb-6">
        {[1, 2, 3, 4, 5].map(i => (
          <div key={i} className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6">
            <Skeleton className="h-4 w-20 mb-2" />
            <Skeleton className="h-8 w-24" />
          </div>
        ))}
      </div>
    );
  }

  if (!stats) return null;

  const statCards = [
    {
      label: t('totalRefunds') || 'Total Refunds',
      value: stats.totalRefunds || 0,
      icon: TrendingUp,
      color: 'blue',
    },
    {
      label: t('totalAmount') || 'Total Amount',
      value: `$${(stats.totalAmount || 0).toFixed(2)}`,
      icon: DollarSign,
      color: 'green',
    },
    {
      label: t('approved') || 'Approved',
      value: stats.approvedCount || 0,
      icon: CheckCircle,
      color: 'emerald',
    },
    {
      label: t('rejected') || 'Rejected',
      value: stats.rejectedCount || 0,
      icon: XCircle,
      color: 'red',
    },
    {
      label: t('pending') || 'Pending',
      value: stats.pendingCount || 0,
      icon: Clock,
      color: 'yellow',
    },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-5 gap-4 mb-6">
      {statCards.map((stat, index) => (
        <div
          key={index}
          className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6 hover:shadow-lg transition-shadow"
        >
          <div className="flex items-center justify-between mb-2">
            <span className="text-sm text-slate-600 dark:text-slate-400">{stat.label}</span>
            <stat.icon className={`w-5 h-5 text-${stat.color}-500`} />
          </div>
          <div className={`text-2xl font-bold text-${stat.color}-500`}>
            {stat.value}
          </div>
        </div>
      ))}
    </div>
  );
};

export default RefundStatistics;
