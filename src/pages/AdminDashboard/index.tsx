// ============================================================
// pages/admin/dashboard/index.tsx
// Trang Dashboard Admin (/admin/dashboard)
// Gồm: 4 stat cards + biểu đồ cột + biểu đồ tròn
//       + bảng top 5 thiết bị + danh sách 5 PENDING mới nhất
// Toàn bộ dùng mock data, skeleton loading cho từng block
// ============================================================

import React, { useEffect, useState } from 'react';
import {
  Row, Col, Card, Statistic, Table, Tag, Typography,
  Skeleton, Space, Button, Tooltip,
} from 'antd';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as ReTooltip,
  ResponsiveContainer, PieChart, Pie, Cell, Legend,
} from 'recharts';
import {
  ArrowRightOutlined,
  ReloadOutlined,
  FileSearchOutlined,
} from '@ant-design/icons';
import { history } from 'umi';
import type { ColumnsType } from 'antd/es/table';

import {
  MOCK_STAT_CARDS,
  MOCK_MONTHLY_DATA,
  MOCK_STATUS_PIE,
  MOCK_TOP_DEVICES,
  MOCK_PENDING_REQUESTS,
  type TopDevice,
  type PendingRequest,
} from './mockData';

const { Title, Text } = Typography;

// ─── Giả lập delay API ───────────────────────────────────────
const simulateFetch = (ms = 1200): Promise<void> =>
  new Promise((resolve) => setTimeout(resolve, ms));

// ─── Columns: Top 5 thiết bị ─────────────────────────────────
const TOP_DEVICE_COLUMNS: ColumnsType<TopDevice> = [
  {
    title: '#',
    dataIndex: 'rank',
    width: 48,
    render: (rank: number) => (
      <Text strong style={{ color: rank <= 3 ? '#fa8c16' : undefined }}>
        {rank}
      </Text>
    ),
  },
  {
    title: 'Tên thiết bị',
    dataIndex: 'name',
    ellipsis: true,
  },
  {
    title: 'Lượt mượn',
    dataIndex: 'borrowCount',
    width: 100,
    align: 'center',
    render: (v: number) => <Tag color="blue">{v}</Tag>,
  },
  {
    title: 'Còn lại',
    dataIndex: 'available',
    width: 80,
    align: 'center',
    render: (v: number) => (
      <Tag color={v === 0 ? 'red' : 'green'}>{v}</Tag>
    ),
  },
];

// ─── Columns: 5 PENDING mới nhất ─────────────────────────────
const PENDING_COLUMNS: ColumnsType<PendingRequest> = [
  {
    title: 'Mã YC',
    dataIndex: 'id',
    width: 100,
    render: (id: string) => <Text code>{id}</Text>,
  },
  {
    title: 'Sinh viên',
    dataIndex: 'studentName',
  },
  {
    title: 'Thiết bị',
    dataIndex: 'deviceName',
    ellipsis: true,
  },
  {
    title: 'Ngày mượn',
    dataIndex: 'borrowDate',
    width: 110,
  },
  {
    title: 'Ngày gửi',
    dataIndex: 'submittedAt',
    width: 140,
    render: (v: string) => <Text type="secondary">{v}</Text>,
  },
  {
    title: '',
    key: 'action',
    width: 60,
    render: (_: unknown, record: PendingRequest) => (
      <Tooltip title="Xem chi tiết">
        <Button
          type="link"
          size="small"
          icon={<FileSearchOutlined />}
          onClick={() => history.push(`/admin/requests/${record.id}`)}
        />
      </Tooltip>
    ),
  },
];

// ─── Component chính ─────────────────────────────────────────
const AdminDashboard: React.FC = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [chartsLoading, setChartsLoading] = useState<boolean>(true);

  // Giả lập 2 lần fetch độc lập (cards + charts)
  useEffect(() => {
    simulateFetch(900).then(() => setLoading(false));
    simulateFetch(1400).then(() => setChartsLoading(false));
  }, []);

  const handleRefresh = () => {
    setLoading(true);
    setChartsLoading(true);
    simulateFetch(900).then(() => setLoading(false));
    simulateFetch(1400).then(() => setChartsLoading(false));
  };

  return (
    <Space direction="vertical" size={24} style={{ display: 'flex' }}>
      {/* ── Tiêu đề + nút làm mới ──────────────────────────── */}
      <Row justify="space-between" align="middle">
        <Title level={4} style={{ margin: 0 }}>
          Dashboard
        </Title>
        <Button icon={<ReloadOutlined />} onClick={handleRefresh}>
          Làm mới
        </Button>
      </Row>

      {/* ── 4 Stat Cards ───────────────────────────────────── */}
      <Row gutter={[16, 16]}>
        {MOCK_STAT_CARDS.map((card) => (
          <Col xs={24} sm={12} lg={6} key={card.title}>
            <Card bordered={false} style={{ borderRadius: 8 }}>
              {loading ? (
                <Skeleton active paragraph={{ rows: 1 }} />
              ) : (
                <Statistic
                  title={card.title}
                  value={card.value}
                  suffix={card.suffix}
                  valueStyle={{ color: card.color, fontWeight: 700 }}
                />
              )}
            </Card>
          </Col>
        ))}
      </Row>

      {/* ── Biểu đồ cột + Biểu đồ tròn ────────────────────── */}
      <Row gutter={[16, 16]}>
        {/* Biểu đồ cột: lượt mượn theo tháng */}
        <Col xs={24} lg={15}>
          <Card
            title="Lượt mượn theo tháng (2025)"
            bordered={false}
            style={{ borderRadius: 8 }}
          >
            {chartsLoading ? (
              <Skeleton active paragraph={{ rows: 6 }} />
            ) : (
              <ResponsiveContainer width="100%" height={280}>
                <BarChart
                  data={MOCK_MONTHLY_DATA}
                  margin={{ top: 8, right: 16, left: -8, bottom: 0 }}
                >
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                  <YAxis tick={{ fontSize: 12 }} allowDecimals={false} />
                  <ReTooltip
                    formatter={(value: number) => [value, 'Lượt mượn']}
                  />
                  <Bar dataKey="count" fill="#1890ff" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </Card>
        </Col>

        {/* Biểu đồ tròn: tỉ lệ trạng thái */}
        <Col xs={24} lg={9}>
          <Card
            title="Tỉ lệ trạng thái yêu cầu"
            bordered={false}
            style={{ borderRadius: 8 }}
          >
            {chartsLoading ? (
              <Skeleton active paragraph={{ rows: 6 }} />
            ) : (
              <ResponsiveContainer width="100%" height={280}>
                <PieChart>
                  <Pie
                    data={MOCK_STATUS_PIE}
                    dataKey="count"
                    nameKey="label"
                    cx="50%"
                    cy="45%"
                    outerRadius={90}
                    label={({ label, percent }) =>
                      `${label} ${(percent * 100).toFixed(0)}%`
                    }
                    labelLine={false}
                  >
                    {MOCK_STATUS_PIE.map((entry) => (
                      <Cell key={entry.status} fill={entry.color} />
                    ))}
                  </Pie>
                  <Legend
                    iconType="circle"
                    formatter={(value) => (
                      <Text style={{ fontSize: 12 }}>{value}</Text>
                    )}
                  />
                </PieChart>
              </ResponsiveContainer>
            )}
          </Card>
        </Col>
      </Row>

      {/* ── Top 5 thiết bị + 5 PENDING mới nhất ───────────── */}
      <Row gutter={[16, 16]}>
        {/* Top 5 thiết bị mượn nhiều trong tháng */}
        <Col xs={24} lg={10}>
          <Card
            title="Top 5 thiết bị mượn nhiều nhất tháng này"
            bordered={false}
            style={{ borderRadius: 8 }}
            extra={
              <Button
                type="link"
                size="small"
                onClick={() => history.push('/admin/equipment')}
              >
                Xem tất cả <ArrowRightOutlined />
              </Button>
            }
          >
            {loading ? (
              <Skeleton active paragraph={{ rows: 5 }} />
            ) : (
              <Table<TopDevice>
                columns={TOP_DEVICE_COLUMNS}
                dataSource={MOCK_TOP_DEVICES}
                pagination={false}
                size="small"
              />
            )}
          </Card>
        </Col>

        {/* 5 yêu cầu PENDING mới nhất */}
        <Col xs={24} lg={14}>
          <Card
            title={
              <Space>
                <Tag color="default">PENDING</Tag>
                5 yêu cầu chờ duyệt mới nhất
              </Space>
            }
            bordered={false}
            style={{ borderRadius: 8 }}
            extra={
              <Button
                type="link"
                size="small"
                onClick={() =>
                  history.push('/admin/requests?status=PENDING')
                }
              >
                Xem tất cả <ArrowRightOutlined />
              </Button>
            }
          >
            {loading ? (
              <Skeleton active paragraph={{ rows: 5 }} />
            ) : (
              <Table<PendingRequest>
                columns={PENDING_COLUMNS}
                dataSource={MOCK_PENDING_REQUESTS}
                pagination={false}
                size="small"
              />
            )}
          </Card>
        </Col>
      </Row>
    </Space>
  );
};

export default AdminDashboard;