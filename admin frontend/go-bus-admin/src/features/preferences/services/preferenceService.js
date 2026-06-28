import axiosInstance from 'services/axiosConfig';

const preferenceService = {
  getByUserId: async (userId) => {
    const response = await axiosInstance.get(`/api/preferences/user/${userId}`);
    return response.data;
  },

  update: async (userId, data) => {
    const response = await axiosInstance.put(`/api/preferences/user/${userId}`, data);
    return response.data;
  },
};

export default preferenceService;
