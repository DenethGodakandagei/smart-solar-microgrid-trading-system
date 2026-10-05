import React, { useState, useEffect } from 'react';
import DataTable from '../../components/common/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
import SearchBar from '../../components/common/SearchBar';
import Pagination from '../../components/common/Pagination';
import Button from '../../components/common/Button';
import ErrorAlert from '../../components/common/ErrorAlert';
import { getBookingHistory } from '../../api/bookingApi';
import { formatDate, formatDateTime } from '../../utils/formatDate';
import { formatEnergy } from '../../utils/formatEnergy';
import { HiOutlineArrowPath } from 'react-icons/hi2';

export default function HistoryPage() {
  const [history, setHistory] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalCount, setTotalCount] = useState(0);

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchHistory = async () => {
    setIsLoading(true);
    setError('');
    try {
      const data = await getBookingHistory({
        search: searchTerm,
        page,
        pageSize: 10,
      });

      if (Array.isArray(data)) {
        setHistory(data);
        setTotalPages(1);
        setTotalCount(data.length);
      } else {
        setHistory(data?.items || []);
        setTotalPages(data?.totalPages || 1);
        setTotalCount(data?.totalCount || 0);
      }
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Failed to fetch booking history from server.';
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, [searchTerm, page]);

  const columns = [
    {
      header: 'Booking Ref',
      key: 'bookingRef',
      render: (item) => (
        <span className="font-mono text-xs font-semibold px-2 py-0.5 rounded bg-slate-100 text-slate-800 border border-slate-200">
          {item.bookingRef || item.id || item._id}
        </span>
      ),
    },
    {
      header: 'Prosumer (NIC)',
      key: 'prosumer',
      render: (item) => (
        <div>
          <div className="font-semibold text-slate-800">{item.prosumerName || 'Prosumer'}</div>
          <div className="text-xs text-slate-400 font-mono">NIC: {item.prosumerNic || item.nic || '—'}</div>
        </div>
      ),
    },
    {
      header: 'Microgrid Node',
      key: 'nodeName',
      render: (item) => (
        <span className="text-xs font-medium text-slate-700">
          {item.nodeName || `Node #${item.nodeId}`}
        </span>
      ),
    },
    {
      header: 'Energy Traded',
      key: 'energy',
      render: (item) => (
        <span className="font-semibold text-slate-800 text-sm">
          {formatEnergy(item.energyKwh || item.quantityKwh)}
        </span>
      ),
    },
    {
      header: 'Execution Window',
      key: 'timestamps',
      render: (item) => (
        <div className="text-xs text-slate-500">
          <div>{formatDateTime(item.startTime || item.reservationStart)}</div>
          <div>to {formatDateTime(item.endTime || item.reservationEnd)}</div>
        </div>
      ),
    },
    {
      header: 'Final State',
      key: 'status',
      render: (item) => <StatusBadge status={item.status || 'Completed'} />,
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">
            Energy Trading History
          </h2>
          <p className="text-sm text-slate-500 mt-1">
            Audit logs and historical settlement of microgrid energy slot transactions
          </p>
        </div>

        <Button
          variant="secondary"
          size="sm"
          onClick={fetchHistory}
          isLoading={isLoading}
          icon={HiOutlineArrowPath}
        >
          Refresh History
        </Button>
      </div>

      {error && <ErrorAlert message={error} onClose={() => setError('')} />}

      <div className="flex items-center justify-between gap-4">
        <SearchBar
          value={searchTerm}
          onChange={(val) => {
            setSearchTerm(val);
            setPage(1);
          }}
          onClear={() => {
            setSearchTerm('');
            setPage(1);
          }}
          placeholder="Filter historical trades by prosumer NIC, ref, or hub..."
        />
      </div>

      <div className="space-y-0">
        <DataTable
          columns={columns}
          data={history}
          keyField="id"
          isLoading={isLoading}
          emptyTitle="No historical records found"
          emptyDescription="Historical bookings will display here once energy reservations reach Completed or Cancelled status."
          className="rounded-b-none border-b-0"
        />

        <Pagination
          currentPage={page}
          totalPages={totalPages}
          totalItems={totalCount}
          pageSize={10}
          onPageChange={setPage}
        />
      </div>
    </div>
  );
}
