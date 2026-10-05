import React from 'react';
import Modal from '../common/Modal';
import Button from '../common/Button';
import StatusBadge from '../common/StatusBadge';
import { formatDateTime } from '../../utils/formatDate';
import { formatEnergy } from '../../utils/formatEnergy';
import {
  HiOutlineBolt,
  HiOutlineCalendar,
  HiOutlineUser,
  HiOutlineCube,
} from 'react-icons/hi2';

export default function BookingDetailCard({ isOpen, onClose, booking, onCancel, onEdit }) {
  if (!booking) return null;

  const isCancelled = booking.status?.toLowerCase() === 'cancelled';
  const isCompleted = booking.status?.toLowerCase() === 'completed';

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`Reservation Reference: ${booking.bookingRef || booking.id || booking._id}`}
      subtitle="Details retrieved from central MongoDB via ASP.NET Web API"
      maxWidth="max-w-lg"
    >
      <div className="space-y-4">
        {/* Status header */}
        <div className="flex items-center justify-between p-3 bg-slate-50 rounded-xl border border-slate-200">
          <span className="text-xs font-medium text-slate-500">Current Lifecycle Status</span>
          <StatusBadge status={booking.status || 'Active'} />
        </div>

        {/* Prosumer Details */}
        <div className="p-3.5 bg-white rounded-xl border border-slate-200 space-y-2">
          <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-slate-500">
            <HiOutlineUser className="w-4 h-4 text-blue-500" />
            Prosumer Identification
          </div>
          <div className="grid grid-cols-2 gap-2 text-xs">
            <div>
              <span className="text-slate-400 block">Full Name:</span>
              <span className="font-semibold text-slate-800">{booking.prosumerName || 'Prosumer'}</span>
            </div>
            <div>
              <span className="text-slate-400 block">NIC (Primary Key):</span>
              <span className="font-mono font-semibold text-slate-800">{booking.prosumerNic || booking.nic || '—'}</span>
            </div>
          </div>
        </div>

        {/* Microgrid Hub & Slot Bay */}
        <div className="p-3.5 bg-white rounded-xl border border-slate-200 space-y-2">
          <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-slate-500">
            <HiOutlineCube className="w-4 h-4 text-purple-500" />
            Microgrid Node Allocation
          </div>
          <div className="grid grid-cols-2 gap-2 text-xs">
            <div>
              <span className="text-slate-400 block">Node Hub:</span>
              <span className="font-semibold text-slate-800">{booking.nodeName || `Node #${booking.nodeId}`}</span>
            </div>
            <div>
              <span className="text-slate-400 block">Assigned Battery Slot:</span>
              <span className="font-semibold text-slate-800">Slot #{booking.slotNumber || 1}</span>
            </div>
          </div>
        </div>

        {/* Energy & Schedule */}
        <div className="p-3.5 bg-white rounded-xl border border-slate-200 space-y-2">
          <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-slate-500">
            <HiOutlineBolt className="w-4 h-4 text-amber-500" />
            Energy & Scheduling Window
          </div>
          <div className="grid grid-cols-2 gap-2 text-xs">
            <div>
              <span className="text-slate-400 block">Reserved Energy:</span>
              <span className="font-bold text-slate-900 text-sm">
                {formatEnergy(booking.energyKwh || booking.quantityKwh)}
              </span>
            </div>
            <div>
              <span className="text-slate-400 block">Tariff Rate / Price:</span>
              <span className="font-semibold text-slate-800">
                {booking.ratePerKwh ? `LKR ${booking.ratePerKwh} / kWh` : 'Standard Microgrid Tariff'}
              </span>
            </div>
            <div className="col-span-2 pt-1 border-t border-slate-100">
              <span className="text-slate-400 block">Scheduled Start:</span>
              <span className="font-semibold text-slate-700">
                {formatDateTime(booking.startTime || booking.reservationStart)}
              </span>
            </div>
            <div className="col-span-2">
              <span className="text-slate-400 block">Scheduled End:</span>
              <span className="font-semibold text-slate-700">
                {formatDateTime(booking.endTime || booking.reservationEnd)}
              </span>
            </div>
          </div>
        </div>

        {/* Policy notice */}
        <div className="p-3 bg-amber-50/60 border border-amber-200/80 rounded-xl text-xs text-amber-900">
          <strong>Backend Reservation Policy:</strong> Updates and cancellations are subject to 12-hour notice limits verified by the central C# Web API.
        </div>

        {/* Footer Actions */}
        <div className="flex justify-end gap-2 pt-4 border-t border-slate-100">
          <Button variant="secondary" onClick={onClose}>
            Close
          </Button>

          {!isCancelled && !isCompleted && onEdit && (
            <Button
              variant="primary"
              onClick={() => {
                onClose();
                onEdit(booking);
              }}
            >
              Update Booking
            </Button>
          )}

          {!isCancelled && !isCompleted && onCancel && (
            <Button
              variant="danger"
              onClick={() => {
                onClose();
                onCancel(booking);
              }}
            >
              Cancel Booking
            </Button>
          )}
        </div>
      </div>
    </Modal>
  );
}
