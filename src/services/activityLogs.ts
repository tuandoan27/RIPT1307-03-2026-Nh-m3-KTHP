import MOCK_ACTIVITY_LOGS, { ActivityLogItem } from '@/pages/AdminActivityLogs/mockData';

const delay = (ms = 200) => new Promise((res) => setTimeout(res, ms));

const store: ActivityLogItem[] = MOCK_ACTIVITY_LOGS.map((m) => ({ ...m }));

export async function listActivityLogs(filters?: {
  action?: string;
  actor?: string;
  objectType?: string;
  dateFrom?: string; // ISO
  dateTo?: string; // ISO
  q?: string;
}) {
  await delay(180);
  let out = store.slice();
  if (!filters) return out.sort((a, b) => +new Date(b.timestamp) - +new Date(a.timestamp));

  if (filters.action) out = out.filter((r) => r.action === filters.action);
  if (filters.actor) out = out.filter((r) => (r.actor || '').toLowerCase().includes(filters.actor!.toLowerCase()));
  if (filters.objectType) out = out.filter((r) => (r.objectType || '').toLowerCase() === filters.objectType!.toLowerCase());
  if (filters.dateFrom) out = out.filter((r) => new Date(r.timestamp) >= new Date(filters.dateFrom!));
  if (filters.dateTo) out = out.filter((r) => new Date(r.timestamp) <= new Date(filters.dateTo!));
  if (filters.q) {
    const q = filters.q.toLowerCase();
    out = out.filter((r) => (
      (r.objectName || '').toLowerCase().includes(q) ||
      (r.details || '').toLowerCase().includes(q) ||
      (r.actor || '').toLowerCase().includes(q)
    ));
  }

  out.sort((a, b) => +new Date(b.timestamp) - +new Date(a.timestamp));
  return out;
}

export default { listActivityLogs };
