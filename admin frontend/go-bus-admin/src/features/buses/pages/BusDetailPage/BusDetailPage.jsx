import React, { useState, useEffect } from 'react';
import { ArrowLeft, Edit, Trash2, Bus as BusIcon, Users, Hash, Tag, MapPin, BarChart3, Layout } from 'lucide-react';
import { Button } from 'shared/components/ui/button';
import { Card, CardContent } from 'shared/components/ui/card';
import { useToast } from 'shared/components/ui/toast';
import { ConfirmDialog } from 'shared/components/feedback/ConfirmDialog';
import EditBusDialog from '../../components/EditBusDialog/EditBusDialog';
import busService from '../../services/busService';
import layoutService from '../../../layouts/services/layoutService';
import routeService from '../../../routes/services/routeService';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';

const BusDetailPage = ({ busId, onBack }) => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const { addToast } = useToast();
  
  const [bus, setBus] = useState(null);
  const [layout, setLayout] = useState(null);
  const [route, setRoute] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [showEditDialog, setShowEditDialog] = useState(false);

  useEffect(() => {
    if (busId) {
      fetchBusDetail();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [busId]);

  const fetchBusDetail = async () => {
    try {
      setLoading(true);
      setError('');

      const busResponse = await busService.getBusById(busId);
      const busData = busResponse.data || busResponse;
      setBus(busData);

      // Fetch layout and route in parallel (non-blocking)
      const [layoutResult, routeResult] = await Promise.allSettled([
        busData.layout?.id ? layoutService.getLayoutById(busData.layout.id) : Promise.resolve(null),
        busData.route?.id ? routeService.getRouteById(busData.route.id) : Promise.resolve(null),
      ]);

      if (layoutResult.status === 'fulfilled' && layoutResult.value) {
        setLayout(layoutResult.value.data || layoutResult.value);
      }
      if (routeResult.status === 'fulfilled' && routeResult.value) {
        setRoute(routeResult.value.data || routeResult.value);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch bus details');
    } finally {
      setLoading(false);
    }
  };

  const handleSaveEdit = async (updatedBus) => {
    setBus(updatedBus);
    setShowEditDialog(false);
    addToast({
      message: t('busUpdatedSuccess') || 'Bus updated successfully!',
      type: 'success'
    });
  };

  const handleConfirmDelete = async () => {
    try {
      await busService.deleteBus(busId);
      addToast({
        message: t('busDeletedSuccess') || 'Bus deleted successfully',
        type: 'success'
      });
      setShowDeleteDialog(false);
      setTimeout(() => {
        onBack();
      }, 1500);
    } catch (err) {
      addToast({
        message: err.response?.data?.message || 'Failed to delete bus',
        type: 'error'
      });
      setShowDeleteDialog(false);
    }
  };

  if (loading) {
    return (
      <div className="flex-1 p-6 md:p-8 overflow-y-auto bg-gradient-to-br from-slate-50 via-purple-50/30 to-slate-50 dark:from-slate-900 dark:via-slate-900 dark:to-slate-900 min-h-screen">
        <div className="mb-6">
          <div className="h-10 w-40 bg-gray-200 dark:bg-gray-800 rounded-lg animate-pulse"></div>
        </div>
        <div className="space-y-6">
          <Card>
            <CardContent className="p-8">
              <div className="flex items-center gap-6">
                <div className="h-32 w-32 bg-gray-200 dark:bg-gray-800 rounded-2xl animate-pulse"></div>
                <div className="flex-1 space-y-3">
                  <div className="h-8 w-48 bg-gray-200 dark:bg-gray-800 rounded animate-pulse"></div>
                  <div className="h-6 w-32 bg-gray-200 dark:bg-gray-800 rounded animate-pulse"></div>
                  <div className="h-4 w-64 bg-gray-200 dark:bg-gray-800 rounded animate-pulse"></div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex-1 p-6 md:p-8 overflow-y-auto bg-gradient-to-br from-slate-50 via-purple-50/30 to-slate-50 dark:from-slate-900 dark:via-slate-900 dark:to-slate-900 min-h-screen">
        <Button variant="outline" onClick={onBack} className="flex items-center gap-2 mb-6">
          <ArrowLeft className="h-4 w-4" />
          {t('backToBuses') || 'Back to Buses'}
        </Button>
        <Card className="border-l-4 border-destructive bg-destructive/5">
          <CardContent className="p-6">
            <p className="text-destructive font-medium">{error}</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!bus) {
    return (
      <div className="flex-1 p-6 md:p-8 overflow-y-auto bg-gradient-to-br from-slate-50 via-purple-50/30 to-slate-50 dark:from-slate-900 dark:via-slate-900 dark:to-slate-900 min-h-screen">
        <Button variant="outline" onClick={onBack} className="flex items-center gap-2 mb-6">
          <ArrowLeft className="h-4 w-4" />
          {t('backToBuses') || 'Back to Buses'}
        </Button>
        <Card>
          <CardContent className="p-20 text-center">
            <p className="text-muted-foreground">{t('busNotFound') || 'Bus not found'}</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  const status = bus.status || bus.busStatus || 'Active';

  return (
    <div className="flex-1 p-6 md:p-8 overflow-y-auto bg-gradient-to-br from-slate-50 via-purple-50/30 to-slate-50 dark:from-slate-900 dark:via-slate-900 dark:to-slate-900 min-h-screen">
      <Button variant="outline" onClick={onBack} className="flex items-center gap-2 mb-6 hover:-translate-x-0.5 transition-transform">
        <ArrowLeft className="h-4 w-4" />
        {t('backToBuses') || 'Back to Buses'}
      </Button>

      <Card className="mb-6 overflow-hidden">
        <CardContent className="p-8">
          <div className="flex flex-col md:flex-row items-start md:items-center gap-6">
            <div className="bg-gradient-to-br from-purple-500 to-indigo-600 p-8 rounded-2xl text-white flex-shrink-0">
              <BusIcon className="h-16 w-16" />
            </div>
            <div className="flex-1">
              <h1 className="text-4xl font-bold text-foreground mb-2">{bus.busNumber}</h1>
              <span className={`inline-flex items-center gap-2 px-4 py-1.5 rounded-full text-sm font-semibold mb-3 ${
                status === 'Active' ? 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-400' :
                status === 'Standby' ? 'bg-amber-100 dark:bg-amber-900/30 text-amber-700 dark:text-amber-400' :
                status === 'Maintenance' ? 'bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-400' :
                status === 'InService' ? 'bg-cyan-100 dark:bg-cyan-900/30 text-cyan-700 dark:text-cyan-400' :
                'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-400'
              }`}>
                <div className={`w-2 h-2 rounded-full ${
                  status === 'Active' ? 'bg-emerald-500' :
                  status === 'Standby' ? 'bg-amber-500' :
                  status === 'Maintenance' ? 'bg-purple-500' :
                  status === 'InService' ? 'bg-cyan-500' :
                  'bg-gray-500'
                }`} />
                {status}
              </span>
              <div className="flex flex-wrap gap-4 text-sm text-muted-foreground">
                <span className="flex items-center gap-2">
                  <Users className="h-4 w-4" />
                  {bus.totalSeats} {t('seats') || 'seats'}
                </span>
                <span className="flex items-center gap-2">
                  <Hash className="h-4 w-4" />
                  {t('busId') || 'Bus ID'}: {bus.id}
                </span>
                <span className="flex items-center gap-2">
                  <Tag className="h-4 w-4" />
                  {bus.busType}
                </span>
              </div>
            </div>
            <div className="flex gap-3 flex-shrink-0">
              <Button onClick={() => setShowEditDialog(true)} className="gap-2">
                <Edit className="h-4 w-4" />
                {t('edit') || 'Edit'}
              </Button>
              <Button variant="destructive" onClick={() => setShowDeleteDialog(true)} className="gap-2">
                <Trash2 className="h-4 w-4" />
                {t('delete') || 'Delete'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card className="hover:shadow-lg transition-shadow">
          <CardContent className="p-6">
            <div className="flex items-center gap-3 mb-4 pb-4 border-b">
              <div className="bg-blue-100 dark:bg-blue-900/30 p-2 rounded-lg">
                <BusIcon className="h-5 w-5 text-blue-600 dark:text-blue-400" />
              </div>
              <h3 className="font-bold text-lg">{t('busInformation') || 'Bus Information'}</h3>
            </div>
            <div className="space-y-3">
              <div className="flex justify-between items-center py-2 border-b border-border/50">
                <span className="text-sm text-muted-foreground">{t('busNumber') || 'Bus Number'}</span>
                <span className="font-semibold">{bus.busNumber}</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-border/50">
                <span className="text-sm text-muted-foreground">{t('busType') || 'Bus Type'}</span>
                <span className="font-semibold">{bus.busType}</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-border/50">
                <span className="text-sm text-muted-foreground">{t('totalSeats') || 'Total Seats'}</span>
                <span className="font-semibold">{bus.totalSeats}</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-border/50">
                <span className="text-sm text-muted-foreground">{t('model') || 'Model'}</span>
                <span className="font-semibold">{bus.model || t('notSpecified') || 'N/A'}</span>
              </div>
              <div className="flex justify-between items-center py-2">
                <span className="text-sm text-muted-foreground">{t('plateNumber') || 'Plate Number'}</span>
                <span className="font-semibold">{bus.plate || t('notSpecified') || 'N/A'}</span>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="hover:shadow-lg transition-shadow">
          <CardContent className="p-6">
            <div className="flex items-center gap-3 mb-4 pb-4 border-b">
              <div className="bg-emerald-100 dark:bg-emerald-900/30 p-2 rounded-lg">
                <MapPin className="h-5 w-5 text-emerald-600 dark:text-emerald-400" />
              </div>
              <h3 className="font-bold text-lg">{t('routeLayout') || 'Route & Layout'}</h3>
            </div>
            <div className="space-y-3">
              <div className="py-2 border-b border-border/50">
                <span className="text-sm text-muted-foreground block mb-1">{t('route') || 'Route'}</span>
                {route ? (
                  <div className="font-semibold">
                    <div className="flex items-center gap-2">
                      <MapPin className="h-4 w-4 text-emerald-600" />
                      {route.origin} → {route.destination}
                    </div>
                    <div className="text-xs text-muted-foreground mt-1">
                      {route.distanceKm} km · {Math.floor(route.durationMinutes / 60)}h {route.durationMinutes % 60}m
                    </div>
                  </div>
                ) : (
                  <span className="font-semibold text-muted-foreground">
                    {bus.routeId ? `#${bus.routeId}` : t('notAssigned') || 'Not Assigned'}
                  </span>
                )}
              </div>
              
              <div className="py-2 border-b border-border/50">
                <span className="text-sm text-muted-foreground block mb-1">{t('layout') || 'Layout'}</span>
                {layout ? (
                  <div className="font-semibold">
                    <div className="flex items-center gap-2">
                      <Layout className="h-4 w-4 text-purple-600" />
                      {layout.name}
                    </div>
                    {layout.description && (
                      <div className="text-xs text-muted-foreground mt-1 italic">
                        {layout.description}
                      </div>
                    )}
                  </div>
                ) : (
                  <span className="font-semibold text-muted-foreground">
                    {bus.layoutId ? `#${bus.layoutId}` : t('notAssigned') || 'Not Assigned'}
                  </span>
                )}
              </div>
              
              <div className="flex justify-between items-center py-2">
                <span className="text-sm text-muted-foreground">{t('status') || 'Status'}</span>
                <span className={`inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold ${
                  status === 'Active' ? 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-400' :
                  status === 'Standby' ? 'bg-amber-100 dark:bg-amber-900/30 text-amber-700 dark:text-amber-400' :
                  status === 'Maintenance' ? 'bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-400' :
                  status === 'InService' ? 'bg-cyan-100 dark:bg-cyan-900/30 text-cyan-700 dark:text-cyan-400' :
                  'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-400'
                }`}>
                  {status}
                </span>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <EditBusDialog
        isOpen={showEditDialog}
        bus={bus}
        onSave={handleSaveEdit}
        onCancel={() => setShowEditDialog(false)}
      />

      <ConfirmDialog
        isOpen={showDeleteDialog}
        onConfirm={handleConfirmDelete}
        onCancel={() => setShowDeleteDialog(false)}
        title={t('deleteBus') || 'Delete Bus'}
        message={`${t('deleteBusConfirm') || 'Are you sure you want to delete'} ${bus?.busNumber}?`}
        confirmText={t('delete') || 'Delete'}
        cancelText={t('cancel') || 'Cancel'}
        type="danger"
      />
    </div>
  );
};

export default BusDetailPage;
