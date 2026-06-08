import React, { useState, useEffect } from 'react';
import { Table, Card, Select, Button, Modal, Descriptions, Timeline, Empty, Space, Spin, Tag } from 'antd';
import { EyeOutlined, DownloadOutlined } from '@ant-design/icons';
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
				const response = await axios.get('/requests/my', {
					params: {
						page: 1,
						pageSize: 100,
					}
				});
				const items = response.data?.data?.items || [];
				const sortedRequests = items.map((item: any) => ({
					id: String(item.id),
					equipmentName: item.equipmentName,
					borrowDate: item.startDate,
					returnDate: item.endDate,
					createdDate: item.createdAt,
					status: item.status,
					history: [],
				})).sort((a: any, b: any) =>
					dayjs(b.createdDate).diff(dayjs(a.createdDate))
				);
				setRequests(sortedRequests);
			} catch (error) {
				console.error('Failed to fetch requests:', error);
				setRequests([]);
			} finally {
				setLoading(false);
			}
		};
		fetchRequests();
	}, []);

	const handleViewDetail = async (record: BorrowRequest) => {
		try {
			const detailResponse = await axios.get(`/requests/${record.id}`);
			const detail = detailResponse.data?.data;
			if (detail) {
				let logs: any[] = [];
				try {
					const logsResponse = await axios.get(`/requests/${record.id}/logs`);
					logs = logsResponse.data?.data || [];
				} catch (err) {
					console.error('Failed to fetch request logs:', err);
				}

				const historyEntries: HistoryEntry[] = logs.map((log: any) => {
					let status = 'PENDING';
					let note = '';
					
					let detailObj: any = {};
					if (typeof log.detail === 'string') {
						try {
							detailObj = JSON.parse(log.detail);
						} catch (e) {}
					} else if (typeof log.detail === 'object') {
						detailObj = log.detail || {};
					}

					switch (log.action) {
						case 'CREATE_REQUEST':
							status = 'PENDING';
							note = detail.note || 'Yêu cầu được tạo';
							break;
						case 'APPROVE_REQUEST':
							status = 'APPROVED';
							note = 'Yêu cầu mượn thiết bị đã được duyệt';
							break;
						case 'REJECT_REQUEST':
							status = 'REJECTED';
							note = detailObj.reason || detail.reason || 'Yêu cầu bị từ chối';
							break;
						case 'RETURN_REQUEST':
							status = 'RETURNED';
							note = 'Đã hoàn trả thiết bị';
							break;
						case 'MARK_OVERDUE':
							status = 'OVERDUE';
							note = 'Quá hạn trả thiết bị';
							break;
						default:
							status = log.action;
							note = log.detail || '';
					}

					return {
						status,
						date: log.createdAt,
						user: log.userName || 'Hệ thống',
						note,
					};
				});

				if (historyEntries.length === 0) {
					historyEntries.push({
						status: 'PENDING',
						date: detail.createdAt,
						user: detail.studentName || 'Sinh viên',
						note: detail.note || 'Yêu cầu được tạo',
					});
					if (detail.status !== 'PENDING') {
						historyEntries.push({
							status: detail.status,
							date: detail.createdAt,
							user: detail.status === 'REJECTED' ? 'Người duyệt' : 'Hệ thống',
							note: detail.status === 'REJECTED' ? detail.reason : undefined,
						});
					}
				}

				const mappedDetail: BorrowRequest = {
					id: String(detail.id),
					equipmentName: detail.equipmentName,
					borrowDate: detail.startDate,
					returnDate: detail.endDate,
					createdDate: detail.createdAt,
					status: detail.status,
					rejectionReason: detail.reason || undefined,
					history: [...historyEntries].reverse(),
				};
				setSelectedRequest(mappedDetail);
				setModalVisible(true);
			}
		} catch (error) {
			console.error('Failed to fetch request detail:', error);
			setSelectedRequest(record);
			setModalVisible(true);
		}
	};

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
			render: (_: any, record: BorrowRequest) => (
				<Button
					type="link"
					icon={<EyeOutlined />}
					onClick={() => handleViewDetail(record)}
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
									color={
										entry.status === 'PENDING' ? 'orange' :
										entry.status === 'APPROVED' ? 'green' :
										entry.status === 'REJECTED' ? 'red' :
										entry.status === 'RETURNED' ? 'cyan' :
										'blue'
									}
								>
									<p>
										<strong>{entry.user}</strong> - {dayjs(entry.date).format('DD/MM/YYYY HH:mm')}
										<Tag
											color={
												entry.status === 'PENDING' ? 'orange' :
												entry.status === 'APPROVED' ? 'green' :
												entry.status === 'REJECTED' ? 'red' :
												entry.status === 'RETURNED' ? 'cyan' :
												'purple'
											}
											style={{ marginLeft: 8 }}
										>
											{entry.status}
										</Tag>
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
