import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import StatsCard from '../../components/common/StatsCard';
import Card from '../../components/common/Card';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorAlert from '../../components/common/ErrorAlert';
import Button from '../../components/common/Button';
import { getOperatorDashboard } from '../../services/reservationService';
import {
  HiOutlineCalendarDays,
  HiOutlineClock,
  HiOutlineCheckBadge,
  HiOutlineBolt,
  HiOutlineArrowPath,
  HiOutlineDocumentChartBar,
} from 'react-icons/hi2';

export default function OperatorDashboard() {
  const [stats, setStats] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const fetchDashboardStats = async () => {
    setIsLoading(true);
    setError('');
    try {
      // Fetch operational summary computed centrally by the ASP.NET Web API
      const data = await getOperatorDashboard();
      setStats(data);
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Failed to synchronize operator dashboard with server.';
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardStats();
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">
            Grid Operator Command Console
          </h2>
          <p className="text-sm text-slate-500 mt-1">
            Real-time energy slot bookings, battery storage bay allocation, and trading dispatch
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="secondary"
            size="sm"
            onClick={fetchDashboardStats}
            isLoading={isLoading}
            icon={HiOutlineArrowPath}
          >
            Refresh Metrics
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={() => navigate('/operator/bookings')}
            icon={HiOutlineCalendarDays}
          >
            Manage Bookings
          </Button>
        </div>
      </div>

      {error && (
        <ErrorAlert
          title="Grid Operator Notice"
          message={error}
          onClose={() => setError('')}
        />
      )}

      {isLoading && !stats ? (
        <LoadingSpinner text="Retrieving operational metrics from Web API..." />
      ) : (
        <>
          {/* Key Metric Cards requested by specifications */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4">
            <StatsCard
              title="Current Active Bookings"
              value={stats?.currentBookingsCount ?? stats?.currentBookings}
              icon={HiOutlineBolt}
              subtitle="Actively trading on grid"
              color="emerald"
              onClick={() => navigate('/operator/bookings')}
            />

            <StatsCard
              title="Pending Bookings"
              value={stats?.pendingBookingsCount ?? stats?.pendingBookings}
              icon={HiOutlineClock}
              subtitle="Awaiting schedule cycle"
              color="amber"
              onClick={() => navigate('/operator/bookings')}
            />

            <StatsCard
              title="Approved Future Reservations"
              value={stats?.approvedFutureReservationsCount ?? stats?.approvedFutureReservations}
              icon={HiOutlineCheckBadge}
              subtitle="Scheduled within 7 days"
              color="blue"
              onClick={() => navigate('/operator/bookings')}
            />

            <StatsCard
              title="Pending Reservations"
              value={stats?.pendingReservationsCount ?? stats?.pendingReservations}
              icon={HiOutlineCalendarDays}
              subtitle="Queued slot requests"
              color="indigo"
              onClick={() => navigate('/operator/bookings')}
            />

            <StatsCard
              title="Historical Bookings"
              value={stats?.bookingHistoryCount ?? stats?.historyCount}
              icon={HiOutlineDocumentChartBar}
              subtitle="Completed and settled trades"
              color="purple"
              onClick={() => navigate('/operator/history')}
            />
          </div>

          {/* Operational Sections */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card
              title="Grid Dispatch Operations"
              subtitle="Shortcuts to real-time grid balancing tools"
            >
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div
                  onClick={() => navigate('/operator/slots')}
                  className="p-4 rounded-xl border border-slate-200 hover:border-blue-400 hover:bg-blue-50/40 cursor-pointer transition-all duration-150"
                >
                  <div className="font-semibold text-slate-800 text-sm">Battery Storage Bay Status</div>
                  <p className="text-xs text-slate-500 mt-1">
                    Examine Available, Reserved, and Occupied slots across solar hubs.
                  </p>
                </div>

                <div
                  onClick={() => navigate('/operator/bookings')}
                  className="p-4 rounded-xl border border-slate-200 hover:border-emerald-400 hover:bg-emerald-50/40 cursor-pointer transition-all duration-150"
                >
                  <div className="font-semibold text-slate-800 text-sm">Update / Cancel Bookings</div>
                  <p className="text-xs text-slate-500 mt-1">
                    Process changes adhering to the backend 12-hour notice rule.
                  </p>
                </div>
              </div>
            </Card>

            <Card
              title="Reservation Rules Summary (Enforced by Web API)"
              subtitle="Central business rules verified by C# backend"
            >
              <div className="space-y-2.5 text-xs text-slate-600">
                <div className="flex items-start gap-2 p-2.5 bg-slate-50 rounded-lg border border-slate-200">
                  <span className="font-bold text-blue-600 shrink-0">7-Day Window:</span>
                  <span>Energy slot reservations must be scheduled within a 7-day forward horizon.</span>
                </div>
                <div className="flex items-start gap-2 p-2.5 bg-slate-50 rounded-lg border border-slate-200">
                  <span className="font-bold text-amber-600 shrink-0">12-Hour Notice for Updates:</span>
                  <span>Booking modifications must be requested at least 12 hours prior to scheduled start.</span>
                </div>
                <div className="flex items-start gap-2 p-2.5 bg-slate-50 rounded-lg border border-slate-200">
                  <span className="font-bold text-red-600 shrink-0">12-Hour Notice for Cancellations:</span>
                  <span>Cancellations within 12 hours of scheduled delivery are blocked by the API.</span>
                </div>
              </div>
            </Card>
          </div>
        </>
      )}
    </div>
  );
}
