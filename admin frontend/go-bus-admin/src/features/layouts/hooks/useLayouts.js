import { useState, useEffect } from 'react';
import layoutService from '../services/layoutService';

export const useLayouts = () => {
  const [layouts, setLayouts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchLayouts = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await layoutService.getAllLayouts();
      const data = response.data || response;
      
      // Parse layout JSON string for each layout
      const parsedLayouts = Array.isArray(data) ? data.map(layout => {
        try {
          const layoutData = typeof layout.layout === 'string' 
            ? JSON.parse(layout.layout) 
            : layout.layout;
          return {
            ...layout,
            ...layoutData
          };
        } catch (e) {
          console.error('Failed to parse layout:', e);
          return layout;
        }
      }) : [];
      
      setLayouts(parsedLayouts);
    } catch (err) {
      setError(err.message || 'Failed to fetch layouts');
      setLayouts([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLayouts();
  }, []);

  return { layouts, loading, error, refetch: fetchLayouts };
};
