import React, { useState, useMemo, useCallback, useEffect, useRef } from 'react';
import { Calendar, TrendingUp, Bus, AlertCircle, Download, TrendingDown, ChevronDown, Check, Wifi, WifiOff } from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { useStats } from '../../hooks/useStats';
import { useBookingVelocity } from '../../hooks/useBookingVelocity';
import { useRevenueStream } from '../../hooks/useRevenueStream';
import { useDashboardRealtime } from '../../hooks/useDashboardRealtime';
import { Skeleton } from 'shared/components/ui/skeleton';
import { Button } from 'shared/components/ui/button';
import { Badge } from 'shared/components/ui/badge';
import { Popover, PopoverContent, PopoverTrigger } from 'shared/components/ui/popover';

const DashboardPage = () => {
  // Date range state
  const [dateRange, setDateRange] = useState('30days');
  const [dateRangeOpen, setDateRangeOpen] = useState(false);
  
  // Calculate date range based on selection using useMemo to prevent infinite loops
  const { fromDate, toDate } = useMemo(() => {
    const now = new Date();
    const toDate = now.toISOString();
    let fromDate;
    
    const fromDateCalc = new Date();
    
    switch (dateRange) {
      case '30days':
        fromDateCalc.setDate(fromDateCalc.getDate() - 30);
        fromDate = fromDateCalc.toISOString();
        break;
      case '2months':
        fromDateCalc.setMonth(fromDateCalc.getMonth() - 2);
        fromDate = fromDateCalc.toISOString();
        break;
      case '3months':
        fromDateCalc.setMonth(fromDateCalc.getMonth() - 3);
        fromDate = fromDateCalc.toISOString();
        break;
      case '6months':
        fromDateCalc.setMonth(fromDateCalc.getMonth() - 6);
        fromDate = fromDateCalc.toISOString();
        break;
      case '12months':
        fromDateCalc.setMonth(fromDateCalc.getMonth() - 12);
        fromDate = fromDateCalc.toISOString();
        break;
      default:
        fromDateCalc.setDate(fromDateCalc.getDate() - 30);
        fromDate = fromDateCalc.toISOString();
    }
    
    return { fromDate, toDate };
  }, [dateRange]);
  
  const { rawStats, loading: statsLoading, refetch: refetchStats } = useStats(fromDate, toDate);
  const { velocityData, loading: velocityLoading, refetch: refetchVelocity } = useBookingVelocity(fromDate, toDate);
  const { revenueStream, loading: revenueLoading, refetch: refetchRevenue } = useRevenueStream(fromDate, toDate);

  const isLoading = statsLoading || velocityLoading || revenueLoading;

  // ── Real-time dashboard updates ────────────────────────────────────────────
  // On each dashboard event, silently re-pull all three datasets (silent=true
  // skips the loading skeleton so cards/charts update in place, no refresh).
  const handleDashboardUpdate = useCallback(() => {
    refetchStats(true);
    refetchVelocity(true);
    refetchRevenue(true);
  }, [refetchStats, refetchVelocity, refetchRevenue]);

  const { isConnected: liveConnected } = useDashboardRealtime(handleDashboardUpdate);

  // Catch-up on (re)connect: the simple broker doesn't replay events missed
  // while the socket was down, so re-pull all datasets whenever the dashboard
  // connection (re)establishes. Initial connect is skipped — the hooks already
  // do their first fetch on mount.
  const prevLiveConnected = useRef(liveConnected);
  useEffect(() => {
    if (liveConnected && !prevLiveConnected.current) {
      handleDashboardUpdate();
    }
    prevLiveConnected.current = liveConnected;
  }, [liveConnected, handleDashboardUpdate]);
  
  // Date range options
  const dateRangeOptions = [
    { value: '30days', label: 'Last 30 Days' },
    { value: '2months', label: 'Last 2 Months' },
    { value: '3months', label: 'Last 3 Months' },
    { value: '6months', label: 'Last 6 Months' },
    { value: '12months', label: 'Last 12 Months' },
  ];
  
  const selectedOption = dateRangeOptions.find(opt => opt.value === dateRange);

  // Format data for Recharts
  const chartData = velocityData.map(day => ({
    date: new Date(day.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
    Seater: day.seaterBookings || 0,
    Sleeper: day.sleeperBookings || 0,
    seaterRevenue: day.seaterRevenue || 0,
    sleeperRevenue: day.sleeperRevenue || 0,
    seaterPercentage: day.seaterPercentage || 0,
    sleeperPercentage: day.sleeperPercentage || 0,
    fullDate: day.date
  }));

  // Calculate growth for each seat type (comparing last day vs previous period average)
  const seatTypeGrowth = velocityData.length > 1 ? (() => {
    const lastDay = velocityData[velocityData.length - 1];
    const previousDays = velocityData.slice(0, -1);
    
    const avgSeater = previousDays.reduce((sum, day) => sum + (day.seaterBookings || 0), 0) / previousDays.length;
    const avgSleeper = previousDays.reduce((sum, day) => sum + (day.sleeperBookings || 0), 0) / previousDays.length;
    
    const seaterGrowth = avgSeater > 0 ? (((lastDay.seaterBookings || 0) - avgSeater) / avgSeater * 100) : 0;
    const sleeperGrowth = avgSleeper > 0 ? (((lastDay.sleeperBookings || 0) - avgSleeper) / avgSleeper * 100) : 0;
    
    return {
      seater: { value: seaterGrowth, bookings: lastDay.seaterBookings || 0 },
      sleeper: { value: sleeperGrowth, bookings: lastDay.sleeperBookings || 0 }
    };
  })() : null;

  // Payment method colors mapping
  const paymentMethodColors = {
    'Bakong': 'bg-blue-500',
    'Card': 'bg-green-500',
    'Wallet': 'bg-purple-500',
    'Cash': 'bg-yellow-500',
    'Bank Transfer': 'bg-indigo-500'
  };

  // Custom tooltip for Recharts
  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      // Find the original data point to get revenue and percentage
      const dataPoint = chartData.find(d => d.date === label);
      
      return (
        <div className="bg-popover text-popover-foreground text-xs rounded-md px-3 py-2 shadow-md border border-border">
          <div className="font-medium mb-2">{label}</div>
          {payload.map((entry, index) => {
            const isSeater = entry.name === 'Seater';
            const revenue = isSeater ? dataPoint?.seaterRevenue : dataPoint?.sleeperRevenue;
            const percentage = isSeater ? dataPoint?.seaterPercentage : dataPoint?.sleeperPercentage;

            return (
              <div key={index} className="mb-1">
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 rounded-full" style={{ backgroundColor: entry.color }}></div>
                  <span className="font-medium">{entry.name}</span>
                </div>
                <div className="ml-4 text-muted-foreground">
                  <div>Count: {entry.value}</div>
                  <div>Revenue: ${(revenue || 0).toFixed(2)}</div>
                  <div>Share: {(percentage || 0).toFixed(1)}%</div>
                </div>
              </div>
            );
          })}
        </div>
      );
    }
    return null;
  };

  if (isLoading) {
    return (
      <div className="flex-1 p-4 sm:p-6 overflow-y-auto bg-background min-h-screen">
        <div className="mb-4 sm:mb-6">
          <Skeleton className="h-7 sm:h-8 w-48 sm:w-64 mb-2" />
          <Skeleton className="h-4 w-64 sm:w-96" />
        </div>
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4 mb-4 sm:mb-6">
          {[1, 2, 3, 4].map(i => (
            <Skeleton key={i} className="h-32 rounded-xl" />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="flex-1 p-4 sm:p-6 overflow-y-auto bg-background min-h-screen">
      {/* Header */}
      <div className="mb-4 sm:mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div>
          <h1 className="text-xl sm:text-2xl font-semibold text-foreground mb-1 flex items-center gap-2">
            <span>Operational Overview</span>
            {liveConnected ? (
              <span
                title="Real-time updates connected"
                className="inline-flex items-center gap-1.5 rounded-full border border-green-200 bg-green-50 px-2 py-0.5 text-xs font-medium text-green-700 dark:border-green-900/50 dark:bg-green-900/20 dark:text-green-400"
              >
                <span className="relative flex h-2 w-2">
                  <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-green-500 opacity-75"></span>
                  <span className="relative inline-flex h-2 w-2 rounded-full bg-green-500"></span>
                </span>
                <Wifi className="w-3 h-3" />
                <span>Live</span>
              </span>
            ) : (
              <span
                title="Real-time updates disconnected"
                className="inline-flex items-center gap-1.5 rounded-full border border-border bg-muted px-2 py-0.5 text-xs font-medium text-muted-foreground"
              >
                <span className="inline-flex h-2 w-2 rounded-full bg-muted-foreground/50"></span>
                <WifiOff className="w-3 h-3" />
                <span>Offline</span>
              </span>
            )}
          </h1>
          {/* <p className="text-xs sm:text-sm text-muted-foreground">
            Real-time performance and system health across the FTA network.
          </p> */}
        </div>
        <div className="flex gap-3">
          {/* Date Range Selector */}
          <Popover open={dateRangeOpen} onOpenChange={setDateRangeOpen}>
            <PopoverTrigger asChild>
              <Button
                variant="outline"
                className="w-full sm:min-w-[160px] justify-between"
              >
                <div className="flex items-center gap-2">
                  <Calendar className="w-4 h-4" />
                  <span>{selectedOption?.label || 'Last 30 Days'}</span>
                </div>
                <ChevronDown className="w-4 h-4 ml-2 opacity-50" />
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-56 p-2">
              <div className="space-y-1">
                {dateRangeOptions.map((option) => (
                  <button
                    key={option.value}
                    onClick={() => {
                      setDateRange(option.value);
                      setDateRangeOpen(false);
                    }}
                    className={`w-full flex items-center justify-between px-3 py-2 text-sm rounded-md transition-colors ${
                      dateRange === option.value
                        ? 'bg-primary/10 text-primary'
                        : 'text-foreground hover:bg-muted'
                    }`}
                  >
                    <span>{option.label}</span>
                    {dateRange === option.value && (
                      <Check className="w-4 h-4" />
                    )}
                  </button>
                ))}
              </div>
            </PopoverContent>
          </Popover>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4 mb-4 sm:mb-6">
        {/* Active Bookings */}
        <div className="bg-card rounded-lg p-3 sm:p-5 border border-border">
          <div className="flex items-start justify-between mb-2 sm:mb-3">
            <div className="w-8 h-8 sm:w-9 sm:h-9 rounded-md bg-blue-50 dark:bg-blue-900/20 flex items-center justify-center">
              <Calendar className="w-4 h-4 sm:w-5 sm:h-5 text-blue-600 dark:text-blue-400" />
            </div>
          </div>
          <div className="text-xs sm:text-sm text-muted-foreground mb-1">Active Bookings</div>
          <div className="text-xl sm:text-2xl font-semibold text-foreground">
            {rawStats?.activeBookings?.toLocaleString() || '0'}
          </div>
          {rawStats?.confirmedBookings > 0 && (
            <div className="text-xs text-muted-foreground mt-1">
              {rawStats.confirmedBookings} confirmed
            </div>
          )}
        </div>

        {/* Total Revenue */}
        <div className="bg-card rounded-lg p-3 sm:p-5 border border-border">
          <div className="flex items-start justify-between mb-2 sm:mb-3">
            <div className="w-8 h-8 sm:w-9 sm:h-9 rounded-md bg-green-50 dark:bg-green-900/20 flex items-center justify-center">
              <TrendingUp className="w-4 h-4 sm:w-5 sm:h-5 text-green-600 dark:text-green-400" />
            </div>
            {rawStats?.todayRevenue > 0 && (
              <span className="text-xs font-medium text-green-600 dark:text-green-400">
                +${parseFloat(rawStats.todayRevenue).toLocaleString('en-US', { minimumFractionDigits: 2 })} today
              </span>
            )}
          </div>
          <div className="text-xs sm:text-sm text-muted-foreground mb-1">Total Revenue</div>
          <div className="text-xl sm:text-2xl font-semibold text-foreground">
            ${parseFloat(rawStats?.totalRevenue || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
          {rawStats?.pendingPayments > 0 && (
            <div className="text-xs text-muted-foreground mt-1">
              {rawStats.pendingPayments} pending payments
            </div>
          )}
        </div>

        {/* Available Fleet */}
        <div className="bg-card rounded-lg p-3 sm:p-5 border border-border">
          <div className="flex items-start justify-between mb-2 sm:mb-3">
            <div className="w-8 h-8 sm:w-9 sm:h-9 rounded-md bg-blue-50 dark:bg-blue-900/20 flex items-center justify-center">
              <Bus className="w-4 h-4 sm:w-5 sm:h-5 text-blue-600 dark:text-blue-400" />
            </div>
          </div>
          <div className="text-xs sm:text-sm text-muted-foreground mb-1">Available Fleet</div>
          <div className="text-xl sm:text-2xl font-semibold text-foreground">
            {rawStats?.availableFleet || '0'}
          </div>
          <div className="text-xs text-muted-foreground mt-1">
            Active buses
          </div>
        </div>

        {/* Pending Refunds */}
        <div className="bg-card rounded-lg p-3 sm:p-5 border border-border">
          <div className="flex items-start justify-between mb-2 sm:mb-3">
            <div className="w-8 h-8 sm:w-9 sm:h-9 rounded-md bg-red-50 dark:bg-red-900/20 flex items-center justify-center">
              <AlertCircle className="w-4 h-4 sm:w-5 sm:h-5 text-red-600 dark:text-red-400" />
            </div>
          </div>
          <div className="text-xs sm:text-sm text-muted-foreground mb-1">Pending Refunds</div>
          <div className="text-xl sm:text-2xl font-semibold text-foreground">
            {rawStats?.pendingRefunds?.toLocaleString() || '0'}
          </div>
          {rawStats?.pendingRefundAmount > 0 && (
            <div className="text-xs text-red-600 dark:text-red-400 mt-1">
              ${parseFloat(rawStats.pendingRefundAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })} amount
            </div>
          )}
        </div>
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 sm:gap-6 mb-4 sm:mb-6">
        {/* Booking Trends - Takes 2 columns */}
        <div className="lg:col-span-2 bg-card rounded-lg p-4 sm:p-6 border border-border">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-4 sm:mb-6">
            <div>
              <h3 className="text-sm sm:text-base font-semibold text-foreground">Booking Trends</h3>
              <p className="text-xs sm:text-sm text-muted-foreground">
                {velocityData.length > 0
                  ? `Ticket sales velocity (${velocityData.length} days)`
                  : 'Ticket sales velocity'}
              </p>
            </div>
            <div className="flex gap-3 sm:gap-4 flex-wrap">
              <div className="flex items-center gap-2">
                <div className="w-2 h-2 rounded-full bg-emerald-500"></div>
                <span className="text-xs text-gray-600 dark:text-gray-400">Seater</span>
                {seatTypeGrowth && seatTypeGrowth.seater.bookings > 0 && (
                  <Badge 
                    variant={seatTypeGrowth.seater.value >= 0 ? "default" : "destructive"}
                    className="text-[10px] px-1.5 py-0 h-4"
                  >
                    {seatTypeGrowth.seater.value >= 0 ? (
                      <TrendingUp className="w-2.5 h-2.5 mr-0.5" />
                    ) : (
                      <TrendingDown className="w-2.5 h-2.5 mr-0.5" />
                    )}
                    {Math.abs(seatTypeGrowth.seater.value).toFixed(0)}%
                  </Badge>
                )}
              </div>
              <div className="flex items-center gap-2">
                <div className="w-2 h-2 rounded-full bg-purple-500"></div>
                <span className="text-xs text-gray-600 dark:text-gray-400">Sleeper</span>
                {seatTypeGrowth && seatTypeGrowth.sleeper.bookings > 0 && (
                  <Badge 
                    variant={seatTypeGrowth.sleeper.value >= 0 ? "default" : "destructive"}
                    className="text-[10px] px-1.5 py-0 h-4"
                  >
                    {seatTypeGrowth.sleeper.value >= 0 ? (
                      <TrendingUp className="w-2.5 h-2.5 mr-0.5" />
                    ) : (
                      <TrendingDown className="w-2.5 h-2.5 mr-0.5" />
                    )}
                    {Math.abs(seatTypeGrowth.sleeper.value).toFixed(0)}%
                  </Badge>
                )}
              </div>
            </div>
          </div>

          {/* Recharts Multi-line Chart */}
          <div className="h-64">
            {chartData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={chartData} margin={{ top: 5, right: 10, left: 0, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" className="stroke-gray-200 dark:stroke-gray-700" />
                  <XAxis 
                    dataKey="date" 
                    className="text-xs"
                    tick={{ fill: 'currentColor', className: 'text-gray-600 dark:text-gray-400' }}
                  />
                  <YAxis 
                    className="text-xs"
                    tick={{ fill: 'currentColor', className: 'text-gray-600 dark:text-gray-400' }}
                  />
                  <Tooltip content={<CustomTooltip />} />
                  <Line 
                    type="monotone" 
                    dataKey="Seater" 
                    stroke="#10b981" 
                    strokeWidth={2}
                    dot={{ fill: '#10b981', r: 4 }}
                    activeDot={{ r: 6 }}
                  />
                  <Line 
                    type="monotone" 
                    dataKey="Sleeper" 
                    stroke="#a855f7" 
                    strokeWidth={2}
                    dot={{ fill: '#a855f7', r: 4 }}
                    activeDot={{ r: 6 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            ) : (
              <div className="w-full h-full flex items-center justify-center text-gray-400">
                No booking data available
              </div>
            )}
          </div>
        </div>

        {/* Revenue Stream */}
        <div className="bg-card rounded-lg p-4 sm:p-6 border border-border">
          <div className="mb-4">
            <h3 className="text-sm sm:text-base font-semibold text-foreground">Revenue Stream</h3>
            <p className="text-xs sm:text-sm text-muted-foreground">Split by Payment Method</p>
          </div>
          <div className="mb-6 rounded-lg border border-border bg-muted/30 p-4">
            <div className="text-xs font-medium text-muted-foreground uppercase tracking-wide mb-1">Weekly Total</div>
            <div className="text-2xl font-bold text-foreground">
              ${revenueStream?.weeklyTotal
                ? parseFloat(revenueStream.weeklyTotal).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
                : '0.00'}
            </div>
          </div>
          <div className="space-y-3">
            {revenueStream?.paymentMethods && revenueStream.paymentMethods.length > 0 ? (
              revenueStream.paymentMethods.map((item, index) => (
                <div key={index} className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div className={`w-2 h-2 rounded-full ${paymentMethodColors[item.method] || 'bg-gray-500'}`}></div>
                    <span className="text-sm text-gray-600 dark:text-gray-400">{item.method}</span>
                  </div>
                  <span className="text-sm font-semibold text-gray-900 dark:text-white">
                    {item.percentage ? `${item.percentage.toFixed(1)}%` : '0%'}
                  </span>
                </div>
              ))
            ) : (
              <div className="text-center text-sm text-gray-400 py-4">
                No revenue data available
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
