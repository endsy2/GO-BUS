import React, { useState, useEffect, useRef } from 'react';
import { Button } from 'shared/components/common/Button';
import { DatePicker } from 'shared/components/ui/date-picker';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import { Filter, X, ChevronUp, ChevronDown } from 'lucide-react';

const EMPTY_FILTERS = {
  username: '',
  bookingStatus: '',
  paymentStatus: '',
  paymentMethod: '',
  refund: '',
  departureFrom: '',
  departureTo: '',
  createdFrom: '',
  createdTo: '',
};

const inputClass =
  'w-full px-3 py-2 border border-slate-300 dark:border-slate-700 rounded-lg bg-white dark:bg-slate-800 text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm';

const labelClass =
  'block text-xs font-medium text-slate-500 dark:text-slate-400 mb-1.5 uppercase tracking-wide';

const sectionTitleClass =
  'text-xs font-semibold text-slate-400 dark:text-slate-500 uppercase tracking-wider mb-2.5';

const BookingFilters = ({ onFilterChange, onReset }) => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;

  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [showFilters, setShowFilters] = useState(true);

  const activeCount = Object.values(filters).filter((v) => v !== '').length;

  // Debounced real-time filtering — apply 400 ms after the user stops typing.
  const skipFirstApply = useRef(true);
  useEffect(() => {
    if (skipFirstApply.current) {
      skipFirstApply.current = false;
      return;
    }
    const handle = setTimeout(() => onFilterChange({ ...filters }), 400);
    return () => clearTimeout(handle);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value }));
  };

  // DatePicker calls onChange(string) directly, not an event object
  const handleDateChange = (name, value) => {
    setFilters((prev) => ({ ...prev, [name]: value }));
  };

  const handleReset = () => {
    const hasActiveFilters = Object.values(filters).some((v) => v !== '');
    skipFirstApply.current = true;
    setFilters(EMPTY_FILTERS);
    if (hasActiveFilters) onReset();
  };

  return (
    <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-4 sm:p-5 mb-6">

      {/* Header row */}
      <div className="flex justify-between items-center">
        <h3 className="text-base font-semibold text-slate-900 dark:text-white flex items-center gap-2">
          <Filter className="w-4 h-4 text-blue-500" />
          {t('filters') || 'Filters'}
          {activeCount > 0 && (
            <span className="inline-flex items-center justify-center h-5 min-w-[1.25rem] px-1.5 rounded-full bg-blue-500 text-white text-xs font-bold">
              {activeCount}
            </span>
          )}
        </h3>
        <div className="flex items-center gap-2">
          {activeCount > 0 && (
            <Button
              variant="secondary"
              size="sm"
              onClick={handleReset}
              className="flex items-center gap-1 text-slate-600 dark:text-slate-300 text-xs border-slate-200 dark:border-slate-700"
            >
              <X className="w-3 h-3" />
              {t('resetFilters') || 'Clear All'}
            </Button>
          )}
          <Button
            variant="secondary"
            size="sm"
            onClick={() => setShowFilters(!showFilters)}
            className="flex items-center gap-1 bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 border-slate-200 dark:border-slate-700 text-slate-700 dark:text-white text-sm"
          >
            {showFilters ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
            {showFilters ? t('hide') || 'Hide' : t('show') || 'Show'}
          </Button>
        </div>
      </div>

      {showFilters && (
        <div className="mt-4 space-y-5">

          {/* ── Section 1: Customer & Status ─────────────────────────── */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 sm:gap-4">
            <div>
              <label className={labelClass}>
                {t('customerName') || 'Customer Name / Username'}
              </label>
              <input
                type="text"
                name="username"
                value={filters.username}
                onChange={handleChange}
                placeholder={t('searchByUsername') || 'Search By Name Or Username'}
                className={inputClass}
              />
            </div>

            <div>
              <label className={labelClass}>{t('bookingStatus') || 'Booking Status'}</label>
              <select name="bookingStatus" value={filters.bookingStatus} onChange={handleChange} className={inputClass}>
                <option value="">{t('allStatuses') || 'All Statuses'}</option>
                <option value="PENDING">Pending</option>
                <option value="CONFIRMED">Confirmed</option>
                <option value="CANCELLED">Cancelled</option>
                <option value="FAILED">Failed</option>
                <option value="REFUND_REQUESTED">Refund Requested</option>
                <option value="REFUNDED">Refunded</option>
              </select>
            </div>

            <div>
              <label className={labelClass}>{t('paymentStatus') || 'Payment Status'}</label>
              <select name="paymentStatus" value={filters.paymentStatus} onChange={handleChange} className={inputClass}>
                <option value="">{t('allStatuses') || 'All Statuses'}</option>
                <option value="PENDING">Pending</option>
                <option value="SUCCESS">Success</option>
                <option value="FAILED">Failed</option>
                <option value="EXPIRED">Expired</option>
                <option value="TIMEOUT">Timeout</option>
                <option value="CANCELLED">Cancelled</option>
                <option value="REFUNDED">Refunded</option>
              </select>
            </div>
          </div>

          {/* ── Section 2: Payment Method & Refund ───────────────────── */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 sm:gap-4">
            <div>
              <label className={labelClass}>{t('paymentMethod') || 'Payment Method'}</label>
              <select name="paymentMethod" value={filters.paymentMethod} onChange={handleChange} className={inputClass}>
                <option value="">{t('allMethods') || 'All Methods'}</option>
                <option value="WALLET">Wallet</option>
                <option value="BAKONG">Bakong QR</option>
                <option value="CASH">Cash</option>
                <option value="ADMIN">Admin (Force-paid)</option>
              </select>
            </div>

            <div>
              <label className={labelClass}>{t('refundRequest') || 'Refund Request'}</label>
              <select name="refund" value={filters.refund} onChange={handleChange} className={inputClass}>
                <option value="">{t('all') || 'All'}</option>
                <option value="true">{t('hasRefundRequest') || 'Has Refund Request'}</option>
                <option value="false">{t('noRefundRequest') || 'No Refund Request'}</option>
              </select>
            </div>
          </div>

          <div className="border-t border-slate-100 dark:border-slate-800" />

          {/* ── Section 3: Travel Date (Departure) ───────────────────── */}
          <div>
            <p className={sectionTitleClass}>
              {t('travelDate') || 'Travel Date (Departure)'}
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
              <div>
                <label className={labelClass}>{t('from') || 'From'}</label>
                <DatePicker
                  value={filters.departureFrom}
                  onChange={(val) => handleDateChange('departureFrom', val)}
                  placeholder="Select departure from"
                />
              </div>
              <div>
                <label className={labelClass}>{t('to') || 'To'}</label>
                <DatePicker
                  value={filters.departureTo}
                  onChange={(val) => handleDateChange('departureTo', val)}
                  placeholder="Select departure to"
                />
              </div>
            </div>
          </div>

          {/* ── Section 4: Booking Date (Created) ────────────────────── */}
          <div>
            <p className={sectionTitleClass}>
              {t('bookingDate') || 'Booking Date (When Booked)'}
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
              <div>
                <label className={labelClass}>{t('from') || 'From'}</label>
                <DatePicker
                  value={filters.createdFrom}
                  onChange={(val) => handleDateChange('createdFrom', val)}
                  placeholder="Select booked from"
                />
              </div>
              <div>
                <label className={labelClass}>{t('to') || 'To'}</label>
                <DatePicker
                  value={filters.createdTo}
                  onChange={(val) => handleDateChange('createdTo', val)}
                  placeholder="Select booked to"
                />
              </div>
            </div>
          </div>

        </div>
      )}
    </div>
  );
};

export default BookingFilters;
