// ============================================================
// pages/admin/dashboard/mockData.ts
// Mock data cho Dashboard — toàn bộ dùng mock theo yêu cầu
// ============================================================

import { RequestStatus } from '@/constants/requestStatus';

// ─── Kiểu dữ liệu ────────────────────────────────────────────
export interface StatCard {
  title: string;
  value: number;
  suffix?: string;
  color: string;
}

export interface MonthlyBorrowData {
  month: string;
  count: number;
}

export interface StatusPieData {
  status: string;
  label: string;
  count: number;
  color: string;
}

export interface TopDevice {
  key: string;
  rank: number;
  name: string;
  borrowCount: number;
  available: number;
}

export interface PendingRequest {
  key: string;
  id: string;
  studentName: string;
  deviceName: string;
  borrowDate: string;
  returnDate: string;
  submittedAt: string;
}

// ─── Stat Cards ───────────────────────────────────────────────
export const MOCK_STAT_CARDS: StatCard[] = [
  { title: 'Tổng lượt mượn', value: 324, color: '#1890ff' },
  { title: 'Đang mượn', value: 47, color: '#52c41a' },
  { title: 'Quá hạn', value: 8, color: '#ff4d4f' },
  { title: 'Tổng thiết bị', value: 63, color: '#722ed1' },
];

// ─── Biểu đồ cột: lượt mượn theo tháng ──────────────────────
export const MOCK_MONTHLY_DATA: MonthlyBorrowData[] = [
  { month: 'T1', count: 18 },
  { month: 'T2', count: 25 },
  { month: 'T3', count: 32 },
  { month: 'T4', count: 28 },
  { month: 'T5', count: 41 },
  { month: 'T6', count: 35 },
  { month: 'T7', count: 22 },
  { month: 'T8', count: 19 },
  { month: 'T9', count: 47 },
  { month: 'T10', count: 38 },
  { month: 'T11', count: 29 },
  { month: 'T12', count: 16 },
];

// ─── Biểu đồ tròn: tỉ lệ trạng thái ─────────────────────────
export const MOCK_STATUS_PIE: StatusPieData[] = [
  { status: RequestStatus.PENDING,  label: 'Chờ duyệt', count: 12, color: '#d9d9d9' },
  { status: RequestStatus.APPROVED, label: 'Đã duyệt',  count: 47, color: '#1890ff' },
  { status: RequestStatus.RETURNED, label: 'Đã trả',    count: 198, color: '#52c41a' },
  { status: RequestStatus.REJECTED, label: 'Từ chối',   count: 59, color: '#ff4d4f' },
  { status: RequestStatus.OVERDUE,  label: 'Quá hạn',   count: 8,  color: '#fa8c16' },
];

// ─── Top 5 thiết bị mượn nhiều nhất trong tháng ──────────────
export const MOCK_TOP_DEVICES: TopDevice[] = [
  { key: '1', rank: 1, name: 'Máy chiếu Epson EB-S41',    borrowCount: 18, available: 2 },
  { key: '2', rank: 2, name: 'Micro không dây Shure PG58', borrowCount: 14, available: 1 },
  { key: '3', rank: 3, name: 'Loa Bluetooth JBL Xtreme 3', borrowCount: 11, available: 3 },
  { key: '4', rank: 4, name: 'Camera Sony ZV-E10',          borrowCount: 9,  available: 0 },
  { key: '5', rank: 5, name: 'Tripod chụp ảnh Manfrotto',   borrowCount: 7,  available: 2 },
];

// ─── 5 yêu cầu PENDING mới nhất chưa xử lý ──────────────────
export const MOCK_PENDING_REQUESTS: PendingRequest[] = [
  {
    key: '1', id: 'REQ-0091',
    studentName: 'Nguyễn Văn An',
    deviceName: 'Máy chiếu Epson EB-S41',
    borrowDate: '2025-01-15T08:30:00Z', returnDate: '2025-01-18T17:00:00Z',
    submittedAt: '2025-01-13T09:21:00Z',
  },
  {
    key: '2', id: 'REQ-0090',
    studentName: 'Trần Thị Bình',
    deviceName: 'Micro không dây Shure PG58',
    borrowDate: '2025-01-14T09:00:00Z', returnDate: '2025-01-16T16:00:00Z',
    submittedAt: '2025-01-13T08:55:00Z',
  },
  {
    key: '3', id: 'REQ-0089',
    studentName: 'Lê Minh Cường',
    deviceName: 'Loa Bluetooth JBL Xtreme 3',
    borrowDate: '2025-01-16T10:00:00Z', returnDate: '2025-01-17T16:00:00Z',
    submittedAt: '2025-01-12T22:10:00Z',
  },
  {
    key: '4', id: 'REQ-0088',
    studentName: 'Phạm Thị Dung',
    deviceName: 'Camera Sony ZV-E10',
    borrowDate: '2025-01-15T08:00:00Z', returnDate: '2025-01-20T18:00:00Z',
    submittedAt: '2025-01-12T18:45:00Z',
  },
  {
    key: '5', id: 'REQ-0087',
    studentName: 'Hoàng Văn Em',
    deviceName: 'Tripod chụp ảnh Manfrotto',
    borrowDate: '2025-01-13T09:30:00Z', returnDate: '2025-01-14T12:00:00Z',
    submittedAt: '2025-01-12T14:30:00Z',
  },
];