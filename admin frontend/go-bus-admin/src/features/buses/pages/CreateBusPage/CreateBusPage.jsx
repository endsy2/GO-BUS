import React, { useState, useEffect } from 'react';
import { ArrowLeft, Check } from 'lucide-react';
import { Button } from 'shared/components/ui/button';
import { Card, CardContent } from 'shared/components/ui/card';
import busService from '../../services/busService';
import routeService from 'features/routes/services/routeService';
import layoutService from 'features/layouts/services/layoutService';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';

const BUS_TYPES = ['SLEEPER', 'SEATER'];
const STATUS_OPTIONS = ['Active', 'Standby', 'Maintenance', 'Inactive', 'InService'];

const CreateBusPage = ({ onBack, onSuccess }) => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;

  const [routes, setRoutes] = useState([]);
  const [layouts, setLayouts] = useState([]);
  const [loadingData, setLoadingData] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [errors, setErrors] = useState({});

  const [form, setForm] = useState({
    routeId: '',
    busNumber: '',
    model: '',
    plate: '',
    busType: 'SLEEPER',
    layoutId: '',
    busStatus: 'Active',
  });

  useEffect(() => {
    const fetchData = async () => {
      setLoadingData(true);
      await Promise.all([fetchRoutes(), fetchLayouts()]);
      setLoadingData(false);
    };
    fetchData();
  }, []);

  const fetchRoutes = async () => {
    try {
      const result = await routeService.getRoutes();
      setRoutes(result.data || result || []);
    } catch {
      // Non-critical — form will show empty dropdown
    }
  };

  const fetchLayouts = async () => {
    try {
      const result = await layoutService.getAllLayouts();
      setLayouts(result.data || result || []);
    } catch {
      // Non-critical — form will show empty dropdown
    }
  };

  const selectedRoute = routes.find(r => String(r.id) === String(form.routeId));

  const parseLayout = (raw) => {
    try {
      const parsed = typeof raw === 'string' ? JSON.parse(raw) : raw;
      
      // The layout structure from DB is:
      // { rows: number, columns: number, totalSeats: number, seats: [...], driverColumn: number, aisleColumns: [...] }
      if (parsed && typeof parsed === 'object') {
        return parsed;
      }
      return { rows: 0, columns: 0, totalSeats: 0, seats: [] };
    } catch {
      return { rows: 0, columns: 0, totalSeats: 0, seats: [] };
    }
  };

  const countSeats = (layout) => {
    const parsed = parseLayout(layout);
    // Use totalSeats from layout or count seats array
    return parsed.totalSeats || parsed.seats?.length || 0;
  };

  const getRowCount = (layout) => {
    const parsed = parseLayout(layout);
    return parsed.rows || 0;
  };

  const set = (field, value) => {
    setForm(prev => ({ ...prev, [field]: value }));
    if (errors[field]) setErrors(prev => ({ ...prev, [field]: '' }));
  };

  const validate = () => {
    const e = {};
    if (!form.routeId) e.routeId = 'Route is required';
    if (!form.busNumber.trim()) e.busNumber = 'Bus number is required';
    if (!form.busType) e.busType = 'Bus type is required';
    if (!form.layoutId) e.layoutId = 'Layout is required';
    return e;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }

    setSubmitting(true);
    try {
      const payload = {
        routeId: Number(form.routeId),
        busNumber: form.busNumber.trim(),
        busType: form.busType,
        layoutId: form.layoutId ? Number(form.layoutId) : null,
        plate: form.plate.trim() || null,
        model: form.model.trim() || null,
        busStatus: form.busStatus || null,
      };

      const result = await busService.createBus(payload);
      onSuccess && onSuccess(result.data || result);
    } catch (err) {
      setErrors({ submit: err.response?.data?.message || 'Failed to create bus. Please try again.' });
    } finally {
      setSubmitting(false);
    }
  };

  if (loadingData) {
    return (
      <div className="flex-1 p-6 md:p-8 overflow-y-auto bg-gradient-to-br from-slate-50 via-purple-50/30 to-slate-50 dark:from-slate-900 dark:via-slate-900 dark:to-slate-900 min-h-screen">
        <div className="flex items-center justify-center py-20">
          <div className="flex flex-col items-center gap-4">
            <div className="h-12 w-12 border-4 border-purple-200 dark:border-purple-800 border-t-purple-600 dark:border-t-purple-400 rounded-full animate-spin" />
            <span className="text-muted-foreground">{t('loading') || 'Loading...'}</span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex-1 p-6 md:p-8 overflow-y-auto bg-gradient-to-br from-slate-50 via-purple-50/30 to-slate-50 dark:from-slate-900 dark:via-slate-900 dark:to-slate-900 min-h-screen">
      <div className="mb-6">
        <Button variant="outline" onClick={onBack} className="flex items-center gap-2 hover:-translate-x-0.5 transition-transform">
          <ArrowLeft className="h-4 w-4" />
          {t('buses') || 'Buses'}
        </Button>
      </div>

      <div className="mb-8">
        <h1 className="text-4xl font-bold mb-2 bg-gradient-to-r from-slate-900 to-slate-700 dark:from-slate-100 dark:to-slate-300 bg-clip-text text-transparent">
          {t('addNewBus') || 'Add New Bus'}
        </h1>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6" noValidate>
        <Card>
          <CardContent className="p-6">
            <h2 className="text-sm font-semibold text-muted-foreground uppercase tracking-wide mb-4 pb-3 border-b">
              {t('routeAssignment') || 'Route Assignment'}
            </h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-2">
                  {t('route') || 'Route'} <span className="text-red-500">*</span>
                </label>
                <select
                  value={form.routeId}
                  onChange={e => set('routeId', e.target.value)}
                  className={`w-full px-3 py-2 border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring ${errors.routeId ? 'border-red-500' : 'border-input'}`}
                >
                  <option value="">{t('selectRoute') || 'Select a route...'}</option>
                  {routes.map(r => (
                    <option key={r.id} value={r.id}>
                      {r.origin} → {r.destination}
                    </option>
                  ))}
                </select>
                {errors.routeId && <p className="text-xs text-red-600 dark:text-red-400 mt-1">{errors.routeId}</p>}
              </div>

              {selectedRoute && (
                <div className="grid grid-cols-2 md:grid-cols-4 gap-3 p-4 bg-muted/50 rounded-lg">
                  <div>
                    <span className="text-xs text-muted-foreground">{t('origin') || 'Origin'}</span>
                    <p className="font-semibold text-sm">{selectedRoute.origin}</p>
                  </div>
                  <div>
                    <span className="text-xs text-muted-foreground">{t('destination') || 'Destination'}</span>
                    <p className="font-semibold text-sm">{selectedRoute.destination}</p>
                  </div>
                  <div>
                    <span className="text-xs text-muted-foreground">{t('distance') || 'Distance'}</span>
                    <p className="font-semibold text-sm">{selectedRoute.distanceKm} km</p>
                  </div>
                  <div>
                    <span className="text-xs text-muted-foreground">{t('duration') || 'Duration'}</span>
                    <p className="font-semibold text-sm">
                      {Math.floor(selectedRoute.durationMinutes / 60)}h {selectedRoute.durationMinutes % 60 > 0 ? `${selectedRoute.durationMinutes % 60}m` : ''}
                    </p>
                  </div>
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <h2 className="text-sm font-semibold text-muted-foreground uppercase tracking-wide mb-4 pb-3 border-b">
              {t('busDetails') || 'Bus Details'}
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-2">
                  {t('busNumber') || 'Bus Number'} <span className="text-red-500">*</span>
                </label>
                <input
                  value={form.busNumber}
                  onChange={e => set('busNumber', e.target.value)}
                  placeholder="e.g. SR-001"
                  className={`w-full px-3 py-2 border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring ${errors.busNumber ? 'border-red-500' : 'border-input'}`}
                />
                {errors.busNumber && <p className="text-xs text-red-600 dark:text-red-400 mt-1">{errors.busNumber}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium mb-2">{t('model') || 'Model'}</label>
                <input
                  value={form.model}
                  onChange={e => set('model', e.target.value)}
                  placeholder="e.g. BYD, Yutong"
                  className="w-full px-3 py-2 border border-input rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-2">{t('plateNumber') || 'Plate Number'}</label>
                <input
                  value={form.plate}
                  onChange={e => set('plate', e.target.value)}
                  placeholder="e.g. 1KY-XXXX"
                  className="w-full px-3 py-2 border border-input rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring"
                />
              </div>
            </div>
            <p className="text-xs text-muted-foreground mt-3">
              {t('totalSeatsAutoCalculated') || 'Total seats will be automatically calculated from the selected layout'}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <h2 className="text-sm font-semibold text-muted-foreground uppercase tracking-wide mb-4 pb-3 border-b">
              {t('busType') || 'Bus Type'}
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              {BUS_TYPES.map(type => (
                <button
                  key={type}
                  type="button"
                  onClick={() => set('busType', type)}
                  className={`p-4 border-2 rounded-lg transition-all ${
                    form.busType === type
                      ? 'border-purple-600 bg-purple-50 dark:bg-purple-900/20'
                      : 'border-border hover:border-purple-300 dark:hover:border-purple-700'
                  }`}
                >
                  <div className="font-semibold text-sm mb-1">{type.replace('_', ' ')}</div>
                  <div className="text-xs text-muted-foreground">
                    {type === 'SLEEPER' ? 'Reclining beds' : 'Standard seats'}
                  </div>
                  {form.busType === type && (
                    <div className="mt-2 flex justify-center">
                      <Check className="h-5 w-5 text-purple-600" />
                    </div>
                  )}
                </button>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <h2 className="text-sm font-semibold text-muted-foreground uppercase tracking-wide mb-4 pb-3 border-b">
              {t('seatLayout') || 'Seat Layout'} <span className="text-red-500">*</span>
            </h2>
            {errors.layoutId && <p className="text-xs text-red-600 dark:text-red-400 mb-3">{errors.layoutId}</p>}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {layouts.map(layout => {
                const parsed = parseLayout(layout.layout);
                const totalSeats = countSeats(layout.layout);
                const rowCount = getRowCount(layout.layout);
                const allSeats = parsed.seats || [];
                
                return (
                  <button
                    key={layout.id}
                    type="button"
                    onClick={() => set('layoutId', layout.id)}
                    className={`p-4 border-2 rounded-lg transition-all text-left ${
                      String(form.layoutId) === String(layout.id)
                        ? 'border-purple-600 bg-purple-50 dark:bg-purple-900/20'
                        : 'border-border hover:border-purple-300 dark:hover:border-purple-700'
                    }`}
                  >
                    <div className="font-semibold mb-1">{layout.name}</div>
                    <div className="text-xs text-muted-foreground mb-3">
                      {totalSeats} seats · {rowCount} rows · {parsed.columns || 0} columns
                    </div>
                    {layout.description && (
                      <div className="text-xs text-muted-foreground mb-3 italic">
                        {layout.description}
                      </div>
                    )}
                    <div className="flex flex-wrap gap-1">
                      {allSeats.slice(0, 20).map((seat, i) => {
                        // Handle both object {seatNumber, isAvailable} and string formats
                        const seatNumber = typeof seat === 'object' ? seat.seatNumber : seat;
                        return (
                          <div 
                            key={i} 
                            className="w-6 h-5 border border-border rounded text-[9px] flex items-center justify-center bg-muted/50 font-mono" 
                            title={`Seat ${seatNumber}`}
                          >
                            {seatNumber}
                          </div>
                        );
                      })}
                      {allSeats.length > 20 && (
                        <div className="px-2 h-5 border border-border rounded text-[9px] flex items-center justify-center bg-muted/50">
                          +{allSeats.length - 20}
                        </div>
                      )}
                    </div>
                    {String(form.layoutId) === String(layout.id) && (
                      <div className="mt-3 flex justify-center">
                        <Check className="h-5 w-5 text-purple-600" />
                      </div>
                    )}
                  </button>
                );
              })}
              {layouts.length === 0 && (
                <p className="col-span-2 text-sm text-muted-foreground py-8 text-center">
                  {t('noLayouts') || 'No layouts available. Create a layout first.'}
                </p>
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <h2 className="text-sm font-semibold text-muted-foreground uppercase tracking-wide mb-4 pb-3 border-b">
              {t('status') || 'Status'}
            </h2>
            <div className="flex flex-wrap gap-3">
              {STATUS_OPTIONS.map(s => (
                <button
                  key={s}
                  type="button"
                  onClick={() => set('busStatus', s)}
                  className={`px-4 py-2 rounded-lg font-semibold text-sm transition-all ${
                    form.busStatus === s
                      ? s === 'Active' ? 'bg-emerald-600 text-white' :
                        s === 'Standby' ? 'bg-amber-600 text-white' :
                        s === 'Maintenance' ? 'bg-purple-600 text-white' :
                        s === 'InService' ? 'bg-cyan-600 text-white' :
                        'bg-gray-600 text-white'
                      : 'bg-muted text-muted-foreground hover:bg-muted/80'
                  }`}
                >
                  {s}
                </button>
              ))}
            </div>
          </CardContent>
        </Card>

        {errors.submit && (
          <Card className="border-l-4 border-destructive bg-destructive/5">
            <CardContent className="p-4">
              <p className="text-sm text-destructive font-medium">{errors.submit}</p>
            </CardContent>
          </Card>
        )}

        <div className="flex justify-end gap-3">
          <Button type="button" variant="outline" onClick={onBack} disabled={submitting}>
            {t('cancel') || 'Cancel'}
          </Button>
          <Button type="submit" disabled={submitting} className="min-w-32">
            {submitting ? (t('creating') || 'Creating...') : (t('createBus') || 'Create Bus')}
          </Button>
        </div>
      </form>
    </div>
  );
};

export default CreateBusPage;
