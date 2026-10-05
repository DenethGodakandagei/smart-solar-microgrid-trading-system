import axiosClient from './axiosClient';

/**
 * Prosumer Management API
 * CRUD operations for prosumer profiles, using NIC as the primary identifier.
 */

export const getProsumers = async (params = {}) => {
  const response = await axiosClient.get('/prosumers', { params });
  return response.data;
};

export const getProsumerByNic = async (nic) => {
  const response = await axiosClient.get(`/prosumers/${nic}`);
  return response.data;
};

export const createProsumer = async (data) => {
  const response = await axiosClient.post('/prosumers', data);
  return response.data;
};

export const updateProsumer = async (nic, data) => {
  const response = await axiosClient.put(`/prosumers/${nic}`, data);
  return response.data;
};

export const deactivateProsumer = async (nic) => {
  const response = await axiosClient.patch(`/prosumers/${nic}/deactivate`);
  return response.data;
};

export const reactivateProsumer = async (nic) => {
  const response = await axiosClient.patch(`/prosumers/${nic}/reactivate`);
  return response.data;
};
