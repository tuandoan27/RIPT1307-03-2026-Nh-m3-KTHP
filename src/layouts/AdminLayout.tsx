import React, { useState } from 'react';
import { Layout, Menu, Avatar, Dropdown, Badge, Typography, Space, Button } from 'antd';
import {
  DashboardOutlined,
  AppstoreOutlined,
  UnorderedListOutlined,
  TeamOutlined,
  BellOutlined,
  FileTextOutlined,
  HistoryOutlined,
  LogoutOutlined,
  UserOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from '@ant-design/icons';
import { history } from 'umi';

const { Sider, Header, Content } = Layout;
const { Text } = Typography;

const MENU_ITEMS = [
  { key: '/admin/dashboard',      icon: <DashboardOutlined />,      label: 'Dashboard' },
  { key: '/admin/equipment',      icon: <AppstoreOutlined />,       label: 'Quản lý thiết bị' },
  { key: '/admin/requests',       icon: <UnorderedListOutlined />,  label: 'Quản lý yêu cầu' },
  { key: '/admin/users',          icon: <TeamOutlined />,           label: 'Quản lý người dùng' },
  { key: '/admin/notifications',  icon: <FileTextOutlined />,       label: 'Log thông báo' },
  { key: '/admin/activity-logs',  icon: <HistoryOutlined />,        label: 'Activity Log' },
];

// Ant Design v4: Dropdown dùng overlay thay vì menu prop
const UserMenu = (
  <Menu>
    <Menu.Item key="profile" icon={<UserOutlined />} onClick={() => history.push('/admin/profile')}>
      Thông tin cá nhân
    </Menu.Item>
    <Menu.Divider />
    <Menu.Item
      key="logout"
      icon={<LogoutOutlined />}
      danger
      onClick={() => {
        localStorage.removeItem('access_token');
        history.push('/user/login');
      }}
    >
      Đăng xuất
    </Menu.Item>
  </Menu>
);

interface AdminLayoutProps {
  children: React.ReactNode;
}

const AdminLayout: React.FC<AdminLayoutProps> = ({ children }) => {
  const [collapsed, setCollapsed] = useState<boolean>(false);
  const currentPath = window.location.pathname;

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {/* ── Sidebar ── */}
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
        trigger={null}
        width={240}
        style={{
          overflow: 'auto',
          height: '100vh',
          position: 'fixed',
          left: 0,
          top: 0,
          bottom: 0,
          background: '#001529',
        }}
      >
        {/* Logo */}
        <div style={{
          height: 64,
          display: 'flex',
          alignItems: 'center',
          justifyContent: collapsed ? 'center' : 'flex-start',
          padding: collapsed ? 0 : '0 24px',
          borderBottom: '1px solid rgba(255,255,255,0.1)',
        }}>
          <AppstoreOutlined style={{ color: '#1890ff', fontSize: 22 }} />
          {!collapsed && (
            <Text strong style={{ color: '#fff', marginLeft: 12, fontSize: 14, whiteSpace: 'nowrap' }}>
              CLB Manager
            </Text>
          )}
        </div>

        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[currentPath]}
          style={{ marginTop: 8 }}
          onClick={({ key }) => history.push(key)}
        >
          {MENU_ITEMS.map((item) => (
            <Menu.Item key={item.key} icon={item.icon}>
              {item.label}
            </Menu.Item>
          ))}
        </Menu>
      </Sider>

      {/* ── Main ── */}
      <Layout style={{ marginLeft: collapsed ? 80 : 240, transition: 'margin-left 0.2s' }}>
        {/* ── Header ── */}
        <Header style={{
          position: 'sticky',
          top: 0,
          zIndex: 100,
          background: '#fff',
          padding: '0 24px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          boxShadow: '0 1px 4px rgba(0,21,41,0.08)',
          height: 64,
        }}>
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{ fontSize: 18 }}
          />

          <Space size={16}>
            <Badge count={0} size="small">
              <Button
                type="text"
                icon={<BellOutlined style={{ fontSize: 20 }} />}
                onClick={() => history.push('/admin/notifications')}
              />
            </Badge>

            {/* Ant Design v4: dùng overlay thay vì menu */}
            <Dropdown overlay={UserMenu} placement="bottomRight" arrow>
              <Space style={{ cursor: 'pointer' }}>
                <Avatar size={32} icon={<UserOutlined />} style={{ backgroundColor: '#1890ff' }} />
                <Text style={{ fontSize: 13 }}>Admin</Text>
              </Space>
            </Dropdown>
          </Space>
        </Header>

        {/* ── Content: dùng children thay vì Outlet (React Router v5) ── */}
        <Content style={{ margin: 24, minHeight: 'calc(100vh - 64px - 48px)' }}>
          {children}
        </Content>
      </Layout>
    </Layout>
  );
};

export default AdminLayout;