import React from 'react';
import SearchBar from '../common/SearchBar';
import Select from '../common/Select';
import Button from '../common/Button';
import { BOOKING_STATUS } from '../../utils/constants';
import { HiOutlineArrowPath } from 'react-icons/hi2';

export default function BookingFilters({
  filters,
  onChange,
  onReset,
  nodes = [],
}) {
  const handleTextChange = (value) => {
    onChange({ ...filters, search: value });
  };

  const handleSelectChange = (name, value) => {
    onChange({ ...filters, [name]: value });
  };

  const statusOptions = [
    { value: '', label: 'All Booking Statuses' },
    { value: BOOKING_STATUS.PENDING, label: 'Pending Approval' },
    { value: BOOKING_STATUS.CONFIRMED, label: 'Confirmed' },
    { value: BOOKING_STATUS.ACTIVE, label: 'Active Trading' },
    { value: BOOKING_STATUS.COMPLETED, label: 'Completed' },
    { value: BOOKING_STATUS.CANCELLED, label: 'Cancelled' },
  ];

  const nodeOptions = [
    { value: '', label: 'All Microgrid Hubs' },
    ...nodes.map((n) => ({
      value: n.id || n._id,
      label: n.name || `Node #${n.id}`,
    })),
  ];

  return (
    <div className="bg-white p-4 rounded-xl border border-slate-200 space-y-3">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
        <SearchBar
          value={filters.search || ''}
          onChange={handleTextChange}
          onClear={() => handleTextChange('')}
          placeholder="Search by ID, prosumer NIC, or name..."
          className="max-w-none"
        />

        <Select
          name="status"
          value={filters.status || ''}
          onChange={(e) => handleSelectChange('status', e.target.value)}
          options={statusOptions}
          placeholder=""
        />

        <Select
          name="nodeId"
          value={filters.nodeId || ''}
          onChange={(e) => handleSelectChange('nodeId', e.target.value)}
          options={nodeOptions}
          placeholder=""
        />
      </div>

      <div className="flex items-center justify-between pt-2 border-t border-slate-100 text-xs">
        <span className="text-slate-400">
          Filters are applied directly as query parameters to the backend REST API
        </span>
        <Button
          variant="ghost"
          size="sm"
          onClick={onReset}
          icon={HiOutlineArrowPath}
        >
          Reset Filters
        </Button>
      </div>
    </div>
  );
}
