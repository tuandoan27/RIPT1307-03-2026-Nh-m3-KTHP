// Mock data and types for Admin Users page
export type AccountStatus = 'ACTIVE' | 'LOCKED';
export type UserActivityType = 'BORROW' | 'RETURN' | 'ADJUST_PENALTY' | 'LOCK_USER' | 'UNLOCK_USER';

export interface BorrowEntry {
  id: string;
  equipmentName: string;
  borrowDate: string; // ISO datetime
  returnDate?: string; // ISO datetime
}

export interface PenaltyEntry {
  id: string;
  type: UserActivityType;
  pointsChange?: number;
  reason: string;
  by: string;
  timestamp: string; // ISO
}

export interface UserItem {
  id: string;
  studentId: string;
  fullName: string;
  email: string;
  avatarUrl?: string;
  penaltyPoints: number;
  status: AccountStatus;
  borrowHistory: BorrowEntry[];
  penaltyHistory: PenaltyEntry[];
}

export const USER_STATUS_LABEL: Record<AccountStatus, string> = {
  ACTIVE: 'Hoạt động',
  LOCKED: 'Bị khóa',
};

export const USER_STATUS_COLOR: Record<AccountStatus, string> = {
  ACTIVE: 'success',
  LOCKED: 'volcano',
};

export const MOCK_USERS: UserItem[] = [
  {
    id: 'u-001',
    studentId: 'S2023001',
    fullName: 'Nguyễn Văn A',
    email: 'a@student.univ.edu',
    avatarUrl: 'https://placehold.co/64x64/1890ff/ffffff?text=NV',
    penaltyPoints: 2,
    status: 'ACTIVE',
    borrowHistory: [
      { id: 'bh-001', equipmentName: 'Raspberry Pi 4', borrowDate: '2025-02-10T09:00:00Z', returnDate: '2025-02-14T16:20:00Z' },
      { id: 'bh-002', equipmentName: 'Arduino Uno', borrowDate: '2025-05-01T08:30:00Z', returnDate: '2025-05-05T11:00:00Z' },
    ],
    penaltyHistory: [
      { id: 'ph-001', type: 'ADJUST_PENALTY', pointsChange: 2, reason: 'Trả muộn 2 ngày', by: 'Admin', timestamp: '2025-02-15T09:10:00Z' },
    ],
  },
  {
    id: 'u-002',
    studentId: 'S2023002',
    fullName: 'Trần Thị B',
    email: 'b@student.univ.edu',
    avatarUrl: 'https://placehold.co/64x64/52c41a/ffffff?text=TB',
    penaltyPoints: 0,
    status: 'ACTIVE',
    borrowHistory: [
      { id: 'bh-003', equipmentName: 'ESP32 DevKit', borrowDate: '2025-03-12T10:00:00Z', returnDate: '2025-03-15T14:30:00Z' },
    ],
    penaltyHistory: [],
  },
  {
    id: 'u-003',
    studentId: 'S2023003',
    fullName: 'Lê Văn C',
    email: 'c@student.univ.edu',
    avatarUrl: 'https://placehold.co/64x64/fa8c16/ffffff?text=LC',
    penaltyPoints: 5,
    status: 'LOCKED',
    borrowHistory: [
      { id: 'bh-004', equipmentName: 'Laptop Dell', borrowDate: '2024-12-20T08:00:00Z', returnDate: '2024-12-30T17:00:00Z' },
    ],
    penaltyHistory: [
      { id: 'ph-002', type: 'ADJUST_PENALTY', pointsChange: 5, reason: 'Thiết bị hư hỏng', by: 'Admin', timestamp: '2025-01-05T14:00:00Z' },
      { id: 'ph-003', type: 'LOCK_USER', reason: 'Nợ phạt lớn', by: 'Admin', timestamp: '2025-01-06T08:30:00Z' },
    ],
  },
  {
    id: 'u-004',
    studentId: 'S2023004',
    fullName: 'Phạm Thị D',
    email: 'd@student.univ.edu',
    avatarUrl: 'https://placehold.co/64x64/722ed1/ffffff?text=PD',
    penaltyPoints: 0,
    status: 'ACTIVE',
    borrowHistory: [],
    penaltyHistory: [],
  },
];

export default MOCK_USERS;
