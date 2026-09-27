import axiosClient from './axiosClient';

/**
 * Authentication API
 * Handles login, logout, and current-user retrieval.
 */

export const login = async (credentials) => {
  const response = await axiosClient.post('/auth/login', credentials);
  return response.data;
};

export const logout = async () => {
  const response = await axiosClient.post('/auth/logout');
  return response.data;
};

export const getCurrentUser = async () => {
  const response = await axiosClient.get('/auth/me');
  return response.data;
};
