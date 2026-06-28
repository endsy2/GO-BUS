import axiosInstance from 'services/axiosConfig';

const dashboardService = {
  /**
   * Get dashboard statistics with optional date range filtering
   * @param {Object} params - Query parameters
   * @param {string} params.fromDate - Start date in ISO format (optional)
   * @param {string} params.toDate - End date in ISO format (optional)
   * @returns {Promise} Dashboard stats response
   */
  getDashboardStats: async (params = {}) => {
    const queryParams = new URLSearchParams();
    
    if (params.fromDate) {
      queryParams.append('fromDate', params.fromDate);
    }
    
    if (params.toDate) {
      queryParams.append('toDate', params.toDate);
    }
    
    const queryString = queryParams.toString();
    const url = `/admin/dashboard/stats${queryString ? `?${queryString}` : ''}`;
    
    return axiosInstance.get(url);
  },

  /**
   * Get booking velocity trend data
   * @param {Object} params - Query parameters
   * @param {string} params.fromDate - Start date in ISO format (optional)
   * @param {string} params.toDate - End date in ISO format (optional)
   * @returns {Promise} Booking velocity trend response
   */
  getBookingVelocity: async (params = {}) => {
    const queryParams = new URLSearchParams();
    
    if (params.fromDate) {
      queryParams.append('fromDate', params.fromDate);
    }
    
    if (params.toDate) {
      queryParams.append('toDate', params.toDate);
    }
    
    const queryString = queryParams.toString();
    const url = `/admin/dashboard/velocity${queryString ? `?${queryString}` : ''}`;
    
    return axiosInstance.get(url);
  },

  /**
   * Get revenue stream by payment method
   * @param {Object} params - Query parameters
   * @param {string} params.fromDate - Start date in ISO format (optional)
   * @param {string} params.toDate - End date in ISO format (optional)
   * @returns {Promise} Revenue stream response
   */
  getRevenueStream: async (params = {}) => {
    const queryParams = new URLSearchParams();
    
    if (params.fromDate) {
      queryParams.append('fromDate', params.fromDate);
    }
    
    if (params.toDate) {
      queryParams.append('toDate', params.toDate);
    }
    
    const queryString = queryParams.toString();
    const url = `/admin/dashboard/revenue-stream${queryString ? `?${queryString}` : ''}`;
    
    return axiosInstance.get(url);
  },
};

export default dashboardService;
