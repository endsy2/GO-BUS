import React, { useState, useEffect, useRef } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from 'shared/components/ui/card';
import { Button } from 'shared/components/common/Button';
import { Input } from 'shared/components/common/Input';
import { Badge } from 'shared/components/common/Badge';
import { Skeleton } from 'shared/components/ui/skeleton';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import { useToast } from 'shared/components/ui/toast';
import { User, Mail, Phone, Shield, Calendar, Save, X, Edit, Briefcase, Camera, Trash2 } from 'lucide-react';
import profileService from '../../services/profileService';
import ConfirmDialog from 'shared/components/feedback/ConfirmDialog';

const ProfilePage = () => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const { addToast } = useToast();
  
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [profileImageUrl, setProfileImageUrl] = useState(null);
  const [uploadingImage, setUploadingImage] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const fileInputRef = useRef(null);
  const [formData, setFormData] = useState({
    userName: '',
    fullName: '',
    email: '',
    phone: '',
    gender: '',
  });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    fetchProfile();
    fetchProfileImage();
  }, []);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const response = await profileService.getProfile();
      const userData = response.data || response;
      setProfile(userData);
      setFormData({
        userName: userData.userName || '',
        fullName: userData.fullName || '',
        email: userData.email || '',
        phone: userData.phone || '',
        gender: userData.gender || '',
      });
    } catch (error) {
      addToast({ message: 'Failed to load profile', type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const fetchProfileImage = async () => {
    try {
      const response = await profileService.getProfileImageUrl();
      const imageUrl = response.data?.imageUrl || response.data?.data?.imageUrl;
      if (imageUrl) {
        setProfileImageUrl(imageUrl);
      }
    } catch (error) {
      // Non-fatal: the avatar simply falls back to the initials gradient.
      // Logged so a genuine failure (e.g. expired presigned URL, MinIO down)
      // is diagnosable instead of silently swallowed.
      console.warn('Failed to load profile image URL:', error?.response?.data?.message || error.message);
    }
  };

  const handleImageUpload = async (event) => {
    const file = event.target.files?.[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith('image/')) {
      addToast({ message: 'Please select an image file', type: 'error' });
      return;
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      addToast({ message: 'Image size must be less than 5MB', type: 'error' });
      return;
    }

    try {
      setUploadingImage(true);
      const response = await profileService.uploadProfileImage(file);
      const imageUrl = response.data?.imageUrl || response.data?.data?.imageUrl;
      setProfileImageUrl(imageUrl);
      addToast({ message: 'Profile image uploaded successfully', type: 'success' });
    } catch (error) {
      addToast({ message: error.response?.data?.message || 'Failed to upload image', type: 'error' });
    } finally {
      setUploadingImage(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const handleDeleteImage = async () => {
    try {
      await profileService.deleteProfileImage();
      setProfileImageUrl(null);
      setDeleteDialogOpen(false);
      addToast({ message: 'Profile image deleted successfully', type: 'success' });
    } catch (error) {
      addToast({ message: error.response?.data?.message || 'Failed to delete image', type: 'error' });
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const validateForm = () => {
    const newErrors = {};
    if (!formData.userName?.trim()) {
      newErrors.userName = 'Username is required';
    } else if (formData.userName.length < 3 || formData.userName.length > 30) {
      newErrors.userName = 'Username must be between 3 and 30 characters';
    } else if (!/^[a-zA-Z0-9_]+$/.test(formData.userName)) {
      newErrors.userName = 'Username may only contain letters, numbers, and underscores';
    }
    if (!formData.fullName?.trim()) {
      newErrors.fullName = 'Full name is required';
    }
    if (!formData.email?.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Invalid email format';
    }
    if (!formData.gender) {
      newErrors.gender = 'Gender is required';
    }
    return newErrors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const newErrors = validateForm();
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    try {
      setSaving(true);
      await profileService.updateProfile(profile.id, formData);
      await fetchProfile();
      setIsEditing(false);
      addToast({ message: 'Profile updated successfully', type: 'success' });
    } catch (error) {
      addToast({ message: error.response?.data?.message || 'Failed to update profile', type: 'error' });
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    setFormData({
      userName: profile.userName || '',
      fullName: profile.fullName || '',
      email: profile.email || '',
      phone: profile.phone || '',
      gender: profile.gender || '',
    });
    setErrors({});
    setIsEditing(false);
  };

  const getAvatarGradient = (name) => {
    const gradients = [
      'from-blue-500 to-indigo-600',
      'from-purple-500 to-pink-600',
      'from-green-500 to-teal-600',
      'from-orange-500 to-red-600',
      'from-cyan-500 to-blue-600',
    ];
    const index = name ? name.charCodeAt(0) % gradients.length : 0;
    return gradients[index];
  };

  if (loading) {
    return (
      <div className="flex-1 p-8 overflow-y-auto bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-900 min-h-screen">
        <div className="max-w-5xl mx-auto">
          <Skeleton className="h-10 w-64 mb-2" />
          <Skeleton className="h-5 w-96 mb-8" />
          
          <div className="grid grid-cols-1 gap-6">
            <Skeleton className="h-64" />
            <Skeleton className="h-96" />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex-1 p-8 overflow-y-auto bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-900 min-h-screen">
      <div className="max-w-5xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-slate-900 dark:text-white mb-2 flex items-center gap-3">
            <div className="bg-gradient-to-br from-blue-500 to-indigo-600 p-3 rounded-2xl shadow-lg">
              <User className="w-8 h-8 text-white" />
            </div>
            {t('myProfile') || 'My Profile'}
          </h1>
          <p className="text-slate-600 dark:text-slate-400 text-lg ml-16">
            {t('manageYourPersonalInformation') || 'Manage your personal information and preferences'}
          </p>
        </div>

        {/* Profile Header Card */}
        <Card className="mb-6 bg-gradient-to-br from-white to-blue-50 dark:from-slate-900 dark:to-slate-800 border-slate-200 dark:border-slate-700 shadow-xl overflow-hidden">
          <div className="relative">
            {/* Background Pattern */}
            <div className="absolute inset-0 bg-gradient-to-r from-blue-500/10 to-indigo-500/10 dark:from-blue-500/5 dark:to-indigo-500/5"></div>
            
            <CardContent className="relative pt-8 pb-6">
              <div className="flex flex-col md:flex-row items-center md:items-start gap-6">
                {/* Avatar with Image Upload */}
                <div className="relative group">
                  {profileImageUrl ? (
                    <img
                      src={profileImageUrl}
                      alt="Profile"
                      className="w-32 h-32 rounded-3xl object-cover shadow-2xl ring-4 ring-white dark:ring-slate-800"
                    />
                  ) : (
                    <div className={`w-32 h-32 rounded-3xl bg-gradient-to-br ${getAvatarGradient(profile?.fullName)} text-white flex items-center justify-center font-bold text-5xl shadow-2xl ring-4 ring-white dark:ring-slate-800`}>
                      {profile?.fullName ? profile.fullName.charAt(0).toUpperCase() : '?'}
                    </div>
                  )}
                  
                  {/* Upload/Delete Overlay */}
                  <div className="absolute inset-0 rounded-3xl bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2">
                    <button
                      onClick={() => fileInputRef.current?.click()}
                      disabled={uploadingImage}
                      className="bg-white/90 hover:bg-white text-slate-900 p-2.5 rounded-xl shadow-lg transition-all hover:scale-110"
                      title="Upload Image"
                    >
                      {uploadingImage ? (
                        <div className="w-5 h-5 border-2 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
                      ) : (
                        <Camera className="w-5 h-5" />
                      )}
                    </button>
                    {profileImageUrl && (
                      <button
                        onClick={() => setDeleteDialogOpen(true)}
                        className="bg-red-500/90 hover:bg-red-500 text-white p-2.5 rounded-xl shadow-lg transition-all hover:scale-110"
                        title="Delete Image"
                      >
                        <Trash2 className="w-5 h-5" />
                      </button>
                    )}
                  </div>
                  
                  {/* Hidden File Input */}
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/*"
                    onChange={handleImageUpload}
                    className="hidden"
                  />
                  
                  {profile?.isEmployee && (
                    <div className="absolute -bottom-2 -right-2 bg-gradient-to-br from-blue-500 to-indigo-600 text-white p-2 rounded-xl shadow-lg">
                      <Briefcase className="w-5 h-5" />
                    </div>
                  )}
                </div>

                {/* User Info */}
                <div className="flex-1 text-center md:text-left">
                  <h2 className="text-3xl font-bold text-slate-900 dark:text-white mb-2">
                    {profile?.fullName || 'N/A'}
                  </h2>
                  <p className="text-lg text-slate-600 dark:text-slate-400 mb-4">
                    @{profile?.userName || 'N/A'}
                  </p>
                  
                  {/* Roles */}
                  {profile?.roles && profile.roles.length > 0 && (
                    <div className="flex flex-wrap gap-2 justify-center md:justify-start mb-4">
                      {profile.roles.map((role, i) => (
                        <Badge key={i} className="bg-gradient-to-r from-blue-500 to-indigo-600 text-white px-4 py-1.5 text-sm font-semibold shadow-md">
                          <Shield className="w-3 h-3 mr-1.5" />
                          {role.name}
                        </Badge>
                      ))}
                    </div>
                  )}

                  {/* Meta Info */}
                  <div className="flex flex-wrap gap-4 justify-center md:justify-start text-sm text-slate-600 dark:text-slate-400">
                    <div className="flex items-center gap-2 bg-white/50 dark:bg-slate-800/50 px-3 py-1.5 rounded-lg">
                      <Calendar className="w-4 h-4 text-blue-500" />
                      <span>Joined {profile?.createdAt ? new Date(profile.createdAt).toLocaleDateString() : 'N/A'}</span>
                    </div>
                    {profile?.isEmployee && (
                      <div className="flex items-center gap-2 bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400 px-3 py-1.5 rounded-lg font-semibold">
                        <Briefcase className="w-4 h-4" />
                        <span>Employee</span>
                      </div>
                    )}
                  </div>
                </div>

                {/* Edit Button */}
                {!isEditing && (
                  <Button
                    onClick={() => setIsEditing(true)}
                    className="bg-gradient-to-r from-blue-500 to-indigo-600 hover:from-blue-600 hover:to-indigo-700 text-white shadow-lg px-6 py-2.5 rounded-xl font-semibold"
                  >
                    <Edit className="w-4 h-4 mr-2" />
                    Edit Profile
                  </Button>
                )}
              </div>
            </CardContent>
          </div>
        </Card>

        {/* Profile Information Card */}
        <Card className="bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-700 shadow-xl">
          <CardHeader className="border-b border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800/50">
            <CardTitle className="text-2xl text-slate-900 dark:text-white flex items-center gap-2">
              <div className="bg-gradient-to-br from-blue-500 to-indigo-600 p-2 rounded-lg">
                <User className="w-5 h-5 text-white" />
              </div>
              {t('profileInformation') || 'Profile Information'}
            </CardTitle>
          </CardHeader>
          <CardContent className="p-8">
            {isEditing ? (
              <form onSubmit={handleSubmit} className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <Input
                    label={t('username') || 'Username'}
                    name="userName"
                    value={formData.userName}
                    onChange={handleChange}
                    error={errors.userName}
                    required
                    disabled
                    placeholder="e.g. john_doe"
                    className="bg-slate-100 dark:bg-slate-900 border-slate-300 dark:border-slate-700 text-slate-500 dark:text-slate-400 cursor-not-allowed"
                  />

                  <Input
                    label={t('fullName') || 'Full Name'}
                    name="fullName"
                    value={formData.fullName}
                    onChange={handleChange}
                    error={errors.fullName}
                    required
                    className="bg-white dark:bg-slate-800 border-slate-300 dark:border-slate-700 text-slate-900 dark:text-white"
                  />

                  <Input
                    label={t('email') || 'Email'}
                    name="email"
                    type="email"
                    value={formData.email}
                    onChange={handleChange}
                    error={errors.email}
                    required
                    disabled
                    className="bg-slate-100 dark:bg-slate-900 border-slate-300 dark:border-slate-700 text-slate-500 dark:text-slate-400 cursor-not-allowed"
                  />

                  <Input
                    label={t('phone') || 'Phone'}
                    name="phone"
                    type="tel"
                    value={formData.phone}
                    onChange={handleChange}
                    error={errors.phone}
                    className="bg-white dark:bg-slate-800 border-slate-300 dark:border-slate-700 text-slate-900 dark:text-white"
                  />

                  <div>
                    <label className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2">
                      {t('gender') || 'Gender'}
                      <span className="text-red-500 ml-0.5">*</span>
                    </label>
                    <select
                      name="gender"
                      value={formData.gender}
                      onChange={handleChange}
                      className={`w-full px-4 py-2.5 border rounded-lg bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all ${
                        errors.gender
                          ? 'border-red-500 dark:border-red-500'
                          : 'border-slate-300 dark:border-slate-700'
                      }`}
                    >
                      <option value="">Select Gender</option>
                      <option value="MALE">Male</option>
                      <option value="FEMALE">Female</option>
                      <option value="OTHER">Other</option>
                    </select>
                    {errors.gender && (
                      <p className="mt-1 text-sm text-red-500">{errors.gender}</p>
                    )}
                  </div>
                </div>

                <div className="flex gap-3 pt-6 border-t border-slate-200 dark:border-slate-700">
                  <Button
                    type="submit"
                    disabled={saving}
                    className="flex items-center gap-2 bg-gradient-to-r from-blue-500 to-indigo-600 hover:from-blue-600 hover:to-indigo-700 text-white shadow-lg px-6 py-2.5 rounded-xl font-semibold"
                  >
                    {saving ? (
                      <>
                        <Save className="w-4 h-4 animate-pulse" />
                        {t('saving') || 'Saving...'}
                      </>
                    ) : (
                      <>
                        <Save className="w-4 h-4" />
                        {t('saveChanges') || 'Save Changes'}
                      </>
                    )}
                  </Button>
                  <Button
                    type="button"
                    variant="secondary"
                    onClick={handleCancel}
                    disabled={saving}
                    className="flex items-center gap-2 bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white px-6 py-2.5 rounded-xl font-semibold"
                  >
                    <X className="w-4 h-4" />
                    {t('cancel') || 'Cancel'}
                  </Button>
                </div>
              </form>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                {/* Full Name */}
                <div className="space-y-2">
                  <label className="text-sm font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide flex items-center gap-2">
                    <User className="w-4 h-4" />
                    {t('fullName') || 'Full Name'}
                  </label>
                  <p className="text-xl font-bold text-slate-900 dark:text-white">
                    {profile?.fullName || 'N/A'}
                  </p>
                </div>

                {/* Username */}
                <div className="space-y-2">
                  <label className="text-sm font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide flex items-center gap-2">
                    <User className="w-4 h-4" />
                    {t('username') || 'Username'}
                  </label>
                  <p className="text-xl font-bold text-slate-900 dark:text-white">
                    @{profile?.userName || 'N/A'}
                  </p>
                </div>

                {/* Email */}
                <div className="space-y-2">
                  <label className="text-sm font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide flex items-center gap-2">
                    <Mail className="w-4 h-4" />
                    {t('email') || 'Email'}
                  </label>
                  <p className="text-xl font-bold text-slate-900 dark:text-white break-all">
                    {profile?.email || 'N/A'}
                  </p>
                </div>

                {/* Phone */}
                <div className="space-y-2">
                  <label className="text-sm font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide flex items-center gap-2">
                    <Phone className="w-4 h-4" />
                    {t('phone') || 'Phone'}
                  </label>
                  <p className="text-xl font-bold text-slate-900 dark:text-white">
                    {profile?.phone || 'N/A'}
                  </p>
                </div>

                {/* Gender */}
                <div className="space-y-2">
                  <label className="text-sm font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                    {t('gender') || 'Gender'}
                  </label>
                  <p className="text-xl font-bold text-slate-900 dark:text-white">
                    {profile?.gender || 'N/A'}
                  </p>
                </div>

                {/* Joined Date */}
                <div className="space-y-2">
                  <label className="text-sm font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide flex items-center gap-2">
                    <Calendar className="w-4 h-4" />
                    {t('memberSince') || 'Member Since'}
                  </label>
                  <p className="text-xl font-bold text-slate-900 dark:text-white">
                    {profile?.createdAt ? new Date(profile.createdAt).toLocaleDateString('en-US', { 
                      year: 'numeric', 
                      month: 'long', 
                      day: 'numeric' 
                    }) : 'N/A'}
                  </p>
                </div>
              </div>
            )}

            {/* Google Account Info */}
            {!isEditing && profile?.googleId && (
              <div className="mt-8 pt-6 border-t border-slate-200 dark:border-slate-700">
                <div className="flex items-center gap-3 text-sm text-slate-600 dark:text-slate-400 bg-blue-50 dark:bg-blue-900/20 px-4 py-3 rounded-lg">
                  <div className="bg-white dark:bg-slate-800 p-2 rounded-lg">
                    <Mail className="w-4 h-4 text-blue-500" />
                  </div>
                  <span className="font-medium">
                    {t('linkedWithGoogle') || 'This account is linked with Google'}
                  </span>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Delete Image Confirmation Dialog */}
      <ConfirmDialog
        isOpen={deleteDialogOpen}
        onCancel={() => setDeleteDialogOpen(false)}
        onConfirm={handleDeleteImage}
        title="Delete Profile Image"
        message="Are you sure you want to delete your profile image? This action cannot be undone."
        type="danger"
      />
    </div>
  );
};

export default ProfilePage;
