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
	const [totalItems, setTotalItems] = useState(0);
	const [loading, setLoading] = useState(true);
	const [searchText, setSearchText] = useState('');
	const [statusFilter, setStatusFilter] = useState<'all' | 'available' | 'unavailable'>('all');
	const [currentPage, setCurrentPage] = useState(1);
	const pageSize = 9;
	const debounceTimer = React.useRef<NodeJS.Timeout>();

	// Fetch equipment list
	const fetchEquipment = async (page = 1, query = '') => {
		setLoading(true);
		try {
			const response = await axios.get('/equipment', {
				params: {
					page,
					pageSize,
					keyword: query || undefined,
				},
			});
			const responseData = response.data?.data;
			if (responseData && Array.isArray(responseData.items)) {
				const mapped: Equipment[] = responseData.items.map((item: any) => ({
					id: String(item.id),
					name: item.name,
					description: item.description,
					image: item.imageUrl || 'https://via.placeholder.com/300x200?text=Equipment',
					totalQuantity: item.totalQuantity,
					availableQuantity: item.availableQuantity,
					isDeleted: false,
					status: item.availableQuantity > 0 ? 'available' : 'unavailable',
				}));
				setEquipment(mapped);
				setTotalItems(responseData.total || mapped.length);
			} else {
				setEquipment([]);
				setTotalItems(0);
			}
		} catch (error) {
			console.error('Failed to fetch equipment:', error);
			setEquipment([]);
			setTotalItems(0);
		} finally {
			setLoading(false);
		}
	};

	useEffect(() => {
		fetchEquipment(currentPage, searchText);
	}, [currentPage]);

	// Search with debounce
	const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
		const value = e.target.value;
		setSearchText(value);
		setCurrentPage(1);

		if (debounceTimer.current) {
			clearTimeout(debounceTimer.current);
		}

		debounceTimer.current = setTimeout(() => {
			fetchEquipment(1, value);
		}, 300);
	};

	// Filter logic for status
	const filteredEquipment = useMemo(() => {
		return equipment.filter((item) => {
			const matchesStatus = statusFilter === 'all' ||
				(statusFilter === 'available' && item.availableQuantity > 0) ||
				(statusFilter === 'unavailable' && item.availableQuantity === 0);

			return matchesStatus;
		});
	}, [equipment, statusFilter]);

	// Rendered items are simply the filtered equipment on current page
	const paginatedEquipment = filteredEquipment;

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
							total={totalItems}
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
