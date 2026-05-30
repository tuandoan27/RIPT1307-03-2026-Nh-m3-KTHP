import React, { useEffect, useState } from 'react';
import { Card, Space, Typography, Tag, message, Select, Modal, Form, Input, Divider, List, Alert, Skeleton } from 'antd';
import { getRequestById, getRequestHistory, approveRequest, rejectRequest, returnRequest } from '@/services/requests';
import { REQUEST_STATUS_LABEL, REQUEST_STATUS_COLOR, type RequestItem } from './mockData';
import { getAvailableTransitions } from './stateMachine';

const { Title, Text } = Typography;
const { Option } = Select;

const RequestDetail: React.FC = () => {
  const [request, setRequest] = useState<RequestItem | null>(null);
  const [historyList, setHistoryList] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [changing, setChanging] = useState(false);

  // extract id from url
  const id = window.location.pathname.split('/').pop() || '';

  const load = async () => {
    setLoading(true);
    const r = await getRequestById(id);
    setRequest(r);
    const h = await getRequestHistory(id);
    setHistoryList(h);
    setLoading(false);
  };

  useEffect(() => { load(); }, [id]);

  const handleTransition = async (to: string) => {
    if (!request) return;
    if (to === 'APPROVED') {
      setChanging(true);
      try {
        await approveRequest(request.id);
        message.success('Đã phê duyệt');
        await load();
      } catch (err: any) {
        message.error(err?.message || 'Lỗi');
      } finally { setChanging(false); }
    } else if (to === 'RETURNED') {
      setChanging(true);
      try {
        await returnRequest(request.id);
        message.success('Xác nhận trả thành công');
        await load();
      } catch (err: any) {
        message.error(err?.message || 'Lỗi');
      } finally { setChanging(false); }
    } else if (to === 'REJECTED') {
      // show modal for reason
      Modal.confirm({
        title: 'Từ chối yêu cầu',
        content: (
          <div>
            <Form id="rejectForm">
              <Form.Item name="reason" rules={[{ required: true, message: 'Nhập lý do' }]}>
                <Input.TextArea rows={3} />
              </Form.Item>
            </Form>
          </div>
        ),
        onOk: async () => {
          const el = document.getElementById('rejectForm');
          // read value via DOM as quick workaround
          const textarea = el?.querySelector('textarea') as HTMLTextAreaElement | null;
          const reason = textarea?.value || '';
          if (!reason) {
            message.error('Lý do là bắt buộc');
            throw new Error('no reason');
          }
          setChanging(true);
          try {
            await rejectRequest(request.id, reason);
            message.success('Đã từ chối yêu cầu');
            await load();
          } catch (err: any) {
            message.error(err?.message || 'Lỗi');
          } finally { setChanging(false); }
        }
      });
    }
  };

  if (!request) return <Card>Không tìm thấy yêu cầu</Card>;

  const allowed = getAvailableTransitions(request.status);

  return (
    <Skeleton loading={loading} active>
      <Card style={{ borderRadius: 8 }}>
        <Space direction="vertical" style={{ display: 'flex' }}>
          <Title level={4}>Chi tiết yêu cầu <Text code>{request.id}</Text></Title>

        {request.status === 'OVERDUE' && (
          <Alert message="Yêu cầu đang quá hạn trả" type="warning" showIcon />
        )}

        <div>
          <Text strong>Sinh viên:</Text> <Text>{request.studentName}</Text>
        </div>
        <div>
          <Text strong>Thiết bị:</Text> <Text>{request.equipmentName}</Text>
        </div>
        <div>
          <Text strong>Ngày mượn:</Text> <Text>{request.borrowDate}</Text>
        </div>
        <div>
          <Text strong>Ngày trả:</Text> <Text>{request.returnDate}</Text>
        </div>
        <div>
          <Text strong>Ghi chú:</Text> <Text>{request.note || '—'}</Text>
        </div>

        <Divider />

        <Space>
          <Text strong>Trạng thái hiện tại:</Text>
          <Tag color={REQUEST_STATUS_COLOR[request.status]}>{REQUEST_STATUS_LABEL[request.status]}</Tag>
        </Space>

        <div>
          <Text strong>Thay đổi trạng thái:</Text>
          <Select style={{ width: 240, marginLeft: 12 }} placeholder="Chọn hành động" onChange={(v) => handleTransition(v)} disabled={changing}>
            {allowed.map((s) => (
              <Option key={s} value={s}>{REQUEST_STATUS_LABEL[s]}</Option>
            ))}
          </Select>
        </div>

        <Divider />

        <div>
          <Text strong>Lịch sử trạng thái:</Text>
          <List
            dataSource={historyList}
            renderItem={(item) => (
              <List.Item>
                <List.Item.Meta
                  title={<Text>{item.status} — <Text type="secondary">{item.by}</Text></Text>}
                  description={<div><Text type="secondary">{new Date(item.timestamp).toLocaleString()}</Text><div>{item.note}</div></div>}
                />
              </List.Item>
            )}
          />
        </div>
      </Space>
    </Card>
    </Skeleton>
  );
};

export default RequestDetail;
