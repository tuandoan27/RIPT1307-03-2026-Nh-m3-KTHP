// ============================================================
// pages/admin/requests/mockData.ts
// Mock data và types cho trang Quản lý Yêu cầu
// ============================================================

import { MOCK_EQUIPMENT } from '../AdminDevice/mockData';

export type RequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'RETURNED' | 'OVERDUE';

export interface RequestHistoryEntry {
  status: RequestStatus;
  by: string;
  timestamp: string;
  note?: string;
}

export interface RequestItem {
  id: string;
  studentName: string;
  equipmentId: string;
  equipmentName: string;
  borrowDate: string; // ISO date
  returnDate: string; // ISO date
  submittedAt: string; // ISO date
  status: RequestStatus;
  note?: string;
  reason?: string; // when rejected
  history: RequestHistoryEntry[];
}

// Một vài request mẫu
export const MOCK_REQUESTS: RequestItem[] = [
  {
    id: 'RQ001',
    studentName: 'Nguyễn Văn A',
    equipmentId: 'EQ002',
    equipmentName: MOCK_EQUIPMENT.find((e) => e.id === 'EQ002')?.name || 'Unknown',
    borrowDate: '2025-05-01T08:30:00Z',
    returnDate: '2025-05-03T17:00:00Z',
    submittedAt: '2025-04-25T09:00:00Z',
    status: 'PENDING',
    note: 'Dùng cho buổi thuyết trình môn học',
    history: [
      { status: 'PENDING', by: 'Student', timestamp: '2025-04-25T09:00:00Z' },
    ],
  },
  {
    id: 'RQ002',
    studentName: 'Trần Thị B',
    equipmentId: 'EQ003',
    equipmentName: MOCK_EQUIPMENT.find((e) => e.id === 'EQ003')?.name || 'Unknown',
    borrowDate: '2025-04-28T09:00:00Z',
    returnDate: '2025-05-02T16:00:00Z',
    submittedAt: '2025-04-20T10:30:00Z',
    status: 'APPROVED',
    note: 'Sự kiện câu lạc bộ',
    history: [
      { status: 'PENDING', by: 'Student', timestamp: '2025-04-20T10:30:00Z' },
      { status: 'APPROVED', by: 'Admin', timestamp: '2025-04-21T08:00:00Z', note: 'Đã phê duyệt' },
    ],
  },
  {
    id: 'RQ003',
    studentName: 'Lê Văn C',
    equipmentId: 'EQ004',
    equipmentName: MOCK_EQUIPMENT.find((e) => e.id === 'EQ004')?.name || 'Unknown',
    borrowDate: '2025-04-10T08:00:00Z',
    returnDate: '2025-04-12T18:00:00Z',
    submittedAt: '2025-04-05T11:00:00Z',
    status: 'OVERDUE',
    note: 'Mượn dài hạn',
    history: [
      { status: 'PENDING', by: 'Student', timestamp: '2025-04-05T11:00:00Z' },
      { status: 'APPROVED', by: 'Admin', timestamp: '2025-04-06T09:00:00Z' },
      { status: 'OVERDUE', by: 'System', timestamp: '2025-04-13T00:00:00Z', note: 'Quá hạn trả' },
    ],
  },
];

export const REQUEST_STATUS_LABEL: Record<RequestStatus, string> = {
  PENDING: 'Đang chờ',
  APPROVED: 'Đã duyệt',
  REJECTED: 'Từ chối',
  RETURNED: 'Đã trả',
  OVERDUE: 'Quá hạn',
};

export const REQUEST_STATUS_COLOR: Record<RequestStatus, string> = {
  PENDING: 'default',
  APPROVED: 'success',
  REJECTED: 'error',
  RETURNED: 'processing',
  OVERDUE: 'warning',
};

export default MOCK_REQUESTS;
