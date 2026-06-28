import React, { useState } from 'react';
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
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from 'shared/components/ui/select';
import { DateTimePicker } from 'shared/components/ui/datetime-picker';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import { useToast } from 'shared/components/ui/toast';
import { Loader2, Tag } from 'lucide-react';
import promoService from '../../services/promoService';

const CreatePromoDialog = ({ open, onClose, onSuccess }) => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const { addToast } = useToast();
  
  const [formData, setFormData] = useState({
    code: '',
    description: '',
    discountType: 'PERCENTAGE',
    discountValue: '',
    maxUses: '',
    validFrom: '',
    validTo: '',
    status: 'ACTIVE',
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

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

  const validate = () => {
    const newErrors = {};
    if (!formData.code.trim()) {
      newErrors.code = t('codeRequired') || 'Code is required';
    }
    if (!formData.discountValue || formData.discountValue <= 0) {
      newErrors.discountValue = t('discountValueRequired') || 'Discount value must be greater than 0';
    }
    if (formData.discountType === 'PERCENTAGE' && formData.discountValue > 100) {
      newErrors.discountValue = 'Percentage cannot exceed 100';
    }
    if (!formData.validFrom) {
      newErrors.validFrom = t('validFromRequired') || 'Valid from date is required';
    }
    return newErrors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const newErrors = validate();
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setLoading(true);
    try {
      const payload = {
        code: formData.code.trim().toUpperCase(),
        description: formData.description.trim() || null,
        discountType: formData.discountType,
        discountValue: parseFloat(formData.discountValue),
        maxUses: formData.maxUses ? parseInt(formData.maxUses) : null,
        validFrom: formData.validFrom,
        validTo: formData.validTo || null,
        status: formData.status,
      };
      
      await promoService.createPromo(payload);
      setFormData({
        code: '',
        description: '',
        discountType: 'PERCENTAGE',
        discountValue: '',
        maxUses: '',
        validFrom: '',
        validTo: '',
        status: 'ACTIVE',
      });
      setErrors({});
      onSuccess();
      addToast({ message: t('promoCreatedSuccess') || 'Promo code created successfully', type: 'success' });
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Failed to create promo code';
      addToast({ message: errorMessage, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setFormData({
        code: '',
        description: '',
        discountType: 'PERCENTAGE',
        discountValue: '',
        maxUses: '',
        validFrom: '',
        validTo: '',
        status: 'ACTIVE',
      });
      setErrors({});
      onClose();
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Tag className="w-5 h-5 text-blue-500" />
            {t('createPromo') || 'Create Promo Code'}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label={t('promoCode') || 'Promo Code'}
            name="code"
            value={formData.code}
            onChange={handleChange}
            placeholder="SUMMER2024"
            error={errors.code}
            required
          />

          <div className="space-y-2">
            <Label>{t('description') || 'Description'}</Label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder={t('enterDescription') || 'Enter description...'}
              rows={3}
              className="w-full px-3 py-2 border border-input rounded-lg bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring resize-none"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label>{t('discountType') || 'Discount Type'}</Label>
              <Select
                value={formData.discountType}
                onValueChange={(value) => {
                  setFormData(prev => ({ ...prev, discountType: value }));
                  if (errors.discountType) setErrors(prev => ({ ...prev, discountType: '' }));
                }}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="PERCENTAGE">Percentage (%)</SelectItem>
                  <SelectItem value="FIXED_AMOUNT">Fixed Amount ($)</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <Input
              label={t('discountValue') || 'Discount Value'}
              name="discountValue"
              type="number"
              step="0.01"
              min="0"
              value={formData.discountValue}
              onChange={handleChange}
              placeholder={formData.discountType === 'PERCENTAGE' ? '10' : '5.00'}
              error={errors.discountValue}
              required
            />
          </div>

          <Input
            label={t('maxUsage') || 'Max Usage (Total)'}
            name="maxUses"
            type="number"
            min="1"
            value={formData.maxUses}
            onChange={handleChange}
            placeholder="100"
          />

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label>{t('validFrom') || 'Valid From'}</Label>
              <DateTimePicker
                value={formData.validFrom}
                onChange={(value) => setFormData(prev => ({ ...prev, validFrom: value }))}
                placeholder="Select Start Date And Time"
              />
              {errors.validFrom && <p className="text-sm text-destructive mt-1">{errors.validFrom}</p>}
            </div>

            <div className="space-y-2">
              <Label>{t('validUntil') || 'Valid Until (Optional)'}</Label>
              <DateTimePicker
                value={formData.validTo}
                onChange={(value) => setFormData(prev => ({ ...prev, validTo: value }))}
                placeholder="No Expiration"
              />
              {errors.validTo && <p className="text-sm text-destructive mt-1">{errors.validTo}</p>}
            </div>
          </div>

          <DialogFooter className="gap-2">
            <Button
              type="button"
              variant="secondary"
              onClick={handleClose}
              disabled={loading}
            >
              {t('cancel') || 'Cancel'}
            </Button>
            <Button
              type="submit"
              variant="primary"
              disabled={loading}
              className="flex items-center gap-2"
            >
              {loading && <Loader2 className="w-4 h-4 animate-spin" />}
              {loading ? (t('creating') || 'Creating...') : (t('create') || 'Create')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default CreatePromoDialog;
