import axiosInstance from 'services/axiosConfig';

// Wraps backend AdminUserController (/api/admin/users/**).
// axiosInstance already prefixes /api, so paths here start with /admin/users.
const customerService = {
  getCustomers: async (params = {}) => {
    const response = await axiosInstance.get('/admin/users', { params });
    return response.data;
  },

  // Legacy "specification" shape used by CustomersPage. Translates to the real
  // AdminUserController.getUsers params before hitting GET /admin/users.
  getBySpecification: async (params = {}) => {
    const {
      pageStart,
      pageSize,
      isActive,
      isEmployee,
      email,
      phone,
      username,
      // dropped — not supported by /admin/users:
      isDeleted, userId,
      ...rest
    } = params;

    const translated = { ...rest };
    if (isEmployee !== undefined && isEmployee !== null) translated.isEmployee = isEmployee;
    if (pageStart != null) translated.page = Math.max(0, Number(pageStart) - 1);
    if (pageSize != null) translated.size = pageSize;
    if (isActive !== undefined && isActive !== null && isActive !== '') {
      translated.active = isActive === true || isActive === 'true';
    }

    // Backend has one `search` field; pick the first non-empty filter value.
    const searchTerm = [username, email, phone]
      .map(v => (v == null ? '' : String(v).trim()))
      .find(v => v !== '');
    if (searchTerm) translated.search = searchTerm;

    const response = await axiosInstance.get('/admin/users', { params: translated });
    return response.data;
  },

  getCustomerById: async (customerId) => {
    const response = await axiosInstance.get(`/admin/users/${customerId}`);
    return response.data;
  },

  // Lifetime booking stats for a customer. Lives on the booking-service admin
  // controller (/api/admin/bookings/**), not the user endpoint above.
  // Returns the envelope { status, message, data: { totalBookings, totalSpent, activeTickets } }.
  getCustomerBookingStats: async (customerId) => {
    const response = await axiosInstance.get(`/admin/bookings/user-detail-stats/${customerId}`);
    return response.data;
  },

  // AdminUserController has no POST endpoint; admin customer creation reuses /users.
  createCustomer: async (customerData) => {
    const response = await axiosInstance.post('/users', customerData);
    return response.data;
  },

  updateCustomer: async (customerId, customerData) => {
    const response = await axiosInstance.put(`/admin/users/${customerId}`, customerData);
    return response.data;
  },

  setCustomerStatus: async (customerId, active) => {
    const response = await axiosInstance.patch(`/admin/users/${customerId}/status`, { active });
    return response.data;
  },

  resetCustomerPassword: async (customerId, newPassword) => {
    const response = await axiosInstance.patch(`/admin/users/${customerId}/reset-password`, { newPassword });
    return response.data;
  },

  unlinkGoogle: async (customerId) => {
    const response = await axiosInstance.patch(`/admin/users/${customerId}/unlink-google`);
    return response.data;
  },

  assignRoles: async (customerId, roles) => {
    const response = await axiosInstance.put(`/admin/users/${customerId}/roles`, { roles });
    return response.data;
  },

  adjustWallet: async (customerId, adjustment) => {
    const response = await axiosInstance.patch(`/admin/users/${customerId}/wallet/adjust`, adjustment);
    return response.data;
  },

  setWalletStatus: async (customerId, status) => {
    const response = await axiosInstance.patch(`/admin/users/${customerId}/wallet/status`, null, {
      params: { status },
    });
    return response.data;
  },

  deleteCustomer: async (customerId) => {
    const response = await axiosInstance.delete(`/admin/users/${customerId}`);
    return response.data;
  },
};

export default customerService;
