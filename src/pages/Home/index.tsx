import React, { useState, useEffect, useMemo } from 'react';
import { Card, Row, Col, Input, Select, Pagination, Empty, Skeleton, Tag, Button, Space } from 'antd';
import { SearchOutlined, ShoppingCartOutlined } from '@ant-design/icons';
import { history } from 'umi';
import axios from '@/utils/axios';
import styles from './index.less';

interface Equipment {
	id: string;
	name: string;
	description: string;
	image: string;
	totalQuantity: number;
	availableQuantity: number;
	isDeleted: boolean;
	status: 'available' | 'unavailable';
}

const Home: React.FC = () => {
	const [equipment, setEquipment] = useState<Equipment[]>([]);
	const [loading, setLoading] = useState(true);
	const [searchText, setSearchText] = useState('');
	const [statusFilter, setStatusFilter] = useState<'all' | 'available' | 'unavailable'>('all');
	const [currentPage, setCurrentPage] = useState(1);
	const pageSize = 9;
	const debounceTimer = React.useRef<NodeJS.Timeout>();

	// Fetch equipment list
	const fetchEquipment = async (query = '') => {
		setLoading(true);
		try {
			const response = await axios.get('/equipment', {
				params: { search: query },
			});
			const equipmentList = (response.data?.data || []).filter((item: Equipment) => !item.isDeleted);
			setEquipment(equipmentList);
		} catch (error) {
			console.error('Failed to fetch equipment:', error);
			// Mock data fallback
			const mockEquipment: Equipment[] = [
				{
					id: '1',
					name: 'Laptop Dell XPS 15',
					description: 'Laptop cao cấp cho đồ án',
					image: 'https://via.placeholder.com/300x200?text=Laptop+XPS',
					totalQuantity: 5,
					availableQuantity: 2,
					isDeleted: false,
					status: 'available',
				},
				{
					id: '2',
					name: 'Máy Chiếu Epson',
					description: 'Máy chiếu 1080p cho tiết học',
					image: 'https://via.placeholder.com/300x200?text=Projector',
					totalQuantity: 3,
					availableQuantity: 1,
					isDeleted: false,
					status: 'available',
				},
				{
					id: '3',
					name: 'Bộ Vi Xử Lý Raspberry Pi',
					description: 'Bo mạch nhúng cho IoT',
					image: 'https://via.placeholder.com/300x200?text=RaspberryPi',
					totalQuantity: 10,
					availableQuantity: 0,
					isDeleted: false,
					status: 'unavailable',
				},
				{
					id: '4',
					name: 'Máy Ảnh Canon EOS',
					description: 'Máy ảnh chuyên nghiệp',
					image: 'https://via.placeholder.com/300x200?text=Camera',
					totalQuantity: 2,
					availableQuantity: 2,
					isDeleted: false,
					status: 'available',
				},
				{
					id: '5',
					name: 'Loa Bluetooth JBL',
					description: 'Loa di động chất lượng cao',
					image: 'https://via.placeholder.com/300x200?text=Speaker',
					totalQuantity: 8,
					availableQuantity: 5,
					isDeleted: false,
					status: 'available',
				},
				{
					id: '6',
					name: 'Tablet iPad Pro',
					description: 'Tablet cao cấp 12.9 inch',
					image: 'https://via.placeholder.com/300x200?text=iPad',
					totalQuantity: 4,
					availableQuantity: 1,
					isDeleted: false,
					status: 'available',
				},
				{
					id: '7',
					name: 'Máy In Brother HL-L8360CDW',
					description: 'Máy in laser đa năng',
					image: 'https://via.placeholder.com/300x200?text=Printer',
					totalQuantity: 3,
					availableQuantity: 0,
					isDeleted: false,
					status: 'unavailable',
				},
				{
					id: '8',
					name: 'Router WiFi TP-Link',
					description: 'Router WiFi 6 tốc độ cao',
					image: 'https://via.placeholder.com/300x200?text=Router',
					totalQuantity: 6,
					availableQuantity: 4,
					isDeleted: false,
					status: 'available',
				},
				{
					id: '9',
					name: 'Bộ Micro Condenser',
					description: 'Micro chuyên nghiệp ghi âm',
					image: 'https://via.placeholder.com/300x200?text=Microphone',
					totalQuantity: 5,
					availableQuantity: 3,
					isDeleted: false,
					status: 'available',
				},
			];
			setEquipment(mockEquipment);
		} finally {
			setLoading(false);
		}
	};

	useEffect(() => {
		fetchEquipment();
	}, []);

	// Search with debounce
	const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
		const value = e.target.value;
		setSearchText(value);
		setCurrentPage(1);

		if (debounceTimer.current) {
			clearTimeout(debounceTimer.current);
		}

		debounceTimer.current = setTimeout(() => {
			fetchEquipment(value);
		}, 300);
	};

	// Filter and search logic
	const filteredEquipment = useMemo(() => {
		return equipment.filter((item) => {
			const matchesSearch = item.name.toLowerCase().includes(searchText.toLowerCase()) ||
				item.description.toLowerCase().includes(searchText.toLowerCase());

			const matchesStatus = statusFilter === 'all' ||
				(statusFilter === 'available' && item.availableQuantity > 0) ||
				(statusFilter === 'unavailable' && item.availableQuantity === 0);

			return matchesSearch && matchesStatus;
		});
	}, [equipment, searchText, statusFilter]);

	// Pagination
	const paginatedEquipment = useMemo(() => {
		const startIndex = (currentPage - 1) * pageSize;
		return filteredEquipment.slice(startIndex, startIndex + pageSize);
	}, [filteredEquipment, currentPage]);

	return (
		<div className={styles.container}>
			<div className={styles.header}>
				<h1>Danh Sách Thiết Bị Mượn</h1>
			</div>

			<div className={styles.filters}>
				<Space>
					<Input
						placeholder='Tìm kiếm thiết bị...'
						prefix={<SearchOutlined />}
						value={searchText}
						onChange={handleSearchChange}
						style={{ width: 300 }}
					/>
					<Select
						value={statusFilter}
						onChange={setStatusFilter}
						style={{ width: 150 }}
					>
						<Select.Option value="all">Tất cả</Select.Option>
						<Select.Option value="available">Còn hàng</Select.Option>
						<Select.Option value="unavailable">Hết hàng</Select.Option>
					</Select>
				</Space>
			</div>

			{loading && paginatedEquipment.length === 0 ? (
				<Row gutter={[16, 16]}>
					{Array.from({ length: 9 }).map((_, i) => (
						<Col key={i} xs={24} sm={12} lg={8}>
							<Skeleton active paragraph={{ rows: 4 }} />
						</Col>
					))}
				</Row>
			) : paginatedEquipment.length === 0 ? (
				<Empty
					description="Không tìm thấy thiết bị nào"
					style={{ marginTop: 48 }}
				/>
			) : (
				<>
					<Row gutter={[16, 16]}>
						{paginatedEquipment.map((item) => (
							<Col key={item.id} xs={24} sm={12} lg={8}>
								<Card
									hoverable
									cover={
										<img
											alt={item.name}
											src={item.image}
											style={{ height: 200, objectFit: 'cover' }}
										/>
									}
									className={styles.card}
								>
									<Card.Meta
										title={item.name}
										description={item.description}
									/>
									<div className={styles.info}>
										<div className={styles.quantity}>
											<span>Còn lại: <strong>{item.availableQuantity}/{item.totalQuantity}</strong></span>
										</div>
										<div className={styles.status}>
											{item.availableQuantity > 0 ? (
												<Tag color="green">Còn hàng</Tag>
											) : (
												<Tag color="red">Hết hàng</Tag>
											)}
										</div>
									</div>
									<Button
										type="primary"
										block
										icon={<ShoppingCartOutlined />}
										onClick={() => history.push(`/equipment/${item.id}`)}
										disabled={item.availableQuantity === 0}
									>
										Mượn Thiết Bị
									</Button>
								</Card>
							</Col>
						))}
					</Row>

					<div className={styles.pagination}>
						<Pagination
							current={currentPage}
							pageSize={pageSize}
							total={filteredEquipment.length}
							onChange={setCurrentPage}
							showSizeChanger={false}
						/>
					</div>
				</>
			)}
		</div>
	);
};

export default Home;
