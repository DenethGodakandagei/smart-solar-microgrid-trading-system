import axiosClient from './axiosClient';

/**
 * Booking Management API
 * CRUD operations for energy slot bookings, history, and dashboard stats.
 */

export const getBookings = async (params = {}) => {
  const response = await axiosClient.get('/bookings', { params });
  return response.data;
};

export const getBookingById = async (id) => {
  const response = await axiosClient.get(`/bookings/${id}`);
  return response.data;
};

export const createBooking = async (data) => {
  const response = await axiosClient.post('/bookings', data);
  return response.data;
};

export const updateBooking = async (id, data) => {
  const response = await axiosClient.put(`/bookings/${id}`, data);
  return response.data;
};

export const cancelBooking = async (id) => {
  const response = await axiosClient.patch(`/bookings/${id}/cancel`);
  return response.data;
};

export const getBookingHistory = async (params = {}) => {
  const response = await axiosClient.get('/bookings/history', { params });
  return response.data;
};

export const getBookingStats = async () => {
  const response = await axiosClient.get('/bookings/stats');
  return response.data;
};

export const getBackofficeDashboard = async () => {
  const response = await axiosClient.get('/dashboard/backoffice');
  return response.data;
};

export const getOperatorDashboard = async () => {
  const response = await axiosClient.get('/dashboard/operator');
  return response.data;
};
