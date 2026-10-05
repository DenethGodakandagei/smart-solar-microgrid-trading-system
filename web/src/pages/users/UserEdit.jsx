import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import UserForm from '../../components/backoffice/UserForm';
import { updateUser, getUsers } from '../../services/userService';
import { useNotification } from '../../context/NotificationContext';
import Button from '../../components/common/Button';

export default function UserEdit() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { showSuccess, showError } = useNotification();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [user, setUser] = useState(null);

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const users = await getUsers();
        const found = Array.isArray(users) ? users.find(u => u.id === id || u._id === id) : users?.items?.find(u => u.id === id || u._id === id);
        if (found) setUser(found);
        else showError('User not found');
      } catch (err) {
        showError('Failed to load user details.');
      }
    };
    fetchUser();
  }, [id, showError]);

  const handleSaveUser = async (formData) => {
    setIsSubmitting(true);
    try {
      await updateUser(id, formData);
      showSuccess('User account updated successfully.');
      navigate('/backoffice/users');
    } catch (err) {
      throw err;
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!user) return <div className="p-4">Loading user...</div>;

  return (
    <div className="space-y-6 max-w-2xl mx-auto">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">Edit User</h2>
          <p className="text-sm text-slate-500 mt-1">Update details for {user.username}</p>
        </div>
        <Button variant="secondary" onClick={() => navigate('/backoffice/users')}>Back to List</Button>
      </div>
      <div className="bg-white p-6 rounded-lg shadow border border-slate-200">
        <UserForm user={user} onSave={handleSaveUser} onCancel={() => navigate('/backoffice/users')} isLoading={isSubmitting} />
      </div>
    </div>
  );
}
