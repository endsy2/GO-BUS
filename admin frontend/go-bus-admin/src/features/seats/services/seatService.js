import axiosInstance from 'services/axiosConfig';

const seatService = {
  getById: async (id) => {
    const response = await axiosInstance.get(`/seats/${id}`);
    return response.data;
  },

  getByBus: async (busId) => {
    const response = await axiosInstance.get(`/seats/bus/${busId}`);
    return response.data;
  },

  getByStatus: async (busId, status) => {
    const response = await axiosInstance.get(`/seats/bus/${busId}/status/${status}`);
    return response.data;
  },

  getByType: async (busId, type) => {
    const response = await axiosInstance.get(`/seats/bus/${busId}/type/${type}`);
    return response.data;
  },

  getBySeatNumber: async (busId, seatNumber) => {
    const response = await axiosInstance.get(`/seats/bus/${busId}/number/${seatNumber}`);
    return response.data;
  },

  countByStatus: async (busId, status) => {
    const response = await axiosInstance.get(`/seats/bus/${busId}/count/${status}`);
    return response.data;
  },

  create: async (data) => {
    const response = await axiosInstance.post('/seats', data);
    return response.data;
  },

  bulkCreate: async (busId, data) => {
    const response = await axiosInstance.post(`/seats/bus/${busId}/bulk`, data);
    return response.data;
  },

  update: async (id, data) => {
    const response = await axiosInstance.put(`/seats/${id}`, data);
    return response.data;
  },

  updateStatus: async (id, data) => {
    const response = await axiosInstance.patch(`/seats/${id}/status`, data);
    return response.data;
  },

  delete: async (id) => {
    const response = await axiosInstance.delete(`/seats/${id}`);
    return response.data;
  },
};

export default seatService;
