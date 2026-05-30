import React, { useEffect, useMemo, useState } from 'react';
import { Row, Col, Card, Table, Select, Input, DatePicker, Button, Space, Tag, Skeleton, Empty, message, Statistic, Avatar, Tooltip } from 'antd';
import { listActivityLogs } from '@/services/activityLogs';
import { ActivityAction } from '@/constants/requestStatus';
import type { ActivityLogItem } from './mockData';
import './style.less';
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  LockOutlined,
  UnlockOutlined,
  MailOutlined,
  RollbackOutlined,
  AppstoreOutlined,
  PercentageOutlined,
} from '@ant-design/icons';

const { RangePicker } = DatePicker;
const { Search } = Input;

const ACTIONS = Object.values(ActivityAction) as string[];

const mapActionToIcon = (action: string) => {
  switch (action) {
    case ActivityAction.APPROVE_REQUEST:
      return <CheckCircleOutlined style={{ color: '#52c41a' }} />;
    case ActivityAction.REJECT_REQUEST:
      return <CloseCircleOutlined style={{ color: '#ff4d4f' }} />;
    case ActivityAction.LOCK_USER:
      return <LockOutlined style={{ color: '#fa8c16' }} />;
    case ActivityAction.UNLOCK_USER:
      return <UnlockOutlined style={{ color: '#1890ff' }} />;
    case ActivityAction.MANUAL_SEND_EMAIL:
      return <MailOutlined style={{ color: '#722ed1' }} />;
    case ActivityAction.RETURN_REQUEST:
      return <RollbackOutlined />;
    case ActivityAction.CREATE_DEVICE:
    case ActivityAction.UPDATE_DEVICE:
    case ActivityAction.UPDATE_STOCK:
      return <AppstoreOutlined />;
    case ActivityAction.ADJUST_PENALTY:
      return <PercentageOutlined />;
    default:
      return <AppstoreOutlined />;
  }
};

const AdminActivityLogsPage: React.FC = () => {
  const [items, setItems] = useState<ActivityLogItem[]>([]);
  const [allItems, setAllItems] = useState<ActivityLogItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [actionFilter, setActionFilter] = useState<string | undefined>(undefined);
  const [actorFilter, setActorFilter] = useState('');
  const [objectTypeFilter, setObjectTypeFilter] = useState<string | undefined>(undefined);
  const [dateRange, setDateRange] = useState<any>(null);
  const [q, setQ] = useState('');

  const loadFiltered = async () => {
    setLoading(true);
    try {
      const filters: any = {};
      if (actionFilter) filters.action = actionFilter;
      if (actorFilter) filters.actor = actorFilter;
      if (objectTypeFilter) filters.objectType = objectTypeFilter;
      if (dateRange && dateRange[0]) {
        const from = dateRange[0]?.toDate ? dateRange[0].toDate().toISOString() : dateRange[0]?.toISOString?.();
        const to = dateRange[1]?.toDate ? dateRange[1].toDate().toISOString() : dateRange[1]?.toISOString?.();
        filters.dateFrom = from;
        filters.dateTo = to;
      }
      if (q) filters.q = q;
      const data = await listActivityLogs(filters);
      setItems(data);
    } catch (err: any) {
      message.error(err?.message || 'Không thể tải activity logs');
    } finally {
      setLoading(false);
    }
  };

  const loadAll = async () => {
    try {
      const all = await listActivityLogs();
      setAllItems(all);
    } catch (e) {
      // ignore
    }
  };

  useEffect(() => { loadAll(); }, []);
  useEffect(() => { loadFiltered(); }, [actionFilter, actorFilter, objectTypeFilter, dateRange, q]);

  const total = allItems.length;
  const todayCount = allItems.filter((r) => new Date(r.timestamp).toDateString() === new Date().toDateString()).length;
  const uniqueActors = new Set(allItems.map((r) => r.actor)).size;

  const columns = [
    {
      title: 'Người thực hiện',
      dataIndex: 'actor',
      key: 'actor',
      render: (actor: string) => (
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <Avatar style={{ background: '#7265e6', marginRight: 8 }}>{actor?.charAt(0)}</Avatar>
          <div style={{ fontWeight: 600 }}>{actor}</div>
        </div>
      ),
    },
    {
      title: 'Action',
      dataIndex: 'action',
      key: 'action',
      render: (a: string) => (
        <Tooltip title={a}>
          <Tag icon={mapActionToIcon(a)}>{a}</Tag>
        </Tooltip>
      ),
    },
    {
      title: 'Đối tượng',
      key: 'object',
      render: (_: any, rec: ActivityLogItem) => (
        <div>
          <div style={{ fontWeight: 600 }}>{rec.objectName || rec.objectType || '-'}</div>
          <div style={{ color: '#888' }}>{rec.objectType || '-'}{rec.objectId ? ` • ${rec.objectId}` : ''}</div>
        </div>
      ),
    },
    { title: 'Chi tiết', dataIndex: 'details', key: 'details', render: (d: string) => <div style={{ maxWidth: 360 }}>{d || '-'}</div> },
    { title: 'Thời gian', dataIndex: 'timestamp', key: 'timestamp', render: (t: string) => <div style={{ color: '#888' }}>{new Date(t).toLocaleString()}</div> },
  ];

  return (
    <div className="admin-activity-logs-page">
      <Row justify="space-between" align="middle" style={{ marginBottom: 12 }}>
        <Col>
          <h3 style={{ margin: 0 }}>Activity Log</h3>
          <div style={{ color: '#888' }}>Bảng ghi lại hành động hệ thống và người dùng</div>
        </Col>
        <Col>
          <Space>
            <Button onClick={() => { loadFiltered(); loadAll(); }}>Làm mới</Button>
          </Space>
        </Col>
      </Row>

      <Row gutter={12} className="activity-stats" style={{ marginBottom: 12 }}>
        <Col xs={24} sm={8} md={8} lg={6}>
          <Card className="stat-card">
            <Statistic title="Tổng log" value={total} />
            <div style={{ color: '#888', marginTop: 6 }}>Bản ghi tổng</div>
          </Card>
        </Col>
        <Col xs={24} sm={8} md={8} lg={6}>
          <Card className="stat-card">
            <Statistic title="Hôm nay" value={todayCount} />
            <div style={{ color: '#888', marginTop: 6 }}>Bản ghi hôm nay</div>
          </Card>
        </Col>
        <Col xs={24} sm={8} md={8} lg={6}>
          <Card className="stat-card">
            <Statistic title="Người thực hiện" value={uniqueActors} />
            <div style={{ color: '#888', marginTop: 6 }}>Số người thực hiện</div>
          </Card>
        </Col>
      </Row>

      <Card style={{ marginBottom: 12 }} bodyStyle={{ padding: 12 }}>
        <Row gutter={12} align="middle">
          <Col xs={24} sm={8} md={6}>
            <Select allowClear placeholder="Action" style={{ width: '100%' }} value={actionFilter} onChange={(v) => setActionFilter(v)}>
              {ACTIONS.map((act) => <Select.Option key={act} value={act}>{act}</Select.Option>)}
            </Select>
          </Col>

          <Col xs={24} sm={8} md={6}>
            <Input placeholder="Người thực hiện" value={actorFilter} onChange={(e) => setActorFilter(e.target.value)} />
          </Col>

          <Col xs={24} sm={8} md={6}>
            <Select allowClear placeholder="Đối tượng" style={{ width: '100%' }} value={objectTypeFilter} onChange={(v) => setObjectTypeFilter(v)}>
              <Select.Option value="Device">Device</Select.Option>
              <Select.Option value="User">User</Select.Option>
              <Select.Option value="Request">Request</Select.Option>
              <Select.Option value="Notification">Notification</Select.Option>
            </Select>
          </Col>

          <Col xs={24} sm={12} md={6}>
            <RangePicker onChange={(r) => setDateRange(r)} />
          </Col>

          <Col xs={24} sm={12} md={6}>
            <Search placeholder="Tìm kiếm chi tiết, tên đối tượng hoặc người" onSearch={(val) => setQ(val)} value={q} allowClear />
          </Col>
        </Row>
      </Card>

      <Card>
        {loading && items.length === 0 ? (
          <Skeleton active paragraph={{ rows: 6 }} />
        ) : (
          <Table
            columns={columns}
            dataSource={items}
            rowKey="id"
            pagination={{ pageSize: 10 }}
            locale={{ emptyText: <Empty description="Không có bản ghi" /> }}
          />
        )}
      </Card>
    </div>
  );
};

export default AdminActivityLogsPage;
