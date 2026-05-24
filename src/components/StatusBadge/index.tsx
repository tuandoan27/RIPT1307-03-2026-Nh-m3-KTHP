import React from 'react';
import './index.less';

export type StatusType = 'PENDING' | 'APPROVED' | 'REJECTED' | 'RETURNED' | 'OVERDUE';

interface StatusBadgeProps {
	status: StatusType;
	text?: string;
	style?: React.CSSProperties;
}

const statusConfig = {
	PENDING: {
		text: 'Chờ duyệt',
		className: 'status-pending',
	},
	APPROVED: {
		text: 'Đã duyệt',
		className: 'status-approved',
	},
	REJECTED: {
		text: 'Từ chối',
		className: 'status-rejected',
	},
	RETURNED: {
		text: 'Trả lại',
		className: 'status-returned',
	},
	OVERDUE: {
		text: 'Quá hạn',
		className: 'status-overdue',
	},
};

const StatusBadge: React.FC<StatusBadgeProps> = ({ status, text, style }) => {
	const config = statusConfig[status] || statusConfig.PENDING;
	const displayText = text || config.text;

	return (
		<span className={`status-badge ${config.className}`} style={style}>
			<span className='status-dot' />
			<span className='status-text'>{displayText}</span>
		</span>
	);
};

export default StatusBadge;
