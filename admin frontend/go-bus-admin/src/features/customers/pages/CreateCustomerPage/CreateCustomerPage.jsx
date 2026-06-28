import React, { useState } from 'react';
import { ArrowLeft, AlertCircle } from 'lucide-react';
import { Button } from 'shared/components/ui/button';
import { Input } from 'shared/components/ui/input';
import { Label } from 'shared/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from 'shared/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from 'shared/components/ui/select';
import customerService from '../../services/customerService';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';

const CreateCustomerPage = ({ onCancel, onSuccess, isEmployee = false }) => {
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
    isEmployee: isEmployee
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [apiError, setApiError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
    setApiError('');
  };

  const handleSelectChange = (name, value) => {
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.userName.trim()) {
      newErrors.userName = t('usernameRequired') || 'Username is required';
    }
    if (!formData.fullName.trim()) {
      newErrors.fullName = t('fullNameRequired') || 'Full name is required';
    }
    if (!formData.email.trim()) {
      newErrors.email = t('emailRequired') || 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = t('emailInvalid') || 'Email is invalid';
    }
    if (!formData.phone.trim()) {
      newErrors.phone = t('phoneRequired') || 'Phone is required';
    }
    if (!formData.password) {
      newErrors.password = t('passwordRequired') || 'Password is required';
    } else if (formData.password.length < 6) {
      newErrors.password = t('passwordMinLength') || 'Password must be at least 6 characters';
    }
    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = t('passwordsDoNotMatch') || 'Passwords do not match';
    }
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
        isEmployee,
      };
      await customerService.createCustomer(payload);
      if (onSuccess) onSuccess();
    } catch (err) {
      setApiError(err.response?.data?.message || t('failedToCreateCustomer') || 'Failed to create customer');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    if (onCancel) {
      onCancel();
    }
  };

  return (
    <div className="flex-1 p-6 md:p-8 overflow-y-auto bg-gradient-to-br from-slate-50 via-blue-50/30 to-slate-50 dark:from-slate-900 dark:via-slate-900 dark:to-slate-900 min-h-screen">
      {/* Back Button */}
      <Button 
        variant="outline" 
        onClick={handleCancel}
        className="mb-6 gap-2"
      >
        <ArrowLeft className="h-4 w-4" />
        {t('backToCustomers') || 'Back to Customers'}
      </Button>

      {/* Form Card */}
      <div className="flex justify-center">
        <Card className="w-full max-w-4xl">
          <CardHeader>
            <CardTitle className="text-3xl">
              {isEmployee 
                ? (t('createNewTeamMember') || 'Create New Team Member')
                : (t('createNewCustomer') || 'Create New Customer')
              }
            </CardTitle>
            <CardDescription>
              {isEmployee
                ? (t('addNewTeamMemberToSystem') || 'Add a new team member to the system')
                : (t('addNewCustomerToSystem') || 'Add a new customer to the system')
              }
            </CardDescription>
          </CardHeader>
          <CardContent>
            {/* Error Message */}
            {apiError && (
              <Card className="mb-6 border-l-4 border-destructive bg-destructive/5">
                <CardContent className="flex items-start gap-3 pt-6">
                  <AlertCircle className="h-5 w-5 text-destructive flex-shrink-0 mt-0.5" />
                  <span className="font-medium text-destructive">{apiError}</span>
                </CardContent>
              </Card>
            )}

            {/* Form */}
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Username */}
                <div className="space-y-2">
                  <Label htmlFor="userName">
                    {t('username') || 'Username'} *
                  </Label>
                  <Input
                    id="userName"
                    name="userName"
                    value={formData.userName}
                    onChange={handleChange}
                    placeholder={t('enterUsername') || 'Enter username'}
                    required
                    className={errors.userName ? 'border-destructive' : ''}
                  />
                  {errors.userName && (
                    <p className="text-sm text-destructive">{errors.userName}</p>
                  )}
                </div>

                {/* Full Name */}
                <div className="space-y-2">
                  <Label htmlFor="fullName">
                    {t('fullName') || 'Full Name'} *
                  </Label>
                  <Input
                    id="fullName"
                    name="fullName"
                    value={formData.fullName}
                    onChange={handleChange}
                    placeholder={t('enterFullName') || 'Enter full name'}
                    required
                    className={errors.fullName ? 'border-destructive' : ''}
                  />
                  {errors.fullName && (
                    <p className="text-sm text-destructive">{errors.fullName}</p>
                  )}
                </div>

                {/* Email */}
                <div className="space-y-2">
                  <Label htmlFor="email">
                    {t('email') || 'Email'} *
                  </Label>
                  <Input
                    id="email"
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder={t('enterEmailAddress') || 'Enter email address'}
                    required
                    className={errors.email ? 'border-destructive' : ''}
                  />
                  {errors.email && (
                    <p className="text-sm text-destructive">{errors.email}</p>
                  )}
                </div>

                {/* Phone */}
                <div className="space-y-2">
                  <Label htmlFor="phone">
                    {t('phone') || 'Phone'} *
                  </Label>
                  <Input
                    id="phone"
                    type="tel"
                    name="phone"
                    value={formData.phone}
                    onChange={handleChange}
                    placeholder={t('enterPhoneNumber') || 'Enter phone number'}
                    required
                    className={errors.phone ? 'border-destructive' : ''}
                  />
                  {errors.phone && (
                    <p className="text-sm text-destructive">{errors.phone}</p>
                  )}
                </div>

                {/* Gender */}
                <div className="space-y-2">
                  <Label htmlFor="gender">
                    {t('gender') || 'Gender'} *
                  </Label>
                  <Select
                    value={formData.gender}
                    onValueChange={(value) => handleSelectChange('gender', value)}
                  >
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
                  <Label htmlFor="password">
                    {t('password') || 'Password'} *
                  </Label>
                  <Input
                    id="password"
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    placeholder={t('enterPassword') || 'Enter password'}
                    required
                    className={errors.password ? 'border-destructive' : ''}
                  />
                  {errors.password && (
                    <p className="text-sm text-destructive">{errors.password}</p>
                  )}
                </div>

                {/* Confirm Password - Full Width */}
                <div className="space-y-2 md:col-span-2">
                  <Label htmlFor="confirmPassword">
                    {t('confirmPassword') || 'Confirm Password'} *
                  </Label>
                  <Input
                    id="confirmPassword"
                    type="password"
                    name="confirmPassword"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    placeholder={t('confirmPasswordPlaceholder') || 'Confirm password'}
                    required
                    className={errors.confirmPassword ? 'border-destructive' : ''}
                  />
                  {errors.confirmPassword && (
                    <p className="text-sm text-destructive">{errors.confirmPassword}</p>
                  )}
                </div>
              </div>

              {/* Form Actions */}
              <div className="flex gap-3 justify-end pt-5 border-t">
                <Button 
                  type="button" 
                  variant="outline" 
                  onClick={handleCancel}
                  disabled={loading}
                >
                  {t('cancel') || 'Cancel'}
                </Button>
                <Button type="submit" disabled={loading}>
                  {loading 
                    ? (t('creating') || 'Creating...') 
                    : isEmployee 
                      ? (t('createTeamMember') || 'Create Team Member')
                      : (t('createCustomer') || 'Create Customer')
                  }
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default CreateCustomerPage;
