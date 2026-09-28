import axiosClient from './axiosClient';

/**
 * Pending Activation API
 * Manages pending user activation requests.
 */

export const getPendingActivations = async (params = {}) => {
  const response = await axiosClient.get('/activations/pending', { params });
  return response.data;
};

export const getActivationById = async (id) => {
  const response = await axiosClient.get(`/activations/${id}`);
  return response.data;
};

export const approveActivation = async (id) => {
  const response = await axiosClient.post(`/activations/${id}/approve`);
  return response.data;
};

export const rejectActivation = async (id, data = {}) => {
  const response = await axiosClient.post(`/activations/${id}/reject`, data);
  return response.data;
};
