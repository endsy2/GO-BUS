import { useState, useEffect } from 'react';
import promoService from '../services/promoService';

export const usePromos = () => {
  const [promos, setPromos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchPromos = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await promoService.getAllPromos();
      const data = response.data || response;
      setPromos(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'Failed to fetch promo codes');
      setPromos([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPromos();
  }, []);

  return {
    promos,
    loading,
    error,
    refetch: fetchPromos,
  };
};
