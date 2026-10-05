import React from 'react';
import DataTable from '../common/DataTable';
import StatusBadge from '../common/StatusBadge';
import Button from '../common/Button';
import { HiOutlineEye, HiOutlinePencilSquare, HiOutlineNoSymbol } from 'react-icons/hi2';

export default function UserTable({ users, isLoading, onEdit, onDeactivate, onView }) {
  const columns = [
    {
      header: 'User',
      key: 'username',
      render: (user) => (
        <div>
          <div className="font-semibold text-slate-800">{user.fullName || user.name || user.username}</div>
          <div className="text-xs text-slate-400">@{user.username}</div>
        </div>
      ),
    },
    {
      header: 'Email',
      key: 'email',
      render: (user) => <span className="text-slate-600 text-xs">{user.email || '—'}</span>,
    },
    {
      header: 'Role',
      key: 'role',
      render: (user) => (
        <span className="inline-flex items-center px-2 py-0.5 rounded-md text-xs font-semibold bg-slate-100 text-slate-700 border border-slate-200">
          {user.role}
        </span>
      ),
    },
    {
      header: 'Status',
      key: 'status',
      render: (user) => <StatusBadge status={user.status || 'Active'} />,
    },
    {
      header: 'Actions',
      key: 'actions',
      align: 'right',
      render: (user) => (
        <div className="flex items-center justify-end gap-1.5">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onEdit(user)}
            title="Edit user details"
          >
            <HiOutlinePencilSquare className="w-4 h-4 text-blue-600" />
            <span className="sr-only">Edit</span>
          </Button>
          {user.status !== 'Inactive' && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onDeactivate(user)}
              title="Deactivate account"
            >
              <HiOutlineNoSymbol className="w-4 h-4 text-red-500 hover:text-red-700" />
              <span className="sr-only">Deactivate</span>
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <DataTable
      columns={columns}
      data={users}
      keyField="id"
      isLoading={isLoading}
      emptyTitle="No system users found"
      emptyDescription="Create a new Backoffice or Grid Operator account using the button above."
    />
  );
}
