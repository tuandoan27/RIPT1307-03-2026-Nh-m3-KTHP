// src/services/notifications.ts
// Gọi real API:
//   GET  /api/admin/notifications/logs
//   POST /api/admin/notifications/retry-email/{id}
import adminRequest from './adminRequest';

// ─── Types ────────────────────────────────────────────────────────────────
export type NotificationStatus = 'SUCCESS' | 'FAILED' | 'PENDING';

export interface NotificationItem {
  id: string;
  recipientEmail: string;
  recipientName: string;  // BE chỉ trả email, recipientName = email
  type: string;           // BE log không có type → ''
  subject: string;
  sentAt: string;         // ← createdAt
  status: NotificationStatus;
  retryCount: number;
  errorMessage?: string;
  activity: any[];        // BE không có timeline → []
}

// ─── Internal BE response types ───────────────────────────────────────────
interface NotificationLogResponse {
  id: number;
  recipient: string;   // mapped từ toEmail
  subject: string;
  status: 'SUCCESS' | 'FAILED' | 'RETRYING';
  retryCount: number;
  errorMessage?: string;
  createdAt: string;
}

interface PageResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}

// ─── Mapping BE → FE ──────────────────────────────────────────────────────
function mapStatus(s: 'SUCCESS' | 'FAILED' | 'RETRYING'): NotificationStatus {
  // Backend RETRYING → Frontend PENDING
  return s === 'RETRYING' ? 'PENDING' : s;
}

function mapNotif(b: NotificationLogResponse): NotificationItem {
  return {
    id: String(b.id),
    recipientEmail: b.recipient,
    recipientName: b.recipient,  // chỉ có email
    type: '',                    // log endpoint không trả type
    subject: b.subject,
    sentAt: b.createdAt,
    status: mapStatus(b.status),
    retryCount: b.retryCount,
    errorMessage: b.errorMessage,
    activity: [],               // không có trên BE
  };
}

// ─── Service ──────────────────────────────────────────────────────────────
export async function listNotifications(filters?: {
  type?: string;    // BE log không có type → bỏ qua
  status?: string;  // SUCCESS | FAILED | PENDING (map PENDING → RETRYING)
  q?: string;       // client-side email search
}): Promise<NotificationItem[]> {
  const params: Record<string, any> = { page: 1, pageSize: 100 };

  if (filters?.status) {
    const statusMap: Record<string, string> = {
      SUCCESS: 'SUCCESS',
      FAILED:  'FAILED',
      PENDING: 'RETRYING',
    };
    params.status = statusMap[filters.status] ?? filters.status;
  }

  const res = await adminRequest.get<{ data: PageResponse<NotificationLogResponse> }>(
    '/admin/notifications/logs',
    { params },
  );

  let items = (res.data.data?.items ?? []).map(mapNotif);

  // Client-side search theo email (BE log không có full-text search)
  if (filters?.q?.trim()) {
    const lc = filters.q.toLowerCase();
    items = items.filter((i) =>
      i.recipientEmail.toLowerCase().includes(lc),
    );
  }

  return items;
}

export async function resendNotification(id: string, _by?: string): Promise<void> {
  await adminRequest.post(`/admin/notifications/retry-email/${id}`);
}

export async function getNotificationById(_id: string): Promise<NotificationItem | null> {
  // BE không có endpoint lấy single log → return null
  return null;
}

export default { listNotifications, resendNotification, getNotificationById };