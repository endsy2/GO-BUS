import React, { useEffect, useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from 'shared/components/ui/dialog';
import { Button } from 'shared/components/ui/button';
import { Label } from 'shared/components/ui/label';
import { Input } from 'shared/components/common/Input';
import { DateTimePicker } from 'shared/components/ui/datetime-picker';
import { useToast } from 'shared/components/ui/toast';
import scheduleService from '../../services/scheduleService';
import { findConflictingSchedule } from '../../utils/scheduleConflict';

const normalizeTime = (value) => {
  if (!value) return '';

  if (value.includes('T')) {
    return value.split('T')[1].slice(0, 5);
  }

  return value.slice(0, 5);
};

const normalizeDateTimeForPicker = (dateTime, date, time) => {
  if (dateTime) {
    return dateTime.slice(0, 16);
  }

  if (date && time) {
    const datePart = date.split('T')[0];
    const timePart = normalizeTime(time);
    return `${datePart}T${timePart}`;
  }

  return '';
};

const toIsoWithSeconds = (value) => {
  if (!value) return '';
  if (value.length === 16) return `${value}:00`;
  return value;
};

const buildInitialFormData = (schedule) => ({
  busId: schedule?.busId || schedule?.bus?.id || '',
  departureDateTime: normalizeDateTimeForPicker(
    schedule?.departureDateTime,
    schedule?.departureDate,
    schedule?.departureTime
  ),
  arrivalDateTime: normalizeDateTimeForPicker(
    schedule?.arrivalDateTime,
    schedule?.departureDate,
    schedule?.arrivalTime
  ),
  price: schedule?.price || '',
});

const EditScheduleDialog = ({ open, schedule, onClose, onSuccess }) => {
  const { addToast } = useToast();
  const [formData, setFormData] = useState(() => buildInitialFormData(schedule));
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setFormData(buildInitialFormData(schedule));
  }, [schedule]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    const departureDateTime = formData.departureDateTime || normalizeDateTimeForPicker(
      schedule?.departureDateTime,
      schedule?.departureDate,
      schedule?.departureTime
    );

    const arrivalDateTime = formData.arrivalDateTime || normalizeDateTimeForPicker(
      schedule?.arrivalDateTime,
      schedule?.departureDate,
      schedule?.arrivalTime
    );

     const payload = {
      busId: formData.busId || schedule?.busId || schedule?.bus?.id,
      price: parseFloat(formData.price),
      departureDateTime: toIsoWithSeconds(departureDateTime),
      // departureTime: normalizeTime(departureDateTime),
      arrivalDateTime: toIsoWithSeconds(arrivalDateTime),
    };

    if (!payload.busId || !payload.departureDateTime || !payload.arrivalDateTime || Number.isNaN(payload.price)) {
      addToast({ message: 'Please provide departure date/time, arrival date/time, and a valid price.', type: 'error' });
      return;
    }
    if (new Date(payload.arrivalDateTime).getTime() <= new Date(payload.departureDateTime).getTime()) {
      addToast({ message: 'Arrival time must be after departure time.', type: 'error' });
      return;
    }

    try {
      setLoading(true);

      // Fast guard: a bus can't run two overlapping trips. Exclude this schedule
      // from the check. The backend enforces this too.
      const existingRes = await scheduleService.getSchedulesByBus(payload.busId);
      const conflict = findConflictingSchedule(
        existingRes?.data || [],
        payload.departureDateTime,
        payload.arrivalDateTime,
        schedule.id,
      );
      if (conflict) {
        addToast({
          message: `This bus already has a schedule from ${new Date(conflict.departureDateTime).toLocaleString()} to ${new Date(conflict.arrivalDateTime).toLocaleString()}. Pick a different time or bus.`,
          type: 'error',
        });
        return;
      }

      await scheduleService.updateSchedule(schedule.id, payload);
      onSuccess();
      onClose();
    } catch (error) {
      console.error('Failed to update schedule:', error);
      addToast({ message: error.response?.data?.message || 'Failed to update schedule', type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Edit Schedule</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Route</Label>
              <div className="rounded-lg border border-input bg-muted px-3 py-2 text-sm text-foreground">
                {schedule?.route?.origin && schedule?.route?.destination
                  ? `${schedule.route.origin} → ${schedule.route.destination}`
                  : 'Route not assigned'}
              </div>
            </div>
            <div className="space-y-2">
              <Label>Departure Time</Label>
              <DateTimePicker
                value={formData.departureDateTime}
                onChange={(value) => setFormData({ ...formData, departureDateTime: value })}
                placeholder="Select Departure Date And Time"
              />
            </div>
            <div className="space-y-2">
              <Label>Arrival Time</Label>
              <DateTimePicker
                value={formData.arrivalDateTime}
                onChange={(value) => setFormData({ ...formData, arrivalDateTime: value })}
                placeholder="Select Arrival Date And Time"
              />
            </div>
            <Input
              label="Price ($)"
              name="price"
              type="number"
              step="0.01"
              min="0"
              value={formData.price}
              onChange={(e) => setFormData({ ...formData, price: e.target.value })}
              required
              placeholder="0.00"
            />
          </div>
          <DialogFooter className="mt-6">
            <Button
              type="button"
              variant="outline"
              onClick={onClose}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={loading}
              className="flex items-center gap-2"
            >
              {loading && <span className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin" />}
              {loading ? 'Updating...' : 'Update'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default EditScheduleDialog;
