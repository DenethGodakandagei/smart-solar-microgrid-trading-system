import { mockDb } from './mockData';
import { ROLES, STATUS, BOOKING_STATUS } from '../utils/constants';

/**
 * Handles mock API requests when the real C# Web API server is unreachable.
 * Mimics exact C# Web API responses and HTTP status codes (200, 400, 409).
 */
export async function handleMockRequest(config) {
  const method = (config.method || 'get').toLowerCase();
  const url = (config.url || '').replace(/^\/api/, '').replace(/^\//, '');
  const data = typeof config.data === 'string' ? JSON.parse(config.data || '{}') : (config.data || {});
  const params = config.params || {};

  // Artificial network latency simulation (150ms)
  await new Promise((resolve) => setTimeout(resolve, 150));

  // --- 1. AUTHENTICATION ---
  if (url === 'auth/login' && method === 'post') {
    const identifier = (data.username || '').toLowerCase().trim();

    // Default determination based on credentials or input
    let role = ROLES.BACKOFFICE;
    let name = 'Admin Officer';

    if (identifier.includes('rashi') || identifier.includes('operator')) {
      role = ROLES.GRID_OPERATOR;
      name = 'Rashi Perera (Operator)';
    } else if (identifier.includes('admin') || identifier.includes('backoffice')) {
      role = ROLES.BACKOFFICE;
      name = 'System Administrator';
    } else {
      // Find matching mock user or default to Backoffice
      const matched = mockDb.users.find((u) => u.email.toLowerCase() === identifier || u.username.toLowerCase() === identifier);
      if (matched) {
        role = matched.role;
        name = matched.fullName;
      }
    }

    const mockUser = {
      id: `USR-${Date.now().toString().slice(-4)}`,
      username: identifier.split('@')[0] || 'user',
      name,
      fullName: name,
      email: data.username,
      role,
    };

    return {
      status: 200,
      data: {
        token: `mock-jwt-${role.toLowerCase()}-${Date.now()}`,
        user: mockUser,
      },
    };
  }

  if (url === 'auth/me' && method === 'get') {
    const stored = localStorage.getItem('authUser');
    if (stored) {
      return { status: 200, data: JSON.parse(stored) };
    }
    return {
      status: 200,
      data: mockDb.users[0],
    };
  }

  if (url === 'auth/logout' && method === 'post') {
    return { status: 200, data: { message: 'Logged out successfully' } };
  }

  // --- 2. USERS ---
  if (url === 'users' && method === 'get') {
    let result = [...mockDb.users];
    if (params.search) {
      const q = params.search.toLowerCase();
      result = result.filter(
        (u) =>
          u.username.toLowerCase().includes(q) ||
          u.fullName.toLowerCase().includes(q) ||
          u.email.toLowerCase().includes(q)
      );
    }
    return { status: 200, data: result };
  }

  if (url === 'users' && method === 'post') {
    const newUser = {
      id: `USR-${Date.now().toString().slice(-3)}`,
      ...data,
      status: data.status || STATUS.ACTIVE,
    };
    mockDb.users.unshift(newUser);
    return { status: 201, data: newUser };
  }

  if (url.startsWith('users/') && url.endsWith('/deactivate') && method === 'patch') {
    const id = url.split('/')[1];
    const u = mockDb.users.find((user) => user.id === id);
    if (u) u.status = STATUS.INACTIVE;
    return { status: 200, data: u };
  }

  if (url.startsWith('users/') && method === 'put') {
    const id = url.split('/')[1];
    const index = mockDb.users.findIndex((u) => u.id === id);
    if (index !== -1) {
      mockDb.users[index] = { ...mockDb.users[index], ...data };
      return { status: 200, data: mockDb.users[index] };
    }
    return { status: 404, data: { message: 'User not found' } };
  }

  // --- 3. PENDING ACTIVATIONS ---
  if (url === 'activations/pending' && method === 'get') {
    return {
      status: 200,
      data: mockDb.activations.filter((a) => a.status === STATUS.PENDING),
    };
  }

  if (url.startsWith('activations/') && url.endsWith('/approve') && method === 'post') {
    const id = url.split('/')[1];
    const act = mockDb.activations.find((a) => a.id === id);
    if (act) {
      act.status = STATUS.APPROVED;
      // Also register as prosumer if requestedRole was Prosumer
      if (act.requestedRole === 'Prosumer' && !mockDb.prosumers.some((p) => p.nic === act.nic)) {
        mockDb.prosumers.unshift({
          nic: act.nic,
          fullName: act.fullName,
          email: act.email,
          phone: act.contactNumber,
          solarCapacityKw: 5.0,
          batteryStorageKwh: 10.0,
          status: STATUS.ACTIVE,
        });
      }
    }
    return { status: 200, data: act };
  }

  if (url.startsWith('activations/') && url.endsWith('/reject') && method === 'post') {
    const id = url.split('/')[1];
    const act = mockDb.activations.find((a) => a.id === id);
    if (act) act.status = STATUS.REJECTED;
    return { status: 200, data: act };
  }

  // --- 4. PROSUMERS ---
  if (url === 'prosumers' && method === 'get') {
    let result = [...mockDb.prosumers];
    if (params.search) {
      const q = params.search.toLowerCase();
      result = result.filter(
        (p) =>
          p.nic.toLowerCase().includes(q) ||
          p.fullName.toLowerCase().includes(q)
      );
    }
    return { status: 200, data: result };
  }

  if (url === 'prosumers' && method === 'post') {
    if (mockDb.prosumers.some((p) => p.nic === data.nic)) {
      const err = new Error();
      err.response = { status: 409, data: { message: `Prosumer with NIC ${data.nic} already exists in the system.` } };
      throw err;
    }
    const newP = { ...data, status: STATUS.ACTIVE };
    mockDb.prosumers.unshift(newP);
    return { status: 201, data: newP };
  }

  if (url.startsWith('prosumers/') && url.endsWith('/deactivate') && method === 'patch') {
    const nic = url.split('/')[1];
    const p = mockDb.prosumers.find((item) => item.nic === nic);
    if (p) p.status = STATUS.INACTIVE;
    return { status: 200, data: p };
  }

  if (url.startsWith('prosumers/') && url.endsWith('/reactivate') && method === 'patch') {
    const nic = url.split('/')[1];
    const p = mockDb.prosumers.find((item) => item.nic === nic);
    if (p) p.status = STATUS.ACTIVE;
    return { status: 200, data: p };
  }

  if (url.startsWith('prosumers/') && method === 'put') {
    const nic = url.split('/')[1];
    const index = mockDb.prosumers.findIndex((p) => p.nic === nic);
    if (index !== -1) {
      mockDb.prosumers[index] = { ...mockDb.prosumers[index], ...data };
      return { status: 200, data: mockDb.prosumers[index] };
    }
    return { status: 404, data: { message: 'Prosumer not found' } };
  }

  // --- 5. MICROGRID NODES ---
  if (url === 'nodes' && method === 'get') {
    let result = [...mockDb.nodes];
    if (params.search) {
      const q = params.search.toLowerCase();
      result = result.filter(
        (n) =>
          n.name.toLowerCase().includes(q) ||
          (n.nodeCode && n.nodeCode.toLowerCase().includes(q))
      );
    }
    return { status: 200, data: result };
  }

  if (url === 'nodes' && method === 'post') {
    const newNode = {
      id: `NODE-${Date.now().toString().slice(-3)}`,
      ...data,
      availableSlotsCount: data.batterySlotsCount || 8,
      occupiedSlotsCount: 0,
      reservedSlotsCount: 0,
      status: STATUS.ACTIVE,
      schedule: {
        operationalStartTime: '06:00',
        operationalEndTime: '18:00',
        peakTradingStartTime: '10:00',
        peakTradingEndTime: '14:00',
        maintenanceDay: 'Sunday',
      },
    };
    mockDb.nodes.push(newNode);
    return { status: 201, data: newNode };
  }

  if (url.startsWith('nodes/') && url.endsWith('/schedule') && method === 'put') {
    const id = url.split('/')[1];
    const node = mockDb.nodes.find((n) => n.id === id);
    if (node) {
      node.schedule = { ...node.schedule, ...data };
      return { status: 200, data: node };
    }
    return { status: 404, data: { message: 'Node not found' } };
  }

  /**
   * NODE DEACTIVATION RULE:
   * "If active energy reservations exist, server blocks deactivation."
   * NODE-001 and NODE-002 have active reservations in mock data.
   */
  if (url.startsWith('nodes/') && url.endsWith('/deactivate') && method === 'patch') {
    const id = url.split('/')[1];
    const hasActiveReservations = mockDb.bookings.some(
      (b) => b.nodeId === id && (b.status === BOOKING_STATUS.ACTIVE || b.status === BOOKING_STATUS.CONFIRMED)
    );

    if (hasActiveReservations) {
      const err = new Error();
      err.response = {
        status: 409,
        data: {
          message: 'Deactivation blocked: Active energy slot reservations currently exist on this microgrid hub.',
        },
      };
      throw err;
    }

    const node = mockDb.nodes.find((n) => n.id === id);
    if (node) node.status = STATUS.INACTIVE;
    return { status: 200, data: node };
  }

  if (url.startsWith('nodes/') && url.endsWith('/slots') && method === 'get') {
    const id = url.split('/')[1];
    return { status: 200, data: mockDb.getSlotsForNode(id) };
  }

  // --- 6. BOOKINGS ---
  if (url === 'bookings' && method === 'get') {
    let result = [...mockDb.bookings];
    if (params.search) {
      const q = params.search.toLowerCase();
      result = result.filter(
        (b) =>
          b.bookingRef.toLowerCase().includes(q) ||
          b.prosumerNic.toLowerCase().includes(q) ||
          b.prosumerName.toLowerCase().includes(q)
      );
    }
    if (params.status) {
      result = result.filter((b) => b.status === params.status);
    }
    if (params.nodeId) {
      result = result.filter((b) => b.nodeId === params.nodeId);
    }
    return { status: 200, data: result };
  }

  if (url === 'bookings/history' && method === 'get') {
    const completedOrCancelled = mockDb.bookings.filter(
      (b) => b.status === BOOKING_STATUS.COMPLETED || b.status === BOOKING_STATUS.CANCELLED
    );
    return {
      status: 200,
      data: {
        items: completedOrCancelled,
        totalCount: completedOrCancelled.length,
        totalPages: 1,
      },
    };
  }

  /**
   * CANCELLATION RULE (Enforced by server):
   * "Cancellations require at least 12 hours' notice."
   */
  if (url.startsWith('bookings/') && url.endsWith('/cancel') && method === 'patch') {
    const id = url.split('/')[1];
    const booking = mockDb.bookings.find((b) => b.id === id);

    if (booking) {
      const startMs = new Date(booking.startTime).getTime();
      const nowMs = Date.now();
      const hoursUntilStart = (startMs - nowMs) / (1000 * 60 * 60);

      // If scheduled in less than 12 hours, reject with 400!
      if (hoursUntilStart > 0 && hoursUntilStart < 12) {
        const err = new Error();
        err.response = {
          status: 400,
          data: {
            message: 'Cancellation is not allowed within 12 hours of the scheduled start time.',
          },
        };
        throw err;
      }

      booking.status = BOOKING_STATUS.CANCELLED;
      return { status: 200, data: booking };
    }
    return { status: 404, data: { message: 'Booking not found' } };
  }

  if (url.startsWith('bookings/') && method === 'put') {
    const id = url.split('/')[1];
    const booking = mockDb.bookings.find((b) => b.id === id);
    if (booking) {
      const startMs = new Date(booking.startTime).getTime();
      const nowMs = Date.now();
      const hoursUntilStart = (startMs - nowMs) / (1000 * 60 * 60);

      if (hoursUntilStart > 0 && hoursUntilStart < 12) {
        const err = new Error();
        err.response = {
          status: 400,
          data: {
            message: 'Booking updates require at least 12 hours notice prior to scheduled slot.',
          },
        };
        throw err;
      }

      Object.assign(booking, data);
      return { status: 200, data: booking };
    }
    return { status: 404, data: { message: 'Booking not found' } };
  }

  // --- 7. DASHBOARDS ---
  if (url === 'dashboard/backoffice' && method === 'get') {
    return {
      status: 200,
      data: {
        totalUsers: mockDb.users.length,
        activeUsers: mockDb.users.filter((u) => u.status === STATUS.ACTIVE).length,
        pendingActivations: mockDb.activations.filter((a) => a.status === STATUS.PENDING).length,
        totalProsumers: mockDb.prosumers.length,
        activeNodes: mockDb.nodes.filter((n) => n.status === STATUS.ACTIVE).length,
      },
    };
  }

  if (url === 'dashboard/operator' && method === 'get') {
    return {
      status: 200,
      data: {
        currentBookingsCount: mockDb.bookings.filter((b) => b.status === BOOKING_STATUS.ACTIVE).length,
        pendingBookingsCount: mockDb.bookings.filter((b) => b.status === BOOKING_STATUS.PENDING).length,
        approvedFutureReservationsCount: mockDb.bookings.filter((b) => b.status === BOOKING_STATUS.CONFIRMED).length,
        pendingReservationsCount: mockDb.bookings.filter((b) => b.status === BOOKING_STATUS.PENDING).length,
        bookingHistoryCount: mockDb.bookings.filter(
          (b) => b.status === BOOKING_STATUS.COMPLETED || b.status === BOOKING_STATUS.CANCELLED
        ).length,
      },
    };
  }

  // Fallback for unhandled endpoints
  return { status: 200, data: [] };
}
