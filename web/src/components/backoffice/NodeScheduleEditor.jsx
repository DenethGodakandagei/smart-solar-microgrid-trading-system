import React, { useState, useEffect } from 'react';
import Modal from '../common/Modal';
import Input from '../common/Input';
import Button from '../common/Button';
import ErrorAlert from '../common/ErrorAlert';

export default function NodeScheduleEditor({ isOpen, onClose, onSave, node = null, isLoading = false }) {
  const [schedule, setSchedule] = useState({
    operationalStartTime: '06:00',
    operationalEndTime: '18:00',
    peakTradingStartTime: '10:00',
    peakTradingEndTime: '14:00',
    maintenanceDay: 'Sunday',
  });
  const [error, setError] = useState('');

  useEffect(() => {
    if (node?.schedule) {
      setSchedule({
        operationalStartTime: node.schedule.operationalStartTime || '06:00',
        operationalEndTime: node.schedule.operationalEndTime || '18:00',
        peakTradingStartTime: node.schedule.peakTradingStartTime || '10:00',
        peakTradingEndTime: node.schedule.peakTradingEndTime || '14:00',
        maintenanceDay: node.schedule.maintenanceDay || 'Sunday',
      });
    }
    setError('');
  }, [node, isOpen]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setSchedule((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      await onSave(schedule, node?.id || node?._id);
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Failed to update operational schedule on server.';
      setError(msg);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`Operational Schedule: ${node?.name || 'Node'}`}
      subtitle="Adjust daily solar generation and trading windows enforced by Web API"
      maxWidth="max-w-md"
    >
      {error && <ErrorAlert message={error} onClose={() => setError('')} className="mb-4" />}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-2 gap-3">
          <Input
            label="Operational Start"
            type="time"
            name="operationalStartTime"
            value={schedule.operationalStartTime}
            onChange={handleChange}
            required
          />
          <Input
            label="Operational End"
            type="time"
            name="operationalEndTime"
            value={schedule.operationalEndTime}
            onChange={handleChange}
            required
          />
        </div>

        <div className="grid grid-cols-2 gap-3">
          <Input
            label="Peak Trading Start"
            type="time"
            name="peakTradingStartTime"
            value={schedule.peakTradingStartTime}
            onChange={handleChange}
            required
          />
          <Input
            label="Peak Trading End"
            type="time"
            name="peakTradingEndTime"
            value={schedule.peakTradingEndTime}
            onChange={handleChange}
            required
          />
        </div>

        <Input
          label="Weekly Maintenance Window"
          name="maintenanceDay"
          value={schedule.maintenanceDay}
          onChange={handleChange}
          placeholder="e.g. Sunday 00:00 - 04:00"
        />

        <div className="flex justify-end gap-3 pt-4 border-t border-slate-100">
          <Button variant="secondary" onClick={onClose} disabled={isLoading}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" isLoading={isLoading}>
            Update Schedule
          </Button>
        </div>
      </form>
    </Modal>
  );
}
