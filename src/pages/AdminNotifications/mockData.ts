// Mock data and types for Admin Notifications page
export type NotificationStatus = 'SUCCESS' | 'FAILED' | 'PENDING';

export interface NotificationActivity {
  type: string;
  by: string;
  timestamp: string;
  note?: string;
}

export interface NotificationItem {
  id: string;
  recipientEmail: string;
  recipientName?: string;
  type: string;
  subject?: string;
  sentAt: string; // ISO
  status: NotificationStatus;
  activity: NotificationActivity[];
}

export const MOCK_NOTIFICATIONS: NotificationItem[] = [
  {
    id: 'N001',
    recipientEmail: 'a@student.univ.edu',
    recipientName: 'Nguyễn Văn A',
    type: 'WELCOME',
    subject: 'Chào mừng bạn đến với hệ thống',
    sentAt: '2025-05-01T08:30:00Z',
    status: 'SUCCESS',
    activity: [
      { type: 'AUTO_SEND', by: 'System', timestamp: '2025-05-01T08:30:00Z' },
    ],
  },
  {
    id: 'N002',
    recipientEmail: 'b@student.univ.edu',
    recipientName: 'Trần Thị B',
    type: 'REMINDER',
    subject: 'Nhắc trả thiết bị',
    sentAt: '2025-04-28T09:00:00Z',
    status: 'FAILED',
    activity: [
      { type: 'AUTO_SEND', by: 'System', timestamp: '2025-04-28T09:00:00Z', note: 'SMTP error' },
    ],
  },
  {
    id: 'N003',
    recipientEmail: 'c@student.univ.edu',
    recipientName: 'Lê Văn C',
    type: 'PENALTY',
    subject: 'Thông báo điểm phạt',
    sentAt: '2025-04-30T11:00:00Z',
    status: 'SUCCESS',
    activity: [
      { type: 'AUTO_SEND', by: 'System', timestamp: '2025-04-30T11:00:00Z' },
    ],
  },
  {
    id: 'N004',
    recipientEmail: 'd@student.univ.edu',
    recipientName: 'Phạm Thị D',
    type: 'REMINDER',
    subject: 'Nhắc mượn thiết bị',
    sentAt: '2025-04-25T10:30:00Z',
    status: 'FAILED',
    activity: [
      { type: 'AUTO_SEND', by: 'System', timestamp: '2025-04-25T10:30:00Z', note: 'Recipient mailbox full' },
    ],
  },
];

export default MOCK_NOTIFICATIONS;
