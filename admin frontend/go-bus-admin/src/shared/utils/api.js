// Global event emitter for 401 errors
let unauthorizedHandler = null;
let isRefreshing = false;
let failedQueue = [];

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

const refreshToken = async () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const refreshToken = user?.refreshToken;

  if (!refreshToken) {
    throw new Error('No refresh token available');
  }

  const response = await fetch(`${process.env.REACT_APP_BASE_URL || 'http://localhost:8080'}/api/auth/refresh`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ refreshToken })
  });

  if (!response.ok) {
    throw new Error('Token refresh failed');
  }

  const data = await response.json();
  const newTokenData = data.data || data;

  const updatedUser = {
    ...user,
    token: newTokenData.token || newTokenData.accessToken,
    accessToken: newTokenData.token || newTokenData.accessToken,
    refreshToken: newTokenData.refreshToken || refreshToken
  };
  
  localStorage.setItem('user', JSON.stringify(updatedUser));
  
  return updatedUser.token || updatedUser.accessToken;
};

export const handleApiResponse = async (response, originalRequest) => {
  if (response.status === 401) {
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      }).then(token => {
        const retryConfig = {
          ...originalRequest,
          headers: {
            ...originalRequest.headers,
            'Authorization': `Bearer ${token}`
          }
        };
        return fetch(originalRequest.url, retryConfig);
      }).catch(err => {
        return Promise.reject(err);
      });
    }

    isRefreshing = true;

    try {
      const newToken = await refreshToken();
      processQueue(null, newToken);
      isRefreshing = false;

      const retryConfig = {
        ...originalRequest,
        headers: {
          ...originalRequest.headers,
          'Authorization': `Bearer ${newToken}`
        }
      };
      return fetch(originalRequest.url, retryConfig);
    } catch (error) {
      processQueue(error, null);
      isRefreshing = false;
      
      if (unauthorizedHandler) {
        unauthorizedHandler();
      }
      
      return response;
    }
  }
  
  return response;
};

export const apiRequest = async (url, options = {}) => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const token = user?.token || user?.accessToken;

  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const requestConfig = {
    ...options,
    headers,
  };

  const response = await fetch(url, requestConfig);

  const originalRequest = {
    url,
    ...requestConfig
  };

  return await handleApiResponse(response, originalRequest);
};
