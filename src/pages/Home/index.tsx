import React, { useState, useEffect, useMemo } from 'react';
import { Row, Col, Input, Select, Pagination, Empty, Tag, Button, Space } from 'antd';
import { SearchOutlined, ShoppingCartOutlined, StopOutlined } from '@ant-design/icons';
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


const SkeletonCard: React.FC = () => (
    <div className={styles.card}>
        <div className={styles.skeletonCover} />
        <div className={styles.cardBody}>
            <div className={`${styles.skeletonLine} ${styles.skeletonTitle}`} />
            <div className={`${styles.skeletonLine} ${styles.skeletonDesc}`} />
            <div className={`${styles.skeletonLine} ${styles.skeletonDescShort}`} />
            <div className={styles.skeletonInfo}>
                <div className={`${styles.skeletonLine} ${styles.skeletonBadge}`} />
                <div className={`${styles.skeletonLine} ${styles.skeletonTag}`} />
            </div>
            <div className={`${styles.skeletonLine} ${styles.skeletonBtn}`} />
        </div>
    </div>
);


const Home: React.FC = () => {
    const [equipment, setEquipment] = useState<Equipment[]>([]);
    const [totalItems, setTotalItems] = useState(0);
    const [loading, setLoading] = useState(true);
    const [searchText, setSearchText] = useState('');
    const [statusFilter, setStatusFilter] = useState<'all' | 'available' | 'unavailable'>('all');
    const [currentPage, setCurrentPage] = useState(1);
    const pageSize = 9;
    const debounceTimer = React.useRef<NodeJS.Timeout>();


    const fetchEquipment = async (page = 1, query = '') => {
        setLoading(true);
        try {
            const response = await axios.get('/equipment', {
                params: { page, pageSize, keyword: query || undefined },
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


    const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        setSearchText(value);
        setCurrentPage(1);
        if (debounceTimer.current) clearTimeout(debounceTimer.current);
        debounceTimer.current = setTimeout(() => fetchEquipment(1, value), 300);
    };


    const filteredEquipment = useMemo(() => {
        return equipment.filter((item) => {
            const matchesStatus =
                statusFilter === 'all' ||
                (statusFilter === 'available' && item.availableQuantity > 0) ||
                (statusFilter === 'unavailable' && item.availableQuantity === 0);
            return matchesStatus;
        });
    }, [equipment, statusFilter]);


    return (
        <div className={styles.container}>
            {/* Header */}
            <div className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>Danh Sách Thiết Bị Mượn</h1>
                <p className={styles.pageSubtitle}>Khám phá và đặt mượn thiết bị câu lạc bộ</p>
            </div>


            {/* Filter Bar */}
            <div className={styles.filterBar}>
                <Space wrap>
                    <Input
                        placeholder="Tìm kiếm thiết bị..."
                        prefix={<SearchOutlined className={styles.searchIcon} />}
                        value={searchText}
                        onChange={handleSearchChange}
                        className={styles.searchInput}
                    />
                    <Select
                        value={statusFilter}
                        onChange={setStatusFilter}
                        className={styles.statusSelect}
                    >
                        <Select.Option value="all">Tất cả</Select.Option>
                        <Select.Option value="available">Còn hàng</Select.Option>
                        <Select.Option value="unavailable">Hết hàng</Select.Option>
                    </Select>
                </Space>
            </div>


            {/* Grid */}
            {loading ? (
                <Row gutter={[20, 20]}>
                    {Array.from({ length: 9 }).map((_, i) => (
                        <Col key={i} xs={24} sm={12} lg={8}>
                            <SkeletonCard />
                        </Col>
                    ))}
                </Row>
            ) : filteredEquipment.length === 0 ? (
                <Empty description="Không tìm thấy thiết bị nào" style={{ marginTop: 64 }} />
            ) : (
                <>
                    <Row gutter={[20, 20]}>
                        {filteredEquipment.map((item) => {
                            const unavailable = item.availableQuantity === 0;
                            return (
                                <Col key={item.id} xs={24} sm={12} lg={8}>
                                    <div className={`${styles.card} ${unavailable ? styles.cardUnavailable : ''}`}>
                                        {/* Cover image */}
                                        <div className={styles.cardCover}>
                                            <img
                                                alt={item.name}
                                                src={item.image}
                                                className={styles.coverImg}
                                            />
                                            {unavailable && (
                                                <div className={styles.coverOverlay}>
                                                    <span className={styles.overlayLabel}>
                                                        <StopOutlined /> Hết hàng
                                                    </span>
                                                </div>
                                            )}
                                        </div>


                                        {/* Body */}
                                        <div className={styles.cardBody}>
                                            <div className={styles.cardMeta}>
                                                <h3 className={styles.cardTitle}>{item.name}</h3>
                                                <p className={styles.cardDesc}>{item.description}</p>
                                            </div>


                                            <div className={styles.cardInfo}>
                                                <span className={styles.quantity}>
                                                    Còn lại:{' '}
                                                    <strong className={unavailable ? styles.quantityZero : styles.quantityAvail}>
                                                        {item.availableQuantity}/{item.totalQuantity}
                                                    </strong>
                                                </span>
                                                {unavailable ? (
                                                    <Tag color="red" className={styles.statusTag}>Hết hàng</Tag>
                                                ) : (
                                                    <Tag color="green" className={styles.statusTag}>Còn hàng</Tag>
                                                )}
                                            </div>


                                            <Button
                                                type="primary"
                                                block
                                                icon={<ShoppingCartOutlined />}
                                                className={styles.borrowBtn}
                                                onClick={() => history.push(`/equipment/${item.id}`)}
                                                disabled={unavailable}
                                            >
                                                Mượn Thiết Bị
                                            </Button>
                                        </div>
                                    </div>
                                </Col>
                            );
                        })}
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
