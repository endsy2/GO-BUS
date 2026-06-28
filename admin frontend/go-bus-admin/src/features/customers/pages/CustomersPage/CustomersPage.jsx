import React, { useState, useEffect } from 'react';
import { Plus, X, Users, Mail, Phone, Calendar, Eye, Copy, AlertCircle, Power, Filter, ChevronUp, ChevronDown } from 'lucide-react';
import { Button } from 'shared/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from 'shared/components/ui/card';
import { Badge } from 'shared/components/common/Badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from 'shared/components/ui/table';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from 'shared/components/ui/dialog';
import { useToast } from 'shared/components/ui/toast';
import EditCustomerDialog from '../../components/EditCustomerDialog/EditCustomerDialog';
import { Pagination } from 'shared/components/feedback/Pagination';
import CustomerDetailPage from '../CustomerDetailPage/CustomerDetailPage';
import CreateCustomerPage from '../CreateCustomerPage/CreateCustomerPage';
import customerService from '../../services/customerService';
import adminService from 'features/admin/services/adminService';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import { canViewCustomers, canEditCustomers, canDeleteCustomers, canCreateCustomers } from 'shared/utils/permissions';
import useAuth from 'shared/hooks/useAuth';

// ── Shared filter field styling (matches BookingFilters) ───────────────────────
const inputClass =
  'w-full px-3 py-2 border border-slate-300 dark:border-slate-700 rounded-lg bg-white dark:bg-slate-800 text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm';

const labelClass =
  'block text-xs font-medium text-slate-500 dark:text-slate-400 mb-1.5 uppercase tracking-wide';

const sectionTitleClass =
  'text-xs font-semibold text-slate-400 dark:text-slate-500 uppercase tracking-wider mb-2.5';

const CustomersPage = () => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const { addToast } = useToast();
  const currentUser = useAuth();

  const canView = canViewCustomers(currentUser);
  const canEdit = canEditCustomers(currentUser);
  const canDelete = canDeleteCustomers(currentUser);
  const canCreate = canCreateCustomers(currentUser);

  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showStatusDialog, setShowStatusDialog] = useState(false);
  const [customerToToggle, setCustomerToToggle] = useState(null);
  const [showEditDialog, setShowEditDialog] = useState(false);
  const [customerToEdit, setCustomerToEdit] = useState(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [showDetailView, setShowDetailView] = useState(false);
  const [selectedCustomerId, setSelectedCustomerId] = useState(null);
  const [filters, setFilters] = useState({
    email: '',
    phone: '',
    username: '',
    isActive: 'all',
    isDeleted: 'all'
  });
  const [showFilters, setShowFilters] = useState(true);
  const activeFilterCount =
    [filters.username, filters.email, filters.phone].filter((v) => v.trim() !== '').length +
    (filters.isActive !== 'all' ? 1 : 0) +
    (filters.isDeleted !== 'all' ? 1 : 0);
  // currentPage is 0-based (matches <Pagination> convention).
  // The service expects 1-based pageStart, so we add +1 at the call site.
  const [pagination, setPagination] = useState({
    currentPage: 0,
    pageSize: 15,
    totalPages: 0,
    totalElements: 0
  });

  useEffect(() => {
    fetchCustomers();
  }, []);

  // Real-time filter effect with debouncing
  useEffect(() => {
    const hasFilters =
      filters.email.trim() !== '' ||
      filters.phone.trim() !== '' ||
      filters.username.trim() !== '' ||
      filters.isActive !== 'all' ||
      filters.isDeleted !== 'all';

    if (!hasFilters) return;

    const timeoutId = setTimeout(() => {
      // Reset to first page (0-based) when filters change
      fetchCustomers(filters, 0, pagination.pageSize);
    }, 500);

    return () => clearTimeout(timeoutId);
  }, [filters]);

  // page is 0-based; the service expects 1-based pageStart, so we add +1 here.
  const fetchCustomers = async (filterParams = null, page = 0, size = 15) => {
    try {
      setLoading(true);

      const params = { pageStart: page + 1, pageSize: size, isEmployee: false };

      if (filterParams) {
        Object.entries(filterParams).forEach(([key, value]) => {
          if (value && value.toString().trim() !== '' && value !== 'all') {
            if (key === 'userId') {
              const numValue = parseInt(value, 10);
              if (!isNaN(numValue)) params[key] = numValue;
            } else {
              params[key] = value.toString().trim();
            }
          }
        });
      }

      const result = await customerService.getBySpecification(params);
      const pageData = result.data || {};
      setCustomers(pageData.content || []);
      setPagination({
        currentPage: page,                      // store as 0-based
        pageSize: pageData.size || size,
        totalPages: pageData.totalPages || 1,
        totalElements: pageData.totalElements || 0,
      });
      setError('');
    } catch (err) {
      setError(err.response?.data?.message || 'Network error. Please check your connection.');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleClearFilters = () => {
    setFilters({
      email: '',
      phone: '',
      username: '',
      isActive: 'all',
      isDeleted: 'all'
    });
    fetchCustomers(null, 0, pagination.pageSize);
  };

  // newPage arrives 0-based from <Pagination>
  const handlePageChange = (newPage) => {
    fetchCustomers(filters, newPage, pagination.pageSize);
  };

  const handlePageSizeChange = (newSize) => {
    fetchCustomers(filters, 0, newSize);
  };

  const handleCopyToClipboard = (text, label) => {
    navigator.clipboard.writeText(text).then(() => {
      addToast({ message: `${label} ${t('copiedToClipboard')}`, type: 'success' });
    }).catch(() => {
      addToast({ message: t('failedToCopy'), type: 'error' });
    });
  };

  const handleToggleStatusClick = (customer) => {
    if (!canDelete) {
      addToast({ message: 'You do not have permission to change customer status', type: 'error' });
      return;
    }
    setCustomerToToggle(customer);
    setShowStatusDialog(true);
  };

  const handleEditClick = (customer) => {
    if (!canEdit) {
      addToast({ message: 'You do not have permission to edit customers', type: 'error' });
      return;
    }
    setCustomerToEdit(customer);
    setShowEditDialog(true);
  };

  const handleViewClick = (customer) => {
    setSelectedCustomerId(customer.id);
    setShowDetailView(true);
  };

  const handleBackFromDetail = () => {
    setShowDetailView(false);
    setSelectedCustomerId(null);
    // Refresh the customer list when coming back from detail (currentPage is 0-based)
    fetchCustomers(filters, pagination.currentPage, pagination.pageSize);
  };

  const handleSaveEdit = async (updateData) => {
    if (!customerToEdit) return;
    try {
      const result = await customerService.updateCustomer(customerToEdit.id, updateData);
      const updatedCustomer = result.data || result;
      setCustomers(customers.map(c =>
        c.id === customerToEdit.id ? { ...c, ...updatedCustomer } : c
      ));
      setShowEditDialog(false);
      setCustomerToEdit(null);
      setError('');
      addToast({ message: 'Customer updated successfully!', type: 'success' });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update customer');
    }
  };

  const cancelEdit = () => {
    setShowEditDialog(false);
    setCustomerToEdit(null);
  };

  const handleCreateClick = () => {
    if (!canCreate) {
      addToast({ message: 'You do not have permission to create customers', type: 'error' });
      return;
    }
    setShowCreateForm(true);
  };

  const handleCancelCreate = () => {
    setShowCreateForm(false);
  };

  const handleCreateSuccess = () => {
    setShowCreateForm(false);
    addToast({ message: 'Customer created successfully!', type: 'success' });
    // Go back to first page after creation to show the new customer
    fetchCustomers(filters, 0, pagination.pageSize);
  };

  const confirmToggleStatus = async () => {
    if (!customerToToggle) return;
    try {
      const newStatus = !customerToToggle.active;
      const result = await adminService.users.setStatus(customerToToggle.id, { active: newStatus });
      const updatedCustomer = result.data || result;
      setCustomers(customers.map(c =>
        c.id === customerToToggle.id ? { ...c, active: updatedCustomer.active } : c
      ));
      setShowStatusDialog(false);
      setCustomerToToggle(null);
      addToast({
        message: `Customer ${newStatus ? 'activated' : 'deactivated'} successfully`,
        type: 'success',
      });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update customer status');
      setShowStatusDialog(false);
    }
  };

  const cancelToggleStatus = () => {
    setShowStatusDialog(false);
    setCustomerToToggle(null);
  };

  if (!canView) {
    return (
      <div className="flex-1 p-4 sm:p-6 lg:p-8 overflow-y-auto bg-background min-h-screen">
        <Card>
          <CardHeader>
            <CardTitle>{t('customerManagement')}</CardTitle>
            <CardDescription>{t('customerManagementDesc')}</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col items-center py-12">
            <AlertCircle className="h-12 w-12 text-muted-foreground mb-4" />
            <h3 className="text-xl font-semibold mb-2">Access Denied</h3>
            <p className="text-sm text-muted-foreground text-center max-w-md">
              You do not have permission to view customer information. Please contact your administrator.
            </p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="flex-1 p-4 sm:p-6 lg:p-8 overflow-y-auto bg-background min-h-screen">
        <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3 mb-4 sm:mb-6 lg:mb-8">
          <div>
            <h1 className="text-xl sm:text-2xl font-semibold text-foreground mb-1">{t('customerManagement')}</h1>
            <p className="text-sm sm:text-base text-muted-foreground">{t('customerManagementDesc')}</p>
          </div>
          <Button disabled className="w-full sm:w-auto justify-center">
            <Plus className="h-4 w-4 mr-2" />
            {t('addCustomer')}
          </Button>
        </div>

        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-4 sm:p-5 mb-6 sm:mb-8">
          <div className="flex justify-between items-center">
            <h3 className="text-base font-semibold text-slate-900 dark:text-white flex items-center gap-2">
              <Filter className="w-4 h-4 text-blue-500" />
              Filter Customers
            </h3>
          </div>
          <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 sm:gap-4">
            {[1, 2, 3].map(i => (
              <div key={i} className="space-y-2">
                <div className="h-3 w-20 bg-muted animate-pulse rounded" />
                <div className="h-10 bg-muted animate-pulse rounded-lg" />
              </div>
            ))}
          </div>
        </div>

        <Card>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>{t('customerId')}</TableHead>
                <TableHead>{t('name')}</TableHead>
                <TableHead>{t('email')}</TableHead>
                <TableHead>{t('phone')}</TableHead>
                <TableHead>{t('gender')}</TableHead>
                <TableHead>{t('joinedDate')}</TableHead>
                <TableHead>{t('actions')}</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {[1, 2, 3, 4, 5].map(i => (
                <TableRow key={i}>
                  <TableCell><div className="h-4 bg-muted animate-pulse rounded w-16" /></TableCell>
                  <TableCell>
                    <div className="flex items-center gap-3">
                      <div className="w-11 h-11 rounded-xl bg-muted animate-pulse" />
                      <div className="flex-1 space-y-2">
                        <div className="h-4 bg-muted animate-pulse rounded w-32" />
                        <div className="h-3 bg-muted animate-pulse rounded w-24" />
                      </div>
                    </div>
                  </TableCell>
                  <TableCell><div className="h-4 bg-muted animate-pulse rounded w-40" /></TableCell>
                  <TableCell><div className="h-4 bg-muted animate-pulse rounded w-32" /></TableCell>
                  <TableCell><div className="h-6 bg-muted animate-pulse rounded-full w-20" /></TableCell>
                  <TableCell><div className="h-4 bg-muted animate-pulse rounded w-28" /></TableCell>
                  <TableCell>
                    <div className="flex gap-2">
                      <div className="w-9 h-9 bg-muted animate-pulse rounded-lg" />
                      <div className="w-9 h-9 bg-muted animate-pulse rounded-lg" />
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Card>
      </div>
    );
  }

  // Show detail view
  if (showDetailView) {
    return (
      <CustomerDetailPage
        customerId={selectedCustomerId}
        onBack={handleBackFromDetail}
      />
    );
  }

  // Show create form
  if (showCreateForm) {
    return (
      <CreateCustomerPage
        onCancel={handleCancelCreate}
        onSuccess={handleCreateSuccess}
      />
    );
  }

  // Show customer list
  return (
    <div className="flex-1 p-4 sm:p-6 lg:p-8 overflow-y-auto bg-background min-h-screen">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3 mb-4 sm:mb-6 lg:mb-8">
        <div>
          <h1 className="text-xl sm:text-2xl font-semibold text-foreground mb-1">
            {t('customerManagement')}
          </h1>
          <p className="text-sm sm:text-base text-muted-foreground">{t('customerManagementDesc')}</p>
        </div>
        <Button onClick={handleCreateClick} disabled={!canCreate} className="w-full sm:w-auto justify-center">
          <Plus className="h-4 w-4 mr-2" />
          {t('addCustomer')}
        </Button>
      </div>

      {/* Error Message */}
      {error && (
        <Card className="mb-6 border-l-4 border-destructive">
          <CardContent className="flex items-start gap-3 pt-6">
            <AlertCircle className="h-5 w-5 text-destructive flex-shrink-0 mt-0.5" />
            <span className="font-medium text-destructive">{error}</span>
          </CardContent>
        </Card>
      )}

      {/* Filters */}
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-4 sm:p-5 mb-6 sm:mb-8">

        {/* Header row */}
        <div className="flex justify-between items-center">
          <h3 className="text-base font-semibold text-slate-900 dark:text-white flex items-center gap-2">
            <Filter className="w-4 h-4 text-blue-500" />
            Filter Customers
            {activeFilterCount > 0 && (
              <span className="inline-flex items-center justify-center h-5 min-w-[1.25rem] px-1.5 rounded-full bg-blue-500 text-white text-xs font-bold">
                {activeFilterCount}
              </span>
            )}
          </h3>
          <div className="flex items-center gap-2">
            {activeFilterCount > 0 && (
              <Button
                variant="outline"
                size="sm"
                onClick={handleClearFilters}
                className="flex items-center gap-1 text-xs"
              >
                <X className="w-3 h-3" />
                {t('clearFilters')}
              </Button>
            )}
            <Button
              variant="outline"
              size="sm"
              onClick={() => setShowFilters(!showFilters)}
              className="flex items-center gap-1 text-sm"
            >
              {showFilters ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
              {showFilters ? 'Hide' : 'Show'}
            </Button>
          </div>
        </div>

        {showFilters && (
          <div className="mt-4 space-y-5">

            {/* ── Section 1: Customer info ───────────────────────── */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 sm:gap-4">
              <div>
                <label className={labelClass}>{t('username')}</label>
                <input
                  type="text"
                  name="username"
                  value={filters.username}
                  onChange={handleFilterChange}
                  placeholder={t('username')}
                  className={inputClass}
                />
              </div>
              <div>
                <label className={labelClass}>{t('email')}</label>
                <input
                  type="email"
                  name="email"
                  value={filters.email}
                  onChange={handleFilterChange}
                  placeholder={t('email')}
                  className={inputClass}
                />
              </div>
              <div>
                <label className={labelClass}>{t('phone')}</label>
                <input
                  type="tel"
                  name="phone"
                  value={filters.phone}
                  onChange={handleFilterChange}
                  placeholder={t('phone')}
                  className={inputClass}
                />
              </div>
            </div>

            <div className="border-t border-slate-100 dark:border-slate-800" />

            {/* ── Section 2: Status ──────────────────────────────── */}
            <div>
              <p className={sectionTitleClass}>Status</p>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                <div>
                  <label className={labelClass}>Is Active</label>
                  <select
                    name="isActive"
                    value={filters.isActive}
                    onChange={handleFilterChange}
                    className={inputClass}
                  >
                    <option value="all">All</option>
                    <option value="true">Active</option>
                    <option value="false">Inactive</option>
                  </select>
                </div>
                <div>
                  <label className={labelClass}>Is Deleted</label>
                  <select
                    name="isDeleted"
                    value={filters.isDeleted}
                    onChange={handleFilterChange}
                    className={inputClass}
                  >
                    <option value="all">All</option>
                    <option value="false">Not Deleted</option>
                    <option value="true">Deleted</option>
                  </select>
                </div>
              </div>
            </div>

          </div>
        )}
      </div>

      {/* Table Card */}
      <Card className="overflow-hidden">
        <div className="overflow-x-auto">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>{t('customerId')}</TableHead>
              <TableHead>{t('name')}</TableHead>
              <TableHead>{t('email')}</TableHead>
              <TableHead>{t('phone')}</TableHead>
              <TableHead>{t('gender')}</TableHead>
              <TableHead>Is Active</TableHead>
              <TableHead>Is Deleted</TableHead>
              <TableHead>{t('joinedDate')}</TableHead>
              <TableHead>{t('actions')}</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {customers.length === 0 ? (
              <TableRow>
                <TableCell colSpan={9} className="text-center py-20">
                  <div className="flex flex-col items-center gap-4">
                    <div className="bg-muted p-6 rounded-full">
                      <Users className="h-12 w-12 text-muted-foreground" />
                    </div>
                    <p className="text-lg font-semibold">{t('noCustomersFound')}</p>
                    <p className="text-sm text-muted-foreground">Try adjusting your filters or add a new customer</p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              customers.map(customer => (
                <TableRow key={customer.id} className="group">
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-primary text-sm">#{customer.id}</span>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-6 w-6 opacity-0 group-hover:opacity-100"
                        onClick={() => handleCopyToClipboard(customer.id.toString(), 'Customer ID')}
                      >
                        <Copy className="h-3 w-3" />
                      </Button>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-muted flex items-center justify-center text-xs font-medium text-muted-foreground flex-shrink-0">
                        {customer.fullName ? customer.fullName.charAt(0).toUpperCase() : '?'}
                      </div>
                      <div>
                        <div className="font-bold text-sm">{customer.fullName}</div>
                        <div className="text-xs text-muted-foreground font-medium">@{customer.userName}</div>
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2 group">
                      <Mail className="h-3.5 w-3.5 text-muted-foreground flex-shrink-0" />
                      <span className="text-sm">{customer.email}</span>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-6 w-6 opacity-0 group-hover:opacity-100 ml-auto"
                        onClick={() => handleCopyToClipboard(customer.email, 'Email')}
                      >
                        <Copy className="h-3 w-3" />
                      </Button>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2 group">
                      <Phone className="h-3.5 w-3.5 text-muted-foreground flex-shrink-0" />
                      <span className="text-sm">{customer.phone}</span>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-6 w-6 opacity-0 group-hover:opacity-100 ml-auto"
                        onClick={() => handleCopyToClipboard(customer.phone, 'Phone')}
                      >
                        <Copy className="h-3 w-3" />
                      </Button>
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant="secondary">{customer.gender}</Badge>
                  </TableCell>
                  <TableCell>
                    <Badge variant={customer.active ? 'success' : 'default'}>
                      {customer.active ? 'Active' : 'Inactive'}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    {customer.isDeleted ? (
                      <Badge variant="danger">Deleted</Badge>
                    ) : (
                      <span className="text-xs text-muted-foreground">–</span>
                    )}
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <Calendar className="h-3.5 w-3.5" />
                      <span className="font-medium">{formatDate(customer.createdAt)}</span>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex gap-1">
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 text-muted-foreground hover:text-foreground"
                        onClick={() => handleViewClick(customer)}
                      >
                        <Eye className="h-4 w-4" />
                      </Button>
                      {canDelete && (
                        <Button
                          variant="ghost"
                          size="icon"
                          className={`h-8 w-8 ${customer.active
                            ? 'text-destructive hover:text-destructive hover:bg-destructive/10'
                            : 'text-muted-foreground hover:text-foreground'
                          }`}
                          onClick={() => handleToggleStatusClick(customer)}
                          title={customer.active ? t('deactivateCustomer') : t('activateCustomer')}
                        >
                          <Power className="h-4 w-4" />
                        </Button>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
        
        </div>
        {customers.length > 0 && (
          <Pagination
            currentPage={pagination.currentPage}
            totalPages={pagination.totalPages}
            pageSize={pagination.pageSize}
            totalElements={pagination.totalElements}
            onPageChange={handlePageChange}
            onPageSizeChange={handlePageSizeChange}
          />
        )}
      </Card>

      {/* Status Toggle Confirmation Dialog */}
      <Dialog open={showStatusDialog} onOpenChange={setShowStatusDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {customerToToggle?.active ? t('deactivateCustomer') : t('activateCustomer')}
            </DialogTitle>
            <DialogDescription>
              {customerToToggle?.active
                ? t('confirmDeactivateCustomer').replace('{name}', customerToToggle?.fullName || 'this customer')
                : t('confirmActivateCustomer').replace('{name}', customerToToggle?.fullName || 'this customer')
              }
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={cancelToggleStatus}>
              {t('cancel')}
            </Button>
            <Button
              variant={customerToToggle?.active ? "destructive" : "default"}
              onClick={confirmToggleStatus}
            >
              {customerToToggle?.active ? t('deactivate') : t('activate')}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <EditCustomerDialog
        isOpen={showEditDialog}
        customer={customerToEdit}
        onSave={handleSaveEdit}
        onCancel={cancelEdit}
      />
    </div>
  );
};

export default CustomersPage;
