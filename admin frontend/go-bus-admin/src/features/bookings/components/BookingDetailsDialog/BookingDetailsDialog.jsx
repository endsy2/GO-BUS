import React, { useState, useEffect } from 'react';
import { formatStatus } from 'shared/utils/formatters';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'shared/components/ui/dialog';
import { Skeleton } from 'shared/components/ui/skeleton';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import { 
  User, 
  MapPin, 
  Calendar, 
  Clock, 
  CreditCard, 
  DollarSign,
  Bus,
  Armchair,
  Phone,
  Mail
} from 'lucide-react';
import bookingService from '../../services/bookingService';
import { Badge } from 'shared/components/common/Badge';

const BookingDetailsDialog = ({ open, onClose, bookingId }) => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const [booking, setBooking] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (open && bookingId) {
      fetchBookingDetails();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, bookingId]);

  const fetchBookingDetails = async () => {
    try {
      setLoading(true);
      const response = await bookingService.getBookingById(bookingId);
      setBooking(response.data);
    } catch (error) {
      console.error('Failed to fetch booking details:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    const statusMap = {
      CONFIRMED: 'success',
      PENDING: 'warning',
      CANCELLED: 'danger',
      COMPLETED: 'info',
    };
    return statusMap[status] || 'default';
  };

  const getPaymentStatusColor = (status) => {
    const statusMap = {
      PAID: 'success',
      PENDING: 'warning',
      FAILED: 'danger',
      REFUNDED: 'info',
    };
    return statusMap[status] || 'default';
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-3xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-xl sm:text-2xl">
            <div className="bg-gradient-to-br from-indigo-500 to-purple-600 p-1.5 sm:p-2 rounded-lg">
              <Bus className="w-5 h-5 sm:w-6 sm:h-6 text-white" />
            </div>
            {t('bookingDetails') || 'Booking Details'}
          </DialogTitle>
        </DialogHeader>

        {loading ? (
          <div className="space-y-6">
            {[1, 2, 3, 4].map(i => (
              <div key={i} className="space-y-3">
                <Skeleton className="h-6 w-32" />
                <Skeleton className="h-20 w-full" />
              </div>
            ))}
          </div>
        ) : booking ? (
          <div className="space-y-6">
            {/* Booking Status */}
            <div className="bg-gradient-to-br from-indigo-50 to-purple-50 dark:from-indigo-900/20 dark:to-purple-900/20 rounded-xl p-4 sm:p-5 border border-indigo-200 dark:border-indigo-800">
              <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3">
                <div>
                  <h3 className="text-base sm:text-lg font-bold text-slate-900 dark:text-slate-100 mb-1">
                    {t('bookingId') || 'Booking ID'}: #{booking.id}
                  </h3>
                  <p className="text-xs sm:text-sm text-slate-600 dark:text-slate-400">
                    {t('createdAt') || 'Created'}: {booking.createdAt ? new Date(booking.createdAt).toLocaleString() : 'N/A'}
                  </p>
                </div>
                <div className="flex gap-2 flex-wrap">
                  <Badge variant={getStatusColor(booking.bookingStatus)} className="text-xs">
                    {formatStatus(booking.bookingStatus)}
                  </Badge>
                  <Badge variant={getPaymentStatusColor(booking.paymentStatus)} className="text-xs">
                    {formatStatus(booking.paymentStatus)}
                  </Badge>
                </div>
              </div>
            </div>

            {/* Customer Information */}
            <div className="bg-white dark:bg-slate-800 rounded-xl p-4 sm:p-5 border border-slate-200 dark:border-slate-700">
              <h3 className="text-base sm:text-lg font-bold text-slate-900 dark:text-slate-100 mb-3 sm:mb-4 flex items-center gap-2">
                <User className="w-4 h-4 sm:w-5 sm:h-5 text-indigo-600" />
                {t('customerInformation') || 'Customer Information'}
              </h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                <div>
                  <p className="text-sm text-slate-600 dark:text-slate-400 mb-1">
                    {t('name') || 'Name'}
                  </p>
                  <p className="font-semibold text-slate-900 dark:text-slate-100">
                    {booking.user?.fullName || 'N/A'}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-slate-600 dark:text-slate-400 mb-1 flex items-center gap-1">
                    <Mail className="w-3 h-3" />
                    {t('email') || 'Email'}
                  </p>
                  <p className="font-semibold text-slate-900 dark:text-slate-100">
                    {booking.user?.email || 'N/A'}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-slate-600 dark:text-slate-400 mb-1 flex items-center gap-1">
                    <Phone className="w-3 h-3" />
                    {t('phone') || 'Phone'}
                  </p>
                  <p className="font-semibold text-slate-900 dark:text-slate-100">
                    {booking.user?.phone || booking.phoneNumber || 'N/A'}
                  </p>
                </div>
              </div>
            </div>

            {/* Schedule Information */}
            <div className="bg-white dark:bg-slate-800 rounded-xl p-4 sm:p-5 border border-slate-200 dark:border-slate-700">
              <h3 className="text-base sm:text-lg font-bold text-slate-900 dark:text-slate-100 mb-3 sm:mb-4 flex items-center gap-2">
                <MapPin className="w-4 h-4 sm:w-5 sm:h-5 text-indigo-600" />
                {t('scheduleInformation') || 'Schedule Information'}
              </h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                <div>
                  <p className="text-sm text-slate-600 dark:text-slate-400 mb-1">
                    {t('scheduleId') || 'Schedule ID'}
                  </p>
                  <p className="font-semibold text-slate-900 dark:text-slate-100">
                    #{booking.schedule?.id || 'N/A'}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-slate-600 dark:text-slate-400 mb-1 flex items-center gap-1">
                    <Bus className="w-3 h-3" />
                    {t('busNumber') || 'Bus Number'}
                  </p>
                  <p className="font-semibold text-slate-900 dark:text-slate-100">
                    {booking.schedule?.busNumber || 'N/A'}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-slate-600 dark:text-slate-400 mb-1">
                    {t('busType') || 'Bus Type'}
                  </p>
                  <p className="font-semibold text-slate-900 dark:text-slate-100">
                    {booking.schedule?.busType || 'N/A'}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-slate-600 dark:text-slate-400 mb-1">
                    {t('pricePerSeat') || 'Price Per Seat'}
                  </p>
                  <p className="font-semibold text-slate-900 dark:text-slate-100">
                    ${booking.schedule?.price?.toFixed(2) || '0.00'}
                  </p>
                </div>
                {booking.schedule?.departureDate && (
                  <div>
                    <p className="text-sm text-slate-600 dark:text-slate-400 mb-1 flex items-center gap-1">
                      <Calendar className="w-3 h-3" />
                      {t('departureDate') || 'Departure Date'}
                    </p>
                    <p className="font-semibold text-slate-900 dark:text-slate-100">
                      {new Date(booking.schedule.departureDate).toLocaleDateString()}
                    </p>
                  </div>
                )}
                {booking.schedule?.departureTime && (
                  <div>
                    <p className="text-sm text-slate-600 dark:text-slate-400 mb-1 flex items-center gap-1">
                      <Clock className="w-3 h-3" />
                      {t('departureTime') || 'Departure Time'}
                    </p>
                    <p className="font-semibold text-slate-900 dark:text-slate-100">
                      {booking.schedule.departureTime}
                    </p>
                  </div>
                )}
              </div>
            </div>

            {/* Seat Information */}
            {booking.seats && booking.seats.length > 0 && (
              <div className="bg-white dark:bg-slate-800 rounded-xl p-4 sm:p-5 border border-slate-200 dark:border-slate-700">
                <h3 className="text-base sm:text-lg font-bold text-slate-900 dark:text-slate-100 mb-3 sm:mb-4 flex items-center gap-2">
                  <Armchair className="w-4 h-4 sm:w-5 sm:h-5 text-indigo-600" />
                  {t('seatInformation') || 'Seat Information'}
                </h3>
                <div className="flex flex-wrap gap-1.5 sm:gap-2">
                  {booking.seats.map((seat, index) => (
                    <div 
                      key={index}
                      className="bg-indigo-100 dark:bg-indigo-900/30 text-indigo-700 dark:text-indigo-300 px-4 py-2 rounded-lg font-semibold"
                    >
                      {t('seat') || 'Seat'} #{seat.seatId}
                    </div>
                  ))}
                </div>
                <p className="text-sm text-slate-600 dark:text-slate-400 mt-3">
                  {t('totalSeats') || 'Total Seats'}: {booking.seats.length}
                </p>
              </div>
            )}

            {/* Payment Information */}
            <div className="bg-white dark:bg-slate-800 rounded-xl p-4 sm:p-5 border border-slate-200 dark:border-slate-700">
              <h3 className="text-base sm:text-lg font-bold text-slate-900 dark:text-slate-100 mb-3 sm:mb-4 flex items-center gap-2">
                <CreditCard className="w-4 h-4 sm:w-5 sm:h-5 text-indigo-600" />
                {t('paymentInformation') || 'Payment Information'}
              </h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                <div>
                  <p className="text-sm text-slate-600 dark:text-slate-400 mb-1">
                    {t('paymentMethod') || 'Payment Method'}
                  </p>
                  <p className="font-semibold text-slate-900 dark:text-slate-100">
                    {booking.paymentMethod || booking.payments?.[0]?.method || 'N/A'}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-slate-600 dark:text-slate-400 mb-1">
                    {t('paymentStatus') || 'Payment Status'}
                  </p>
                  <Badge variant={getPaymentStatusColor(booking.paymentStatus)}>
                    {booking.paymentStatus}
                  </Badge>
                </div>
                <div>
                  <p className="text-sm text-slate-600 dark:text-slate-400 mb-1 flex items-center gap-1">
                    <DollarSign className="w-3 h-3" />
                    {t('totalAmount') || 'Total Amount'}
                  </p>
                  <p className="font-semibold text-2xl text-indigo-600 dark:text-indigo-400">
                    ${booking.totalAmount?.toFixed(2) || '0.00'}
                  </p>
                </div>
                {booking.payments?.[0]?.transactionId && (
                  <div>
                    <p className="text-sm text-slate-600 dark:text-slate-400 mb-1">
                      {t('transactionId') || 'Transaction ID'}
                    </p>
                    <p className="font-semibold text-slate-900 dark:text-slate-100">
                      {booking.payments[0].transactionId}
                    </p>
                  </div>
                )}
                {booking.payments?.[0]?.paidAt && (
                  <div className="col-span-2">
                    <p className="text-sm text-slate-600 dark:text-slate-400 mb-1">
                      {t('paidAt') || 'Paid At'}
                    </p>
                    <p className="font-semibold text-slate-900 dark:text-slate-100">
                      {new Date(booking.payments[0].paidAt).toLocaleString()}
                    </p>
                  </div>
                )}
                {booking.promo && (
                  <div className="col-span-2">
                    <p className="text-sm text-slate-600 dark:text-slate-400 mb-1">
                      {t('promoCode') || 'Promo Code'}
                    </p>
                    <p className="font-semibold text-slate-900 dark:text-slate-100">
                      {booking.promo.code || booking.promo}
                    </p>
                  </div>
                )}
              </div>
            </div>
          </div>
        ) : (
          <div className="text-center py-12">
            <p className="text-slate-600 dark:text-slate-400">
              {t('bookingNotFound') || 'Booking not found'}
            </p>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default BookingDetailsDialog;
