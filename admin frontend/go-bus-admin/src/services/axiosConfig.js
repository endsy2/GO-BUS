import axios from 'axios';

const BASE_URL = process.env.REACT_APP_BASE_URL || 'https://go-bus-gateway-service-production.up.railway.app';

// Create axios instance with /api prefix
const axiosInstance = axios.create({
  baseURL: `${BASE_URL}/api`,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Global state for token refresh
let isRefreshing = false;
let failedQueue = [];
let unauthorizedHandler = null;

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

export const setUnauthorizedHandler = (handler) => {
  unauthorizedHandler = handler;
};

// Request interceptor - Add auth token
axiosInstance.interceptors.request.use(
  (config) => {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const token = user?.token || user?.accessToken;
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // For multipart uploads, drop the default JSON Content-Type so the browser
    // sets `multipart/form-data` with the required boundary automatically.
    if (config.data instanceof FormData) {
      config.headers.delete('Content-Type');
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - Handle token refresh
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Handle 401 errors
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // Queue the request while token is being refreshed
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then(token => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return axiosInstance(originalRequest);
          })
          .catch(err => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        const refreshToken = user?.refreshToken;

        if (!refreshToken) {
          throw new Error('No refresh token available');
        }

        // Refresh token request
        const response = await axios.post(`${BASE_URL}/api/auth/refresh`, {
          refreshToken
        });

        const newTokenData = response.data.data || response.data;
        const newToken = newTokenData.token || newTokenData.accessToken;

        // Update user in localStorage
        const updatedUser = {
          ...user,
          token: newToken,
          accessToken: newToken,
          refreshToken: newTokenData.refreshToken || refreshToken
        };
        localStorage.setItem('user', JSON.stringify(updatedUser));

        // Process queued requests
        processQueue(null, newToken);
        isRefreshing = false;

        // Retry original request
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return axiosInstance(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        isRefreshing = false;

        // Clear the stale session immediately so queued requests don't retry
        // with an expired token while the logout dialog is still visible.
        localStorage.removeItem('user');

        if (unauthorizedHandler) {
          unauthorizedHandler();
        }

        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
