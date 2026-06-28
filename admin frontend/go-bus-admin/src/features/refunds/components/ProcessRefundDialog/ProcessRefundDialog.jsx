import React, { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from 'shared/components/ui/dialog';
import { Button } from 'shared/components/common/Button';
import { Label } from 'shared/components/ui/label';
import { CheckCircle, XCircle } from 'lucide-react';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';

const ProcessRefundDialog = ({ open, onClose, refund, onProcess }) => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;

  const [action, setAction] = useState('approve'); // 'approve' or 'reject'
  const [rejectionReason, setRejectionReason] = useState('');
  const [processing, setProcessing] = useState(false);

  if (!refund) return null;

  const handleSubmit = async () => {
    setProcessing(true);
    try {
      await onProcess(refund.id, action === 'approve', action === 'reject' ? rejectionReason : null);
      onClose();
    } catch (error) {
      console.error('Error processing refund:', error);
    } finally {
      setProcessing(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{t('processRefund') || 'Process Refund'}</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          {/* Refund Info */}
          <div className="bg-muted rounded-lg p-4 space-y-2">
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Refund ID:</span>
              <span className="font-semibold text-foreground">#{refund.id}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Booking ID:</span>
              <span className="font-semibold text-foreground">#{refund.bookingId}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Amount:</span>
              <span className="font-bold text-green-500">${(refund.amount ?? refund.refundAmount)?.toFixed(2) ?? '0.00'}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Reason:</span>
              <span className="text-foreground">{refund.reason || 'N/A'}</span>
            </div>
          </div>

          {/* Action Selection */}
          <div className="space-y-2">
            <Label>{t('action') || 'Action'}</Label>
            <div className="flex gap-3">
              <button
                type="button"
                onClick={() => setAction('approve')}
                className={`flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-lg border-2 transition-all ${
                  action === 'approve'
                    ? 'border-green-500 bg-green-50 dark:bg-green-900/20 text-green-600 dark:text-green-400'
                    : 'border-border text-muted-foreground hover:border-green-300'
                }`}
              >
                <CheckCircle className="w-5 h-5" />
                <span className="font-semibold">{t('approve') || 'Approve'}</span>
              </button>
              <button
                type="button"
                onClick={() => setAction('reject')}
                className={`flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-lg border-2 transition-all ${
                  action === 'reject'
                    ? 'border-destructive bg-red-50 dark:bg-red-900/20 text-destructive'
                    : 'border-border text-muted-foreground hover:border-red-300'
                }`}
              >
                <XCircle className="w-5 h-5" />
                <span className="font-semibold">{t('reject') || 'Reject'}</span>
              </button>
            </div>
          </div>

          {/* Rejection Reason */}
          {action === 'reject' && (
            <div className="space-y-2">
              <Label>{t('rejectionReason') || 'Rejection Reason'} *</Label>
              <textarea
                value={rejectionReason}
                onChange={(e) => setRejectionReason(e.target.value)}
                rows={3}
                placeholder={t('enterRejectionReason') || 'Enter reason for rejection...'}
                className="w-full px-4 py-2 border border-input rounded-lg bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring resize-none"
              />
            </div>
          )}
        </div>

        <DialogFooter className="gap-3">
          <Button
            variant="secondary"
            onClick={onClose}
            disabled={processing}
          >
            {t('cancel') || 'Cancel'}
          </Button>
          <Button
            variant={action === 'approve' ? 'primary' : 'danger'}
            onClick={handleSubmit}
            disabled={processing || (action === 'reject' && !rejectionReason.trim())}
            className={`flex items-center gap-2 ${
              action === 'approve' ? 'bg-green-500 hover:bg-green-600' : ''
            } text-white`}
          >
            {processing ? t('processing') || 'Processing...' : t('confirm') || 'Confirm'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default ProcessRefundDialog;
