// src/pages/admin/dashboard/index.tsx
// Đã cập nhật: bỏ @/mocks + simulateFetch, dùng fetchDashboard() từ @/services/dashboard
import React, { useEffect, useState } from 'react';
import {
  Row, Col, Card, Statistic, Table, Tag, Typography,
  Skeleton, Space, Button, Tooltip, message,
} from 'antd';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as ReTooltip,
  ResponsiveContainer, PieChart, Pie, Cell, Legend,
} from 'recharts';
import { ArrowRightOutlined, ReloadOutlined, FileSearchOutlined } from '@ant-design/icons';
import { history } from 'umi';
import type { ColumnsType } from 'antd/es/table';

import {
  fetchDashboard,
  type StatCard,
  type MonthlyBorrowData,
  type StatusPieData,
  type TopDevice,
  type PendingRequest,
} from '@/services/dashboard';

const { Title, Text } = Typography;

// ─── Columns: Top 5 thiết bị ─────────────────────────────────────────────
const TOP_DEVICE_COLUMNS: ColumnsType<TopDevice> = [
  {
    title: '#',
    dataIndex: 'rank',
    width: 48,
    render: (rank: number) => (
      <Text strong style={{ color: rank <= 3 ? '#fa8c16' : undefined }}>{rank}</Text>
    ),
  },
  { title: 'Tên thiết bị', dataIndex: 'name', ellipsis: true },
  {
    title: 'Lượt mượn',
    dataIndex: 'borrowCount',
    width: 100,
    align: 'center',
    render: (v: number) => <Tag color="blue">{v}</Tag>,
  },
];

// ─── Columns: 5 PENDING mới nhất ─────────────────────────────────────────
const PENDING_COLUMNS: ColumnsType<PendingRequest> = [
  {
    title: 'Mã YC',
    dataIndex: 'id',
    width: 100,
    render: (id: string) => <Text code>{id}</Text>,
  },
  { title: 'Sinh viên',  dataIndex: 'studentName' },
  { title: 'Thiết bị',  dataIndex: 'deviceName', ellipsis: true },
  { title: 'Ngày mượn', dataIndex: 'borrowDate',  width: 110 },
  {
    title: 'Ngày gửi',
    dataIndex: 'submittedAt',
    width: 160,
    render: (v: string) => <Text type="secondary">{new Date(v).toLocaleString()}</Text>,
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

// ─── Component chính ──────────────────────────────────────────────────────
const AdminDashboard: React.FC = () => {
  const [loading, setLoading] = useState(true);

  const [statCards,       setStatCards]       = useState<StatCard[]>([]);
  const [monthlyData,     setMonthlyData]     = useState<MonthlyBorrowData[]>([]);
  const [statusPie,       setStatusPie]       = useState<StatusPieData[]>([]);
  const [topDevices,      setTopDevices]      = useState<TopDevice[]>([]);
  const [pendingRequests, setPendingRequests] = useState<PendingRequest[]>([]);

  const load = async () => {
    setLoading(true);
    try {
      const d = await fetchDashboard();
      setStatCards(d.statCards);
      setMonthlyData(d.monthlyData);
      setStatusPie(d.statusPie);
      setTopDevices(d.topDevices);
      setPendingRequests(d.pendingRequests);
    } catch (err: any) {
      message.error(err?.message || 'Không thể tải dữ liệu dashboard');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  return (
    <Space direction="vertical" size={24} style={{ display: 'flex' }}>
      {/* ── Tiêu đề + nút làm mới ──────────────────────────────────────── */}
      <Row justify="space-between" align="middle">
        <Title level={4} style={{ margin: 0 }}>Dashboard</Title>
        <Button icon={<ReloadOutlined />} onClick={load}>Làm mới</Button>
      </Row>

      {/* ── 4 Stat Cards ───────────────────────────────────────────────── */}
      <Row gutter={[16, 16]}>
        {loading
          ? [0, 1, 2, 3].map((i) => (
              <Col xs={24} sm={12} lg={6} key={i}>
                <Card bordered={false} style={{ borderRadius: 8 }}>
                  <Skeleton active paragraph={{ rows: 1 }} />
                </Card>
              </Col>
            ))
          : statCards.map((card) => (
              <Col xs={24} sm={12} lg={6} key={card.title}>
                <Card bordered={false} style={{ borderRadius: 8 }}>
                  <Statistic
                    title={card.title}
                    value={card.value}
                    suffix={card.suffix}
                    valueStyle={{ color: card.color, fontWeight: 700 }}
                  />
                </Card>
              </Col>
            ))}
      </Row>

      {/* ── Biểu đồ cột + Biểu đồ tròn ────────────────────────────────── */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={15}>
          <Card title="Lượt mượn theo tháng" bordered={false} style={{ borderRadius: 8 }}>
            {loading ? (
              <Skeleton active paragraph={{ rows: 6 }} />
            ) : (
              <ResponsiveContainer width="100%" height={280}>
                <BarChart data={monthlyData} margin={{ top: 8, right: 16, left: -8, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                  <YAxis tick={{ fontSize: 12 }} allowDecimals={false} />
                  <ReTooltip formatter={(v: number) => [v, 'Lượt mượn']} />
                  <Bar dataKey="count" fill="#1890ff" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </Card>
        </Col>

        <Col xs={24} lg={9}>
          <Card title="Tỉ lệ trạng thái yêu cầu" bordered={false} style={{ borderRadius: 8 }}>
            {loading ? (
              <Skeleton active paragraph={{ rows: 6 }} />
            ) : (
              <ResponsiveContainer width="100%" height={280}>
                <PieChart>
                  <Pie
                    data={statusPie}
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
                    {statusPie.map((entry) => (
                      <Cell key={entry.status} fill={entry.color} />
                    ))}
                  </Pie>
                  <Legend
                    iconType="circle"
                    formatter={(v) => <Text style={{ fontSize: 12 }}>{v}</Text>}
                  />
                </PieChart>
              </ResponsiveContainer>
            )}
          </Card>
        </Col>
      </Row>

      {/* ── Top 5 thiết bị + 5 PENDING mới nhất ───────────────────────── */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={10}>
          <Card
            title="Top 5 thiết bị mượn nhiều nhất tháng này"
            bordered={false}
            style={{ borderRadius: 8 }}
            extra={
              <Button type="link" size="small" onClick={() => history.push('/admin/equipment')}>
                Xem tất cả <ArrowRightOutlined />
              </Button>
            }
          >
            {loading ? (
              <Skeleton active paragraph={{ rows: 5 }} />
            ) : (
              <Table<TopDevice>
                columns={TOP_DEVICE_COLUMNS}
                dataSource={topDevices}
                rowKey="rank"
                pagination={false}
                size="small"
              />
            )}
          </Card>
        </Col>

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
                onClick={() => history.push('/admin/requests?status=PENDING')}
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
                dataSource={pendingRequests}
                rowKey="id"
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