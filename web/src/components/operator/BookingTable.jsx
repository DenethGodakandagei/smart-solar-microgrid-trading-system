import React from 'react';
import DataTable from '../common/DataTable';
import StatusBadge from '../common/StatusBadge';
import Button from '../common/Button';
import { formatDateTime } from '../../utils/formatDate';
import { formatEnergy } from '../../utils/formatEnergy';
import {
  HiOutlineEye,
  HiOutlinePencilSquare,
  HiOutlineXMark,
} from 'react-icons/hi2';

export default function BookingTable({
  bookings,
  isLoading,
  onView,
  onEdit,
  onCancel,
}) {
  const columns = [
    {
      header: 'Booking ID',
      key: 'id',
      render: (b) => (
        <span className="font-mono text-xs font-semibold px-2 py-0.5 rounded bg-slate-100 text-slate-800 border border-slate-200">
          {b.bookingRef || b.id || b._id}
        </span>
      ),
    },
    {
      header: 'Prosumer',
      key: 'prosumer',
      render: (b) => (
        <div>
          <div className="font-semibold text-slate-800">{b.prosumerName || 'Prosumer'}</div>
          <div className="text-xs text-slate-400 font-mono">NIC: {b.prosumerNic || b.nic || '—'}</div>
        </div>
      ),
    },
    {
      header: 'Hub & Slot',
      key: 'node',
      render: (b) => (
        <div className="text-xs text-slate-700">
          <div className="font-medium">{b.nodeName || `Node #${b.nodeId}`}</div>
          <div className="text-slate-400">Bay #{b.slotNumber || 1}</div>
        </div>
      ),
    },
    {
      header: 'Energy (kWh)',
      key: 'energy',
      render: (b) => (
        <span className="font-semibold text-slate-800 text-sm">
          {formatEnergy(b.energyKwh || b.quantityKwh)}
        </span>
      ),
    },
    {
      header: 'Scheduled Time Window',
      key: 'time',
      render: (b) => (
        <div className="text-xs text-slate-600">
          <div>{formatDateTime(b.startTime || b.reservationStart)}</div>
          <div className="text-slate-400">to {formatDateTime(b.endTime || b.reservationEnd)}</div>
        </div>
      ),
    },
    {
      header: 'Status',
      key: 'status',
      render: (b) => <StatusBadge status={b.status || 'Active'} />,
    },
    {
      header: 'Actions',
      key: 'actions',
      align: 'right',
      render: (b) => {
        const isCancelled = b.status?.toLowerCase() === 'cancelled';
        const isCompleted = b.status?.toLowerCase() === 'completed';

        return (
          <div className="flex items-center justify-end gap-1.5">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onView(b)}
              title="View full booking specifications"
            >
              <HiOutlineEye className="w-4 h-4 text-slate-600" />
              <span className="sr-only">View</span>
            </Button>

            {!isCancelled && !isCompleted && (
              <>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => onEdit(b)}
                  title="Update booking parameters (Requires 12h notice - verified by API)"
                >
                  <HiOutlinePencilSquare className="w-4 h-4 text-blue-600" />
                  <span className="sr-only">Edit</span>
                </Button>

                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => onCancel(b)}
                  title="Cancel booking (Requires 12h notice - verified by API)"
                >
                  <HiOutlineXMark className="w-4 h-4 text-red-500 hover:text-red-700" />
                  <span className="sr-only">Cancel</span>
                </Button>
              </>
            )}
          </div>
        );
      },
    },
  ];

  return (
    <DataTable
      columns={columns}
      data={bookings}
      keyField="id"
      isLoading={isLoading}
      emptyTitle="No energy bookings located"
      emptyDescription="Create a new solar slot reservation or adjust filter parameters."
    />
  );
}
