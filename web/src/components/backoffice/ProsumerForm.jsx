import React, { useState, useEffect } from 'react';
import Modal from '../common/Modal';
import Input from '../common/Input';
import Select from '../common/Select';
import Button from '../common/Button';
import ErrorAlert from '../common/ErrorAlert';
import { STATUS } from '../../utils/constants';

export default function ProsumerForm({ isOpen, onClose, onSave, prosumer = null, isLoading = false }) {
  const isEditing = !!prosumer;

  const [formData, setFormData] = useState({
    nic: '',
    fullName: '',
    email: '',
    phone: '',
    address: '',
    gridNodeId: '',
    solarCapacityKw: '',
    batteryStorageKwh: '',
    status: STATUS.ACTIVE,
  });
  const [error, setError] = useState('');

  useEffect(() => {
    if (prosumer) {
      setFormData({
        nic: prosumer.nic || '',
        fullName: prosumer.fullName || prosumer.name || '',
        email: prosumer.email || '',
        phone: prosumer.phone || prosumer.contactNumber || '',
        address: prosumer.address || '',
        gridNodeId: prosumer.gridNodeId || prosumer.nodeId || '',
        solarCapacityKw: prosumer.solarCapacityKw ?? '',
        batteryStorageKwh: prosumer.batteryStorageKwh ?? '',
        status: prosumer.status || STATUS.ACTIVE,
      });
    } else {
      setFormData({
        nic: '',
        fullName: '',
        email: '',
        phone: '',
        address: '',
        gridNodeId: '',
        solarCapacityKw: '',
        batteryStorageKwh: '',
        status: STATUS.ACTIVE,
      });
    }
    setError('');
  }, [prosumer, isOpen]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!formData.nic.trim()) {
      setError('National Identity Card (NIC) is required as the primary identifier.');
      return;
    }
    if (!formData.fullName.trim()) {
      setError('Prosumer full name is required.');
      return;
    }

    try {
      await onSave(formData, prosumer?.nic || formData.nic);
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Failed to save prosumer profile. Verify API parameters.';
      setError(msg);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditing ? 'Update Prosumer Profile' : 'Register New Prosumer'}
      subtitle="NIC acts as the unique primary identifier across the solar microgrid network"
      maxWidth="max-w-lg"
    >
      {error && <ErrorAlert message={error} onClose={() => setError('')} className="mb-4" />}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Primary identifier - NIC */}
        <Input
          label="National Identity Card (NIC)"
          name="nic"
          value={formData.nic}
          onChange={handleChange}
          required
          disabled={isEditing}
          helperText={isEditing ? 'NIC cannot be modified as it is the primary identifier.' : 'Enter unique National Identity Card number'}
          placeholder="e.g. 199012345678 or 901234567V"
        />

        <Input
          label="Full Legal Name"
          name="fullName"
          value={formData.fullName}
          onChange={handleChange}
          required
          placeholder="e.g. A. B. Silva"
        />

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Email Address"
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="e.g. silva@solar.lk"
          />

          <Input
            label="Contact Phone"
            type="tel"
            name="phone"
            value={formData.phone}
            onChange={handleChange}
            placeholder="+94 77 123 4567"
          />
        </div>

        <Input
          label="Premises Address"
          name="address"
          value={formData.address}
          onChange={handleChange}
          placeholder="e.g. 42 Solar Way, Colombo 07"
        />

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Installed Solar Capacity (kW)"
            type="number"
            step="0.1"
            name="solarCapacityKw"
            value={formData.solarCapacityKw}
            onChange={handleChange}
            placeholder="e.g. 5.5"
          />

          <Input
            label="Battery Storage Capacity (kWh)"
            type="number"
            step="0.1"
            name="batteryStorageKwh"
            value={formData.batteryStorageKwh}
            onChange={handleChange}
            placeholder="e.g. 10.0"
          />
        </div>

        <Select
          label="Status"
          name="status"
          value={formData.status}
          onChange={handleChange}
          options={[
            { value: STATUS.ACTIVE, label: 'Active' },
            { value: STATUS.INACTIVE, label: 'Inactive' },
          ]}
        />

        <div className="flex justify-end gap-3 pt-4 border-t border-slate-100">
          <Button variant="secondary" onClick={onClose} disabled={isLoading}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" isLoading={isLoading}>
            {isEditing ? 'Save Profile' : 'Register Prosumer'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
