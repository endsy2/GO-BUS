import React, { useState, useCallback, useEffect } from 'react';
import { Card } from 'shared/components/ui/card';
import { Button } from 'shared/components/ui/button';
import { ConfirmDialog } from 'shared/components/feedback/ConfirmDialog';
import { DatePicker } from 'shared/components/ui/date-picker';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from 'shared/components/ui/table';
import { Pagination } from 'shared/components/feedback/Pagination';
import { useToast } from 'shared/components/ui/toast';
import { Plus, Edit, Trash2, Loader2, Filter, X, ChevronUp, ChevronDown } from 'lucide-react';
import scheduleService from '../../services/scheduleService';
import busService from '../../../buses/services/busService';
import routeService from '../../../routes/services/routeService';
import CreateScheduleDialog from '../../components/CreateScheduleDialog/CreateScheduleDialog';
import EditScheduleDialog from '../../components/EditScheduleDialog/EditScheduleDialog';

// ── Shared filter field styling (matches BookingFilters) ───────────────────────
const inputClass =
  'w-full px-3 py-2 border border-slate-300 dark:border-slate-700 rounded-lg bg-white dark:bg-slate-800 text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm';

const labelClass =
  'block text-xs font-medium text-slate-500 dark:text-slate-400 mb-1.5 uppercase tracking-wide';

const sectionTitleClass =
  'text-xs font-semibold text-slate-400 dark:text-slate-500 uppercase tracking-wider mb-2.5';

const SchedulesPage = () => {
  const { addToast } = useToast();
  // Local YYYY-MM-DD (avoids toISOString() shifting the day in UTC+7).
  const getTodayLocalDate = () => {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  // Earliest selectable date in the pickers (today at midnight).
  const minSelectableDate = new Date();
  minSelectableDate.setHours(0, 0, 0, 0);

  const initialFilters = {
    routeId: '',
    busId: '',
    fromDate: getTodayLocalDate(), // Auto-set to today
    toDate: '',
    maxPrice: '',
  };

  const [schedules, setSchedules] = useState([]);
  const [buses, setBuses] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingBuses, setLoadingBuses] = useState(false);
  const [loadingRoutes, setLoadingRoutes] = useState(false);
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [editingSchedule, setEditingSchedule] = useState(null);
  const [deleteScheduleId, setDeleteScheduleId] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [filters, setFilters] = useState(initialFilters);
  const [showFilters, setShowFilters] = useState(true);
  const activeCount = Object.values(filters).filter((v) => v !== '').length;
  // currentPage is 0-based. The service expects 1-based pageNo, so we add +1 at the call site.
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(15);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Fetch schedules — depends on filters, currentPage, pageSize.
  // useEffect([fetchSchedules]) ensures exactly one fetch per dependency change.
  const fetchSchedules = useCallback(async () => {
    try {
      setLoading(true);
      const response = await scheduleService.filterSchedules(
        filters.routeId || null,
        filters.fromDate || null,
        filters.toDate || null,
        filters.maxPrice || null,
        currentPage + 1,   // service is 1-based
        pageSize
      );

      const scheduleData = response.data?.content || response.content || [];
      setSchedules(Array.isArray(scheduleData) ? scheduleData : []);
      setTotalPages(response.data?.totalPages || 0);
      setTotalElements(response.data?.totalElements || 0);
    } catch (error) {
      console.error('Failed to fetch schedules:', error);
      setSchedules([]);
    } finally {
      setLoading(false);
    }
  }, [filters, currentPage, pageSize]);

  // ── Dropdown data fetchers (mount-only, independent of schedule pagination) ──

  const fetchBuses = async () => {
    setLoadingBuses(true);
    try {
      const busesRes = await busService.getBuses({ pageSize: 1000 });
      const busesData = busesRes.data?.content || busesRes.data || busesRes;
      setBuses(Array.isArray(busesData) ? busesData : []);
    } catch (error) {
      console.error('Failed to fetch buses:', error);
    } finally {
      setLoadingBuses(false);
    }
  };

  const fetchRoutes = async () => {
    setLoadingRoutes(true);
    try {
      const routesRes = await routeService.getRoutes();
      const routesData = routesRes?.data || routesRes;
      const routesArray = Array.isArray(routesData)
        ? routesData
        : Array.isArray(routesData?.data)
          ? routesData.data
          : [];
      setRoutes(routesArray);
    } catch (error) {
      console.error('Failed to fetch routes:', error);
      setRoutes([]);
    } finally {
      setLoadingRoutes(false);
    }
  };

  // Fetch buses and routes on mount (independent of pagination state)
  useEffect(() => {
    fetchBuses();
    fetchRoutes();
  }, []);

  // Trigger a schedule fetch whenever fetchSchedules changes (i.e., whenever any dep changes)
  useEffect(() => {
    fetchSchedules();
  }, [fetchSchedules]);

  const handleDelete = (id) => {
    setDeleteScheduleId(id);
  };

  const handleConfirmDelete = async () => {
    if (!deleteScheduleId) return;
    try {
      setDeleting(true);
      await scheduleService.deleteSchedule(deleteScheduleId);
      setDeleteScheduleId(null);
      // fetchSchedules will re-run because its identity doesn't change here;
      // call it explicitly to refresh after delete.
      fetchSchedules();
    } catch (error) {
      console.error('Failed to delete schedule:', error);
      addToast({ message: error.response?.data?.message || 'Failed to delete schedule', type: 'error' });
    } finally {
      setDeleting(false);
    }
  };

  // newPage arrives 0-based from <Pagination>
  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
  };

  const handlePageSizeChange = (newSize) => {
    setCurrentPage(0);
    setPageSize(newSize);
  };

  // Reset to first page when filters change (called inline on filter updates below)
  const handleClearFilters = () => {
    setCurrentPage(0);
    setFilters(initialFilters);
  };

  // Helper: update a single filter key and reset to first page
  const updateFilter = (key, value) => {
    setCurrentPage(0);
    setFilters(prev => ({ ...prev, [key]: value }));
  };

  return (
    <div className="flex-1 p-4 sm:p-6 overflow-y-auto bg-background min-h-screen">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3 mb-4 sm:mb-6">
        <div>
          <h1 className="text-xl sm:text-2xl font-semibold text-foreground mb-1">
            Schedules Management
          </h1>
          <p className="text-sm text-muted-foreground">Manage bus schedules and timetables</p>
        </div>
        <Button
          onClick={() => setShowCreateDialog(true)}
          className="flex items-center gap-2 w-full sm:w-auto justify-center sm:justify-start"
        >
          <Plus className="w-4 h-4" />
          Create Schedule
        </Button>
      </div>

      {/* Filters */}
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-4 sm:p-5 mb-4 sm:mb-6">

        {/* Header row */}
        <div className="flex justify-between items-center">
          <h3 className="text-base font-semibold text-slate-900 dark:text-white flex items-center gap-2">
            <Filter className="w-4 h-4 text-blue-500" />
            Filter Schedules
            {activeCount > 0 && (
              <span className="inline-flex items-center justify-center h-5 min-w-[1.25rem] px-1.5 rounded-full bg-blue-500 text-white text-xs font-bold">
                {activeCount}
              </span>
            )}
          </h3>
          <div className="flex items-center gap-2">
            {activeCount > 0 && (
              <Button
                variant="outline"
                size="sm"
                onClick={handleClearFilters}
                disabled={loading}
                className="flex items-center gap-1 text-xs"
              >
                <X className="w-3 h-3" />
                Clear All
              </Button>
            )}
            <Button
              variant="outline"
              size="sm"
              onClick={() => setShowFilters(!showFilters)}
              className="flex items-center gap-1 text-sm"
            >
              {showFilters ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
              {showFilters ? 'Hide' : 'Show'}
            </Button>
          </div>
        </div>

        {showFilters && (
          <div className="mt-4 space-y-5">

            {/* ── Section 1: Route, Bus & Price ──────────────────────── */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 sm:gap-4">
              <div>
                <label className={labelClass}>Route</label>
                <select
                  value={filters.routeId}
                  onChange={(e) => updateFilter('routeId', e.target.value)}
                  disabled={loadingRoutes}
                  className={inputClass}
                >
                  <option value="">All Routes</option>
                  {routes.map((route) => (
                    <option key={route.id} value={String(route.id)}>
                      {route.origin} → {route.destination}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className={labelClass}>Bus</label>
                <select
                  value={filters.busId}
                  onChange={(e) => updateFilter('busId', e.target.value)}
                  disabled={loadingBuses}
                  className={inputClass}
                >
                  <option value="">All Buses</option>
                  {buses.map((bus) => (
                    <option key={bus.id} value={String(bus.id)}>
                      {bus.busNumber} | {bus.busType}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className={labelClass}>Max Price</label>
                <input
                  type="number"
                  name="maxPrice"
                  step="0.01"
                  min="0"
                  value={filters.maxPrice}
                  onChange={(e) => updateFilter('maxPrice', e.target.value)}
                  placeholder="Any Price"
                  className={inputClass}
                />
              </div>
            </div>

            <div className="border-t border-slate-100 dark:border-slate-800" />

            {/* ── Section 2: Departure Date ──────────────────────────── */}
            <div>
              <p className={sectionTitleClass}>Departure Date</p>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                <div>
                  <label className={labelClass}>From</label>
                  <DatePicker
                    value={filters.fromDate}
                    onChange={(value) => updateFilter('fromDate', value)}
                    placeholder="Select from date"
                    minDate={minSelectableDate}
                  />
                </div>
                <div>
                  <label className={labelClass}>To</label>
                  <DatePicker
                    value={filters.toDate}
                    onChange={(value) => updateFilter('toDate', value)}
                    placeholder="Select to date"
                    minDate={filters.fromDate ? new Date(filters.fromDate) : minSelectableDate}
                  />
                </div>
              </div>
            </div>

          </div>
        )}
      </div>

      {/* Results Card */}
      <Card className="overflow-hidden">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="hover:bg-muted/50">
                <TableHead className="font-medium">ID</TableHead>
                <TableHead className="font-medium">Bus Number</TableHead>
                <TableHead className="font-medium">Route</TableHead>
                <TableHead className="font-medium">Departure</TableHead>
                <TableHead className="font-medium">Arrival</TableHead>
                <TableHead className="font-medium">Price</TableHead>
                <TableHead className="font-medium text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center py-12 text-muted-foreground">
                    <Loader2 className="w-6 h-6 animate-spin mx-auto mb-2" />
                    Loading schedules...
                  </TableCell>
                </TableRow>
              ) : schedules.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center py-12 text-muted-foreground">
                    No schedules found. Try adjusting your filters.
                  </TableCell>
                </TableRow>
              ) : (
                schedules.map((schedule) => (
                  <TableRow key={schedule.id} className="hover:bg-muted/50 transition-colors">
                    <TableCell className="font-medium">#{schedule.id}</TableCell>
                    <TableCell>{schedule.busNumber || 'N/A'}</TableCell>
                    <TableCell>
                      {schedule.route?.origin && schedule.route?.destination
                        ? `${schedule.route.origin} → ${schedule.route.destination}`
                        : 'Route not assigned'}
                    </TableCell>
                    <TableCell>
                      {schedule.departureDateTime ? new Date(schedule.departureDateTime).toLocaleString() : 'N/A'}
                    </TableCell>
                    <TableCell>
                      {schedule.arrivalDateTime ? new Date(schedule.arrivalDateTime).toLocaleString() : schedule.arrivalTime || 'N/A'}
                    </TableCell>
                    <TableCell className="font-medium">${schedule.price?.toFixed(2) || '0.00'}</TableCell>
                    <TableCell className="text-right">
                      <div className="flex gap-2 justify-end">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => setEditingSchedule(schedule)}
                          title="Edit"
                        >
                          <Edit className="w-3 h-3 sm:mr-1" />
                          <span className="hidden sm:inline">Edit</span>
                        </Button>
                        <Button
                          size="sm"
                          variant="destructive"
                          onClick={() => handleDelete(schedule.id)}
                          title="Delete"
                        >
                          <Trash2 className="w-3 h-3 sm:mr-1" />
                          <span className="hidden sm:inline">Delete</span>
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>

        {/* Pagination */}
        {schedules.length > 0 && (
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            pageSize={pageSize}
            totalElements={totalElements}
            onPageChange={handlePageChange}
            onPageSizeChange={handlePageSizeChange}
          />
        )}
      </Card>

      {/* Dialogs */}
      {showCreateDialog && (
        <CreateScheduleDialog
          open={showCreateDialog}
          onClose={() => setShowCreateDialog(false)}
          onSuccess={() => {
            setShowCreateDialog(false);
            fetchSchedules();
          }}
        />
      )}

      {editingSchedule && (
        <EditScheduleDialog
          open={!!editingSchedule}
          schedule={editingSchedule}
          onClose={() => setEditingSchedule(null)}
          onSuccess={() => {
            setEditingSchedule(null);
            fetchSchedules();
          }}
        />
      )}

      <ConfirmDialog
        isOpen={deleteScheduleId !== null}
        onConfirm={handleConfirmDelete}
        onCancel={() => { if (!deleting) setDeleteScheduleId(null); }}
        title="Delete Schedule"
        message={`Are you sure you want to delete schedule #${deleteScheduleId}?`}
        confirmText={deleting ? 'Deleting...' : 'Delete'}
        cancelText="Cancel"
        type="danger"
      />
    </div>
  );
};

export default SchedulesPage;
