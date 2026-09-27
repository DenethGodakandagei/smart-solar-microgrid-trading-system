import React from 'react';
import Card from '../common/Card';
import StatusBadge from '../common/StatusBadge';
import { HiOutlineBolt, HiOutlineCheckCircle, HiOutlineClock } from 'react-icons/hi2';

export default function SlotAvailabilityGrid({ slots = [], nodeName, isLoading = false }) {
  if (isLoading) {
    return (
      <div className="p-8 text-center bg-white rounded-xl border border-slate-200">
        <div className="w-8 h-8 border-2 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto mb-2" />
        <p className="text-xs text-slate-500">Querying slot availability state from C# Web API...</p>
      </div>
    );
  }

  if (!slots || slots.length === 0) {
    return (
      <div className="p-8 text-center bg-white rounded-xl border border-dashed border-slate-200">
        <p className="text-sm font-medium text-slate-600">No slot data returned by server for this node.</p>
      </div>
    );
  }

  const getSlotStateConfig = (slot) => {
    const status = (slot.status || 'Available').toLowerCase();
    switch (status) {
      case 'available':
        return {
          bg: 'bg-emerald-50 border-emerald-300 hover:border-emerald-400',
          badge: 'Available',
          icon: HiOutlineCheckCircle,
          iconColor: 'text-emerald-500',
          text: 'Open for Energy Trading',
        };
      case 'reserved':
        return {
          bg: 'bg-indigo-50 border-indigo-300 hover:border-indigo-400',
          badge: 'Reserved',
          icon: HiOutlineClock,
          iconColor: 'text-indigo-500',
          text: `Reserved (${slot.reservedUntil ? new Date(slot.reservedUntil).toLocaleTimeString() : 'Active'})`,
        };
      case 'occupied':
        return {
          bg: 'bg-purple-50 border-purple-300 hover:border-purple-400',
          badge: 'Occupied',
          icon: HiOutlineBolt,
          iconColor: 'text-purple-500',
          text: 'Active Discharge / Charge Cycle',
        };
      default:
        return {
          bg: 'bg-slate-50 border-slate-300',
          badge: slot.status,
          icon: HiOutlineBolt,
          iconColor: 'text-slate-400',
          text: slot.status,
        };
    }
  };

  return (
    <Card
      title={`Storage Rack Bays — ${nodeName || 'Microgrid Hub'}`}
      subtitle="Visual representation of real-time slot states maintained in MongoDB"
    >
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
        {slots.map((slot, index) => {
          const slotNum = slot.slotNumber || slot.number || index + 1;
          const config = getSlotStateConfig(slot);
          const Icon = config.icon;

          return (
            <div
              key={slot.id || slotNum}
              className={`p-4 rounded-xl border-2 transition-all duration-150 shadow-2xs ${config.bg}`}
            >
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-bold uppercase tracking-wider text-slate-700">
                  Bay Slot #{slotNum}
                </span>
                <StatusBadge status={config.badge} />
              </div>

              <div className="flex items-center gap-2 my-2">
                <Icon className={`w-5 h-5 ${config.iconColor}`} />
                <span className="text-xs font-semibold text-slate-800 truncate">
                  {slot.currentEnergyKwh ? `${slot.currentEnergyKwh} kWh Stored` : 'Battery Bay'}
                </span>
              </div>

              <p className="text-[11px] text-slate-500 mt-1 line-clamp-1">
                {slot.prosumerNic ? `Allocated: ${slot.prosumerNic}` : config.text}
              </p>
            </div>
          );
        })}
      </div>
    </Card>
  );
}
