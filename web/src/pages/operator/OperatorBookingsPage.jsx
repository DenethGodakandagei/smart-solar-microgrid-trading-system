import React, { useState, useEffect } from 'react';
import BookingTable from '../../components/operator/BookingTable';
import BookingDetailCard from '../../components/operator/BookingDetailCard';
import BookingFilters from '../../components/operator/BookingFilters';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';
import Button from '../../components/common/Button';
import ErrorAlert from '../../components/common/ErrorAlert';
import { useNotification } from '../../context/NotificationContext';
import {
  getBookings,
  updateBooking,
  cancelBooking,
} from '../../api/bookingApi';
import { getNodes } from '../../api/nodeApi';
import { HiOutlineArrowPath } from 'react-icons/hi2';

export default function OperatorBookingsPage() {
  const [bookings, setBookings] = useState([]);
  const [nodes, setNodes] = useState([]);
  const [filters, setFilters] = useState({
    search: '',
    status: '',
    nodeId: '',
  });

  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // Modals & targets
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [isDetailOpen, setIsDetailOpen] = useState(false);
  const [cancelTarget, setCancelTarget] = useState(null);
  const [editTarget, setEditTarget] = useState(null);
  const [editFormData, setEditFormData] = useState({
    energyKwh: '',
    startTime: '',
    endTime: '',
  });

  const { showSuccess, showError } = useNotification();

  const fetchBookingsData = async () => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const [bookingsData, nodesData] = await Promise.all([
        getBookings(filters),
        getNodes(),
      ]);
      setBookings(Array.isArray(bookingsData) ? bookingsData : bookingsData?.items || []);
      setNodes(Array.isArray(nodesData) ? nodesData : nodesData?.items || []);
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Failed to retrieve booking records from Web API.';
      setErrorMessage(msg);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchBookingsData();
  }, [filters]);

  const handleView = (booking) => {
    setSelectedBooking(booking);
    setIsDetailOpen(true);
  };

  const handleOpenEdit = (booking) => {
    setEditTarget(booking);
    setEditFormData({
      energyKwh: booking.energyKwh || booking.quantityKwh || '',
      startTime: booking.startTime ? booking.startTime.slice(0, 16) : '',
      endTime: booking.endTime ? booking.endTime.slice(0, 16) : '',
    });
  };

  /**
   * Update booking:
   * Backend enforces the 12-hour notice rule and 7-day scheduling window.
   * If rejected by API, React displays the server's validation message verbatim.
   */
  const handleSaveUpdate = async (e) => {
    e.preventDefault();
    if (!editTarget) return;

    setIsSubmitting(true);
    setErrorMessage('');
    try {
      await updateBooking(editTarget.id || editTarget._id, editFormData);
      showSuccess(`Booking #${editTarget.bookingRef || editTarget.id} updated successfully.`);
      setEditTarget(null);
      fetchBookingsData();
    } catch (err) {
      // Extract exact validation error from API (e.g., "Updates require at least 12 hours' notice.")
      const serverMessage =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Failed to update booking.';
      setErrorMessage(`Server Validation Rule: ${serverMessage}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  /**
   * Cancel booking:
   * Backend enforces the 12-hour notice rule.
   * If rejected by API, React displays the server's cancellation denial message verbatim.
   */
  const handleConfirmCancel = async () => {
    if (!cancelTarget) return;

    setIsSubmitting(true);
    setErrorMessage('');
    try {
      await cancelBooking(cancelTarget.id || cancelTarget._id);
      showSuccess(`Booking #${cancelTarget.bookingRef || cancelTarget.id} cancelled successfully.`);
      setCancelTarget(null);
      fetchBookingsData();
    } catch (err) {
      // Server rejected cancellation (e.g. "Cancellation is not allowed within 12 hours.")
      const serverMessage =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Cancellation rejected by Web API.';

      // Display the verbatim API message to the user
      setErrorMessage(`Server Cancellation Rule: ${serverMessage}`);
      showError(serverMessage);
      setCancelTarget(null);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">
            Energy Slot Booking Management
          </h2>
          <p className="text-sm text-slate-500 mt-1">
            Oversee solar power trades, dispatch adjustments, and slot reservation lifecycles
          </p>
        </div>

        <Button
          variant="secondary"
          size="sm"
          onClick={fetchBookingsData}
          isLoading={isLoading}
          icon={HiOutlineArrowPath}
        >
          Refresh Feed
        </Button>
      </div>

      {/* Critical: API error message display */}
      {errorMessage && (
        <ErrorAlert
          title="Reservation Policy Notice"
          message={errorMessage}
          onClose={() => setErrorMessage('')}
        />
      )}

      {/* Filters */}
      <BookingFilters
        filters={filters}
        onChange={setFilters}
        onReset={() => setFilters({ search: '', status: '', nodeId: '' })}
        nodes={nodes}
      />

      {/* Booking Data Table */}
      <BookingTable
        bookings={bookings}
        isLoading={isLoading}
        onView={handleView}
        onEdit={handleOpenEdit}
        onCancel={(booking) => setCancelTarget(booking)}
      />

      {/* Booking Detail Card Modal */}
      <BookingDetailCard
        isOpen={isDetailOpen}
        onClose={() => setIsDetailOpen(false)}
        booking={selectedBooking}
        onEdit={handleOpenEdit}
        onCancel={(b) => setCancelTarget(b)}
      />

      {/* Edit Booking Modal */}
      <Modal
        isOpen={!!editTarget}
        onClose={() => setEditTarget(null)}
        title={`Update Booking #${editTarget?.bookingRef || editTarget?.id}`}
        subtitle="Subject to server-enforced 12-hour notice & 7-day scheduling window"
        maxWidth="max-w-md"
      >
        <form onSubmit={handleSaveUpdate} className="space-y-4">
          <Input
            label="Energy Allocated (kWh)"
            type="number"
            step="0.1"
            name="energyKwh"
            value={editFormData.energyKwh}
            onChange={(e) =>
              setEditFormData((prev) => ({ ...prev, energyKwh: e.target.value }))
            }
            required
          />

          <Input
            label="Scheduled Start Time"
            type="datetime-local"
            name="startTime"
            value={editFormData.startTime}
            onChange={(e) =>
              setEditFormData((prev) => ({ ...prev, startTime: e.target.value }))
            }
            required
          />

          <Input
            label="Scheduled End Time"
            type="datetime-local"
            name="endTime"
            value={editFormData.endTime}
            onChange={(e) =>
              setEditFormData((prev) => ({ ...prev, endTime: e.target.value }))
            }
            required
          />

          <div className="flex justify-end gap-3 pt-4 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setEditTarget(null)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" isLoading={isSubmitting}>
              Submit Update Request
            </Button>
          </div>
        </form>
      </Modal>

      {/* Confirm Cancellation Dialog */}
      <ConfirmDialog
        isOpen={!!cancelTarget}
        onClose={() => setCancelTarget(null)}
        onConfirm={handleConfirmCancel}
        title="Cancel Energy Slot Booking"
        message={`Confirm cancellation of booking #${cancelTarget?.bookingRef || cancelTarget?.id}? Note: The Web API enforces a strict 12-hour minimum notice requirement.`}
        confirmText="Confirm Cancellation"
        variant="danger"
        isLoading={isSubmitting}
      />
    </div>
  );
}
