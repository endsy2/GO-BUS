import React, { useState } from 'react';
import { useLayouts } from '../../hooks/useLayouts';
import { Button } from 'shared/components/common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'shared/components/ui/card';
import { Skeleton } from 'shared/components/ui/skeleton';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import { useToast } from 'shared/components/ui/toast';
import { LayoutGrid, Plus, Edit, Trash2, Grid3x3 } from 'lucide-react';
import CreateLayoutDialog from '../../components/CreateLayoutDialog/CreateLayoutDialog';
import EditLayoutDialog from '../../components/EditLayoutDialog/EditLayoutDialog';
import ConfirmDialog from 'shared/components/feedback/ConfirmDialog';
import layoutService from '../../services/layoutService';

const LayoutsPage = () => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const { layouts, loading, refetch } = useLayouts();
  const { addToast } = useToast();
  
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedLayout, setSelectedLayout] = useState(null);

  const handleCreateSuccess = () => {
    setCreateDialogOpen(false);
    refetch();
    addToast({ message: t('layoutCreatedSuccess') || 'Layout created successfully', type: 'success' });
  };

  const handleEditClick = (layout) => {
    setSelectedLayout(layout);
    setEditDialogOpen(true);
  };

  const handleEditSuccess = () => {
    setEditDialogOpen(false);
    setSelectedLayout(null);
    refetch();
    addToast({ message: t('layoutUpdatedSuccess') || 'Layout updated successfully', type: 'success' });
  };

  const handleDeleteClick = (layout) => {
    setSelectedLayout(layout);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    try {
      await layoutService.deleteLayout(selectedLayout.id);
      setDeleteDialogOpen(false);
      setSelectedLayout(null);
      refetch();
      addToast({ message: t('layoutDeletedSuccess') || 'Layout deleted successfully', type: 'success' });
    } catch (error) {
      addToast({ message: error.response?.data?.message || 'Failed to delete layout', type: 'error' });
    }
  };

  if (loading) {
    return (
      <div className="flex-1 p-8 overflow-y-auto bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-900 min-h-screen">
        <div className="flex justify-between items-center mb-8">
          <div>
            <Skeleton className="h-8 w-64 mb-2" />
            <Skeleton className="h-4 w-96" />
          </div>
          <Skeleton className="h-10 w-32" />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
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
    <div className="flex-1 p-8 overflow-y-auto bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-900 min-h-screen">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-100 mb-2 flex items-center gap-3">
            <LayoutGrid className="w-8 h-8 text-primary" />
            {t('layoutsManagement') || 'Layouts Management'}
          </h1>
          <p className="text-slate-600 dark:text-slate-400">
            {t('manageBusSeatLayouts') || 'Manage bus seat layouts and configurations'}
          </p>
        </div>
        <Button 
          variant="primary" 
          onClick={() => setCreateDialogOpen(true)}
          className="flex items-center gap-2"
        >
          <Plus className="w-4 h-4" />
          {t('newLayout') || 'New Layout'}
        </Button>
      </div>

      {layouts.length === 0 ? (
        <Card className="text-center py-12">
          <CardContent>
            <Grid3x3 className="w-16 h-16 mx-auto mb-4 text-slate-400" />
            <h3 className="text-xl font-semibold text-slate-900 dark:text-slate-100 mb-2">
              {t('noLayoutsFound') || 'No layouts found'}
            </h3>
            <p className="text-slate-600 dark:text-slate-400 mb-4">
              {t('createFirstLayout') || 'Create your first bus seat layout to get started'}
            </p>
            <Button variant="primary" onClick={() => setCreateDialogOpen(true)}>
              <Plus className="w-4 h-4 mr-2" />
              {t('createLayout') || 'Create Layout'}
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
          {layouts.map(layout => (
            <Card 
              key={layout.id}
              className="transition-transform duration-300 hover:-translate-y-1 hover:shadow-lg"
            >
              <CardHeader>
                <CardTitle className="flex items-center gap-2 text-primary">
                  <Grid3x3 className="w-5 h-5" />
                  {layout.name}
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="flex justify-between items-center py-2 border-b border-slate-200 dark:border-slate-700">
                  <span className="text-sm text-slate-600 dark:text-slate-400">
                    {t('totalSeats') || 'Total Seats'}:
                  </span>
                  <span className="font-semibold text-slate-900 dark:text-slate-100">
                    {layout.totalSeats || 0}
                  </span>
                </div>
                <div className="flex justify-between items-center py-2">
                  <span className="text-sm text-slate-600 dark:text-slate-400">
                    {t('rows') || 'Rows'}:
                  </span>
                  <span className="font-semibold text-slate-900 dark:text-slate-100">
                    {layout.rows || 0}
                  </span>
                </div>
                <div className="flex justify-between items-center py-2">
                  <span className="text-sm text-slate-600 dark:text-slate-400">
                    {t('columns') || 'Columns'}:
                  </span>
                  <span className="font-semibold text-slate-900 dark:text-slate-100">
                    {layout.columns || 0}
                  </span>
                </div>
                {layout.description && (
                  <p className="text-sm text-slate-600 dark:text-slate-400 pt-2 border-t border-slate-200 dark:border-slate-700">
                    {layout.description}
                  </p>
                )}
                <div className="flex gap-2 pt-3">
                  <Button 
                    variant="secondary" 
                    className="flex-1 flex items-center justify-center gap-2"
                    onClick={() => handleEditClick(layout)}
                  >
                    <Edit className="w-4 h-4" />
                    {t('edit') || 'Edit'}
                  </Button>
                  <Button 
                    variant="danger"
                    className="w-10 h-10 p-0 flex items-center justify-center"
                    onClick={() => handleDeleteClick(layout)}
                  >
                    <Trash2 className="w-4 h-4" />
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <CreateLayoutDialog
        open={createDialogOpen}
        onClose={() => setCreateDialogOpen(false)}
        onSuccess={handleCreateSuccess}
      />

      {selectedLayout && (
        <EditLayoutDialog
          open={editDialogOpen}
          onClose={() => {
            setEditDialogOpen(false);
            setSelectedLayout(null);
          }}
          layout={selectedLayout}
          onSuccess={handleEditSuccess}
        />
      )}

      <ConfirmDialog
        isOpen={deleteDialogOpen}
        onCancel={() => {
          setDeleteDialogOpen(false);
          setSelectedLayout(null);
        }}
        onConfirm={handleDeleteConfirm}
        title={t('deleteLayout') || 'Delete Layout'}
        message={`${t('confirmDeleteLayout') || 'Are you sure you want to delete'} "${selectedLayout?.name}"?`}
      />
    </div>
  );
};

export default LayoutsPage;
