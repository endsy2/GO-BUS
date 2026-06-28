import React, { useState } from 'react';
import { Download, Users, Award, PieChart, Calendar, Clock } from 'lucide-react';
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

const CustomerReportsTab = () => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const { addToast } = useToast();

  // Active Customer Report State
  const [activeCustomerLoading, setActiveCustomerLoading] = useState(false);
  const [activeCustomerFilters, setActiveCustomerFilters] = useState({
    startDate: '',
    endDate: ''
  });

  // Frequent Traveler Report State
  const [frequentTravelerLoading, setFrequentTravelerLoading] = useState(false);
  const [frequentTravelerFilters, setFrequentTravelerFilters] = useState({
    startDate: '',
    endDate: '',
    limit: '50'
  });

  // Customer Demographics Report State
  const [demographicsLoading, setDemographicsLoading] = useState(false);
  const [demographicsFilters, setDemographicsFilters] = useState({
    startDate: '',
    endDate: ''
  });

  // Booking Patterns by Day Report State
  const [patternsByDayLoading, setPatternsByDayLoading] = useState(false);
  const [patternsByDayFilters, setPatternsByDayFilters] = useState({
    startDate: '',
    endDate: ''
  });

  // Booking Patterns by Hour Report State
  const [patternsByHourLoading, setPatternsByHourLoading] = useState(false);
  const [patternsByHourFilters, setPatternsByHourFilters] = useState({
    startDate: '',
    endDate: ''
  });

  // ========== Active Customer Report ==========
  const handleDownloadActiveCustomerReport = async () => {
    if (!activeCustomerFilters.startDate || !activeCustomerFilters.endDate) {
      addToast({ message: 'Please select start and end dates', type: 'error' });
      return;
    }

    setActiveCustomerLoading(true);
    try {
      const blob = await reportService.getActiveCustomerReport(
        activeCustomerFilters.startDate,
        activeCustomerFilters.endDate
      );
      
      const filename = `active_customer_report_${activeCustomerFilters.startDate}_to_${activeCustomerFilters.endDate}.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Active customer report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading active customer report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download active customer report', type: 'error' });
    } finally {
      setActiveCustomerLoading(false);
    }
  };

  // ========== Frequent Traveler Report ==========
  const handleDownloadFrequentTravelerReport = async () => {
    if (!frequentTravelerFilters.startDate || !frequentTravelerFilters.endDate) {
      addToast({ message: 'Please select start and end dates', type: 'error' });
      return;
    }

    setFrequentTravelerLoading(true);
    try {
      const blob = await reportService.getFrequentTravelerReport(
        frequentTravelerFilters.startDate,
        frequentTravelerFilters.endDate,
        parseInt(frequentTravelerFilters.limit)
      );
      
      const filename = `frequent_traveler_report_${frequentTravelerFilters.startDate}_to_${frequentTravelerFilters.endDate}.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Frequent traveler report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading frequent traveler report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download frequent traveler report', type: 'error' });
    } finally {
      setFrequentTravelerLoading(false);
    }
  };

  // ========== Customer Demographics Report ==========
  const handleDownloadDemographicsReport = async () => {
    if (!demographicsFilters.startDate || !demographicsFilters.endDate) {
      addToast({ message: 'Please select start and end dates', type: 'error' });
      return;
    }

    setDemographicsLoading(true);
    try {
      const blob = await reportService.getCustomerDemographicsReport(
        demographicsFilters.startDate,
        demographicsFilters.endDate
      );
      
      const filename = `customer_demographics_report_${demographicsFilters.startDate}_to_${demographicsFilters.endDate}.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Customer demographics report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading demographics report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download demographics report', type: 'error' });
    } finally {
      setDemographicsLoading(false);
    }
  };

  // ========== Booking Patterns by Day Report ==========
  const handleDownloadPatternsByDayReport = async () => {
    if (!patternsByDayFilters.startDate || !patternsByDayFilters.endDate) {
      addToast({ message: 'Please select start and end dates', type: 'error' });
      return;
    }

    setPatternsByDayLoading(true);
    try {
      const blob = await reportService.getBookingPatternsByDayReport(
        patternsByDayFilters.startDate,
        patternsByDayFilters.endDate
      );
      
      const filename = `booking_patterns_by_day_${patternsByDayFilters.startDate}_to_${patternsByDayFilters.endDate}.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Booking patterns by day report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading patterns by day report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download patterns by day report', type: 'error' });
    } finally {
      setPatternsByDayLoading(false);
    }
  };

  // ========== Booking Patterns by Hour Report ==========
  const handleDownloadPatternsByHourReport = async () => {
    if (!patternsByHourFilters.startDate || !patternsByHourFilters.endDate) {
      addToast({ message: 'Please select start and end dates', type: 'error' });
      return;
    }

    setPatternsByHourLoading(true);
    try {
      const blob = await reportService.getBookingPatternsByHourReport(
        patternsByHourFilters.startDate,
        patternsByHourFilters.endDate
      );
      
      const filename = `booking_patterns_by_hour_${patternsByHourFilters.startDate}_to_${patternsByHourFilters.endDate}.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Booking patterns by hour report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading patterns by hour report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download patterns by hour report', type: 'error' });
    } finally {
      setPatternsByHourLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Active Customer Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-green-500 flex items-center justify-center text-white">
              <Users className="w-5 h-5" />
            </div>
            Active Customer Report
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Identify active customers who made bookings during the specified period
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Start Date
              </label>
              <DatePicker
                value={activeCustomerFilters.startDate}
                onChange={(value) => setActiveCustomerFilters({ ...activeCustomerFilters, startDate: value })}
                placeholder="Select Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                End Date
              </label>
              <DatePicker
                value={activeCustomerFilters.endDate}
                onChange={(value) => setActiveCustomerFilters({ ...activeCustomerFilters, endDate: value })}
                placeholder="Select Date"
              />
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleDownloadActiveCustomerReport}
                disabled={activeCustomerLoading}
                className="w-full gap-2"
              >
                <Download className="w-4 h-4" />
                {activeCustomerLoading ? 'Generating...' : 'Download Excel'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Frequent Traveler Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-yellow-500 flex items-center justify-center text-white">
              <Award className="w-5 h-5" />
            </div>
            Frequent Traveler Report
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Identify top frequent travelers based on booking count
          </p>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Start Date
              </label>
              <DatePicker
                value={frequentTravelerFilters.startDate}
                onChange={(value) => setFrequentTravelerFilters({ ...frequentTravelerFilters, startDate: value })}
                placeholder="Select Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                End Date
              </label>
              <DatePicker
                value={frequentTravelerFilters.endDate}
                onChange={(value) => setFrequentTravelerFilters({ ...frequentTravelerFilters, endDate: value })}
                placeholder="Select Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Top Travelers
              </label>
              <Select
                value={frequentTravelerFilters.limit}
                onValueChange={(value) => setFrequentTravelerFilters({ ...frequentTravelerFilters, limit: value })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="25">Top 25</SelectItem>
                  <SelectItem value="50">Top 50</SelectItem>
                  <SelectItem value="100">Top 100</SelectItem>
                  <SelectItem value="200">Top 200</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleDownloadFrequentTravelerReport}
                disabled={frequentTravelerLoading}
                className="w-full gap-2"
              >
                <Download className="w-4 h-4" />
                {frequentTravelerLoading ? 'Generating...' : 'Download Excel'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Customer Demographics Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-purple-500 flex items-center justify-center text-white">
              <PieChart className="w-5 h-5" />
            </div>
            Customer Demographics Report
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Analyze customer demographics and segmentation data
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Start Date
              </label>
              <DatePicker
                value={demographicsFilters.startDate}
                onChange={(value) => setDemographicsFilters({ ...demographicsFilters, startDate: value })}
                placeholder="Select Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                End Date
              </label>
              <DatePicker
                value={demographicsFilters.endDate}
                onChange={(value) => setDemographicsFilters({ ...demographicsFilters, endDate: value })}
                placeholder="Select Date"
              />
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleDownloadDemographicsReport}
                disabled={demographicsLoading}
                className="w-full gap-2"
              >
                <Download className="w-4 h-4" />
                {demographicsLoading ? 'Generating...' : 'Download Excel'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Booking Patterns by Day Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-cyan-500 flex items-center justify-center text-white">
              <Calendar className="w-5 h-5" />
            </div>
            Booking Patterns by Day of Week
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Analyze booking patterns by day of the week to identify peak days
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Start Date
              </label>
              <DatePicker
                value={patternsByDayFilters.startDate}
                onChange={(value) => setPatternsByDayFilters({ ...patternsByDayFilters, startDate: value })}
                placeholder="Select Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                End Date
              </label>
              <DatePicker
                value={patternsByDayFilters.endDate}
                onChange={(value) => setPatternsByDayFilters({ ...patternsByDayFilters, endDate: value })}
                placeholder="Select Date"
              />
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleDownloadPatternsByDayReport}
                disabled={patternsByDayLoading}
                className="w-full gap-2"
              >
                <Download className="w-4 h-4" />
                {patternsByDayLoading ? 'Generating...' : 'Download Excel'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Booking Patterns by Hour Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-rose-500 flex items-center justify-center text-white">
              <Clock className="w-5 h-5" />
            </div>
            Booking Patterns by Hour
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Analyze booking patterns by hour of the day to identify peak booking times
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Start Date
              </label>
              <DatePicker
                value={patternsByHourFilters.startDate}
                onChange={(value) => setPatternsByHourFilters({ ...patternsByHourFilters, startDate: value })}
                placeholder="Select Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                End Date
              </label>
              <DatePicker
                value={patternsByHourFilters.endDate}
                onChange={(value) => setPatternsByHourFilters({ ...patternsByHourFilters, endDate: value })}
                placeholder="Select Date"
              />
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleDownloadPatternsByHourReport}
                disabled={patternsByHourLoading}
                className="w-full gap-2"
              >
                <Download className="w-4 h-4" />
                {patternsByHourLoading ? 'Generating...' : 'Download Excel'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default CustomerReportsTab;
