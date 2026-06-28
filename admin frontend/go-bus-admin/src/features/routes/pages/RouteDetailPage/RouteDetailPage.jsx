import React, { useState, useEffect } from 'react';
import { Button } from 'shared/components/common/Button';
import { Icon } from 'shared/components/common/Icon';
import { Input } from 'shared/components/common/Input';
import { Snackbar } from 'shared/components/common/Snackbar';
import { ConfirmDialog } from 'shared/components/feedback/ConfirmDialog';
import { routeService } from 'features/routes';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';

const RouteDetailPage = ({ routeId, onBack }) => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;

  const [route, setRoute] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [isEditing, setIsEditing] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
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

  const [formErrors, setFormErrors] = useState({});

  useEffect(() => {
    if (routeId) fetchRouteDetails();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [routeId]);

  const parseLocation = (locationStr) => {
    if (!locationStr) return null;
    try {
      const parsed = typeof locationStr === 'string' ? JSON.parse(locationStr) : locationStr;
      if (parsed?.lat !== undefined && parsed?.lng !== undefined) return parsed;
      return null;
    } catch {
      return null;
    }
  };

  const fetchRouteDetails = async () => {
    try {
      setLoading(true);
      const result = await routeService.getRouteById(routeId);
      const routeData = result.data || result;
      setRoute(routeData);
      const originLoc = parseLocation(routeData.originLocation);
      const destLoc = parseLocation(routeData.destinationLocation);
      setFormData({
        origin: routeData.origin || '',
        destination: routeData.destination || '',
        distanceKm: routeData.distanceKm?.toString() || '',
        durationMinutes: routeData.durationMinutes?.toString() || '',
        originLat: originLoc?.lat?.toString() || '',
        originLng: originLoc?.lng?.toString() || '',
        destLat: destLoc?.lat?.toString() || '',
        destLng: destLoc?.lng?.toString() || '',
      });
      setError('');
    } catch (err) {
      setError(err.response?.data?.message || 'Network error. Please check your connection.');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (formErrors[name]) setFormErrors(prev => ({ ...prev, [name]: '' }));
  };

  const validateForm = () => {
    const errors = {};
    if (!formData.origin.trim()) errors.origin = 'Origin is required';
    if (!formData.destination.trim()) errors.destination = 'Destination is required';
    if (!formData.distanceKm || isNaN(formData.distanceKm) || parseFloat(formData.distanceKm) <= 0)
      errors.distanceKm = 'Valid distance is required';
    if (!formData.durationMinutes || isNaN(formData.durationMinutes) || parseInt(formData.durationMinutes) <= 0)
      errors.durationMinutes = 'Valid duration is required';
    if (formData.originLat && (isNaN(parseFloat(formData.originLat)) || parseFloat(formData.originLat) < -90 || parseFloat(formData.originLat) > 90))
      errors.originLat = 'Latitude must be between -90 and 90';
    if (formData.originLng && (isNaN(parseFloat(formData.originLng)) || parseFloat(formData.originLng) < -180 || parseFloat(formData.originLng) > 180))
      errors.originLng = 'Longitude must be between -180 and 180';
    if (formData.destLat && (isNaN(parseFloat(formData.destLat)) || parseFloat(formData.destLat) < -90 || parseFloat(formData.destLat) > 90))
      errors.destLat = 'Latitude must be between -90 and 90';
    if (formData.destLng && (isNaN(parseFloat(formData.destLng)) || parseFloat(formData.destLng) < -180 || parseFloat(formData.destLng) > 180))
      errors.destLng = 'Longitude must be between -180 and 180';
    return errors;
  };

  const handleSave = async () => {
    const errors = validateForm();
    if (Object.keys(errors).length > 0) { setFormErrors(errors); return; }

    setSaving(true);
    try {
      const originLocationValue = formData.originLat && formData.originLng
        ? JSON.stringify({ lat: parseFloat(formData.originLat), lng: parseFloat(formData.originLng) })
        : null;

      const destinationLocationValue = formData.destLat && formData.destLng
        ? JSON.stringify({ lat: parseFloat(formData.destLat), lng: parseFloat(formData.destLng) })
        : null;

      const updateData = {
        origin: formData.origin,
        destination: formData.destination,
        distanceKm: parseFloat(formData.distanceKm),
        durationMinutes: parseInt(formData.durationMinutes),
        ...(originLocationValue && { originLocation: originLocationValue }),
        ...(destinationLocationValue && { destinationLocation: destinationLocationValue }),
      };

      const result = await routeService.updateRoute(routeId, updateData);
      setRoute(result.data || result);
      setIsEditing(false);
      setSnackbar({ isOpen: true, message: t('routeUpdated') || 'Route updated successfully!', type: 'success' });
    } catch (err) {
      setError(err.response?.data?.message || 'Network error. Failed to update route.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    try {
      await routeService.deleteRoute(routeId);
      setSnackbar({ isOpen: true, message: t('routeDeleted') || 'Route deleted successfully!', type: 'success' });
      setTimeout(() => onBack(), 1000);
    } catch (err) {
      setError(err.response?.data?.message || 'Network error. Failed to delete route.');
    } finally {
      setShowDeleteDialog(false);
    }
  };

  const handleCancel = () => {
    if (route) {
      const originLoc = parseLocation(route.originLocation);
      const destLoc = parseLocation(route.destinationLocation);
      setFormData({
        origin: route.origin || '',
        destination: route.destination || '',
        distanceKm: route.distanceKm?.toString() || '',
        durationMinutes: route.durationMinutes?.toString() || '',
        originLat: originLoc?.lat?.toString() || '',
        originLng: originLoc?.lng?.toString() || '',
        destLat: destLoc?.lat?.toString() || '',
        destLng: destLoc?.lng?.toString() || '',
      });
    }
    setFormErrors({});
    setIsEditing(false);
  };

  const formatDuration = (minutes) => {
    if (!minutes) return '—';
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    return m === 0 ? `${h}h` : `${h}h ${m}m`;
  };

  const openInMaps = (lat, lng) => {
    window.open(`https://www.google.com/maps?q=${lat},${lng}&z=12&hl=en`, '_blank');
  };

  if (loading) {
    return (
      <div className="p-6 bg-gray-50 dark:bg-gray-900 min-h-screen">
        <div className="mb-6">
          <div className="h-10 w-40 bg-gray-200 dark:bg-gray-800 rounded-lg animate-pulse"></div>
        </div>
        <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 overflow-hidden">
          <div className="p-7 border-b border-gray-200 dark:border-gray-700">
            <div className="h-6 w-48 bg-gray-200 dark:bg-gray-700 rounded mb-2 animate-pulse"></div>
            <div className="h-4 w-72 bg-gray-200 dark:bg-gray-700 rounded animate-pulse"></div>
          </div>
          <div className="p-7 bg-gray-50 dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700">
            <div className="flex items-center gap-4">
              <div className="h-11 w-11 bg-gray-200 dark:bg-gray-700 rounded-xl animate-pulse"></div>
              <div className="flex-1">
                <div className="h-5 w-32 bg-gray-200 dark:bg-gray-700 rounded mb-2 animate-pulse"></div>
                <div className="h-3 w-20 bg-gray-200 dark:bg-gray-700 rounded animate-pulse"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (!route) {
    return (
      <div className="p-6 bg-gray-50 dark:bg-gray-900 min-h-screen">
        <div className="mb-6">
          <Button variant="outline" onClick={onBack} className="flex items-center gap-2">
            <Icon name="arrow-left" size={20} />
            {t('backToRoutes') || 'Back to Routes'}
          </Button>
        </div>
        <div className="flex flex-col items-center justify-center py-20 text-center">
          <Icon name="alert-circle" size={48} className="text-gray-400 mb-4" />
          <h3 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">{t('routeNotFound') || 'Route Not Found'}</h3>
          <p className="text-gray-600 dark:text-gray-400 mb-6">{t('routeNotFoundDesc') || "The route you're looking for doesn't exist or has been deleted."}</p>
          <Button variant="primary" onClick={onBack}>{t('backToRoutes') || 'Back to Routes'}</Button>
        </div>
      </div>
    );
  }

  const location = parseLocation(route.location);
  const originLocation = parseLocation(route.originLocation);
  const destinationLocation = parseLocation(route.destinationLocation);

  return (
    <div className="p-6 bg-gray-50 dark:bg-gray-900 min-h-screen">
      <div className="mb-6">
        <Button variant="outline" onClick={onBack} className="flex items-center gap-2 hover:-translate-x-0.5 transition-transform">
          <Icon name="arrow-left" size={20} />
          {t('backToRoutes') || 'Back to Routes'}
        </Button>
      </div>

      {error && (
        <div className="flex items-center gap-3 px-5 py-4 bg-white dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl text-red-600 dark:text-red-400 mb-6 font-medium">
          <Icon name="alert-circle" size={20} />
          <span>{error}</span>
        </div>
      )}

      <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 overflow-hidden shadow-sm">
        <div className="flex justify-between items-start gap-6 p-7 border-b border-gray-200 dark:border-gray-700">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white mb-1">{t('routeDetails') || 'Route Details'}</h1>
            <p className="text-sm text-gray-600 dark:text-gray-400">{t('routeDetailsDesc') || 'Manage route information and settings'}</p>
          </div>
          <div className="flex gap-2.5 flex-shrink-0">
            {!isEditing ? (
              <>
                <Button variant="secondary" onClick={() => setIsEditing(true)}>
                  <Icon name="edit" size={16} />
                  {t('edit') || 'Edit Route'}
                </Button>
                <Button variant="danger" onClick={() => setShowDeleteDialog(true)}>
                  <Icon name="trash" size={16} />
                  {t('delete') || 'Delete'}
                </Button>
              </>
            ) : (
              <>
                <Button variant="secondary" onClick={handleCancel}>
                  {t('cancel') || 'Cancel'}
                </Button>
                <Button variant="primary" onClick={handleSave} disabled={saving}>
                  {saving ? (t('saving') || 'Saving...') : (t('saveChanges') || 'Save Changes')}
                </Button>
              </>
            )}
          </div>
        </div>

        {!isEditing ? (
          <>
            <div className="flex items-center gap-0 p-7 bg-gray-50 dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700">
              <div className="flex items-center gap-3.5 flex-shrink-0">
                <div className="w-11 h-11 rounded-xl bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center text-white flex-shrink-0">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                    <circle cx="12" cy="10" r="3"/>
                  </svg>
                </div>
                <div>
                  <div className="text-lg font-bold text-gray-900 dark:text-white whitespace-nowrap">{route.origin}</div>
                  <div className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wide">{t('origin') || 'Origin'}</div>
                </div>
              </div>

              <div className="flex-1 flex flex-col items-center gap-2 px-5 min-w-0">
                <div className="relative w-full h-6 flex items-center">
                  <div className="w-full h-0.5 bg-gradient-to-r from-blue-500 to-emerald-500 rounded-full relative overflow-hidden">
                    <div className="absolute top-0 left-0 w-3/5 h-full bg-gradient-to-r from-transparent via-white/40 to-transparent animate-[shimmer_2.5s_infinite]"></div>
                  </div>
                  <span className="absolute left-1/2 -translate-x-1/2 text-lg animate-[busMove_3s_ease-in-out_infinite]">🚌</span>
                </div>
                <div className="flex gap-4 justify-center">
                  <span className="flex items-center gap-1.5 text-xs font-semibold text-gray-600 dark:text-gray-400 bg-white dark:bg-gray-800 px-2.5 py-1 rounded-full border border-gray-200 dark:border-gray-700">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M3 12h4l3-9 4 18 3-9h4"/>
                    </svg>
                    {route.distanceKm} km
                  </span>
                  <span className="flex items-center gap-1.5 text-xs font-semibold text-gray-600 dark:text-gray-400 bg-white dark:bg-gray-800 px-2.5 py-1 rounded-full border border-gray-200 dark:border-gray-700">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                    </svg>
                    {formatDuration(route.durationMinutes)}
                  </span>
                </div>
              </div>

              <div className="flex items-center gap-3.5 flex-shrink-0">
                <div className="w-11 h-11 rounded-xl bg-gradient-to-br from-emerald-500 to-emerald-600 flex items-center justify-center text-white flex-shrink-0">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                    <circle cx="12" cy="10" r="3"/>
                  </svg>
                </div>
                <div>
                  <div className="text-lg font-bold text-gray-900 dark:text-white whitespace-nowrap">{route.destination}</div>
                  <div className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wide">{t('destination') || 'Destination'}</div>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-4 gap-0 border-b border-gray-200 dark:border-gray-700">
              <div className="p-5 border-r border-gray-200 dark:border-gray-700">
                <div className="flex items-center gap-1.5 text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase tracking-wide mb-2">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/>
                  </svg>
                  {t('routeId') || 'Route ID'}
                </div>
                <div className="text-lg font-bold text-blue-600 dark:text-blue-400 font-mono">RT-{route.id}</div>
              </div>
              <div className="p-5 border-r border-gray-200 dark:border-gray-700">
                <div className="flex items-center gap-1.5 text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase tracking-wide mb-2">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M3 12h4l3-9 4 18 3-9h4"/>
                  </svg>
                  {t('distance') || 'Distance'}
                </div>
                <div className="text-lg font-bold text-gray-900 dark:text-white">{route.distanceKm} km</div>
              </div>
              <div className="p-5 border-r border-gray-200 dark:border-gray-700">
                <div className="flex items-center gap-1.5 text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase tracking-wide mb-2">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                  </svg>
                  {t('duration') || 'Duration'}
                </div>
                <div className="text-lg font-bold text-gray-900 dark:text-white">{formatDuration(route.durationMinutes)}</div>
              </div>
              <div className="p-5">
                <div className="flex items-center gap-1.5 text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase tracking-wide mb-2">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <rect x="1" y="3" width="15" height="13" rx="2"/><path d="M16 8h4l3 3v5h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>
                  </svg>
                  {t('totalBusesAssigned') || 'Buses Assigned'}
                </div>
                <div>
                  <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-semibold ${(route.busCount || 0) > 0 ? 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-400' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400'}`}>
                    {route.busCount || 0} {(route.busCount === 1) ? 'bus' : 'buses'}
                  </span>
                </div>
              </div>
            </div>

            <div className="p-7">
              <div className="mb-5">
                <div className="flex items-center gap-2 text-base font-bold text-gray-900 dark:text-white">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-blue-600">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                    <circle cx="12" cy="10" r="3"/>
                  </svg>
                  {t('locationCoordinates') || 'GPS Coordinates'}
                </div>
              </div>

              {(originLocation || destinationLocation) ? (
                <div className="space-y-6">
                  {/* Origin Coordinates */}
                  {originLocation && (
                    <div>
                      <div className="flex items-center gap-2 mb-3">
                        <span className="w-2.5 h-2.5 rounded-full bg-blue-600 shadow-[0_0_0_3px_rgba(59,130,246,0.2)]"></span>
                        <h3 className="text-sm font-bold text-gray-900 dark:text-white">{t('originCoordinates') || 'Origin Coordinates'}</h3>
                      </div>
                      <div className="grid grid-cols-2 gap-4 mb-4">
                        <div className="bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl p-4 hover:border-blue-500 transition-colors">
                          <div className="flex items-center gap-1.5 text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase tracking-wide mb-1.5">
                            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <line x1="12" y1="2" x2="12" y2="22"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
                            </svg>
                            Latitude
                          </div>
                          <div className="text-2xl font-bold text-gray-900 dark:text-white font-mono tracking-tight">{originLocation.lat.toFixed(6)}°</div>
                          <div className="text-xs text-blue-600 dark:text-blue-400 font-semibold mt-1">{originLocation.lat >= 0 ? 'North' : 'South'}</div>
                        </div>
                        <div className="bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl p-4 hover:border-blue-500 transition-colors">
                          <div className="flex items-center gap-1.5 text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase tracking-wide mb-1.5">
                            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
                            </svg>
                            Longitude
                          </div>
                          <div className="text-2xl font-bold text-gray-900 dark:text-white font-mono tracking-tight">{originLocation.lng.toFixed(6)}°</div>
                          <div className="text-xs text-blue-600 dark:text-blue-400 font-semibold mt-1">{originLocation.lng >= 0 ? 'East' : 'West'}</div>
                        </div>
                      </div>

                      <div className="border border-gray-200 dark:border-gray-700 rounded-xl overflow-hidden">
                        <div className="flex justify-between items-center px-4 py-3 bg-gray-50 dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700">
                          <span className="flex items-center gap-1.5 text-sm font-semibold text-gray-900 dark:text-white">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <polygon points="3 6 9 3 15 6 21 3 21 18 15 21 9 18 3 21"/>
                            </svg>
                            Origin Map Preview
                          </span>
                          <Button onClick={() => openInMaps(originLocation.lat, originLocation.lng)} className="flex items-center gap-1.5 text-xs font-semibold hover:-translate-y-0.5 transition-transform shadow-sm hover:shadow-md">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/>
                            </svg>
                            Open in Google Maps
                          </Button>
                        </div>
                        <div className="relative w-full h-64 bg-gray-100 dark:bg-gray-800">
                          <iframe
                            title="Origin Location Map"
                            className="w-full h-full border-0"
                            src={`https://www.openstreetmap.org/export/embed.html?bbox=${originLocation.lng - 0.1},${originLocation.lat - 0.08},${originLocation.lng + 0.1},${originLocation.lat + 0.08}&layer=mapnik&marker=${originLocation.lat},${originLocation.lng}`}
                            allowFullScreen
                            loading="lazy"
                          />
                        </div>
                        <div className="flex items-center gap-1.5 px-4 py-2.5 bg-gray-50 dark:bg-gray-900 border-t border-gray-200 dark:border-gray-700 text-xs text-gray-600 dark:text-gray-400 font-mono">
                          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-blue-600 flex-shrink-0">
                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
                          </svg>
                          {originLocation.lat.toFixed(4)}, {originLocation.lng.toFixed(4)}
                        </div>
                      </div>
                    </div>
                  )}

                  {/* Destination Coordinates */}
                  {destinationLocation && (
                    <div>
                      <div className="flex items-center gap-2 mb-3">
                        <span className="w-2.5 h-2.5 rounded-full bg-emerald-600 shadow-[0_0_0_3px_rgba(16,185,129,0.2)]"></span>
                        <h3 className="text-sm font-bold text-gray-900 dark:text-white">{t('destinationCoordinates') || 'Destination Coordinates'}</h3>
                      </div>
                      <div className="grid grid-cols-2 gap-4 mb-4">
                        <div className="bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl p-4 hover:border-emerald-500 transition-colors">
                          <div className="flex items-center gap-1.5 text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase tracking-wide mb-1.5">
                            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <line x1="12" y1="2" x2="12" y2="22"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
                            </svg>
                            Latitude
                          </div>
                          <div className="text-2xl font-bold text-gray-900 dark:text-white font-mono tracking-tight">{destinationLocation.lat.toFixed(6)}°</div>
                          <div className="text-xs text-emerald-600 dark:text-emerald-400 font-semibold mt-1">{destinationLocation.lat >= 0 ? 'North' : 'South'}</div>
                        </div>
                        <div className="bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl p-4 hover:border-emerald-500 transition-colors">
                          <div className="flex items-center gap-1.5 text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase tracking-wide mb-1.5">
                            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
                            </svg>
                            Longitude
                          </div>
                          <div className="text-2xl font-bold text-gray-900 dark:text-white font-mono tracking-tight">{destinationLocation.lng.toFixed(6)}°</div>
                          <div className="text-xs text-emerald-600 dark:text-emerald-400 font-semibold mt-1">{destinationLocation.lng >= 0 ? 'East' : 'West'}</div>
                        </div>
                      </div>

                      <div className="border border-gray-200 dark:border-gray-700 rounded-xl overflow-hidden">
                        <div className="flex justify-between items-center px-4 py-3 bg-gray-50 dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700">
                          <span className="flex items-center gap-1.5 text-sm font-semibold text-gray-900 dark:text-white">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <polygon points="3 6 9 3 15 6 21 3 21 18 15 21 9 18 3 21"/>
                            </svg>
                            Destination Map Preview
                          </span>
                          <Button onClick={() => openInMaps(destinationLocation.lat, destinationLocation.lng)} className="flex items-center gap-1.5 text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 hover:-translate-y-0.5 transition-transform shadow-sm hover:shadow-md">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/>
                            </svg>
                            Open in Google Maps
                          </Button>
                        </div>
                        <div className="relative w-full h-64 bg-gray-100 dark:bg-gray-800">
                          <iframe
                            title="Destination Location Map"
                            className="w-full h-full border-0"
                            src={`https://www.openstreetmap.org/export/embed.html?bbox=${destinationLocation.lng - 0.1},${destinationLocation.lat - 0.08},${destinationLocation.lng + 0.1},${destinationLocation.lat + 0.08}&layer=mapnik&marker=${destinationLocation.lat},${destinationLocation.lng}`}
                            allowFullScreen
                            loading="lazy"
                          />
                        </div>
                        <div className="flex items-center gap-1.5 px-4 py-2.5 bg-gray-50 dark:bg-gray-900 border-t border-gray-200 dark:border-gray-700 text-xs text-gray-600 dark:text-gray-400 font-mono">
                          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-emerald-600 flex-shrink-0">
                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
                          </svg>
                          {destinationLocation.lat.toFixed(4)}, {destinationLocation.lng.toFixed(4)}
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              ) : (
                <div className="flex flex-col items-center text-center py-10 px-6 bg-gray-50 dark:bg-gray-900 border-2 border-dashed border-gray-200 dark:border-gray-700 rounded-xl text-gray-500 dark:text-gray-400 gap-2">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
                    <line x1="2" y1="2" x2="22" y2="22"/>
                  </svg>
                  <p className="text-base font-semibold text-gray-700 dark:text-gray-300 mt-1">{t('noLocationData') || 'No location coordinates available'}</p>
                  <span className="text-sm">{t('noLocationDataDesc') || 'Edit this route to add GPS coordinates'}</span>
                </div>
              )}
            </div>
          </>
        ) : (
          <div className="p-7">
            <div className="grid grid-cols-2 gap-5">
              <div className="flex flex-col gap-2">
                <label className="text-sm font-semibold text-gray-900 dark:text-white">{t('origin') || 'Origin'} *</label>
                <Input
                  type="text"
                  name="origin"
                  value={formData.origin}
                  onChange={handleInputChange}
                  placeholder="e.g. Phnom Penh"
                  error={formErrors.origin}
                />
              </div>

              <div className="flex flex-col gap-2">
                <label className="text-sm font-semibold text-gray-900 dark:text-white">{t('destination') || 'Destination'} *</label>
                <Input
                  type="text"
                  name="destination"
                  value={formData.destination}
                  onChange={handleInputChange}
                  placeholder="e.g. Kampot"
                  error={formErrors.destination}
                />
              </div>

              <div className="flex flex-col gap-2">
                <label className="text-sm font-semibold text-gray-900 dark:text-white">{t('distance') || 'Distance'} (km) *</label>
                <Input
                  type="number"
                  name="distanceKm"
                  value={formData.distanceKm}
                  onChange={handleInputChange}
                  placeholder="e.g. 148"
                  min="1"
                  step="0.1"
                  error={formErrors.distanceKm}
                />
              </div>

              <div className="flex flex-col gap-2">
                <label className="text-sm font-semibold text-gray-900 dark:text-white">{t('duration') || 'Duration'} ({t('minutes') || 'minutes'}) *</label>
                <Input
                  type="number"
                  name="durationMinutes"
                  value={formData.durationMinutes}
                  onChange={handleInputChange}
                  placeholder="e.g. 360"
                  min="1"
                  error={formErrors.durationMinutes}
                />
              </div>

              <div className="col-span-2 space-y-5">
                {/* Origin Coordinates */}
                <div className="bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl p-5">
                  <div className="flex items-center gap-2 mb-3">
                    <span className="w-2.5 h-2.5 rounded-full bg-blue-600 shadow-[0_0_0_3px_rgba(59,130,246,0.2)]"></span>
                    <label className="text-sm font-semibold text-gray-900 dark:text-white">
                      {t('originCoordinates') || 'Origin Coordinates'}
                      <span className="text-xs font-normal text-gray-500 dark:text-gray-400 ml-2">({t('optional') || 'optional'})</span>
                    </label>
                  </div>
                  <div className="grid grid-cols-2 gap-3">
                    <div className="flex items-center border-2 border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden bg-white dark:bg-gray-800 focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20 transition-all">
                      <span className="px-3 py-2.5 bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400 text-xs font-bold tracking-wider border-r border-gray-200 dark:border-gray-600 font-mono">LAT</span>
                      <input
                        type="number"
                        name="originLat"
                        value={formData.originLat}
                        onChange={handleInputChange}
                        placeholder="e.g. 11.5564"
                        step="0.0001"
                        className={`flex-1 px-3 py-2.5 border-0 outline-none bg-transparent text-gray-900 dark:text-white text-sm font-mono min-w-0 ${formErrors.originLat ? 'text-red-600' : ''}`}
                      />
                    </div>
                    <div className="flex items-center border-2 border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden bg-white dark:bg-gray-800 focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20 transition-all">
                      <span className="px-3 py-2.5 bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400 text-xs font-bold tracking-wider border-r border-gray-200 dark:border-gray-600 font-mono">LNG</span>
                      <input
                        type="number"
                        name="originLng"
                        value={formData.originLng}
                        onChange={handleInputChange}
                        placeholder="e.g. 104.9282"
                        step="0.0001"
                        className={`flex-1 px-3 py-2.5 border-0 outline-none bg-transparent text-gray-900 dark:text-white text-sm font-mono min-w-0 ${formErrors.originLng ? 'text-red-600' : ''}`}
                      />
                    </div>
                  </div>
                  {(formErrors.originLat || formErrors.originLng) && (
                    <span className="text-xs text-red-600 dark:text-red-400 font-medium mt-2 block">{formErrors.originLat || formErrors.originLng}</span>
                  )}
                  {formData.originLat && formData.originLng && !formErrors.originLat && !formErrors.originLng && (
                    <div className="mt-3 border border-gray-200 dark:border-gray-700 rounded-xl overflow-hidden">
                      <div className="flex items-center gap-1.5 px-3.5 py-2.5 bg-gray-50 dark:bg-gray-900 font-semibold text-sm text-gray-900 dark:text-white border-b border-gray-200 dark:border-gray-700">
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <polygon points="3 6 9 3 15 6 21 3 21 18 15 21 9 18 3 21" />
                        </svg>
                        Origin Map Preview
                      </div>
                      <iframe
                        title="Origin Coordinate Preview"
                        className="w-full h-48 border-0"
                        src={`https://www.openstreetmap.org/export/embed.html?bbox=${parseFloat(formData.originLng) - 0.1},${parseFloat(formData.originLat) - 0.08},${parseFloat(formData.originLng) + 0.1},${parseFloat(formData.originLat) + 0.08}&layer=mapnik&marker=${formData.originLat},${formData.originLng}`}
                        loading="lazy"
                      />
                    </div>
                  )}
                </div>

                {/* Destination Coordinates */}
                <div className="bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl p-5">
                  <div className="flex items-center gap-2 mb-3">
                    <span className="w-2.5 h-2.5 rounded-full bg-emerald-600 shadow-[0_0_0_3px_rgba(16,185,129,0.2)]"></span>
                    <label className="text-sm font-semibold text-gray-900 dark:text-white">
                      {t('destinationCoordinates') || 'Destination Coordinates'}
                      <span className="text-xs font-normal text-gray-500 dark:text-gray-400 ml-2">({t('optional') || 'optional'})</span>
                    </label>
                  </div>
                  <div className="grid grid-cols-2 gap-3">
                    <div className="flex items-center border-2 border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden bg-white dark:bg-gray-800 focus-within:border-emerald-500 focus-within:ring-2 focus-within:ring-emerald-500/20 transition-all">
                      <span className="px-3 py-2.5 bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400 text-xs font-bold tracking-wider border-r border-gray-200 dark:border-gray-600 font-mono">LAT</span>
                      <input
                        type="number"
                        name="destLat"
                        value={formData.destLat}
                        onChange={handleInputChange}
                        placeholder="e.g. 13.3633"
                        step="0.0001"
                        className={`flex-1 px-3 py-2.5 border-0 outline-none bg-transparent text-gray-900 dark:text-white text-sm font-mono min-w-0 ${formErrors.destLat ? 'text-red-600' : ''}`}
                      />
                    </div>
                    <div className="flex items-center border-2 border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden bg-white dark:bg-gray-800 focus-within:border-emerald-500 focus-within:ring-2 focus-within:ring-emerald-500/20 transition-all">
                      <span className="px-3 py-2.5 bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400 text-xs font-bold tracking-wider border-r border-gray-200 dark:border-gray-600 font-mono">LNG</span>
                      <input
                        type="number"
                        name="destLng"
                        value={formData.destLng}
                        onChange={handleInputChange}
                        placeholder="e.g. 103.8448"
                        step="0.0001"
                        className={`flex-1 px-3 py-2.5 border-0 outline-none bg-transparent text-gray-900 dark:text-white text-sm font-mono min-w-0 ${formErrors.destLng ? 'text-red-600' : ''}`}
                      />
                    </div>
                  </div>
                  {(formErrors.destLat || formErrors.destLng) && (
                    <span className="text-xs text-red-600 dark:text-red-400 font-medium mt-2 block">{formErrors.destLat || formErrors.destLng}</span>
                  )}
                  {formData.destLat && formData.destLng && !formErrors.destLat && !formErrors.destLng && (
                    <div className="mt-3 border border-gray-200 dark:border-gray-700 rounded-xl overflow-hidden">
                      <div className="flex items-center gap-1.5 px-3.5 py-2.5 bg-gray-50 dark:bg-gray-900 font-semibold text-sm text-gray-900 dark:text-white border-b border-gray-200 dark:border-gray-700">
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <polygon points="3 6 9 3 15 6 21 3 21 18 15 21 9 18 3 21" />
                        </svg>
                        Destination Map Preview
                      </div>
                      <iframe
                        title="Destination Coordinate Preview"
                        className="w-full h-48 border-0"
                        src={`https://www.openstreetmap.org/export/embed.html?bbox=${parseFloat(formData.destLng) - 0.1},${parseFloat(formData.destLat) - 0.08},${parseFloat(formData.destLng) + 0.1},${parseFloat(formData.destLat) + 0.08}&layer=mapnik&marker=${formData.destLat},${formData.destLng}`}
                        loading="lazy"
                      />
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      <ConfirmDialog
        isOpen={showDeleteDialog}
        onConfirm={handleDelete}
        onCancel={() => setShowDeleteDialog(false)}
        title={t('deleteRoute') || 'Delete Route'}
        message={`${t('deleteRouteConfirm') || 'Are you sure you want to delete the route from'} ${route?.origin} ${t('to') || 'to'} ${route?.destination}? ${t('thisActionCannotBeUndone') || 'This action cannot be undone.'}`}
        confirmText={t('delete') || 'Delete'}
        cancelText={t('cancel') || 'Cancel'}
        type="danger"
      />

      <Snackbar
        isOpen={snackbar.isOpen}
        message={snackbar.message}
        type={snackbar.type}
        onClose={() => setSnackbar(s => ({ ...s, isOpen: false }))}
        duration={3000}
      />

      <style jsx>{`
        @keyframes shimmer {
          0% { left: -60%; }
          100% { left: 160%; }
        }
        @keyframes busMove {
          0%, 100% { left: 30%; }
          50% { left: 70%; }
        }
      `}</style>
    </div>
  );
};

export default RouteDetailPage;
