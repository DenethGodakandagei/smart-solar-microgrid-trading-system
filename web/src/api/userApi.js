import axiosClient from './axiosClient';

/**
 * User Management API
 * CRUD operations for Backoffice and Grid Operator accounts.
 */

export const getUsers = async (params = {}) => {
  const response = await axiosClient.get('/users', { params });
  return response.data;
};

export const getUserById = async (id) => {
  const response = await axiosClient.get(`/users/${id}`);
  return response.data;
};

export const createUser = async (data) => {
  const response = await axiosClient.post('/users', data);
  return response.data;
};

export const updateUser = async (id, data) => {
  const response = await axiosClient.put(`/users/${id}`, data);
  return response.data;
};

export const deactivateUser = async (id) => {
  const response = await axiosClient.patch(`/users/${id}/deactivate`);
  return response.data;
};
