/**
 * UI-only constants — used for display purposes and route navigation.
 * These do NOT enforce business rules. The API is the final authority.
 */

// Roles returned by the API after authentication
export const ROLES = {
  BACKOFFICE: 'Backoffice',
  GRID_OPERATOR: 'GridOperator',
};

// Status values used for StatusBadge display
export const STATUS = {
  ACTIVE: 'Active',
  INACTIVE: 'Inactive',
  PENDING: 'Pending',
  BLOCKED: 'Blocked',
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
  CANCELLED: 'Cancelled',
  COMPLETED: 'Completed',
};

// Booking status values for display
export const BOOKING_STATUS = {
  PENDING: 'Pending',
  CONFIRMED: 'Confirmed',
  ACTIVE: 'Active',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
};

// Slot status values for display
export const SLOT_STATUS = {
  AVAILABLE: 'Available',
  OCCUPIED: 'Occupied',
  RESERVED: 'Reserved',
  MAINTENANCE: 'Maintenance',
};

// Sidebar navigation definitions by role
export const BACKOFFICE_NAV = [
  { label: 'Dashboard', path: '/backoffice/dashboard', icon: 'HiOutlineHome' },
  { label: 'Users', path: '/backoffice/users', icon: 'HiOutlineUsers' },
  { label: 'Pending Activations', path: '/backoffice/pending-activations', icon: 'HiOutlineClipboardCheck' },
  { label: 'Prosumers', path: '/backoffice/prosumers', icon: 'HiOutlineUserGroup' },
  { label: 'Microgrid Nodes', path: '/backoffice/nodes', icon: 'HiOutlineCube' },
  { label: 'Bookings', path: '/backoffice/bookings', icon: 'HiOutlineCalendar' },
];

export const OPERATOR_NAV = [
  { label: 'Dashboard', path: '/operator/dashboard', icon: 'HiOutlineHome' },
  { label: 'Bookings', path: '/operator/bookings', icon: 'HiOutlineCalendar' },
  { label: 'Slot Availability', path: '/operator/slots', icon: 'HiOutlineBolt' },
  { label: 'History', path: '/operator/history', icon: 'HiOutlineClock' },
];
