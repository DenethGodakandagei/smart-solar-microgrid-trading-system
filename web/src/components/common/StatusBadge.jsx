import React from 'react';

export default function StatusBadge({ status, className = '' }) {
  if (!status) return null;

  const normalized = String(status).toLowerCase();

  const styles = {
    active: 'bg-emerald-50 text-emerald-700 border-emerald-200/60 ring-emerald-500/10',
    approved: 'bg-emerald-50 text-emerald-700 border-emerald-200/60 ring-emerald-500/10',
    available: 'bg-emerald-50 text-emerald-700 border-emerald-200/60 ring-emerald-500/10',
    completed: 'bg-blue-50 text-blue-700 border-blue-200/60 ring-blue-500/10',
    confirmed: 'bg-blue-50 text-blue-700 border-blue-200/60 ring-blue-500/10',
    pending: 'bg-amber-50 text-amber-700 border-amber-200/60 ring-amber-500/10',
    reserved: 'bg-indigo-50 text-indigo-700 border-indigo-200/60 ring-indigo-500/10',
    occupied: 'bg-purple-50 text-purple-700 border-purple-200/60 ring-purple-500/10',
    inactive: 'bg-slate-100 text-slate-600 border-slate-200 ring-slate-500/10',
    rejected: 'bg-red-50 text-red-700 border-red-200/60 ring-red-500/10',
    cancelled: 'bg-red-50 text-red-700 border-red-200/60 ring-red-500/10',
    blocked: 'bg-rose-50 text-rose-700 border-rose-300 ring-rose-500/10',
    maintenance: 'bg-orange-50 text-orange-700 border-orange-200/60 ring-orange-500/10',
  };

  const dotColors = {
    active: 'bg-emerald-500',
    approved: 'bg-emerald-500',
    available: 'bg-emerald-500',
    completed: 'bg-blue-500',
    confirmed: 'bg-blue-500',
    pending: 'bg-amber-500',
    reserved: 'bg-indigo-500',
    occupied: 'bg-purple-500',
    inactive: 'bg-slate-400',
    rejected: 'bg-red-500',
    cancelled: 'bg-red-500',
    blocked: 'bg-rose-500',
    maintenance: 'bg-orange-500',
  };

  const badgeStyle = styles[normalized] || 'bg-slate-100 text-slate-700 border-slate-200';
  const dotColor = dotColors[normalized] || 'bg-slate-400';

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium border ring-1 ring-inset ${badgeStyle} ${className}`}
    >
      <span className={`w-1.5 h-1.5 rounded-full ${dotColor}`} />
      {status}
    </span>
  );
}
