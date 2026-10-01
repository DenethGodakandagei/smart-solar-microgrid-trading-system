import React, { useState, useEffect } from 'react';
import ProsumerTable from '../../components/backoffice/ProsumerTable';
import ProsumerForm from '../../components/backoffice/ProsumerForm';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import SearchBar from '../../components/common/SearchBar';
import Button from '../../components/common/Button';
import ErrorAlert from '../../components/common/ErrorAlert';
import { useNotification } from '../../context/NotificationContext';
import {
  getProsumers,
  createProsumer,
  updateProsumer,
  deactivateProsumer,
  reactivateProsumer,
} from '../../services/prosumerService';
import { HiOutlineUserPlus, HiOutlineArrowPath } from 'react-icons/hi2';

export default function ProsumerList() {
  const [prosumers, setProsumers] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // Modals & Dialog targets
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedProsumer, setSelectedProsumer] = useState(null);
  const [deactivateTarget, setDeactivateTarget] = useState(null);
  const [reactivateTarget, setReactivateTarget] = useState(null);

  const { showSuccess, showError } = useNotification();

  const fetchProsumers = async () => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const data = await getProsumers(searchTerm ? { search: searchTerm } : {});
      setProsumers(Array.isArray(data) ? data : data?.items || []);
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Failed to fetch prosumer registry from Web API.';
      setErrorMessage(msg);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchProsumers();
  }, [searchTerm]);

  const handleOpenCreate = () => {
    setSelectedProsumer(null);
    setIsFormOpen(true);
  };

  const handleOpenEdit = (p) => {
    setSelectedProsumer(p);
    setIsFormOpen(true);
  };

  const handleSaveProsumer = async (formData, nic) => {
    setIsSubmitting(true);
    try {
      if (selectedProsumer) {
        await updateProsumer(nic, formData);
        showSuccess(`Prosumer profile (NIC: ${nic}) updated successfully.`);
      } else {
        await createProsumer(formData);
        showSuccess(`Prosumer (NIC: ${formData.nic}) registered successfully.`);
      }
      setIsFormOpen(false);
      fetchProsumers();
    } catch (err) {
      // Allow modal to display server rejection
      throw err;
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleConfirmDeactivate = async () => {
    if (!deactivateTarget) return;

    setIsSubmitting(true);
    try {
      await deactivateProsumer(deactivateTarget.nic);
      showSuccess(`Prosumer with NIC ${deactivateTarget.nic} has been deactivated.`);
      setDeactivateTarget(null);
      fetchProsumers();
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Deactivation rejected by Web API.';
      showError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleConfirmReactivate = async () => {
    if (!reactivateTarget) return;

    setIsSubmitting(true);
    try {
      // Reactivation is exclusive to Backoffice users (enforced by backend policy)
      await reactivateProsumer(reactivateTarget.nic);
      showSuccess(`Prosumer with NIC ${reactivateTarget.nic} has been reactivated.`);
      setReactivateTarget(null);
      fetchProsumers();
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Reactivation rejected by server.';
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
            Prosumer Profile Management
          </h2>
          <p className="text-sm text-slate-500 mt-1">
            Maintain registered solar generators and storage contributors indexed by National Identity Card (NIC)
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="secondary"
            size="sm"
            onClick={fetchProsumers}
            isLoading={isLoading}
            icon={HiOutlineArrowPath}
          >
            Refresh
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={handleOpenCreate}
            icon={HiOutlineUserPlus}
          >
            Register Prosumer
          </Button>
        </div>
      </div>

      {errorMessage && (
        <ErrorAlert
          title="Prosumer Service Notice"
          message={errorMessage}
          onClose={() => setErrorMessage('')}
        />
      )}

      {/* Search Bar - Search by NIC or Name */}
      <div className="flex items-center justify-between gap-4">
        <SearchBar
          value={searchTerm}
          onChange={setSearchTerm}
          onClear={() => setSearchTerm('')}
          placeholder="Search by NIC primary key or prosumer name..."
        />
      </div>

      {/* Prosumer Table */}
      <ProsumerTable
        prosumers={prosumers}
        isLoading={isLoading}
        onEdit={handleOpenEdit}
        onDeactivate={(p) => setDeactivateTarget(p)}
        onReactivate={(p) => setReactivateTarget(p)}
      />

      {/* Prosumer Form Modal */}
      <ProsumerForm
        isOpen={isFormOpen}
        onClose={() => setIsFormOpen(false)}
        onSave={handleSaveProsumer}
        prosumer={selectedProsumer}
        isLoading={isSubmitting}
      />

      {/* Deactivate Dialog */}
      <ConfirmDialog
        isOpen={!!deactivateTarget}
        onClose={() => setDeactivateTarget(null)}
        onConfirm={handleConfirmDeactivate}
        title="Deactivate Prosumer"
        message={`Are you sure you want to deactivate prosumer ${deactivateTarget?.fullName || deactivateTarget?.nic}? They will no longer be eligible to publish energy offerings.`}
        confirmText="Deactivate"
        variant="danger"
        isLoading={isSubmitting}
      />

      {/* Reactivate Dialog (Backoffice only) */}
      <ConfirmDialog
        isOpen={!!reactivateTarget}
        onClose={() => setReactivateTarget(null)}
        onConfirm={handleConfirmReactivate}
        title="Reactivate Prosumer Account"
        message={`Reactivate trading privileges for prosumer ${reactivateTarget?.fullName} (NIC: ${reactivateTarget?.nic})? Only Backoffice officers are permitted to perform this action.`}
        confirmText="Reactivate Account"
        variant="success"
        isLoading={isSubmitting}
      />
    </div>
  );
}
