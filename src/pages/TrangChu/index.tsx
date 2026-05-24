import { Card, Space } from 'antd';
import './components/style.less';
import { unitName } from '@/services/base/constant';
import { useModel } from 'umi';
import StatusBadge from '@/components/StatusBadge';

const TrangChu = () => {
	const { data } = useModel('randomuser');

	return (
		<Card bodyStyle={{ height: '100%' }}>
			<div className='home-welcome' style={{ textAlign: 'center', padding: '24px 0' }}>
				<div>
					<b>{data.length} users</b>
				</div>
				<h1 className='title'>THỰC HÀNH LẬP TRÌNH WEB</h1>
				<h2 className='sub-title'>{unitName.toUpperCase()}</h2>

				<div style={{ marginTop: '48px' }}>
					<h3 style={{ marginBottom: '24px', fontWeight: 600, color: '#333' }}>
						Demo Component StatusBadge (FE Sinh viên)
					</h3>
					<Space size='middle' wrap style={{ justifyContent: 'center' }}>
						<StatusBadge status='PENDING' />
						<StatusBadge status='APPROVED' />
						<StatusBadge status='REJECTED' />
						<StatusBadge status='RETURNED' />
						<StatusBadge status='OVERDUE' />
					</Space>
				</div>
			</div>
		</Card>
	);
};

export default TrangChu;
