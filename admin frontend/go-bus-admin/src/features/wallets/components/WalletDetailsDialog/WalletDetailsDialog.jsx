import React, { useEffect, useState } from 'react';
import { formatStatus } from 'shared/utils/formatters';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'shared/components/ui/dialog';
import { Badge } from 'shared/components/common/Badge';
import { Skeleton } from 'shared/components/ui/skeleton';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import walletService from '../../services/walletService';
import { 
  Wallet, 
  User, 
  DollarSign, 
  Calendar,
  Clock,
  CreditCard,
  Activity
} from 'lucide-react';

const WalletDetailsDialog = ({ open, onClose, walletId }) => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  
  const [wallet, setWallet] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (open && walletId) {
      fetchWalletDetails();
    }
  }, [open, walletId]);

  const fetchWalletDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await walletService.getWalletById(walletId);
      setWallet(response);
    } catch (err) {
      setError(err.message || 'Failed to fetch wallet details');
    } finally {
      setLoading(false);
    }
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

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleString();
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800">
        <DialogHeader>
          <DialogTitle className="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-3">
            <div className="bg-blue-500/10 p-2 rounded-lg">
              <Wallet className="w-6 h-6 text-blue-500" />
            </div>
            {t('walletDetails') || 'Wallet Details'}
          </DialogTitle>
        </DialogHeader>

        {loading ? (
          <div className="space-y-4 py-4">
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-20 w-full" />
          </div>
        ) : error ? (
          <div className="py-8 text-center">
            <p className="text-red-500 dark:text-red-400">{error}</p>
          </div>
        ) : wallet ? (
          <div className="space-y-6 py-4">
            {/* Wallet ID & Status */}
            <div className="bg-slate-50 dark:bg-slate-800/50 rounded-xl p-5 border border-slate-200 dark:border-slate-700">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center gap-3">
                  <div className="bg-blue-500/10 p-3 rounded-lg">
                    <CreditCard className="w-6 h-6 text-blue-500" />
                  </div>
                  <div>
                    <p className="text-xs text-slate-500 dark:text-slate-400 mb-1">
                      {t('walletId') || 'Wallet ID'}
                    </p>
                    <p className="font-mono text-sm font-semibold text-slate-900 dark:text-white break-all">
                      {wallet.id}
                    </p>
                  </div>
                </div>
                <Badge variant={getStatusColor(wallet.status)} className="text-sm px-3 py-1">
                  {formatStatus(wallet.status)}
                </Badge>
              </div>
            </div>

            {/* User ID */}
            <div className="bg-slate-50 dark:bg-slate-800/50 rounded-xl p-5 border border-slate-200 dark:border-slate-700">
              <div className="flex items-center gap-3">
                <div className="bg-purple-500/10 p-3 rounded-lg">
                  <User className="w-6 h-6 text-purple-500" />
                </div>
                <div className="flex-1">
                  <p className="text-xs text-slate-500 dark:text-slate-400 mb-1">
                    {t('userId') || 'User ID'}
                  </p>
                  <p className="text-lg font-semibold text-slate-900 dark:text-white">
                    #{wallet.userId}
                  </p>
                </div>
              </div>
            </div>

            {/* Balance & Currency */}
            <div className="bg-gradient-to-br from-green-50 to-emerald-50 dark:from-green-900/20 dark:to-emerald-900/20 rounded-xl p-5 border border-green-200 dark:border-green-800">
              <div className="flex items-center gap-3 mb-3">
                <div className="bg-green-500/20 p-3 rounded-lg">
                  <DollarSign className="w-6 h-6 text-green-600 dark:text-green-400" />
                </div>
                <div className="flex-1">
                  <p className="text-xs text-green-700 dark:text-green-400 mb-1">
                    {t('currentBalance') || 'Current Balance'}
                  </p>
                  <p className="text-3xl font-bold text-green-600 dark:text-green-400">
                    ${wallet.balance?.toFixed(2)}
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-2 text-sm text-green-700 dark:text-green-400">
                <span className="font-medium">{t('currency') || 'Currency'}:</span>
                <span className="font-semibold">{wallet.currency || 'USD'}</span>
              </div>
            </div>

            {/* Last Transaction */}
            <div className="bg-slate-50 dark:bg-slate-800/50 rounded-xl p-5 border border-slate-200 dark:border-slate-700">
              <div className="flex items-center gap-3">
                <div className="bg-orange-500/10 p-3 rounded-lg">
                  <Activity className="w-6 h-6 text-orange-500" />
                </div>
                <div className="flex-1">
                  <p className="text-xs text-slate-500 dark:text-slate-400 mb-1">
                    {t('lastTransaction') || 'Last Transaction'}
                  </p>
                  <p className="text-sm font-medium text-slate-900 dark:text-white">
                    {formatDate(wallet.lastTransaction)}
                  </p>
                </div>
              </div>
            </div>

            {/* Timestamps */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Created At */}
              <div className="bg-slate-50 dark:bg-slate-800/50 rounded-xl p-4 border border-slate-200 dark:border-slate-700">
                <div className="flex items-center gap-3">
                  <div className="bg-blue-500/10 p-2 rounded-lg">
                    <Calendar className="w-5 h-5 text-blue-500" />
                  </div>
                  <div className="flex-1">
                    <p className="text-xs text-slate-500 dark:text-slate-400 mb-1">
                      {t('createdAt') || 'Created At'}
                    </p>
                    <p className="text-sm font-medium text-slate-900 dark:text-white">
                      {formatDate(wallet.createdAt)}
                    </p>
                  </div>
                </div>
              </div>

              {/* Updated At */}
              <div className="bg-slate-50 dark:bg-slate-800/50 rounded-xl p-4 border border-slate-200 dark:border-slate-700">
                <div className="flex items-center gap-3">
                  <div className="bg-indigo-500/10 p-2 rounded-lg">
                    <Clock className="w-5 h-5 text-indigo-500" />
                  </div>
                  <div className="flex-1">
                    <p className="text-xs text-slate-500 dark:text-slate-400 mb-1">
                      {t('updatedAt') || 'Updated At'}
                    </p>
                    <p className="text-sm font-medium text-slate-900 dark:text-white">
                      {formatDate(wallet.updatedAt)}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        ) : null}
      </DialogContent>
    </Dialog>
  );
};

export default WalletDetailsDialog;
