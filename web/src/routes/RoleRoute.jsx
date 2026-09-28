import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

/**
 * RoleRoute
 * Wraps routes that require a specific user role.
 * Redirects to /unauthorized if the user's role is not in the allowedRoles list.
 *
 * Note: This is a UI convenience only — the C# Web API enforces actual authorization.
 *
 * @param {{ allowedRoles: string[] }} props
 */
export default function RoleRoute({ allowedRoles }) {
  const { user } = useAuth();

  if (!user || !allowedRoles.includes(user.role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
}
