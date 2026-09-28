import React, { useState, useEffect } from 'react';
import Modal from '../common/Modal';
import Input from '../common/Input';
import Select from '../common/Select';
import Button from '../common/Button';
import ErrorAlert from '../common/ErrorAlert';
import { STATUS } from '../../utils/constants';

export default function NodeForm({ isOpen, onClose, onSave, node = null, isLoading = false }) {
  const isEditing = !!node;

  const [formData, setFormData] = useState({
    name: '',
    nodeCode: '',
    latitude: '',
    longitude: '',
    capacityKwh: '',
    batterySlotsCount: 4,
    status: STATUS.ACTIVE,
  });
  const [error, setError] = useState('');

  useEffect(() => {
    if (node) {
      setFormData({
        name: node.name || '',
        nodeCode: node.nodeCode || node.code || '',
        latitude: node.latitude || node.gpsLatitude || '',
        longitude: node.longitude || node.gpsLongitude || '',
        capacityKwh: node.capacityKwh ?? node.capacity ?? '',
        batterySlotsCount: node.batterySlotsCount ?? node.totalSlots ?? 4,
        status: node.status || STATUS.ACTIVE,
      });
    } else {
      setFormData({
        name: '',
        nodeCode: '',
        latitude: '',
        longitude: '',
        capacityKwh: '',
        batterySlotsCount: 4,
        status: STATUS.ACTIVE,
      });
    }
    setError('');
  }, [node, isOpen]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!formData.name.trim()) {
      setError('Node identification name is required.');
      return;
    }
    if (!formData.latitude || !formData.longitude) {
      setError('GPS Latitude and Longitude are required for microgrid dispatching.');
      return;
    }

    try {
      await onSave(formData, node?.id || node?._id);
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Failed to save microgrid node. Verify API response.';
      setError(msg);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditing ? 'Configure Microgrid Hub' : 'Register New Microgrid Node'}
      subtitle="Define geographical coordinates, kW/h solar capacity, and battery storage bays"
      maxWidth="max-w-lg"
    >
      {error && <ErrorAlert message={error} onClose={() => setError('')} className="mb-4" />}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Node Name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
            placeholder="e.g. Colombo West Hub 01"
          />
          <Input
            label="Node Code / Identifier"
            name="nodeCode"
            value={formData.nodeCode}
            onChange={handleChange}
            placeholder="e.g. NODE-CW-01"
          />
        </div>

        {/* GPS location coordinates */}
        <div className="p-3.5 bg-slate-50 border border-slate-200 rounded-xl space-y-3">
          <span className="text-xs font-semibold text-slate-700 block uppercase tracking-wider">
            GPS Coordinates (Dec Degrees)
          </span>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <Input
              label="Latitude"
              type="number"
              step="0.000001"
              name="latitude"
              value={formData.latitude}
              onChange={handleChange}
              required
              placeholder="e.g. 6.927079"
            />
            <Input
              label="Longitude"
              type="number"
              step="0.000001"
              name="longitude"
              value={formData.longitude}
              onChange={handleChange}
              required
              placeholder="e.g. 79.861244"
            />
          </div>
        </div>

        {/* Capacity and battery slots */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Rated Capacity (kW/h)"
            type="number"
            step="0.1"
            name="capacityKwh"
            value={formData.capacityKwh}
            onChange={handleChange}
            required
            placeholder="e.g. 250.0"
            helperText="Total solar dispatch capacity"
          />

          <Input
            label="Total Battery Storage Slots"
            type="number"
            min="1"
            max="64"
            name="batterySlotsCount"
            value={formData.batterySlotsCount}
            onChange={handleChange}
            required
            placeholder="e.g. 8"
            helperText="Physical storage rack slots"
          />
        </div>

        <Select
          label="Operational Status"
          name="status"
          value={formData.status}
          onChange={handleChange}
          options={[
            { value: STATUS.ACTIVE, label: 'Active (Operational)' },
            { value: STATUS.INACTIVE, label: 'Inactive (Offline)' },
          ]}
        />

        <div className="flex justify-end gap-3 pt-4 border-t border-slate-100">
          <Button variant="secondary" onClick={onClose} disabled={isLoading}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" isLoading={isLoading}>
            {isEditing ? 'Save Node' : 'Register Node'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
