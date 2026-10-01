import { Routes, Route, Navigate } from 'react-router-dom';
import { ROLES } from '../utils/constants';

// Route guards
import ProtectedRoute from './ProtectedRoute';
import RoleRoute from './RoleRoute';

// Layouts
import BackofficeLayout from '../layouts/BackofficeLayout';
import OperatorLayout from '../layouts/OperatorLayout';

// Auth pages
import LoginPage from '../pages/auth/LoginPage';

// Backoffice pages
import BackofficeDashboard from '../pages/dashboard/BackofficeDashboard';
import UserList from '../pages/users/UserList';
import PendingActivationsPage from '../pages/users/PendingActivationsPage';
import ProsumerList from '../pages/prosumers/ProsumerList';
import StationList from '../pages/stations/StationList';
import ReservationList from '../pages/reservations/ReservationList';

// Operator pages
import OperatorDashboard from '../pages/dashboard/OperatorDashboard';
import OperatorReservationList from '../pages/reservations/OperatorReservationList';
import SlotList from '../pages/slots/SlotList';
import HistoryPage from '../pages/reservations/HistoryPage';

// Shared pages
import NotFoundPage from '../pages/shared/NotFoundPage';
import UnauthorizedPage from '../pages/shared/UnauthorizedPage';

/**
 * AppRoutes — Central route tree for the application.
 *
 * Structure:
 *   /login                         — Public
 *   /backoffice/*                   — Protected + Backoffice role
 *   /operator/*                     — Protected + GridOperator role
 *   /unauthorized                   — Shown when role mismatch
 *   *                               — 404
 */
export default function AppRoutes() {
  return (
    <Routes>
      {/* ── Public Routes ── */}
      <Route path="/login" element={<LoginPage />} />

      {/* ── Backoffice Routes ── */}
      <Route element={<ProtectedRoute />}>
        <Route element={<RoleRoute allowedRoles={[ROLES.BACKOFFICE]} />}>
          <Route element={<BackofficeLayout />}>
            <Route path="/backoffice" element={<Navigate to="/backoffice/dashboard" replace />} />
            <Route path="/backoffice/dashboard" element={<BackofficeDashboard />} />
            <Route path="/backoffice/users" element={<UserList />} />
            <Route path="/backoffice/pending-activations" element={<PendingActivationsPage />} />
            <Route path="/backoffice/prosumers" element={<ProsumerList />} />
            <Route path="/backoffice/nodes" element={<StationList />} />
            <Route path="/backoffice/bookings" element={<ReservationList />} />
          </Route>
        </Route>
      </Route>

      {/* ── Grid Operator Routes ── */}
      <Route element={<ProtectedRoute />}>
        <Route element={<RoleRoute allowedRoles={[ROLES.GRID_OPERATOR]} />}>
          <Route element={<OperatorLayout />}>
            <Route path="/operator" element={<Navigate to="/operator/dashboard" replace />} />
            <Route path="/operator/dashboard" element={<OperatorDashboard />} />
            <Route path="/operator/bookings" element={<OperatorReservationList />} />
            <Route path="/operator/slots" element={<SlotList />} />
            <Route path="/operator/history" element={<HistoryPage />} />
          </Route>
        </Route>
      </Route>

      {/* ── Shared / Fallback Routes ── */}
      <Route path="/unauthorized" element={<UnauthorizedPage />} />
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
