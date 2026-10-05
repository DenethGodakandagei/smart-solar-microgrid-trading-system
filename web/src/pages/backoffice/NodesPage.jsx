import React, { useState, useEffect } from 'react';
import NodeTable from '../../components/backoffice/NodeTable';
import NodeForm from '../../components/backoffice/NodeForm';
import NodeScheduleEditor from '../../components/backoffice/NodeScheduleEditor';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import SearchBar from '../../components/common/SearchBar';
import Button from '../../components/common/Button';
import ErrorAlert from '../../components/common/ErrorAlert';
import { useNotification } from '../../context/NotificationContext';
import {
  getNodes,
  createNode,
  updateNode,
  updateNodeSchedule,
  deactivateNode,
} from '../../api/nodeApi';
import { HiOutlinePlus, HiOutlineArrowPath } from 'react-icons/hi2';

export default function NodesPage() {
  const [nodes, setNodes] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // Tracks nodes where the server blocked deactivation due to active energy reservations
  const [blockedNodeIds, setBlockedNodeIds] = useState([]);

  // Modals & targets
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedNode, setSelectedNode] = useState(null);
  const [isScheduleOpen, setIsScheduleOpen] = useState(false);
  const [scheduleTargetNode, setScheduleTargetNode] = useState(null);
  const [deactivateTarget, setDeactivateTarget] = useState(null);

  const { showSuccess, showError } = useNotification();

  const fetchNodes = async () => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const data = await getNodes(searchTerm ? { search: searchTerm } : {});
      setNodes(Array.isArray(data) ? data : data?.items || []);
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Failed to fetch microgrid nodes from Web API.';
      setErrorMessage(msg);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchNodes();
  }, [searchTerm]);

  const handleOpenCreate = () => {
    setSelectedNode(null);
    setIsFormOpen(true);
  };

  const handleOpenEdit = (node) => {
    setSelectedNode(node);
    setIsFormOpen(true);
  };

  const handleOpenSchedule = (node) => {
    setScheduleTargetNode(node);
    setIsScheduleOpen(true);
  };

  const handleSaveNode = async (formData, id) => {
    setIsSubmitting(true);
    try {
      if (id) {
        await updateNode(id, formData);
        showSuccess(`Microgrid node '${formData.name}' updated.`);
      } else {
        await createNode(formData);
        showSuccess(`Microgrid node '${formData.name}' registered.`);
      }
      setIsFormOpen(false);
      fetchNodes();
    } catch (err) {
      throw err;
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSaveSchedule = async (scheduleData, id) => {
    setIsSubmitting(true);
    try {
      await updateNodeSchedule(id, scheduleData);
      showSuccess(`Operational schedule updated for node.`);
      setIsScheduleOpen(false);
      fetchNodes();
    } catch (err) {
      throw err;
    } finally {
      setIsSubmitting(false);
    }
  };

  /**
   * Deactivate node logic:
   * Note: The React frontend DOES NOT check active reservations.
   * We call the API directly. If the server returns a 409 or business error indicating
   * that active energy reservations exist, we catch the response and display the exact
   * server message to the user, adding the node to the blocked list.
   */
  const handleConfirmDeactivate = async () => {
    if (!deactivateTarget) return;

    const targetId = deactivateTarget.id || deactivateTarget._id;
    setIsSubmitting(true);
    try {
      await deactivateNode(targetId);
      showSuccess(`Node '${deactivateTarget.name}' has been deactivated.`);
      // Remove from blocked list if it was previously blocked
      setBlockedNodeIds((prev) => prev.filter((id) => id !== targetId));
      setDeactivateTarget(null);
      fetchNodes();
    } catch (err) {
      // Server rejected the deactivation (e.g. 409 Conflict: active reservations exist)
      const serverMessage =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Node deactivation was blocked by the server.';

      // Display the server message prominently
      setErrorMessage(`Deactivation Blocked: ${serverMessage}`);

      // Track this node so the UI visually flags that reservations prevent deactivation
      if (!blockedNodeIds.includes(targetId)) {
        setBlockedNodeIds((prev) => [...prev, targetId]);
      }
      setDeactivateTarget(null);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">
            Microgrid Node Infrastructure
          </h2>
          <p className="text-sm text-slate-500 mt-1">
            Manage solar distribution hubs, GPS coordinates, rated kW/h capacities, and battery bays
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="secondary"
            size="sm"
            onClick={fetchNodes}
            isLoading={isLoading}
            icon={HiOutlineArrowPath}
          >
            Refresh
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={handleOpenCreate}
            icon={HiOutlinePlus}
          >
            Register Node
          </Button>
        </div>
      </div>

      {errorMessage && (
        <ErrorAlert
          title="Server Deactivation Rule Notice"
          message={errorMessage}
          onClose={() => setErrorMessage('')}
        />
      )}

      {/* Search / Filter */}
      <div className="flex items-center justify-between gap-4">
        <SearchBar
          value={searchTerm}
          onChange={setSearchTerm}
          onClear={() => setSearchTerm('')}
          placeholder="Search by node name or code..."
        />
      </div>

      {/* Nodes Table */}
      <NodeTable
        nodes={nodes}
        isLoading={isLoading}
        blockedNodeIds={blockedNodeIds}
        onEdit={handleOpenEdit}
        onEditSchedule={handleOpenSchedule}
        onDeactivate={(node) => setDeactivateTarget(node)}
      />

      {/* Node Form Modal */}
      <NodeForm
        isOpen={isFormOpen}
        onClose={() => setIsFormOpen(false)}
        onSave={handleSaveNode}
        node={selectedNode}
        isLoading={isSubmitting}
      />

      {/* Schedule Editor Modal */}
      <NodeScheduleEditor
        isOpen={isScheduleOpen}
        onClose={() => setIsScheduleOpen(false)}
        onSave={handleSaveSchedule}
        node={scheduleTargetNode}
        isLoading={isSubmitting}
      />

      {/* Deactivate Confirm Dialog */}
      <ConfirmDialog
        isOpen={!!deactivateTarget}
        onClose={() => setDeactivateTarget(null)}
        onConfirm={handleConfirmDeactivate}
        title="Deactivate Microgrid Node"
        message={`Attempt to deactivate ${deactivateTarget?.name}? Note: The server will reject this action if active energy reservations are currently allocated.`}
        confirmText="Proceed with Deactivation"
        variant="danger"
        isLoading={isSubmitting}
      />
    </div>
  );
}
