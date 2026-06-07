// src/services/activityLogs.ts
// Gọi real API: GET /api/admin/activity-logs
import adminRequest from './adminRequest';

// ─── Types (shape khớp mockData để page không cần đổi) ───────────────────
export interface ActivityLogItem {
  id: number;
  actor: string;           // ← performedBy
  action: string;
  objectType: string;      // ← targetType
  objectId: number | null; // ← targetId
  objectName?: string;     // BE không có — để undefined
  details: string;         // ← detail
  timestamp: string;       // ← createdAt (ISO)
}

// ─── Internal BE response types ───────────────────────────────────────────
interface ActivityLogResponse {
  id: number;
  performedBy: string;
  action: string;
  targetType: string | null;
  targetId: number | null;
  detail: string | null;
  createdAt: string;
}

interface PageResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}

// ─── Mapping BE → FE ──────────────────────────────────────────────────────
function mapLog(b: ActivityLogResponse): ActivityLogItem {
  return {
    id: b.id,
    actor: b.performedBy ?? 'System',
    action: b.action,
    objectType: b.targetType ?? '',
    objectId: b.targetId ?? null,
    details: b.detail ?? '',
    timestamp: b.createdAt,
  };
}

// ─── Service ──────────────────────────────────────────────────────────────
/**
 * Lấy danh sách activity logs.
 *
 * Lưu ý mapping filter:
 *   actor      → không có trên BE (filter client-side)
 *   objectType → targetType (BE)
 *   dateFrom   → startDate  (BE, format yyyy-MM-dd)
 *   dateTo     → endDate    (BE, format yyyy-MM-dd)
 *   q          → không có trên BE (filter client-side)
 */
export async function listActivityLogs(filters?: {
  action?: string;
  actor?: string;
  objectType?: string;
  dateFrom?: string; // ISO string
  dateTo?: string;   // ISO string
  q?: string;
}): Promise<ActivityLogItem[]> {
  const params: Record<string, any> = { page: 1, pageSize: 200 };

  if (filters?.action)     params.action     = filters.action;
  if (filters?.objectType) params.targetType  = filters.objectType;
  // dateFrom/dateTo là ISO string từ DatePicker → cắt lấy phần date
  if (filters?.dateFrom)   params.startDate   = filters.dateFrom.substring(0, 10);
  if (filters?.dateTo)     params.endDate     = filters.dateTo.substring(0, 10);

  const res = await adminRequest.get<{ data: PageResponse<ActivityLogResponse> }>(
    '/admin/activity-logs',
    { params },
  );

  let items = (res.data.data?.items ?? []).map(mapLog);

  // Client-side filter cho actor & q (BE không hỗ trợ tìm theo tên / full-text)
  if (filters?.actor?.trim()) {
    const lc = filters.actor.toLowerCase();
    items = items.filter((i) => i.actor.toLowerCase().includes(lc));
  }
  if (filters?.q?.trim()) {
    const lc = filters.q.toLowerCase();
    items = items.filter(
      (i) =>
        i.actor.toLowerCase().includes(lc) ||
        (i.objectType ?? '').toLowerCase().includes(lc) ||
        (i.details ?? '').toLowerCase().includes(lc),
    );
  }

  return items;
}

export default { listActivityLogs };