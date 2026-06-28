import React, { useState, useEffect, useRef } from 'react';
import { Users, Shield, UserCheck, Save, AlertCircle, Trash2 } from 'lucide-react';
import { Button } from 'shared/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from 'shared/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from 'shared/components/ui/table';
import { Badge } from 'shared/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from 'shared/components/ui/tabs';
import { Checkbox } from 'shared/components/ui/checkbox';
import { Label } from 'shared/components/ui/label';
import { Skeleton } from 'shared/components/ui/skeleton';
import { useToast } from 'shared/components/ui/toast';
import AssignRoleDialog from '../../components/AssignRoleDialog/AssignRoleDialog';
import CreateTeamMemberPage from '../CreateTeamMemberPage/CreateTeamMemberPage';
import { Pagination } from 'shared/components/feedback/Pagination';
import { ConfirmDialog } from 'shared/components/feedback/ConfirmDialog';
import adminService from 'features/admin/services/adminService';
import userService from '../../services/teamService';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';

const TeamPage = () => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const { addToast } = useToast();

  const [activeTab, setActiveTab] = useState('members');

  // Members state
  const [teamMembers, setTeamMembers] = useState([]);
  const [membersLoading, setMembersLoading] = useState(true);
  // currentPage is 0-based (matches <Pagination> convention).
  // The service expects 1-based pageStart, so we add +1 at the call site.
  const [membersPagination, setMembersPagination] = useState({
    currentPage: 0,
    pageSize: 15,
    totalPages: 0,
    totalElements: 0
  });

  // Roles state
  const [roles, setRoles] = useState([]);
  const [rolesLoading, setRolesLoading] = useState(true);

  // Permissions state
  const [allPermissions, setAllPermissions] = useState([]);
  const [rolePermissions, setRolePermissions] = useState({});
  const [savingRole, setSavingRole] = useState(null);

  // UI state
  const [error, setError] = useState('');
  const [showAssignRoleDialog, setShowAssignRoleDialog] = useState(false);
  const [showCreateMember, setShowCreateMember] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [deletingUser, setDeletingUser] = useState(false);

  const groupCheckboxRefs = useRef({});

  // ─── Data fetching ────────────────────────────────────────────

  useEffect(() => {
    if (activeTab === 'roles') {
      fetchRoles();
    } else {
      fetchTeamMembers();
    }
  }, [activeTab]);

  useEffect(() => {
    if (roles.length > 0) {
      const initial = {};
      roles.forEach(role => {
        initial[role.id] = new Set((role.permissions || []).map(p => p.id));
      });
      setRolePermissions(initial);
      fetchAllPermissions();
    }
  }, [roles]);

  useEffect(() => {
    const grouped = groupPermissions(allPermissions);
    roles.forEach(role => {
      const checkedSet = rolePermissions[role.id] || new Set();
      Object.entries(grouped).forEach(([groupName, perms]) => {
        const el = groupCheckboxRefs.current[`${role.id}-${groupName}`];
        if (el) {
          const allChecked = perms.every(p => checkedSet.has(p.id));
          const someChecked = perms.some(p => checkedSet.has(p.id));
          
          el.checked = allChecked;
          el.indeterminate = !allChecked && someChecked;
        }
      });
    });
  }, [rolePermissions, allPermissions, roles]);

  // page is 0-based; the service expects 1-based pageStart, so we add +1 here.
  const fetchTeamMembers = async (page = 0, size = 15) => {
    try {
      setMembersLoading(true);
      const result = await userService.getBySpecification({ pageStart: page + 1, pageSize: size, isEmployee: true });
      const pageData = result.data || {};
      setTeamMembers(pageData.content || []);
      setMembersPagination({
        currentPage: page,                      // store as 0-based
        pageSize: pageData.size || size,
        totalPages: pageData.totalPages || 1,
        totalElements: pageData.totalElements || 0,
      });
      setError('');
    } catch (err) {
      setError(err.response?.data?.message || 'Network error. Please check your connection.');
    } finally {
      setMembersLoading(false);
    }
  };

  const fetchRoles = async () => {
    try {
      setRolesLoading(true);
      const result = await adminService.roles.getAll();
      const rolesData = result.data || result;
      setRoles(Array.isArray(rolesData) ? rolesData : []);
      setError('');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch roles');
    } finally {
      setRolesLoading(false);
    }
  };

  const fetchAllPermissions = async () => {
    try {
      const result = await adminService.permissions.getAll();
      const data = result.data || result;
      setAllPermissions(Array.isArray(data) ? data : []);
    } catch (err) {
      // Permissions are supplementary — silently ignore fetch failures
    }
  };

  // ─── Handlers ─────────────────────────────────────────────────

  const formatDate = (dateString) => {
    if (!dateString) return '—';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric', month: 'short', day: 'numeric',
    });
  };

  const handleAssignRole = (user) => {
    setSelectedUser(user);
    setShowAssignRoleDialog(true);
  };

  const handleSaveRoles = async (roles) => {
    if (!selectedUser) return;
    try {
      await adminService.users.assignRoles(selectedUser.id, { roles });
      setShowAssignRoleDialog(false);
      addToast({ message: 'Roles updated successfully!', type: 'success' });
      // Stay on the current page after assigning a role (currentPage is 0-based)
      fetchTeamMembers(membersPagination.currentPage, membersPagination.pageSize);
    } catch (err) {
      addToast({ message: err.response?.data?.message || 'Failed to update roles', type: 'error' });
    }
  };

  // newPage arrives 0-based from <Pagination>
  const handleMembersPageChange = (newPage) => {
    fetchTeamMembers(newPage, membersPagination.pageSize);
  };

  const handleMembersPageSizeChange = (newSize) => {
    fetchTeamMembers(0, newSize);
  };

  const cancelAssignRole = () => {
    setShowAssignRoleDialog(false);
    setSelectedUser(null);
  };

  const handleCreateMemberSuccess = () => {
    setShowCreateMember(false);
    addToast({ message: 'Team member created successfully!', type: 'success' });
    // Go back to first page after creation to show the new member
    fetchTeamMembers(0, membersPagination.pageSize);
  };

  const handleCreateMemberCancel = () => {
    setShowCreateMember(false);
  };

  const handleDeleteUser = (user) => {
    setSelectedUser(user);
    setShowDeleteDialog(true);
  };

  const confirmDeleteUser = async () => {
    if (!selectedUser) return;
    setDeletingUser(true);
    try {
      await adminService.users.delete(selectedUser.id);
      setShowDeleteDialog(false);
      addToast({
        message: `${selectedUser.fullName || selectedUser.userName} has been deleted successfully!`,
        type: 'success',
      });
      // Stay on the current page after deletion if still valid, else go back one
      const pageAfterDelete =
        teamMembers.length === 1 && membersPagination.currentPage > 0
          ? membersPagination.currentPage - 1
          : membersPagination.currentPage;
      fetchTeamMembers(pageAfterDelete, membersPagination.pageSize);
    } catch (err) {
      addToast({ message: err.response?.data?.message || 'Failed to delete team member', type: 'error' });
    } finally {
      setDeletingUser(false);
      setSelectedUser(null);
    }
  };

  const cancelDeleteUser = () => {
    setShowDeleteDialog(false);
    setSelectedUser(null);
  };

  const togglePermission = (roleId, permissionId) => {
    setRolePermissions(prev => {
      const current = new Set(prev[roleId] || []);
      if (current.has(permissionId)) current.delete(permissionId);
      else current.add(permissionId);
      return { ...prev, [roleId]: current };
    });
  };

  const togglePermissionGroup = (roleId, perms, allChecked) => {
    setRolePermissions(prev => {
      const current = new Set(prev[roleId] || []);
      if (allChecked) perms.forEach(p => current.delete(p.id));
      else perms.forEach(p => current.add(p.id));
      return { ...prev, [roleId]: current };
    });
  };

  const handleSaveRolePermissions = async (role) => {
    setSavingRole(role.id);
    try {
      const selectedIds = rolePermissions[role.id] || new Set();
      // Backend expects a list of permission NAMES under the `permissions` key,
      // but rolePermissions stores permission IDs — map them back to names.
      const permissions = allPermissions
        .filter(p => selectedIds.has(p.id))
        .map(p => p.name);
      await adminService.roles.updatePermissions(role.id, { permissions });
      addToast({
        message: `Permissions for ${role.name.replace('ROLE_', '')} updated!`,
        type: 'success',
      });
      fetchRoles();
    } catch (err) {
      addToast({ message: err.response?.data?.message || 'Failed to update permissions', type: 'error' });
    } finally {
      setSavingRole(null);
    }
  };

  // ─── Helpers ──────────────────────────────────────────────────

  const groupPermissions = (permissions) =>
    permissions.reduce((groups, perm) => {
      const group = perm.name.includes('_') ? perm.name.split('_')[0] : 'OTHER';
      if (!groups[group]) groups[group] = [];
      groups[group].push(perm);
      return groups;
    }, {});

  const formatPermissionLabel = (name) =>
    name.replace(/_/g, ' ').toLowerCase().replace(/^\w/, c => c.toUpperCase());

  const getRoleDisplayName = (role) =>
    typeof role === 'string' ? role.replace('ROLE_', '') : role.name?.replace('ROLE_', '');

  const avatarGradients = [
    'from-blue-500 to-indigo-600',
    'from-emerald-500 to-blue-500',
    'from-amber-500 to-red-500',
    'from-purple-500 to-pink-500',
    'from-cyan-500 to-blue-500',
  ];

  const getAvatarGradient = (name = '') => {
    const code = (name.charCodeAt(0) || 0) % avatarGradients.length;
    return avatarGradients[code];
  };

  // ─── Render: Team Members ──────────────────────────────────────

  const renderTeamMembers = () => {
    if (membersLoading) {
      return (
        <div className="space-y-6">
          <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-2">
            <div className="space-y-2">
              <Skeleton className="h-7 w-48" />
              <Skeleton className="h-4 w-64" />
            </div>
            <Skeleton className="h-6 w-24" />
          </div>

          <Card>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Member</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Phone</TableHead>
                  <TableHead>Role</TableHead>
                  <TableHead>Joined</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {[...Array(5)].map((_, i) => (
                  <TableRow key={i}>
                    <TableCell>
                      <div className="flex items-center gap-3">
                        <Skeleton className="h-11 w-11 rounded-xl" />
                        <div className="space-y-2">
                          <Skeleton className="h-4 w-32" />
                          <Skeleton className="h-3 w-24" />
                        </div>
                      </div>
                    </TableCell>
                    <TableCell><Skeleton className="h-4 w-40" /></TableCell>
                    <TableCell><Skeleton className="h-4 w-32" /></TableCell>
                    <TableCell><Skeleton className="h-6 w-20 rounded-full" /></TableCell>
                    <TableCell><Skeleton className="h-4 w-28" /></TableCell>
                    <TableCell>
                      <div className="flex gap-2">
                        <Skeleton className="h-9 w-9 rounded-lg" />
                        <Skeleton className="h-9 w-9 rounded-lg" />
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

    return (
      <div className="space-y-4 sm:space-y-6">
        <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3">
          <div>
            <h2 className="text-lg sm:text-2xl font-bold text-slate-900 dark:text-slate-100">
              {t('teamMembers') || 'Team Members'}
            </h2>
            <p className="text-xs sm:text-sm text-muted-foreground">
              {t('manageTeamMembersDesc') || 'Manage members and their access levels'}
            </p>
          </div>
          <div className="flex flex-wrap items-center gap-2 sm:gap-3">
            {teamMembers.length > 0 && (
              <Badge variant="secondary" className="gap-1.5">
                <Users className="h-3.5 w-3.5" />
                {teamMembers.length} {teamMembers.length === 1 ? 'member' : 'members'}
              </Badge>
            )}
            <Button
              onClick={() => setShowCreateMember(true)}
              className="gap-2 w-full sm:w-auto justify-center"
            >
              <Users className="h-4 w-4" />
              {t('createTeamMember') || 'Create Team Member'}
            </Button>
          </div>
        </div>

        <Card className="overflow-hidden">
          <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-gradient-to-r from-slate-100 via-slate-50 to-slate-100 dark:from-slate-800 dark:via-slate-700 dark:to-slate-800">
                <TableHead className="font-bold text-xs uppercase">{t('member') || 'Member'}</TableHead>
                <TableHead className="font-bold text-xs uppercase">{t('email') || 'Email'}</TableHead>
                <TableHead className="font-bold text-xs uppercase">{t('phone') || 'Phone'}</TableHead>
                <TableHead className="font-bold text-xs uppercase">{t('role') || 'Role'}</TableHead>
                <TableHead className="font-bold text-xs uppercase">{t('joined') || 'Joined'}</TableHead>
                <TableHead className="font-bold text-xs uppercase">{t('actions') || 'Actions'}</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {teamMembers.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} className="text-center py-20">
                    <div className="flex flex-col items-center gap-4">
                      <div className="bg-muted p-6 rounded-full">
                        <Users className="h-12 w-12 text-muted-foreground" />
                      </div>
                      <p className="text-lg font-semibold">{t('noTeamMembersFound') || 'No team members found'}</p>
                    </div>
                  </TableCell>
                </TableRow>
              ) : (
                teamMembers.map(member => (
                  <TableRow key={member.id}>
                    <TableCell>
                      <div className="flex items-center gap-3">
                        <div className={`w-11 h-11 rounded-xl bg-gradient-to-br ${getAvatarGradient(member.fullName)} text-white flex items-center justify-center font-bold text-base shadow-lg`}>
                          {member.fullName ? member.fullName.charAt(0).toUpperCase() : '?'}
                        </div>
                        <div>
                          <div className="font-bold text-sm">{member.fullName || '—'}</div>
                          <div className="text-xs text-muted-foreground font-medium">@{member.userName}</div>
                        </div>
                      </div>
                    </TableCell>
                    <TableCell className="text-sm">{member.email || '—'}</TableCell>
                    <TableCell className="text-sm">{member.phone || '—'}</TableCell>
                    <TableCell>
                      <div className="flex flex-wrap gap-1.5">
                        {member.roles && member.roles.length > 0 ? (
                          member.roles.map((role, i) => (
                            <Badge key={i} className="bg-gradient-to-r from-blue-500 to-indigo-500 text-white">
                              {getRoleDisplayName(role)}
                            </Badge>
                          ))
                        ) : (
                          <Badge variant="outline" className="text-muted-foreground">
                            {t('noRole') || 'No Role'}
                          </Badge>
                        )}
                      </div>
                    </TableCell>
                    <TableCell className="text-sm text-muted-foreground">{formatDate(member.createdAt)}</TableCell>
                    <TableCell>
                      <div className="flex gap-2">
                        <Button
                          size="icon"
                          variant="outline"
                          onClick={() => handleAssignRole(member)}
                          title="Assign Roles"
                        >
                          <Shield className="h-4 w-4" />
                        </Button>
                        <Button
                          size="icon"
                          variant="outline"
                          onClick={() => handleDeleteUser(member)}
                          title="Delete member"
                          className="hover:bg-destructive hover:text-destructive-foreground"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
          
          </div>
          {teamMembers.length > 0 && (
            <Pagination
              currentPage={membersPagination.currentPage}
              totalPages={membersPagination.totalPages}
              pageSize={membersPagination.pageSize}
              totalElements={membersPagination.totalElements}
              onPageChange={handleMembersPageChange}
              onPageSizeChange={handleMembersPageSizeChange}
            />
          )}
        </Card>
      </div>
    );
  };

  // ─── Render: Roles & Permissions ──────────────────────────────

  const renderRolesAndPermissions = () => {
    if (rolesLoading) {
      return (
        <div className="space-y-6">
          <div className="space-y-2">
            <Skeleton className="h-7 w-56" />
            <Skeleton className="h-4 w-96" />
          </div>

          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {[...Array(3)].map((_, i) => (
              <Card key={i}>
                <CardHeader>
                  <div className="flex items-start gap-3">
                    <Skeleton className="h-10 w-10 rounded-lg" />
                    <div className="flex-1 space-y-2">
                      <Skeleton className="h-5 w-32" />
                      <Skeleton className="h-4 w-48" />
                    </div>
                    <Skeleton className="h-6 w-16" />
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  {[...Array(3)].map((_, j) => (
                    <div key={j} className="space-y-3">
                      <Skeleton className="h-5 w-24" />
                      <div className="space-y-2">
                        {[...Array(4)].map((_, k) => (
                          <Skeleton key={k} className="h-5 w-full" />
                        ))}
                      </div>
                    </div>
                  ))}
                  <Skeleton className="h-10 w-full" />
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      );
    }

    const grouped = groupPermissions(allPermissions);

    return (
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100">
            {t('rolesAndPermissions') || 'Roles & Permissions'}
          </h2>
          <p className="text-sm text-muted-foreground">
            {t('rolesAndPermissionsDesc') || 'Toggle permissions per role, then save to apply changes.'}
          </p>
        </div>

        {roles.length === 0 ? (
          <Card>
            <CardContent className="flex flex-col items-center py-12">
              <Shield className="h-12 w-12 text-muted-foreground mb-4" />
              <p className="text-lg font-semibold">{t('noRolesFound') || 'No roles found'}</p>
            </CardContent>
          </Card>
        ) : (
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {roles.map(role => {
              const checkedSet = rolePermissions[role.id] || new Set();
              const checkedCount = checkedSet.size;

              return (
                <Card key={role.id} className="flex flex-col">
                  <CardHeader>
                    <div className="flex items-start gap-3">
                      <div className="bg-gradient-to-br from-blue-500 to-indigo-600 p-2.5 rounded-lg text-white">
                        <Shield className="h-5 w-5" />
                      </div>
                      <div className="flex-1">
                        <CardTitle className="text-lg">{role.name.replace('ROLE_', '')}</CardTitle>
                        <CardDescription className="text-xs">{role.description || 'No description'}</CardDescription>
                      </div>
                      <Badge variant="secondary" className="text-xs">
                        {checkedCount}/{allPermissions.length}
                      </Badge>
                    </div>
                  </CardHeader>
                  <CardContent className="flex-1 space-y-4">
                    {allPermissions.length === 0 ? (
                      <p className="text-sm text-muted-foreground text-center py-4">
                        {t('noPermissionsAvailable') || 'No permissions available'}
                      </p>
                    ) : (
                      Object.entries(grouped).map(([groupName, perms]) => {
                        const allChecked = perms.every(p => checkedSet.has(p.id));
                        const refKey = `${role.id}-${groupName}`;

                        return (
                          <div key={groupName} className="space-y-3">
                            <div className="flex items-center justify-between border-b pb-2">
                              <Label className="flex items-center gap-2 cursor-pointer font-semibold text-sm">
                                <Checkbox
                                  checked={allChecked}
                                  ref={el => { groupCheckboxRefs.current[refKey] = el; }}
                                  onCheckedChange={() => togglePermissionGroup(role.id, perms, allChecked)}
                                />
                                <span>{groupName}</span>
                              </Label>
                              <span className="text-xs text-muted-foreground">
                                {perms.filter(p => checkedSet.has(p.id)).length}/{perms.length}
                              </span>
                            </div>

                            <div className="space-y-2 pl-6">
                              {perms.map(perm => (
                                <Label key={perm.id} className="flex items-center gap-2 cursor-pointer text-sm font-normal">
                                  <Checkbox
                                    checked={checkedSet.has(perm.id)}
                                    onCheckedChange={() => togglePermission(role.id, perm.id)}
                                  />
                                  <span className="text-muted-foreground">{formatPermissionLabel(perm.name)}</span>
                                </Label>
                              ))}
                            </div>
                          </div>
                        );
                      })
                    )}

                    <Button
                      className="w-full gap-2"
                      onClick={() => handleSaveRolePermissions(role)}
                      disabled={savingRole === role.id}
                    >
                      {savingRole === role.id ? (
                        t('saving') || 'Saving…'
                      ) : (
                        <>
                          <Save className="h-4 w-4" />
                          {t('savePermissions') || 'Save permissions'}
                        </>
                      )}
                    </Button>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        )}
      </div>
    );
  };

  // ─── Main Render ──────────────────────────────────────────────

  // Show create member page if active
  if (showCreateMember) {
    return (
      <CreateTeamMemberPage
        onSuccess={handleCreateMemberSuccess}
        onCancel={handleCreateMemberCancel}
      />
    );
  }

  return (
    <div className="flex-1 p-4 sm:p-6 lg:p-8 overflow-y-auto bg-gradient-to-br from-slate-50 via-blue-50/30 to-slate-50 dark:from-slate-900 dark:via-slate-900 dark:to-slate-900 min-h-screen">
      {/* Header */}
      <div className="mb-4 sm:mb-6 lg:mb-8">
        <h1 className="text-xl sm:text-2xl lg:text-4xl font-bold mb-1 sm:mb-2 bg-gradient-to-r from-slate-900 to-slate-700 dark:from-slate-100 dark:to-slate-300 bg-clip-text text-transparent">
          {t('teamManagement') || 'Team Management'}
        </h1>
        <p className="text-sm sm:text-base text-muted-foreground">
          {t('teamManagementDesc') || 'Manage members, roles, and access permissions'}
        </p>
      </div>

      {/* Error Message */}
      {error && (
        <Card className="mb-6 border-l-4 border-destructive bg-destructive/5">
          <CardContent className="flex items-start gap-3 pt-6">
            <AlertCircle className="h-5 w-5 text-destructive flex-shrink-0 mt-0.5" />
            <span className="font-medium text-destructive">{error}</span>
          </CardContent>
        </Card>
      )}

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
        <TabsList className="grid w-full sm:max-w-md grid-cols-2">
          <TabsTrigger value="members" className="gap-2">
            <UserCheck className="h-4 w-4" />
            {t('teamMembers') || 'Team Members'}
          </TabsTrigger>
          <TabsTrigger value="roles" className="gap-2">
            <Shield className="h-4 w-4" />
            {t('rolesAndPermissions') || 'Roles & Permissions'}
          </TabsTrigger>
        </TabsList>

        <TabsContent value="members" className="mt-6">
          {renderTeamMembers()}
        </TabsContent>

        <TabsContent value="roles" className="mt-6">
          {renderRolesAndPermissions()}
        </TabsContent>
      </Tabs>

      <AssignRoleDialog
        isOpen={showAssignRoleDialog}
        user={selectedUser}
        onSave={handleSaveRoles}
        onCancel={cancelAssignRole}
      />

      <ConfirmDialog
        isOpen={showDeleteDialog}
        title={t('deleteTeamMember') || 'Delete Team Member'}
        message={
          selectedUser 
            ? `${t('confirmDeleteTeamMember') || 'Are you sure you want to delete'} "${selectedUser.fullName || selectedUser.userName}"? ${t('thisActionCannotBeUndone') || 'This action cannot be undone.'}`
            : ''
        }
        onConfirm={confirmDeleteUser}
        onCancel={cancelDeleteUser}
        confirmText={t('delete') || 'Delete'}
        cancelText={t('cancel') || 'Cancel'}
        isDestructive={true}
        loading={deletingUser}
      />
    </div>
  );
};

export default TeamPage;
