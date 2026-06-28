import axiosInstance from 'services/axiosConfig';

const adminService = {
  users: {
    list: async (params) => {
      const response = await axiosInstance.get('/admin/users', { params });
      return response.data;
    },

    getById: async (id) => {
      const response = await axiosInstance.get(`/admin/users/${id}`);
      return response.data;
    },

    setStatus: async (id, data) => {
      const response = await axiosInstance.patch(`/admin/users/${id}/status`, data);
      return response.data;
    },

    resetPassword: async (id, data) => {
      const response = await axiosInstance.patch(`/admin/users/${id}/reset-password`, data);
      return response.data;
    },

    unlinkGoogle: async (id) => {
      const response = await axiosInstance.patch(`/admin/users/${id}/unlink-google`);
      return response.data;
    },

    assignRoles: async (id, data) => {
      const response = await axiosInstance.put(`/admin/users/${id}/roles`, data);
      return response.data;
    },

    adjustWallet: async (id, data) => {
      const response = await axiosInstance.patch(`/admin/users/${id}/wallet/adjust`, data);
      return response.data;
    },

    setWalletStatus: async (id, data) => {
      const response = await axiosInstance.patch(`/admin/users/${id}/wallet/status`, data);
      return response.data;
    },

    delete: async (id) => {
      const response = await axiosInstance.delete(`/admin/users/${id}`);
      return response.data;
    },
  },

  roles: {
    getAll: async () => {
      const response = await axiosInstance.get('/admin/roles');
      return response.data;
    },

    getPaged: async (params) => {
      const response = await axiosInstance.get('/admin/roles/paged', { params });
      return response.data;
    },

    getById: async (id) => {
      const response = await axiosInstance.get(`/admin/roles/${id}`);
      return response.data;
    },

    create: async (data) => {
      const response = await axiosInstance.post('/admin/roles', data);
      return response.data;
    },

    update: async (id, data) => {
      const response = await axiosInstance.put(`/admin/roles/${id}`, data);
      return response.data;
    },

    delete: async (id) => {
      const response = await axiosInstance.delete(`/admin/roles/${id}`);
      return response.data;
    },

    updatePermissions: async (id, data) => {
      const response = await axiosInstance.put(`/admin/roles/${id}/permissions`, data);
      return response.data;
    },
  },

  permissions: {
    getAll: async () => {
      const response = await axiosInstance.get('/admin/permissions');
      return response.data;
    },

    getById: async (id) => {
      const response = await axiosInstance.get(`/admin/permissions/${id}`);
      return response.data;
    },

    create: async (data) => {
      const response = await axiosInstance.post('/admin/permissions', data);
      return response.data;
    },

    update: async (id, data) => {
      const response = await axiosInstance.put(`/admin/permissions/${id}`, data);
      return response.data;
    },

    delete: async (id) => {
      const response = await axiosInstance.delete(`/admin/permissions/${id}`);
      return response.data;
    },
  },

  bookings: {
    list: async (params) => {
      const response = await axiosInstance.get('/admin/bookings', { params });
      return response.data;
    },

    confirm: async (id) => {
      const response = await axiosInstance.patch(`/admin/bookings/${id}/confirm`);
      return response.data;
    },

    cancel: async (id) => {
      const response = await axiosInstance.patch(`/admin/bookings/${id}/cancel`);
      return response.data;
    },

    forcePay: async (id) => {
      const response = await axiosInstance.patch(`/admin/bookings/${id}/force-pay`);
      return response.data;
    },

    delete: async (id) => {
      const response = await axiosInstance.delete(`/admin/bookings/${id}`);
      return response.data;
    },
  },
};

export default adminService;
