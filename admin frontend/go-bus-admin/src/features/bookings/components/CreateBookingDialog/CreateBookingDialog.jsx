import React, { useState, useEffect, useCallback, useMemo } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from 'shared/components/ui/dialog';
import { Input } from 'shared/components/common/Input';
import { Button } from 'shared/components/common/Button';
import { Label } from 'shared/components/ui/label';
import { DateTimePicker } from 'shared/components/ui/datetime-picker';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import { useToast } from 'shared/components/ui/toast';
import { Loader2, Ticket, Calendar, Bus, Armchair, MapPin, Clock, X, Wifi, WifiOff } from 'lucide-react';
import bookingService from '../../services/bookingService';
import scheduleService from '../../../schedules/services/scheduleService';
import routeService from '../../../routes/services/routeService';
import { useSeatWebSocket } from '../../hooks/useSeatWebSocket';

const CreateBookingDialog = ({ open, onClose, onSuccess }) => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const { addToast } = useToast();
  
  const [step, setStep] = useState(1); // 1: Schedule, 2: Bus & Seats, 3: Confirm
  const [schedules, setSchedules] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [scheduleSeats, setScheduleSeats] = useState([]); // List of ScheduleSeatResponse
  const [busDetails, setBusDetails] = useState(null);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingSchedules, setLoadingSchedules] = useState(false);
  const [loadingRoutes, setLoadingRoutes] = useState(false);
  const [loadingBus, setLoadingBus] = useState(false);
  
  const [formData, setFormData] = useState({
    scheduleId: '',
    phoneNumber: '',
  });
  const [scheduleFilters, setScheduleFilters] = useState({
    routeId: '',
    fromDate: '',
    toDate: '',
    maxPrice: '',
  });
  const [errors, setErrors] = useState({});

  /**
   * The user ID does not change during a session — memoising it avoids a
   * localStorage read and JSON.parse on every single render of this dialog.
   * Computed once when the component mounts.
   */
  const currentUserId = useMemo(() => {
    try {
      const user = JSON.parse(localStorage.getItem('user') || 'null');
      return user?.id ?? null;
    } catch {
      return null;
    }
  }, []);

  // Handle seat updates from WebSocket
  const handleSeatUpdate = useCallback((data) => {

    // Update seat status in real-time
    setScheduleSeats(prevSeats => {
      
      const updatedSeats = prevSeats.map(seat => {
        if (seat.id === data.seatId) {
          
          return {
            ...seat,
            status: data.status,
            bookingId: data.bookingId,
            pendingUserId: data.userId
          };
        }
        return seat;
      });
      
      return updatedSeats;
    });

    // Handle different event types
    const isMyAction = data.userId === currentUserId;

    switch (data.type) {
      case 'SEAT_SELECTED':
        // Don't show notification for seat selections
        break;

      case 'SEAT_DESELECTED':
        // Don't show notification for seat deselections
        break;

      case 'SEAT_SELECTION_EXPIRED':
        addToast({
          message: `Seat ${data.seatNumber} selection expired`,
          type: 'info'
        });
        break;

      case 'SEAT_BOOKED':
        // Remove from selected seats if it was booked by someone else
        setSelectedSeats(prevSelected => {
          const wasSelected = prevSelected.some(s => s.id === data.seatId);
          if (wasSelected && !isMyAction) {
            addToast({
              message: `Seat ${data.seatNumber} was just booked by another user`,
              type: 'warning'
            });
          }
          return prevSelected.filter(s => s.id !== data.seatId);
        });
        break;

      case 'SEAT_RELEASED':
        // Don't show notification for seat releases
        break;

      default:
    }
  }, [addToast, currentUserId]);

  /**
   * Subscribe to real-time seat updates only when:
   *   - the dialog is open
   *   - the user is on the seat-selection step
   *   - a schedule has been chosen
   *
   * isConnected is reactive state (driven by the service's event emitter) so
   * the Live/Offline indicator updates automatically — no polling useEffect needed.
   */
  const wsEnabled = Boolean(open && step === 2 && formData.scheduleId);
  const { isConnected: wsConnected, sendMessage } = useSeatWebSocket(
    formData.scheduleId ? parseInt(formData.scheduleId) : null,
    handleSeatUpdate,
    wsEnabled
  );

  // Fetch schedules with optional filters
  const fetchSchedules = useCallback(async () => {
    setLoadingSchedules(true);
    setErrors({});
    try {
      // Only send date filters if BOTH are provided (backend requirement)
      const fromDate = (scheduleFilters.fromDate && scheduleFilters.toDate) ? scheduleFilters.fromDate : null;
      const toDate = (scheduleFilters.fromDate && scheduleFilters.toDate) ? scheduleFilters.toDate : null;
      
      const response = await scheduleService.filterSchedules(
        scheduleFilters.routeId || null,
        fromDate,
        toDate,
        scheduleFilters.maxPrice || null,
        1,
        100 // Get more schedules since filters are optional
      );
      setSchedules(response.data?.content || []);
      if (response.data?.content?.length === 0) {
        addToast({ message: 'No schedules found with these filters', type: 'info' });
      }
    } catch (error) {
      addToast({ message: error.response?.data?.message || 'Failed to load schedules', type: 'error' });
      setSchedules([]);
    } finally {
      setLoadingSchedules(false);
    }
  }, [scheduleFilters.routeId, scheduleFilters.fromDate, scheduleFilters.toDate, scheduleFilters.maxPrice, addToast]);

  // Fetch routes and schedules on dialog open
  useEffect(() => {
    if (open) {
      fetchRoutes();
      fetchSchedules();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]); // Only react to open/close, not filter changes

  // Auto-refresh schedules when filters change (debounced)
  useEffect(() => {
    if (!open) return;
    const timeoutId = setTimeout(() => {
      fetchSchedules();
    }, 500);
    return () => clearTimeout(timeoutId);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [scheduleFilters.routeId, scheduleFilters.fromDate, scheduleFilters.toDate, scheduleFilters.maxPrice]); // Only react to filter changes, not open

  const fetchRoutes = async () => {
    setLoadingRoutes(true);
    try {
      const response = await routeService.getRoutes({ pageSize: 1000 });
      const routesData = response.data?.content || response.data || [];
      setRoutes(Array.isArray(routesData) ? routesData : []);
    } catch (error) {
      addToast({ message: 'Failed to load routes', type: 'error' });
      setRoutes([]);
    } finally {
      setLoadingRoutes(false);
    }
  };

  const fetchBusDetails = async (scheduleId) => {
    setLoadingBus(true);
    try {
      // Fetch schedule seats from new endpoint
      const response = await scheduleService.getScheduleSeats(scheduleId);
      const busData = response.data || {};
      const seats = busData.seats || [];
      
      setScheduleSeats(seats);
      
      if (seats.length === 0) {
        addToast({ message: 'No seats found for this schedule', type: 'warning' });
        setBusDetails(null);
        return;
      }
      
      // Auto-select seats that are PENDING by current user
      const myPendingSeats = seats.filter(
        seat => seat.status === 'PENDING' && seat.pendingUserId == currentUserId
      );
      
      // ALWAYS check localStorage for pending selections (backend may not have pendingUserId)
      let restoredFromLocalStorage = false;
      try {
        const storedSelection = localStorage.getItem('pendingSeatSelection');
        if (storedSelection) {
          const parsed = JSON.parse(storedSelection);
          
          // Only restore if same user and same schedule
          if (parsed.userId === currentUserId && parsed.scheduleId === parseInt(scheduleId)) {
            
            // Verify these seats still exist and are available/pending
            const validSeats = parsed.seats.filter(storedSeat => {
              const seat = seats.find(s => s.id === storedSeat.id);
              const isValid = seat && (seat.status === 'AVAILABLE' || seat.status === 'PENDING');
              return isValid;
            });
            
            if (validSeats.length > 0) {
              // Add localStorage seats to myPendingSeats (avoid duplicates)
              validSeats.forEach(storedSeat => {
                const seat = seats.find(s => s.id === storedSeat.id);
                if (seat && !myPendingSeats.find(s => s.id === seat.id)) {
                  myPendingSeats.push({
                    id: seat.id,
                    seatNumber: seat.seatNumber,
                    status: seat.status
                  });
                }
              });
              restoredFromLocalStorage = true;
            } else {
              localStorage.removeItem('pendingSeatSelection');
            }
          } else {
          }
        } else {
        }
      } catch (error) {
        console.error('❌ Error checking localStorage:', error);
      }
      
      
      if (myPendingSeats.length > 0) {
        
        const selectedSeatsData = myPendingSeats.map(seat => ({
          id: seat.id,
          seatNumber: seat.seatNumber
        }));
        
        setSelectedSeats(selectedSeatsData);
        
        // Store in localStorage for persistence across page refreshes
        const pendingSelection = {
          userId: currentUserId,
          scheduleId: scheduleId,
          seats: selectedSeatsData,
          timestamp: new Date().toISOString()
        };
        localStorage.setItem('pendingSeatSelection', JSON.stringify(pendingSelection));
        
      } else {
      }
      
      // Create busDetails object from response
      setBusDetails({
        id: busData.id,
        busNumber: busData.busNumber,
        plate: busData.plate,
        model: busData.model,
        busType: busData.busType,
        status: busData.status,
        totalSeats: busData.totalSeats || seats.length,
        route: busData.route,
        layout: busData.layout
      });
    } catch (error) {
      console.error('Error fetching schedule seats:', error);
      addToast({ message: 'Failed to load seat information', type: 'error' });
      setBusDetails(null);
      setScheduleSeats([]);
    } finally {
      setLoadingBus(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setScheduleFilters(prev => ({
      ...prev,
      [name]: value
    }));
    // Clear error for this field
    if (errors[name]) {
      setErrors(prev => {
        const newErrors = { ...prev };
        delete newErrors[name];
        return newErrors;
      });
    }
  };

  const handleScheduleSelect = async (schedule) => {
    setFormData(prev => ({ ...prev, scheduleId: schedule.id }));
    await fetchBusDetails(schedule.id);
    setStep(2);
  };

  const handleSeatToggle = (scheduleSeatId, seatNumber, scheduleSeat) => {
    
    // Check if seat is in local selection
    const exists = selectedSeats.find(s => s.id === scheduleSeatId);
    
    // User ID comparison
    if (scheduleSeat.status === 'PENDING') {
      
      if (scheduleSeat.pendingUserId === currentUserId) {
      } else if (scheduleSeat.pendingUserId == currentUserId) {
      } else {
      }
    }
    

    if (!wsConnected || !sendMessage) {
      console.error('❌ WebSocket not connected or sendMessage not available');
      addToast({ message: 'WebSocket not connected. Please refresh the page.', type: 'error' });
      return;
    }
    
    // Check if seat is pending by another user (but allow if already in our selection)
    // Use loose equality (==) to handle type mismatches between string and number
    if (!exists && scheduleSeat.status === 'PENDING' && scheduleSeat.pendingUserId != currentUserId) {
      console.warn('⚠️ Seat is pending by another user');
      console.warn('  - Pending User ID:', scheduleSeat.pendingUserId, '(type:', typeof scheduleSeat.pendingUserId + ')');
      console.warn('  - Current User ID:', currentUserId, '(type:', typeof currentUserId + ')');
      addToast({ message: 'This seat is being selected by another user', type: 'warning' });
      return;
    }
    
    if (exists) {
      // Deselect seat - send WebSocket message
      
      const message = {
        scheduleId: parseInt(formData.scheduleId),
        seatId: scheduleSeatId,
        userId: currentUserId
      };
      
      const success = sendMessage('/app/seat/deselect', message);
      
      setSelectedSeats(prev => {
        const updated = prev.filter(s => s.id !== scheduleSeatId);
        
        // Update localStorage
        if (updated.length > 0) {
          const pendingSelection = {
            userId: currentUserId,
            scheduleId: parseInt(formData.scheduleId),
            seats: updated,
            timestamp: new Date().toISOString()
          };
          localStorage.setItem('pendingSeatSelection', JSON.stringify(pendingSelection));
        } else {
          localStorage.removeItem('pendingSeatSelection');
        }
        
        return updated;
      });
    } else {
      // Select seat - send WebSocket message
      
      const message = {
        scheduleId: parseInt(formData.scheduleId),
        seatId: scheduleSeatId,
        userId: currentUserId
      };
      
      const success = sendMessage('/app/seat/select', message);
      
      setSelectedSeats(prev => {
        const updated = [...prev, { id: scheduleSeatId, seatNumber }];
        
        // Update localStorage
        const pendingSelection = {
          userId: currentUserId,
          scheduleId: parseInt(formData.scheduleId),
          seats: updated,
          timestamp: new Date().toISOString()
        };
        localStorage.setItem('pendingSeatSelection', JSON.stringify(pendingSelection));
        
        return updated;
      });
    }
    
  };

  const validateStep1 = () => {
    const newErrors = {};
    if (!formData.scheduleId) {
      newErrors.scheduleId = t('scheduleRequired') || 'Schedule is required';
    }
    return newErrors;
  };

  const validateStep2 = () => {
    const newErrors = {};
    if (selectedSeats.length === 0) {
      newErrors.seats = t('atLeastOneSeatRequired') || 'Please select at least one seat';
    }
    return newErrors;
  };

  const handleNext = () => {
    if (step === 1) {
      const newErrors = validateStep1();
      if (Object.keys(newErrors).length > 0) {
        setErrors(newErrors);
        return;
      }
      if (!formData.scheduleId) {
        addToast({ message: 'Please select a schedule', type: 'error' });
        return;
      }
    } else if (step === 2) {
      const newErrors = validateStep2();
      if (Object.keys(newErrors).length > 0) {
        setErrors(newErrors);
        addToast({ message: newErrors.seats, type: 'error' });
        return;
      }
      setStep(3);
    }
  };

  const handleBack = () => {
    if (step === 2) {
      setStep(1);
      setBusDetails(null);
      setScheduleSeats([]);
      setSelectedSeats([]);
    } else if (step === 3) {
      setStep(2);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setLoading(true);
    try {
      const payload = {
        scheduleId: parseInt(formData.scheduleId),
        seatIds: selectedSeats.map(seat => seat.id),
        phoneNumber: formData.phoneNumber.trim() || null,
      };
      
      await bookingService.createBooking(payload);
      
      // Clear localStorage after successful booking
      localStorage.removeItem('pendingSeatSelection');
      
      // Reset form
      setFormData({
        scheduleId: '',
        phoneNumber: '',
      });
      setScheduleFilters({
        routeId: '',
        fromDate: '',
        toDate: '',
        maxPrice: '',
      });
      setSelectedSeats([]);
      setScheduleSeats([]);
      setBusDetails(null);
      setStep(1);
      setErrors({});
      onSuccess();
      addToast({ message: t('bookingCreatedSuccess') || 'Booking created successfully', type: 'success' });
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Failed to create booking';
      addToast({ message: errorMessage, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setFormData({
        scheduleId: '',
        phoneNumber: '',
      });
      setScheduleFilters({
        routeId: '',
        fromDate: '',
        toDate: '',
        maxPrice: '',
      });
      setSelectedSeats([]);
      setScheduleSeats([]);
      setBusDetails(null);
      setStep(1);
      setErrors({});
      onClose();
    }
  };

  const selectedSchedule = schedules.find(s => s.id === formData.scheduleId);
  const totalAmount = selectedSeats.length * (selectedSchedule?.price || 0);

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-4xl min-h-[500px] sm:min-h-[600px] max-h-[90vh] overflow-y-auto bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-base sm:text-lg text-slate-900 dark:text-white">
            <Ticket className="w-4 h-4 sm:w-5 sm:h-5 text-blue-500" />
            {t('createBooking') || 'Create Booking'} - Step {step} of 3
            {/* WebSocket Status Indicator (only show on step 2) */}
            {step === 2 && (
              <span className="flex items-center gap-1 text-sm font-normal ml-auto">
                {wsConnected ? (
                  <>
                    <Wifi className="w-4 h-4 text-green-500" />
                    <span className="text-green-500">Live</span>
                  </>
                ) : (
                  <>
                    <WifiOff className="w-4 h-4 text-slate-400" />
                    <span className="text-slate-400">Offline</span>
                  </>
                )}
              </span>
            )}
          </DialogTitle>
          {step === 2 && wsConnected && (
            <p className="text-xs text-green-500 mt-1">
              • Real-time seat updates enabled
            </p>
          )}
        </DialogHeader>

        {/* Step 1: Select Schedule */}
        {step === 1 && (
          <div className="space-y-4">
            <div className="border-t border-slate-200 dark:border-slate-800 pt-4">
              <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-3 flex items-center gap-2">
                <Calendar className="w-5 h-5 text-blue-500" />
                {t('filterSchedules') || 'Filter Schedules (Optional)'}
              </h3>
              <div className="space-y-3 mb-4">
                {/* Route Selection */}
                <div className="space-y-2">
                  <Label className="text-sm font-semibold text-slate-700 dark:text-slate-300 flex items-center gap-2">
                    <MapPin className="w-4 h-4 text-blue-400" />
                    Route
                  </Label>
                  <div className="relative">
                    <select
                      name="routeId"
                      value={scheduleFilters.routeId}
                      onChange={handleFilterChange}
                      disabled={loadingRoutes}
                      className="w-full px-4 py-3 border border-slate-300 dark:border-slate-700/50 rounded-xl bg-white dark:bg-slate-800/50 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/50 transition-all duration-200 appearance-none cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <option value="" className="bg-white dark:bg-slate-800">
                        {loadingRoutes ? 'Loading routes...' : 'All Routes'}
                      </option>
                      {routes.map(route => (
                        <option key={route.id} value={route.id} className="bg-white dark:bg-slate-800">
                          {route.origin} → {route.destination} ({route.distanceKm} km)
                        </option>
                      ))}
                    </select>
                    <svg className="absolute right-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400 pointer-events-none" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                    </svg>
                  </div>
                </div>

                {/* Date and Price Filters */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div>
                    <Label className="text-sm text-slate-700 dark:text-slate-300">{t('fromDate') || 'From Date'}</Label>
                    <DateTimePicker
                      value={scheduleFilters.fromDate}
                      onChange={(value) => {
                        setScheduleFilters(prev => ({ ...prev, fromDate: value }));
                        if (errors.fromDate) {
                          setErrors(prev => {
                            const newErrors = { ...prev };
                            delete newErrors.fromDate;
                            return newErrors;
                          });
                        }
                      }}
                      placeholder="Select From Date"
                    />
                    {errors.fromDate && <p className="text-sm text-red-400 mt-1">{errors.fromDate}</p>}
                  </div>
                  <div>
                    <Label className="text-sm text-slate-700 dark:text-slate-300">{t('toDate') || 'To Date'}</Label>
                    <DateTimePicker
                      value={scheduleFilters.toDate}
                      onChange={(value) => {
                        setScheduleFilters(prev => ({ ...prev, toDate: value }));
                        if (errors.toDate) {
                          setErrors(prev => {
                            const newErrors = { ...prev };
                            delete newErrors.toDate;
                            return newErrors;
                          });
                        }
                      }}
                      placeholder="Select To Date"
                    />
                    {errors.toDate && <p className="text-sm text-red-400 mt-1">{errors.toDate}</p>}
                  </div>
                </div>
                <Input
                  label={t('maxPrice') || 'Max Price'}
                  name="maxPrice"
                  type="number"
                  step="0.01"
                  min="0"
                  value={scheduleFilters.maxPrice}
                  onChange={handleFilterChange}
                  placeholder="100.00"
                  className="bg-white dark:bg-slate-800 border-slate-300 dark:border-slate-700 text-slate-900 dark:text-white"
                />
              </div>
            </div>

            <div className="border-t border-slate-200 dark:border-slate-800 pt-4">
              <h3 className="text-base sm:text-lg font-semibold text-slate-900 dark:text-white mb-3">
                {t('availableSchedules') || 'Available Schedules'}
              </h3>
              <div className="space-y-2 h-48 sm:h-64 overflow-y-auto">
                {loadingSchedules ? (
                  <div className="text-center py-8 text-slate-600 dark:text-slate-400">
                    <Loader2 className="w-6 h-6 animate-spin mx-auto mb-2" />
                    Loading schedules...
                  </div>
                ) : schedules.length === 0 ? (
                  <div className="text-center py-8 text-slate-600 dark:text-slate-400">
                    No schedules found. Try adjusting filters.
                  </div>
                ) : (
                  schedules.map(schedule => (
                    <button
                      key={schedule.id}
                      onClick={() => handleScheduleSelect(schedule)}
                      className={`w-full p-4 rounded-lg border transition-all text-left ${
                        formData.scheduleId === schedule.id
                          ? 'bg-blue-500/20 border-blue-500'
                          : 'bg-slate-50 dark:bg-slate-800 border-slate-200 dark:border-slate-700 hover:border-slate-300 dark:hover:border-slate-600'
                      }`}
                    >
                      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-start gap-3 sm:gap-4">
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 mb-2">
                            <Bus className="w-4 h-4 text-blue-400 flex-shrink-0" />
                            <p className="font-semibold text-sm sm:text-base text-slate-900 dark:text-white">Bus #{schedule.busNumber}</p>
                          </div>
                          
                          {schedule.route && (
                            <div className="flex items-start gap-2 mb-2">
                              <MapPin className="w-3 h-3 text-green-400 flex-shrink-0 mt-0.5" />
                              <div className="flex-1 min-w-0">
                                <p className="text-xs sm:text-sm text-slate-700 dark:text-slate-300 break-words">
                                  {schedule.route.origin} → {schedule.route.destination}
                                </p>
                                <span className="text-xs text-slate-500">
                                  ({schedule.route.distanceKm} km)
                                </span>
                              </div>
                            </div>
                          )}
                          
                          <div className="space-y-1">
                            <div className="flex items-start gap-1 text-xs text-slate-600 dark:text-slate-400">
                              <Clock className="w-3 h-3 text-green-400 flex-shrink-0 mt-0.5" />
                              <span className="break-words">Depart: {new Date(schedule.departureDateTime).toLocaleString()}</span>
                            </div>
                            <div className="flex items-start gap-1 text-xs text-slate-600 dark:text-slate-400">
                              <Clock className="w-3 h-3 text-orange-400 flex-shrink-0 mt-0.5" />
                              <span className="break-words">Arrive: {new Date(schedule.arrivalDateTime).toLocaleString()}</span>
                            </div>
                          </div>
                        </div>
                        
                        <div className="text-left sm:text-right flex-shrink-0">
                          <p className="text-xl sm:text-2xl font-bold text-green-400">${schedule.price}</p>
                          <p className="text-xs text-slate-500">per seat</p>
                        </div>
                      </div>
                    </button>
                  ))
                )}
              </div>
            </div>

            <DialogFooter className="flex-col sm:flex-row gap-2">
              <Button
                type="button"
                variant="secondary"
                onClick={handleClose}
                className="bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white w-full sm:w-auto"
              >
                {t('cancel') || 'Cancel'}
              </Button>
              <Button
                type="button"
                onClick={handleNext}
                disabled={!formData.scheduleId}
                className="bg-blue-500 hover:bg-blue-600 text-white w-full sm:w-auto"
              >
                {t('next') || 'Next'}
              </Button>
            </DialogFooter>
          </div>
        )}

        {/* Step 2: Select Seats */}
        {step === 2 && (
          <div className="space-y-4">
            {loadingBus ? (
              <div className="text-center py-12 text-slate-600 dark:text-slate-400">
                <Loader2 className="w-8 h-8 animate-spin mx-auto mb-3" />
                Loading bus details...
              </div>
            ) : busDetails ? (
              <>
                <div className="bg-slate-50 dark:bg-slate-800 p-3 sm:p-4 rounded-lg border border-slate-200 dark:border-slate-700">
                  <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
                    <div className="flex-1 min-w-0">
                      <h3 className="text-base sm:text-lg font-bold text-slate-900 dark:text-white flex items-center gap-2">
                        <Bus className="w-4 h-4 sm:w-5 sm:h-5 text-blue-500 flex-shrink-0" />
                        <span className="truncate">Bus #{busDetails.busNumber}</span>
                      </h3>
                      <p className="text-xs text-slate-500 mt-1 truncate">
                        {busDetails.plate} • {busDetails.model} • {busDetails.busType}
                      </p>
                      <p className="text-xs sm:text-sm text-slate-600 dark:text-slate-400 mt-2">
                        {busDetails.totalSeats} total seats
                      </p>
                      {busDetails.route && (
                        <p className="text-xs sm:text-sm text-slate-700 dark:text-slate-300 mt-1 flex items-start gap-1">
                          <MapPin className="w-3 h-3 text-green-400 flex-shrink-0 mt-0.5" />
                          <span className="break-words">
                            {busDetails.route.origin} → {busDetails.route.destination}
                            <span className="text-xs text-slate-500 ml-1">({busDetails.route.distanceKm} km)</span>
                          </span>
                        </p>
                      )}
                    </div>
                    <div className="text-left sm:text-right flex-shrink-0">
                      <p className="text-xl sm:text-2xl font-bold text-green-400">${selectedSchedule?.price}</p>
                      <p className="text-xs text-slate-500">per seat</p>
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="text-base sm:text-lg font-semibold text-slate-900 dark:text-white mb-3 flex items-center gap-2">
                    <Armchair className="w-4 h-4 sm:w-5 sm:h-5 text-blue-500" />
                    <span className="text-sm sm:text-base">{t('selectSeats') || 'Select Seats'} ({selectedSeats.length} selected)</span>
                  </h3>
                  {errors.seats && (
                    <p className="text-xs sm:text-sm text-red-400 mb-2">{errors.seats}</p>
                  )}
                  
                  {/* Legend */}
                  <div className="bg-slate-100 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700 rounded-lg p-2 sm:p-3 mb-3 sm:mb-4">
                    <div className="grid grid-cols-2 sm:flex sm:flex-wrap gap-2 sm:gap-4 text-xs">
                      <div className="flex items-center gap-1.5 sm:gap-2">
                        <div className="w-6 h-6 sm:w-8 sm:h-8 bg-green-500 rounded flex items-center justify-center flex-shrink-0">
                          <Armchair className="w-3 h-3 sm:w-4 sm:h-4 text-white" />
                        </div>
                        <span className="text-slate-700 dark:text-slate-300 text-xs">Available</span>
                      </div>
                      <div className="flex items-center gap-1.5 sm:gap-2">
                        <div className="w-6 h-6 sm:w-8 sm:h-8 bg-blue-500 rounded flex items-center justify-center animate-pulse flex-shrink-0">
                          <Armchair className="w-3 h-3 sm:w-4 sm:h-4 text-white" />
                        </div>
                        <span className="text-slate-700 dark:text-slate-300 text-xs">Your Selection</span>
                      </div>
                      <div className="flex items-center gap-1.5 sm:gap-2">
                        <div className="w-6 h-6 sm:w-8 sm:h-8 bg-orange-500 rounded flex items-center justify-center flex-shrink-0">
                          <Armchair className="w-3 h-3 sm:w-4 sm:h-4 text-white" />
                        </div>
                        <span className="text-slate-700 dark:text-slate-300 text-xs">Being Selected</span>
                      </div>
                      <div className="flex items-center gap-1.5 sm:gap-2">
                        <div className="w-6 h-6 sm:w-8 sm:h-8 bg-slate-300 dark:bg-slate-700 rounded flex items-center justify-center flex-shrink-0">
                          <X className="w-3 h-3 sm:w-4 sm:h-4 text-slate-500" />
                        </div>
                        <span className="text-slate-700 dark:text-slate-300 text-xs">Booked</span>
                      </div>
                    </div>
                  </div>

                  {/* Seat Grid - Layout with Driver and Aisles */}
                  <div className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg p-3 sm:p-6">
                    {scheduleSeats.length > 0 && busDetails.layout ? (
                      (() => {
                        try {
                          // Handle both JSON string and object formats
                          let layoutData;
                          if (typeof busDetails.layout.layout === 'string') {
                            layoutData = JSON.parse(busDetails.layout.layout);
                          } else {
                            layoutData = busDetails.layout.layout;
                          }
                          
                          const { rows, columns, seats: layoutSeats, aisleColumns, driverColumn } = layoutData;
                          const aisleColsArray = aisleColumns ? String(aisleColumns).split(',').map(c => parseInt(c.trim()) - 1) : [];
                          
                          // Create a map of seat numbers to schedule seat data
                          const seatMap = {};
                          scheduleSeats.forEach(scheduleSeat => {
                            seatMap[scheduleSeat.seatNumber] = scheduleSeat;
                          });
                          
                          return (
                            <div className="overflow-x-auto pb-4 sm:pb-6">
                              {/* Driver Row */}
                              {driverColumn && (
                                <div className="mb-3 sm:mb-4 flex gap-1 sm:gap-2 justify-center">
                                  {Array.from({ length: columns || 0 }).map((_, colIndex) => {
                                    const isDriver = driverColumn === colIndex + 1;
                                    const isAisle = aisleColsArray.includes(colIndex);
                                    
                                    return (
                                      <div key={`driver-${colIndex}`}>
                                        {isDriver ? (
                                          <div className="w-12 h-12 sm:w-16 sm:h-16 bg-slate-800 dark:bg-slate-700 rounded-lg flex items-center justify-center text-white font-bold text-xl sm:text-2xl border-2 border-slate-600">
                                            🚗
                                          </div>
                                        ) : isAisle ? (
                                          <div className="w-12 h-12 sm:w-16 sm:h-16 bg-slate-100 dark:bg-slate-700 border-2 border-dashed border-slate-400 dark:border-slate-500 rounded-lg flex items-center justify-center">
                                            <span className="text-[8px] sm:text-[10px] text-slate-500 dark:text-slate-400 font-bold">AISLE</span>
                                          </div>
                                        ) : (
                                          <div className="w-12 h-12 sm:w-16 sm:h-16"></div>
                                        )}
                                      </div>
                                    );
                                  })}
                                </div>
                              )}

                              {/* Customer Seats Grid */}
                              <div className="flex flex-col gap-1 sm:gap-2 items-center">
                                {layoutSeats.map((row, rowIndex) => (
                                  <div key={rowIndex} className="flex gap-1 sm:gap-2">
                                    {row.map((seat, colIndex) => {
                                      const isAisle = aisleColsArray.includes(colIndex);
                                      const scheduleSeat = seatMap[seat.seatNumber];
                                      const isSelected = scheduleSeat && selectedSeats.find(s => s.id === scheduleSeat.id);
                                      const isAvailable = scheduleSeat && scheduleSeat.status === 'AVAILABLE';
                                      const isPending = scheduleSeat && scheduleSeat.status === 'PENDING';
                                      const isPendingByMe = isPending && scheduleSeat.pendingUserId === currentUserId;
                                      const isPendingByOther = isPending && scheduleSeat.pendingUserId !== currentUserId;
                                      const isBooked = scheduleSeat && scheduleSeat.status === 'BOOKED';
                                      const isEmpty = !seat.seatNumber || seat.seatNumber === '';
                                      
                                      // Allow clicking if: available, pending by me, OR selected locally
                                      const canClick = isAvailable || isPendingByMe || isSelected;
                                      
                                      // Determine visual state - prioritize local selection
                                      let seatColor = '';
                                      if (isBooked) {
                                        seatColor = 'bg-slate-300 dark:bg-slate-600 cursor-not-allowed';
                                      } else if (isPendingByOther) {
                                        seatColor = 'bg-orange-500 cursor-not-allowed';
                                      } else if (isSelected) {
                                        // If locally selected, always show blue (even if backend hasn't updated yet)
                                        seatColor = 'bg-blue-500 hover:bg-blue-600 text-white cursor-pointer border-2 border-blue-400 animate-pulse';
                                      } else if (isPendingByMe) {
                                        // If pending by me but not in local selection, show blue
                                        seatColor = 'bg-blue-500 hover:bg-blue-600 text-white cursor-pointer border-2 border-blue-400 animate-pulse';
                                      } else {
                                        // Available - show green
                                        seatColor = 'bg-green-500 hover:bg-green-600 text-white cursor-pointer';
                                      }
                                      
                                      if (isEmpty) {
                                        return (
                                          <div
                                            key={`${rowIndex}-${colIndex}`}
                                            className="w-12 h-12 sm:w-16 sm:h-16 border-2 border-dashed border-slate-300 dark:border-slate-700 rounded-lg bg-slate-50 dark:bg-slate-900/50"
                                          />
                                        );
                                      }
                                      
                                      if (isAisle) {
                                        return (
                                          <div
                                            key={`${rowIndex}-${colIndex}`}
                                            className="w-12 h-12 sm:w-16 sm:h-16 bg-slate-100 dark:bg-slate-700 border-2 border-dashed border-slate-400 dark:border-slate-500 rounded-lg flex items-center justify-center"
                                          >
                                            <span className="text-[8px] sm:text-[10px] text-slate-500 dark:text-slate-400 font-bold">AISLE</span>
                                          </div>
                                        );
                                      }
                                      
                                      if (!scheduleSeat) {
                                        return (
                                          <div
                                            key={`${rowIndex}-${colIndex}`}
                                            className="w-12 h-12 sm:w-16 sm:h-16 bg-slate-300 dark:bg-slate-600 rounded-lg flex flex-col items-center justify-center opacity-50"
                                          >
                                            <X className="w-4 h-4 sm:w-5 sm:h-5 text-slate-600 dark:text-slate-300" />
                                            <span className="text-[10px] sm:text-xs font-mono mt-0.5 sm:mt-1 text-slate-600 dark:text-slate-300">{seat.seatNumber}</span>
                                          </div>
                                        );
                                      }
                                      
                                      return (
                                        <div key={`${rowIndex}-${colIndex}`} className="relative group">
                                          <button
                                            type="button"
                                            onClick={(e) => {
                                              e.stopPropagation();
                                              if (canClick) {
                                                handleSeatToggle(scheduleSeat.id, scheduleSeat.seatNumber, scheduleSeat);
                                              }
                                            }}
                                            disabled={isBooked || isPendingByOther}
                                            className={`w-12 h-12 sm:w-16 sm:h-16 rounded-lg flex flex-col items-center justify-center transition-all ${seatColor}`}
                                          >
                                            {isBooked ? (
                                              <X className="w-4 h-4 sm:w-5 sm:h-5 text-slate-600 dark:text-slate-300" />
                                            ) : (
                                              <Armchair className="w-4 h-4 sm:w-6 sm:h-6" />
                                            )}
                                          </button>
                                          <div className="absolute -bottom-4 sm:-bottom-5 left-1/2 -translate-x-1/2 text-[8px] sm:text-[10px] font-mono text-slate-600 dark:text-slate-400 opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">
                                            {scheduleSeat.seatNumber}
                                            {isPendingByOther && ' (Locked)'}
                                          </div>
                                        </div>
                                      );
                                    })}
                                  </div>
                                ))}
                              </div>
                            </div>
                          );
                        } catch (error) {
                          console.error('Error parsing layout:', error);
                          return (
                            <div className="text-center py-8 text-red-400">
                              <p className="text-sm">Failed to load seat layout</p>
                            </div>
                          );
                        }
                      })()
                    ) : scheduleSeats.length > 0 ? (
                      <div className="grid grid-cols-4 sm:grid-cols-6 md:grid-cols-8 lg:grid-cols-10 gap-1.5 sm:gap-2">
                        {scheduleSeats.map((scheduleSeat) => {
                          const isSelected = selectedSeats.find(s => s.id === scheduleSeat.id);
                          const isAvailable = scheduleSeat.status === 'AVAILABLE';
                          const isPending = scheduleSeat.status === 'PENDING';
                          const isPendingByMe = isPending && scheduleSeat.pendingUserId === currentUserId;
                          const isPendingByOther = isPending && scheduleSeat.pendingUserId !== currentUserId;
                          const isBooked = scheduleSeat.status === 'BOOKED';
                          
                          // Allow clicking if: available, pending by me, OR selected locally
                          const canClick = isAvailable || isPendingByMe || isSelected;
                          
                          // Determine visual state - prioritize local selection
                          let seatColor = '';
                          if (isBooked) {
                            seatColor = 'bg-slate-300 dark:bg-slate-600 cursor-not-allowed';
                          } else if (isPendingByOther) {
                            seatColor = 'bg-orange-500 text-white cursor-not-allowed';
                          } else if (isSelected) {
                            // If locally selected, always show blue (even if backend hasn't updated yet)
                            seatColor = 'bg-blue-500 hover:bg-blue-600 text-white cursor-pointer animate-pulse';
                          } else if (isPendingByMe) {
                            // If pending by me but not in local selection, show blue
                            seatColor = 'bg-blue-500 hover:bg-blue-600 text-white cursor-pointer animate-pulse';
                          } else {
                            // Available - show green
                            seatColor = 'bg-green-500 hover:bg-green-600 text-white cursor-pointer';
                          }
                          
                          return (
                            <button
                              key={scheduleSeat.id}
                              type="button"
                              onClick={(e) => {
                                e.stopPropagation();
                                if (canClick) {
                                  handleSeatToggle(scheduleSeat.id, scheduleSeat.seatNumber, scheduleSeat);
                                }
                              }}
                              disabled={isBooked || isPendingByOther}
                              className={`h-12 sm:h-16 rounded-lg flex flex-col items-center justify-center transition-all relative group ${seatColor}`}
                            >
                              {isBooked ? (
                                <X className="w-4 h-4 sm:w-5 sm:h-5 text-slate-600 dark:text-slate-300" />
                              ) : (
                                <Armchair className="w-4 h-4 sm:w-5 sm:h-5" />
                              )}
                              <span className="text-[10px] sm:text-xs font-mono mt-0.5 sm:mt-1">{scheduleSeat.seatNumber}</span>
                              {isPendingByOther && (
                                <span className="absolute top-0 right-0 text-[8px] bg-red-500 text-white px-1 rounded">🔒</span>
                              )}
                            </button>
                          );
                        })}
                      </div>
                    ) : (
                      <div className="text-center py-12 text-slate-500 dark:text-slate-400">
                        <Armchair className="w-12 h-12 mx-auto mb-3 opacity-30" />
                        <p className="text-sm">No seats available for this schedule</p>
                      </div>
                    )}
                  </div>
                </div>

                <div className="bg-blue-500/10 border border-blue-500/20 rounded-lg p-3 sm:p-4">
                  <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-2">
                    <span className="text-xs sm:text-sm text-slate-700 dark:text-slate-300">Selected Seats:</span>
                    <span className="text-xs sm:text-sm font-semibold text-slate-900 dark:text-white break-words">
                      {selectedSeats.map(s => s.seatNumber).join(', ') || 'None'}
                    </span>
                  </div>
                  <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-1 sm:gap-2 mt-2 pt-2 border-t border-blue-500/20">
                    <span className="text-xs sm:text-sm text-slate-700 dark:text-slate-300">Total Amount:</span>
                    <span className="text-lg sm:text-xl font-bold text-green-400">${totalAmount.toFixed(2)}</span>
                  </div>
                </div>
              </>
            ) : (
              <div className="text-center py-12 text-red-400">
                Failed to load bus details
              </div>
            )}

            <DialogFooter className="flex-col sm:flex-row gap-2">
              <Button
                type="button"
                variant="secondary"
                onClick={handleBack}
                className="bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white w-full sm:w-auto"
              >
                {t('back') || 'Back'}
              </Button>
              <Button
                type="button"
                onClick={handleNext}
                disabled={selectedSeats.length === 0}
                className="bg-blue-500 hover:bg-blue-600 text-white w-full sm:w-auto"
              >
                {t('next') || 'Next'}
              </Button>
            </DialogFooter>
          </div>
        )}

        {/* Step 3: Confirm & Submit */}
        {step === 3 && (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="bg-slate-50 dark:bg-slate-800 p-4 rounded-lg border border-slate-200 dark:border-slate-700 space-y-3">
              <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-3">
                {t('bookingSummary') || 'Booking Summary'}
              </h3>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-slate-600 dark:text-slate-400">Schedule:</span>
                  <span className="text-slate-900 dark:text-white font-semibold">#{formData.scheduleId}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-600 dark:text-slate-400">Route:</span>
                  <span className="text-slate-900 dark:text-white font-semibold">
                    {busDetails?.route?.origin} → {busDetails?.route?.destination}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-600 dark:text-slate-400">Seats:</span>
                  <span className="text-slate-900 dark:text-white font-semibold">
                    {selectedSeats.map(s => s.seatNumber).join(', ')}
                  </span>
                </div>
                <div className="flex justify-between pt-2 border-t border-slate-200 dark:border-slate-700">
                  <span className="text-slate-600 dark:text-slate-400">Total Amount:</span>
                  <span className="text-xl font-bold text-green-400">${totalAmount.toFixed(2)}</span>
                </div>
              </div>
            </div>

            <Input
              label={t('phoneNumber') || 'Phone Number (Optional)'}
              name="phoneNumber"
              type="tel"
              value={formData.phoneNumber}
              onChange={handleChange}
              placeholder="012345678"
              className="bg-white dark:bg-slate-800 border-slate-300 dark:border-slate-700 text-slate-900 dark:text-white"
            />


<DialogFooter className="gap-2">
              <Button
                type="button"
                variant="secondary"
                onClick={handleBack}
                disabled={loading}
                className="bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white"
              >
                {t('back') || 'Back'}
              </Button>
              <Button
                type="submit"
                variant="primary"
                disabled={loading}
                className="flex items-center gap-2 bg-blue-500 hover:bg-blue-600 text-white"
              >
                {loading && <Loader2 className="w-4 h-4 animate-spin" />}
                {loading ? (t('creating') || 'Creating...') : (t('confirmBooking') || 'Confirm Booking')}
              </Button>
            </DialogFooter>
          </form>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default CreateBookingDialog;
