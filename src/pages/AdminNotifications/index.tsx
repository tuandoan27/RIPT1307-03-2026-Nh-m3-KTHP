import React, { useEffect, useMemo, useState } from 'react';
import { Row, Col, Card, Table, Select, Input, Button, Space, Tag, message, Modal, Typography, Statistic, Progress, Avatar, Tooltip, Timeline } from 'antd';
import { ReloadOutlined, MailOutlined, CheckCircleOutlined, ExclamationCircleOutlined, InfoCircleOutlined } from '@ant-design/icons';
import './style.less';

import { listNotifications, resendNotification } from '@/services/notifications';
import { MOCK_NOTIFICATIONS } from './mockData';
import type { NotificationItem, NotificationStatus } from './mockData';

const { Option } = Select;
const { Search } = Input;
const { Text } = Typography;

const StatusTag: React.FC<{ status: NotificationStatus }> = ({ status }) => {
  const color = status === 'SUCCESS' ? 'green' : status === 'FAILED' ? 'red' : 'default';
  const icon = status === 'SUCCESS' ? <CheckCircleOutlined /> : status === 'FAILED' ? <ExclamationCircleOutlined /> : <InfoCircleOutlined />;
  return (
    <Tag icon={icon} color={color === 'default' ? undefined : undefined}>
      {status}
    </Tag>
  );
};

const AdminNotificationsPage: React.FC = () => {
  const [items, setItems] = useState<NotificationItem[]>([]);
  const [allItems, setAllItems] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [typeFilter, setTypeFilter] = useState<string | undefined>(undefined);
  const [statusFilter, setStatusFilter] = useState<NotificationStatus | undefined>(undefined);
  const [q, setQ] = useState('');

  const availableTypes = useMemo(() => Array.from(new Set(MOCK_NOTIFICATIONS.map((m) => m.type))), []);

  const loadFiltered = async () => {
    setLoading(true);
    try {
      const data = await listNotifications({ type: typeFilter, status: statusFilter, q: q || undefined });
      setItems(data);
    } catch (err: any) {
      message.error(err?.message || 'Không thể tải dữ liệu');
    } finally {
      setLoading(false);
    }
  };

  const loadAll = async () => {
    try {
      const all = await listNotifications();
      setAllItems(all);
    } catch (err: any) {
      // ignore
    }
  };

  useEffect(() => { loadAll(); }, []);
  useEffect(() => { loadFiltered(); }, [typeFilter, statusFilter, q]);

  const handleResend = (rec: NotificationItem) => {
    Modal.confirm({
      title: 'Gửi lại email',
      content: `Gửi lại email tới ${rec.recipientEmail}? Hành động sẽ được ghi vào activity log.`,
      onOk: async () => {
        try {
          setLoading(true);
          await resendNotification(rec.id, 'Admin');
          message.success('Đã gửi lại email và ghi log MANUAL_SEND_EMAIL');
          await Promise.all([loadFiltered(), loadAll()]);
        } catch (err: any) {
          message.error(err?.message || 'Gửi lại thất bại');
        } finally {
          setLoading(false);
        }
      }
    });
  };

  // stats
  const total = allItems.length;
  const failedCount = allItems.filter((n) => n.status === 'FAILED').length;
  const successRate = total === 0 ? 0 : Math.round(((total - failedCount) / total) * 100);
  const lastSent = allItems.length ? new Date(allItems[0].sentAt).toLocaleString() : '-';

  const columns = [
    {
      title: 'Người nhận',
      dataIndex: 'recipientEmail',
      key: 'recipient',
      render: (_: any, rec: NotificationItem) => (
        <div className="notif-recipient">
          <Avatar style={{ backgroundColor: '#2f54eb', marginRight: 8 }}>{(rec.recipientName || rec.recipientEmail || '').charAt(0)}</Avatar>
          <div>
            <div style={{ fontWeight: 700 }}>{rec.recipientName || rec.recipientEmail}</div>
            <div style={{ color: '#888' }}>{rec.recipientEmail}</div>
          </div>
        </div>
      ),
    },
    { title: 'Loại', dataIndex: 'type', key: 'type', render: (t: string) => <Tag>{t}</Tag> },
    { title: 'Tiêu đề', dataIndex: 'subject', key: 'subject', render: (s: string) => <Text ellipsis style={{ maxWidth: 300 }}>{s}</Text> },
    { title: 'Thời gian', dataIndex: 'sentAt', key: 'sentAt', render: (v: string) => <div style={{ color: '#888' }}>{new Date(v).toLocaleString()}</div> },
    { title: 'Trạng thái', dataIndex: 'status', key: 'status', render: (s: NotificationStatus) => <StatusTag status={s} /> },
    {
      title: 'Hành động',
      key: 'actions',
      width: 180,
      render: (_: any, rec: NotificationItem) => (
        <Space>
          <Tooltip title="Làm mới danh sách"><Button size="small" onClick={() => loadFiltered()} icon={<ReloadOutlined />} /></Tooltip>
          {rec.status === 'FAILED' && (
            <Button type="primary" danger size="small" icon={<MailOutlined />} onClick={() => handleResend(rec)}>Gửi lại</Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div className="admin-notifications-page">
      <Row justify="space-between" align="middle" style={{ marginBottom: 12 }}>
        <Col>
          <h3 style={{ margin: 0 }}>Log thông báo (Email)</h3>
          <div style={{ color: '#888' }}>Danh sách email đã gửi và trạng thái</div>
        </Col>
        <Col>
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => { loadFiltered(); loadAll(); }}>Làm mới</Button>
          </Space>
        </Col>
      </Row>

      <Row gutter={12} className="notification-stats" style={{ marginBottom: 12 }}>
        <Col xs={24} sm={12} md={8} lg={6}>
          <Card className="stat-card">
            <Space align="center">
              <Avatar size={48} style={{ background: '#1890ff' }} icon={<MailOutlined />} />
              <div>
                <Statistic title="Tổng thông báo" value={total} />
                <div style={{ color: '#888', fontSize: 12 }}>Lần gửi gần nhất: {lastSent}</div>
              </div>
            </Space>
          </Card>
        </Col>

        <Col xs={24} sm={12} md={8} lg={6}>
          <Card className="stat-card">
            <Space align="center">
              <Avatar size={48} style={{ background: '#ff4d4f' }} icon={<ExclamationCircleOutlined />} />
              <div>
                <Statistic title="Thất bại" value={failedCount} valueStyle={{ color: '#ff4d4f' }} />
                <div style={{ color: '#888', fontSize: 12 }}> cần gửi lại</div>
              </div>
            </Space>
          </Card>
        </Col>

        <Col xs={24} sm={24} md={8} lg={12}>
          <Card className="stat-card progress-card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <div style={{ fontWeight: 700 }}>Tỉ lệ thành công</div>
                <div style={{ color: '#888' }}>{successRate}%</div>
              </div>
              <div style={{ width: 220 }}>
                <Progress percent={successRate} status={successRate === 100 ? 'success' : 'normal'} />
              </div>
            </div>
          </Card>
        </Col>
      </Row>

      <Card style={{ marginBottom: 12 }} bodyStyle={{ padding: 12 }}>
        <Row gutter={12} align="middle">
          <Col xs={24} sm={8} md={6}>
            <Select allowClear placeholder="Loại" style={{ width: '100%' }} value={typeFilter} onChange={(v) => setTypeFilter(v)}>
              {availableTypes.map((t) => <Option key={t} value={t}>{t}</Option>)}
            </Select>
          </Col>
          <Col xs={24} sm={8} md={6}>
            <Select allowClear placeholder="Trạng thái" style={{ width: '100%' }} value={statusFilter} onChange={(v) => setStatusFilter(v as NotificationStatus)}>
              <Option value="SUCCESS">SUCCESS</Option>
              <Option value="FAILED">FAILED</Option>
              <Option value="PENDING">PENDING</Option>
            </Select>
          </Col>
          <Col xs={24} sm={8} md={8}>
            <Search placeholder="Tìm theo email hoặc tên" allowClear onSearch={(val) => setQ(val)} onChange={(e) => setQ(e.target.value)} value={q} />
          </Col>
        </Row>
      </Card>

      <Card>
        <Table
          columns={columns}
          dataSource={items}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 8 }}
          expandable={{
            expandedRowRender: (rec: NotificationItem) => (
              <div style={{ padding: 8 }}>
                <div style={{ marginBottom: 8 }}><strong>Subject:</strong> {rec.subject}</div>
                <div style={{ marginBottom: 8 }}><strong>Activity:</strong></div>
                <Timeline>
                  {rec.activity.map((a, idx) => (
                    <Timeline.Item key={idx} color={a.type === 'MANUAL_SEND_EMAIL' ? 'blue' : a.type === 'AUTO_SEND' ? 'gray' : 'gray'}>
                      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <div>
                          <div style={{ fontWeight: 700 }}>{a.type}</div>
                          <div style={{ color: '#444' }}>{a.by} {a.note ? `— ${a.note}` : ''}</div>
                        </div>
                        <div style={{ color: '#888' }}>{new Date(a.timestamp).toLocaleString()}</div>
                      </div>
                    </Timeline.Item>
                  ))}
                </Timeline>
              </div>
            ),
          }}
        />
      </Card>
    </div>
  );
};

export default AdminNotificationsPage;
