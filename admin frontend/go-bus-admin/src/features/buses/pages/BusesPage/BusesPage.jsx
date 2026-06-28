import React, { useState, useEffect, useRef } from 'react';
import { Plus, X, Eye, Trash2, AlertCircle, Bus as BusIcon, Users, MapPin, CheckCircle, PauseCircle, Wrench, XCircle, PlayCircle } from 'lucide-react';
import { Button } from 'shared/components/ui/button';
import { Badge } from 'shared/components/common/Badge';
import { Card, CardContent } from 'shared/components/ui/card';
import { SearchInput } from 'shared/components/common/SearchInput';
import { Skeleton } from 'shared/components/ui/skeleton';
import { useToast } from 'shared/components/ui/toast';
import { ConfirmDialog } from 'shared/components/feedback/ConfirmDialog';
import { Pagination } from 'shared/components/feedback/Pagination';
import BusDetailPage from '../BusDetailPage/BusDetailPage';
import CreateBusPage from '../CreateBusPage/CreateBusPage';
import busService from '../../services/busService';
import { routeService } from 'features/routes';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';

const BUS_TYPE_LABELS = { SLEEPER: 'Sleeper', SEATER: 'Seater', AC: 'AC' };
const BUS_STATUS_LABELS = { Active: 'Active', Standby: 'Standby', Maintenance: 'Maintenance', Inactive: 'Inactive', InService: 'In Service' };

const BusesPage = () => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const { addToast } = useToast();

  const [buses, setBuses] = useState([]);
  const [routes, setRoutes] = useState({});
  const [loading, setLoading] = useState(true);
  const [searching, setSearching] = useState(false);
  const [error, setError] = useState('');
  const [selectedBusId, setSelectedBusId] = useState(null);
  const [showCreatePage, setShowCreatePage] = useState(false);
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [filterType, setFilterType] = useState('ALL');
  const [search, setSearch] = useState('');
  const [filterRoute, setFilterRoute] = useState('ALL');
  const [minSeats, setMinSeats] = useState('');
  const [maxSeats, setMaxSeats] = useState('');
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [busToDelete, setBusToDelete] = useState(null);
  const searchDebounceRef = useRef(null);
  const [pagination, setPagination] = useState({
    currentPage: 0,
    pageSize: 15,
    totalPages: 0,
    totalElements: 0
  });

  /**
   * Single bus-fetch function that always includes all active filters.
   * Accepts explicit page/size so callers can pass a freshly computed value
   * without waiting for a React state update to land.
   */
  const fetchBuses = async (page = pagination.currentPage, size = pagination.pageSize) => {
    try {
      const params = { pageNo: page + 1, pageSize: size };  // service is 1-based
      if (filterRoute !== 'ALL' && filterRoute) params.routeId = filterRoute;
      if (filterType !== 'ALL' && filterType) params.busType = filterType;
      if (filterStatus !== 'ALL' && filterStatus) params.status = filterStatus;
      if (search.trim()) params.busNumber = search.trim();
      if (minSeats && !isNaN(minSeats)) params.minSeats = minSeats;
      if (maxSeats && !isNaN(maxSeats)) params.maxSeats = maxSeats;

      const result = await busService.getBuses(params);
      const data = result.data || result;
      const busData = data.content || data;
      setBuses(Array.isArray(busData) ? busData : []);
      setPagination(prev => ({
        ...prev,
        currentPage: page,
        pageSize: size,
        totalPages: data.totalPages || 1,
        totalElements: data.totalElements || (Array.isArray(busData) ? busData.length : 0),
      }));
      setError('');
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.response?.data?.data?.message || 'Failed to fetch buses';
      setError(errorMessage);
      console.error('Error fetching buses:', err);
    }
  };

  // Initial load: fetch buses and routes in parallel
  useEffect(() => {
    const init = async () => {
      setLoading(true);
      await Promise.all([fetchBuses(0, pagination.pageSize), fetchRoutes()]);
      setLoading(false);
    };
    init();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Re-fetch when page or page-size changes (skip during initial load)
  useEffect(() => {
    if (!loading) {
      fetchBuses(pagination.currentPage, pagination.pageSize);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pagination.currentPage, pagination.pageSize]);

  // Auto-fetch when dropdown filters change — reset to page 0
  useEffect(() => {
    if (!loading) {
      if (searchDebounceRef.current) clearTimeout(searchDebounceRef.current);
      searchDebounceRef.current = setTimeout(() => {
        fetchBuses(0, pagination.pageSize);
        setPagination(prev => ({ ...prev, currentPage: 0 }));
      }, 0);
    }
    return () => { if (searchDebounceRef.current) clearTimeout(searchDebounceRef.current); };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterRoute, filterType, filterStatus, minSeats, maxSeats]);

  // Debounced auto-fetch when search text changes — reset to page 0
  useEffect(() => {
    if (!loading) {
      if (searchDebounceRef.current) clearTimeout(searchDebounceRef.current);
      searchDebounceRef.current = setTimeout(() => {
        fetchBuses(0, pagination.pageSize);
        setPagination(prev => ({ ...prev, currentPage: 0 }));
      }, 500);
    }
    return () => { if (searchDebounceRef.current) clearTimeout(searchDebounceRef.current); };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [search]);

  const handleSearch = () => {
    if (searchDebounceRef.current) clearTimeout(searchDebounceRef.current);
    setSearching(true);
    fetchBuses(0, pagination.pageSize).finally(() => {
      setPagination(prev => ({ ...prev, currentPage: 0 }));
      setSearching(false);
    });
  };

  const handleClearFilters = () => {
    if (searchDebounceRef.current) clearTimeout(searchDebounceRef.current);
    setFilterStatus('ALL');
    setFilterType('ALL');
    setFilterRoute('ALL');
    setSearch('');
    setMinSeats('');
    setMaxSeats('');
    // fetchBuses will be called by the filter effects above
    setPagination(prev => ({ ...prev, currentPage: 0 }));
  };

  // newPage arrives 0-based from <Pagination>
  const handlePageChange = (newPage) => {
    setPagination(prev => ({ ...prev, currentPage: newPage }));
  };

  const handlePageSizeChange = (newSize) => {
    setPagination(prev => ({ ...prev, currentPage: 0, pageSize: newSize }));
  };

  const fetchRoutes = async () => {
    try {
      const result = await routeService.getRoutes();
      const data = result.data || result;
      const routesArray = Array.isArray(data) ? data : [];
      const map = {};
      routesArray.forEach(r => {
        map[r.id] = `${r.origin} → ${r.destination}`;
      });
      setRoutes(map);
    } catch (err) {
      console.error('Error fetching routes:', err);
    }
  };

  const getRouteName = (routeId) => {
    if (!routeId) return 'Not Assigned';
    return routes[routeId] || `Route ${routeId}`;
  };

  const handleDeleteBus = (bus) => {
    setBusToDelete(bus);
    setShowDeleteDialog(true);
  };

  const handleConfirmDelete = async () => {
    if (!busToDelete) return;
    try {
      await busService.deleteBus(busToDelete.id);
      setBuses(prev => prev.filter(b => b.id !== busToDelete.id));
      addToast({
        message: `Bus ${busToDelete.busNumber} deleted successfully`,
        type: 'success'
      });
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Failed to delete bus';
      addToast({ message: errorMessage, type: 'error' });
    } finally {
      setShowDeleteDialog(false);
      setBusToDelete(null);
    }
  };

  const statusCounts = {
    active: buses.filter(b => (b.status || b.busStatus) === 'Active').length,
    standby: buses.filter(b => (b.status || b.busStatus) === 'Standby').length,
    maintenance: buses.filter(b => (b.status || b.busStatus) === 'Maintenance').length,
    inactive: buses.filter(b => (b.status || b.busStatus) === 'Inactive').length,
    inservice: buses.filter(b => (b.status || b.busStatus) === 'InService').length,
  };

  if (showCreatePage) {
    return (
      <CreateBusPage
        onBack={() => setShowCreatePage(false)}
        onSuccess={() => {
          setShowCreatePage(false);
          // Go to first page so the newly created bus is visible
          fetchBuses(0, pagination.pageSize);
          setPagination(prev => ({ ...prev, currentPage: 0 }));
          addToast({ message: t('busCreated') || 'Bus created successfully', type: 'success' });
        }}
      />
    );
  }

  if (selectedBusId) {
    return <BusDetailPage busId={selectedBusId} onBack={() => {
      setSelectedBusId(null);
      fetchBuses(pagination.currentPage, pagination.pageSize);
    }} />;
  }

  if (loading) {
    return (
      <div className="flex-1 p-4 sm:p-6 md:p-8 overflow-y-auto bg-background min-h-screen">
        <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3 mb-4 sm:mb-6 lg:mb-8">
          <div className="space-y-2">
            <Skeleton className="h-8 sm:h-10 w-48 sm:w-64" />
            <Skeleton className="h-4 w-64 sm:w-96" />
          </div>
          <Skeleton className="h-10 w-full sm:w-40" />
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3 sm:gap-4 md:gap-6 mb-4 sm:mb-6 lg:mb-8">
          {[...Array(6)].map((_, i) => (
            <Card key={i}>
              <CardContent className="p-6">
                <div className="flex items-center gap-4">
                  <Skeleton className="h-12 w-12 rounded-xl" />
                  <div className="space-y-2 flex-1">
                    <Skeleton className="h-8 w-16" />
                    <Skeleton className="h-4 w-24" />
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>

        <Card>
          <CardContent className="p-6 space-y-4">
            <div className="flex gap-2">
              <Skeleton className="h-10 flex-1" />
              <Skeleton className="h-10 w-24" />
            </div>
            {[...Array(5)].map((_, i) => (
              <Skeleton key={i} className="h-24 w-full" />
            ))}
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="flex-1 p-4 sm:p-6 md:p-8 overflow-y-auto bg-background min-h-screen">
      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3 mb-4 sm:mb-6 lg:mb-8">
        <div>
          <h1 className="text-xl sm:text-2xl font-semibold text-foreground mb-1">
            {t('busManagement') || 'Bus Management'}
          </h1>
          <p className="text-sm sm:text-base text-muted-foreground">
            {t('busManagementDesc') || 'Manage your fleet of buses, routes, and schedules'}
          </p>
        </div>
        <Button onClick={() => setShowCreatePage(true)} className="gap-2 w-full sm:w-auto justify-center">
          <Plus className="h-4 w-4" />
          {t('addNewBus') || 'Add New Bus'}
        </Button>
      </div>

      {error && (
        <Card className="mb-6 border-l-4 border-destructive bg-destructive/5">
          <CardContent className="flex items-start gap-3 pt-6">
            <AlertCircle className="h-5 w-5 text-destructive flex-shrink-0 mt-0.5" />
            <span className="font-medium text-destructive">{error}</span>
          </CardContent>
        </Card>
      )}

      <div className="grid grid-cols-3 sm:grid-cols-6 gap-3 sm:gap-4 mb-4 sm:mb-6">
        <Card>
          <CardContent className="p-3 sm:p-4">
            <div className="flex items-center gap-2 sm:gap-3">
              <div className="bg-blue-50 dark:bg-blue-900/20 p-2 rounded-md flex-shrink-0">
                <BusIcon className="h-4 w-4 text-blue-600 dark:text-blue-400" />
              </div>
              <div className="min-w-0">
                <div className="text-lg sm:text-xl font-semibold text-foreground">{buses.length}</div>
                <div className="text-xs text-muted-foreground truncate">{t('totalBuses') || 'Total'}</div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-3 sm:p-4">
            <div className="flex items-center gap-2 sm:gap-3">
              <div className="bg-green-50 dark:bg-green-900/20 p-2 rounded-md flex-shrink-0">
                <CheckCircle className="h-4 w-4 text-green-600 dark:text-green-400" />
              </div>
              <div className="min-w-0">
                <div className="text-lg sm:text-xl font-semibold text-foreground">{statusCounts.active}</div>
                <div className="text-xs text-muted-foreground truncate">{t('active') || 'Active'}</div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-3 sm:p-4">
            <div className="flex items-center gap-2 sm:gap-3">
              <div className="bg-amber-50 dark:bg-amber-900/20 p-2 rounded-md flex-shrink-0">
                <PauseCircle className="h-4 w-4 text-amber-600 dark:text-amber-400" />
              </div>
              <div className="min-w-0">
                <div className="text-lg sm:text-xl font-semibold text-foreground">{statusCounts.standby}</div>
                <div className="text-xs text-muted-foreground truncate">{t('standby') || 'Standby'}</div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-3 sm:p-4">
            <div className="flex items-center gap-2 sm:gap-3">
              <div className="bg-purple-50 dark:bg-purple-900/20 p-2 rounded-md flex-shrink-0">
                <Wrench className="h-4 w-4 text-purple-600 dark:text-purple-400" />
              </div>
              <div className="min-w-0">
                <div className="text-lg sm:text-xl font-semibold text-foreground">{statusCounts.maintenance}</div>
                <div className="text-xs text-muted-foreground truncate">{t('maintenance') || 'Maint.'}</div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-3 sm:p-4">
            <div className="flex items-center gap-2 sm:gap-3">
              <div className="bg-muted p-2 rounded-md flex-shrink-0">
                <XCircle className="h-4 w-4 text-muted-foreground" />
              </div>
              <div className="min-w-0">
                <div className="text-lg sm:text-xl font-semibold text-foreground">{statusCounts.inactive}</div>
                <div className="text-xs text-muted-foreground truncate">{t('inactive') || 'Inactive'}</div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-3 sm:p-4">
            <div className="flex items-center gap-2 sm:gap-3">
              <div className="bg-blue-50 dark:bg-blue-900/20 p-2 rounded-md flex-shrink-0">
                <PlayCircle className="h-4 w-4 text-blue-600 dark:text-blue-400" />
              </div>
              <div className="min-w-0">
                <div className="text-lg sm:text-xl font-semibold text-foreground">{statusCounts.inservice}</div>
                <div className="text-xs text-muted-foreground truncate">{t('inservice') || 'In Svc'}</div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardContent className="p-6">
          <div className="flex flex-col sm:flex-row gap-2 mb-6">
            <SearchInput
              className="flex-1"
              value={search}
              onChange={setSearch}
              onSubmit={handleSearch}
              placeholder={t('searchBuses') || 'Search buses by number or model...'}
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3 sm:gap-4 mb-4 sm:mb-6">
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium">{t('route') || 'Route'}</label>
              <select
                value={filterRoute}
                onChange={e => setFilterRoute(e.target.value)}
                className="px-3 py-2 border border-input rounded-lg bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              >
                <option value="ALL">{t('allRoutes') || 'All Routes'}</option>
                {Object.entries(routes).map(([id, name]) => (
                  <option key={id} value={id}>{name}</option>
                ))}
              </select>
            </div>

            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium">{t('type') || 'Type'}</label>
              <select
                value={filterType}
                onChange={e => setFilterType(e.target.value)}
                className="px-3 py-2 border border-input rounded-lg bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              >
                <option value="ALL">{t('allTypes') || 'All Types'}</option>
                <option value="AC">AC</option>
                <option value="SLEEPER">Sleeper</option>
                <option value="SEATER">Seater</option>
              </select>
            </div>

            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium">{t('status') || 'Status'}</label>
              <select
                value={filterStatus}
                onChange={e => setFilterStatus(e.target.value)}
                className="px-3 py-2 border border-input rounded-lg bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              >
                <option value="ALL">{t('allStatus') || 'All Status'}</option>
                <option value="Active">{t('active') || 'Active'}</option>
                <option value="Standby">{t('standby') || 'Standby'}</option>
                <option value="Maintenance">{t('maintenance') || 'Maintenance'}</option>
                <option value="Inactive">{t('inactive') || 'Inactive'}</option>
                <option value="InService">{t('inservice') || 'In Service'}</option>
              </select>
            </div>

            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium">{t('minSeats') || 'Min Seats'}</label>
              <input
                type="number"
                placeholder="Min"
                value={minSeats}
                onChange={e => setMinSeats(e.target.value)}
                className="px-3 py-2 border border-input rounded-lg bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                min="1"
              />
            </div>

            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium">{t('maxSeats') || 'Max Seats'}</label>
              <input
                type="number"
                placeholder="Max"
                value={maxSeats}
                onChange={e => setMaxSeats(e.target.value)}
                className="px-3 py-2 border border-input rounded-lg bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                min="1"
              />
            </div>

            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium opacity-0">Clear</label>
              <Button
                variant="outline"
                onClick={handleClearFilters}
                className="gap-2"
              >
                <X className="h-4 w-4" />
                {t('clearFilters') || 'Clear Filters'}
              </Button>
            </div>
          </div>

          {searching ? (
            <div className="space-y-4">
              {[...Array(5)].map((_, i) => (
                <Skeleton key={i} className="h-24 w-full" />
              ))}
            </div>
          ) : buses.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-20 text-center">
              <BusIcon className="h-16 w-16 text-muted-foreground mb-4" />
              <h3 className="text-2xl font-bold text-foreground mb-2">
                {t('noBusesFound') || 'No buses found'}
              </h3>
              <p className="text-muted-foreground mb-6">
                {t('tryAdjustingFilters') || "Try adjusting your search or filters to find what you're looking for"}
              </p>
              <Button variant="outline" onClick={handleClearFilters} className="gap-2">
                <X className="h-4 w-4" />
                {t('clearFilters') || 'Clear Filters'}
              </Button>
            </div>
          ) : (
            <>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="border-b">
                    <tr className="text-left text-sm font-medium text-muted-foreground">
                      <th className="pb-3 px-4">{t('busInfo') || 'Bus Information'}</th>
                      <th className="pb-3 px-4">{t('type') || 'Type'}</th>
                      <th className="pb-3 px-4">{t('route') || 'Route'}</th>
                      <th className="pb-3 px-4">{t('seats') || 'Seats'}</th>
                      <th className="pb-3 px-4">{t('status') || 'Status'}</th>
                      <th className="pb-3 px-4 text-center">{t('actions') || 'Actions'}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {buses.map(bus => (
                      <tr key={bus.id} className="border-b hover:bg-muted/50 transition-colors">
                        <td className="py-4 px-4">
                          <div className="flex flex-col gap-1">
                            <div className="flex items-center gap-3">
                              <h3 className="font-bold text-foreground">{bus.busNumber}</h3>
                              <span className="text-xs text-muted-foreground bg-muted px-2 py-0.5 rounded-full">
                                ID: B-{bus.id}
                              </span>
                            </div>
                            <div className="flex gap-4 text-sm text-muted-foreground">
                              <span className="flex items-center gap-1">
                                <svg className="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                  <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
                                  <line x1="1" y1="10" x2="23" y2="10"/>
                                </svg>
                                {bus.plate || 'N/A'}
                              </span>
                              <span className="flex items-center gap-1">
                                <svg className="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                                  <polyline points="14 2 14 8 20 8"/>
                                </svg>
                                {bus.model || 'N/A'}
                              </span>
                            </div>
                          </div>
                        </td>
                        <td className="py-4 px-4">
                          <Badge variant={bus.busType === 'SLEEPER' ? 'info' : 'default'}>
                            {BUS_TYPE_LABELS[bus.busType] || bus.busType || 'N/A'}
                          </Badge>
                        </td>
                        <td className="py-4 px-4">
                          <div className="flex items-center gap-2 text-sm">
                            <MapPin className="h-4 w-4 text-muted-foreground" />
                            <span className="text-foreground">{getRouteName(bus.routeId)}</span>
                          </div>
                        </td>
                        <td className="py-4 px-4">
                          <div className="flex items-center gap-2 text-sm">
                            <Users className="h-4 w-4 text-muted-foreground" />
                            {bus.totalSeats ?? 'N/A'}
                          </div>
                        </td>
                        <td className="py-4 px-4">
                          <Badge variant={
                            (bus.status || bus.busStatus) === 'Active' ? 'success' :
                            (bus.status || bus.busStatus) === 'Standby' ? 'pending' :
                            (bus.status || bus.busStatus) === 'Maintenance' ? 'info' :
                            (bus.status || bus.busStatus) === 'InService' ? 'confirmed' :
                            'default'
                          }>
                            {BUS_STATUS_LABELS[bus.status || bus.busStatus] || bus.status || bus.busStatus || 'N/A'}
                          </Badge>
                        </td>
                        <td className="py-4 px-4">
                          <div className="flex items-center justify-center gap-1">
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => setSelectedBusId(bus.id)}
                              title={t('viewDetails') || 'View Details'}
                              className="text-muted-foreground hover:text-foreground"
                            >
                              <Eye className="h-4 w-4" />
                            </Button>
                            <Button
                              size="sm"
                              variant="destructive"
                              onClick={() => handleDeleteBus(bus)}
                              title={t('delete') || 'Delete'}
                            >
                              <Trash2 className="w-3 h-3 sm:mr-1" />
                              <span className="hidden sm:inline">{t('delete') || 'Delete'}</span>
                            </Button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="mt-6 pt-4 border-t">
                <Pagination
                  currentPage={pagination.currentPage}
                  totalPages={pagination.totalPages}
                  pageSize={pagination.pageSize}
                  totalElements={pagination.totalElements}
                  onPageChange={handlePageChange}
                  onPageSizeChange={handlePageSizeChange}
                />
              </div>
            </>
          )}
        </CardContent>
      </Card>

      <ConfirmDialog
        isOpen={showDeleteDialog}
        onConfirm={handleConfirmDelete}
        onCancel={() => setShowDeleteDialog(false)}
        title={t('deleteBus') || 'Delete Bus'}
        message={`${t('deleteBusConfirm') || 'Are you sure you want to delete'} ${busToDelete?.busNumber}? ${t('thisActionCannotBeUndone') || 'This action cannot be undone.'}`}
        confirmText={t('delete') || 'Delete'}
        cancelText={t('cancel') || 'Cancel'}
        type="danger"
      />
    </div>
  );
};

export default BusesPage;
