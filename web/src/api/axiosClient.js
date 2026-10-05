import axios from 'axios';
import { handleMockRequest } from './mockAdapter';

// Create a configured Axios instance
const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:5000/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 5000,
});

// ── Request Interceptor ──
// Attaches the JWT token to every outgoing request
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ── Response Interceptor ──
// If live backend is reachable, returns response normally.
// If network connection fails (backend server is offline), falls back to mockAdapter.
axiosClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    // Check if network error occurred (backend offline or unreachable)
    if (!error.response && error.config) {
      console.warn(
        `[SmartSolar Client] Live C# Web API unreachable at ${axiosClient.defaults.baseURL}. Activating mock service fallback for: ${error.config.url}`
      );
      try {
        const mockResult = await handleMockRequest(error.config);
        return mockResult;
      } catch (mockError) {
        return Promise.reject(mockError);
      }
    }

    const { status } = error.response;

    switch (status) {
      case 401:
        // Token expired or invalid — clear state, redirect to login
        localStorage.removeItem('authToken');
        localStorage.removeItem('authUser');
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
        break;

      case 403:
        console.warn('Access denied (403)');
        break;

      case 500:
        console.error('Server error (500):', error.response.data);
        break;

      default:
        break;
    }

    return Promise.reject(error);
  }
);

export default axiosClient;
