import React, { useState, useEffect } from 'react';
import UserTable from '../../components/backoffice/UserTable';
import UserForm from '../../components/backoffice/UserForm';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import SearchBar from '../../components/common/SearchBar';
import Button from '../../components/common/Button';
import ErrorAlert from '../../components/common/ErrorAlert';
import { useNotification } from '../../context/NotificationContext';
import { getUsers, createUser, updateUser, deactivateUser } from '../../api/userApi';
import { HiOutlineUserPlus, HiOutlineArrowPath } from 'react-icons/hi2';

export default function UsersPage() {
  const [users, setUsers] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // Modals & Dialogs
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [deactivateTarget, setDeactivateTarget] = useState(null);

  const { showSuccess, showError } = useNotification();

  const fetchUsers = async () => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const data = await getUsers(searchTerm ? { search: searchTerm } : {});
      // Support array response or paginated wrapper ({ items: [] })
      setUsers(Array.isArray(data) ? data : data?.items || []);
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Failed to fetch user accounts from Web API.';
      setErrorMessage(msg);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [searchTerm]);

  const handleOpenCreate = () => {
    setSelectedUser(null);
    setIsFormOpen(true);
  };

  const handleOpenEdit = (user) => {
    setSelectedUser(user);
    setIsFormOpen(true);
  };

  const handleSaveUser = async (formData, id) => {
    setIsSubmitting(true);
    try {
      if (id) {
        await updateUser(id, formData);
        showSuccess('User account updated successfully.');
      } else {
        await createUser(formData);
        showSuccess('New user account created successfully.');
      }
      setIsFormOpen(false);
      fetchUsers();
    } catch (err) {
      // Re-throw so modal form displays the exact API message
      throw err;
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleConfirmDeactivate = async () => {
    if (!deactivateTarget) return;

    setIsSubmitting(true);
    try {
      await deactivateUser(deactivateTarget.id || deactivateTarget._id);
      showSuccess(`Account '${deactivateTarget.username}' has been deactivated.`);
      setDeactivateTarget(null);
      fetchUsers();
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Failed to deactivate account.';
      showError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">
            System User Management
          </h2>
          <p className="text-sm text-slate-500 mt-1">
            Provision and manage Backoffice officers and Grid Operator accounts
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="secondary"
            size="sm"
            onClick={fetchUsers}
            isLoading={isLoading}
            icon={HiOutlineArrowPath}
          >
            Refresh
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={handleOpenCreate}
            icon={HiOutlineUserPlus}
          >
            Create User
          </Button>
        </div>
      </div>

      {errorMessage && (
        <ErrorAlert
          title="User Service Notice"
          message={errorMessage}
          onClose={() => setErrorMessage('')}
        />
      )}

      {/* Filter / Search Bar */}
      <div className="flex items-center justify-between gap-4">
        <SearchBar
          value={searchTerm}
          onChange={setSearchTerm}
          onClear={() => setSearchTerm('')}
          placeholder="Search by username, full name, or email..."
        />
      </div>

      {/* User Accounts Table */}
      <UserTable
        users={users}
        isLoading={isLoading}
        onEdit={handleOpenEdit}
        onDeactivate={(user) => setDeactivateTarget(user)}
      />

      {/* User Form Modal (Create / Edit) */}
      <UserForm
        isOpen={isFormOpen}
        onClose={() => setIsFormOpen(false)}
        onSave={handleSaveUser}
        user={selectedUser}
        isLoading={isSubmitting}
      />

      {/* Confirm Deactivation Dialog */}
      <ConfirmDialog
        isOpen={!!deactivateTarget}
        onClose={() => setDeactivateTarget(null)}
        onConfirm={handleConfirmDeconfirmDeactivate => handleConfirmDeactivate()}
        title="Deactivate User Account"
        message={`Are you sure you want to deactivate ${deactivateTarget?.fullName || deactivateTarget?.username}? The user will be barred from authenticating.`}
        confirmText="Deactivate Account"
        variant="danger"
        isLoading={isSubmitting}
      />
    </div>
  );
}
