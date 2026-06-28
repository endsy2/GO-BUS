import axiosInstance from 'services/axiosConfig';

const reportService = {
  // ========== Dashboard Stats ==========
  
  // Get dashboard statistics
  // getDashboardStats: async () => {
  //   const response = await axiosInstance.get('/admin/dashboard/stats');
  //   return response.data;
  // },

  // ========== Financial Reports (AdminFinancialReportController) ==========
  
  // Get revenue report (Excel download)
  getRevenueReport: async (startDate, endDate, period = 'DAILY') => {
    const response = await axiosInstance.get('/admin/financial-reports/revenue', {
      params: { startDate, endDate, period },
      responseType: 'blob'
    });
    return response.data;
  },

  // Get payment method report (Excel download)
  getPaymentMethodReport: async (startDate, endDate) => {
    const response = await axiosInstance.get('/admin/financial-reports/payment-methods', {
      params: { startDate, endDate },
      responseType: 'blob'
    });
    return response.data;
  },

  // Get refund & cancellation report (Excel download)
  getRefundCancellationReport: async (startDate, endDate, period = 'DAILY') => {
    const response = await axiosInstance.get('/admin/financial-reports/refunds-cancellations', {
      params: { startDate, endDate, period },
      responseType: 'blob'
    });
    return response.data;
  },

  // Get promo code usage report (Excel download)
  getPromoCodeUsageReport: async (startDate, endDate) => {
    const response = await axiosInstance.get('/admin/financial-reports/promo-codes', {
      params: { startDate, endDate },
      responseType: 'blob'
    });
    return response.data;
  },

  // ========== Operational Reports (AdminReportController) ==========
  
  // Get booking report (Excel download)
  getBookingReport: async (period = 'DAILY', date = null, startDate = null, endDate = null, year = null, month = null) => {
    const params = { period };
    if (date) params.date = date;
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    if (year) params.year = year;
    if (month) params.month = month;
    
    const response = await axiosInstance.get('/admin/reports-operation/bookings', {
      params,
      responseType: 'blob'
    });
    return response.data;
  },

  // Get route revenue report (Excel download)
  getRouteRevenueReport: async (startDate, endDate) => {
    const response = await axiosInstance.get('/admin/reports-operation/revenue/routes', {
      params: { startDate, endDate },
      responseType: 'blob'
    });
    return response.data;
  },

  // Get popular routes report (Excel download)
  getPopularRoutesReport: async (startDate, endDate, limit = 10) => {
    const response = await axiosInstance.get('/admin/reports-operation/routes/popular', {
      params: { startDate, endDate, limit },
      responseType: 'blob'
    });
    return response.data;
  },

  // Get seat occupancy report (Excel download)
  getSeatOccupancyReport: async (scheduleId = null) => {
    const params = {};
    if (scheduleId) params.scheduleId = scheduleId;
    
    const response = await axiosInstance.get('/admin/report-operation/occupancy', {
      params,
      responseType: 'blob'
    });
    return response.data;
  },

  // Get ticket sales report (Excel download)
  getTicketSalesReport: async (startDate, endDate, routeId = null, busId = null) => {
    const params = { startDate, endDate };
    if (routeId) params.routeId = routeId;
    if (busId) params.busId = busId;
    
    const response = await axiosInstance.get('/admin/reports-operation/tickets/sales', {
      params,
      responseType: 'blob'
    });
    return response.data;
  },

  // ========== Customer Reports (AdminCustomerReportController) ==========
  
  // Get active customer report (Excel download)
  getActiveCustomerReport: async (startDate, endDate) => {
    const response = await axiosInstance.get('/admin/reports-customer/customers/active', {
      params: { startDate, endDate },
      responseType: 'blob'
    });
    return response.data;
  },

  // Get frequent traveler report (Excel download)
  getFrequentTravelerReport: async (startDate, endDate, limit = 50) => {
    const response = await axiosInstance.get('/admin/reports-customer/customers/frequent-travelers', {
      params: { startDate, endDate, limit },
      responseType: 'blob'
    });
    return response.data;
  },

  // Get customer demographics report (Excel download)
  getCustomerDemographicsReport: async (startDate, endDate) => {
    const response = await axiosInstance.get('/admin/reports-customer/customers/demographics', {
      params: { startDate, endDate },
      responseType: 'blob'
    });
    return response.data;
  },

  // Get booking patterns by day report (Excel download)
  getBookingPatternsByDayReport: async (startDate, endDate) => {
    const response = await axiosInstance.get('/admin/reports-customer/bookings/patterns-by-day', {
      params: { startDate, endDate },
      responseType: 'blob'
    });
    return response.data;
  },

  // Get booking patterns by hour report (Excel download)
  getBookingPatternsByHourReport: async (startDate, endDate) => {
    const response = await axiosInstance.get('/admin/reports-customer/bookings/patterns-by-hour', {
      params: { startDate, endDate },
      responseType: 'blob'
    });
    return response.data;
  },

  // ========== Bus Reports (AdminBusReportController) ==========
  
  // Get bus utilization report (Excel download)
  getBusUtilizationReport: async (startDate, endDate) => {
    const response = await axiosInstance.get('/admin/reports-bus/buses/utilization', {
      params: { startDate, endDate },
      responseType: 'blob'
    });
    return response.data;
  },

  // Get route performance report (Excel download)
  getRoutePerformanceReport: async (startDate, endDate) => {
    const response = await axiosInstance.get('/admin/reports-bus/routes/performance', {
      params: { startDate, endDate },
      responseType: 'blob'
    });
    return response.data;
  },

  // Get inactive bus report (Excel download)
  getInactiveBusReport: async (daysThreshold = 30) => {
    const response = await axiosInstance.get('/admin/reports-bus/buses/inactive', {
      params: { daysThreshold },
      responseType: 'blob'
    });
    return response.data;
  },

  // Get inactive route report (Excel download)
  getInactiveRouteReport: async (daysThreshold = 30) => {
    const response = await axiosInstance.get('/admin/reports-bus/routes/inactive', {
      params: { daysThreshold },
      responseType: 'blob'
    });
    return response.data;
  },

  // Get bus capacity analysis report (Excel download)
  getBusCapacityAnalysisReport: async (startDate, endDate) => {
    const response = await axiosInstance.get('/admin/reports-bus/buses/capacity-analysis', {
      params: { startDate, endDate },
      responseType: 'blob'
    });
    return response.data;
  },

  // ========== Helper Functions ==========
  
  // Download blob as file
  downloadFile: (blob, filename) => {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  },
};

export default reportService;
