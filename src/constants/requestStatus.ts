// ============================================================
// constants/requestStatus.ts
// Định nghĩa tập trung tất cả enum trạng thái yêu cầu
// Import file này ở mọi nơi cần dùng, KHÔNG hardcode string
// ============================================================

export enum RequestStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  RETURNED = 'RETURNED',
  OVERDUE = 'OVERDUE',
}

export const REQUEST_STATUS_LABEL: Record<RequestStatus, string> = {
  [RequestStatus.PENDING]: 'Chờ duyệt',
  [RequestStatus.APPROVED]: 'Đã duyệt',
  [RequestStatus.REJECTED]: 'Từ chối',
  [RequestStatus.RETURNED]: 'Đã trả',
  [RequestStatus.OVERDUE]: 'Quá hạn',
};

export const REQUEST_STATUS_COLOR: Record<RequestStatus, string> = {
  [RequestStatus.PENDING]: 'default',
  [RequestStatus.APPROVED]: 'blue',
  [RequestStatus.REJECTED]: 'red',
  [RequestStatus.RETURNED]: 'green',
  [RequestStatus.OVERDUE]: 'orange',
};

export enum ActivityAction {
  CREATE_DEVICE = 'CREATE_DEVICE',
  UPDATE_DEVICE = 'UPDATE_DEVICE',
  DELETE_DEVICE = 'DELETE_DEVICE',
  APPROVE_REQUEST = 'APPROVE_REQUEST',
  REJECT_REQUEST = 'REJECT_REQUEST',
  RETURN_REQUEST = 'RETURN_REQUEST',
  LOCK_USER = 'LOCK_USER',
  UNLOCK_USER = 'UNLOCK_USER',
  ADJUST_PENALTY = 'ADJUST_PENALTY',
  MANUAL_SEND_EMAIL = 'MANUAL_SEND_EMAIL',
  UPDATE_STOCK = 'UPDATE_STOCK',
}