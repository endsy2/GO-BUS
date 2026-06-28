import React, { useState } from 'react';
import { formatStatus } from 'shared/utils/formatters';
import { useRefunds } from '../../hooks/useRefunds';
import { Badge } from 'shared/components/common/Badge';
import { Button } from 'shared/components/common/Button';
import { Skeleton } from 'shared/components/ui/skeleton';
import { Pagination } from 'shared/components/feedback/Pagination';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import { useToast } from 'shared/components/ui/toast';
import {
  DollarSign,
  User,
  Calendar,
  FileText,
  CheckCircle,
  CreditCard,
} from 'lucide-react';
import RefundFilters from '../../components/RefundFilters/RefundFilters';
import ProcessRefundDialog from '../../components/ProcessRefundDialog/ProcessRefundDialog';
import RefundStatistics from '../../components/RefundStatistics/RefundStatistics';
import refundService from '../../services/refundService';

const RefundsPage = () => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const { addToast } = useToast();
  const { refunds, loading, pagination, filters, updateFilters, resetFilters, goToPage, changePageSize, refetch } = useRefunds();
  
  const [processDialogOpen, setProcessDialogOpen] = useState(false);
  const [selectedRefund, setSelectedRefund] = useState(null);

  const handleProcessClick = (refund) => {
    setSelectedRefund(refund);
    setProcessDialogOpen(true);
  };

  const handleProcessRefund = async (refundId, approved, rejectionReason) => {
    try {
      await refundService.processRefund(refundId, approved, rejectionReason);
      setProcessDialogOpen(false);
      setSelectedRefund(null);
      refetch();
      addToast({ 
        message: approved 
          ? (t('refundApprovedSuccess') || 'Refund approved successfully')
          : (t('refundRejectedSuccess') || 'Refund rejected successfully'),
        type: 'success' 
      });
    } catch (error) {
      addToast({ 
        message: error.response?.data?.message || 'Failed to process refund', 
        type: 'error' 
      });
    }
  };

  const handleFilterChange = (newFilters) => {
    updateFilters(newFilters);
  };

  const handleResetFilters = () => {
    resetFilters();
  };

  const getStatusColor = (status) => {
    const statusMap = {
      PENDING: 'warning',
      APPROVED: 'success',
      REJECTED: 'danger',
      COMPLETED: 'info',
    };
    return statusMap[status] || 'default';
  };

  const getRowBackgroundColor = (status) => {
    const colorMap = {
      APPROVED: 'bg-green-50/50 dark:bg-green-900/10 hover:bg-green-100/50 dark:hover:bg-green-900/20 border-l-4 border-l-green-500',
      COMPLETED: 'bg-blue-50/50 dark:bg-blue-900/10 hover:bg-blue-100/50 dark:hover:bg-blue-900/20 border-l-4 border-l-blue-500',
      PENDING: 'bg-yellow-50/50 dark:bg-yellow-900/10 hover:bg-yellow-100/50 dark:hover:bg-yellow-900/20 border-l-4 border-l-yellow-500',
      REJECTED: 'bg-red-50/50 dark:bg-red-900/10 hover:bg-red-100/50 dark:hover:bg-red-900/20 border-l-4 border-l-red-500',
    };
    return colorMap[status] || 'hover:bg-slate-50 dark:hover:bg-slate-800/30 border-l-4 border-l-transparent';
  };

  return (
    <div className="flex-1 p-4 sm:p-6 lg:p-8 overflow-y-auto bg-slate-50 dark:bg-slate-950 min-h-screen">
      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3 mb-4 sm:mb-6 lg:mb-8">
        <div>
          <h1 className="text-xl sm:text-2xl lg:text-3xl font-bold text-slate-900 dark:text-white mb-1 sm:mb-2 flex items-center gap-2 sm:gap-3">
            <DollarSign className="w-6 h-6 sm:w-7 sm:h-7 text-blue-500 flex-shrink-0" />
            {t('refundsManagement') || 'Refunds Management'}
          </h1>
          <p className="text-sm sm:text-base text-slate-600 dark:text-slate-400">
            {t('manageAllRefundRequests') || 'Manage all refund requests'}
          </p>
        </div>
      </div>

      {/* Filters */}
      <RefundFilters
        onFilterChange={handleFilterChange}
        onReset={handleResetFilters}
      />

      {/* Statistics */}
      {filters.fromDate && filters.toDate && (
        <RefundStatistics fromDate={filters.fromDate} toDate={filters.toDate} />
      )}

      {/* Refunds Table */}
      {loading ? (
        <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-slate-100 dark:bg-slate-800/50 border-b border-slate-200 dark:border-slate-700">
                <tr>
                  {['Refund ID', 'Booking ID', 'Customer', 'Amount', 'Refund Method', 'Reason', 'Status', 'Created Date', 'Actions'].map(col => (
                    <th key={col} className="px-4 py-3">
                      <Skeleton className="h-4 w-20" />
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200 dark:divide-slate-800">
                {[1, 2, 3, 4, 5].map(i => (
                  <tr key={i}>
                    {[1, 2, 3, 4, 5, 6, 7, 8, 9].map(j => (
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
      ) : refunds.length === 0 ? (
        <div className="text-center py-12 sm:py-20 bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800">
          <div className="bg-slate-100 dark:bg-slate-800 w-16 h-16 sm:w-20 sm:h-20 rounded-full flex items-center justify-center mx-auto mb-4 sm:mb-6">
            <DollarSign className="w-8 h-8 sm:w-10 sm:h-10 text-slate-400 dark:text-slate-500" />
          </div>
          <h3 className="text-lg sm:text-xl font-semibold text-slate-900 dark:text-white mb-2">
            {t('noRefundsFound') || 'No refunds found'}
          </h3>
          <p className="text-sm sm:text-base text-slate-600 dark:text-slate-400 px-4">
            {t('noRefundsMatchFilters') || 'No refunds match your current filters'}
          </p>
        </div>
      ) : (
        <>
          {/* Desktop Table View */}
          <div className="hidden md:block bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-slate-100 dark:bg-slate-800/50 border-b border-slate-200 dark:border-slate-700">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
                      {t('refundId') || 'Refund ID'}
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
                      {t('bookingId') || 'Booking ID'}
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
                      {t('customer') || 'Customer'}
                    </th>
                    <th className="px-4 py-3 text-right text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
                      {t('amount') || 'Amount'}
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
                      {t('refundMethod') || 'Refund Method'}
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
                      {t('reason') || 'Reason'}
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
                  {refunds.map(refund => (
                    <tr key={refund.id} className={`transition-colors ${getRowBackgroundColor(refund.status)}`}>
                      <td className="px-4 py-4">
                        <div className="flex items-center gap-2">
                          <div className="bg-blue-500/10 p-2 rounded-lg">
                            <FileText className="w-4 h-4 text-blue-500" />
                          </div>
                          <span className="font-semibold text-slate-900 dark:text-white">#{refund.id}</span>
                        </div>
                      </td>
                      <td className="px-4 py-4">
                        <span className="font-medium text-slate-900 dark:text-white">#{refund.bookingId}</span>
                      </td>
                      <td className="px-4 py-4">
                        <div className="flex items-center gap-2">
                          <User className="w-4 h-4 text-purple-400" />
                          <span className="text-slate-900 dark:text-white">{refund.customerName || 'N/A'}</span>
                        </div>
                      </td>
                      <td className="px-4 py-4 text-right">
                        <span className="font-bold text-lg text-green-400">
                          ${(refund.amount ?? refund.refundAmount)?.toFixed(2) ?? '0.00'}
                        </span>
                      </td>
                      <td className="px-4 py-4">
                        <span className="text-sm text-slate-900 dark:text-white">
                          {refund.refundMethod ? formatStatus(refund.refundMethod) : 'N/A'}
                        </span>
                      </td>
                      <td className="px-4 py-4">
                        <span className="text-sm text-slate-600 dark:text-slate-400 max-w-xs truncate block">
                          {refund.reason || 'N/A'}
                        </span>
                      </td>
                      <td className="px-4 py-4">
                        <Badge variant={getStatusColor(refund.status)}>
                          {formatStatus(refund.status)}
                        </Badge>
                      </td>
                      <td className="px-4 py-4">
                        <div className="flex items-center gap-2">
                          <Calendar className="w-4 h-4 text-pink-400" />
                          <span className="text-slate-900 dark:text-white text-sm">
                            {refund.createdAt ? new Date(refund.createdAt).toLocaleDateString() : 'N/A'}
                          </span>
                        </div>
                      </td>
                      <td className="px-4 py-4">
                        <div className="flex justify-center gap-2">
                          {refund.status === 'PENDING' && (
                            <Button
                              variant="ghost"
                              className="w-9 h-9 p-0 flex items-center justify-center text-emerald-600 dark:text-emerald-400 hover:bg-emerald-500/10"
                              onClick={() => handleProcessClick(refund)}
                              title="Process Refund"
                            >
                              <CheckCircle className="w-4 h-4" />
                            </Button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Mobile Card View */}
          <div className="md:hidden space-y-3">
            {refunds.map(refund => (
              <div
                key={refund.id}
                className={`bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 overflow-hidden ${getRowBackgroundColor(refund.status)}`}
              >
                {/* Card Header */}
                <div className="p-3 border-b border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/30">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <div className="bg-blue-500/10 p-1.5 rounded-lg">
                        <FileText className="w-4 h-4 text-blue-500" />
                      </div>
                      <span className="font-bold text-slate-900 dark:text-white text-sm">#{refund.id}</span>
                      <span className="text-xs text-slate-500 dark:text-slate-400">Booking #{refund.bookingId}</span>
                    </div>
                    <span className="font-bold text-base text-green-400">${(refund.amount ?? refund.refundAmount)?.toFixed(2) ?? '0.00'}</span>
                  </div>
                  <Badge variant={getStatusColor(refund.status)} className="text-xs">
                    {formatStatus(refund.status)}
                  </Badge>
                </div>

                {/* Card Body */}
                <div className="p-3 space-y-2">
                  <div className="flex items-center gap-2 text-sm">
                    <User className="w-3.5 h-3.5 text-purple-400 flex-shrink-0" />
                    <span className="text-slate-900 dark:text-white font-medium truncate">{refund.customerName || 'N/A'}</span>
                  </div>
                  {refund.reason && (
                    <p className="text-xs text-slate-500 dark:text-slate-400 line-clamp-2">{refund.reason}</p>
                  )}
                  <div className="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400">
                    <CreditCard className="w-3 h-3 text-amber-400" />
                    <span>{refund.refundMethod ? formatStatus(refund.refundMethod) : 'N/A'}</span>
                  </div>
                  <div className="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400">
                    <Calendar className="w-3 h-3 text-pink-400" />
                    <span>{refund.createdAt ? new Date(refund.createdAt).toLocaleDateString() : 'N/A'}</span>
                  </div>
                </div>

                {/* Card Actions */}
                {refund.status === 'PENDING' && (
                  <div className="p-3 border-t border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/30">
                    <Button
                      variant="secondary"
                      className="w-full flex items-center justify-center gap-2 text-emerald-600 dark:text-emerald-400"
                      onClick={() => handleProcessClick(refund)}
                    >
                      <CheckCircle className="w-4 h-4" />
                      <span className="font-medium">Process Refund</span>
                    </Button>
                  </div>
                )}
              </div>
            ))}
          </div>

          {/* Pagination */}
          {refunds.length > 0 && (
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

      {/* Process Refund Dialog */}
      <ProcessRefundDialog
        open={processDialogOpen}
        onClose={() => {
          setProcessDialogOpen(false);
          setSelectedRefund(null);
        }}
        refund={selectedRefund}
        onProcess={handleProcessRefund}
      />
    </div>
  );
};

export default RefundsPage;
