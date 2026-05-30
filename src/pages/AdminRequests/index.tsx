// ============================================================
// pages/admin/requests/index.tsx
// Danh sách Yêu cầu (Admin)
// ============================================================

import React, { useEffect, useState } from 'react';
import { Table, Card, Space, Button, Tooltip, Tag, Input, Select, message, Modal, Form, Row, Col, Badge, Divider, List } from 'antd';
import { FileSearchOutlined, CheckOutlined, CloseOutlined, ReloadOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { history } from 'umi';

import { listRequests, approveRequest, rejectRequest, returnRequest } from '@/services/requests';
import { REQUEST_STATUS_LABEL, REQUEST_STATUS_COLOR, type RequestItem, type RequestStatus } from './mockData';

const { Search } = Input;
const { Option } = Select;

const RequestsPage: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [requests, setRequests] = useState<RequestItem[]>([]);
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  // Reject modal
  const [rejecting, setRejecting] = useState(false);
  const [rejectVisible, setRejectVisible] = useState(false);
  const [selectedRequestId, setSelectedRequestId] = useState<string | null>(null);
  const [form] = Form.useForm();

  const load = async () => {
    setLoading(true);
    try {
      const data = await listRequests();
      setRequests(data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleApprove = async (id: string) => {
    setLoading(true);
    try {
      await approveRequest(id);
      message.success('Đã phê duyệt yêu cầu');
      await load();
    } catch (err: any) {
      message.error(err?.message || 'Phê duyệt thất bại');
    } finally {
      setLoading(false);
    }
  };

  const openRejectModal = (id: string) => {
    setSelectedRequestId(id);
    form.resetFields();
    setRejectVisible(true);
  };

  const handleReject = async () => {
    try {
      const values = await form.validateFields();
      setRejecting(true);
      if (!selectedRequestId) throw new Error('No request selected');
      await rejectRequest(selectedRequestId, values.reason);
      message.success('Đã từ chối yêu cầu');
      setRejectVisible(false);
      await load();
    } catch (err: any) {
      message.error(err?.message || 'Từ chối thất bại');
    } finally {
      setRejecting(false);
    }
  };

  const handleReturn = async (id: string) => {
    setLoading(true);
    try {
      await returnRequest(id);
      message.success('Xác nhận trả thiết bị thành công');
      await load();
    } catch (err: any) {
      message.error(err?.message || 'Xử lý trả thất bại');
    } finally {
      setLoading(false);
    }
  };

  const filtered = requests
    .filter((r) => (statusFilter === 'ALL' ? true : r.status === statusFilter))
    .filter((r) => (searchText ? r.studentName.toLowerCase().includes(searchText.toLowerCase()) : true));

  const columns: ColumnsType<RequestItem> = [
    { title: 'Mã YC', dataIndex: 'id', width: 110, render: (v) => <code>{v}</code> },
    { title: 'Sinh viên', dataIndex: 'studentName' },
    { title: 'Thiết bị', dataIndex: 'equipmentName', ellipsis: true },
    { title: 'Ngày mượn', dataIndex: 'borrowDate', width: 120 },
    { title: 'Ngày trả', dataIndex: 'returnDate', width: 120 },
    { title: 'Ngày gửi', dataIndex: 'submittedAt', width: 160, render: (v) => <span style={{ color: '#888' }}>{v}</span> },
    { title: 'Trạng thái', dataIndex: 'status', width: 120, render: (s: RequestStatus) => <Tag color={REQUEST_STATUS_COLOR[s]}>{REQUEST_STATUS_LABEL[s]}</Tag> },
    {
      title: 'Thao tác',
      key: 'actions',
      width: 220,
      align: 'center',
      render: (_: any, record: RequestItem) => (
        <Space>
          <Tooltip title="Xem chi tiết">
            <Button type="text" shape="circle" icon={<FileSearchOutlined />} onClick={() => history.push(`/admin/requests/${record.id}`)} />
          </Tooltip>

          {record.status === 'PENDING' && (
            <Tooltip title="Duyệt nhanh">
              <Button type="primary" size="small" shape="round" icon={<CheckOutlined />} onClick={() => handleApprove(record.id)}>
                Duyệt
              </Button>
            </Tooltip>
          )}

          {record.status === 'PENDING' && (
            <Tooltip title="Từ chối">
              <Button danger size="small" shape="round" icon={<CloseOutlined />} onClick={() => openRejectModal(record.id)}>
                Từ chối
              </Button>
            </Tooltip>
          )}

          {(record.status === 'APPROVED' || record.status === 'OVERDUE') && (
            <Tooltip title="Xác nhận trả">
              <Button size="small" onClick={() => handleReturn(record.id)}>
                Xác nhận trả
              </Button>
            </Tooltip>
          )}
        </Space>
      ),
    },
  ];

  const total = requests.length;
  const pendingCount = requests.filter((r) => r.status === 'PENDING').length;
  // const approvedCount = requests.filter((r) => r.status === 'APPROVED').length;
  const overdueCount = requests.filter((r) => r.status === 'OVERDUE').length;

  return (
    <Space direction="vertical" size={20} style={{ display: 'flex' }}>
      <Row justify="space-between" align="middle">
        <Col>
          <div>
            <h3 style={{ margin: 0 }}>Quản lý yêu cầu</h3>
            <div style={{ marginTop: 6 }}>
              <span style={{ color: '#888', fontSize: 13 }}>{total} yêu cầu</span>
              {overdueCount > 0 && (
                <Badge count={`${overdueCount} quá hạn`} style={{ backgroundColor: '#fa8c16', marginLeft: 12 }} />
              )}
              {pendingCount > 0 && (
                <Badge count={`${pendingCount} chờ`} style={{ backgroundColor: '#1890ff', marginLeft: 8 }} />
              )}
            </div>
          </div>
        </Col>

        <Col>
          <Space>
            <Button icon={<ReloadOutlined />} onClick={load}>Làm mới</Button>
          </Space>
        </Col>
      </Row>

      <Card bordered={false} style={{ borderRadius: 8 }} bodyStyle={{ padding: '12px 16px' }}>
        <Row gutter={[12, 12]} align="middle">
          <Col xs={24} sm={14} md={16}>
            <Search
              placeholder="Tìm theo tên sinh viên..."
              allowClear
              onChange={(e) => setSearchText(e.target.value)}
              value={searchText}
            />
          </Col>
          <Col xs={24} sm={10} md={8}>
            <Select value={statusFilter} onChange={(v) => setStatusFilter(v)} style={{ width: '100%' }}>
              <Option value="ALL">Tất cả trạng thái</Option>
              <Option value="PENDING">Đang chờ</Option>
              <Option value="APPROVED">Đã duyệt</Option>
              <Option value="REJECTED">Từ chối</Option>
              <Option value="RETURNED">Đã trả</Option>
              <Option value="OVERDUE">Quá hạn</Option>
            </Select>
          </Col>
        </Row>
      </Card>

      <Card bordered={false} style={{ borderRadius: 8 }}>
        <Table<RequestItem>
          columns={columns}
          dataSource={filtered}
          rowKey="id"
          loading={loading}
          size="middle"
          scroll={{ x: 900 }}
          pagination={{ pageSize: 8, showSizeChanger: true, pageSizeOptions: ['8', '16', '32'], showTotal: (total) => `${total} yêu cầu` }}
          locale={{ emptyText: (
            <div style={{ textAlign: 'center' }}>
              <div style={{ marginBottom: 8 }}>Không có yêu cầu phù hợp</div>
            </div>
          ) }}
          expandedRowRender={(record) => (
            <div style={{ padding: '12px 24px' }}>
              <div style={{ marginBottom: 8 }}><strong>Ghi chú:</strong> <span style={{ color: '#555' }}>{record.note || '—'}</span></div>
              <Divider style={{ margin: '8px 0' }} />
              <List
                size="small"
                dataSource={record.history}
                renderItem={(h) => (
                  <List.Item>
                    <List.Item.Meta
                      title={<span>{h.status} — <span style={{ color: '#888' }}>{h.by}</span></span>}
                      description={<div><span style={{ color: '#888' }}>{new Date(h.timestamp).toLocaleString()}</span><div>{h.note}</div></div>}
                    />
                  </List.Item>
                )}
              />
            </div>
          )}
          onRow={(record) => ({
            style: record.status === 'OVERDUE' ? { background: '#fff7e6' } : undefined,
          })}
        />
      </Card>

      <Modal
        title="Lý do từ chối"
        visible={rejectVisible}
        onCancel={() => setRejectVisible(false)}
        onOk={handleReject}
        confirmLoading={rejecting}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="reason" rules={[{ required: true, message: 'Vui lòng nhập lý do' }]}>
            <Input.TextArea rows={4} />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
};

export default RequestsPage;
