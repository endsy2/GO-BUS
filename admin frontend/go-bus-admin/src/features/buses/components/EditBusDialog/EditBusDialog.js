import React, { useState, useEffect, useCallback } from 'react';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'shared/components/ui/dialog';
import { Button } from 'shared/components/ui/button';
import { Input } from 'shared/components/ui/input';
import { Label } from 'shared/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from 'shared/components/ui/select';
import { Loader2 } from 'lucide-react';
import busService from '../../services/busService';
import routeService from 'features/routes/services/routeService';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import { cn } from 'lib/utils';

const BUS_TYPES = ['SLEEPER', 'SEATER'];
const STATUS_OPTIONS = ['Active', 'Standby', 'Maintenance', 'Inactive', 'InService'];

const EditBusDialog = ({ isOpen, bus, onSave, onCancel }) => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;

  const [routes, setRoutes] = useState([]);
  const [loadingData, setLoadingData] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [errors, setErrors] = useState({});

  const [form, setForm] = useState({
    routeId: '',
    busNumber: '',
    model: '',
    plate: '',
    totalSeats: '',
    busType: 'SLEEPER',
    busStatus: 'Active',
  });

  const fetchRoutes = useCallback(async () => {
    try {
      const result = await routeService.getRoutes();
      setRoutes(result.data || result || []);
    } catch {
      // Non-critical — dropdown will show empty
    }
  }, []);

  const fetchData = useCallback(async () => {
    setLoadingData(true);
    await fetchRoutes();
    setLoadingData(false);
  }, [fetchRoutes]);

  useEffect(() => {
    if (isOpen && bus) {
      setForm({
        routeId: bus.routeId || '',
        busNumber: bus.busNumber || '',
        model: bus.model || '',
        plate: bus.plate || '',
        totalSeats: bus.totalSeats || '',
        busType: bus.busType || 'SLEEPER',
        busStatus: bus.status || bus.busStatus || 'Active',
      });
      fetchData();
    }
  }, [isOpen, bus, fetchData]);

  const set = (field, value) => {
    setForm(prev => ({ ...prev, [field]: value }));
    if (errors[field]) setErrors(prev => ({ ...prev, [field]: '' }));
  };

  const validate = () => {
    const e = {};
    if (!form.routeId) e.routeId = 'Route is required';
    if (!form.busNumber.trim()) e.busNumber = 'Bus number is required';
    if (!form.plate.trim()) e.plate = 'Plate number is required';
    if (form.totalSeats && isNaN(Number(form.totalSeats))) e.totalSeats = 'Must be a number';
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
        model: form.model.trim() || undefined,
        plate: form.plate.trim(),
        totalSeats: form.totalSeats ? Number(form.totalSeats) : undefined,
        busType: form.busType,
        busStatus: form.busStatus,
      };

      const result = await busService.updateBus(bus.id, payload);
      onSave && onSave(result.data || result);
    } catch (err) {
      setErrors({ submit: err.response?.data?.message || 'Failed to update bus. Please try again.' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = () => {
    setErrors({});
    onCancel();
  };

  const selectedRoute = routes.find(r => String(r.id) === String(form.routeId));

  return (
    <Dialog open={isOpen} onOpenChange={handleCancel}>
      <DialogContent className="max-w-[600px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{t('editBus') || 'Edit Bus'}</DialogTitle>
        </DialogHeader>

        {loadingData ? (
          <div className="flex items-center justify-center gap-2 py-10 text-muted-foreground">
            <Loader2 className="h-5 w-5 animate-spin" />
            <span>{t('loading')}</span>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Route */}
            <div className="space-y-3">
              <h3 className="text-sm font-semibold pb-2 border-b">{t('routeAssignment') || 'Route Assignment'}</h3>
              <div className="space-y-2">
                <Label>{t('route') || 'Route'} <span className="text-destructive">*</span></Label>
                <Select value={String(form.routeId)} onValueChange={(val) => set('routeId', val)}>
                  <SelectTrigger className={errors.routeId ? 'border-destructive' : ''}>
                    <SelectValue placeholder={t('selectRoute') || 'Select Route'} />
                  </SelectTrigger>
                  <SelectContent>
                    {routes.length === 0 ? (
                      <div className="p-2 text-sm text-muted-foreground">No routes available</div>
                    ) : (
                      routes.map(r => (
                        <SelectItem key={r.id} value={String(r.id)}>
                          {r.origin} → {r.destination}
                        </SelectItem>
                      ))
                    )}
                  </SelectContent>
                </Select>
                {errors.routeId && <p className="text-sm text-destructive">{errors.routeId}</p>}
              </div>
              {selectedRoute && (
                <div className="flex items-center gap-3 p-2 bg-muted rounded-md text-xs text-muted-foreground">
                  <span>{selectedRoute.origin} → {selectedRoute.destination}</span>
                  <span>{selectedRoute.distanceKm} km</span>
                </div>
              )}
            </div>

            {/* Bus Details */}
            <div className="space-y-3">
              <h3 className="text-sm font-semibold pb-2 border-b">{t('busDetails') || 'Bus Details'}</h3>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-2">
                  <Label>{t('busNumber') || 'Bus Number'} <span className="text-destructive">*</span></Label>
                  <Input
                    value={form.busNumber}
                    onChange={(e) => set('busNumber', e.target.value)}
                    placeholder="e.g. SR-001"
                    className={errors.busNumber ? 'border-destructive' : ''}
                  />
                  {errors.busNumber && <p className="text-sm text-destructive">{errors.busNumber}</p>}
                </div>
                <div className="space-y-2">
                  <Label>{t('model') || 'Model'}</Label>
                  <Input
                    value={form.model}
                    onChange={(e) => set('model', e.target.value)}
                    placeholder="e.g. BYD, Yutong"
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-2">
                  <Label>{t('plateNumber') || 'Plate Number'} <span className="text-destructive">*</span></Label>
                  <Input
                    value={form.plate}
                    onChange={(e) => set('plate', e.target.value)}
                    placeholder="e.g. 1KY-XXXX"
                    className={errors.plate ? 'border-destructive' : ''}
                  />
                  {errors.plate && <p className="text-sm text-destructive">{errors.plate}</p>}
                </div>
              </div>
            </div>

            {/* Bus Type */}
            <div className="space-y-3">
              <h3 className="text-sm font-semibold pb-2 border-b">{t('busType') || 'Bus Type'}</h3>
              <div className="grid grid-cols-2 gap-2">
                {BUS_TYPES.map(type => (
                  <button
                    key={type}
                    type="button"
                    className={cn(
                      "p-3 border rounded-lg text-xs font-medium text-center cursor-pointer transition-all",
                      form.busType === type 
                        ? "border-2 border-primary bg-primary/10 text-primary" 
                        : "border-border bg-background text-muted-foreground hover:bg-accent hover:border-primary"
                    )}
                    onClick={() => set('busType', type)}
                  >
                    {type.replace('_', ' ')}
                  </button>
                ))}
              </div>
            </div>

            {/* Status */}
            <div className="space-y-3">
              <h3 className="text-sm font-semibold pb-2 border-b">{t('status') || 'Status'}</h3>
              <div className="grid grid-cols-3 gap-2">
                {STATUS_OPTIONS.map(s => (
                  <button
                    key={s}
                    type="button"
                    className={cn(
                      "p-3 border rounded-lg text-xs font-medium text-center cursor-pointer transition-all",
                      form.busStatus === s 
                        ? "border-2 border-primary bg-primary/10 text-primary" 
                        : "border-border bg-background text-muted-foreground hover:bg-accent hover:border-primary"
                    )}
                    onClick={() => set('busStatus', s)}
                  >
                    {s}
                  </button>
                ))}
              </div>
            </div>

            {errors.submit && (
              <div className="p-3 bg-destructive/10 border border-destructive/20 rounded-md text-sm text-destructive">
                {errors.submit}
              </div>
            )}
          </form>
        )}

        <DialogFooter>
          <Button type="button" variant="outline" onClick={handleCancel} disabled={submitting}>
            {t('cancel') || 'Cancel'}
          </Button>
          <Button type="submit" onClick={handleSubmit} disabled={submitting || loadingData}>
            {submitting ? (t('updating') || 'Updating...') : (t('updateBus') || 'Update Bus')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default EditBusDialog;
