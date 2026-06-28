import axiosInstance from 'services/axiosConfig';

const promoService = {
  // Get all promo codes
  getAllPromos: async () => {
    const response = await axiosInstance.get('/promos');
    return response.data;
  },

  // Get promo code by ID
  getPromoById: async (id) => {
    const response = await axiosInstance.get(`/promos/${id}`);
    return response.data;
  },

  // Get promo code by code
  getPromoByCode: async (code) => {
    const response = await axiosInstance.get(`/promos/code/${code}`);
    return response.data;
  },

  // Create promo code
  createPromo: async (promoData) => {
    const response = await axiosInstance.post('/promos', promoData);
    return response.data;
  },

  // Update promo code
  updatePromo: async (id, promoData) => {
    const response = await axiosInstance.put(`/promos/${id}`, promoData);
    return response.data;
  },

  // Delete promo code
  deletePromo: async (id) => {
    const response = await axiosInstance.delete(`/promos/${id}`);
    return response.data;
  },
};

export default promoService;
