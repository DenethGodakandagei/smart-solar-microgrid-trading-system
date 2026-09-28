import React from 'react';
import Modal from './Modal';
import Button from './Button';
import { HiExclamationTriangle, HiInformationCircle } from 'react-icons/hi2';

export default function ConfirmDialog({
  isOpen,
  onClose,
  onConfirm,
  title = 'Confirm Action',
  message = 'Are you sure you want to proceed with this action?',
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  variant = 'danger',
  isLoading = false,
}) {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title} maxWidth="max-w-md">
      <div className="flex items-start gap-4">
        <div
          className={`p-3 rounded-xl shrink-0 ${
            variant === 'danger'
              ? 'bg-red-50 text-red-600 border border-red-100'
              : 'bg-blue-50 text-blue-600 border border-blue-100'
          }`}
        >
          {variant === 'danger' ? (
            <HiExclamationTriangle className="w-6 h-6" />
          ) : (
            <HiInformationCircle className="w-6 h-6" />
          )}
        </div>
        <div className="flex-1">
          <p className="text-sm text-slate-600 leading-relaxed">{message}</p>
        </div>
      </div>

      <div className="mt-6 flex justify-end gap-3 pt-4 border-t border-slate-100">
        <Button variant="secondary" onClick={onClose} disabled={isLoading}>
          {cancelText}
        </Button>
        <Button variant={variant} onClick={onConfirm} isLoading={isLoading}>
          {confirmText}
        </Button>
      </div>
    </Modal>
  );
}
