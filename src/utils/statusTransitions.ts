// ============================================================
// utils/statusTransitions.ts
// Helper getValidTransitions(currentStatus) — logic thuần FE
// Trả về danh sách trạng thái hợp lệ có thể chuyển đến
// Dựa theo State Transition Rule trong tài liệu nghiệp vụ:
//   PENDING  → APPROVED | REJECTED
//   APPROVED → RETURNED | OVERDUE
//   OVERDUE  → RETURNED
//   REJECTED, RETURNED → không có transition tiếp theo
// ============================================================

import { RequestStatus, REQUEST_STATUS_LABEL } from 'src/constants/requestStatus';

/** Map định nghĩa tất cả transition hợp lệ */
const VALID_TRANSITIONS: Record<RequestStatus, RequestStatus[]> = {
  [RequestStatus.PENDING]: [RequestStatus.APPROVED, RequestStatus.REJECTED],
  [RequestStatus.APPROVED]: [RequestStatus.RETURNED, RequestStatus.OVERDUE],
  [RequestStatus.OVERDUE]: [RequestStatus.RETURNED],
  [RequestStatus.REJECTED]: [],
  [RequestStatus.RETURNED]: [],
};

export interface TransitionOption {
  status: RequestStatus;
  label: string;
}

/**
 * Trả về danh sách các trạng thái hợp lệ có thể chuyển đến
 * từ trạng thái hiện tại.
 *
 * @param currentStatus - Trạng thái hiện tại của yêu cầu
 * @returns Mảng TransitionOption gồm { status, label }
 *
 * @example
 * getValidTransitions(RequestStatus.PENDING)
 * // → [{ status: 'APPROVED', label: 'Đã duyệt' }, { status: 'REJECTED', label: 'Từ chối' }]
 */
export function getValidTransitions(currentStatus: RequestStatus): TransitionOption[] {
  const nextStatuses = VALID_TRANSITIONS[currentStatus] ?? [];
  return nextStatuses.map((status) => ({
    status,
    label: REQUEST_STATUS_LABEL[status],
  }));
}

/**
 * Kiểm tra một transition cụ thể có hợp lệ không.
 * Dùng để validate phía FE trước khi gọi API.
 *
 * @param from - Trạng thái nguồn
 * @param to   - Trạng thái đích
 * @returns true nếu transition hợp lệ
 */
export function isValidTransition(from: RequestStatus, to: RequestStatus): boolean {
  return VALID_TRANSITIONS[from]?.includes(to) ?? false;
}

/**
 * Trả về label mô tả transition để hiển thị trên UI.
 *
 * @example
 * getTransitionLabel(RequestStatus.PENDING, RequestStatus.APPROVED)
 * // → 'Chờ duyệt → Đã duyệt'
 */
export function getTransitionLabel(from: RequestStatus, to: RequestStatus): string {
  return `${REQUEST_STATUS_LABEL[from]} → ${REQUEST_STATUS_LABEL[to]}`;
}