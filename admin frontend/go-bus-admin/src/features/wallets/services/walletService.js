import axiosInstance from 'services/axiosConfig';

const walletService = {
  // Create wallet for user (used by CustomerDetailPage)
  createWallet: async (userId) => {
    const response = await axiosInstance.post(`/admin/wallets/user/${userId}`);
    return response.data;
  },

  // Get wallet by user ID
  getWalletByUser: async (userId) => {
    const response = await axiosInstance.get(`/admin/wallets/user/${userId}`);
    return response.data;
  },

  // Get wallet by ID
  getWalletById: async (walletId) => {
    const response = await axiosInstance.get(`/admin/wallets/${walletId}`);
    return response.data.data; // Extract data from ApiResponse wrapper
  },

  // Get all wallets with filters and pagination
  getWallets: async (filters = {}, page = 0, size = 10) => {
    const params = new URLSearchParams();
    
    if (filters.name) params.append('name', filters.name);
    if (filters.userId) params.append('userId', filters.userId);
    if (filters.status) params.append('status', filters.status);
    if (filters.minBalance) params.append('minBalance', filters.minBalance);
    if (filters.maxBalance) params.append('maxBalance', filters.maxBalance);
    if (filters.currency) params.append('currency', filters.currency);
    params.append('page', page);
    params.append('size', size);

    const response = await axiosInstance.get(`/admin/wallets?${params.toString()}`);
    return response.data;
  },

  // Get transactions with filters and pagination
  getTransactions: async (filters = {}, page = 1, size = 10) => {
    const params = new URLSearchParams();
    
    if (filters.walletId) params.append('walletId', filters.walletId);
    if (filters.type) params.append('type', filters.type);
    if (filters.status) params.append('status', filters.status);
    if (filters.referenceId) params.append('referenceId', filters.referenceId);
    if (filters.fromDate) params.append('fromDate', filters.fromDate);   // ISO yyyy-MM-dd
    if (filters.toDate) params.append('toDate', filters.toDate);         // ISO yyyy-MM-dd
    if (filters.minAmount) params.append('minAmount', filters.minAmount);
    if (filters.maxAmount) params.append('maxAmount', filters.maxAmount);
    params.append('page', page);
    params.append('size', size);

    const response = await axiosInstance.get(`/admin/wallets/transactions?${params.toString()}`);
    return response.data;
  },
};

export default walletService;
