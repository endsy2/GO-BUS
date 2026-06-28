import axiosInstance from 'services/axiosConfig';

const ticketService = {
  getById: async (id) => {
    const response = await axiosInstance.get(`/api/tickets/${id}`);
    return response.data;
  },

  regenerateQR: async (bookingId) => {
    const response = await axiosInstance.post(`/api/tickets/booking/${bookingId}/regenerate`);
    return response.data;
  },
};

export default ticketService;
