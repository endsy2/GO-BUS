import React, { useState, useCallback, useEffect, useRef } from 'react';
import { formatStatus } from 'shared/utils/formatters';
import { useWallets } from '../../hooks/useWallets';
import { useTransactions } from '../../hooks/useTransactions';
import { Card, CardContent } from 'shared/components/ui/card';
import { Badge } from 'shared/components/common/Badge';
import { Button } from 'shared/components/common/Button';
import { DatePicker } from 'shared/components/ui/date-picker';
import { Skeleton } from 'shared/components/ui/skeleton';
import { Pagination } from 'shared/components/feedback/Pagination';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import WalletDetailsDialog from '../../components/WalletDetailsDialog/WalletDetailsDialog';
import {
  Wallet,
  User,
  DollarSign,
  TrendingUp,
  Filter,
  X,
  ArrowUpRight,
  ArrowDownLeft,
  Eye,
  Copy,
  Check,
  ChevronUp,
  ChevronDown
} from 'lucide-react';

// ── Shared filter field styling (matches BookingFilters) ───────────────────────
const inputClass =
  'w-full px-3 py-2 border border-slate-300 dark:border-slate-700 rounded-lg bg-white dark:bg-slate-800 text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm';

const labelClass =
  'block text-xs font-medium text-slate-500 dark:text-slate-400 mb-1.5 uppercase tracking-wide';

const sectionTitleClass =
  'text-xs font-semibold text-slate-400 dark:text-slate-500 uppercase tracking-wider mb-2.5';

// Trim string filter values (non-strings pass through). Empty values are kept
// on purpose so they overwrite — and thereby clear — any previously-applied
// value when merged in the hook; the service layer drops empties before
// building the query string.
const trimFilters = (filters) => {
  const out = {};
  Object.keys(filters).forEach(key => {
    out[key] = typeof filters[key] === 'string' ? filters[key].trim() : filters[key];
  });
  return out;
};

const CopyButton = ({ text }) => {
  const [copied, setCopied] = useState(false);
  const handleCopy = useCallback(() => {
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  }, [text]);
  return (
    <Button
      variant="ghost"
      size="icon"
      onClick={handleCopy}
      title={copied ? 'Copied!' : 'Copy'}
      className="h-6 w-6 text-muted-foreground hover:text-foreground"
    >
      {copied ? <Check className="w-3.5 h-3.5 text-green-500" /> : <Copy className="w-3.5 h-3.5" />}
    </Button>
  );
};

const WalletsPage = () => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;

  const [activeTab, setActiveTab] = useState('wallets');
  const [showWalletFilters, setShowWalletFilters] = useState(false);
  const [showTxFilters, setShowTxFilters] = useState(false);
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);
  const [selectedWalletId, setSelectedWalletId] = useState(null);

  const [walletFilters, setWalletFilters] = useState({
    name: '',
    status: '',
    minBalance: '',
    maxBalance: '',
  });
  
  const [txFilters, setTxFilters] = useState({
    type: '',
    status: '',
    referenceId: '',
    fromDate: '',
    toDate: '',
    minAmount: '',
    maxAmount: '',
  });

  const walletActiveCount = Object.values(walletFilters).filter((v) => v !== '').length;
  const txActiveCount = Object.values(txFilters).filter((v) => v !== '').length;

  const {
    wallets, loading: walletsLoading, pagination: walletPagination,
    updateFilters: updateWalletFilters, goToPage: goToWalletPage, changePageSize: changeWalletPageSize,
  } = useWallets();
  const {
    transactions, loading: txLoading, pagination: txPagination,
    updateFilters: updateTxFilters, goToPage: goToTxPage, changePageSize: changeTxPageSize,
  } = useTransactions();

  // Real-time search: all wallet filters apply automatically (debounced 400ms
  // after the last change) so the list updates as you type / pick — no
  // "Apply Filters" button needed. We send every field (incl. empty ones) so
  // clearing a field actually removes it from the applied filters.
  const skipFirstWalletSync = useRef(true);
  useEffect(() => {
    if (skipFirstWalletSync.current) {
      skipFirstWalletSync.current = false;
      return;
    }
    const handle = setTimeout(() => updateWalletFilters(trimFilters(walletFilters)), 400);
    return () => clearTimeout(handle);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [walletFilters]);

  // Real-time transaction filters apply automatically (debounced) too.
  const skipFirstTxSync = useRef(true);
  useEffect(() => {
    if (skipFirstTxSync.current) {
      skipFirstTxSync.current = false;
      return;
    }
    const handle = setTimeout(() => updateTxFilters(trimFilters(txFilters)), 400);
    return () => clearTimeout(handle);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [txFilters]);

  const handleWalletFilterChange = (e) => {
    const { name, value } = e.target;
    setWalletFilters(prev => ({ ...prev, [name]: value }));
  };

  const handleTxFilterChange = (e) => {
    const { name, value } = e.target;
    setTxFilters(prev => ({ ...prev, [name]: value }));
  };

  const resetWalletFilters = () => {
    const empty = { name: '', status: '', minBalance: '', maxBalance: '' };
    setWalletFilters(empty);
    // Skip the debounced effect's duplicate run and fetch the unfiltered list now.
    skipFirstWalletSync.current = true;
    updateWalletFilters(empty);
  };

  const resetTxFilters = () => {
    const empty = { type: '', status: '', referenceId: '', fromDate: '', toDate: '', minAmount: '', maxAmount: '' };
    setTxFilters(empty);
    // Skip the debounced effect's duplicate run and fetch the unfiltered list now.
    skipFirstTxSync.current = true;
    updateTxFilters(empty);
  };

  const handleViewDetails = (walletId) => {
    setSelectedWalletId(walletId);
    setDetailsDialogOpen(true);
  };

  const getStatusColor = (status) => {
    const statusMap = {
      ACTIVE: 'success',
      INACTIVE: 'warning',
      SUSPENDED: 'danger',
      CLOSED: 'default',
    };
    return statusMap[status] || 'default';
  };

  const getTxStatusColor = (status) => {
    const statusMap = {
      COMPLETED: 'success',
      PENDING: 'warning',
      FAILED: 'danger',
      CANCELLED: 'default',
    };
    return statusMap[status] || 'default';
  };

  // Inflows credit the wallet (green); outflows debit it (red).
  const isInflow = (type) => type === 'TOP_UP' || type === 'REFUND' || type === 'BONUS';

  const getTxTypeIcon = (type) => {
    return isInflow(type)
      ? <ArrowDownLeft className="w-4 h-4 text-green-400" />
      : <ArrowUpRight className="w-4 h-4 text-red-400" />;
  };

  const getTxTypeColor = (type) => {
    return isInflow(type)
      ? 'text-green-400'
      : 'text-red-400';
  };

  return (
    <div className="flex-1 p-4 sm:p-6 lg:p-8 overflow-y-auto bg-slate-50 dark:bg-slate-950 min-h-screen">
      <div className="mb-4 sm:mb-6 lg:mb-8">
        <h1 className="text-xl sm:text-2xl lg:text-3xl font-bold text-slate-900 dark:text-white mb-1 sm:mb-2 flex items-center gap-2 sm:gap-3">
          <Wallet className="w-6 h-6 sm:w-7 sm:h-7 text-blue-500 dark:text-blue-400 flex-shrink-0" />
          {t('walletManagement') || 'Wallet Management'}
        </h1>
        <p className="text-sm sm:text-base text-slate-600 dark:text-slate-400">
          {t('manageUserWalletsTransactions') || 'Manage user wallets and transactions'}
        </p>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 mb-4 sm:mb-6">
        <Button
          variant={activeTab === 'wallets' ? 'primary' : 'secondary'}
          onClick={() => setActiveTab('wallets')}
        >
          {t('wallets') || 'Wallets'}
        </Button>
        <Button
          variant={activeTab === 'transactions' ? 'primary' : 'secondary'}
          onClick={() => setActiveTab('transactions')}
        >
          {t('transactions') || 'Transactions'}
        </Button>
      </div>

      {/* Wallets Tab */}
      {activeTab === 'wallets' && (
        <>
          {/* Wallet Filters */}
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-4 sm:p-5 mb-6">
            <div className="flex justify-between items-center">
              <h3 className="text-base font-semibold text-slate-900 dark:text-white flex items-center gap-2">
                <Filter className="w-4 h-4 text-blue-500" />
                {t('filters') || 'Filters'}
                {walletActiveCount > 0 && (
                  <span className="inline-flex items-center justify-center h-5 min-w-[1.25rem] px-1.5 rounded-full bg-blue-500 text-white text-xs font-bold">
                    {walletActiveCount}
                  </span>
                )}
              </h3>
              <div className="flex items-center gap-2">
                {walletActiveCount > 0 && (
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={resetWalletFilters}
                    className="flex items-center gap-1 text-slate-600 dark:text-slate-300 text-xs border-slate-200 dark:border-slate-700"
                  >
                    <X className="w-3 h-3" />
                    {t('resetFilters') || 'Clear All'}
                  </Button>
                )}
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => setShowWalletFilters(!showWalletFilters)}
                  className="flex items-center gap-1 bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 border-slate-200 dark:border-slate-700 text-slate-700 dark:text-white text-sm"
                >
                  {showWalletFilters ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                  {showWalletFilters ? t('hide') || 'Hide' : t('show') || 'Show'}
                </Button>
              </div>
            </div>

            {showWalletFilters && (
              <div className="mt-4 space-y-5">

                {/* ── Section 1: Name & Status ───────────────────────── */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                  <div>
                    <label className={labelClass}>{t('name') || 'Name'}</label>
                    <input
                      type="text"
                      name="name"
                      value={walletFilters.name}
                      onChange={handleWalletFilterChange}
                      placeholder={t('searchByName') || 'Search by name...'}
                      className={inputClass}
                    />
                  </div>

                  <div>
                    <label className={labelClass}>{t('status') || 'Status'}</label>
                    <select
                      name="status"
                      value={walletFilters.status}
                      onChange={handleWalletFilterChange}
                      className={inputClass}
                    >
                      <option value="">{t('all') || 'All'}</option>
                      <option value="ACTIVE">Active</option>
                      <option value="INACTIVE">Inactive</option>
                      <option value="SUSPENDED">Suspended</option>
                      <option value="CLOSED">Closed</option>
                    </select>
                  </div>
                </div>

                <div className="border-t border-slate-100 dark:border-slate-800" />

                {/* ── Section 2: Balance Range ───────────────────────── */}
                <div>
                  <p className={sectionTitleClass}>{t('balance') || 'Balance'} Range</p>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                    <div>
                      <label className={labelClass}>{t('minBalance') || 'Min Balance'}</label>
                      <input
                        type="number"
                        name="minBalance"
                        value={walletFilters.minBalance}
                        onChange={handleWalletFilterChange}
                        placeholder="0.00"
                        className={inputClass}
                      />
                    </div>
                    <div>
                      <label className={labelClass}>{t('maxBalance') || 'Max Balance'}</label>
                      <input
                        type="number"
                        name="maxBalance"
                        value={walletFilters.maxBalance}
                        onChange={handleWalletFilterChange}
                        placeholder="10000.00"
                        className={inputClass}
                      />
                    </div>
                  </div>
                </div>

              </div>
            )}
          </div>

          {/* Wallets Table */}
          {walletsLoading ? (
            <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-slate-100 dark:bg-slate-800/50 border-b border-slate-200 dark:border-slate-700">
                    <tr>
                      <th className="px-4 py-3 text-left"><Skeleton className="h-4 w-20" /></th>
                      <th className="px-4 py-3 text-left"><Skeleton className="h-4 w-20" /></th>
                      <th className="px-4 py-3 text-left"><Skeleton className="h-4 w-20" /></th>
                      <th className="px-4 py-3 text-left"><Skeleton className="h-4 w-20" /></th>
                      <th className="px-4 py-3 text-left"><Skeleton className="h-4 w-20" /></th>
                      <th className="px-4 py-3 text-left"><Skeleton className="h-4 w-20" /></th>
                    </tr>
                  </thead>
                  <tbody>
                    {[1, 2, 3, 4].map(i => (
                      <tr key={i}>
                        <td className="px-4 py-4"><Skeleton className="h-4 w-32" /></td>
                        <td className="px-4 py-4"><Skeleton className="h-4 w-24" /></td>
                        <td className="px-4 py-4"><Skeleton className="h-4 w-28" /></td>
                        <td className="px-4 py-4"><Skeleton className="h-4 w-20" /></td>
                        <td className="px-4 py-4"><Skeleton className="h-4 w-24" /></td>
                        <td className="px-4 py-4"><Skeleton className="h-4 w-20" /></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          ) : wallets.length === 0 ? (
            <div className="text-center py-20 bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800">
              <div className="bg-slate-100 dark:bg-slate-800 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6">
                <Wallet className="w-10 h-10 text-slate-400 dark:text-slate-500" />
              </div>
              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-2">
                {t('noWalletsFound') || 'No wallets found'}
              </h3>
              <p className="text-slate-600 dark:text-slate-400">
                {t('noWalletsMatchFilters') || 'No wallets match your current filters'}
              </p>
            </div>
          ) : (
            <>
              <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 overflow-hidden">
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead className="bg-slate-100 dark:bg-slate-800/50 border-b border-slate-200 dark:border-slate-700">
                      <tr>
                        <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
                          {t('walletId') || 'Wallet ID'}
                        </th>
                        <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
                          {t('user') || 'User'}
                        </th>
                        <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
                          {t('balance') || 'Balance'}
                        </th>
                        <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
                          {t('currency') || 'Currency'}
                        </th>
                        <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
                          {t('status') || 'Status'}
                        </th>
                        <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
                          {t('createdDate') || 'Created Date'}
                        </th>
                        <th className="px-4 py-3 text-center text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
                          {t('actions') || 'Actions'}
                        </th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-200 dark:divide-slate-800">
                      {wallets.map(wallet => (
                        <tr 
                          key={wallet.id} 
                          className={`transition-colors ${
                            wallet.status === 'ACTIVE' 
                              ? 'bg-green-50/50 dark:bg-green-900/10 hover:bg-green-100/50 dark:hover:bg-green-900/20 border-l-4 border-l-green-500'
                              : wallet.status === 'SUSPENDED'
                              ? 'bg-red-50/50 dark:bg-red-900/10 hover:bg-red-100/50 dark:hover:bg-red-900/20 border-l-4 border-l-red-500'
                              : wallet.status === 'INACTIVE'
                              ? 'bg-yellow-50/50 dark:bg-yellow-900/10 hover:bg-yellow-100/50 dark:hover:bg-yellow-900/20 border-l-4 border-l-yellow-500'
                              : 'hover:bg-slate-50 dark:hover:bg-slate-800/30 border-l-4 border-l-transparent'
                          }`}
                        >
                          <td className="px-4 py-4">
                            <div className="flex items-center gap-2">
                              <div className="bg-blue-500/10 p-2 rounded-lg flex-shrink-0">
                                <Wallet className="w-4 h-4 text-blue-500" />
                              </div>
                              <span className="font-semibold text-slate-900 dark:text-white text-sm truncate max-w-[160px]" title={wallet.id}>
                                {wallet.id}
                              </span>
                              <CopyButton text={wallet.id} />
                            </div>
                          </td>
                          <td className="px-4 py-4">
                            <div className="flex items-center gap-2">
                              <div className="bg-purple-500/10 p-2 rounded-lg flex-shrink-0">
                                <User className="w-4 h-4 text-purple-500" />
                              </div>
                              <div className="min-w-0">
                                <p className="text-sm font-semibold text-slate-900 dark:text-white truncate max-w-[140px]" title={wallet.userName || wallet.fullName}>
                                  {wallet.userName || wallet.fullName || `#${wallet.userId}`}
                                </p>
                                <p className="text-xs text-slate-500 dark:text-slate-400">ID: {wallet.userId}</p>
                              </div>
                              <CopyButton text={wallet.userName || wallet.fullName || String(wallet.userId)} />
                            </div>
                          </td>
                          <td className="px-4 py-4">
                            <div className="flex items-center gap-2">
                              <DollarSign className="w-4 h-4 text-green-400" />
                              <span className="font-bold text-lg text-green-400">
                                ${wallet.balance?.toFixed(2)}
                              </span>
                            </div>
                          </td>
                          <td className="px-4 py-4">
                            <span className="text-slate-900 dark:text-white">{wallet.currency || 'USD'}</span>
                          </td>
                          <td className="px-4 py-4">
                            <Badge variant={getStatusColor(wallet.status)}>
                              {formatStatus(wallet.status)}
                            </Badge>
                          </td>
                          <td className="px-4 py-4">
                            <div className="flex items-center gap-2">
                              <span className="text-slate-900 dark:text-white text-sm">
                                {wallet.createdAt ? new Date(wallet.createdAt).toLocaleDateString() : 'N/A'}
                              </span>
                            </div>
                          </td>
                          <td className="px-4 py-4">
                            <div className="flex justify-center">
                              <Button
                                variant="ghost"
                                size="icon"
                                onClick={() => handleViewDetails(wallet.id)}
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

              {/* Pagination */}
              {wallets.length > 0 && (
                <Pagination
                  currentPage={walletPagination.currentPage}
                  totalPages={walletPagination.totalPages}
                  pageSize={walletPagination.size}
                  totalElements={walletPagination.totalElements}
                  onPageChange={goToWalletPage}
                  onPageSizeChange={changeWalletPageSize}
                />
              )}
            </>
          )}
        </>
      )}

      {/* Transactions Tab */}
      {activeTab === 'transactions' && (
        <>
          {/* Transaction Filters */}
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-4 sm:p-5 mb-6">
            <div className="flex justify-between items-center">
              <h3 className="text-base font-semibold text-slate-900 dark:text-white flex items-center gap-2">
                <Filter className="w-4 h-4 text-blue-500" />
                {t('filters') || 'Filters'}
                {txActiveCount > 0 && (
                  <span className="inline-flex items-center justify-center h-5 min-w-[1.25rem] px-1.5 rounded-full bg-blue-500 text-white text-xs font-bold">
                    {txActiveCount}
                  </span>
                )}
              </h3>
              <div className="flex items-center gap-2">
                {txActiveCount > 0 && (
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={resetTxFilters}
                    className="flex items-center gap-1 text-slate-600 dark:text-slate-300 text-xs border-slate-200 dark:border-slate-700"
                  >
                    <X className="w-3 h-3" />
                    {t('resetFilters') || 'Clear All'}
                  </Button>
                )}
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => setShowTxFilters(!showTxFilters)}
                  className="flex items-center gap-1 bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 border-slate-200 dark:border-slate-700 text-slate-700 dark:text-white text-sm"
                >
                  {showTxFilters ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                  {showTxFilters ? t('hide') || 'Hide' : t('show') || 'Show'}
                </Button>
              </div>
            </div>

            {showTxFilters && (
              <div className="mt-4 space-y-5">

                {/* ── Section 1: Type, Status & Reference ────────────── */}
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 sm:gap-4">
                  <div>
                    <label className={labelClass}>{t('type') || 'Type'}</label>
                    <select
                      name="type"
                      value={txFilters.type}
                      onChange={handleTxFilterChange}
                      className={inputClass}
                    >
                      <option value="">{t('all') || 'All'}</option>
                      <option value="TOP_UP">Top Up</option>
                      <option value="PAYMENT">Payment</option>
                      <option value="REFUND">Refund</option>
                      <option value="WITHDRAWAL">Withdrawal</option>
                      <option value="BONUS">Bonus</option>
                    </select>
                  </div>

                  <div>
                    <label className={labelClass}>{t('status') || 'Status'}</label>
                    <select
                      name="status"
                      value={txFilters.status}
                      onChange={handleTxFilterChange}
                      className={inputClass}
                    >
                      <option value="">{t('all') || 'All'}</option>
                      <option value="PENDING">Pending</option>
                      <option value="COMPLETED">Completed</option>
                      <option value="FAILED">Failed</option>
                      <option value="CANCELLED">Cancelled</option>
                    </select>
                  </div>

                  <div>
                    <label className={labelClass}>{t('referenceId') || 'Reference ID'}</label>
                    <input
                      type="text"
                      name="referenceId"
                      value={txFilters.referenceId}
                      onChange={handleTxFilterChange}
                      placeholder={t('searchByReference') || 'Search by reference...'}
                      className={inputClass}
                    />
                  </div>
                </div>

                <div className="border-t border-slate-100 dark:border-slate-800" />

                {/* ── Section 2: Date Range ──────────────────────────── */}
                <div>
                  <p className={sectionTitleClass}>{t('date') || 'Date'} Range</p>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                    <div>
                      <label className={labelClass}>{t('fromDate') || 'From Date'}</label>
                      <DatePicker
                        value={txFilters.fromDate}
                        onChange={(value) => setTxFilters(prev => ({ ...prev, fromDate: value }))}
                        placeholder={t('fromDate') || 'From Date'}
                      />
                    </div>
                    <div>
                      <label className={labelClass}>{t('toDate') || 'To Date'}</label>
                      <DatePicker
                        value={txFilters.toDate}
                        onChange={(value) => setTxFilters(prev => ({ ...prev, toDate: value }))}
                        placeholder={t('toDate') || 'To Date'}
                      />
                    </div>
                  </div>
                </div>

                <div className="border-t border-slate-100 dark:border-slate-800" />

                {/* ── Section 3: Amount Range ────────────────────────── */}
                <div>
                  <p className={sectionTitleClass}>{t('amount') || 'Amount'} Range</p>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                    <div>
                      <label className={labelClass}>{t('minAmount') || 'Min Amount'}</label>
                      <input
                        type="number"
                        name="minAmount"
                        value={txFilters.minAmount}
                        onChange={handleTxFilterChange}
                        placeholder="0.00"
                        className={inputClass}
                      />
                    </div>
                    <div>
                      <label className={labelClass}>{t('maxAmount') || 'Max Amount'}</label>
                      <input
                        type="number"
                        name="maxAmount"
                        value={txFilters.maxAmount}
                        onChange={handleTxFilterChange}
                        placeholder="10000.00"
                        className={inputClass}
                      />
                    </div>
                  </div>
                </div>

              </div>
            )}
          </div>

          {/* Transactions List */}
          {txLoading ? (
            <div className="space-y-3">
              {[1, 2, 3, 4].map(i => (
                <Card key={i} className="bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800">
                  <CardContent className="p-4">
                    <Skeleton className="h-16 w-full" />
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : transactions.length === 0 ? (
            <div className="text-center py-20 bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800">
              <div className="bg-slate-100 dark:bg-slate-800 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6">
                <TrendingUp className="w-10 h-10 text-slate-400 dark:text-slate-500" />
              </div>
              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-2">
                {t('noTransactionsFound') || 'No transactions found'}
              </h3>
              <p className="text-slate-600 dark:text-slate-400">
                {t('noTransactionsMatchFilters') || 'No transactions match your current filters'}
              </p>
            </div>
          ) : (
            <>
              <div className="space-y-3">
                {transactions.map(tx => (
                  <Card 
                    key={tx.id}
                    className="bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800 transition-all duration-300 hover:border-slate-300 dark:hover:border-slate-700"
                  >
                    <CardContent className="p-4">
                      <div className="flex items-center justify-between gap-4">
                        <div className="flex items-center gap-4 flex-1 min-w-0">
                          <div className={`p-3 rounded-lg ${
                            isInflow(tx.type)
                              ? 'bg-green-500/10'
                              : 'bg-red-500/10'
                          }`}>
                            {getTxTypeIcon(tx.type)}
                          </div>
                          
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-2 mb-1">
                              <p className="font-semibold text-slate-900 dark:text-white">{tx.userName}</p>
                              <Badge variant={getTxStatusColor(tx.status)} className="text-xs">
                                {formatStatus(tx.status)}
                              </Badge>
                            </div>
                            <p className="text-sm text-slate-600 dark:text-slate-400">
                              {formatStatus(tx.type)} • {new Date(tx.createdAt).toLocaleString()}
                            </p>
                            {tx.description && (
                              <p className="text-xs text-slate-500 dark:text-slate-500 mt-1">{tx.description}</p>
                            )}
                          </div>
                        </div>

                        <div className="text-right">
                          <p className={`text-xl font-bold ${getTxTypeColor(tx.type)}`}>
                            {isInflow(tx.type) ? '+' : '-'}
                            ${tx.amount?.toFixed(2)}
                          </p>
                          {tx.referenceId && (
                            <p className="text-xs text-slate-500 dark:text-slate-500 mt-1">
                              Ref: {tx.referenceId}
                            </p>
                          )}
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>

              {/* Pagination */}
              {transactions.length > 0 && (
                <Pagination
                  currentPage={txPagination.currentPage}
                  totalPages={txPagination.totalPages}
                  pageSize={txPagination.size}
                  totalElements={txPagination.totalElements}
                  onPageChange={goToTxPage}
                  onPageSizeChange={changeTxPageSize}
                />
              )}
            </>
          )}
        </>
      )}

      {/* Wallet Details Dialog */}
      <WalletDetailsDialog
        open={detailsDialogOpen}
        onClose={() => {
          setDetailsDialogOpen(false);
          setSelectedWalletId(null);
        }}
        walletId={selectedWalletId}
      />
    </div>
  );
};

export default WalletsPage;
