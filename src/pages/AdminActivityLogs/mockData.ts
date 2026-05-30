import { ActivityAction } from '@/constants/requestStatus';

export interface ActivityLogItem {
  id: string;
  action: ActivityAction;
  actor: string;
  objectType?: string; // e.g. 'Device' | 'User' | 'Request' | 'Notification'
  objectId?: string;
  objectName?: string;
  details?: string;
  timestamp: string; // ISO
}

export const MOCK_ACTIVITY_LOGS: ActivityLogItem[] = [
  {
    id: 'AL001',
    action: ActivityAction.CREATE_DEVICE,
    actor: 'Admin',
    objectType: 'Device',
    objectId: 'EQ001',
    objectName: 'Máy chiếu Epson EB-X41',
    timestamp: '2025-01-10T08:05:00Z',
  },
  {
    id: 'AL002',
    action: ActivityAction.UPDATE_STOCK,
    actor: 'Admin',
    objectType: 'Device',
    objectId: 'EQ003',
    objectName: 'Micro không dây Shure BLX288',
    details: 'Nhập thêm 2 bộ từ kho dự trữ',
    timestamp: '2025-03-20T10:30:00Z',
  },
  {
    id: 'AL003',
    action: ActivityAction.APPROVE_REQUEST,
    actor: 'Manager',
    objectType: 'Request',
    objectId: 'REQ007',
    objectName: 'Mượn máy chiếu',
    details: 'Duyệt cho 3 ngày',
    timestamp: '2025-02-15T09:20:00Z',
  },
  {
    id: 'AL004',
    action: ActivityAction.MANUAL_SEND_EMAIL,
    actor: 'Admin',
    objectType: 'Notification',
    objectId: 'N002',
    objectName: 'REMINDER to b@student.univ.edu',
    details: 'Gửi lại do lỗi SMTP',
    timestamp: '2025-04-28T09:20:00Z',
  },
  {
    id: 'AL005',
    action: ActivityAction.LOCK_USER,
    actor: 'Admin',
    objectType: 'User',
    objectId: 'U123',
    objectName: 'Nguyễn Văn A',
    details: 'Khoá vì vi phạm nội quy',
    timestamp: '2025-04-15T13:00:00Z',
  },
  {
    id: 'AL006',
    action: ActivityAction.ADJUST_PENALTY,
    actor: 'System',
    objectType: 'User',
    objectId: 'U124',
    objectName: 'Trần Thị B',
    details: 'Tăng điểm phạt 10%',
    timestamp: '2025-05-01T08:45:00Z',
  },
  {
    id: 'AL007',
    action: ActivityAction.REJECT_REQUEST,
    actor: 'Admin',
    objectType: 'Request',
    objectId: 'REQ010',
    objectName: 'Mượn micro',
    details: 'Không có thiết bị',
    timestamp: '2025-05-02T09:15:00Z',
  },
  {
    id: 'AL008',
    action: ActivityAction.UNLOCK_USER,
    actor: 'Admin',
    objectType: 'User',
    objectId: 'U123',
    objectName: 'Nguyễn Văn A',
    details: 'Mở khoá sau khi kháng cáo',
    timestamp: '2025-05-03T10:00:00Z',
  },
];

export default MOCK_ACTIVITY_LOGS;
