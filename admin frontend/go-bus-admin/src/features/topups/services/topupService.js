import axiosInstance from 'services/axiosConfig';

const topupService = {
  getByUserId: async (userId) => {
    const response = await axiosInstance.get(`/api/topups/user/${userId}`);
    return response.data;
  },

  getByUserIdAndStatus: async (userId, status) => {
    const response = await axiosInstance.get(`/api/topups/user/${userId}/status/${status}`);
    return response.data;
  },

  getByTransactionId: async (txId) => {
    const response = await axiosInstance.get(`/api/topups/transaction/${txId}`);
    return response.data;
  },

  create: async (userId, data) => {
    const response = await axiosInstance.post(`/api/topups/user/${userId}`, data);
    return response.data;
  },
};

export default topupService;
