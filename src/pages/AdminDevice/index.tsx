// ============================================================
// pages/admin/equipment/index.tsx
// Trang Quản lý thiết bị (/admin/equipment)
// ============================================================

import React, { useEffect, useState, useCallback } from 'react';
import {
  Row, Col, Card, Table, Tag, Typography, Skeleton,
  Space, Button, Input, Select, Tooltip, Popconfirm,
  Avatar, message, Empty, Badge,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  NumberOutlined,
  FilterOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

import {
  MOCK_EQUIPMENT,
  MOCK_ACTIVITY_LOGS,
  getStockStatus,
  STOCK_STATUS_LABEL,
  STOCK_STATUS_COLOR,
  type Equipment,
  type StockFilter,
  type ActivityLog,
} from './mockData';

import EquipmentFormModal, {
  type EquipmentFormValues,
} from './components/EquipmentFormModal';

import StockUpdateModal, {
  type StockUpdateFormValues,
} from './components/StockUpdateModal';

const { Title, Text } = Typography;
const { Search } = Input;
const { Option } = Select;

// ─── Giả lập delay API ───────────────────────────────────────
const simulateFetch = (ms = 900): Promise<void> =>
  new Promise((resolve) => setTimeout(resolve, ms));

let logIdCounter = MOCK_ACTIVITY_LOGS.length + 1;

const writeLog = (
  log: Omit<ActivityLog, 'id' | 'timestamp' | 'performedBy'>,
  logs: ActivityLog[],
  setLogs: React.Dispatch<React.SetStateAction<ActivityLog[]>>,
) => {
  const newEntry: ActivityLog = {
    ...log,
    id: `LOG${String(logIdCounter++).padStart(3, '0')}`,
    performedBy: 'Admin',
    timestamp: new Date().toISOString(),
  };
  setLogs((prev) => [newEntry, ...prev]);
};

// ─── Component chính ─────────────────────────────────────────

const EquipmentPage: React.FC = () => {
  // ── State ──────────────────────────────────────────────────
  const [loading, setLoading]                   = useState<boolean>(true);
  const [submitting, setSubmitting]             = useState<boolean>(false);
  const [equipments, setEquipments]             = useState<Equipment[]>([]);
  const [activityLogs, setActivityLogs]         = useState<ActivityLog[]>(MOCK_ACTIVITY_LOGS);

  const [searchText, setSearchText]             = useState<string>('');
  const [stockFilter, setStockFilter]           = useState<StockFilter>('ALL');

  const [formOpen, setFormOpen]                 = useState<boolean>(false);
  const [formMode, setFormMode]                 = useState<'create' | 'edit'>('create');
  const [editingDevice, setEditingDevice]       = useState<Equipment | null>(null);

  const [stockModalOpen, setStockModalOpen]     = useState<boolean>(false);
  const [stockDevice, setStockDevice]           = useState<Equipment | null>(null);

  // ── Load dữ liệu giả lập ──────────────────────────────────
  useEffect(() => {
    simulateFetch().then(() => {
      setEquipments(MOCK_EQUIPMENT);
      setLoading(false);
    });
  }, []);

  const handleRefresh = () => {
    setLoading(true);
    setSearchText('');
    setStockFilter('ALL');
    simulateFetch(700).then(() => {
      setEquipments([...MOCK_EQUIPMENT]);
      setLoading(false);
    });
  };

  // ── Filtered data ──────────────────────────────────────────
  const filteredData = equipments
    .filter((e) => !e.is_deleted)
    .filter((e) =>
      searchText
        ? e.name.toLowerCase().includes(searchText.toLowerCase())
        : true,
    )
    .filter((e) => {
      if (stockFilter === 'ALL') return true;
      return getStockStatus(e.availableQuantity, e.totalQuantity) === stockFilter;
    });

  // ── Thêm thiết bị ──────────────────────────────────────────
  const handleCreate = useCallback(
    async (values: EquipmentFormValues, imagePreview: string) => {
      setSubmitting(true);
      await simulateFetch(600);

      const newDevice: Equipment = {
        id: `EQ${String(equipments.length + 1).padStart(3, '0')}`,
        name: values.name,
        description: values.description || '',
        imageUrl: imagePreview || 'https://placehold.co/80x80/8c8c8c/ffffff?text=IMG',
        totalQuantity: values.totalQuantity,
        availableQuantity: values.totalQuantity, // ban đầu còn đủ
        createdAt: new Date().toISOString(),
        is_deleted: false,
        hasApprovedRequest: false,
      };

      setEquipments((prev) => [...prev, newDevice]);
      writeLog(
        { action: 'CREATE_DEVICE', deviceId: newDevice.id, deviceName: newDevice.name },
        activityLogs,
        setActivityLogs,
      );

      setSubmitting(false);
      setFormOpen(false);
      message.success(`Đã thêm thiết bị "${newDevice.name}" thành công!`);
    },
    [equipments, activityLogs],
  );

  // ── Sửa thiết bị ──────────────────────────────────────────
  const handleUpdate = useCallback(
    async (values: EquipmentFormValues, imagePreview: string) => {
      if (!editingDevice) return;
      setSubmitting(true);
      await simulateFetch(600);

      const diff = values.totalQuantity - editingDevice.totalQuantity;
      const newAvailable = Math.max(0, editingDevice.availableQuantity + diff);

      setEquipments((prev) =>
        prev.map((e) =>
          e.id === editingDevice.id
            ? {
                ...e,
                name: values.name,
                description: values.description || '',
                imageUrl: imagePreview || e.imageUrl,
                totalQuantity: values.totalQuantity,
                availableQuantity: newAvailable,
              }
            : e,
        ),
      );
      writeLog(
        { action: 'UPDATE_DEVICE', deviceId: editingDevice.id, deviceName: values.name },
        activityLogs,
        setActivityLogs,
      );

      setSubmitting(false);
      setFormOpen(false);
      setEditingDevice(null);
      message.success(`Đã cập nhật thiết bị "${values.name}"!`);
    },
    [editingDevice, activityLogs],
  );

  // ── Xóa thiết bị (soft delete) ────────────────────────────
  const handleDelete = useCallback(
    async (device: Equipment) => {
      if (device.hasApprovedRequest) {
        message.error('Không thể xóa: Thiết bị đang có yêu cầu được APPROVED!');
        return;
      }
      setEquipments((prev) =>
        prev.map((e) => (e.id === device.id ? { ...e, is_deleted: true } : e)),
      );
      writeLog(
        { action: 'DELETE_DEVICE', deviceId: device.id, deviceName: device.name },
        activityLogs,
        setActivityLogs,
      );
      message.success(`Đã xóa thiết bị "${device.name}".`);
    },
    [activityLogs],
  );

  // ── Cập nhật tồn kho ──────────────────────────────────────
  const handleStockUpdate = useCallback(
    async (values: StockUpdateFormValues) => {
      if (!stockDevice) return;
      setSubmitting(true);
      await simulateFetch(500);

      setEquipments((prev) =>
        prev.map((e) =>
          e.id === stockDevice.id
            ? { ...e, availableQuantity: values.newAvailable }
            : e,
        ),
      );
      writeLog(
        {
          action: 'UPDATE_STOCK',
          deviceId: stockDevice.id,
          deviceName: stockDevice.name,
          note: values.reason,
        },
        activityLogs,
        setActivityLogs,
      );

      setSubmitting(false);
      setStockModalOpen(false);
      setStockDevice(null);
      message.success(`Đã cập nhật tồn kho cho "${stockDevice.name}".`);
    },
    [stockDevice, activityLogs],
  );

  // ── Table Columns ──────────────────────────────────────────
  const COLUMNS: ColumnsType<Equipment> = [
    {
      title: 'Thiết bị',
      key: 'device',
      width: 280,
      render: (_, record) => (
        <Space>
          <Avatar
            src={record.imageUrl}
            size={48}
            shape="square"
            style={{ borderRadius: 6, flexShrink: 0 }}
          />
          <div>
            <Text strong style={{ display: 'block', lineHeight: '1.4' }}>
              {record.name}
            </Text>
            <Text type="secondary" style={{ fontSize: 12 }} ellipsis={{ tooltip: record.description }}>
              {record.description || '—'}
            </Text>
          </div>
        </Space>
      ),
    },
    {
      title: 'Tổng SL',
      dataIndex: 'totalQuantity',
      width: 90,
      align: 'center',
      sorter: (a, b) => a.totalQuantity - b.totalQuantity,
      render: (v: number) => <Text strong>{v}</Text>,
    },
    {
      title: 'Còn lại',
      dataIndex: 'availableQuantity',
      width: 90,
      align: 'center',
      sorter: (a, b) => a.availableQuantity - b.availableQuantity,
      render: (v: number, record) => {
        const status = getStockStatus(v, record.totalQuantity);
        return (
          <Tag color={STOCK_STATUS_COLOR[status]} style={{ minWidth: 32, textAlign: 'center' }}>
            {v}
          </Tag>
        );
      },
    },
    {
      title: 'Tình trạng',
      key: 'status',
      width: 120,
      align: 'center',
      render: (_, record) => {
        const status = getStockStatus(record.availableQuantity, record.totalQuantity);
        return (
          <Tag color={STOCK_STATUS_COLOR[status]}>
            {STOCK_STATUS_LABEL[status]}
          </Tag>
        );
      },
    },
    {
      title: 'Ngày thêm',
      dataIndex: 'createdAt',
      width: 120,
      sorter: (a, b) =>
        new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
      render: (v: string) => (
        <Text style={{ fontSize: 13 }}>
          {dayjs(v).format('DD/MM/YYYY')}
        </Text>
      ),
    },
    {
      title: 'Thao tác',
      key: 'actions',
      width: 160,
      fixed: 'right',
      align: 'center',
      render: (_, record) => {
        return (
          <Space size={4}>
            {/* Cập nhật tồn kho */}
            <Tooltip title="Cập nhật tồn kho">
              <Button
                type="text"
                size="small"
                icon={<NumberOutlined />}
                style={{ color: '#1890ff' }}
                onClick={() => {
                  setStockDevice(record);
                  setStockModalOpen(true);
                }}
              />
            </Tooltip>

            {/* Sửa */}
            <Tooltip title="Chỉnh sửa">
              <Button
                type="text"
                size="small"
                icon={<EditOutlined />}
                style={{ color: '#52c41a' }}
                onClick={() => {
                  setEditingDevice(record);
                  setFormMode('edit');
                  setFormOpen(true);
                }}
              />
            </Tooltip>

            {/* Xóa */}
            <Tooltip
              title={
                record.hasApprovedRequest
                  ? 'Không thể xóa: đang có yêu cầu APPROVED'
                  : 'Xóa thiết bị'
              }
            >
              <Popconfirm
                  title={`Xoá thiết bị — Bạn chắc chắn muốn xóa "${record.name}"? Hành động không thể hoàn tác.`}
                  okText="Xoá"
                  okButtonProps={{ danger: true }}
                  cancelText="Huỷ"
                  disabled={record.hasApprovedRequest}
                  onConfirm={() => handleDelete(record)}
                >
                <Button
                  type="text"
                  size="small"
                  danger
                  disabled={record.hasApprovedRequest}
                  icon={<DeleteOutlined />}
                />
              </Popconfirm>
            </Tooltip>
          </Space>
        );
      },
    },
  ];

  // ── Summary stats ──────────────────────────────────────────
  const activeEquipments = equipments.filter((e) => !e.is_deleted);
  const outOfStock = activeEquipments.filter(
    (e) => getStockStatus(e.availableQuantity, e.totalQuantity) === 'OUT_OF_STOCK',
  ).length;
  const lowStock = activeEquipments.filter(
    (e) => getStockStatus(e.availableQuantity, e.totalQuantity) === 'LOW_STOCK',
  ).length;

  // ── Render ──────────────────────────────────────────────────
  return (
    <Space direction="vertical" size={20} style={{ display: 'flex' }}>
      {/* ── Header ──────────────────────────────────────────── */}
      <Row justify="space-between" align="middle" wrap={false}>
        <Col>
          <Title level={4} style={{ margin: 0 }}>
            Quản lý thiết bị
          </Title>
          {!loading && (
            <Text type="secondary" style={{ fontSize: 13 }}>
              {activeEquipments.length} thiết bị đang hoạt động
              {outOfStock > 0 && (
                <Badge
                  count={`${outOfStock} hết hàng`}
                  style={{ marginLeft: 8, background: '#ff4d4f' }}
                />
              )}
              {lowStock > 0 && (
                <Badge
                  count={`${lowStock} sắp hết`}
                  style={{ marginLeft: 4, background: '#fa8c16' }}
                />
              )}
            </Text>
          )}
        </Col>
        <Col>
          <Space>
            <Tooltip title="Làm mới">
              <Button icon={<ReloadOutlined />} onClick={handleRefresh} />
            </Tooltip>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => {
                setFormMode('create');
                setEditingDevice(null);
                setFormOpen(true);
              }}
            >
              Thêm thiết bị
            </Button>
          </Space>
        </Col>
      </Row>

      {/* ── Thanh tìm kiếm + lọc ────────────────────────────── */}
      <Card bordered={false} style={{ borderRadius: 8 }} bodyStyle={{ padding: '16px 20px' }}>
        <Row gutter={[12, 12]} align="middle">
          <Col xs={24} sm={14} md={16}>
            <Search
              prefix={<SearchOutlined style={{ color: '#bfbfbf' }} />}
              placeholder="Tìm kiếm theo tên thiết bị..."
              allowClear
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              style={{ width: '100%' }}
            />
          </Col>
          <Col xs={24} sm={10} md={8}>
            <Select
              value={stockFilter}
              onChange={(v) => setStockFilter(v)}
              style={{ width: '100%' }}
              suffixIcon={<FilterOutlined />}
            >
              <Option value="ALL">Tất cả tình trạng</Option>
              <Option value="IN_STOCK">
                <Tag color="success" style={{ margin: 0 }}>Còn hàng</Tag>
              </Option>
              <Option value="LOW_STOCK">
                <Tag color="warning" style={{ margin: 0 }}>Sắp hết</Tag>
              </Option>
              <Option value="OUT_OF_STOCK">
                <Tag color="error" style={{ margin: 0 }}>Hết hàng</Tag>
              </Option>
            </Select>
          </Col>
        </Row>
      </Card>

      {/* ── Bảng danh sách ──────────────────────────────────── */}
      <Card bordered={false} style={{ borderRadius: 8 }}>
        {loading ? (
          <Space direction="vertical" style={{ display: 'flex' }} size={16}>
            {Array.from({ length: 5 }).map((_, i) => (
              <Skeleton key={i} active avatar paragraph={{ rows: 1 }} />
            ))}
          </Space>
        ) : (
          <Table<Equipment>
            columns={COLUMNS}
            dataSource={filteredData}
            rowKey="id"
            scroll={{ x: 800 }}
            size="middle"
            pagination={{
              pageSize: 8,
              showSizeChanger: true,
              pageSizeOptions: ['8', '16', '32'],
              showTotal: (total) => `${total} thiết bị`,
            }}
            locale={{
              emptyText: (
                <Empty
                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                  description={
                    searchText || stockFilter !== 'ALL'
                      ? 'Không tìm thấy thiết bị phù hợp với bộ lọc.'
                      : 'Chưa có thiết bị nào. Hãy thêm thiết bị đầu tiên!'
                  }
                >
                  {!(searchText || stockFilter !== 'ALL') && (
                    <Button
                      type="primary"
                      icon={<PlusOutlined />}
                      onClick={() => {
                        setFormMode('create');
                        setEditingDevice(null);
                        setFormOpen(true);
                      }}
                    >
                      Thêm thiết bị
                    </Button>
                  )}
                </Empty>
              ),
            }}
          />
        )}
      </Card>

      {/* ── Modal Thêm / Sửa ────────────────────────────────── */}
      <EquipmentFormModal
        open={formOpen}
        mode={formMode}
        initialData={editingDevice}
        onCancel={() => {
          setFormOpen(false);
          setEditingDevice(null);
        }}
        onSubmit={formMode === 'create' ? handleCreate : handleUpdate}
        confirmLoading={submitting}
      />

      {/* ── Modal Cập nhật tồn kho ───────────────────────────── */}
      <StockUpdateModal
        open={stockModalOpen}
        device={stockDevice}
        onCancel={() => {
          setStockModalOpen(false);
          setStockDevice(null);
        }}
        onSubmit={handleStockUpdate}
        confirmLoading={submitting}
      />
    </Space>
  );
};

export default EquipmentPage;