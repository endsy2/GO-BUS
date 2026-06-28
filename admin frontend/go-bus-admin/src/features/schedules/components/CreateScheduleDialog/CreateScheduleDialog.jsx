import React, { useState, useEffect } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from 'shared/components/ui/dialog';
import { Button } from 'shared/components/ui/button';
import { Label } from 'shared/components/ui/label';
import { Input } from 'shared/components/common/Input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from 'shared/components/ui/select';
import { DateTimePicker } from 'shared/components/ui/datetime-picker';
import { useToast } from 'shared/components/ui/toast';
import { Calendar, Bus, Clock, DollarSign, Loader2, CheckCircle2, XCircle, AlertTriangle } from 'lucide-react';
import scheduleService from '../../services/scheduleService';
import busService from '../../../buses/services/busService';
import { findConflictingSchedule } from '../../utils/scheduleConflict';

const fmt = (value) =>
  value ? new Date(value).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' }) : '';

const CreateScheduleDialog = ({ open, onClose, onSuccess }) => {
  const { addToast } = useToast();
  const [formData, setFormData] = useState({
    busId: '',
    departureDateTime: '',
    arrivalDateTime: '',
    price: '',
  });
  const [buses, setBuses] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingData, setLoadingData] = useState(true);

  // Existing schedules of the currently-selected bus — shown so the admin can
  // see which times are taken and pick a free slot before submitting.
  const [busSchedules, setBusSchedules] = useState([]);
  const [loadingSchedules, setLoadingSchedules] = useState(false);

  useEffect(() => {
    if (open) {
      // Start every open with a clean form so stale selections don't linger.
      setFormData({ busId: '', departureDateTime: '', arrivalDateTime: '', price: '' });
      fetchBuses();
    }
  }, [open]);

  const fetchBuses = async () => {
    setLoadingData(true);
    try {
      const busesRes = await busService.getBuses({ pageSize: 1000 });
      const busesData = busesRes.data?.content || busesRes.data || busesRes;
      setBuses(Array.isArray(busesData) ? busesData : []);
    } catch (error) {
      console.error('Failed to fetch buses:', error);
      addToast({ message: 'Failed to load buses. Please try again.', type: 'error' });
    } finally {
      setLoadingData(false);
    }
  };

  // Load the selected bus's schedules whenever the bus changes.
  useEffect(() => {
    if (!open || !formData.busId) {
      setBusSchedules([]);
      return;
    }
    let cancelled = false;
    setLoadingSchedules(true);
    scheduleService
      .getSchedulesByBus(parseInt(formData.busId))
      .then((res) => { if (!cancelled) setBusSchedules(res?.data || []); })
      .catch(() => { if (!cancelled) setBusSchedules([]); })
      .finally(() => { if (!cancelled) setLoadingSchedules(false); });
    return () => { cancelled = true; };
  }, [open, formData.busId]);

  // ── Live availability state (derived) ──────────────────────────────────────
  const hasTimes = Boolean(formData.departureDateTime && formData.arrivalDateTime);
  const departureInPast =
    Boolean(formData.departureDateTime) &&
    new Date(formData.departureDateTime).getTime() < Date.now();
  const invalidRange =
    hasTimes &&
    new Date(formData.arrivalDateTime).getTime() <= new Date(formData.departureDateTime).getTime();
  const conflict =
    hasTimes && !invalidRange
      ? findConflictingSchedule(busSchedules, formData.departureDateTime, formData.arrivalDateTime)
      : null;
  const blockSubmit = departureInPast || invalidRange || Boolean(conflict) || loadingSchedules;

  // Only show schedules departing today or later — past trips aren't relevant
  // when picking a new slot.
  const startOfToday = new Date();
  startOfToday.setHours(0, 0, 0, 0);
  const sortedSchedules = [...busSchedules]
    .filter((s) => new Date(s.departureDateTime).getTime() >= startOfToday.getTime())
    .sort((a, b) => new Date(a.departureDateTime) - new Date(b.departureDateTime));

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.busId || !formData.departureDateTime || !formData.arrivalDateTime || !formData.price) {
      addToast({ message: 'Please fill in all required fields.', type: 'error' });
      return;
    }
    if (departureInPast) {
      addToast({ message: 'Departure time cannot be in the past.', type: 'error' });
      return;
    }
    if (invalidRange) {
      addToast({ message: 'Arrival time must be after departure time.', type: 'error' });
      return;
    }
    if (conflict) {
      addToast({
        message: `This bus already has a schedule from ${fmt(conflict.departureDateTime)} to ${fmt(conflict.arrivalDateTime)}. Pick a different time or bus.`,
        type: 'error',
      });
      return;
    }
    try {
      setLoading(true);
      await scheduleService.createSchedule({
        busId: parseInt(formData.busId),
        price: parseFloat(formData.price),
        departureDateTime: formData.departureDateTime,
        arrivalDateTime: formData.arrivalDateTime,
      });
      onSuccess();
      onClose();
      setFormData({ busId: '', departureDateTime: '', arrivalDateTime: '', price: '' });
    } catch (error) {
      // Backend is the source of truth — surface its conflict message if our
      // local data was stale.
      addToast({ message: error.response?.data?.message || 'Failed to create schedule', type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const availableBuses = buses.filter(
    bus => (bus.status || bus.busStatus) === 'Active' || (bus.status || bus.busStatus) === 'Standby'
  );
  const unavailableBuses = buses.filter(
    bus => (bus.status || bus.busStatus) !== 'Active' && (bus.status || bus.busStatus) !== 'Standby'
  );

  const getBusLabel = (bus) =>
    [
      bus.busNumber,
      bus.busType,
      bus.totalSeats ? `${bus.totalSeats} seats` : null,
      bus.route ? `${bus.route.origin} → ${bus.route.destination}` : null,
      bus.plate,
      bus.status || bus.busStatus,
    ]
      .filter(Boolean)
      .join(' | ');

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-xl">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Calendar className="w-5 h-5 text-blue-400" />
            Create Schedule
          </DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <div className="space-y-5 py-4">
            {/* Bus Selection */}
            <div className="space-y-2">
              <Label className="flex items-center gap-2">
                <Bus className="w-4 h-4 text-blue-400" />
                Bus
              </Label>
              <Select
                value={formData.busId}
                onValueChange={(value) => setFormData(prev => ({ ...prev, busId: value }))}
                disabled={loadingData}
                required
              >
                <SelectTrigger>
                  <SelectValue placeholder={loadingData ? 'Loading buses...' : 'Select a bus'} />
                </SelectTrigger>
                <SelectContent>
                  {availableBuses.map(bus => (
                    <SelectItem key={bus.id} value={String(bus.id)}>
                      {getBusLabel(bus)}
                    </SelectItem>
                  ))}
                  {unavailableBuses.length > 0 && availableBuses.length > 0 && (
                    <div className="px-2 py-1 text-xs text-muted-foreground font-semibold">
                      ─── Unavailable Buses ───
                    </div>
                  )}
                  {unavailableBuses.map(bus => (
                    <SelectItem key={bus.id} value={String(bus.id)} disabled>
                      {getBusLabel(bus)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <p className="text-xs text-muted-foreground">
                Only Active and Standby buses are available for scheduling
              </p>
            </div>

            {/* Existing schedules for the selected bus — so the admin can pick a free slot */}
            {formData.busId && (
              <div className="rounded-lg border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900/50 p-3">
                <div className="flex items-center gap-2 mb-2 text-sm font-medium text-slate-700 dark:text-slate-300">
                  <Clock className="w-4 h-4 text-blue-400" />
                  Booked times for this bus
                </div>
                {loadingSchedules ? (
                  <p className="text-xs text-muted-foreground flex items-center gap-1.5">
                    <Loader2 className="w-3.5 h-3.5 animate-spin" /> Loading…
                  </p>
                ) : sortedSchedules.length === 0 ? (
                  <p className="text-xs text-emerald-600 dark:text-emerald-400">
                    No schedules yet — this bus is completely free.
                  </p>
                ) : (
                  <ul className="space-y-1 max-h-32 overflow-y-auto">
                    {sortedSchedules.map((s) => {
                      const isConflict = conflict && conflict.id === s.id;
                      return (
                        <li
                          key={s.id}
                          className={`text-xs flex items-center gap-2 rounded px-2 py-1 ${
                            isConflict
                              ? 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300'
                              : 'text-slate-600 dark:text-slate-400'
                          }`}
                        >
                          <span
                            className={`w-1.5 h-1.5 rounded-full flex-shrink-0 ${
                              isConflict ? 'bg-red-500' : 'bg-slate-400'
                            }`}
                          />
                          <span>{fmt(s.departureDateTime)} → {fmt(s.arrivalDateTime)}</span>
                          {isConflict && <span className="ml-auto font-semibold">Overlaps</span>}
                        </li>
                      );
                    })}
                  </ul>
                )}
              </div>
            )}

            {/* Departure DateTime */}
            <div className="space-y-2">
              <Label className="flex items-center gap-2">
                <Clock className="w-4 h-4 text-green-500" />
                Departure Date &amp; Time
              </Label>
              <DateTimePicker
                value={formData.departureDateTime}
                onChange={(value) => setFormData(prev => ({ ...prev, departureDateTime: value }))}
                placeholder="Select Departure Date And Time"
              />
            </div>

            {/* Arrival DateTime */}
            <div className="space-y-2">
              <Label className="flex items-center gap-2">
                <Clock className="w-4 h-4 text-orange-400" />
                Arrival Date &amp; Time
              </Label>
              <DateTimePicker
                value={formData.arrivalDateTime}
                onChange={(value) => setFormData(prev => ({ ...prev, arrivalDateTime: value }))}
                placeholder="Select Arrival Date And Time"
              />
            </div>

            {/* Live availability status for the chosen bus + time */}
            {formData.busId && formData.departureDateTime && (
              departureInPast ? (
                <div className="flex items-center gap-2 text-sm rounded-lg px-3 py-2 bg-amber-100 dark:bg-amber-900/30 text-amber-700 dark:text-amber-300">
                  <AlertTriangle className="w-4 h-4 flex-shrink-0" />
                  Departure time is in the past. Choose a future time.
                </div>
              ) : invalidRange ? (
                <div className="flex items-center gap-2 text-sm rounded-lg px-3 py-2 bg-amber-100 dark:bg-amber-900/30 text-amber-700 dark:text-amber-300">
                  <AlertTriangle className="w-4 h-4 flex-shrink-0" />
                  Arrival must be after departure.
                </div>
              ) : hasTimes && conflict ? (
                <div className="flex items-center gap-2 text-sm rounded-lg px-3 py-2 bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300">
                  <XCircle className="w-4 h-4 flex-shrink-0" />
                  <span>
                    Conflicts with {fmt(conflict.departureDateTime)} → {fmt(conflict.arrivalDateTime)}. Pick another time or bus.
                  </span>
                </div>
              ) : hasTimes ? (
                <div className="flex items-center gap-2 text-sm rounded-lg px-3 py-2 bg-emerald-100 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-300">
                  <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
                  This bus is free for the selected time.
                </div>
              ) : null
            )}

            {/* Price */}
            <Input
              label={
                <span className="flex items-center gap-2">
                  <DollarSign className="w-4 h-4 text-emerald-500" />
                  Price ($)
                </span>
              }
              name="price"
              type="number"
              step="0.01"
              min="0"
              value={formData.price}
              onChange={(e) => setFormData(prev => ({ ...prev, price: e.target.value }))}
              required
              placeholder="0.00"
            />
          </div>

          <DialogFooter className="gap-2 pt-4">
            <Button
              type="button"
              variant="outline"
              onClick={onClose}
              disabled={loading}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={loading || loadingData || blockSubmit}
              className="flex items-center gap-2"
            >
              {loading && <Loader2 className="w-4 h-4 animate-spin" />}
              {loading ? 'Creating...' : 'Create Schedule'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default CreateScheduleDialog;
