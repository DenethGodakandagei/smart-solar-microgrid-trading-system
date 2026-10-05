import React from 'react';
import DataTable from '../common/DataTable';
import StatusBadge from '../common/StatusBadge';
import Button from '../common/Button';
import { formatCapacity, formatEnergy } from '../../utils/formatEnergy';
import {
  HiOutlinePencilSquare,
  HiOutlineNoSymbol,
  HiOutlineArrowPath,
} from 'react-icons/hi2';

export default function ProsumerTable({
  prosumers,
  isLoading,
  onEdit,
  onDeactivate,
  onReactivate,
}) {
  const columns = [
    {
      header: 'Primary Identifier (NIC)',
      key: 'nic',
      render: (p) => (
        <span className="font-mono text-xs font-semibold px-2 py-1 rounded bg-slate-100 text-slate-800 border border-slate-200">
          {p.nic}
        </span>
      ),
    },
    {
      header: 'Prosumer Name',
      key: 'fullName',
      render: (p) => (
        <div>
          <div className="font-semibold text-slate-800">{p.fullName || p.name}</div>
          <div className="text-xs text-slate-400">{p.email || p.phone || '—'}</div>
        </div>
      ),
    },
    {
      header: 'Solar Gen / Battery',
      key: 'capacity',
      render: (p) => (
        <div className="text-xs text-slate-600">
          <div>Solar: <strong className="text-slate-800">{formatCapacity(p.solarCapacityKw)}</strong></div>
          <div>Storage: <strong className="text-slate-800">{formatEnergy(p.batteryStorageKwh)}</strong></div>
        </div>
      ),
    },
    {
      header: 'Status',
      key: 'status',
      render: (p) => <StatusBadge status={p.status || 'Active'} />,
    },
    {
      header: 'Actions',
      key: 'actions',
      align: 'right',
      render: (p) => {
        const isInactive = p.status?.toLowerCase() === 'inactive' || p.status?.toLowerCase() === 'deactivated';

        return (
          <div className="flex items-center justify-end gap-1.5">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onEdit(p)}
              title="Edit prosumer profile"
            >
              <HiOutlinePencilSquare className="w-4 h-4 text-blue-600" />
              <span className="sr-only">Edit</span>
            </Button>

            {/* Backoffice only reactivation / deactivation toggle */}
            {isInactive ? (
              <Button
                variant="ghost"
                size="sm"
                onClick={() => onReactivate(p)}
                className="text-emerald-600 hover:text-emerald-700 hover:bg-emerald-50"
                title="Reactivate prosumer account (Backoffice exclusive)"
              >
                <HiOutlineArrowPath className="w-4 h-4 mr-1 inline" />
                <span className="text-xs font-medium">Reactivate</span>
              </Button>
            ) : (
              <Button
                variant="ghost"
                size="sm"
                onClick={() => onDeactivate(p)}
                title="Deactivate prosumer account"
              >
                <HiOutlineNoSymbol className="w-4 h-4 text-red-500 hover:text-red-700" />
                <span className="sr-only">Deactivate</span>
              </Button>
            )}
          </div>
        );
      },
    },
  ];

  return (
    <DataTable
      columns={columns}
      data={prosumers}
      keyField="nic"
      isLoading={isLoading}
      emptyTitle="No prosumers registered"
      emptyDescription="Register a prosumer profile using the 'Register Prosumer' button above."
    />
  );
}
