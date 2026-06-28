import React, { useState } from 'react';
import { usePromos } from '../../hooks/usePromos';
import { Card, CardContent, CardHeader, CardTitle } from 'shared/components/ui/card';
import { Badge } from 'shared/components/common/Badge';
import { Button } from 'shared/components/common/Button';
import { Skeleton } from 'shared/components/ui/skeleton';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import { useToast } from 'shared/components/ui/toast';
import {
  Tag,
  Plus,
  Edit,
  Trash2,
  Calendar
} from 'lucide-react';
import CreatePromoDialog from '../../components/CreatePromoDialog/CreatePromoDialog';
import EditPromoDialog from '../../components/EditPromoDialog/EditPromoDialog';
import ConfirmDialog from 'shared/components/feedback/ConfirmDialog';
import promoService from '../../services/promoService';

const PromosPage = () => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const { addToast } = useToast();
  const { promos, loading, refetch } = usePromos();
  
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedPromo, setSelectedPromo] = useState(null);

  const handleCreateSuccess = () => {
    setCreateDialogOpen(false);
    refetch();
  };

  const handleEditClick = (promo) => {
    setSelectedPromo(promo);
    setEditDialogOpen(true);
  };

  const handleEditSuccess = () => {
    setEditDialogOpen(false);
    setSelectedPromo(null);
    refetch();
  };

  const handleDeleteClick = (promo) => {
    setSelectedPromo(promo);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    try {
      await promoService.deletePromo(selectedPromo.id);
      setDeleteDialogOpen(false);
      setSelectedPromo(null);
      refetch();
      addToast({ message: t('promoDeletedSuccess') || 'Promo code deleted successfully', type: 'success' });
    } catch (error) {
      addToast({ message: error.response?.data?.message || 'Failed to delete promo code', type: 'error' });
    }
  };

  const getStatusBadge = (promo) => {
    if (promo.status === 'INACTIVE') {
      return <Badge variant="default">INACTIVE</Badge>;
    }
    
    const now = new Date();
    const validFrom = new Date(promo.validFrom);
    const validTo = promo.validTo ? new Date(promo.validTo) : null;
    
    if (now < validFrom) {
      return <Badge variant="info">UPCOMING</Badge>;
    } else if (validTo && now > validTo) {
      return <Badge variant="default">EXPIRED</Badge>;
    } else if (promo.maxUses && promo.usedCount >= promo.maxUses) {
      return <Badge variant="danger">EXHAUSTED</Badge>;
    } else {
      return <Badge variant="success">ACTIVE</Badge>;
    }
  };

  const getDiscountDisplay = (promo) => {
    if (promo.discountType === 'PERCENTAGE') {
      return `${promo.discountValue}%`;
    } else {
      return `$${promo.discountValue.toFixed(2)}`;
    }
  };

  if (loading) {
    return (
      <div className="flex-1 p-4 sm:p-6 lg:p-8 overflow-y-auto bg-background min-h-screen">
        <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3 mb-4 sm:mb-6 lg:mb-8">
          <div>
            <Skeleton className="h-8 sm:h-10 w-48 sm:w-80 mb-2" />
            <Skeleton className="h-4 sm:h-5 w-64 sm:w-96" />
          </div>
          <Skeleton className="h-10 w-full sm:w-40" />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 sm:gap-5">
          {[1, 2, 3, 4].map(i => (
            <Card key={i}>
              <CardHeader>
                <Skeleton className="h-6 w-32" />
              </CardHeader>
              <CardContent className="space-y-3">
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-4 w-3/4" />
                <div className="flex gap-2 pt-2">
                  <Skeleton className="h-9 flex-1" />
                  <Skeleton className="h-9 w-9" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="flex-1 p-4 sm:p-6 lg:p-8 overflow-y-auto bg-slate-50 dark:bg-slate-950 min-h-screen">
      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3 mb-4 sm:mb-6 lg:mb-8">
        <div>
          <h1 className="text-xl sm:text-2xl font-semibold text-foreground mb-1">
            {t('promoManagement') || 'Promo Code Management'}
          </h1>
          <p className="text-sm text-muted-foreground">
            {t('managePromoCodes') || 'Manage promotional codes and discounts'}
          </p>
        </div>
        <Button
          variant="primary"
          onClick={() => setCreateDialogOpen(true)}
          className="flex items-center justify-center gap-2 w-full sm:w-auto"
        >
          <Plus className="w-4 h-4 sm:w-5 sm:h-5" />
          {t('newPromo') || 'New Promo'}
        </Button>
      </div>

      {promos.length === 0 ? (
        <div className="text-center py-16 bg-card rounded-lg border border-border">
          <div className="bg-muted w-14 h-14 rounded-full flex items-center justify-center mx-auto mb-4">
            <Tag className="w-7 h-7 text-muted-foreground" />
          </div>
          <h3 className="text-base font-semibold text-foreground mb-1">
            {t('noPromosFound') || 'No promo codes found'}
          </h3>
          <p className="text-sm text-muted-foreground mb-6">
            {t('createFirstPromo') || 'Create your first promo code to get started'}
          </p>
          <Button
            variant="primary"
            onClick={() => setCreateDialogOpen(true)}
            className="flex items-center gap-2"
          >
            <Plus className="w-5 h-5" />
            {t('createPromo') || 'Create Promo'}
          </Button>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 sm:gap-5">
          {promos.map(promo => (
            <Card key={promo.id}>
              <CardHeader className="pb-3">
                <div className="flex items-start justify-between gap-2">
                  <CardTitle className="text-base font-semibold text-foreground truncate">
                    {promo.code}
                  </CardTitle>
                  {getStatusBadge(promo)}
                </div>
              </CardHeader>

              <CardContent className="space-y-3">
                {/* Description */}
                {promo.description && (
                  <p className="text-sm text-muted-foreground line-clamp-2">
                    {promo.description}
                  </p>
                )}

                {/* Discount + Usage */}
                <div className="flex items-center justify-between text-sm">
                  <span className="text-muted-foreground">{t('discount') || 'Discount'}</span>
                  <span className="font-semibold text-foreground">{getDiscountDisplay(promo)}</span>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <span className="text-muted-foreground">{t('usage') || 'Usage'}</span>
                  <span className="font-medium text-foreground">{promo.usedCount || 0} / {promo.maxUses || '∞'}</span>
                </div>

                {/* Valid Period */}
                <div className="flex items-center gap-2 text-xs text-muted-foreground pt-2 border-t border-border">
                  <Calendar className="w-3 h-3" />
                  <span>
                    {new Date(promo.validFrom).toLocaleDateString()} – {promo.validTo ? new Date(promo.validTo).toLocaleDateString() : 'No expiration'}
                  </span>
                </div>

                {/* Actions */}
                <div className="flex gap-2 pt-3 border-t border-border">
                  <Button
                    variant="secondary"
                    className="flex-1 flex items-center justify-center gap-2"
                    onClick={() => handleEditClick(promo)}
                  >
                    <Edit className="w-4 h-4" />
                    {t('edit') || 'Edit'}
                  </Button>
                  <Button
                    variant="danger"
                    className="w-11 h-11 p-0 flex items-center justify-center"
                    onClick={() => handleDeleteClick(promo)}
                  >
                    <Trash2 className="w-4 h-4" />
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <CreatePromoDialog
        open={createDialogOpen}
        onClose={() => setCreateDialogOpen(false)}
        onSuccess={handleCreateSuccess}
      />

      {selectedPromo && (
        <EditPromoDialog
          open={editDialogOpen}
          onClose={() => {
            setEditDialogOpen(false);
            setSelectedPromo(null);
          }}
          promo={selectedPromo}
          onSuccess={handleEditSuccess}
        />
      )}

      <ConfirmDialog
        isOpen={deleteDialogOpen}
        onCancel={() => {
          setDeleteDialogOpen(false);
          setSelectedPromo(null);
        }}
        onConfirm={handleDeleteConfirm}
        title={t('deletePromo') || 'Delete Promo Code'}
        message={`${t('confirmDeletePromo') || 'Are you sure you want to delete promo code'} "${selectedPromo?.code}"?`}
        type="danger"
      />
    </div>
  );
};

export default PromosPage;
