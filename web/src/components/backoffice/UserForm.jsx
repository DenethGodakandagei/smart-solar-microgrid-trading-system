import React, { useState, useEffect } from 'react';
import Modal from '../common/Modal';
import Input from '../common/Input';
import Select from '../common/Select';
import Button from '../common/Button';
import ErrorAlert from '../common/ErrorAlert';
import { ROLES, STATUS } from '../../utils/constants';

export default function UserForm({ onCancel, onSave, user = null, isLoading = false }) {
  const isEditing = !!user;

  const [formData, setFormData] = useState({
    username: '',
    fullName: '',
    email: '',
    role: ROLES.BACKOFFICE,
    status: STATUS.ACTIVE,
    password: '',
  });
  const [error, setError] = useState('');

  useEffect(() => {
    if (user) {
      setFormData({
        username: user.username || '',
        fullName: user.fullName || user.name || '',
        email: user.email || '',
        role: user.role || ROLES.BACKOFFICE,
        status: user.status || STATUS.ACTIVE,
        password: '', // leave empty when editing unless changing
      });
    } else {
      setFormData({
        username: '',
        fullName: '',
        email: '',
        role: ROLES.BACKOFFICE,
        status: STATUS.ACTIVE,
        password: '',
      });
    }
    setError('');
  }, [user, true]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!formData.username.trim() || !formData.email.trim()) {
      setError('Username and Email are required.');
      return;
    }

    if (!isEditing && !formData.password) {
      setError('Password is required for new accounts.');
      return;
    }

    try {
      await onSave(formData, user?.id || user?._id);
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Failed to save user. Please check API response.';
      setError(msg);
    }
  };

  return (
    <div className="space-y-4">
      {error && <ErrorAlert message={error} onCancel={() => setError('')} className="mb-4" />}

      <form onSubmit={handleSubmit} className="space-y-4">
        <Input
          label="Username"
          name="username"
          value={formData.username}
          onChange={handleChange}
          required
          disabled={isEditing}
          placeholder="e.g. jdoe"
        />

        <Input
          label="Full Name"
          name="fullName"
          value={formData.fullName}
          onChange={handleChange}
          required
          placeholder="e.g. John Doe"
        />

        <Input
          label="Email Address"
          type="email"
          name="email"
          value={formData.email}
          onChange={handleChange}
          required
          placeholder="e.g. john@microgrid.lk"
        />

        <Select
          label="System Role"
          name="role"
          value={formData.role}
          onChange={handleChange}
          required
          options={[
            { value: ROLES.BACKOFFICE, label: 'Backoffice Officer' },
            { value: ROLES.GRID_OPERATOR, label: 'Grid Operator' },
          ]}
        />

        <Select
          label="Account Status"
          name="status"
          value={formData.status}
          onChange={handleChange}
          required
          options={[
            { value: STATUS.ACTIVE, label: 'Active' },
            { value: STATUS.INACTIVE, label: 'Inactive' },
          ]}
        />

        <Input
          label={isEditing ? 'New Password (leave blank to keep current)' : 'Password'}
          type="password"
          name="password"
          value={formData.password}
          onChange={handleChange}
          required={!isEditing}
          placeholder="••••••••"
        />

        <div className="flex justify-end gap-3 pt-4 border-t border-slate-100">
          <Button variant="secondary" onClick={onCancel} disabled={isLoading}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" isLoading={isLoading}>
            {isEditing ? 'Save Changes' : 'Create User'}
          </Button>
        </div>
      </form>
    </div>
  );
}
