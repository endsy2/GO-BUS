import React, { useState } from 'react';
import { Icon } from 'shared/components/common/Icon';
import { Button } from 'shared/components/common/Button';
import { Snackbar } from 'shared/components/common/Snackbar';
import { routeService } from 'features/routes';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';

const CreateRoutePage = ({ onBack, onSuccess }) => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;

  const [submitting, setSubmitting] = useState(false);
  const [snackbar, setSnackbar] = useState({ isOpen: false, message: '', type: 'success' });

  const [formData, setFormData] = useState({
    origin: '',
    destination: '',
    distanceKm: '',
    durationMinutes: '',
    originLat: '',
    originLng: '',
    destLat: '',
    destLng: '',
  });

  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});

  const formatDurationPreview = (minutes) => {
    const m = parseInt(minutes);
    if (!m || isNaN(m) || m <= 0) return null;
    const h = Math.floor(m / 60);
    const rem = m % 60;
    return rem === 0 ? `${h}h` : `${h}h ${rem}m`;
  };

  const validateField = (name, value) => {
    switch (name) {
      case 'origin':
        if (!value.trim()) return t('originRequired') || 'Origin is required';
        if (value.trim().length < 2) return t('originTooShort') || 'At least 2 characters';
        return '';
      case 'destination':
        if (!value.trim()) return t('destinationRequired') || 'Destination is required';
        if (value.trim().length < 2) return t('destinationTooShort') || 'At least 2 characters';
        if (value.trim().toLowerCase() === formData.origin.trim().toLowerCase())
          return t('sameOriginDestination') || 'Origin and destination cannot be the same';
        return '';
      case 'distanceKm':
        if (!value) return t('distanceRequired') || 'Distance is required';
        if (isNaN(value) || parseFloat(value) <= 0) return t('distanceInvalid') || 'Enter a valid positive number';
        if (parseFloat(value) > 10000) return t('distanceTooLarge') || 'Distance seems too large';
        return '';
      case 'durationMinutes':
        if (!value) return t('durationRequired') || 'Duration is required';
        if (isNaN(value) || parseInt(value) <= 0) return t('durationInvalid') || 'Enter a valid positive number';
        if (parseInt(value) > 10080) return t('durationTooLarge') || 'Duration exceeds 1 week';
        return '';
      case 'originLat':
      case 'destLat':
        if (value && (isNaN(value) || parseFloat(value) < -90 || parseFloat(value) > 90))
          return t('latInvalid') || 'Latitude must be between -90 and 90';
        return '';
      case 'originLng':
      case 'destLng':
        if (value && (isNaN(value) || parseFloat(value) < -180 || parseFloat(value) > 180))
          return t('lngInvalid') || 'Longitude must be between -180 and 180';
        return '';
      default:
        return '';
    }
  };

  const validateAll = () => {
    const newErrors = {};
    Object.keys(formData).forEach(key => {
      const err = validateField(key, formData[key]);
      if (err) newErrors[key] = err;
    });
    return newErrors;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (touched[name]) {
      setErrors(prev => ({ ...prev, [name]: validateField(name, value) }));
    }
  };

  const handleBlur = (e) => {
    const { name, value } = e.target;
    setTouched(prev => ({ ...prev, [name]: true }));
    setErrors(prev => ({ ...prev, [name]: validateField(name, value) }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const allTouched = Object.keys(formData).reduce((acc, k) => ({ ...acc, [k]: true }), {});
    setTouched(allTouched);

    const validationErrors = validateAll();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setSubmitting(true);
    try {
      const originLocationValue = formData.originLat && formData.originLng
        ? JSON.stringify({ lat: parseFloat(formData.originLat), lng: parseFloat(formData.originLng) })
        : null;

      const destinationLocationValue = formData.destLat && formData.destLng
        ? JSON.stringify({ lat: parseFloat(formData.destLat), lng: parseFloat(formData.destLng) })
        : null;

      const payload = {
        origin: formData.origin.trim(),
        destination: formData.destination.trim(),
        distanceKm: parseFloat(formData.distanceKm),
        durationMinutes: parseInt(formData.durationMinutes),
        ...(originLocationValue && { originLocation: originLocationValue }),
        ...(destinationLocationValue && { destinationLocation: destinationLocationValue }),
      };

      await routeService.createRoute(payload);
      setSnackbar({ isOpen: true, message: t('routeCreated') || 'Route created successfully!', type: 'success' });
      setTimeout(() => onSuccess?.(), 1200);
    } catch (err) {
      setSnackbar({
        isOpen: true,
        message: err.response?.data?.message || 'Network error. Please check your connection.',
        type: 'error',
      });
    } finally {
      setSubmitting(false);
    }
  };

  const durationPreview = formatDurationPreview(formData.durationMinutes);
  const hasOriginCoords = formData.originLat && formData.originLng;
  const hasDestCoords = formData.destLat && formData.destLng;
  const requiredFields = 4;
  const progress = Math.min(
    Math.round((Object.entries(formData)
      .filter(([k]) => ['origin', 'destination', 'distanceKm', 'durationMinutes'].includes(k))
      .filter(([, v]) => v.trim() !== '').length / requiredFields) * 100),
    100
  );

  return (
    <div className="p-6 bg-gray-50 dark:bg-gray-900 min-h-screen">
      <div className="mb-7">
        <div className="flex flex-col gap-4">
          <Button variant="outline" onClick={onBack} className="flex items-center gap-2 self-start hover:-translate-x-0.5 transition-transform">
            <Icon name="arrow-left" size={18} />
            {t('backToRoutes') || 'Back to Routes'}
          </Button>
          <div>
            <h1 className="flex items-center gap-3 text-3xl font-bold text-gray-900 dark:text-white mb-1.5">
              <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-blue-600">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="16" />
                <line x1="8" y1="12" x2="16" y2="12" />
              </svg>
              {t('createRoute') || 'Create New Route'}
            </h1>
            <p className="text-base text-gray-600 dark:text-gray-400">{t('createRouteDesc') || 'Add a new bus route with origin, destination, and GPS coordinates'}</p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[1fr_380px] gap-6 items-start">
        <div className="flex flex-col gap-0">
          <form onSubmit={handleSubmit} noValidate>
            <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-2xl p-5 mb-5">
              <div className="flex justify-between items-center mb-2.5">
                <span className="text-sm font-semibold text-gray-600 dark:text-gray-400 uppercase tracking-wide">{t('formCompletion') || 'Form Completion'}</span>
                <span className="text-lg font-bold text-blue-600">{progress}%</span>
              </div>
              <div className="w-full h-1.5 bg-gray-100 dark:bg-gray-700 rounded-full overflow-hidden mb-2.5">
                <div className="h-full bg-gradient-to-r from-blue-600 to-cyan-500 rounded-full transition-all duration-400" style={{ width: `${progress}%` }}></div>
              </div>
              <span className="text-xs text-gray-500 dark:text-gray-400">
                {progress === 100
                  ? (t('readyToCreate') || '✓ Ready to create route')
                  : (t('fillRequired') || 'Fill in all required fields to continue')}
              </span>
            </div>

            <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-2xl p-6 mb-5">
              <div className="flex items-start gap-3.5 mb-5">
                <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center text-white flex-shrink-0">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <line x1="3" y1="12" x2="21" y2="12" />
                    <line x1="3" y1="6" x2="21" y2="6" />
                    <line x1="3" y1="18" x2="21" y2="18" />
                  </svg>
                </div>
                <div>
                  <h2 className="text-base font-bold text-gray-900 dark:text-white">{t('routePath') || 'Route Path'}</h2>
                  <p className="text-sm text-gray-600 dark:text-gray-400">{t('routePathDesc') || 'Define the start and end points of this route'}</p>
                </div>
              </div>

              <div className="flex flex-col gap-0">
                <div className={`flex flex-col gap-1.5 ${touched.origin && errors.origin ? 'mb-0' : 'mb-3'}`}>
                  <label className="flex items-center gap-2 text-sm font-semibold text-gray-900 dark:text-white">
                    <span className="w-2.5 h-2.5 rounded-full bg-blue-600 shadow-[0_0_0_3px_rgba(59,130,246,0.2)]"></span>
                    {t('origin') || 'Origin'} <span className="text-red-500 text-base">*</span>
                  </label>
                  <div className="relative">
                    <input
                      type="text"
                      name="origin"
                      value={formData.origin}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      placeholder={t('originPlaceholder') || 'e.g. Phnom Penh'}
                      className={`w-full px-4 py-3 border-2 rounded-lg text-base bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-white transition-all ${touched.origin && errors.origin ? 'border-red-500 focus:ring-red-500/20' : touched.origin && !errors.origin && formData.origin ? 'border-emerald-500 pr-10' : 'border-gray-200 dark:border-gray-700'} focus:outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 focus:bg-white dark:focus:bg-gray-800`}
                      autoComplete="off"
                    />
                    {touched.origin && !errors.origin && formData.origin && (
                      <div className="absolute right-3 top-1/2 -translate-y-1/2 text-emerald-500">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                          <polyline points="20 6 9 17 4 12" />
                        </svg>
                      </div>
                    )}
                  </div>
                  {touched.origin && errors.origin && (
                    <span className="flex items-center gap-1.5 text-xs text-red-600 dark:text-red-400 font-medium">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" />
                      </svg>
                      {errors.origin}
                    </span>
                  )}
                </div>

                <div className="flex flex-col items-start py-1.5 gap-0.5 px-1">
                  <div className="flex flex-col gap-0.5">
                    <span className="w-1 h-1 rounded-full bg-gray-300 dark:bg-gray-600"></span>
                    <span className="w-1 h-1 rounded-full bg-gray-300 dark:bg-gray-600"></span>
                    <span className="w-1 h-1 rounded-full bg-gray-300 dark:bg-gray-600"></span>
                  </div>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-gray-400 ml-0.5">
                    <line x1="12" y1="5" x2="12" y2="19" /><polyline points="19 12 12 19 5 12" />
                  </svg>
                </div>

                <div className={`flex flex-col gap-1.5`}>
                  <label className="flex items-center gap-2 text-sm font-semibold text-gray-900 dark:text-white">
                    <span className="w-2.5 h-2.5 rounded-full bg-emerald-600 shadow-[0_0_0_3px_rgba(16,185,129,0.2)]"></span>
                    {t('destination') || 'Destination'} <span className="text-red-500 text-base">*</span>
                  </label>
                  <div className="relative">
                    <input
                      type="text"
                      name="destination"
                      value={formData.destination}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      placeholder={t('destinationPlaceholder') || 'e.g. Siem Reap'}
                      className={`w-full px-4 py-3 border-2 rounded-lg text-base bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-white transition-all ${touched.destination && errors.destination ? 'border-red-500 focus:ring-red-500/20' : touched.destination && !errors.destination && formData.destination ? 'border-emerald-500 pr-10' : 'border-gray-200 dark:border-gray-700'} focus:outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 focus:bg-white dark:focus:bg-gray-800`}
                      autoComplete="off"
                    />
                    {touched.destination && !errors.destination && formData.destination && (
                      <div className="absolute right-3 top-1/2 -translate-y-1/2 text-emerald-500">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                          <polyline points="20 6 9 17 4 12" />
                        </svg>
                      </div>
                    )}
                  </div>
                  {touched.destination && errors.destination && (
                    <span className="flex items-center gap-1.5 text-xs text-red-600 dark:text-red-400 font-medium">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" />
                      </svg>
                      {errors.destination}
                    </span>
                  )}
                </div>
              </div>
            </div>

            <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-2xl p-6 mb-5">
              <div className="flex items-start gap-3.5 mb-5">
                <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-emerald-500 to-emerald-600 flex items-center justify-center text-white flex-shrink-0">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M3 12h4l3-9 4 18 3-9h4" />
                  </svg>
                </div>
                <div>
                  <h2 className="text-base font-bold text-gray-900 dark:text-white">{t('tripDetails') || 'Trip Details'}</h2>
                  <p className="text-sm text-gray-600 dark:text-gray-400">{t('tripDetailsDesc') || 'Enter distance and travel time for this route'}</p>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-5">
                <div className="flex flex-col gap-1.5">
                  <label className="text-sm font-semibold text-gray-900 dark:text-white">
                    {t('distance') || 'Distance'} <span className="text-red-500 text-base">*</span>
                  </label>
                  <div className="relative">
                    <input
                      type="number"
                      name="distanceKm"
                      value={formData.distanceKm}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      placeholder="e.g. 314"
                      className={`w-full px-4 py-3 pr-12 border-2 rounded-lg text-base bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-white transition-all ${touched.distanceKm && errors.distanceKm ? 'border-red-500 focus:ring-red-500/20' : 'border-gray-200 dark:border-gray-700'} focus:outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 focus:bg-white dark:focus:bg-gray-800`}
                      min="0.1"
                      step="0.1"
                    />
                    <span className="absolute right-3.5 top-1/2 -translate-y-1/2 text-sm font-semibold text-gray-600 dark:text-gray-400 bg-gray-100 dark:bg-gray-700 px-2 py-0.5 rounded pointer-events-none">km</span>
                  </div>
                  {touched.distanceKm && errors.distanceKm && (
                    <span className="flex items-center gap-1.5 text-xs text-red-600 dark:text-red-400 font-medium">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" />
                      </svg>
                      {errors.distanceKm}
                    </span>
                  )}
                </div>

                <div className="flex flex-col gap-1.5">
                  <label className="text-sm font-semibold text-gray-900 dark:text-white">
                    {t('duration') || 'Duration'} <span className="text-red-500 text-base">*</span>
                  </label>
                  <div className="relative">
                    <input
                      type="number"
                      name="durationMinutes"
                      value={formData.durationMinutes}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      placeholder="e.g. 360"
                      className={`w-full px-4 py-3 pr-14 border-2 rounded-lg text-base bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-white transition-all ${touched.durationMinutes && errors.durationMinutes ? 'border-red-500 focus:ring-red-500/20' : 'border-gray-200 dark:border-gray-700'} focus:outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 focus:bg-white dark:focus:bg-gray-800`}
                      min="1"
                    />
                    <span className="absolute right-3.5 top-1/2 -translate-y-1/2 text-sm font-semibold text-gray-600 dark:text-gray-400 bg-gray-100 dark:bg-gray-700 px-2 py-0.5 rounded pointer-events-none">min</span>
                  </div>
                  {touched.durationMinutes && errors.durationMinutes ? (
                    <span className="flex items-center gap-1.5 text-xs text-red-600 dark:text-red-400 font-medium">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" />
                      </svg>
                      {errors.durationMinutes}
                    </span>
                  ) : durationPreview ? (
                    <span className="flex items-center gap-1.5 text-xs text-emerald-600 dark:text-emerald-400 font-semibold">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" />
                      </svg>
                      {durationPreview}
                    </span>
                  ) : null}
                </div>
              </div>
            </div>

            <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-2xl p-6 mb-5">
              <div className="flex items-start gap-3.5 mb-5">
                <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-purple-500 to-purple-600 flex items-center justify-center text-white flex-shrink-0">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" /><circle cx="12" cy="10" r="3" />
                  </svg>
                </div>
                <div>
                  <h2 className="flex items-center gap-2.5 text-base font-bold text-gray-900 dark:text-white">
                    {t('locationCoordinates') || 'GPS Coordinates'}
                    <span className="text-xs font-semibold bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400 px-2 py-0.5 rounded-full uppercase tracking-wide">{t('optional') || 'Optional'}</span>
                  </h2>
                  <p className="text-sm text-gray-600 dark:text-gray-400">{t('coordsDesc') || 'Add coordinates for origin and destination to show on map'}</p>
                </div>
              </div>

              {/* Origin Coordinates */}
              <div className="mb-5">
                <div className="flex items-center gap-2 mb-3">
                  <span className="w-2.5 h-2.5 rounded-full bg-blue-600 shadow-[0_0_0_3px_rgba(59,130,246,0.2)]"></span>
                  <h3 className="text-sm font-bold text-gray-900 dark:text-white">{t('Origin Coordinates') || 'Origin Coordinates'}</h3>
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div className="flex flex-col gap-1.5">
                    <label className="text-sm font-semibold text-gray-900 dark:text-white">Latitude</label>
                    <div className="flex items-center border-2 border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden bg-white dark:bg-gray-800 focus-within:border-blue-500 focus-within:ring-4 focus-within:ring-blue-500/10 transition-all">
                      <span className="px-3 py-3 bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400 text-xs font-bold tracking-wider border-r border-gray-200 dark:border-gray-600 font-mono">LAT</span>
                      <input
                        type="number"
                        name="originLat"
                        value={formData.originLat}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        placeholder="e.g. 11.5564"
                        step="0.0001"
                        className={`flex-1 px-3 py-3 border-0 outline-none bg-transparent text-gray-900 dark:text-white text-sm font-mono min-w-0 ${errors.originLat ? 'text-red-600' : ''}`}
                      />
                    </div>
                    {touched.originLat && errors.originLat && (
                      <span className="flex items-center gap-1.5 text-xs text-red-600 dark:text-red-400 font-medium">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" />
                        </svg>
                        {errors.originLat}
                      </span>
                    )}
                  </div>

                  <div className="flex flex-col gap-1.5">
                    <label className="text-sm font-semibold text-gray-900 dark:text-white">Longitude</label>
                    <div className="flex items-center border-2 border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden bg-white dark:bg-gray-800 focus-within:border-blue-500 focus-within:ring-4 focus-within:ring-blue-500/10 transition-all">
                      <span className="px-3 py-3 bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400 text-xs font-bold tracking-wider border-r border-gray-200 dark:border-gray-600 font-mono">LNG</span>
                      <input
                        type="number"
                        name="originLng"
                        value={formData.originLng}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        placeholder="e.g. 104.9282"
                        step="0.0001"
                        className={`flex-1 px-3 py-3 border-0 outline-none bg-transparent text-gray-900 dark:text-white text-sm font-mono min-w-0 ${errors.originLng ? 'text-red-600' : ''}`}
                      />
                    </div>
                    {touched.originLng && errors.originLng && (
                      <span className="flex items-center gap-1.5 text-xs text-red-600 dark:text-red-400 font-medium">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" />
                        </svg>
                        {errors.originLng}
                      </span>
                    )}
                  </div>
                </div>

                {hasOriginCoords && !errors.originLat && !errors.originLng && (
                  <div className="mt-3 border border-gray-200 dark:border-gray-700 rounded-xl overflow-hidden animate-[fadeIn_0.3s_ease]">
                    <div className="flex items-center gap-1.5 px-3.5 py-2.5 bg-gray-50 dark:bg-gray-900 font-semibold text-sm text-gray-900 dark:text-white border-b border-gray-200 dark:border-gray-700">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <polygon points="3 6 9 3 15 6 21 3 21 18 15 21 9 18 3 21" />
                      </svg>
                      {t('originMapPreview') || 'Origin Map Preview'}
                    </div>
                    <iframe
                      title="Origin Coordinate Preview"
                      className="w-full h-50 border-0"
                      src={`https://www.openstreetmap.org/export/embed.html?bbox=${parseFloat(formData.originLng) - 0.1},${parseFloat(formData.originLat) - 0.08},${parseFloat(formData.originLng) + 0.1},${parseFloat(formData.originLat) + 0.08}&layer=mapnik&marker=${formData.originLat},${formData.originLng}`}
                      loading="lazy"
                    />
                  </div>
                )}
              </div>

              {/* Destination Coordinates */}
              <div>
                <div className="flex items-center gap-2 mb-3">
                  <span className="w-2.5 h-2.5 rounded-full bg-emerald-600 shadow-[0_0_0_3px_rgba(16,185,129,0.2)]"></span>
                  <h3 className="text-sm font-bold text-gray-900 dark:text-white">{t('Destination Coordinates') || 'Destination Coordinates'}</h3>
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div className="flex flex-col gap-1.5">
                    <label className="text-sm font-semibold text-gray-900 dark:text-white">Latitude</label>
                    <div className="flex items-center border-2 border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden bg-white dark:bg-gray-800 focus-within:border-emerald-500 focus-within:ring-4 focus-within:ring-emerald-500/10 transition-all">
                      <span className="px-3 py-3 bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400 text-xs font-bold tracking-wider border-r border-gray-200 dark:border-gray-600 font-mono">LAT</span>
                      <input
                        type="number"
                        name="destLat"
                        value={formData.destLat}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        placeholder="e.g. 13.3671"
                        step="0.0001"
                        className={`flex-1 px-3 py-3 border-0 outline-none bg-transparent text-gray-900 dark:text-white text-sm font-mono min-w-0 ${errors.destLat ? 'text-red-600' : ''}`}
                      />
                    </div>
                    {touched.destLat && errors.destLat && (
                      <span className="flex items-center gap-1.5 text-xs text-red-600 dark:text-red-400 font-medium">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" />
                        </svg>
                        {errors.destLat}
                      </span>
                    )}
                  </div>

                  <div className="flex flex-col gap-1.5">
                    <label className="text-sm font-semibold text-gray-900 dark:text-white">Longitude</label>
                    <div className="flex items-center border-2 border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden bg-white dark:bg-gray-800 focus-within:border-emerald-500 focus-within:ring-4 focus-within:ring-emerald-500/10 transition-all">
                      <span className="px-3 py-3 bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400 text-xs font-bold tracking-wider border-r border-gray-200 dark:border-gray-600 font-mono">LNG</span>
                      <input
                        type="number"
                        name="destLng"
                        value={formData.destLng}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        placeholder="e.g. 103.8448"
                        step="0.0001"
                        className={`flex-1 px-3 py-3 border-0 outline-none bg-transparent text-gray-900 dark:text-white text-sm font-mono min-w-0 ${errors.destLng ? 'text-red-600' : ''}`}
                      />
                    </div>
                    {touched.destLng && errors.destLng && (
                      <span className="flex items-center gap-1.5 text-xs text-red-600 dark:text-red-400 font-medium">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" />
                        </svg>
                        {errors.destLng}
                      </span>
                    )}
                  </div>
                </div>

                {hasDestCoords && !errors.destLat && !errors.destLng && (
                  <div className="mt-3 border border-gray-200 dark:border-gray-700 rounded-xl overflow-hidden animate-[fadeIn_0.3s_ease]">
                    <div className="flex items-center gap-1.5 px-3.5 py-2.5 bg-gray-50 dark:bg-gray-900 font-semibold text-sm text-gray-900 dark:text-white border-b border-gray-200 dark:border-gray-700">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <polygon points="3 6 9 3 15 6 21 3 21 18 15 21 9 18 3 21" />
                      </svg>
                      {t('destinationMapPreview') || 'Destination Map Preview'}
                    </div>
                    <iframe
                      title="Destination Coordinate Preview"
                      className="w-full h-50 border-0"
                      src={`https://www.openstreetmap.org/export/embed.html?bbox=${parseFloat(formData.destLng) - 0.1},${parseFloat(formData.destLat) - 0.08},${parseFloat(formData.destLng) + 0.1},${parseFloat(formData.destLat) + 0.08}&layer=mapnik&marker=${formData.destLat},${formData.destLng}`}
                      loading="lazy"
                    />
                  </div>
                )}
              </div>
            </div>

            <div className="flex justify-end gap-3 pt-1">
              <button
                type="button"
                onClick={onBack}
                disabled={submitting}
                className="inline-flex items-center gap-2 px-7 py-3.5 rounded-xl text-base font-bold transition-all bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {t('cancel') || 'Cancel'}
              </button>
              <button
                type="submit"
                className={`inline-flex items-center gap-2 px-7 py-3.5 rounded-xl text-base font-bold transition-all ${submitting ? 'bg-gray-300 dark:bg-gray-700 text-gray-600 dark:text-gray-400 cursor-not-allowed' : progress === 100 ? 'bg-gradient-to-r from-blue-600 to-cyan-500 text-white hover:from-blue-700 hover:to-cyan-600 hover:-translate-y-0.5 shadow-lg hover:shadow-xl' : 'bg-blue-600 text-white hover:bg-blue-700 hover:-translate-y-0.5 shadow-md hover:shadow-lg'}`}
                disabled={submitting}
              >
                {submitting ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                    {t('creating') || 'Creating...'}
                  </>
                ) : (
                  <>
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" />
                    </svg>
                    {t('createRoute') || 'Create Route'}
                  </>
                )}
              </button>
            </div>
          </form>
        </div>

        <div className="lg:sticky lg:top-6">
          <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 overflow-hidden shadow-sm">
            <div className="flex items-center gap-2 px-5 py-4 bg-gray-50 dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 text-sm font-bold text-gray-600 dark:text-gray-400 uppercase tracking-wide">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" /><circle cx="12" cy="12" r="3" />
              </svg>
              {t('livePreview') || 'Live Preview'}
            </div>

            <div className="p-5 flex flex-col gap-5">
              <div className="flex items-center gap-0 p-4 bg-gray-50 dark:bg-gray-900 rounded-xl border border-gray-200 dark:border-gray-700">
                <div className="flex items-center gap-2.5 flex-shrink-0 max-w-[100px]">
                  <div className="w-2.5 h-2.5 rounded-full bg-blue-600 shadow-[0_0_0_3px_rgba(59,130,246,0.2)] flex-shrink-0"></div>
                  <div className="min-w-0">
                    <div className="text-sm font-bold text-gray-900 dark:text-white overflow-hidden text-ellipsis whitespace-nowrap">
                      {formData.origin || <span className="text-gray-400 dark:text-gray-500 font-normal italic text-xs">{t('origin') || 'Origin'}</span>}
                    </div>
                    <div className="text-[10px] font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wide">{t('origin') || 'Origin'}</div>
                  </div>
                </div>

                <div className="flex-1 flex flex-col items-center gap-2 px-2 min-w-[40px]">
                  <div className="relative w-full h-6 flex items-center">
                    <div className="w-full h-0.5 bg-gradient-to-r from-blue-500 to-emerald-500 rounded-full"></div>
                    <span className="absolute left-1/2 -translate-x-1/2 text-base animate-[busFloat_2.5s_ease-in-out_infinite]">🚌</span>
                  </div>
                </div>

                <div className="flex items-center gap-2.5 flex-shrink-0 max-w-[100px]">
                  <div className="w-2.5 h-2.5 rounded-full bg-emerald-600 shadow-[0_0_0_3px_rgba(16,185,129,0.2)] flex-shrink-0"></div>
                  <div className="min-w-0">
                    <div className="text-sm font-bold text-gray-900 dark:text-white overflow-hidden text-ellipsis whitespace-nowrap">
                      {formData.destination || <span className="text-gray-400 dark:text-gray-500 font-normal italic text-xs">{t('destination') || 'Destination'}</span>}
                    </div>
                    <div className="text-[10px] font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wide">{t('destination') || 'Destination'}</div>
                  </div>
                </div>
              </div>

              <div className="flex flex-col gap-2.5">
                <div className="flex items-center gap-2.5 px-3.5 py-2.5 bg-gray-50 dark:bg-gray-900 rounded-lg border border-gray-200 dark:border-gray-700">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-gray-400 flex-shrink-0">
                    <path d="M3 12h4l3-9 4 18 3-9h4" />
                  </svg>
                  <span className="text-sm text-gray-600 dark:text-gray-400 font-medium flex-1">{t('distance') || 'Distance'}</span>
                  <span className="text-sm font-bold text-gray-900 dark:text-white font-mono">
                    {formData.distanceKm ? `${formData.distanceKm} km` : <span className="text-gray-400 dark:text-gray-500">—</span>}
                  </span>
                </div>
                <div className="flex items-center gap-2.5 px-3.5 py-2.5 bg-gray-50 dark:bg-gray-900 rounded-lg border border-gray-200 dark:border-gray-700">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-gray-400 flex-shrink-0">
                    <circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" />
                  </svg>
                  <span className="text-sm text-gray-600 dark:text-gray-400 font-medium flex-1">{t('duration') || 'Duration'}</span>
                  <span className="text-sm font-bold text-gray-900 dark:text-white font-mono">
                    {durationPreview || <span className="text-gray-400 dark:text-gray-500">—</span>}
                  </span>
                </div>
                <div className="flex items-center gap-2.5 px-3.5 py-2.5 bg-gray-50 dark:bg-gray-900 rounded-lg border border-gray-200 dark:border-gray-700">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-gray-400 flex-shrink-0">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" /><circle cx="12" cy="10" r="3" />
                  </svg>
                  <span className="text-sm text-gray-600 dark:text-gray-400 font-medium flex-1">Origin GPS</span>
                  <span className="text-sm font-bold text-gray-900 dark:text-white font-mono">
                    {hasOriginCoords
                      ? `${parseFloat(formData.originLat).toFixed(4)}, ${parseFloat(formData.originLng).toFixed(4)}`
                      : <span className="text-gray-400 dark:text-gray-500">—</span>}
                  </span>
                </div>
                <div className="flex items-center gap-2.5 px-3.5 py-2.5 bg-gray-50 dark:bg-gray-900 rounded-lg border border-gray-200 dark:border-gray-700">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-gray-400 flex-shrink-0">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" /><circle cx="12" cy="10" r="3" />
                  </svg>
                  <span className="text-sm text-gray-600 dark:text-gray-400 font-medium flex-1">Dest GPS</span>
                  <span className="text-sm font-bold text-gray-900 dark:text-white font-mono">
                    {hasDestCoords
                      ? `${parseFloat(formData.destLat).toFixed(4)}, ${parseFloat(formData.destLng).toFixed(4)}`
                      : <span className="text-gray-400 dark:text-gray-500">—</span>}
                  </span>
                </div>
              </div>

            </div>
          </div>
        </div>
      </div>

      <Snackbar
        isOpen={snackbar.isOpen}
        message={snackbar.message}
        type={snackbar.type}
        onClose={() => setSnackbar(s => ({ ...s, isOpen: false }))}
        duration={3000}
      />

      <style jsx>{`
        @keyframes busFloat {
          0%, 100% { left: 25%; }
          50% { left: 75%; }
        }
        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(8px); }
          to { opacity: 1; transform: translateY(0); }
        }
      `}</style>
    </div>
  );
};

export default CreateRoutePage;
