import { getMockRequests, type RequestItem, getMockEquipment, type Equipment } from '@/mocks';
import { isValidTransition } from '../pages/AdminRequests/stateMachine';

// In-memory stores (mock backend)
const REQUESTS: RequestItem[] = getMockRequests();
const EQUIPMENTS: Equipment[] = getMockEquipment(); // modify in-place for simplicity

const simulate = (ms = 400) => new Promise((res) => setTimeout(res, ms));

export const listRequests = async () => {
  await simulate();
  return REQUESTS;
};

export const getRequestById = async (id: string) => {
  await simulate(200);
  return REQUESTS.find((r) => r.id === id) || null;
};

export const approveRequest = async (id: string, admin = 'Admin') => {
  await simulate(400);
  const req = REQUESTS.find((r) => r.id === id);
  if (!req) throw new Error('Request not found');
  if (!isValidTransition(req.status, 'APPROVED')) throw new Error('Invalid transition');

  const equip = EQUIPMENTS.find((e) => e.id === req.equipmentId);
  if (!equip) throw new Error('Equipment not found');
  if (equip.availableQuantity <= 0) throw new Error('Insufficient stock to approve');

  // Simulate atomic transaction: re-check then update both
  equip.availableQuantity = Math.max(0, equip.availableQuantity - 1);
  req.status = 'APPROVED';
  req.history.push({ status: 'APPROVED', by: admin, timestamp: new Date().toISOString(), note: 'Approved' });
  return req;
};

export const rejectRequest = async (id: string, reason: string, admin = 'Admin') => {
  await simulate(300);
  const req = REQUESTS.find((r) => r.id === id);
  if (!req) throw new Error('Request not found');
  if (!isValidTransition(req.status, 'REJECTED')) throw new Error('Invalid transition');

  req.status = 'REJECTED';
  req.reason = reason;
  req.history.push({ status: 'REJECTED', by: admin, timestamp: new Date().toISOString(), note: reason });
  return req;
};

export const returnRequest = async (id: string, admin = 'Admin') => {
  await simulate(300);
  const req = REQUESTS.find((r) => r.id === id);
  if (!req) throw new Error('Request not found');
  if (!isValidTransition(req.status, 'RETURNED')) throw new Error('Invalid transition');

  const equip = EQUIPMENTS.find((e) => e.id === req.equipmentId);
  if (!equip) throw new Error('Equipment not found');
  equip.availableQuantity = Math.min(equip.totalQuantity, equip.availableQuantity + 1);
  req.status = 'RETURNED';
  req.history.push({ status: 'RETURNED', by: admin, timestamp: new Date().toISOString(), note: 'Returned' });
  return req;
};

export const getRequestHistory = async (id: string) => {
  await simulate(200);
  const req = REQUESTS.find((r) => r.id === id);
  return req ? req.history : [];
};

export default {
  listRequests,
  getRequestById,
  approveRequest,
  rejectRequest,
  returnRequest,
  getRequestHistory,
};
