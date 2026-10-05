import React from 'react';
import DataTable from '../common/DataTable';
import StatusBadge from '../common/StatusBadge';
import Button from '../common/Button';
import { formatEnergy } from '../../utils/formatEnergy';
import {
  HiOutlinePencilSquare,
  HiOutlineNoSymbol,
  HiOutlineClock,
  HiOutlineMapPin,
  HiOutlineLockClosed,
} from 'react-icons/hi2';

export default function NodeTable({
  nodes,
  isLoading,
  blockedNodeIds = [],
  onEdit,
  onEditSchedule,
  onDeactivate,
}) {
  const columns = [
    {
      header: 'Microgrid Node',
      key: 'name',
      render: (node) => (
        <div>
          <div className="font-semibold text-slate-800 flex items-center gap-1.5">
            {node.name}
            {blockedNodeIds.includes(node.id || node._id) && (
              <span
                title="Deactivation blocked: Server returned active energy reservations"
                className="inline-flex items-center gap-1 text-[10px] font-semibold bg-rose-50 text-rose-700 px-2 py-0.5 rounded-full border border-rose-200"
              >
                <HiOutlineLockClosed className="w-3 h-3 text-rose-500" />
                Active Bookings Blocked
              </span>
            )}
          </div>
          <div className="text-xs text-slate-400 font-mono">
            {node.nodeCode || node.code || `ID: ${node.id || node._id}`}
          </div>
        </div>
      ),
    },
    {
      header: 'GPS Location',
      key: 'gps',
      render: (node) => {
        const lat = node.latitude || node.gpsLatitude;
        const lng = node.longitude || node.gpsLongitude;
        if (!lat && !lng) return <span className="text-slate-400 text-xs">—</span>;

        return (
          <div className="flex items-center gap-1.5 text-xs text-slate-600 font-mono">
            <HiOutlineMapPin className="w-3.5 h-3.5 text-blue-500 shrink-0" />
            <span>
              {Number(lat).toFixed(4)}, {Number(lng).toFixed(4)}
            </span>
          </div>
        );
      },
    },
    {
      header: 'Capacity (kW/h)',
      key: 'capacityKwh',
      render: (node) => (
        <span className="font-semibold text-slate-800 text-sm">
          {formatEnergy(node.capacityKwh || node.capacity)}
        </span>
      ),
    },
    {
      header: 'Battery Slots',
      key: 'batterySlots',
      render: (node) => {
        const total = node.batterySlotsCount ?? node.totalSlots ?? 0;
        const available = node.availableSlotsCount ?? node.availableSlots ?? total;
        return (
          <div className="text-xs">
            <span className="font-semibold text-emerald-600">{available}</span>
            <span className="text-slate-400"> / {total} Available</span>
          </div>
        );
      },
    },
    {
      header: 'Status',
      key: 'status',
      render: (node) => <StatusBadge status={node.status || 'Active'} />,
    },
    {
      header: 'Actions',
      key: 'actions',
      align: 'right',
      render: (node) => (
        <div className="flex items-center justify-end gap-1.5">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onEdit(node)}
            title="Edit node specifications"
          >
            <HiOutlinePencilSquare className="w-4 h-4 text-blue-600" />
            <span className="sr-only">Edit</span>
          </Button>

          <Button
            variant="ghost"
            size="sm"
            onClick={() => onEditSchedule(node)}
            title="Edit operational schedule"
          >
            <HiOutlineClock className="w-4 h-4 text-amber-600" />
            <span className="sr-only">Schedule</span>
          </Button>

          {node.status !== 'Inactive' && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onDeactivate(node)}
              title="Deactivate node"
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
      data={nodes}
      keyField="id"
      isLoading={isLoading}
      emptyTitle="No microgrid nodes registered"
      emptyDescription="Register solar microgrid hubs using the 'Register Node' button above."
    />
  );
}
