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
import BackofficeDashboard from '../pages/backoffice/BackofficeDashboard';
import UsersPage from '../pages/backoffice/UsersPage';
import PendingActivationsPage from '../pages/backoffice/PendingActivationsPage';
import ProsumersPage from '../pages/backoffice/ProsumersPage';
import NodesPage from '../pages/backoffice/NodesPage';
import BookingsPage from '../pages/backoffice/BookingsPage';

// Operator pages
import OperatorDashboard from '../pages/operator/OperatorDashboard';
import OperatorBookingsPage from '../pages/operator/OperatorBookingsPage';
import SlotsPage from '../pages/operator/SlotsPage';
import HistoryPage from '../pages/operator/HistoryPage';

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
            <Route path="/backoffice/users" element={<UsersPage />} />
            <Route path="/backoffice/pending-activations" element={<PendingActivationsPage />} />
            <Route path="/backoffice/prosumers" element={<ProsumersPage />} />
            <Route path="/backoffice/nodes" element={<NodesPage />} />
            <Route path="/backoffice/bookings" element={<BookingsPage />} />
          </Route>
        </Route>
      </Route>

      {/* ── Grid Operator Routes ── */}
      <Route element={<ProtectedRoute />}>
        <Route element={<RoleRoute allowedRoles={[ROLES.GRID_OPERATOR]} />}>
          <Route element={<OperatorLayout />}>
            <Route path="/operator" element={<Navigate to="/operator/dashboard" replace />} />
            <Route path="/operator/dashboard" element={<OperatorDashboard />} />
            <Route path="/operator/bookings" element={<OperatorBookingsPage />} />
            <Route path="/operator/slots" element={<SlotsPage />} />
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
