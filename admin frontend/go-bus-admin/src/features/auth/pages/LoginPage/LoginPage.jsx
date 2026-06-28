import React, { useState } from 'react';
import { Input } from 'shared/components/common/Input';
import { Button } from 'shared/components/common/Button';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import { AlertCircle, Loader2, Eye, EyeOff, Mail, Lock, Bus } from 'lucide-react';
import authService from '../../services/authService';
import userService from 'features/team/services/teamService';

const LoginPage = ({ onLoginSuccess }) => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [apiError, setApiError] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email is invalid';
    }
    if (!formData.password) {
      newErrors.password = 'Password is required';
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
      // Step 1: Login to get token
      const responseData = await authService.login(formData);
      const data = responseData.data || responseData;

      // Store token so the profile request can attach it via the interceptor
      localStorage.setItem('user', JSON.stringify(data));

      try {
        const profileResult = await userService.getProfile();
        const profileData = profileResult.data || profileResult;
        const userProfile = {
          ...data,
          ...profileData,
          fullName: profileData.fullName || profileData.name || data.fullName || 'User',
          userName: profileData.userName || profileData.username || data.userName || formData.email.split('@')[0],
        };
        localStorage.setItem('user', JSON.stringify(userProfile));
        onLoginSuccess(userProfile);
      } catch {
        // Profile fetch is best-effort; fall back to auth data
        const userProfile = {
          ...data,
          fullName: data.fullName || data.name || 'User',
          userName: data.userName || data.username || formData.email.split('@')[0],
        };
        localStorage.setItem('user', JSON.stringify(userProfile));
        onLoginSuccess(userProfile);
      }
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.response?.data?.data?.message || 'Login failed. Please try again.';
      setApiError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-slate-950 p-5 relative overflow-hidden">
      {/* Animated background elements */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-blue-500/10 rounded-full blur-3xl animate-pulse"></div>
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-purple-500/10 rounded-full blur-3xl animate-pulse delay-1000"></div>
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-indigo-500/5 rounded-full blur-3xl"></div>
      </div>

      <div className="relative bg-slate-900 p-10 rounded-2xl shadow-2xl w-full max-w-md border border-slate-800 backdrop-blur-sm">
        {/* Logo and Title */}
        <div className="text-center mb-8">
          <div className="mb-6 flex justify-center items-center">
            <div className="relative">
              <div className="absolute inset-0 bg-blue-500/20 blur-xl rounded-full"></div>
              <img 
                src="/Blue Minimal Idea Free Education Logo.png" 
                alt="GoBus Admin" 
                className="relative max-w-[200px] max-h-20 w-auto h-auto object-contain drop-shadow-2xl rounded-xl"
              />
            </div>
          </div>
          <h1 className="text-2xl font-bold text-white mb-2">
            {/* {t('busBookingSystem') || 'Go Bus Booking System'} */}
            {'Go Bus Booking System'}
          </h1>
          <p className="text-slate-400 text-sm">
            {t('signInToAccount') || 'Sign in to your account'}
          </p>
        </div>

        <form onSubmit={handleSubmit} className="flex flex-col gap-5">
          {apiError && (
            <div className="bg-red-500/10 text-red-400 p-4 rounded-lg border border-red-500/20 flex items-start gap-3 animate-shake">
              <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5" />
              <span className="text-sm">{apiError}</span>
            </div>
          )}

          {/* Email Input */}
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">
              {t('email') || 'Email'}
            </label>
            <div className="relative">
              <div className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500">
                <Mail className="w-5 h-5" />
              </div>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                placeholder="admin@example.com"
                className={`w-full pl-11 pr-4 py-3 bg-slate-800 border ${
                  errors.email ? 'border-red-500' : 'border-slate-700'
                } rounded-lg text-white placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all`}
                required
              />
            </div>
            {errors.email && (
              <p className="text-red-400 text-xs mt-1 flex items-center gap-1">
                <AlertCircle className="w-3 h-3" />
                {errors.email}
              </p>
            )}
          </div>

          {/* Password Input */}
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">
              {t('password') || 'Password'}
            </label>
            <div className="relative">
              <div className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500">
                <Lock className="w-5 h-5" />
              </div>
              <input
                type={showPassword ? 'text' : 'password'}
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="••••••••"
                className={`w-full pl-11 pr-12 py-3 bg-slate-800 border ${
                  errors.password ? 'border-red-500' : 'border-slate-700'
                } rounded-lg text-white placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all`}
                required
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300 transition-colors"
              >
                {showPassword ? (
                  <EyeOff className="w-5 h-5" />
                ) : (
                  <Eye className="w-5 h-5" />
                )}
              </button>
            </div>
            {errors.password && (
              <p className="text-red-400 text-xs mt-1 flex items-center gap-1">
                <AlertCircle className="w-3 h-3" />
                {errors.password}
              </p>
            )}
          </div>

          {/* Submit Button */}
          <Button
            type="submit"
            disabled={loading}
            className="w-full py-3 bg-blue-500 hover:bg-blue-600 text-white font-semibold rounded-lg flex items-center justify-center gap-2 transition-all duration-200 shadow-lg shadow-blue-500/20 hover:shadow-blue-500/40 disabled:opacity-50 disabled:cursor-not-allowed mt-2"
          >
            {loading ? (
              <>
                <Loader2 className="w-5 h-5 animate-spin" />
                {t('signingIn') || 'Signing in...'}
              </>
            ) : (
              <>
                <Bus className="w-5 h-5" />
                {t('signIn') || 'Sign In'}
              </>
            )}
          </Button>
        </form>

        {/* Footer */}
        <div className="mt-8 text-center">
          <p className="text-slate-500 text-xs">
            © 2026 GoBus. All rights reserved.
          </p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
