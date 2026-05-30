import { getMockUsers, type UserItem, type PenaltyEntry, type BorrowEntry } from '@/mocks';

// In-memory clone of mock users
let USERS: UserItem[] = getMockUsers().map((u) => ({
  ...u,
  borrowHistory: [...u.borrowHistory],
  penaltyHistory: [...u.penaltyHistory],
}));

const delay = (ms = 200) => new Promise((res) => setTimeout(res, ms));

const genId = (prefix = 'id') => `${prefix}-${Math.random().toString(36).slice(2, 9)}`;

const clone = <T,>(v: T): T => JSON.parse(JSON.stringify(v));

export async function listUsers(): Promise<UserItem[]> {
  await delay();
  return clone(USERS);
}

export async function getUserById(id: string): Promise<UserItem> {
  await delay();
  const u = USERS.find((x) => x.id === id);
  if (!u) throw new Error('User not found');
  return clone(u);
}

export async function lockUser(id: string, reason: string, by = 'Admin'): Promise<UserItem> {
  await delay();
  const u = USERS.find((x) => x.id === id);
  if (!u) throw new Error('User not found');
  if (u.status === 'LOCKED') throw new Error('User already locked');
  u.status = 'LOCKED';
  const entry: PenaltyEntry = { id: genId('ph'), type: 'LOCK_USER', reason, by, timestamp: new Date().toISOString() };
  u.penaltyHistory = [entry, ...u.penaltyHistory];
  return clone(u);
}

export async function unlockUser(id: string, reason: string, by = 'Admin'): Promise<UserItem> {
  await delay();
  const u = USERS.find((x) => x.id === id);
  if (!u) throw new Error('User not found');
  if (u.status === 'ACTIVE') throw new Error('User is not locked');
  u.status = 'ACTIVE';
  const entry: PenaltyEntry = { id: genId('ph'), type: 'UNLOCK_USER', reason, by, timestamp: new Date().toISOString() };
  u.penaltyHistory = [entry, ...u.penaltyHistory];
  return clone(u);
}

export async function adjustPenalty(id: string, pointsDelta: number, reason: string, by = 'Admin'): Promise<UserItem> {
  await delay();
  const u = USERS.find((x) => x.id === id);
  if (!u) throw new Error('User not found');
  u.penaltyPoints = Math.max(0, (u.penaltyPoints || 0) + pointsDelta);
  const entry: PenaltyEntry = { id: genId('ph'), type: 'ADJUST_PENALTY', pointsChange: pointsDelta, reason, by, timestamp: new Date().toISOString() };
  u.penaltyHistory = [entry, ...u.penaltyHistory];
  return clone(u);
}

export async function getUserHistory(id: string): Promise<{ borrowHistory: BorrowEntry[]; penaltyHistory: PenaltyEntry[] }> {
  await delay();
  const u = USERS.find((x) => x.id === id);
  if (!u) throw new Error('User not found');
  return clone({ borrowHistory: u.borrowHistory, penaltyHistory: u.penaltyHistory });
}

export async function resetUsersForTests() {
  USERS = getMockUsers().map((u) => ({ ...u, borrowHistory: [...u.borrowHistory], penaltyHistory: [...u.penaltyHistory] }));
}

export default {
  listUsers,
  getUserById,
  lockUser,
  unlockUser,
  adjustPenalty,
  getUserHistory,
  resetUsersForTests,
};
