import React, { useState, useEffect } from 'react';
import SlotAvailabilityGrid from '../../components/operator/SlotAvailabilityGrid';
import SlotStatusCard from '../../components/operator/SlotStatusCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorAlert from '../../components/common/ErrorAlert';
import Button from '../../components/common/Button';
import { getNodes, getNodeSlots } from '../../api/nodeApi';
import { HiOutlineArrowPath } from 'react-icons/hi2';

export default function SlotsPage() {
  const [nodes, setNodes] = useState([]);
  const [selectedNode, setSelectedNode] = useState(null);
  const [slots, setSlots] = useState([]);

  const [isLoadingNodes, setIsLoadingNodes] = useState(true);
  const [isLoadingSlots, setIsLoadingSlots] = useState(false);
  const [error, setError] = useState('');

  // Fetch nodes on mount
  const fetchNodesList = async () => {
    setIsLoadingNodes(true);
    setError('');
    try {
      const data = await getNodes();
      const nodeList = Array.isArray(data) ? data : data?.items || [];
      setNodes(nodeList);

      if (nodeList.length > 0 && !selectedNode) {
        setSelectedNode(nodeList[0]);
      }
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Failed to fetch microgrid hubs.';
      setError(msg);
    } finally {
      setIsLoadingNodes(false);
    }
  };

  useEffect(() => {
    fetchNodesList();
  }, []);

  // Fetch slots whenever selectedNode changes
  const fetchSlotsForNode = async (node) => {
    if (!node) return;
    setIsLoadingSlots(true);
    setError('');
    try {
      const nodeId = node.id || node._id;
      // Exclusively call Web API — frontend does not calculate slot availability
      const slotData = await getNodeSlots(nodeId);
      setSlots(Array.isArray(slotData) ? slotData : slotData?.slots || []);
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        `Failed to retrieve slots for ${node.name}.`;
      setError(msg);
      setSlots([]);
    } finally {
      setIsLoadingSlots(false);
    }
  };

  useEffect(() => {
    if (selectedNode) {
      fetchSlotsForNode(selectedNode);
    }
  }, [selectedNode]);

  const handleSelectNode = (node) => {
    setSelectedNode(node);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">
            Battery Storage Slot Availability
          </h2>
          <p className="text-sm text-slate-500 mt-1">
            Real-time occupancy status of battery bays across microgrid solar hubs
          </p>
        </div>

        <Button
          variant="secondary"
          size="sm"
          onClick={() => {
            fetchNodesList();
            if (selectedNode) fetchSlotsForNode(selectedNode);
          }}
          isLoading={isLoadingNodes || isLoadingSlots}
          icon={HiOutlineArrowPath}
        >
          Refresh Slot Grid
        </Button>
      </div>

      {error && <ErrorAlert message={error} onClose={() => setError('')} />}

      {/* Node selection list */}
      <div>
        <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500 mb-3">
          Select Solar Microgrid Hub
        </h3>

        {isLoadingNodes ? (
          <LoadingSpinner text="Loading microgrid hubs..." />
        ) : nodes.length === 0 ? (
          <div className="p-6 bg-white rounded-xl border border-dashed border-slate-200 text-center text-sm text-slate-500">
            No microgrid hubs available. Backoffice officers can register nodes.
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {nodes.map((node) => (
              <SlotStatusCard
                key={node.id || node._id}
                node={node}
                isSelected={selectedNode?.id === node.id || selectedNode?._id === node._id}
                onSelect={handleSelectNode}
              />
            ))}
          </div>
        )}
      </div>

      {/* Visual Slot Grid */}
      <SlotAvailabilityGrid
        slots={slots}
        nodeName={selectedNode?.name}
        isLoading={isLoadingSlots}
      />
    </div>
  );
}
