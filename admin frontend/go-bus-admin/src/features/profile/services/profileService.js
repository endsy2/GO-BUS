import axiosInstance from 'services/axiosConfig';

const profileService = {
  // Get current user profile
  getProfile: async () => {
    const response = await axiosInstance.get('/users/profile');
    return response.data;
  },

  // Update current user profile
  updateProfile: async (userId, profileData) => {
    // Only send fields that are allowed to be updated.
    // gender maps to a backend enum (Gender) — an empty string can't be coerced
    // to an enum and triggers a 500, so send null when it isn't set.
    const updatePayload = {
      userName: profileData.userName,
      fullName: profileData.fullName,
      phone: profileData.phone || null,
      gender: profileData.gender ? profileData.gender : null,
    };

    const response = await axiosInstance.put(`/users/${userId}`, updatePayload);
    return response.data;
  },

  // Upload profile image
  uploadProfileImage: async (file) => {
    const formData = new FormData();
    formData.append('file', file);

    // Content-Type is intentionally not set here — the axios interceptor strips it
    // for FormData so the browser adds `multipart/form-data; boundary=…`.
    const response = await axiosInstance.post('/profile/image', formData);
    return response.data;
  },

  // Delete profile image
  deleteProfileImage: async () => {
    const response = await axiosInstance.delete('/profile/image');
    return response.data;
  },

  // Get profile image URL
  getProfileImageUrl: async () => {
    const response = await axiosInstance.get('/profile/image/url');
    return response.data;
  },
};

export default profileService;
