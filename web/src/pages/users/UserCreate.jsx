import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import UserForm from '../../components/backoffice/UserForm';
import { createUser } from '../../services/userService';
import { useNotification } from '../../context/NotificationContext';
import Button from '../../components/common/Button';

export default function UserCreate() {
  const navigate = useNavigate();
  const { showSuccess } = useNotification();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSaveUser = async (formData) => {
    setIsSubmitting(true);
    try {
      await createUser(formData);
      showSuccess('New user account created successfully.');
      navigate('/backoffice/users');
    } catch (err) {
      throw err;
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6 max-w-2xl mx-auto">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">Create User</h2>
          <p className="text-sm text-slate-500 mt-1">Add a new Backoffice or Grid Operator account</p>
        </div>
        <Button variant="secondary" onClick={() => navigate('/backoffice/users')}>Back to List</Button>
      </div>
      <div className="bg-white p-6 rounded-lg shadow border border-slate-200">
        <UserForm onSave={handleSaveUser} onCancel={() => navigate('/backoffice/users')} isLoading={isSubmitting} />
      </div>
    </div>
  );
}
