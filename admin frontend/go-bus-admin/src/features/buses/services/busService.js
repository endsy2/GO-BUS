import axiosInstance from 'services/axiosConfig';

const busService = {
  // Get all buses with filters
  getBuses: async (params = {}) => {
    const response = await axiosInstance.get('/buses/filter', { params });
    return response.data;
  },

  // Get bus by ID
  getBusById: async (busId) => {
    const response = await axiosInstance.get(`/buses/${busId}`);
    return response.data;
  },

  // Create bus
  createBus: async (busData) => {
    const response = await axiosInstance.post('/buses', busData);
    return response.data;
  },

  // Update bus
  updateBus: async (busId, busData) => {
    const response = await axiosInstance.put(`/buses/${busId}`, busData);
    return response.data;
  },

  // Delete bus
  deleteBus: async (busId) => {
    const response = await axiosInstance.delete(`/buses/${busId}`);
    return response.data;
  },

  // Get bus statistics
  getBusStats: async () => {
    const response = await axiosInstance.get('/buses/stats');
    return response.data;
  },
};

export default busService;
