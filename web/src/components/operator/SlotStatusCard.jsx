import React from 'react';
import Card from '../common/Card';
import Button from '../common/Button';
import { HiOutlineBolt } from 'react-icons/hi2';

export default function SlotStatusCard({ node, isSelected, onSelect }) {
  const total = node.batterySlotsCount ?? node.totalSlots ?? 0;
  const available = node.availableSlotsCount ?? node.availableSlots ?? 0;
  const occupied = node.occupiedSlotsCount ?? 0;
  const reserved = node.reservedSlotsCount ?? 0;

  return (
    <div
      onClick={() => onSelect(node)}
      className={`p-4 rounded-xl border-2 transition-all duration-150 cursor-pointer bg-white ${
        isSelected
          ? 'border-blue-600 ring-2 ring-blue-500/20 shadow-md'
          : 'border-slate-200 hover:border-slate-300 shadow-2xs'
      }`}
    >
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-lg bg-blue-50 text-blue-600">
            <HiOutlineBolt className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-slate-800">{node.name}</h4>
            <p className="text-xs text-slate-400 font-mono">{node.nodeCode || `Node #${node.id}`}</p>
          </div>
        </div>
        <span
          className={`text-xs px-2 py-0.5 rounded-full font-semibold ${
            isSelected ? 'bg-blue-600 text-white' : 'bg-slate-100 text-slate-600'
          }`}
        >
          {isSelected ? 'Viewing' : 'Select'}
        </span>
      </div>

      {/* Breakdown metrics */}
      <div className="grid grid-cols-3 gap-2 pt-2 border-t border-slate-100 text-center">
        <div className="p-2 rounded-lg bg-emerald-50">
          <span className="text-[10px] uppercase font-bold text-emerald-700 block">Available</span>
          <span className="text-base font-bold text-emerald-800">{available}</span>
        </div>
        <div className="p-2 rounded-lg bg-indigo-50">
          <span className="text-[10px] uppercase font-bold text-indigo-700 block">Reserved</span>
          <span className="text-base font-bold text-indigo-800">{reserved}</span>
        </div>
        <div className="p-2 rounded-lg bg-purple-50">
          <span className="text-[10px] uppercase font-bold text-purple-700 block">Occupied</span>
          <span className="text-base font-bold text-purple-800">{occupied}</span>
        </div>
      </div>
    </div>
  );
}
