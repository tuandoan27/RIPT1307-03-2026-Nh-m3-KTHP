// src/services/dashboard.ts  (FILE MỚI — thay thế @/mocks cho AdminDashboard)
// Gọi real API: GET /api/admin/dashboard
import adminRequest from './adminRequest';

// ─── Types (export để AdminDashboard page import) ─────────────────────────
export interface StatCard {
  title: string;
  value: number;
  suffix?: string;
  color?: string;
}

export interface MonthlyBorrowData {
  month: string;   // 'T1' … 'T12'
  count: number;
}

export interface StatusPieData {
  status: string;
  label: string;
  count: number;
  color: string;
}

export interface TopDevice {
  rank: number;
  name: string;
  borrowCount: number;
  available: number; // BE dashboard không trả → 0
}

export interface PendingRequest {
  id: string;
  studentName: string;
  deviceName: string;
  borrowDate: string;
  submittedAt: string;
}

export interface DashboardData {
  statCards: StatCard[];
  monthlyData: MonthlyBorrowData[];
  statusPie: StatusPieData[];
  topDevices: TopDevice[];
  pendingRequests: PendingRequest[];
}

// ─── Internal BE response types ───────────────────────────────────────────
interface BackendDashboard {
  totalBorrows: number;
  currentBorrowing: number;
  overdue: number;
  totalEquipment: number;
  borrowsByMonth: Array<{ month: number; count: number }>;
  statusRatio: Record<string, number>;
  top5Equipment: Array<{ equipmentId: number; equipmentName: string; count: number }>;
  latest5Pending: Array<{
    id: number;
    studentName: string;
    equipmentName: string;
    startDate: string;
    createdAt: string;
  }>;
}

// ─── Constants ────────────────────────────────────────────────────────────
const MONTH_NAMES = ['T1','T2','T3','T4','T5','T6','T7','T8','T9','T10','T11','T12'];

const STATUS_COLORS: Record<string, string> = {
  PENDING:  '#faad14',
  APPROVED: '#1890ff',
  RETURNED: '#52c41a',
  REJECTED: '#ff4d4f',
  OVERDUE:  '#fa8c16',
};

const STATUS_LABELS: Record<string, string> = {
  PENDING:  'Chờ duyệt',
  APPROVED: 'Đã duyệt',
  RETURNED: 'Đã trả',
  REJECTED: 'Từ chối',
  OVERDUE:  'Quá hạn',
};

// ─── Service ──────────────────────────────────────────────────────────────
export async function fetchDashboard(): Promise<DashboardData> {
  const res = await adminRequest.get<{ data: BackendDashboard }>('/admin/dashboard');
  const d = res.data.data;

  const statCards: StatCard[] = [
    { title: 'Tổng lượt mượn',  value: d.totalBorrows,      color: '#1890ff' },
    { title: 'Đang mượn',       value: d.currentBorrowing,   color: '#52c41a' },
    { title: 'Quá hạn',         value: d.overdue,            color: '#fa8c16' },
    { title: 'Tổng thiết bị',   value: d.totalEquipment,     color: '#722ed1' },
  ];

  const monthlyData: MonthlyBorrowData[] = (d.borrowsByMonth ?? []).map((m) => ({
    month: MONTH_NAMES[m.month - 1] ?? `T${m.month}`,
    count: m.count,
  }));

  const statusPie: StatusPieData[] = Object.entries(d.statusRatio ?? {}).map(
    ([status, count]) => ({
      status,
      label: STATUS_LABELS[status] ?? status,
      count,
      color: STATUS_COLORS[status] ?? '#8884d8',
    }),
  );

  const topDevices: TopDevice[] = (d.top5Equipment ?? []).map((e, i) => ({
    rank: i + 1,
    name: e.equipmentName,
    borrowCount: e.count,
    available: 0,
  }));

  const pendingRequests: PendingRequest[] = (d.latest5Pending ?? []).map((r) => ({
    id: String(r.id),
    studentName: r.studentName,
    deviceName: r.equipmentName,
    borrowDate: r.startDate,
    submittedAt: r.createdAt,
  }));

  return { statCards, monthlyData, statusPie, topDevices, pendingRequests };
}

export default { fetchDashboard };