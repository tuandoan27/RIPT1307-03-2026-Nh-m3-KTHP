// src/services/users.ts
import adminRequest from './adminRequest';

// ─── Types ────────────────────────────────────────────────────────────────
export interface BorrowEntry {
  id: string;
  equipmentName: string;
  startDate: string;
  endDate: string;
  status: string;
  createdAt: string;
}

export interface PenaltyEntry {
  id: string;
  type: string;
  reason: string;
  pointsChange?: number;
  by: string;
  timestamp: string;
}

export interface UserItem {
  id: string;
  fullName: string;
  studentCode: string;
  email: string;
  role: string;
  penaltyPoints: number;
  status: 'ACTIVE' | 'LOCKED';
  createdAt: string;
  borrowHistory: BorrowEntry[];
  penaltyHistory: PenaltyEntry[];
}

// ─── Internal BE response types ───────────────────────────────────────────
interface BackendUserListItem {
  id: number;
  fullName: string;
  studentCode: string;
  email: string;
  role: string;
  penaltyPoint: number;
  locked: boolean;   // ✅ FIX: backend trả về "locked", không phải "isLocked"
  createdAt: string;
}

interface BackendUserDetail extends BackendUserListItem {
  requests: Array<{
    id: number;
    equipmentName: string;
    startDate: string;
    endDate: string;
    status: string;
    createdAt: string;
  }>;
}

interface PageResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}

// ─── Mapping BE → FE ──────────────────────────────────────────────────────
function mapListItem(b: BackendUserListItem): UserItem {
  return {
    id: String(b.id),
    fullName: b.fullName,
    studentCode: b.studentCode,
    email: b.email,
    role: b.role,
    penaltyPoints: b.penaltyPoint,
    status: b.locked ? 'LOCKED' : 'ACTIVE',  // ✅ FIX: b.isLocked → b.locked
    createdAt: b.createdAt,
    borrowHistory: [],
    penaltyHistory: [],
  };
}

function mapDetail(b: BackendUserDetail): UserItem {
  return {
    ...mapListItem(b),
    borrowHistory: (b.requests ?? []).map((r) => ({
      id: String(r.id),
      equipmentName: r.equipmentName,
      startDate: r.startDate,
      endDate: r.endDate,
      status: r.status,
      createdAt: r.createdAt,
    })),
  };
}

// ─── Service ──────────────────────────────────────────────────────────────
export async function listUsers(): Promise<UserItem[]> {
  const res = await adminRequest.get<{ data: PageResponse<BackendUserListItem> }>('/admin/users', {
    params: { page: 1, pageSize: 200 },
  });
  return (res.data.data?.items ?? []).map(mapListItem);
}

export async function getUserById(id: string): Promise<UserItem> {
  const res = await adminRequest.get<{ data: BackendUserDetail }>(`/admin/users/${id}`);
  if (!res.data.data) throw new Error('User not found');
  return mapDetail(res.data.data);
}

export async function lockUser(id: string, _reason?: string, _by?: string): Promise<UserItem> {
  await adminRequest.put(`/admin/users/${id}/lock`);
  return getUserById(id);
}

export async function unlockUser(id: string, _reason?: string, _by?: string): Promise<UserItem> {
  await adminRequest.put(`/admin/users/${id}/unlock`);
  return getUserById(id);
}

export async function adjustPenalty(
  id: string,
  pointsDelta: number,
  reason: string,
  _by?: string,
): Promise<UserItem> {
  const res = await adminRequest.put<{ data: BackendUserDetail }>(
    `/admin/users/${id}/adjust-penalty`,
    { delta: pointsDelta, reason },
  );
  return mapDetail(res.data.data);
}

export async function resetPenalty(id: string): Promise<void> {
  await adminRequest.put(`/admin/users/${id}/reset-penalty`);
}

export async function getUserHistory(id: string) {
  const u = await getUserById(id);
  return { borrowHistory: u.borrowHistory, penaltyHistory: u.penaltyHistory };
}

export async function resetUsersForTests() {
  console.warn('[users] resetUsersForTests: no-op on real API');
}

export default {
  listUsers,
  getUserById,
  lockUser,
  unlockUser,
  adjustPenalty,
  resetPenalty,
  getUserHistory,
  resetUsersForTests,
};