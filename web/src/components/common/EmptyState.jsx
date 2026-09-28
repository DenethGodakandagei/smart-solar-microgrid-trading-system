import React from 'react';
import { HiOutlineFolderOpen } from 'react-icons/hi2';
import Button from './Button';

export default function EmptyState({
  icon: Icon = HiOutlineFolderOpen,
  title = 'No records found',
  description = 'There is currently no data to display.',
  actionText,
  onAction,
  className = '',
}) {
  return (
    <div
      className={`flex flex-col items-center justify-center p-12 text-center bg-white rounded-xl border border-dashed border-slate-200 ${className}`}
    >
      <div className="w-14 h-14 rounded-2xl bg-slate-50 border border-slate-100 flex items-center justify-center text-slate-400 mb-4 shadow-2xs">
        <Icon className="w-7 h-7" />
      </div>
      <h4 className="text-base font-semibold text-slate-800 mb-1">{title}</h4>
      <p className="text-sm text-slate-500 max-w-sm mb-6 leading-relaxed">{description}</p>
      {actionText && onAction && (
        <Button variant="primary" onClick={onAction}>
          {actionText}
        </Button>
      )}
    </div>
  );
}
