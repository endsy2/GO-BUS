import axiosInstance from 'services/axiosConfig';

const userService = {
  // Get user profile
  getProfile: async () => {
    const response = await axiosInstance.get('/users/profile');
    return response.data;
  },

  // Flexible user search via the specification endpoint.
  // Lives on UserController at GET /api/users/specification (NOT /admin/users — that
  // controller has no /specification route, so the path matched /{id} and 500'd).
  // params: { pageStart, pageSize, isEmployee, email, phone, username, isActive, isDeleted, ... }
  getBySpecification: async (params = {}) => {
    const response = await axiosInstance.get('/users/specification', { params });
    return response.data;
  },

  // Update user profile
  updateProfile: async (profileData) => {
    const response = await axiosInstance.put('/users/profile', profileData);
    return response.data;
  },

  // Create user
  createUser: async (userData) => {
    const response = await axiosInstance.post('/users', userData);
    return response.data;
  },

  // Assign role
  assignRole: async (userId, roleData) => {
    const response = await axiosInstance.post(`/users/${userId}/role`, roleData);
    return response.data;
  },
};

export default userService;
