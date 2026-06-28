import React, { useState } from 'react';
import { Download, Bus, Activity, AlertTriangle, BarChart3 } from 'lucide-react';
import { Button } from 'shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from 'shared/components/ui/card';
import { Input } from 'shared/components/common/Input';
import { DatePicker } from 'shared/components/ui/date-picker';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'shared/components/ui/select';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import { useToast } from 'shared/components/ui/toast';
import reportService from '../../services/reportService';

const BusReportsTab = () => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const { addToast } = useToast();

  // Bus Utilization Report State
  const [utilizationLoading, setUtilizationLoading] = useState(false);
  const [utilizationFilters, setUtilizationFilters] = useState({
    startDate: '',
    endDate: ''
  });

  // Route Performance Report State
  const [performanceLoading, setPerformanceLoading] = useState(false);
  const [performanceFilters, setPerformanceFilters] = useState({
    startDate: '',
    endDate: ''
  });

  // Inactive Bus Report State
  const [inactiveBusLoading, setInactiveBusLoading] = useState(false);
  const [inactiveBusFilters, setInactiveBusFilters] = useState({
    daysThreshold: '30'
  });

  // Inactive Route Report State
  const [inactiveRouteLoading, setInactiveRouteLoading] = useState(false);
  const [inactiveRouteFilters, setInactiveRouteFilters] = useState({
    daysThreshold: '30'
  });

  // Bus Capacity Analysis Report State
  const [capacityLoading, setCapacityLoading] = useState(false);
  const [capacityFilters, setCapacityFilters] = useState({
    startDate: '',
    endDate: ''
  });

  // ========== Bus Utilization Report ==========
  const handleDownloadUtilizationReport = async () => {
    if (!utilizationFilters.startDate || !utilizationFilters.endDate) {
      addToast({ message: 'Please select start and end dates', type: 'error' });
      return;
    }

    setUtilizationLoading(true);
    try {
      const blob = await reportService.getBusUtilizationReport(
        utilizationFilters.startDate,
        utilizationFilters.endDate
      );
      
      const filename = `bus_utilization_report_${utilizationFilters.startDate}_to_${utilizationFilters.endDate}.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Bus utilization report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading bus utilization report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download bus utilization report', type: 'error' });
    } finally {
      setUtilizationLoading(false);
    }
  };

  // ========== Route Performance Report ==========
  const handleDownloadPerformanceReport = async () => {
    if (!performanceFilters.startDate || !performanceFilters.endDate) {
      addToast({ message: 'Please select start and end dates', type: 'error' });
      return;
    }

    setPerformanceLoading(true);
    try {
      const blob = await reportService.getRoutePerformanceReport(
        performanceFilters.startDate,
        performanceFilters.endDate
      );
      
      const filename = `route_performance_report_${performanceFilters.startDate}_to_${performanceFilters.endDate}.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Route performance report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading route performance report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download route performance report', type: 'error' });
    } finally {
      setPerformanceLoading(false);
    }
  };

  // ========== Inactive Bus Report ==========
  const handleDownloadInactiveBusReport = async () => {
    setInactiveBusLoading(true);
    try {
      const blob = await reportService.getInactiveBusReport(
        parseInt(inactiveBusFilters.daysThreshold)
      );
      
      const filename = `inactive_bus_report_${inactiveBusFilters.daysThreshold}_days.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Inactive bus report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading inactive bus report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download inactive bus report', type: 'error' });
    } finally {
      setInactiveBusLoading(false);
    }
  };

  // ========== Inactive Route Report ==========
  const handleDownloadInactiveRouteReport = async () => {
    setInactiveRouteLoading(true);
    try {
      const blob = await reportService.getInactiveRouteReport(
        parseInt(inactiveRouteFilters.daysThreshold)
      );
      
      const filename = `inactive_route_report_${inactiveRouteFilters.daysThreshold}_days.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Inactive route report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading inactive route report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download inactive route report', type: 'error' });
    } finally {
      setInactiveRouteLoading(false);
    }
  };

  // ========== Bus Capacity Analysis Report ==========
  const handleDownloadCapacityReport = async () => {
    if (!capacityFilters.startDate || !capacityFilters.endDate) {
      addToast({ message: 'Please select start and end dates', type: 'error' });
      return;
    }

    setCapacityLoading(true);
    try {
      const blob = await reportService.getBusCapacityAnalysisReport(
        capacityFilters.startDate,
        capacityFilters.endDate
      );
      
      const filename = `bus_capacity_analysis_${capacityFilters.startDate}_to_${capacityFilters.endDate}.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Bus capacity analysis report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading capacity analysis report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download capacity analysis report', type: 'error' });
    } finally {
      setCapacityLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Bus Utilization Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-blue-500 flex items-center justify-center text-white">
              <Bus className="w-5 h-5" />
            </div>
            Bus Utilization Report
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Analyze how efficiently buses are being utilized across all routes
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Start Date
              </label>
              <DatePicker
                value={utilizationFilters.startDate}
                onChange={(value) => setUtilizationFilters({ ...utilizationFilters, startDate: value })}
                placeholder="Select Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                End Date
              </label>
              <DatePicker
                value={utilizationFilters.endDate}
                onChange={(value) => setUtilizationFilters({ ...utilizationFilters, endDate: value })}
                placeholder="Select Date"
              />
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleDownloadUtilizationReport}
                disabled={utilizationLoading}
                className="w-full gap-2"
              >
                <Download className="w-4 h-4" />
                {utilizationLoading ? 'Generating...' : 'Download Excel'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Route Performance Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-green-500 flex items-center justify-center text-white">
              <Activity className="w-5 h-5" />
            </div>
            Route Performance Report
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Evaluate route performance metrics including revenue and occupancy rates
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Start Date
              </label>
              <DatePicker
                value={performanceFilters.startDate}
                onChange={(value) => setPerformanceFilters({ ...performanceFilters, startDate: value })}
                placeholder="Select Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                End Date
              </label>
              <DatePicker
                value={performanceFilters.endDate}
                onChange={(value) => setPerformanceFilters({ ...performanceFilters, endDate: value })}
                placeholder="Select Date"
              />
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleDownloadPerformanceReport}
                disabled={performanceLoading}
                className="w-full gap-2"
              >
                <Download className="w-4 h-4" />
                {performanceLoading ? 'Generating...' : 'Download Excel'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Inactive Bus Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-red-500 flex items-center justify-center text-white">
              <AlertTriangle className="w-5 h-5" />
            </div>
            Inactive Bus Report
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Identify buses that have been inactive for a specified number of days
          </p>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Inactive Days Threshold
              </label>
              <Select
                value={inactiveBusFilters.daysThreshold}
                onValueChange={(value) => setInactiveBusFilters({ ...inactiveBusFilters, daysThreshold: value })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="7">7 Days</SelectItem>
                  <SelectItem value="14">14 Days</SelectItem>
                  <SelectItem value="30">30 Days</SelectItem>
                  <SelectItem value="60">60 Days</SelectItem>
                  <SelectItem value="90">90 Days</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleDownloadInactiveBusReport}
                disabled={inactiveBusLoading}
                className="w-full gap-2"
              >
                <Download className="w-4 h-4" />
                {inactiveBusLoading ? 'Generating...' : 'Download Excel'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Inactive Route Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-orange-500 flex items-center justify-center text-white">
              <AlertTriangle className="w-5 h-5" />
            </div>
            Inactive Route Report
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Identify routes that have been inactive for a specified number of days
          </p>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Inactive Days Threshold
              </label>
              <Select
                value={inactiveRouteFilters.daysThreshold}
                onValueChange={(value) => setInactiveRouteFilters({ ...inactiveRouteFilters, daysThreshold: value })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="7">7 Days</SelectItem>
                  <SelectItem value="14">14 Days</SelectItem>
                  <SelectItem value="30">30 Days</SelectItem>
                  <SelectItem value="60">60 Days</SelectItem>
                  <SelectItem value="90">90 Days</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleDownloadInactiveRouteReport}
                disabled={inactiveRouteLoading}
                className="w-full gap-2"
              >
                <Download className="w-4 h-4" />
                {inactiveRouteLoading ? 'Generating...' : 'Download Excel'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Bus Capacity Analysis Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-indigo-500 flex items-center justify-center text-white">
              <BarChart3 className="w-5 h-5" />
            </div>
            Bus Capacity Analysis Report
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Analyze bus capacity utilization and identify optimization opportunities
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Start Date
              </label>
              <DatePicker
                value={capacityFilters.startDate}
                onChange={(value) => setCapacityFilters({ ...capacityFilters, startDate: value })}
                placeholder="Select Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                End Date
              </label>
              <DatePicker
                value={capacityFilters.endDate}
                onChange={(value) => setCapacityFilters({ ...capacityFilters, endDate: value })}
                placeholder="Select Date"
              />
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleDownloadCapacityReport}
                disabled={capacityLoading}
                className="w-full gap-2"
              >
                <Download className="w-4 h-4" />
                {capacityLoading ? 'Generating...' : 'Download Excel'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default BusReportsTab;
