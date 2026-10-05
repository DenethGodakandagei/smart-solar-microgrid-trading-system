import axiosClient from './axiosClient';

/**
 * Microgrid Node Management API
 * CRUD operations for solar/grid nodes, schedule updates, and deactivation.
 */

export const getNodes = async (params = {}) => {
  const response = await axiosClient.get('/nodes', { params });
  return response.data;
};

export const getNodeById = async (id) => {
  const response = await axiosClient.get(`/nodes/${id}`);
  return response.data;
};

export const createNode = async (data) => {
  const response = await axiosClient.post('/nodes', data);
  return response.data;
};

export const updateNode = async (id, data) => {
  const response = await axiosClient.put(`/nodes/${id}`, data);
  return response.data;
};

export const updateNodeSchedule = async (id, scheduleData) => {
  const response = await axiosClient.put(`/nodes/${id}/schedule`, scheduleData);
  return response.data;
};

export const deactivateNode = async (id) => {
  const response = await axiosClient.patch(`/nodes/${id}/deactivate`);
  return response.data;
};

export const getNodeSlots = async (nodeId, params = {}) => {
  const response = await axiosClient.get(`/nodes/${nodeId}/slots`, { params });
  return response.data;
};
