import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import StatsCard from '../../components/common/StatsCard';
import Card from '../../components/common/Card';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorAlert from '../../components/common/ErrorAlert';
import Button from '../../components/common/Button';
import { getBackofficeDashboard } from '../../api/bookingApi';
import {
  HiOutlineUsers,
  HiOutlineCheckCircle,
  HiOutlineClipboardDocumentCheck,
  HiOutlineUserGroup,
  HiOutlineCube,
  HiOutlineArrowPath,
  HiOutlinePlus,
} from 'react-icons/hi2';

export default function BackofficeDashboard() {
  const [stats, setStats] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const fetchDashboardData = async () => {
    setIsLoading(true);
    setError('');
    try {
      // Fetch summary metrics supplied exclusively by the C# Web API
      const data = await getBackofficeDashboard();
      setStats(data);
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Unable to load Backoffice dashboard statistics from server.';
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">
            Backoffice Administration Portal
          </h2>
          <p className="text-sm text-slate-500 mt-1">
            System administration, user authorization, prosumer verification, and microgrid infrastructure
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="secondary"
            size="sm"
            onClick={fetchDashboardData}
            isLoading={isLoading}
            icon={HiOutlineArrowPath}
          >
            Refresh
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={() => navigate('/backoffice/nodes')}
            icon={HiOutlinePlus}
          >
            Register Node
          </Button>
        </div>
      </div>

      {error && (
        <ErrorAlert
          title="Server Synchronization Notice"
          message={error}
          onClose={() => setError('')}
        />
      )}

      {isLoading && !stats ? (
        <LoadingSpinner text="Synchronizing dashboard with Web API..." />
      ) : (
        <>
          {/* Key Metric Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4">
            <StatsCard
              title="Total System Users"
              value={stats?.totalUsers}
              icon={HiOutlineUsers}
              subtitle="Registered web accounts"
              color="blue"
              onClick={() => navigate('/backoffice/users')}
            />

            <StatsCard
              title="Active Users"
              value={stats?.activeUsers}
              icon={HiOutlineCheckCircle}
              subtitle="Permitted to access portal"
              color="emerald"
              onClick={() => navigate('/backoffice/users')}
            />

            <StatsCard
              title="Pending Activations"
              value={stats?.pendingActivations}
              icon={HiOutlineClipboardDocumentCheck}
              subtitle="Require approval review"
              color="amber"
              onClick={() => navigate('/backoffice/pending-activations')}
            />

            <StatsCard
              title="Total Prosumers"
              value={stats?.totalProsumers}
              icon={HiOutlineUserGroup}
              subtitle="NIC registered producers"
              color="indigo"
              onClick={() => navigate('/backoffice/prosumers')}
            />

            <StatsCard
              title="Active Solar Nodes"
              value={stats?.activeNodes}
              icon={HiOutlineCube}
              subtitle="Online microgrid hubs"
              color="purple"
              onClick={() => navigate('/backoffice/nodes')}
            />
          </div>

          {/* Operational Status & Action Shortcuts */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card title="Quick Management Shortcuts" subtitle="Direct access to operational workflows">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div
                  onClick={() => navigate('/backoffice/pending-activations')}
                  className="p-4 rounded-xl border border-slate-200 hover:border-amber-400 hover:bg-amber-50/40 cursor-pointer transition-all duration-150"
                >
                  <div className="font-semibold text-slate-800 text-sm">Review Pending Activations</div>
                  <p className="text-xs text-slate-500 mt-1">
                    Process incoming registration queues verified by server rules.
                  </p>
                </div>

                <div
                  onClick={() => navigate('/backoffice/prosumers')}
                  className="p-4 rounded-xl border border-slate-200 hover:border-blue-400 hover:bg-blue-50/40 cursor-pointer transition-all duration-150"
                >
                  <div className="font-semibold text-slate-800 text-sm">Prosumer NIC Directory</div>
                  <p className="text-xs text-slate-500 mt-1">
                    Maintain prosumer solar profiles and handle reactivation requests.
                  </p>
                </div>

                <div
                  onClick={() => navigate('/backoffice/nodes')}
                  className="p-4 rounded-xl border border-slate-200 hover:border-purple-400 hover:bg-purple-50/40 cursor-pointer transition-all duration-150"
                >
                  <div className="font-semibold text-slate-800 text-sm">Microgrid Node Infrastructure</div>
                  <p className="text-xs text-slate-500 mt-1">
                    Manage GPS locations, kW/h capacity, and schedules.
                  </p>
                </div>

                <div
                  onClick={() => navigate('/backoffice/users')}
                  className="p-4 rounded-xl border border-slate-200 hover:border-emerald-400 hover:bg-emerald-50/40 cursor-pointer transition-all duration-150"
                >
                  <div className="font-semibold text-slate-800 text-sm">Account Administration</div>
                  <p className="text-xs text-slate-500 mt-1">
                    Manage Backoffice officers and Grid Operator accounts.
                  </p>
                </div>
              </div>
            </Card>

            <Card title="System Architecture & Integrity" subtitle="Client-server boundary guarantees">
              <div className="space-y-3 text-xs text-slate-600 leading-relaxed">
                <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
                  <strong className="text-slate-800 block mb-0.5">Pure UI Layer Implementation:</strong>
                  All business logic, database queries, and transaction rules reside exclusively in the C# ASP.NET Web API and MongoDB.
                </div>
                <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
                  <strong className="text-slate-800 block mb-0.5">Primary Key Enforcement:</strong>
                  National Identity Card (NIC) is maintained as the primary immutable identifier across all prosumer data transactions.
                </div>
                <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
                  <strong className="text-slate-800 block mb-0.5">Authorization Authority:</strong>
                  Backoffice officer rights are verified on each API invocation via bearer token headers.
                </div>
              </div>
            </Card>
          </div>
        </>
      )}
    </div>
  );
}
