import React from 'react';
import { HiCheckCircle, HiXMark } from 'react-icons/hi2';

export default function SuccessAlert({ message, title = 'Success', onClose, className = '' }) {
  if (!message) return null;

  return (
    <div
      className={`rounded-xl bg-emerald-50 border border-emerald-200/80 p-4 text-sm text-emerald-800 flex items-start gap-3 shadow-xs ${className}`}
      role="alert"
    >
      <HiCheckCircle className="w-5 h-5 text-emerald-500 shrink-0 mt-0.5" />
      <div className="flex-1">
        {title && <h5 className="font-semibold text-emerald-900 mb-0.5">{title}</h5>}
        <div className="text-emerald-700 leading-relaxed">{message}</div>
      </div>
      {onClose && (
        <button
          type="button"
          onClick={onClose}
          className="text-emerald-400 hover:text-emerald-600 p-1 rounded-md transition-colors"
        >
          <HiXMark className="w-4 h-4" />
        </button>
      )}
    </div>
  );
}
