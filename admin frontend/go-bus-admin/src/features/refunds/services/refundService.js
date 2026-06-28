import axiosInstance from 'services/axiosConfig';

const refundService = {
  // Get all refunds with optional status filter
  getAllRefunds: async (status = null, page = 0, size = 10) => {
    const params = new URLSearchParams();
    
    if (status) params.append('status', status);
    params.append('page', page);
    params.append('size', size);

    const response = await axiosInstance.get(`/admin/refunds?${params.toString()}`);
    return response.data;
  },

  // Get refunds by date range
  getRefundsByDateRange: async (status, fromDate, toDate, page = 0, size = 10) => {
    const params = new URLSearchParams();
    
    params.append('status', status);
    params.append('fromDate', fromDate);
    params.append('toDate', toDate);
    params.append('page', page);
    params.append('size', size);

    const response = await axiosInstance.get(`/admin/refunds/date-range?${params.toString()}`);
    return response.data;
  },

  // Process refund (approve or reject)
  processRefund: async (refundId, approved, rejectionReason = null) => {
    const response = await axiosInstance.put(`/admin/refunds/${refundId}/process`, {
      approved,
      rejectionReason
    });
    return response.data;
  },

  // Get refund statistics
  getRefundStatistics: async (fromDate, toDate) => {
    const params = new URLSearchParams();
    params.append('fromDate', fromDate);
    params.append('toDate', toDate);

    const response = await axiosInstance.get(`/admin/refunds/statistics?${params.toString()}`);
    return response.data;
  },
};

export default refundService;
