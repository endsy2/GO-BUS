import React, { useState, useEffect, useRef } from 'react';
import { Button } from 'shared/components/common/Button';
import { DatePicker } from 'shared/components/ui/date-picker';
import { Filter, X, ChevronUp, ChevronDown } from 'lucide-react';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';

const inputClass =
  'w-full px-3 py-2 border border-slate-300 dark:border-slate-700 rounded-lg bg-white dark:bg-slate-800 text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm';

const labelClass =
  'block text-xs font-medium text-slate-500 dark:text-slate-400 mb-1.5 uppercase tracking-wide';

const sectionTitleClass =
  'text-xs font-semibold text-slate-400 dark:text-slate-500 uppercase tracking-wider mb-2.5';

const RefundFilters = ({ onFilterChange, onReset }) => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;

  const [showFilters, setShowFilters] = useState(true);
  const [status, setStatus] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');

  const activeCount = [status, fromDate, toDate].filter((v) => v !== '').length;

  // Real-time search: filters apply automatically (debounced 400ms after the
  // last change) so the list updates as you pick — no "Apply" button needed.
  // We always send every field (incl. empty ones) so clearing one actually
  // removes it from the applied filters.
  const skipFirstSync = useRef(true);
  useEffect(() => {
    if (skipFirstSync.current) {
      skipFirstSync.current = false;
      return;
    }
    const handle = setTimeout(() => {
      onFilterChange({
        status,
        fromDate: fromDate ? new Date(fromDate).toISOString() : '',
        toDate: toDate ? new Date(toDate).toISOString() : '',
      });
    }, 400);
    return () => clearTimeout(handle);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [status, fromDate, toDate]);

  const handleReset = () => {
    setStatus('');
    setFromDate('');
    setToDate('');
    // Skip the debounced effect's duplicate run; onReset fetches the unfiltered list now.
    skipFirstSync.current = true;
    onReset();
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

          {/* ── Section 1: Status ──────────────────────────────────── */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 sm:gap-4">
            <div>
              <label className={labelClass}>{t('status') || 'Status'}</label>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value)}
                className={inputClass}
              >
                <option value="">{t('all') || 'All'}</option>
                <option value="PENDING">Pending</option>
                <option value="APPROVED">Approved</option>
                <option value="REJECTED">Rejected</option>
                <option value="COMPLETED">Completed</option>
              </select>
            </div>
          </div>

          <div className="border-t border-slate-100 dark:border-slate-800" />

          {/* ── Section 2: Date Range ──────────────────────────────── */}
          <div>
            <p className={sectionTitleClass}>{t('date') || 'Date'} Range</p>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
              <div>
                <label className={labelClass}>{t('fromDate') || 'From Date'}</label>
                <DatePicker
                  value={fromDate}
                  onChange={(value) => setFromDate(value)}
                  placeholder={t('fromDate') || 'From Date'}
                />
              </div>
              <div>
                <label className={labelClass}>{t('toDate') || 'To Date'}</label>
                <DatePicker
                  value={toDate}
                  onChange={(value) => setToDate(value)}
                  placeholder={t('toDate') || 'To Date'}
                />
              </div>
            </div>
          </div>

        </div>
      )}
    </div>
  );
};

export default RefundFilters;
