// Centralized mock accessors and dashboard derivations
import MOCK_USERS, { type UserItem, type BorrowEntry, type PenaltyEntry } from '@/pages/AdminUsers/mockData';
import { MOCK_EQUIPMENT, type Equipment } from '@/pages/AdminDevice/mockData';
import MOCK_REQUESTS, { type RequestItem, REQUEST_STATUS_LABEL, REQUEST_STATUS_COLOR } from '@/pages/AdminRequests/mockData';
import MOCK_NOTIFICATIONS, { type NotificationItem } from '@/pages/AdminNotifications/mockData';
import MOCK_ACTIVITY_LOGS, { type ActivityLogItem } from '@/pages/AdminActivityLogs/mockData';

// Re-export key types so other modules can import types from '@/mocks'
export type { UserItem, BorrowEntry, PenaltyEntry } from '@/pages/AdminUsers/mockData';
export type { Equipment } from '@/pages/AdminDevice/mockData';
export type { RequestItem } from '@/pages/AdminRequests/mockData';
export type { NotificationItem } from '@/pages/AdminNotifications/mockData';
export type { ActivityLogItem } from '@/pages/AdminActivityLogs/mockData';

export type StatCard = { title: string; value: number; suffix?: string; color: string };
export type MonthlyBorrowData = { month: string; count: number };
export type StatusPieData = { status: string; label: string; count: number; color: string };
export type TopDevice = { key: string; rank: number; name: string; borrowCount: number; available: number };
export type PendingRequest = { key: string; id: string; studentName: string; deviceName: string; borrowDate: string; returnDate: string; submittedAt: string };

const clone = <T,>(v: T): T => JSON.parse(JSON.stringify(v));

export const getMockUsers = (): UserItem[] => clone(MOCK_USERS);
export const getMockEquipment = (): Equipment[] => clone(MOCK_EQUIPMENT);
export const getMockRequests = (): RequestItem[] => clone(MOCK_REQUESTS);
export const getMockNotifications = (): NotificationItem[] => clone(MOCK_NOTIFICATIONS);
export const getMockActivityLogs = (): ActivityLogItem[] => clone(MOCK_ACTIVITY_LOGS);

// Derive dashboard data from base mocks so dashboard always reflects canonical data
export function getDashboardData() {
  const requests = getMockRequests();
  const equipments = getMockEquipment();

  // Stat cards
  const completedStatuses = ['APPROVED', 'RETURNED', 'OVERDUE'];
  const totalBorrows = requests.filter((r) => completedStatuses.includes(r.status)).length;
  const currentlyBorrowing = requests.filter((r) => r.status === 'APPROVED').length;
  const overdue = requests.filter((r) => r.status === 'OVERDUE').length;
  const totalDevices = equipments.filter((e) => !e.is_deleted).length;

  const statCards: StatCard[] = [
    { title: 'Tổng lượt mượn', value: totalBorrows, color: '#1890ff' },
    { title: 'Đang mượn', value: currentlyBorrowing, color: '#52c41a' },
    { title: 'Quá hạn', value: overdue, color: '#ff4d4f' },
    { title: 'Tổng thiết bị', value: totalDevices, color: '#722ed1' },
  ];

  // Monthly borrow counts (T1..T12)
  const months = Array.from({ length: 12 }, (_, i) => `T${i + 1}`);
  const monthlyMap: Record<string, number> = {};
  requests.forEach((r) => {
    try {
      const d = new Date(r.borrowDate);
      if (!isNaN(d.getTime())) {
        const key = `T${d.getMonth() + 1}`;
        monthlyMap[key] = (monthlyMap[key] || 0) + 1;
      }
    } catch (e) {
      // ignore parse errors
    }
  });
  const monthlyData: MonthlyBorrowData[] = months.map((m) => ({ month: m, count: monthlyMap[m] || 0 }));

  // Status pie
  const allStatuses = Object.keys(REQUEST_STATUS_LABEL) as string[];
  const statusCounts: Record<string, number> = {};
  allStatuses.forEach((s) => (statusCounts[s] = 0));
  requests.forEach((r) => { statusCounts[r.status] = (statusCounts[r.status] || 0) + 1; });

  // Map AntD status color tokens (e.g. 'success', 'warning') to hex values for charts
  const STATUS_COLOR_MAP: Record<string, string> = {
    default: '#d9d9d9',
    success: '#52c41a',
    error: '#ff4d4f',
    processing: '#1890ff',
    warning: '#fa8c16',
  };

  const statusPie: StatusPieData[] = allStatuses.map((s) => {
    const token = REQUEST_STATUS_COLOR[s as keyof typeof REQUEST_STATUS_COLOR] as unknown as string;
    const color = (token && STATUS_COLOR_MAP[token]) || (typeof token === 'string' && token.startsWith('#') ? token : '#d9d9d9');
    return {
      status: s,
      label: REQUEST_STATUS_LABEL[s as keyof typeof REQUEST_STATUS_LABEL] || s,
      count: statusCounts[s] || 0,
      color,
    };
  });

  // Top devices by request count
  const deviceMap: Record<string, { equip: Equipment; count: number }> = {};
  equipments.forEach((e) => { deviceMap[e.id] = { equip: e, count: 0 }; });
  requests.forEach((r) => {
    if (r.equipmentId && deviceMap[r.equipmentId]) deviceMap[r.equipmentId].count += 1;
  });
  const topDevices: TopDevice[] = Object.values(deviceMap)
    .sort((a, b) => b.count - a.count)
    .slice(0, 5)
    .map((d, idx) => ({ key: d.equip.id, rank: idx + 1, name: d.equip.name, borrowCount: d.count, available: d.equip.availableQuantity }));

  // Pending requests (most recent first)
  const pendingRequests: PendingRequest[] = requests
    .filter((r) => r.status === 'PENDING')
    .sort((a, b) => new Date(b.submittedAt).getTime() - new Date(a.submittedAt).getTime())
    .slice(0, 5)
    .map((r, idx) => ({ key: `${r.id}-${idx}`, id: r.id, studentName: r.studentName, deviceName: r.equipmentName || '', borrowDate: r.borrowDate, returnDate: r.returnDate, submittedAt: r.submittedAt }));

  return { statCards, monthlyData, statusPie, topDevices, pendingRequests };
}

export default {
  getMockUsers,
  getMockEquipment,
  getMockRequests,
  getMockNotifications,
  getMockActivityLogs,
  getDashboardData,
  deepClone: clone,
};
