import React, { useState } from 'react';
import { Download, FileText, CreditCard, RefreshCw, Tag, Calendar } from 'lucide-react';
import { Button } from 'shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from 'shared/components/ui/card';
import { Input } from 'shared/components/common/Input';
import { DatePicker } from 'shared/components/ui/date-picker';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'shared/components/ui/select';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import { useToast } from 'shared/components/ui/toast';
import reportService from '../../services/reportService';

const FinancialReportsTab = () => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const { addToast } = useToast();

  // Revenue Report State
  const [revenueLoading, setRevenueLoading] = useState(false);
  const [revenueFilters, setRevenueFilters] = useState({
    startDate: '',
    endDate: '',
    period: 'DAILY'
  });

  // Payment Method Report State
  const [paymentLoading, setPaymentLoading] = useState(false);
  const [paymentFilters, setPaymentFilters] = useState({
    startDate: '',
    endDate: ''
  });

  // Refund & Cancellation Report State
  const [refundLoading, setRefundLoading] = useState(false);
  const [refundFilters, setRefundFilters] = useState({
    startDate: '',
    endDate: '',
    period: 'DAILY'
  });

  // Promo Code Report State
  const [promoLoading, setPromoLoading] = useState(false);
  const [promoFilters, setPromoFilters] = useState({
    startDate: '',
    endDate: ''
  });

  // ========== Revenue Report ==========
  const handleDownloadRevenueReport = async () => {
    if (!revenueFilters.startDate || !revenueFilters.endDate) {
      addToast({ message: 'Please select start and end dates', type: 'error' });
      return;
    }

    setRevenueLoading(true);
    try {
      const blob = await reportService.getRevenueReport(
        revenueFilters.startDate,
        revenueFilters.endDate,
        revenueFilters.period
      );
      
      const filename = `revenue_report_${revenueFilters.startDate}_to_${revenueFilters.endDate}_${revenueFilters.period}.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Revenue report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading revenue report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download revenue report', type: 'error' });
    } finally {
      setRevenueLoading(false);
    }
  };

  // ========== Payment Method Report ==========
  const handleDownloadPaymentMethodReport = async () => {
    if (!paymentFilters.startDate || !paymentFilters.endDate) {
      addToast({ message: 'Please select start and end dates', type: 'error' });
      return;
    }

    setPaymentLoading(true);
    try {
      const blob = await reportService.getPaymentMethodReport(
        paymentFilters.startDate,
        paymentFilters.endDate
      );
      
      const filename = `payment_method_report_${paymentFilters.startDate}_to_${paymentFilters.endDate}.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Payment method report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading payment method report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download payment method report', type: 'error' });
    } finally {
      setPaymentLoading(false);
    }
  };

  // ========== Refund & Cancellation Report ==========
  const handleDownloadRefundReport = async () => {
    if (!refundFilters.startDate || !refundFilters.endDate) {
      addToast({ message: 'Please select start and end dates', type: 'error' });
      return;
    }

    setRefundLoading(true);
    try {
      const blob = await reportService.getRefundCancellationReport(
        refundFilters.startDate,
        refundFilters.endDate,
        refundFilters.period
      );
      
      const filename = `refund_cancellation_report_${refundFilters.startDate}_to_${refundFilters.endDate}_${refundFilters.period}.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Refund & cancellation report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading refund report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download refund report', type: 'error' });
    } finally {
      setRefundLoading(false);
    }
  };

  // ========== Promo Code Report ==========
  const handleDownloadPromoCodeReport = async () => {
    if (!promoFilters.startDate || !promoFilters.endDate) {
      addToast({ message: 'Please select start and end dates', type: 'error' });
      return;
    }

    setPromoLoading(true);
    try {
      const blob = await reportService.getPromoCodeUsageReport(
        promoFilters.startDate,
        promoFilters.endDate
      );
      
      const filename = `promo_code_usage_report_${promoFilters.startDate}_to_${promoFilters.endDate}.xlsx`;
      reportService.downloadFile(blob, filename);
      addToast({ message: 'Promo code usage report downloaded successfully', type: 'success' });
    } catch (error) {
      console.error('Error downloading promo code report:', error);
      addToast({ message: error.response?.data?.message || 'Failed to download promo code report', type: 'error' });
    } finally {
      setPromoLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Revenue Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-emerald-500 flex items-center justify-center text-white">
              <FileText className="w-5 h-5" />
            </div>
            Revenue Report
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Generate detailed revenue reports by date range and period (Daily, Weekly, Monthly)
          </p>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Start Date
              </label>
              <DatePicker
                value={revenueFilters.startDate}
                onChange={(value) => setRevenueFilters({ ...revenueFilters, startDate: value })}
                placeholder="Select Start Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                End Date
              </label>
              <DatePicker
                value={revenueFilters.endDate}
                onChange={(value) => setRevenueFilters({ ...revenueFilters, endDate: value })}
                placeholder="Select End Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Period
              </label>
              <Select
                value={revenueFilters.period}
                onValueChange={(value) => setRevenueFilters({ ...revenueFilters, period: value })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="DAILY">Daily</SelectItem>
                  <SelectItem value="WEEKLY">Weekly</SelectItem>
                  <SelectItem value="MONTHLY">Monthly</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleDownloadRevenueReport}
                disabled={revenueLoading}
                className="w-full gap-2"
              >
                <Download className="w-4 h-4" />
                {revenueLoading ? 'Generating...' : 'Download Excel'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Payment Method Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-blue-500 flex items-center justify-center text-white">
              <CreditCard className="w-5 h-5" />
            </div>
            Payment Method Report
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Analyze payment methods used by customers during a specific period
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Start Date
              </label>
              <DatePicker
                value={paymentFilters.startDate}
                onChange={(value) => setPaymentFilters({ ...paymentFilters, startDate: value })}
                placeholder="Select Start Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                End Date
              </label>
              <DatePicker
                value={paymentFilters.endDate}
                onChange={(value) => setPaymentFilters({ ...paymentFilters, endDate: value })}
                placeholder="Select End Date"
              />
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleDownloadPaymentMethodReport}
                disabled={paymentLoading}
                className="w-full gap-2"
              >
                <Download className="w-4 h-4" />
                {paymentLoading ? 'Generating...' : 'Download Excel'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Refund & Cancellation Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-orange-500 flex items-center justify-center text-white">
              <RefreshCw className="w-5 h-5" />
            </div>
            Refund & Cancellation Report
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Track refunds and cancellations with detailed breakdown by period
          </p>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Start Date
              </label>
              <DatePicker
                value={refundFilters.startDate}
                onChange={(value) => setRefundFilters({ ...refundFilters, startDate: value })}
                placeholder="Select Start Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                End Date
              </label>
              <DatePicker
                value={refundFilters.endDate}
                onChange={(value) => setRefundFilters({ ...refundFilters, endDate: value })}
                placeholder="Select End Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Period
              </label>
              <Select
                value={refundFilters.period}
                onValueChange={(value) => setRefundFilters({ ...refundFilters, period: value })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="DAILY">Daily</SelectItem>
                  <SelectItem value="WEEKLY">Weekly</SelectItem>
                  <SelectItem value="MONTHLY">Monthly</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleDownloadRefundReport}
                disabled={refundLoading}
                className="w-full gap-2"
              >
                <Download className="w-4 h-4" />
                {refundLoading ? 'Generating...' : 'Download Excel'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Promo Code Usage Report */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-lg bg-purple-500 flex items-center justify-center text-white">
              <Tag className="w-5 h-5" />
            </div>
            Promo Code Usage Report
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Monitor promo code usage and effectiveness during campaigns
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                Start Date
              </label>
              <DatePicker
                value={promoFilters.startDate}
                onChange={(value) => setPromoFilters({ ...promoFilters, startDate: value })}
                placeholder="Select Start Date"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 block">
                End Date
              </label>
              <DatePicker
                value={promoFilters.endDate}
                onChange={(value) => setPromoFilters({ ...promoFilters, endDate: value })}
                placeholder="Select End Date"
              />
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleDownloadPromoCodeReport}
                disabled={promoLoading}
                className="w-full gap-2"
              >
                <Download className="w-4 h-4" />
                {promoLoading ? 'Generating...' : 'Download Excel'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default FinancialReportsTab;
