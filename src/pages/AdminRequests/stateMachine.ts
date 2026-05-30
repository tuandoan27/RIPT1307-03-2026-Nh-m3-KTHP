import type { RequestStatus } from './mockData';

// Định nghĩa các transition hợp lệ cho request
export const getAvailableTransitions = (status: RequestStatus): RequestStatus[] => {
  switch (status) {
    case 'PENDING':
      return ['APPROVED', 'REJECTED'];
    case 'APPROVED':
      return ['RETURNED', 'OVERDUE'];
    case 'OVERDUE':
      return ['RETURNED'];
    default:
      return [];
  }
};

export const isValidTransition = (from: RequestStatus, to: RequestStatus): boolean =>
  getAvailableTransitions(from).includes(to);

export default { getAvailableTransitions, isValidTransition };
