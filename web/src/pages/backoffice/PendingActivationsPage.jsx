import React, { useState, useEffect } from 'react';
import PendingActivationTable from '../../components/backoffice/PendingActivationTable';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import ErrorAlert from '../../components/common/ErrorAlert';
import Button from '../../components/common/Button';
import { useNotification } from '../../context/NotificationContext';
import {
  getPendingActivations,
  approveActivation,
  rejectActivation,
} from '../../api/activationApi';
import { HiOutlineArrowPath } from 'react-icons/hi2';

export default function PendingActivationsPage() {
  const [activations, setActivations] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // Dialog targets
  const [approveTarget, setApproveTarget] = useState(null);
  const [rejectTarget, setRejectTarget] = useState(null);

  const { showSuccess, showError } = useNotification();

  const fetchActivations = async () => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const data = await getPendingActivations();
      setActivations(Array.isArray(data) ? data : data?.items || []);
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Failed to fetch pending activation queue from Web API.';
      setErrorMessage(msg);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchActivations();
  }, []);

  const handleConfirmApprove = async () => {
    if (!approveTarget) return;

    setIsSubmitting(true);
    try {
      await approveActivation(approveTarget.id || approveTarget._id);
      showSuccess(`Activation for '${approveTarget.fullName || approveTarget.nic}' approved.`);
      setApproveTarget(null);
      fetchActivations();
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Approval action rejected by server.';
      showError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleConfirmReject = async () => {
    if (!rejectTarget) return;

    setIsSubmitting(true);
    try {
      await rejectActivation(rejectTarget.id || rejectTarget._id);
      showSuccess(`Activation request for '${rejectTarget.fullName || rejectTarget.nic}' rejected.`);
      setRejectTarget(null);
      fetchActivations();
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Rejection failed on server.';
      showError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">
            Pending Activations Queue
          </h2>
          <p className="text-sm text-slate-500 mt-1">
            Review prosumer onboarding requests and dispatch approval decisions to the API
          </p>
        </div>

        <Button
          variant="secondary"
          size="sm"
          onClick={fetchActivations}
          isLoading={isLoading}
          icon={HiOutlineArrowPath}
        >
          Refresh Queue
        </Button>
      </div>

      {errorMessage && (
        <ErrorAlert
          title="Activation Queue Notice"
          message={errorMessage}
          onClose={() => setErrorMessage('')}
        />
      )}

      <PendingActivationTable
        activations={activations}
        isLoading={isLoading}
        onApprove={(item) => setApproveTarget(item)}
        onReject={(item) => setRejectTarget(item)}
      />

      {/* Approve Confirmation Dialog */}
      <ConfirmDialog
        isOpen={!!approveTarget}
        onClose={() => setApproveTarget(null)}
        onConfirm={handleConfirmApprove}
        title="Approve User Activation"
        message={`Authorize activation for ${approveTarget?.fullName || approveTarget?.nic}? The applicant will be granted active credentials on the microgrid network.`}
        confirmText="Confirm Approval"
        variant="success"
        isLoading={isSubmitting}
      />

      {/* Reject Confirmation Dialog */}
      <ConfirmDialog
        isOpen={!!rejectTarget}
        onClose={() => setRejectTarget(null)}
        onConfirm={handleConfirmReject}
        title="Reject Activation Request"
        message={`Are you sure you want to reject the registration request for ${rejectTarget?.fullName || rejectTarget?.nic}? This will mark the application rejected in MongoDB.`}
        confirmText="Confirm Rejection"
        variant="danger"
        isLoading={isSubmitting}
      />
    </div>
  );
}
