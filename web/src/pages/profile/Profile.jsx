import React, { useState } from 'react';
import Button from '../../components/common/Button';
import { useAuth } from '../../hooks/useAuth';
import { useNotification } from '../../context/NotificationContext';
import { HiOutlineUser, HiOutlineMail, HiOutlineBadgeCheck } from 'react-icons/hi';

export default function Profile() {
  const { user } = useAuth();
  const { showSuccess } = useNotification();
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    fullName: user?.fullName || '',
    email: user?.email || '',
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // Normally we'd call an API here to update profile
    showSuccess('Profile updated successfully.');
    setIsEditing(false);
  };

  if (!user) return <div className="p-4">Loading profile...</div>;

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">
            My Profile
          </h2>
          <p className="text-sm text-slate-500 mt-1">
            Manage your account settings and preferences
          </p>
        </div>
      </div>

      <div className="bg-white shadow rounded-lg border border-slate-200 overflow-hidden">
        <div className="p-6 sm:p-8">
          <div className="flex items-center gap-4 mb-8">
            <div className="h-16 w-16 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 text-2xl font-bold">
              {user.username ? user.username.charAt(0).toUpperCase() : 'U'}
            </div>
            <div>
              <h3 className="text-xl font-semibold text-slate-900">{user.username}</h3>
              <p className="text-slate-500 flex items-center gap-1">
                <HiOutlineBadgeCheck className="w-4 h-4" />
                {user.role}
              </p>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Username
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <HiOutlineUser className="text-slate-400 w-5 h-5" />
                  </div>
                  <input
                    type="text"
                    disabled
                    className="pl-10 block w-full rounded-md border-slate-300 bg-slate-50 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm text-slate-500 cursor-not-allowed py-2 border"
                    value={user.username || ''}
                  />
                </div>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Role
                </label>
                <input
                  type="text"
                  disabled
                  className="block w-full rounded-md border-slate-300 bg-slate-50 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm text-slate-500 cursor-not-allowed py-2 px-3 border"
                  value={user.role || ''}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Full Name
                </label>
                <input
                  type="text"
                  name="fullName"
                  disabled={!isEditing}
                  value={formData.fullName}
                  onChange={handleChange}
                  className={`block w-full rounded-md shadow-sm sm:text-sm py-2 px-3 border ${
                    isEditing 
                      ? "border-slate-300 focus:border-blue-500 focus:ring-blue-500 bg-white text-slate-900" 
                      : "border-slate-300 bg-slate-50 text-slate-500 cursor-not-allowed"
                  }`}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Email Address
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <HiOutlineMail className="text-slate-400 w-5 h-5" />
                  </div>
                  <input
                    type="email"
                    name="email"
                    disabled={!isEditing}
                    value={formData.email}
                    onChange={handleChange}
                    className={`pl-10 block w-full rounded-md shadow-sm sm:text-sm py-2 border ${
                      isEditing 
                        ? "border-slate-300 focus:border-blue-500 focus:ring-blue-500 bg-white text-slate-900" 
                        : "border-slate-300 bg-slate-50 text-slate-500 cursor-not-allowed"
                    }`}
                  />
                </div>
              </div>
            </div>

            <div className="flex justify-end gap-3 pt-4 border-t border-slate-200">
              {isEditing ? (
                <>
                  <Button type="button" variant="secondary" onClick={() => setIsEditing(false)}>
                    Cancel
                  </Button>
                  <Button type="submit" variant="primary">
                    Save Changes
                  </Button>
                </>
              ) : (
                <Button type="button" variant="primary" onClick={() => setIsEditing(true)}>
                  Edit Profile
                </Button>
              )}
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
