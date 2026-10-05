import { ROLES, STATUS, BOOKING_STATUS, SLOT_STATUS } from '../utils/constants';

// Initial Mock Database State
export const mockDb = {
  users: [
    {
      id: 'USR-001',
      username: 'admin',
      fullName: 'System Administrator',
      email: 'admin@gmail.com',
      role: ROLES.BACKOFFICE,
      status: STATUS.ACTIVE,
    },
    {
      id: 'USR-002',
      username: 'rashi',
      fullName: 'Rashi Perera (Operator)',
      email: 'rashi@gmail.com',
      role: ROLES.GRID_OPERATOR,
      status: STATUS.ACTIVE,
    },
    {
      id: 'USR-003',
      username: 'operator2',
      fullName: 'Sunil Jayawardena',
      email: 'sunil.op@microgrid.lk',
      role: ROLES.GRID_OPERATOR,
      status: STATUS.ACTIVE,
    },
    {
      id: 'USR-004',
      username: 'officer_fernando',
      fullName: 'Kamal Fernando',
      email: 'kamal.backoffice@microgrid.lk',
      role: ROLES.BACKOFFICE,
      status: STATUS.INACTIVE,
    },
  ],

  activations: [
    {
      id: 'ACT-101',
      fullName: 'Anura Bandara',
      nic: '198512345678',
      email: 'anura.bandara@gmail.com',
      contactNumber: '+94 71 234 5678',
      requestedRole: 'Prosumer',
      createdAt: '2026-09-25T08:30:00Z',
      status: STATUS.PENDING,
    },
    {
      id: 'ACT-102',
      fullName: 'Nimali Senanayake',
      nic: '199276543210',
      email: 'nimali.sena@gmail.com',
      contactNumber: '+94 77 987 6543',
      requestedRole: 'Prosumer',
      createdAt: '2026-09-26T14:15:00Z',
      status: STATUS.PENDING,
    },
    {
      id: 'ACT-103',
      fullName: 'Dilan Wickramasinghe',
      nic: '199855443322',
      email: 'dilan.w@gmail.com',
      contactNumber: '+94 75 444 3322',
      requestedRole: 'GridOperator',
      createdAt: '2026-09-27T09:00:00Z',
      status: STATUS.PENDING,
    },
  ],

  prosumers: [
    {
      nic: '199012345678',
      fullName: 'Dinesh Gunasekara',
      email: 'dinesh.g@solar.lk',
      phone: '+94 77 123 4567',
      address: 'No. 45, Solar Avenue, Colombo 07',
      solarCapacityKw: 7.5,
      batteryStorageKwh: 12.0,
      gridNodeId: 'NODE-001',
      status: STATUS.ACTIVE,
    },
    {
      nic: '198245678901',
      fullName: 'Malini Jayasinghe',
      email: 'malini.j@microgrid.lk',
      phone: '+94 71 888 9900',
      address: 'No. 12, Green Hill Road, Kandy',
      solarCapacityKw: 10.0,
      batteryStorageKwh: 20.0,
      gridNodeId: 'NODE-002',
      status: STATUS.ACTIVE,
    },
    {
      nic: '199587654321',
      fullName: 'Kasun Rathnayake',
      email: 'kasun.r@gmail.com',
      phone: '+94 76 555 4433',
      address: 'Coastal View, Galle Fort',
      solarCapacityKw: 5.0,
      batteryStorageKwh: 8.5,
      gridNodeId: 'NODE-003',
      status: STATUS.ACTIVE,
    },
    {
      nic: '197823456789',
      fullName: 'Priyani Alwis',
      email: 'priyani.alwis@yahoo.com',
      phone: '+94 72 333 2211',
      address: 'No. 88, Lake Round, Kurunegala',
      solarCapacityKw: 6.2,
      batteryStorageKwh: 10.0,
      gridNodeId: 'NODE-001',
      status: STATUS.INACTIVE, // Can be reactivated by Backoffice!
    },
  ],

  nodes: [
    {
      id: 'NODE-001',
      name: 'Colombo Central Solar Hub',
      nodeCode: 'NODE-COL-01',
      latitude: 6.927079,
      longitude: 79.861244,
      capacityKwh: 350.0,
      batterySlotsCount: 8,
      availableSlotsCount: 5,
      occupiedSlotsCount: 2,
      reservedSlotsCount: 1,
      status: STATUS.ACTIVE,
      schedule: {
        operationalStartTime: '06:00',
        operationalEndTime: '18:00',
        peakTradingStartTime: '10:00',
        peakTradingEndTime: '14:00',
        maintenanceDay: 'Sunday',
      },
    },
    {
      id: 'NODE-002',
      name: 'Kandy Highland Microgrid Bay',
      nodeCode: 'NODE-KDY-02',
      latitude: 7.290572,
      longitude: 80.633728,
      capacityKwh: 200.0,
      batterySlotsCount: 6,
      availableSlotsCount: 3,
      occupiedSlotsCount: 2,
      reservedSlotsCount: 1,
      status: STATUS.ACTIVE,
      schedule: {
        operationalStartTime: '06:30',
        operationalEndTime: '18:30',
        peakTradingStartTime: '11:00',
        peakTradingEndTime: '15:00',
        maintenanceDay: 'Saturday',
      },
    },
    {
      id: 'NODE-003',
      name: 'Southern Coastal Solar Park',
      nodeCode: 'NODE-GAL-03',
      latitude: 6.053519,
      longitude: 80.220978,
      capacityKwh: 500.0,
      batterySlotsCount: 12,
      availableSlotsCount: 8,
      occupiedSlotsCount: 3,
      reservedSlotsCount: 1,
      status: STATUS.ACTIVE,
      schedule: {
        operationalStartTime: '05:30',
        operationalEndTime: '19:00',
        peakTradingStartTime: '09:30',
        peakTradingEndTime: '14:30',
        maintenanceDay: 'Monday',
      },
    },
  ],

  bookings: [
    {
      id: 'BKG-2026-001',
      bookingRef: 'BKG-2026-001',
      prosumerNic: '199012345678',
      prosumerName: 'Dinesh Gunasekara',
      nodeId: 'NODE-001',
      nodeName: 'Colombo Central Solar Hub',
      slotNumber: 1,
      energyKwh: 45.0,
      ratePerKwh: 38.5,
      // Starts in 48 hours — within 7 days and plenty of notice
      startTime: new Date(Date.now() + 48 * 3600 * 1000).toISOString(),
      endTime: new Date(Date.now() + 52 * 3600 * 1000).toISOString(),
      status: BOOKING_STATUS.CONFIRMED,
    },
    {
      id: 'BKG-2026-002',
      bookingRef: 'BKG-2026-002',
      prosumerNic: '198245678901',
      prosumerName: 'Malini Jayasinghe',
      nodeId: 'NODE-002',
      slotNumber: 2,
      energyKwh: 60.0,
      ratePerKwh: 40.0,
      // Active currently
      startTime: new Date(Date.now() - 1 * 3600 * 1000).toISOString(),
      endTime: new Date(Date.now() + 3 * 3600 * 1000).toISOString(),
      status: BOOKING_STATUS.ACTIVE,
    },
    {
      id: 'BKG-2026-003',
      bookingRef: 'BKG-2026-003',
      prosumerNic: '199587654321',
      prosumerName: 'Kasun Rathnayake',
      nodeId: 'NODE-003',
      slotNumber: 3,
      energyKwh: 30.0,
      ratePerKwh: 36.0,
      // Starts in 6 hours — LESS THAN 12 HOURS! This will trigger the 12h notice rule test!
      startTime: new Date(Date.now() + 6 * 3600 * 1000).toISOString(),
      endTime: new Date(Date.now() + 10 * 3600 * 1000).toISOString(),
      status: BOOKING_STATUS.CONFIRMED,
    },
    {
      id: 'BKG-2026-004',
      bookingRef: 'BKG-2026-004',
      prosumerNic: '199012345678',
      prosumerName: 'Dinesh Gunasekara',
      nodeId: 'NODE-001',
      slotNumber: 4,
      energyKwh: 25.0,
      ratePerKwh: 38.0,
      startTime: new Date(Date.now() + 72 * 3600 * 1000).toISOString(),
      endTime: new Date(Date.now() + 76 * 3600 * 1000).toISOString(),
      status: BOOKING_STATUS.PENDING,
    },
    {
      id: 'BKG-2026-005',
      bookingRef: 'BKG-2026-005',
      prosumerNic: '198245678901',
      prosumerName: 'Malini Jayasinghe',
      nodeId: 'NODE-002',
      slotNumber: 1,
      energyKwh: 50.0,
      ratePerKwh: 39.0,
      // Past completed
      startTime: new Date(Date.now() - 48 * 3600 * 1000).toISOString(),
      endTime: new Date(Date.now() - 44 * 3600 * 1000).toISOString(),
      status: BOOKING_STATUS.COMPLETED,
    },
  ],

  // Node slots generator helper
  getSlotsForNode: (nodeId) => {
    const node = mockDb.nodes.find((n) => n.id === nodeId) || mockDb.nodes[0];
    const total = node.batterySlotsCount || 8;
    const slots = [];

    for (let i = 1; i <= total; i++) {
      let status = 'Available';
      let prosumerNic = null;
      let currentEnergyKwh = 0;

      if (i === 1) {
        status = 'Occupied';
        prosumerNic = '199012345678';
        currentEnergyKwh = 18.5;
      } else if (i === 2) {
        status = 'Occupied';
        prosumerNic = '198245678901';
        currentEnergyKwh = 12.0;
      } else if (i === 3) {
        status = 'Reserved';
        prosumerNic = '199587654321';
        currentEnergyKwh = 0;
      }

      slots.push({
        id: `SLOT-${nodeId}-${i}`,
        slotNumber: i,
        status,
        prosumerNic,
        currentEnergyKwh,
        capacityKwh: 25.0,
      });
    }
    return slots;
  },
};
