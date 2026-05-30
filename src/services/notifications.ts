import { getMockNotifications, type NotificationItem } from '@/mocks';

const delay = (ms = 300) => new Promise((res) => setTimeout(res, ms));

// Clone initial data for in-memory mutation
const store: NotificationItem[] = getMockNotifications().map((n) => ({ ...n, activity: [...n.activity] }));

export async function listNotifications(filters?: { type?: string; status?: string; q?: string; }) {
  await delay(250);
  let out = store.slice();
  if (filters) {
    if (filters.type) out = out.filter((n) => n.type === filters.type);
    if (filters.status) out = out.filter((n) => n.status === filters.status);
    if (filters.q) out = out.filter((n) => (n.recipientEmail || '').toLowerCase().includes(filters.q.toLowerCase()) || (n.recipientName || '').toLowerCase().includes(filters.q.toLowerCase()));
  }
  // most recent first
  out.sort((a, b) => new Date(b.sentAt).getTime() - new Date(a.sentAt).getTime());
  return out;
}

export async function getNotificationById(id: string) {
  await delay(120);
  return store.find((n) => n.id === id) || null;
}

export async function resendNotification(id: string, by = 'Admin') {
  await delay(500);
  const n = store.find((x) => x.id === id);
  if (!n) throw new Error('Notification not found');
  if (n.status !== 'FAILED') throw new Error('Only FAILED notifications can be resent');
  const entry = { type: 'MANUAL_SEND_EMAIL', by, timestamp: new Date().toISOString(), note: 'Resent by admin' };
  n.activity.push(entry);
  // simulate successful resend
  n.status = 'SUCCESS';
  n.sentAt = new Date().toISOString();
  return n;
}

export default { listNotifications, resendNotification, getNotificationById };
