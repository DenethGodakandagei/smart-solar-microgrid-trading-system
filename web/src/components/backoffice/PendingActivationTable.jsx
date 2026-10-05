import React from 'react';
import DataTable from '../common/DataTable';
import Button from '../common/Button';
import StatusBadge from '../common/StatusBadge';
import { formatDate } from '../../utils/formatDate';
import { HiCheck, HiXMark } from 'react-icons/hi2';

export default function PendingActivationTable({
  activations,
  isLoading,
  onApprove,
  onReject,
}) {
  const columns = [
    {
      header: 'Applicant',
      key: 'name',
      render: (item) => (
        <div>
          <div className="font-semibold text-slate-800">{item.fullName || item.name || 'Unnamed Applicant'}</div>
          <div className="text-xs text-slate-400">{item.email || item.contactNumber || '—'}</div>
        </div>
      ),
    },
    {
      header: 'Identifier / NIC',
      key: 'nic',
      render: (item) => (
        <span className="font-mono text-xs px-2 py-0.5 rounded bg-slate-100 text-slate-700 border border-slate-200">
          {item.nic || item.identifier || '—'}
        </span>
      ),
    },
    {
      header: 'Requested Role / Entity',
      key: 'requestedRole',
      render: (item) => (
        <span className="text-xs font-medium text-slate-600">
          {item.requestedRole || item.type || 'Prosumer'}
        </span>
      ),
    },
    {
      header: 'Requested Date',
      key: 'requestedAt',
      render: (item) => (
        <span className="text-xs text-slate-500">
          {formatDate(item.createdAt || item.requestedAt)}
        </span>
      ),
    },
    {
      header: 'Status',
      key: 'status',
      render: (item) => <StatusBadge status={item.status || 'Pending'} />,
    },
    {
      header: 'Review Actions',
      key: 'actions',
      align: 'right',
      render: (item) => (
        <div className="flex items-center justify-end gap-2">
          <Button
            variant="success"
            size="sm"
            onClick={() => onApprove(item)}
            title="Approve activation via Web API"
          >
            <HiCheck className="w-4 h-4 mr-1" />
            Approve
          </Button>
          <Button
            variant="secondary"
            size="sm"
            onClick={() => onReject(item)}
            className="text-red-600 hover:text-red-700 hover:bg-red-50"
            title="Reject activation request"
          >
            <HiXMark className="w-4 h-4 mr-1" />
            Reject
          </Button>
        </div>
      ),
    },
  ];

  return (
    <DataTable
      columns={columns}
      data={activations}
      keyField="id"
      isLoading={isLoading}
      emptyTitle="No pending activation requests"
      emptyDescription="All registration and activation requests have been reviewed and finalized."
    />
  );
}
