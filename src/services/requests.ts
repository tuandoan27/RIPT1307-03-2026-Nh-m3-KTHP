// src/services/requests.ts
// Gọi real API:
//   GET /api/requests              (admin — danh sách tất cả)
//   GET /api/requests/{id}
//   PUT /api/requests/{id}/approve
//   PUT /api/requests/{id}/reject  body: { reason }
//   PUT /api/requests/{id}/return
import adminRequest from './adminRequest';

// ─── Types (shape khớp mockData để AdminRequestsPage không cần đổi) ───────
export type RequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'RETURNED' | 'OVERDUE';

export interface HistoryEntry {
  status: RequestStatus;
  by: string;
  timestamp: string;
  note: string;
}

export interface RequestItem {
  id: string;
  studentName: string;
  studentCode?: string;
  equipmentName: string;
  borrowDate: string;   // ← startDate
  returnDate: string;   // ← endDate
  submittedAt: string;  // ← createdAt
  status: RequestStatus;
  note?: string;
  reason?: string;
  history: HistoryEntry[]; // BE list endpoint không trả history → []
}

// ─── Internal BE response types ───────────────────────────────────────────
interface BackendListItem {
  id: number;
  studentName: string;
  studentCode: string;
  equipmentName: string;
  startDate: string;   // yyyy-MM-dd
  endDate: string;     // yyyy-MM-dd
  status: RequestStatus;
  createdAt: string;   // ISO datetime
}

interface BackendDetail extends BackendListItem {
  studentEmail: string;
  equipmentId: number;
  note?: string;
  reason?: string;
  penaltyApplied: boolean;
}

interface PageResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}

// ─── Mapping BE → FE ──────────────────────────────────────────────────────
function mapListItem(b: BackendListItem): RequestItem {
  return {
    id: String(b.id),
    studentName: b.studentName,
    studentCode: b.studentCode,
    equipmentName: b.equipmentName,
    borrowDate: b.startDate,
    returnDate: b.endDate,
    submittedAt: b.createdAt,
    status: b.status,
    history: [],
  };
}

function mapDetail(b: BackendDetail): RequestItem {
  return {
    ...mapListItem(b),
    note: b.note,
    reason: b.reason,
  };
}

// ─── Service ──────────────────────────────────────────────────────────────
/** Lấy toàn bộ yêu cầu (admin). Dùng pageSize lớn để load hết cho client-side filter. */
export const listRequests = async (): Promise<RequestItem[]> => {
  const res = await adminRequest.get<{ data: PageResponse<BackendListItem> }>('/requests', {
    params: { page: 1, pageSize: 500 },
  });
  return (res.data.data?.items ?? []).map(mapListItem);
};

export const getRequestById = async (id: string): Promise<RequestItem | null> => {
  const res = await adminRequest.get<{ data: BackendDetail }>(`/requests/${id}`);
  return res.data.data ? mapDetail(res.data.data) : null;
};

export const approveRequest = async (id: string): Promise<void> => {
  await adminRequest.put(`/requests/${id}/approve`);
};

export const rejectRequest = async (id: string, reason: string): Promise<void> => {
  await adminRequest.put(`/requests/${id}/reject`, { reason });
};

export const returnRequest = async (id: string): Promise<void> => {
  await adminRequest.put(`/requests/${id}/return`);
};

/** BE không có history endpoint riêng → trả array rỗng */
export const getRequestHistory = async (id: string): Promise<HistoryEntry[]> => {
  const req = await getRequestById(id);
  return req?.history ?? [];
};

export default {
  listRequests,
  getRequestById,
  approveRequest,
  rejectRequest,
  returnRequest,
  getRequestHistory,
};