import React, { useState } from 'react';
import { Download, FileText, TrendingUp, Users, Ticket, MapPin } from 'lucide-react';
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

const OperationalReportsTab = () => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const { addToast } = useToast();

  // Booking Report State
  const [bookingLoading, setBookingLoading] = useState(false);
  const [bookingFilters, setBookingFilters] = useState({
    period: 'DAILY',
    startDate: '',
    endDate: ''
  });

  // Route Revenue Report State
  const [routeRevenueLoading, setRouteRevenueLoading] = useState(false);
  const [routeRevenueFilters, setRouteRevenueFilters] = useState({
    startDate: '',
    endDate: ''
  });

  // Popular Routes Report State
  const [popularRoutesLoading, setPopularRoutesLoading] = useState(false);
  const [popularRoutesFilters, setPopularRoutesFilters] = useState({
    startDate: '',
    endDate: '',
    limit: '10'
  });

  // Seat Occupancy Report State
  const [occupancyLoading, setOccupancyLoading] = useState(false);
  const [occupancyFilters, setOccupancyFilters] = useState({
    scheduleId: ''
  });

  // Ticket Sales Report State
  const [ticketSalesLoading, setTicketSalesLoading] = useState(false);
  const [ticketSalesFilters, setTicketSalesFilters] = useState({
    startDate: '',
    endDate: '',
    routeId: '',
    busId: ''
  });

  // ========== Booking Report ==========
  const handleDownloadBookingReport = async () => {
    if (!bookingFilters.startDate || !bookingFilters.endDate) {
      addToast({ message: 'Please select start and end dates', type: 'error' });
      return;
    }

    setBookingLoading(true);
    try {
      const blob = await reportService.getBookingReport(
        bookingFilters.period,
        null,
        bookingFilters.startDate,
        bookingFilters.endDate,
        null,
        null
      );
      
      const filename = `booking_report_${bookingFilters.startDate}_to_${bookingFilters.endDate}_${bookingFilters.period}.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Booking report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading booking report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download booking report', type: 'error' });
    } finally {
      setBookingLoading(false);
    }
  };

  // ========== Route Revenue Report ==========
  const handleDownloadRouteRevenueReport = async () => {
    if (!routeRevenueFilters.startDate || !routeRevenueFilters.endDate) {
      addToast({ message: 'Please select start and end dates', type: 'error' });
      return;
    }

    setRouteRevenueLoading(true);
    try {
      const blob = await reportService.getRouteRevenueReport(
        routeRevenueFilters.startDate,
        routeRevenueFilters.endDate
      );
      
      const filename = `route_revenue_report_${routeRevenueFilters.startDate}_to_${routeRevenueFilters.endDate}.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Route revenue report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading route revenue report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download route revenue report', type: 'error' });
    } finally {
      setRouteRevenueLoading(false);
    }
  };

  // ========== Popular Routes Report ==========
  const handleDownloadPopularRoutesReport = async () => {
    if (!popularRoutesFilters.startDate || !popularRoutesFilters.endDate) {
      addToast({ message: 'Please select start and end dates', type: 'error' });
      return;
    }

    setPopularRoutesLoading(true);
    try {
      const blob = await reportService.getPopularRoutesReport(
        popularRoutesFilters.startDate,
        popularRoutesFilters.endDate,
        parseInt(popularRoutesFilters.limit)
      );
      
      const filename = `popular_routes_report_${popularRoutesFilters.startDate}_to_${popularRoutesFilters.endDate}.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Popular routes report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading popular routes report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download popular routes report', type: 'error' });
    } finally {
      setPopularRoutesLoading(false);
    }
  };

  // ========== Seat Occupancy Report ==========
  const handleDownloadOccupancyReport = async () => {
    setOccupancyLoading(true);
    try {
      const scheduleId = occupancyFilters.scheduleId ? parseInt(occupancyFilters.scheduleId) : null;
      const blob = await reportService.getSeatOccupancyReport(scheduleId);
      
      const filename = scheduleId 
        ? `seat_occupancy_report_schedule_${scheduleId}.xlsx`
        : `seat_occupancy_report_all.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Seat occupancy report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading seat occupancy report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download seat occupancy report', type: 'error' });
    } finally {
      setOccupancyLoading(false);
    }
  };

  // ========== Ticket Sales Report ==========
  const handleDownloadTicketSalesReport = async () => {
    if (!ticketSalesFilters.startDate || !ticketSalesFilters.endDate) {
      addToast({ message: 'Please select start and end dates', type: 'error' });
      return;
    }

    setTicketSalesLoading(true);
    try {
      const routeId = ticketSalesFilters.routeId ? parseInt(ticketSalesFilters.routeId) : null;
      const busId = ticketSalesFilters.busId ? parseInt(ticketSalesFilters.busId) : null;
      
      const blob = await reportService.getTicketSalesReport(
        ticketSalesFilters.startDate,
        ticketSalesFilters.endDate,
        routeId,
        busId
      );
      
      const filename = `ticket_sales_report_${ticketSalesFilters.startDate}_to_${ticketSalesFilters.endDate}.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Ticket sales report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading ticket sales report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download ticket sales report', type: 'error' });
    } finally {
      setTicketSalesLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Booking Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-blue-500 flex items-center justify-center text-white">
              <FileText className="w-5 h-5" />
            </div>
            Booking Report
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Generate comprehensive booking reports with statistics and trends by period
          </p>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Start Date
              </label>
              <DatePicker
                value={bookingFilters.startDate}
                onChange={(value) => setBookingFilters({ ...bookingFilters, startDate: value })}
                placeholder="Select Start Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                End Date
              </label>
              <DatePicker
                value={bookingFilters.endDate}
                onChange={(value) => setBookingFilters({ ...bookingFilters, endDate: value })}
                placeholder="Select End Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Period
              </label>
              <Select
                value={bookingFilters.period}
                onValueChange={(value) => setBookingFilters({ ...bookingFilters, period: value })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="DAILY">Daily</SelectItem>
                  <SelectItem value="WEEKLY">Weekly</SelectItem>
                  <SelectItem value="MONTHLY">Monthly</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleDownloadBookingReport}
                disabled={bookingLoading}
                className="w-full gap-2"
              >
                <Download className="w-4 h-4" />
                {bookingLoading ? 'Generating...' : 'Download Excel'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Route Revenue Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-green-500 flex items-center justify-center text-white">
              <MapPin className="w-5 h-5" />
            </div>
            Route Revenue Report
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Analyze revenue generated by each route during a specific period
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Start Date
              </label>
              <DatePicker
                value={routeRevenueFilters.startDate}
                onChange={(value) => setRouteRevenueFilters({ ...routeRevenueFilters, startDate: value })}
                placeholder="Select Start Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                End Date
              </label>
              <DatePicker
                value={routeRevenueFilters.endDate}
                onChange={(value) => setRouteRevenueFilters({ ...routeRevenueFilters, endDate: value })}
                placeholder="Select End Date"
              />
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleDownloadRouteRevenueReport}
                disabled={routeRevenueLoading}
                className="w-full gap-2"
              >
                <Download className="w-4 h-4" />
                {routeRevenueLoading ? 'Generating...' : 'Download Excel'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Popular Routes Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-pink-500 flex items-center justify-center text-white">
              <TrendingUp className="w-5 h-5" />
            </div>
            Popular Routes Report
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Identify the most popular routes based on booking volume
          </p>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Start Date
              </label>
              <DatePicker
                value={popularRoutesFilters.startDate}
                onChange={(value) => setPopularRoutesFilters({ ...popularRoutesFilters, startDate: value })}
                placeholder="Select Start Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                End Date
              </label>
              <DatePicker
                value={popularRoutesFilters.endDate}
                onChange={(value) => setPopularRoutesFilters({ ...popularRoutesFilters, endDate: value })}
                placeholder="Select End Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Top Routes
              </label>
              <Select
                value={popularRoutesFilters.limit}
                onValueChange={(value) => setPopularRoutesFilters({ ...popularRoutesFilters, limit: value })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="5">Top 5</SelectItem>
                  <SelectItem value="10">Top 10</SelectItem>
                  <SelectItem value="15">Top 15</SelectItem>
                  <SelectItem value="20">Top 20</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleDownloadPopularRoutesReport}
                disabled={popularRoutesLoading}
                className="w-full gap-2"
              >
                <Download className="w-4 h-4" />
                {popularRoutesLoading ? 'Generating...' : 'Download Excel'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Seat Occupancy Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-indigo-500 flex items-center justify-center text-white">
              <Users className="w-5 h-5" />
            </div>
            Seat Occupancy Report
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Monitor seat occupancy rates across schedules (leave empty for all schedules)
          </p>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Schedule ID (Optional)
              </label>
              <Input
                type="number"
                placeholder="Leave Empty For All Schedules"
                value={occupancyFilters.scheduleId}
                onChange={(e) => setOccupancyFilters({ ...occupancyFilters, scheduleId: e.target.value })}
              />
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleDownloadOccupancyReport}
                disabled={occupancyLoading}
                className="w-full gap-2"
              >
                <Download className="w-4 h-4" />
                {occupancyLoading ? 'Generating...' : 'Download Excel'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Ticket Sales Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-amber-500 flex items-center justify-center text-white">
              <Ticket className="w-5 h-5" />
            </div>
            Ticket Sales Report
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Detailed ticket sales analysis with optional filters for route and bus
          </p>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Start Date
              </label>
              <DatePicker
                value={ticketSalesFilters.startDate}
                onChange={(value) => setTicketSalesFilters({ ...ticketSalesFilters, startDate: value })}
                placeholder="Select Start Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                End Date
              </label>
              <DatePicker
                value={ticketSalesFilters.endDate}
                onChange={(value) => setTicketSalesFilters({ ...ticketSalesFilters, endDate: value })}
                placeholder="Select End Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Route ID (Optional)
              </label>
              <Input
                type="number"
                placeholder="All Routes"
                value={ticketSalesFilters.routeId}
                onChange={(e) => setTicketSalesFilters({ ...ticketSalesFilters, routeId: e.target.value })}
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Bus ID (Optional)
              </label>
              <Input
                type="number"
                placeholder="All Buses"
                value={ticketSalesFilters.busId}
                onChange={(e) => setTicketSalesFilters({ ...ticketSalesFilters, busId: e.target.value })}
              />
            </div>
          </div>
          <div className="mt-4">
            <Button
              onClick={handleDownloadTicketSalesReport}
              disabled={ticketSalesLoading}
              className="gap-2"
            >
              <Download className="w-4 h-4" />
              {ticketSalesLoading ? 'Generating...' : 'Download Excel'}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default OperationalReportsTab;
