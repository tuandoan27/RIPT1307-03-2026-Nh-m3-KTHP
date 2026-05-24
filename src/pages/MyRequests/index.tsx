import React, { useState, useEffect } from 'react';
import { Table, Card, Select, Button, Modal, Descriptions, Timeline, Empty, Space, Spin, Tag } from 'antd';
import { EyeOutlined, DownloadOutlined } from '@ant-design/icons';
import { history } from 'umi';
import dayjs from 'dayjs';
import axios from '@/utils/axios';
import StatusBadge from '@/components/StatusBadge';
import styles from './index.less';

interface BorrowRequest {
	id: string;
	equipmentName: string;
	borrowDate: string;
	returnDate: string;
	createdDate: string;
	status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'RETURNED' | 'OVERDUE';
	rejectionReason?: string;
	history: HistoryEntry[];
}

interface HistoryEntry {
	status: string;
	date: string;
	user: string;
	note?: string;
}

const MyRequests: React.FC = () => {
	const [requests, setRequests] = useState<BorrowRequest[]>([]);
	const [loading, setLoading] = useState(true);
	const [statusFilter, setStatusFilter] = useState<'all' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'RETURNED' | 'OVERDUE'>('all');
	const [selectedRequest, setSelectedRequest] = useState<BorrowRequest | null>(null);
	const [modalVisible, setModalVisible] = useState(false);

	// Fetch requests
	useEffect(() => {
		const fetchRequests = async () => {
			setLoading(true);
			try {
				const response = await axios.get('/my-requests');
				const sortedRequests = (response.data?.data || []).sort((a: BorrowRequest, b: BorrowRequest) =>
					dayjs(b.createdDate).diff(dayjs(a.createdDate))
				);
				setRequests(sortedRequests);
			} catch (error) {
				console.error('Failed to fetch requests:', error);
				// Mock data
				const mockRequests: BorrowRequest[] = [
					{
						id: '1',
						equipmentName: 'Laptop Dell XPS 15',
						borrowDate: dayjs().add(2, 'days').format('YYYY-MM-DD'),
						returnDate: dayjs().add(5, 'days').format('YYYY-MM-DD'),
						createdDate: dayjs().subtract(3, 'days').format('YYYY-MM-DD HH:mm'),
						status: 'APPROVED',
						history: [
							{
								status: 'PENDING',
								date: dayjs().subtract(3, 'days').format('YYYY-MM-DD HH:mm'),
								user: 'System',
								note: 'Yêu cầu được tạo',
							},
							{
								status: 'APPROVED',
								date: dayjs().subtract(1, 'days').format('YYYY-MM-DD HH:mm'),
								user: 'Trần Quốc Anh',
								note: 'Phê duyệt yêu cầu',
							},
						],
					},
					{
						id: '2',
						equipmentName: 'Máy Chiếu Epson',
						borrowDate: dayjs().subtract(10, 'days').format('YYYY-MM-DD'),
						returnDate: dayjs().subtract(5, 'days').format('YYYY-MM-DD'),
						createdDate: dayjs().subtract(15, 'days').format('YYYY-MM-DD HH:mm'),
						status: 'RETURNED',
						history: [
							{
								status: 'PENDING',
								date: dayjs().subtract(15, 'days').format('YYYY-MM-DD HH:mm'),
								user: 'System',
								note: 'Yêu cầu được tạo',
							},
							{
								status: 'APPROVED',
								date: dayjs().subtract(14, 'days').format('YYYY-MM-DD HH:mm'),
								user: 'Nguyễn Văn B',
								note: 'Phê duyệt yêu cầu',
							},
							{
								status: 'RETURNED',
								date: dayjs().subtract(5, 'days').format('YYYY-MM-DD HH:mm'),
								user: 'Nguyễn Văn B',
								note: 'Thiết bị đã được trả',
							},
						],
					},
					{
						id: '3',
						equipmentName: 'iPad Pro 12.9',
						borrowDate: dayjs().subtract(20, 'days').format('YYYY-MM-DD'),
						returnDate: dayjs().subtract(15, 'days').format('YYYY-MM-DD'),
						createdDate: dayjs().subtract(25, 'days').format('YYYY-MM-DD HH:mm'),
						status: 'OVERDUE',
						history: [
							{
								status: 'PENDING',
								date: dayjs().subtract(25, 'days').format('YYYY-MM-DD HH:mm'),
								user: 'System',
								note: 'Yêu cầu được tạo',
							},
							{
								status: 'APPROVED',
								date: dayjs().subtract(24, 'days').format('YYYY-MM-DD HH:mm'),
								user: 'Lê Thị C',
								note: 'Phê duyệt yêu cầu',
							},
						],
					},
					{
						id: '4',
						equipmentName: 'Bộ Vi Xử Lý Raspberry Pi',
						borrowDate: dayjs().add(7, 'days').format('YYYY-MM-DD'),
						returnDate: dayjs().add(14, 'days').format('YYYY-MM-DD'),
						createdDate: dayjs().format('YYYY-MM-DD HH:mm'),
						status: 'REJECTED',
						rejectionReason: 'Thiết bị không có sẵn trong khoảng thời gian yêu cầu',
						history: [
							{
								status: 'PENDING',
								date: dayjs().format('YYYY-MM-DD HH:mm'),
								user: 'System',
								note: 'Yêu cầu được tạo',
							},
							{
								status: 'REJECTED',
								date: dayjs().add(1, 'hours').format('YYYY-MM-DD HH:mm'),
								user: 'Phạm Văn D',
								note: 'Từ chối: Thiết bị không có sẵn trong khoảng thời gian yêu cầu',
							},
						],
					},
				];
				setRequests(mockRequests);
			} finally {
				setLoading(false);
			}
		};
		fetchRequests();
	}, []);

	// Filter requests
	const filteredRequests = requests.filter((req) =>
		statusFilter === 'all' ? true : req.status === statusFilter
	);

	const columns = [
		{
			title: 'Thiết Bị',
			dataIndex: 'equipmentName',
			key: 'equipmentName',
			width: '20%',
		},
		{
			title: 'Ngày Mượn',
			dataIndex: 'borrowDate',
			key: 'borrowDate',
			width: '15%',
			render: (date: string) => dayjs(date).format('DD/MM/YYYY'),
		},
		{
			title: 'Ngày Trả',
			dataIndex: 'returnDate',
			key: 'returnDate',
			width: '15%',
			render: (date: string) => dayjs(date).format('DD/MM/YYYY'),
		},
		{
			title: 'Ngày Gửi',
			dataIndex: 'createdDate',
			key: 'createdDate',
			width: '15%',
			render: (date: string) => dayjs(date).format('DD/MM/YYYY HH:mm'),
		},
		{
			title: 'Trạng Thái',
			dataIndex: 'status',
			key: 'status',
			width: '15%',
			render: (status: string) => <StatusBadge status={status as any} />,
		},
		{
			title: 'Hành Động',
			key: 'action',
			width: '20%',
			render: (_, record: BorrowRequest) => (
				<Button
					type="link"
					icon={<EyeOutlined />}
					onClick={() => {
						setSelectedRequest(record);
						setModalVisible(true);
					}}
				>
					Xem Chi Tiết
				</Button>
			),
		},
	];

	const handleExportPDF = () => {
		if (selectedRequest) {
			// Mock PDF export
			const element = document.getElementById('detail-content');
			if (element) {
				const printWindow = window.open('', '', 'height=400,width=800');
				if (printWindow) {
					printWindow.document.write('<pre>' + element.innerText + '</pre>');
					printWindow.document.close();
					printWindow.print();
				}
			}
		}
	};

	return (
		<div className={styles.container}>
			<Card title="Yêu Cầu Mượn Của Tôi">
				<Space style={{ marginBottom: 16 }}>
					<span>Lọc theo trạng thái:</span>
					<Select
						value={statusFilter}
						onChange={setStatusFilter}
						style={{ width: 200 }}
					>
						<Select.Option value="all">Tất cả</Select.Option>
						<Select.Option value="PENDING">Chờ Duyệt</Select.Option>
						<Select.Option value="APPROVED">Đã Duyệt</Select.Option>
						<Select.Option value="REJECTED">Từ Chối</Select.Option>
						<Select.Option value="RETURNED">Đã Trả</Select.Option>
						<Select.Option value="OVERDUE">Quá Hạn</Select.Option>
					</Select>
				</Space>

				{loading ? (
					<Spin spinning={true} />
				) : filteredRequests.length === 0 ? (
					<Empty description="Chưa có yêu cầu nào" />
				) : (
					<Table
						columns={columns}
						dataSource={filteredRequests}
						rowKey="id"
						pagination={{ pageSize: 10 }}
						scroll={{ x: 800 }}
					/>
				)}
			</Card>

			{/* Detail Modal */}
			<Modal
				title="Chi Tiết Yêu Cầu"
				visible={modalVisible}
				onCancel={() => setModalVisible(false)}
				width={700}
				footer={[
					<Button key="export" icon={<DownloadOutlined />} onClick={handleExportPDF}>
						Xuất PDF
					</Button>,
					<Button key="close" onClick={() => setModalVisible(false)}>
						Đóng
					</Button>,
				]}
			>
				{selectedRequest && (
					<div id="detail-content">
						<Descriptions bordered column={1} size="small" style={{ marginBottom: 24 }}>
							<Descriptions.Item label="Thiết Bị">
								{selectedRequest.equipmentName}
							</Descriptions.Item>
							<Descriptions.Item label="Ngày Mượn">
								{dayjs(selectedRequest.borrowDate).format('DD/MM/YYYY')}
							</Descriptions.Item>
							<Descriptions.Item label="Ngày Trả">
								{dayjs(selectedRequest.returnDate).format('DD/MM/YYYY')}
							</Descriptions.Item>
							<Descriptions.Item label="Trạng Thái">
								<StatusBadge status={selectedRequest.status as any} />
							</Descriptions.Item>
						</Descriptions>

						{selectedRequest.rejectionReason && (
							<Card
								type="inner"
								title="Lý Do Từ Chối"
								style={{ marginBottom: 24, borderColor: '#ff4d4f' }}
							>
								<p style={{ color: '#cf1322', margin: 0 }}>
									{selectedRequest.rejectionReason}
								</p>
							</Card>
						)}

						<h4>Lịch Sử Thay Đổi Trạng Thái</h4>
						<Timeline>
							{selectedRequest.history.map((entry, index) => (
								<Timeline.Item
									key={index}
									dot={
										<Tag color={
											entry.status === 'PENDING' ? 'orange' :
											entry.status === 'APPROVED' ? 'green' :
											entry.status === 'REJECTED' ? 'red' :
											entry.status === 'RETURNED' ? 'cyan' :
											'purple'
										}>
											{entry.status}
										</Tag>
									}
								>
									<p>
										<strong>{entry.user}</strong> - {dayjs(entry.date).format('DD/MM/YYYY HH:mm')}
									</p>
									{entry.note && <p style={{ margin: '4px 0 0 0', color: '#666' }}>{entry.note}</p>}
								</Timeline.Item>
							))}
						</Timeline>
					</div>
				)}
			</Modal>
		</div>
	);
};

export default MyRequests;
