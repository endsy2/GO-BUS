import axiosInstance from 'services/axiosConfig';

const notificationService = {
  getByUserId: async (userId) => {
    const response = await axiosInstance.get(`/api/notifications/user/${userId}`);
    return response.data;
  },

  markAsRead: async (id) => {
    const response = await axiosInstance.patch(`/api/notifications/${id}/read`);
    return response.data;
  },

  delete: async (id) => {
    const response = await axiosInstance.delete(`/api/notifications/${id}`);
    return response.data;
  },
};

export default notificationService;
