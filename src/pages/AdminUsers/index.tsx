import React, { useEffect, useState } from 'react';
import { Row, Col, Card, Input, Select, Button, Space, Avatar, Tag, message, Modal, Form, InputNumber, Tabs, List, Divider, Skeleton, Typography, Progress } from 'antd';
import { LockOutlined, UnlockOutlined, EditOutlined, ReloadOutlined, UserOutlined, TeamOutlined, SafetyOutlined, HistoryOutlined } from '@ant-design/icons';
import './style.less';

import { listUsers, lockUser, unlockUser, adjustPenalty, getUserById } from '@/services/users';
import { USER_STATUS_LABEL, USER_STATUS_COLOR, type UserItem } from './mockData';

const { Search } = Input;
const { Option } = Select;
const { TabPane } = Tabs;

const AdminUsersPage: React.FC = () => {
  const [users, setUsers] = useState<UserItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  const [selectedUserId, setSelectedUserId] = useState<string | null>(null);
  const [selectedUser, setSelectedUser] = useState<UserItem | null>(null);

  // modals
  const [lockVisible, setLockVisible] = useState(false);
  const [unlockVisible, setUnlockVisible] = useState(false);
  const [adjustVisible, setAdjustVisible] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);

  const [reasonForm] = Form.useForm();
  const [adjustForm] = Form.useForm();

  const loadUsers = async () => {
    setLoading(true);
    try {
      const data = await listUsers();
      setUsers(data);
    } catch (err: any) {
      message.error(err?.message || 'Không thể tải danh sách');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, []);

  useEffect(() => {
    if (selectedUserId) loadSelected(selectedUserId);
    else setSelectedUser(null);
  }, [selectedUserId]);

  const loadSelected = async (id: string) => {
    try {
      const u = await getUserById(id);
      setSelectedUser(u);
    } catch (err: any) {
      message.error(err?.message || 'Không thể tải người dùng');
    }
  };

  const filtered = users
    .filter((u) => (statusFilter === 'ALL' ? true : u.status === statusFilter))
    .filter((u) => (searchText ? (u.fullName || '').toLowerCase().includes(searchText.toLowerCase()) || (u.studentId || '').toLowerCase().includes(searchText.toLowerCase()) : true));

  const totalUsers = users.length;
  const lockedCount = users.filter((u) => u.status === 'LOCKED').length;
  const totalPenaltyPoints = users.reduce((s, u) => s + (u.penaltyPoints || 0), 0);

  const getPenaltyPercent = (points?: number) => Math.min(100, Math.round(((points || 0) / 15) * 100));

  const getInitial = (name?: string) => (name && name.length ? name.trim().slice(0, 1).toUpperCase() : 'U');

  // table columns removed (using card grid layout instead)

  const handleLock = async () => {
    try {
      const values = await reasonForm.validateFields();
      if (!selectedUserId) throw new Error('Chưa chọn người dùng');
      setActionLoading(true);
      await lockUser(selectedUserId, values.reason, 'Admin');
      message.success('Đã khóa tài khoản');
      setLockVisible(false);
      await loadUsers();
      await loadSelected(selectedUserId);
    } catch (err: any) {
      message.error(err?.message || 'Khóa thất bại');
    } finally {
      setActionLoading(false);
    }
  };

  const handleUnlock = async () => {
    try {
      const values = await reasonForm.validateFields();
      if (!selectedUserId) throw new Error('Chưa chọn người dùng');
      setActionLoading(true);
      await unlockUser(selectedUserId, values.reason, 'Admin');
      message.success('Đã mở khóa tài khoản');
      setUnlockVisible(false);
      await loadUsers();
      await loadSelected(selectedUserId);
    } catch (err: any) {
      message.error(err?.message || 'Mở khóa thất bại');
    } finally {
      setActionLoading(false);
    }
  };

  const handleAdjust = async () => {
    try {
      const values = await adjustForm.validateFields();
      if (!selectedUserId) throw new Error('Chưa chọn người dùng');
      setActionLoading(true);
      await adjustPenalty(selectedUserId, Number(values.points), values.reason, 'Admin');
      message.success('Đã điều chỉnh điểm phạt');
      setAdjustVisible(false);
      await loadUsers();
      await loadSelected(selectedUserId);
    } catch (err: any) {
      message.error(err?.message || 'Điều chỉnh thất bại');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div>
      <div className="admin-users-hero">
        <Row justify="space-between" align="middle" style={{ marginBottom: 8 }} className="admin-users-header">
          <Col>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <Avatar size={56} className="admin-users-hero-avatar"><UserOutlined /></Avatar>
              <div>
                <Typography.Title level={4} style={{ margin: 0 }}>Quản lý người dùng</Typography.Title>
                <div className="admin-users-subtitle">Kiểm soát tài khoản • Điểm phạt • Lịch sử</div>
              </div>
            </div>
          </Col>
          <Col>
            <Button icon={<ReloadOutlined />} onClick={loadUsers} shape="round" ghost className="refresh-btn">Làm mới</Button>
          </Col>
        </Row>

        <Row gutter={[16, 16]} style={{ marginBottom: 12 }} className="admin-users-stats">
        <Col xs={24} sm={12} md={6} lg={6}>
          <Card bordered={false} className="admin-users-stat-card">
            {loading ? (
              <Skeleton active paragraph={{ rows: 1 }} />
            ) : (
              <>
                <div className="stat-content">
                  <Avatar size={44} className="stat-icon"><TeamOutlined /></Avatar>
                  <div className="stat-body">
                    <div className="stat-title">Tổng người dùng</div>
                    <div className="stat-value">{totalUsers}</div>
                    <div className="stat-sub">Tất cả tài khoản hệ thống</div>
                  </div>
                </div>
                <div className="stat-bar"><div className="stat-bar-inner" style={{ width: '36%', background: '#1890ff' }} /></div>
              </>
            )}
          </Card>
        </Col>

        <Col xs={24} sm={12} md={6} lg={6}>
          <Card bordered={false} className="admin-users-stat-card">
            {loading ? (
              <Skeleton active paragraph={{ rows: 1 }} />
            ) : (
              <>
                <div className="stat-content">
                  <Avatar size={44} className="stat-icon stat-icon-warning"><LockOutlined /></Avatar>
                  <div className="stat-body">
                    <div className="stat-title">Đang bị khóa</div>
                    <div className="stat-value">{lockedCount}</div>
                    <div className="stat-sub">Tài khoản bị khóa</div>
                  </div>
                </div>
                <div className="stat-bar"><div className="stat-bar-inner" style={{ width: '18%', background: '#fa8c16' }} /></div>
              </>
            )}
          </Card>
        </Col>

        <Col xs={24} sm={12} md={6} lg={6}>
          <Card bordered={false} className="admin-users-stat-card">
            {loading ? (
              <Skeleton active paragraph={{ rows: 1 }} />
            ) : (
              <>
                <div className="stat-content">
                  <Avatar size={44} className="stat-icon stat-icon-info"><SafetyOutlined /></Avatar>
                  <div className="stat-body">
                    <div className="stat-title">Tổng điểm phạt</div>
                    <div className="stat-value">{totalPenaltyPoints}</div>
                    <div className="stat-sub">Tổng điểm phạt tích luỹ</div>
                  </div>
                </div>
                <div className="stat-bar"><div className="stat-bar-inner" style={{ width: '28%', background: '#1890ff' }} /></div>
              </>
            )}
          </Card>
        </Col>

        <Col xs={24} sm={12} md={6} lg={6}>
          <Card bordered={false} className="admin-users-stat-card">
            {loading ? (
              <Skeleton active paragraph={{ rows: 1 }} />
            ) : (
              <>
                <div className="stat-content">
                  <Avatar size={44} className="stat-icon stat-icon-success"><HistoryOutlined /></Avatar>
                  <div className="stat-body">
                    <div className="stat-title">Sinh viên có lịch sử</div>
                    <div className="stat-value">{users.filter((u) => u.borrowHistory.length > 0).length}</div>
                    <div className="stat-sub">Có mượn hoặc bị phạt</div>
                  </div>
                </div>
                <div className="stat-bar"><div className="stat-bar-inner" style={{ width: '22%', background: '#52c41a' }} /></div>
              </>
            )}
          </Card>
        </Col>
        </Row>
      </div>

      <Row gutter={16}>
        <Col xs={24} md={4}>
          <Card className="admin-users-side-card" bodyStyle={{ padding: 16 }}>
            <div style={{ marginBottom: 12 }}>
              <h4 style={{ margin: 0 }}>Bộ lọc</h4>
            </div>
            <div style={{ marginBottom: 12 }}>
              <Search placeholder="Tìm tên hoặc mã SV" onChange={(e) => setSearchText(e.target.value)} allowClear />
            </div>
            <div style={{ marginBottom: 12 }}>
              <Select value={statusFilter} onChange={(v) => setStatusFilter(v)} style={{ width: '100%' }}>
                <Option value="ALL">Tất cả trạng thái</Option>
                <Option value="ACTIVE">Hoạt động</Option>
                <Option value="LOCKED">Bị khóa</Option>
              </Select>
            </div>

            <Divider />
            <div>
              <div style={{ fontSize: 13, color: '#888' }}>Tổng</div>
              <div style={{ fontSize: 20, fontWeight: 700 }}>{users.length}</div>
            </div>
            
          </Card>
        </Col>

        <Col xs={24} md={14}>
          <Card className="admin-users-main-card" bodyStyle={{ padding: 12 }}>
            {loading ? (
              <Skeleton active />
            ) : (
              <>
                <div className="main-list-header">
                  <h3 style={{ margin: 0 }}>Danh sách sinh viên</h3>
                </div>
                <div className="admin-users-grid">
                  <Row gutter={[12, 12]}>
                    {filtered.map((u) => (
                      <Col xs={24} sm={12} md={12} lg={12} key={u.id}>
                        <Card hoverable className="admin-user-card" onClick={() => setSelectedUserId(u.id)}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                              {u.avatarUrl ? (
                                <Avatar size={48} src={u.avatarUrl} />
                              ) : (
                                <Avatar size={48} className="admin-users-avatar-gradient">{getInitial(u.fullName)}</Avatar>
                              )}
                              <div>
                                <div style={{ fontWeight: 700, fontSize: 14 }}>{u.fullName}</div>
                                <div style={{ color: '#888', fontSize: 12 }}>{u.studentId}</div>
                                <div style={{ color: '#888', fontSize: 12 }}>{u.email}</div>
                              </div>
                            </div>

                            <div style={{ textAlign: 'right' }}>
                              <Tag color={USER_STATUS_COLOR[u.status]} style={{ fontWeight: 700 }}>{USER_STATUS_LABEL[u.status]}</Tag>
                              <div style={{ marginTop: 8 }}>
                                <Progress type="circle" percent={getPenaltyPercent(u.penaltyPoints)} width={48} strokeColor={(u.penaltyPoints || 0) > 0 ? '#fa8c16' : '#52c41a'} />
                              </div>
                            </div>
                          </div>

                          <div style={{ marginTop: 12, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <div>
                              <div style={{ color: '#888', fontSize: 12 }}>Mượn: {u.borrowHistory.length} • Phạt: {u.penaltyHistory.length}</div>
                            </div>
                            <Space>
                              <Button size="small" shape="round" ghost onClick={(e) => { e.stopPropagation(); setSelectedUserId(u.id); }}>Xem</Button>
                              {u.status === 'ACTIVE' ? (
                                <Button danger size="small" shape="round" onClick={(e) => { e.stopPropagation(); setSelectedUserId(u.id); reasonForm.resetFields(); setLockVisible(true); }}>Khóa</Button>
                              ) : (
                                <Button danger size="small" shape="round" onClick={(e) => { e.stopPropagation(); setSelectedUserId(u.id); reasonForm.resetFields(); setUnlockVisible(true); }}>Mở</Button>
                              )}
                            </Space>
                          </div>
                        </Card>
                      </Col>
                    ))}
                  </Row>
                </div>
              </>
            )}
          </Card>
        </Col>

        <Col xs={24} md={6}>
          <Card className="admin-users-side-card" bodyStyle={{ padding: 12 }}>
            {selectedUser ? (
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                    <Avatar src={selectedUser.avatarUrl} size={64} className={!selectedUser.avatarUrl ? 'admin-users-avatar-gradient' : ''}>{getInitial(selectedUser.fullName)}</Avatar>
                    <div>
                      <h3 style={{ margin: 0 }}>{selectedUser.fullName}</h3>
                      <div style={{ color: '#888' }}>{selectedUser.studentId} • {selectedUser.email}</div>
                    </div>
                  </div>
                  <div>
                    <Tag color={USER_STATUS_COLOR[selectedUser.status]} style={{ fontWeight: 700 }}>{USER_STATUS_LABEL[selectedUser.status]}</Tag>
                  </div>
                </div>

                  <div style={{ marginTop: 12, marginBottom: 12 }}>
                  <Space>
                    {selectedUser.status === 'ACTIVE' ? (
                      <Button danger size="small" icon={<LockOutlined />} onClick={() => { reasonForm.resetFields(); setLockVisible(true); }}>Khóa tài khoản</Button>
                    ) : (
                      <Button type="primary" size="small" icon={<UnlockOutlined />} onClick={() => { reasonForm.resetFields(); setUnlockVisible(true); }}>Mở khóa</Button>
                    )}

                    <Button size="small" icon={<EditOutlined />} onClick={() => { adjustForm.resetFields(); setAdjustVisible(true); }}>Điều chỉnh điểm</Button>
                  </Space>
                </div>

                <div style={{ marginBottom: 12 }}>
                  <div style={{ fontSize: 12, color: '#888' }}>Mức điểm phạt</div>
                  <Progress percent={getPenaltyPercent(selectedUser.penaltyPoints)} strokeColor={(selectedUser.penaltyPoints || 0) > 0 ? '#fa8c16' : '#52c41a'} />
                </div>

                <Tabs defaultActiveKey="1">
                  <TabPane tab={`Lịch sử mượn (${selectedUser.borrowHistory.length})`} key="1">
                    <List
                      dataSource={selectedUser.borrowHistory}
                      renderItem={(b) => (
                        <List.Item>
                          <List.Item.Meta title={b.equipmentName} description={<div>{new Date(b.borrowDate).toLocaleString()} {b.returnDate ? `→ ${new Date(b.returnDate).toLocaleString()}` : '(chưa trả)'}</div>} />
                        </List.Item>
                      )}
                    />
                  </TabPane>
                  <TabPane tab={`Lịch sử điểm/phạt (${selectedUser.penaltyHistory.length})`} key="2">
                    <List
                      dataSource={selectedUser.penaltyHistory}
                      renderItem={(p) => (
                        <List.Item>
                          <List.Item.Meta
                            title={<div style={{ display: 'flex', justifyContent: 'space-between' }}><div>{p.type}</div><div style={{ color: '#888' }}>{new Date(p.timestamp).toLocaleString()}</div></div>}
                            description={<div>{p.pointsChange ? (<strong>{p.pointsChange > 0 ? `+${p.pointsChange}` : p.pointsChange}</strong>) : null} <div>{p.reason}</div><div style={{ color: '#888' }}>by {p.by}</div></div>}
                          />
                        </List.Item>
                      )}
                    />
                  </TabPane>
                </Tabs>
              </div>
            ) : (
              <div className="admin-users-empty-cta">
                <div className="empty-illustration">
                  <div className="empty-circle"><UserOutlined style={{ fontSize: 36, color: '#6b5cf6' }} /></div>
                </div>
                <h4>Chọn một sinh viên</h4>
                <div className="empty-sub">để xem chi tiết thông tin</div>
                <div style={{ marginTop: 16 }}>
                  <Button type="primary" shape="round" className="admin-cta-btn" onClick={() => { if (filtered.length) setSelectedUserId(filtered[0].id); else message.info('Không có sinh viên'); }}>Chọn sinh viên</Button>
                </div>
              </div>
            )}
          </Card>
        </Col>
      </Row>

      <div style={{ textAlign: 'center', marginTop: 12, color: '#888' }}>
        <small>⚡ Dữ liệu được cập nhật theo thời gian thực</small>
      </div>

      <Modal title="Lý do khóa" visible={lockVisible} onCancel={() => setLockVisible(false)} onOk={handleLock} confirmLoading={actionLoading}>
        <Form form={reasonForm} layout="vertical">
          <Form.Item name="reason" rules={[{ required: true, message: 'Vui lòng nhập lý do' }]}>
            <Input.TextArea rows={4} placeholder="Lý do khóa (ví dụ: Nợ phạt, vi phạm quy định)" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="Lý do mở khóa" visible={unlockVisible} onCancel={() => setUnlockVisible(false)} onOk={handleUnlock} confirmLoading={actionLoading}>
        <Form form={reasonForm} layout="vertical">
          <Form.Item name="reason" rules={[{ required: true, message: 'Vui lòng nhập lý do' }]}>
            <Input.TextArea rows={3} placeholder="Lý do mở khóa (ghi chú)" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="Điều chỉnh điểm phạt" visible={adjustVisible} onCancel={() => setAdjustVisible(false)} onOk={handleAdjust} confirmLoading={actionLoading}>
        <Form form={adjustForm} layout="vertical">
          <Form.Item name="points" rules={[{ required: true, message: 'Nhập giá trị điểm (dương để cộng, âm để trừ)' }]}>
            <InputNumber style={{ width: '100%' }} placeholder="Số điểm (vd: 2 hoặc -1)" />
          </Form.Item>
          <Form.Item name="reason" rules={[{ required: true, message: 'Vui lòng nhập lý do' }]}>
            <Input.TextArea rows={3} placeholder="Lý do điều chỉnh" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AdminUsersPage;
