import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getUsers } from '../../services/userService';
import { useNotification } from '../../context/NotificationContext';
import Button from '../../components/common/Button';

export default function UserView() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { showError } = useNotification();
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

  if (!user) return <div className="p-4">Loading user...</div>;

  return (
    <div className="space-y-6 max-w-2xl mx-auto">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">User Details</h2>
          <p className="text-sm text-slate-500 mt-1">View account information</p>
        </div>
        <div className="flex gap-2">
          <Button variant="secondary" onClick={() => navigate('/backoffice/users')}>Back to List</Button>
          <Button variant="primary" onClick={() => navigate(`/backoffice/users/edit/${id}`)}>Edit User</Button>
        </div>
      </div>
      <div className="bg-white p-6 rounded-lg shadow border border-slate-200 space-y-4">
        <div><label className="text-sm text-slate-500">Username</label><p className="font-medium text-slate-900">{user.username}</p></div>
        <div><label className="text-sm text-slate-500">Full Name</label><p className="font-medium text-slate-900">{user.fullName || user.name}</p></div>
        <div><label className="text-sm text-slate-500">Email</label><p className="font-medium text-slate-900">{user.email}</p></div>
        <div><label className="text-sm text-slate-500">Role</label><p className="font-medium text-slate-900">{user.role}</p></div>
        <div><label className="text-sm text-slate-500">Status</label><p className="font-medium text-slate-900">{user.status}</p></div>
      </div>
    </div>
  );
}
