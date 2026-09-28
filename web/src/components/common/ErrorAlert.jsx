import React from 'react';
import { HiXCircle, HiXMark } from 'react-icons/hi2';

export default function ErrorAlert({ message, title = 'Error', onClose, className = '' }) {
  if (!message) return null;

  return (
    <div
      className={`rounded-xl bg-red-50 border border-red-200/80 p-4 text-sm text-red-800 flex items-start gap-3 shadow-xs ${className}`}
      role="alert"
    >
      <HiXCircle className="w-5 h-5 text-red-500 shrink-0 mt-0.5" />
      <div className="flex-1">
        {title && <h5 className="font-semibold text-red-900 mb-0.5">{title}</h5>}
        <div className="text-red-700 whitespace-pre-line leading-relaxed">{message}</div>
      </div>
      {onClose && (
        <button
          type="button"
          onClick={onClose}
          className="text-red-400 hover:text-red-600 p-1 rounded-md transition-colors"
        >
          <HiXMark className="w-4 h-4" />
        </button>
      )}
    </div>
  );
}
