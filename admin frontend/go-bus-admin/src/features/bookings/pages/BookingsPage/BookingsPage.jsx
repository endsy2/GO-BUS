import React, { useState, useCallback, useMemo, useEffect, useRef } from 'react';
import { useBookings } from '../../hooks/useBookings';
import { useMultiScheduleWebSocket } from '../../hooks/useMultiScheduleWebSocket';
import { useBookingEventsWebSocket } from '../../hooks/useBookingEventsWebSocket';
import { Badge } from 'shared/components/common/Badge';
import { Button } from 'shared/components/common/Button';
import { Skeleton } from 'shared/components/ui/skeleton';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import { useToast } from 'shared/components/ui/toast';
import {
  Eye,
  Banknote,
  Plus,
  Ticket,
  Wifi,
  WifiOff,
  Phone,
  Tag,
  CalendarDays,
} from 'lucide-react';
import BookingDetailsDialog from '../../components/BookingDetailsDialog/BookingDetailsDialog';
import CreateBookingDialog from '../../components/CreateBookingDialog/CreateBookingDialog';
import BookingFilters from '../../components/BookingFilters/BookingFilters';
import ConfirmDialog from 'shared/components/feedback/ConfirmDialog';
import { Pagination } from 'shared/components/feedback/Pagination';
import bookingService from '../../services/bookingService';
import { formatStatus } from 'shared/utils/formatters';

// ── Badge colour maps ─────────────────────────────────────────────────────────

const BOOKING_STATUS_COLOR = {
  CONFIRMED: 'success',
  PENDING: 'warning',
  CANCELLED: 'danger',
  FAILED: 'danger',
  REFUND_REQUESTED: 'warning',
  REFUNDED: 'info',
};

const PAYMENT_STATUS_COLOR = {
  SUCCESS: 'success',
  PENDING: 'warning',
  FAILED: 'danger',
  EXPIRED: 'danger',
  TIMEOUT: 'danger',
  CANCELLED: 'danger',
  REFUNDED: 'info',
};

const REFUND_STATUS_COLOR = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger',
  COMPLETED: 'info',
};

// Maps backend status enums → translation keys (see shared/locales/translations.js)
const STATUS_I18N_KEY = {
  CONFIRMED: 'confirmed',
  PENDING: 'pending',
  CANCELLED: 'cancelled',
  FAILED: 'failed',
  REFUND_REQUESTED: 'refundRequested',
  REFUNDED: 'refunded',
  SUCCESS: 'success',
  EXPIRED: 'expired',
  TIMEOUT: 'timeout',
  APPROVED: 'approved',
  REJECTED: 'rejected',
  COMPLETED: 'completed',
};

const PAYMENT_METHOD_LABEL = { WALLET: 'Wallet', BAKONG: 'Bakong QR', CASH: 'Cash', ADMIN: 'Force-paid' };
const PAYMENT_METHOD_COLOR = { WALLET: 'info', BAKONG: 'success', CASH: 'default', ADMIN: 'warning' };

const fmtDate = (dt) =>
  dt
    ? new Date(dt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
    : null;

// ── Component ─────────────────────────────────────────────────────────────────

const BookingsPage = () => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  // Localizes a backend status enum; falls back to title-cased English if unmapped
  const tStatus = (value) => {
    if (!value) return '';
    const key = STATUS_I18N_KEY[value];
    return key ? t(key) : formatStatus(value);
  };
  const { addToast } = useToast();
  const {
    bookings,
    loading,
    pagination,
    updateFilters,
    resetFilters,
    goToPage,
    changePageSize,
    refetch,
    applyBookingEvent,
  } = useBookings();

  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [selectedBookingId, setSelectedBookingId] = useState(null);
  const [markPaidDialogOpen, setMarkPaidDialogOpen] = useState(false);
  const [selectedBooking, setSelectedBooking] = useState(null);

  // ── WebSocket ─────────────────────────────────────────────────────────────

  const scheduleIds = useMemo(
    () => [...new Set(bookings.map((b) => b.scheduleId).filter(Boolean))],
    [bookings]
  );

  const handleSeatUpdate = useCallback(
    (data) => {
      if (data.type === 'SEAT_BOOKED') {
        addToast({ message: `Seat ${data.seatNumber} booked on schedule #${data.scheduleId}`, type: 'info' });
      } else if (data.type === 'SEAT_RELEASED') {
        addToast({ message: `Seat ${data.seatNumber} available on schedule #${data.scheduleId}`, type: 'info' });
      }
    },
    [addToast]
  );

  const { isConnected: wsConnected } = useMultiScheduleWebSocket(
    scheduleIds,
    handleSeatUpdate,
    scheduleIds.length > 0
  );

  const handleBookingEvent = useCallback(
    (event) => {
      const id = event.booking?.id ?? event.bookingId;
      switch (event.type ?? event.action) {
        case 'CREATED':
          addToast({ message: `New booking #${id} received`, type: 'success' });
          break;
        case 'PAYMENT_UPDATED':
          addToast({ message: `Booking #${id} payment updated`, type: 'info' });
          break;
        case 'CANCELLED':
          addToast({ message: `Booking #${id} was cancelled`, type: 'warning' });
          break;
        default:
          break;
      }
      applyBookingEvent(event);
    },
    [addToast, applyBookingEvent]
  );

  const { isConnected: bookingWsConnected } = useBookingEventsWebSocket(handleBookingEvent);
  const liveConnected = wsConnected || bookingWsConnected;

  // ── Catch-up on (re)connect ─────────────────────────────────────────────────
  // STOMP's simple broker does NOT replay messages missed while the socket was
  // down. So whenever the booking-events connection (re)establishes, silently
  // re-pull the current page to absorb any events dropped during the outage.
  // The initial connect is skipped — useBookings already does the first fetch.
  const prevBookingWsConnected = useRef(bookingWsConnected);
  useEffect(() => {
    if (bookingWsConnected && !prevBookingWsConnected.current) {
      refetch(true); // silent: no skeleton flash
    }
    prevBookingWsConnected.current = bookingWsConnected;
  }, [bookingWsConnected, refetch]);

  // ── Filter handlers ───────────────────────────────────────────────────────

  const handleFilterChange = (filters) => updateFilters(filters);
  const handleResetFilters  = () => resetFilters();

  // ── Action handlers ───────────────────────────────────────────────────────

  const handleViewDetails = (bookingId) => {
    setSelectedBookingId(bookingId);
    setDetailsDialogOpen(true);
  };

  const handleMarkPaidClick = (booking) => {
    setSelectedBooking(booking);
    setMarkPaidDialogOpen(true);
  };

  const handleMarkPaidConfirm = async () => {
    try {
      await bookingService.forceMarkPaid(selectedBooking.id);
      setMarkPaidDialogOpen(false);
      setSelectedBooking(null);
      refetch();
      addToast({ message: t('bookingMarkedPaidSuccess') || 'Booking marked as paid successfully', type: 'success' });
    } catch (error) {
      addToast({ message: error.response?.data?.message || 'Failed to mark booking as paid', type: 'error' });
    }
  };

  // ── Skeleton loaders ──────────────────────────────────────────────────────

  const DesktopSkeleton = () => (
    <div className="hidden md:block bg-card rounded-lg border border-border overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-muted/50 border-b border-border">
            <tr>
              {['#', 'Customer', 'Trip', 'Booked On', 'Status', 'Refund', 'Payment', 'Amount', 'Actions'].map((col) => (
                <th key={col} className="px-4 py-3">
                  <Skeleton className="h-4 w-16" />
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {[1, 2, 3, 4, 5].map((i) => (
              <tr key={i}>
                {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((j) => (
                  <td key={j} className="px-4 py-4">
                    <Skeleton className="h-5 w-full" />
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );

  const MobileSkeleton = () => (
    <div className="md:hidden space-y-3">
      {[1, 2, 3].map((i) => (
        <div key={i} className="bg-card rounded-lg border border-border p-4">
          <Skeleton className="h-6 w-32 mb-3" />
          <Skeleton className="h-4 w-full mb-2" />
          <Skeleton className="h-4 w-3/4 mb-2" />
          <Skeleton className="h-4 w-1/2" />
        </div>
      ))}
    </div>
  );

  // ── Desktop table ─────────────────────────────────────────────────────────

  const DesktopTable = () => (
    <div className="hidden md:block bg-card rounded-lg border border-border overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-muted/50 border-b border-border">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider w-16">
                #
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                {t('customer') || 'Customer'}
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                {t('route') || 'Destination'}
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                Travel Date
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                {t('createdDate') || 'Booked On'}
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                {t('status') || 'Status'}
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                {t('refund') || 'Refund'}
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                {t('payment') || 'Payment'}
              </th>
              <th className="px-4 py-3 text-right text-xs font-medium text-muted-foreground uppercase tracking-wider">
                {t('totalAmount') || 'Amount'}
              </th>
              <th className="px-4 py-3 text-center text-xs font-medium text-muted-foreground uppercase tracking-wider w-20">
                {t('actions') || 'Actions'}
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {bookings.map((booking) => (
              <tr key={booking.id} className="hover:bg-muted/50 transition-colors">

                {/* ID + promo indicator */}
                <td className="px-4 py-3">
                  <span className="font-mono text-sm font-medium text-foreground">#{booking.id}</span>
                  {booking.promoId && (
                    <span className="flex items-center gap-0.5 mt-0.5 text-xs text-green-600 dark:text-green-400">
                      <Tag className="w-3 h-3" />
                      <span>Promo</span>
                    </span>
                  )}
                </td>

                {/* Customer — name + phone */}
                <td className="px-4 py-3 max-w-[160px]">
                  <p className="text-sm font-medium text-foreground truncate">
                    {booking.fullName || 'N/A'}
                  </p>
                  {booking.phoneNumber && (
                    <p className="text-xs text-muted-foreground flex items-center gap-1 mt-0.5">
                      <Phone className="w-3 h-3 flex-shrink-0" />
                      <span className="truncate">{booking.phoneNumber}</span>
                    </p>
                  )}
                </td>

                {/* Destination */}
                <td className="px-4 py-3 max-w-[150px]">
                  <span className="text-sm text-foreground truncate block">
                    {booking.destination || 'N/A'}
                  </span>
                </td>

                {/* Travel Date (departure) */}
                <td className="px-4 py-3 whitespace-nowrap">
                  {booking.departureAt ? (
                    <span className="text-sm text-foreground flex items-center gap-1">
                      <CalendarDays className="w-3.5 h-3.5 text-muted-foreground flex-shrink-0" />
                      {fmtDate(booking.departureAt)}
                    </span>
                  ) : (
                    <span className="text-sm text-muted-foreground/50">—</span>
                  )}
                </td>

                {/* Booked On */}
                <td className="px-4 py-3 whitespace-nowrap">
                  <span className="text-sm text-foreground">
                    {fmtDate(booking.createdAt) || 'N/A'}
                  </span>
                </td>

                {/* Booking status */}
                <td className="px-4 py-3">
                  <Badge variant={BOOKING_STATUS_COLOR[booking.bookingStatus] || 'default'}>
                    {tStatus(booking.bookingStatus)}
                  </Badge>
                </td>

                {/* Refund status */}
                <td className="px-4 py-3">
                  {booking.refundStatus ? (
                    <Badge variant={REFUND_STATUS_COLOR[booking.refundStatus] || 'default'}>
                      {tStatus(booking.refundStatus)}
                    </Badge>
                  ) : (
                    <span className="text-sm text-muted-foreground/50">—</span>
                  )}
                </td>

                {/* Payment status + method */}
                <td className="px-4 py-3">
                  <div className="flex flex-col gap-1 items-start">
                    <Badge variant={PAYMENT_STATUS_COLOR[booking.paymentStatus] || 'default'}>
                      {tStatus(booking.paymentStatus)}
                    </Badge>
                    {booking.paymentMethod && (
                      <Badge variant={PAYMENT_METHOD_COLOR[booking.paymentMethod] || 'default'}>
                        {PAYMENT_METHOD_LABEL[booking.paymentMethod] || booking.paymentMethod}
                      </Badge>
                    )}
                  </div>
                </td>

                {/* Amount */}
                <td className="px-4 py-3 text-right whitespace-nowrap">
                  <span className="text-sm font-semibold text-foreground">
                    ${booking.totalAmount?.toFixed(2) ?? '—'}
                  </span>
                </td>

                {/* Actions */}
                <td className="px-4 py-3">
                  <div className="flex items-center justify-center gap-1">
                    {booking.paymentStatus === 'PENDING' &&
                      booking.bookingStatus !== 'CANCELLED' &&
                      booking.bookingStatus !== 'REFUNDED' && (
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-8 w-8 text-muted-foreground hover:text-foreground"
                          onClick={() => handleMarkPaidClick(booking)}
                          title="Force Mark as Paid"
                        >
                          <Banknote className="w-4 h-4" />
                        </Button>
                      )}
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-8 w-8 text-muted-foreground hover:text-foreground"
                      onClick={() => handleViewDetails(booking.id)}
                      title="View Details"
                    >
                      <Eye className="w-4 h-4" />
                    </Button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

    </div>
  );

  // ── Mobile cards ──────────────────────────────────────────────────────────

  const MobileCards = () => (
    <div className="md:hidden space-y-3">
      {bookings.map((booking) => (
        <div key={booking.id} className="bg-card rounded-lg border border-border overflow-hidden">

          {/* Header */}
          <div className="px-4 py-3 border-b border-border">
            <div className="flex items-start justify-between gap-2 mb-2">
              <div className="flex items-center gap-1.5">
                <span className="font-mono font-semibold text-foreground text-sm">#{booking.id}</span>
                {booking.promoId && (
                  <span className="flex items-center gap-0.5 text-xs text-green-600 dark:text-green-400">
                    <Tag className="w-3 h-3" /><span>Promo</span>
                  </span>
                )}
              </div>
              <span className="font-semibold text-foreground text-sm">
                ${booking.totalAmount?.toFixed(2) ?? '—'}
              </span>
            </div>
            <div className="flex flex-wrap gap-1.5">
              <Badge variant={BOOKING_STATUS_COLOR[booking.bookingStatus] || 'default'}>
                {tStatus(booking.bookingStatus)}
              </Badge>
              <Badge variant={PAYMENT_STATUS_COLOR[booking.paymentStatus] || 'default'}>
                {tStatus(booking.paymentStatus)}
              </Badge>
              {booking.paymentMethod && (
                <Badge variant={PAYMENT_METHOD_COLOR[booking.paymentMethod] || 'default'}>
                  {PAYMENT_METHOD_LABEL[booking.paymentMethod] || booking.paymentMethod}
                </Badge>
              )}
              {booking.refundStatus && (
                <Badge variant={REFUND_STATUS_COLOR[booking.refundStatus] || 'default'}>
                  {t('refund') || 'Refund'}: {tStatus(booking.refundStatus)}
                </Badge>
              )}
            </div>
          </div>

          {/* Body */}
          <div className="px-4 py-3 space-y-2 text-sm">
            <div className="flex justify-between gap-2">
              <span className="text-muted-foreground shrink-0">Customer</span>
              <div className="text-right min-w-0">
                <p className="text-foreground font-medium truncate">{booking.fullName || 'N/A'}</p>
                {booking.phoneNumber && (
                  <p className="text-xs text-muted-foreground flex items-center justify-end gap-1">
                    <Phone className="w-3 h-3" />{booking.phoneNumber}
                  </p>
                )}
              </div>
            </div>
            <div className="flex justify-between gap-2">
              <span className="text-muted-foreground shrink-0">Destination</span>
              <span className="text-foreground font-medium truncate">{booking.destination || 'N/A'}</span>
            </div>
            {booking.departureAt && (
              <div className="flex justify-between gap-2">
                <span className="text-muted-foreground shrink-0">Travel Date</span>
                <span className="text-foreground">✈ {fmtDate(booking.departureAt)}</span>
              </div>
            )}
            <div className="flex justify-between gap-2">
              <span className="text-muted-foreground shrink-0">Booked On</span>
              <span className="text-foreground">{fmtDate(booking.createdAt) || '—'}</span>
            </div>
          </div>

          {/* Actions */}
          <div className="px-4 py-3 border-t border-border flex gap-2">
            {booking.paymentStatus === 'PENDING' &&
              booking.bookingStatus !== 'CANCELLED' &&
              booking.bookingStatus !== 'REFUNDED' && (
                <Button
                  variant="outline"
                  size="sm"
                  className="flex-1 flex items-center justify-center gap-2"
                  onClick={() => handleMarkPaidClick(booking)}
                >
                  <Banknote className="w-4 h-4" />
                  Mark Paid
                </Button>
              )}
            <Button
              variant="outline"
              size="sm"
              className="flex-1 flex items-center justify-center gap-2"
              onClick={() => handleViewDetails(booking.id)}
            >
              <Eye className="w-4 h-4" />
              View Details
            </Button>
          </div>
        </div>
      ))}
    </div>
  );

  // ── Page render ───────────────────────────────────────────────────────────

  return (
    <div className="flex-1 p-4 sm:p-6 lg:p-8 overflow-y-auto bg-background min-h-screen">

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3 mb-4 sm:mb-6">
        <div>
          <h1 className="text-xl sm:text-2xl font-semibold text-foreground mb-1 flex items-center gap-2">
            <span>{t('bookingsManagement') || 'Bookings Management'}</span>
            {liveConnected ? (
              <span
                title={t('realtimeConnected') || 'Real-time updates connected'}
                className="inline-flex items-center gap-1.5 rounded-full border border-green-200 bg-green-50 px-2 py-0.5 text-xs font-medium text-green-700 dark:border-green-900/50 dark:bg-green-900/20 dark:text-green-400"
              >
                <span className="relative flex h-2 w-2">
                  <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-green-500 opacity-75"></span>
                  <span className="relative inline-flex h-2 w-2 rounded-full bg-green-500"></span>
                </span>
                <Wifi className="w-3 h-3" />
                <span>{t('live') || 'Live'}</span>
              </span>
            ) : (
              <span
                title={t('realtimeDisconnected') || 'Real-time updates disconnected'}
                className="inline-flex items-center gap-1.5 rounded-full border border-border bg-muted px-2 py-0.5 text-xs font-medium text-muted-foreground"
              >
                <span className="inline-flex h-2 w-2 rounded-full bg-muted-foreground/50"></span>
                <WifiOff className="w-3 h-3" />
                <span>{t('offline') || 'Offline'}</span>
              </span>
            )}
          </h1>
          <p className="text-sm text-muted-foreground">
            {t('manageAllBusTicketBookings') || 'Manage all bus ticket bookings'}
          </p>
        </div>
        <Button
          variant="primary"
          onClick={() => setCreateDialogOpen(true)}
          className="flex items-center justify-center gap-2 w-full sm:w-auto"
        >
          <Plus className="w-4 h-4 sm:w-5 sm:h-5" />
          <span>{t('createBooking') || 'Create Booking'}</span>
        </Button>
      </div>

      <BookingFilters onFilterChange={handleFilterChange} onReset={handleResetFilters} />

      {/* Content */}
      {loading ? (
        <>
          {DesktopSkeleton()}
          {MobileSkeleton()}
        </>
      ) : bookings.length === 0 ? (
        <div className="text-center py-12 sm:py-16 bg-card rounded-lg border border-border">
          <div className="bg-muted w-14 h-14 rounded-full flex items-center justify-center mx-auto mb-4">
            <Ticket className="w-7 h-7 text-muted-foreground" />
          </div>
          <h3 className="text-base font-semibold text-foreground mb-1">
            {t('noBookingsFound') || 'No bookings found'}
          </h3>
          <p className="text-sm text-muted-foreground px-4">
            {t('noBookingsMatchFilters') || 'No bookings match your current filters'}
          </p>
        </div>
      ) : (
        <>
          {DesktopTable()}
          {MobileCards()}

          {bookings.length > 0 && (
            <Pagination
              currentPage={pagination.currentPage}
              totalPages={pagination.totalPages}
              pageSize={pagination.size}
              totalElements={pagination.totalElements}
              onPageChange={goToPage}
              onPageSizeChange={changePageSize}
            />
          )}
        </>
      )}

      {/* Dialogs */}
      <BookingDetailsDialog
        open={detailsDialogOpen}
        onClose={() => { setDetailsDialogOpen(false); setSelectedBookingId(null); }}
        bookingId={selectedBookingId}
      />

      <CreateBookingDialog
        open={createDialogOpen}
        onClose={() => setCreateDialogOpen(false)}
        onSuccess={() => { setCreateDialogOpen(false); refetch(); }}
      />

      <ConfirmDialog
        isOpen={markPaidDialogOpen}
        onCancel={() => { setMarkPaidDialogOpen(false); setSelectedBooking(null); }}
        onConfirm={handleMarkPaidConfirm}
        title={t('markAsPaid') || 'Mark as Paid'}
        message={`${t('confirmMarkPaidMessage') || 'Are you sure you want to mark booking'} #${selectedBooking?.id} ${t('asPaid') || 'as paid'}?`}
        type="success"
      />
    </div>
  );
};

export default BookingsPage;
