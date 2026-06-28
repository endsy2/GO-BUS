import React, { useState, useEffect } from 'react';
import { ArrowLeft, AlertCircle, Shield } from 'lucide-react';
import { Button } from 'shared/components/ui/button';
import { Input } from 'shared/components/ui/input';
import { Label } from 'shared/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from 'shared/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from 'shared/components/ui/select';
import { Badge } from 'shared/components/ui/badge';
import userService from '../../services/teamService';
import adminService from 'features/admin/services/adminService';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';

const CreateTeamMemberPage = ({ onCancel, onSuccess }) => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  
  const [formData, setFormData] = useState({
    userName: '',
    fullName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    gender: 'MALE',
    isEmployee: true
  });
  
  const [roles, setRoles] = useState([]);
  const [selectedRole, setSelectedRole] = useState('');
  const [rolesLoading, setRolesLoading] = useState(true);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [apiError, setApiError] = useState('');

  useEffect(() => {
    fetchRoles();
  }, []);

  const fetchRoles = async () => {
    try {
      setRolesLoading(true);
      const result = await adminService.roles.getAll();
      const rolesData = result.data || result;
      setRoles(Array.isArray(rolesData) ? rolesData : []);
    } catch {
      // Non-critical — role selector will be empty
    } finally {
      setRolesLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
    setApiError('');
  };

  const handleSelectChange = (name, value) => {
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleRoleChange = (value) => {
    setSelectedRole(value);
    if (errors.role) {
      setErrors(prev => ({ ...prev, role: '' }));
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.userName.trim()) newErrors.userName = t('usernameRequired') || 'Username is required';
    if (!formData.fullName.trim()) newErrors.fullName = t('fullNameRequired') || 'Full name is required';
    if (!formData.email.trim()) {
      newErrors.email = t('emailRequired') || 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = t('emailInvalid') || 'Email is invalid';
    }
    if (!formData.phone.trim()) newErrors.phone = t('phoneRequired') || 'Phone is required';
    if (!formData.password) {
      newErrors.password = t('passwordRequired') || 'Password is required';
    } else if (formData.password.length < 6) {
      newErrors.password = t('passwordMinLength') || 'Password must be at least 6 characters';
    }
    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = t('passwordsDoNotMatch') || 'Passwords do not match';
    }
    if (!selectedRole) newErrors.role = t('roleRequired') || 'Please select a role';
    return newErrors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError('');

    const newErrors = validate();
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setLoading(true);

    try {
      const { confirmPassword, ...createData } = formData;
      
      const payload = {
        userName: createData.userName,
        fullName: createData.fullName,
        email: createData.email,
        phone: createData.phone,
        password: createData.password,
        gender: createData.gender,
        isEmployee: true,
      };

      const result = await userService.createUser(payload);
      const userId = (result.data || result)?.id;

      if (userId && selectedRole) {
        try {
          await adminService.users.assignRoles(userId, { roles: [selectedRole] });
        } catch {
          // Role assignment is best-effort; user was created successfully
        }
      }

      if (onSuccess) onSuccess();
    } catch (err) {
      setApiError(err.response?.data?.message || t('failedToCreateTeamMember') || 'Failed to create team member.');
    } finally {
      setLoading(false);
    }
  };

  const getRoleDisplayName = (roleName) => roleName.replace('ROLE_', '');

  const selectedRoleData = roles.find(r => r.name === selectedRole);

  return (
    <div className="flex-1 p-6 md:p-8 overflow-y-auto bg-gradient-to-br from-slate-50 via-blue-50/30 to-slate-50 dark:from-slate-900 dark:via-slate-900 dark:to-slate-900 min-h-screen">
      <Button variant="outline" onClick={() => onCancel?.()} className="mb-6 gap-2">
        <ArrowLeft className="h-4 w-4" />
        {t('backToTeam') || 'Back to Team'}
      </Button>

      <div className="flex justify-center">
        <Card className="w-full max-w-4xl">
          <CardHeader>
            <CardTitle className="text-3xl">
              {t('createNewTeamMember') || 'Create New Team Member'}
            </CardTitle>
            <CardDescription>
              {t('addNewTeamMemberToSystem') || 'Add a new team member to the system'}
            </CardDescription>
          </CardHeader>
          <CardContent>
            {apiError && (
              <Card className="mb-6 border-l-4 border-destructive bg-destructive/5">
                <CardContent className="flex items-start gap-3 pt-6">
                  <AlertCircle className="h-5 w-5 text-destructive flex-shrink-0 mt-0.5" />
                  <span className="font-medium text-destructive">{apiError}</span>
                </CardContent>
              </Card>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Username */}
                <div className="space-y-2">
                  <Label htmlFor="userName">{t('username') || 'Username'} *</Label>
                  <Input
                    id="userName" name="userName" value={formData.userName}
                    onChange={handleChange} placeholder={t('enterUsername') || 'Enter username'}
                    className={errors.userName ? 'border-destructive' : ''}
                  />
                  {errors.userName && <p className="text-sm text-destructive">{errors.userName}</p>}
                </div>

                {/* Full Name */}
                <div className="space-y-2">
                  <Label htmlFor="fullName">{t('fullName') || 'Full Name'} *</Label>
                  <Input
                    id="fullName" name="fullName" value={formData.fullName}
                    onChange={handleChange} placeholder={t('enterFullName') || 'Enter full name'}
                    className={errors.fullName ? 'border-destructive' : ''}
                  />
                  {errors.fullName && <p className="text-sm text-destructive">{errors.fullName}</p>}
                </div>

                {/* Email */}
                <div className="space-y-2">
                  <Label htmlFor="email">{t('email') || 'Email'} *</Label>
                  <Input
                    id="email" type="email" name="email" value={formData.email}
                    onChange={handleChange} placeholder={t('enterEmailAddress') || 'Enter email address'}
                    className={errors.email ? 'border-destructive' : ''}
                  />
                  {errors.email && <p className="text-sm text-destructive">{errors.email}</p>}
                </div>

                {/* Phone */}
                <div className="space-y-2">
                  <Label htmlFor="phone">{t('phone') || 'Phone'} *</Label>
                  <Input
                    id="phone" type="tel" name="phone" value={formData.phone}
                    onChange={handleChange} placeholder={t('enterPhoneNumber') || 'Enter phone number'}
                    className={errors.phone ? 'border-destructive' : ''}
                  />
                  {errors.phone && <p className="text-sm text-destructive">{errors.phone}</p>}
                </div>

                {/* Gender */}
                <div className="space-y-2">
                  <Label>{t('gender') || 'Gender'} *</Label>
                  <Select value={formData.gender} onValueChange={(value) => handleSelectChange('gender', value)}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="MALE">{t('male') || 'Male'}</SelectItem>
                      <SelectItem value="FEMALE">{t('female') || 'Female'}</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                {/* Password */}
                <div className="space-y-2">
                  <Label htmlFor="password">{t('password') || 'Password'} *</Label>
                  <Input
                    id="password" type="password" name="password" value={formData.password}
                    onChange={handleChange} placeholder={t('enterPassword') || 'Enter password'}
                    className={errors.password ? 'border-destructive' : ''}
                  />
                  {errors.password && <p className="text-sm text-destructive">{errors.password}</p>}
                </div>

                {/* Confirm Password */}
                <div className="space-y-2 md:col-span-2">
                  <Label htmlFor="confirmPassword">{t('confirmPassword') || 'Confirm Password'} *</Label>
                  <Input
                    id="confirmPassword" type="password" name="confirmPassword" value={formData.confirmPassword}
                    onChange={handleChange} placeholder={t('confirmPasswordPlaceholder') || 'Confirm password'}
                    className={errors.confirmPassword ? 'border-destructive' : ''}
                  />
                  {errors.confirmPassword && <p className="text-sm text-destructive">{errors.confirmPassword}</p>}
                </div>
              </div>

              {/* Role Assignment */}
              <div className="space-y-4 pt-6 border-t">
                <div className="flex items-center gap-2">
                  <Shield className="h-5 w-5 text-blue-600" />
                  <Label className="text-base font-semibold">
                    {t('assignRole') || 'Assign Role'} *
                  </Label>
                </div>
                <p className="text-sm text-muted-foreground">
                  {t('selectRoleForTeamMember') || 'Select a role for this team member'}
                </p>

                {rolesLoading ? (
                  <div className="text-sm text-muted-foreground">{t('loadingRoles') || 'Loading roles...'}</div>
                ) : roles.length === 0 ? (
                  <div className="text-sm text-muted-foreground">{t('noRolesAvailable') || 'No roles available'}</div>
                ) : (
                  <div className="space-y-2">
                    <Select value={selectedRole} onValueChange={handleRoleChange}>
                      <SelectTrigger className={errors.role ? 'border-destructive' : ''}>
                        <SelectValue placeholder={t('selectRole') || 'Select a role'} />
                      </SelectTrigger>
                      <SelectContent>
                        {roles.map(role => (
                          <SelectItem key={role.id} value={role.name}>
                            {getRoleDisplayName(role.name)}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {errors.role && <p className="text-sm text-destructive">{errors.role}</p>}

                    {selectedRoleData && (
                      <div className="mt-3 p-4 bg-blue-50 dark:bg-blue-950 rounded-lg border border-blue-200 dark:border-blue-800">
                        <div className="space-y-2">
                          <div className="flex items-center gap-2">
                            <Badge className="bg-gradient-to-r from-blue-500 to-indigo-500 text-white">
                              {getRoleDisplayName(selectedRoleData.name)}
                            </Badge>
                            <span className="text-xs text-muted-foreground">
                              {selectedRoleData.permissions?.length || 0} {t('permissions') || 'permissions'}
                            </span>
                          </div>
                          <p className="text-sm text-muted-foreground">
                            {selectedRoleData.description || t('noDescription') || 'No description'}
                          </p>
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>

              {/* Form Actions */}
              <div className="flex gap-3 justify-end pt-5 border-t">
                <Button type="button" variant="outline" onClick={() => onCancel?.()} disabled={loading}>
                  {t('cancel') || 'Cancel'}
                </Button>
                <Button type="submit" disabled={loading}>
                  {loading ? (t('creating') || 'Creating...') : (t('createTeamMember') || 'Create Team Member')}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default CreateTeamMemberPage;