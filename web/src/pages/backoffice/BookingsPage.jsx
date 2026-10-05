import React, { useState, useEffect } from 'react';
import DataTable from '../../components/common/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
import SearchBar from '../../components/common/SearchBar';
import Button from '../../components/common/Button';
import ErrorAlert from '../../components/common/ErrorAlert';
import { getBookings } from '../../api/bookingApi';
import { formatDate, formatDateTime } from '../../utils/formatDate';
import { formatEnergy } from '../../utils/formatEnergy';
import { HiOutlineArrowPath, HiOutlineCalendarDays } from 'react-icons/hi2';

export default function BookingsPage() {
  const [bookings, setBookings] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchBookings = async () => {
    setIsLoading(true);
    setError('');
    try {
      const data = await getBookings(searchTerm ? { search: searchTerm } : {});
      setBookings(Array.isArray(data) ? data : data?.items || []);
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Failed to fetch system bookings from Web API.';
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
  }, [searchTerm]);

  const columns = [
    {
      header: 'Booking Reference',
      key: 'id',
      render: (b) => (
        <span className="font-mono text-xs font-semibold px-2 py-0.5 rounded bg-slate-100 text-slate-800 border border-slate-200">
          {b.bookingRef || b.id || b._id}
        </span>
      ),
    },
    {
      header: 'Prosumer (NIC)',
      key: 'prosumer',
      render: (b) => (
        <div>
          <div className="font-semibold text-slate-800">{b.prosumerName || 'Prosumer'}</div>
          <div className="text-xs text-slate-400 font-mono">NIC: {b.prosumerNic || b.nic || '—'}</div>
        </div>
      ),
    },
    {
      header: 'Microgrid Hub',
      key: 'nodeName',
      render: (b) => (
        <div className="text-xs">
          <div className="font-medium text-slate-700">{b.nodeName || `Node #${b.nodeId}`}</div>
          <div className="text-slate-400">Bay: Slot #{b.slotNumber || 1}</div>
        </div>
      ),
    },
    {
      header: 'Energy Allocated',
      key: 'energyKwh',
      render: (b) => (
        <span className="text-sm font-semibold text-slate-800">
          {formatEnergy(b.energyKwh || b.quantityKwh)}
        </span>
      ),
    },
    {
      header: 'Trading Schedule',
      key: 'schedule',
      render: (b) => (
        <div className="text-xs text-slate-600">
          <div>Start: {formatDateTime(b.startTime || b.reservationStart)}</div>
          <div>End: {formatDateTime(b.endTime || b.reservationEnd)}</div>
        </div>
      ),
    },
    {
      header: 'Status',
      key: 'status',
      render: (b) => <StatusBadge status={b.status || 'Active'} />,
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">
            System Trading Oversight
          </h2>
          <p className="text-sm text-slate-500 mt-1">
            Global monitoring of energy trades and slot reservations across all microgrid clusters
          </p>
        </div>

        <Button
          variant="secondary"
          size="sm"
          onClick={fetchBookings}
          isLoading={isLoading}
          icon={HiOutlineArrowPath}
        >
          Refresh Feed
        </Button>
      </div>

      {error && <ErrorAlert message={error} onClose={() => setError('')} />}

      <div className="flex items-center justify-between gap-4">
        <SearchBar
          value={searchTerm}
          onChange={setSearchTerm}
          onClear={() => setSearchTerm('')}
          placeholder="Filter by booking reference, prosumer NIC, or hub..."
        />
      </div>

      <DataTable
        columns={columns}
        data={bookings}
        keyField="id"
        isLoading={isLoading}
        emptyTitle="No trading reservations recorded"
        emptyDescription="Energy trading records will populate when slots are booked by operators or prosumers."
      />
    </div>
  );
}
