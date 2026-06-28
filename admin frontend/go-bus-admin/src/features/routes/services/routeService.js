import axiosInstance from 'services/axiosConfig';

const routeService = {
  // Get all routes (unpaginated)
  getRoutes: async (params = {}) => {
    const response = await axiosInstance.get('/routes', { params });
    return response.data;
  },

  // Get routes with server-side pagination
  getRoutesPaginated: async (pageNo, pageSize) => {
    const response = await axiosInstance.get('/routes/paginate', { params: { pageNo, pageSize } });
    return response.data;
  },

  // Search routes by origin / destination
  searchRoutes: async (origin, destination) => {
    const response = await axiosInstance.get('/routes/search', { params: { origin, destination } });
    return response.data;
  },

  // Get route by ID
  getRouteById: async (routeId) => {
    const response = await axiosInstance.get(`/routes/${routeId}`);
    return response.data;
  },

  // Create route
  createRoute: async (routeData) => {
    const response = await axiosInstance.post('/routes', routeData);
    return response.data;
  },

  // Update route
  updateRoute: async (routeId, routeData) => {
    const response = await axiosInstance.put(`/routes/${routeId}`, routeData);
    return response.data;
  },

  // Delete route
  deleteRoute: async (routeId) => {
    const response = await axiosInstance.delete(`/routes/${routeId}`);
    return response.data;
  },
};

export default routeService;
