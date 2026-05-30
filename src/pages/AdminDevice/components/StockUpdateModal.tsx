// ============================================================
// pages/admin/equipment/components/StockUpdateModal.tsx
// Modal Cập nhật tồn kho thủ công kèm lý do
// ============================================================

import React, { useEffect } from 'react';
import {
  Modal, Form, InputNumber, Input, Typography,
  Space, Alert, Divider, Tag,
} from 'antd';
import { StockOutlined } from '@ant-design/icons';
import type { Equipment } from '../mockData';
import { getStockStatus, STOCK_STATUS_LABEL, STOCK_STATUS_COLOR } from '../mockData';

const { TextArea } = Input;
const { Text } = Typography;

// ─── Props ────────────────────────────────────────────────────

interface StockUpdateModalProps {
  open: boolean;
  device: Equipment | null;
  onCancel: () => void;
  onSubmit: (values: StockUpdateFormValues) => void;
  confirmLoading?: boolean;
}

export interface StockUpdateFormValues {
  newAvailable: number;
  reason: string;
}

// ─── Component ────────────────────────────────────────────────

const StockUpdateModal: React.FC<StockUpdateModalProps> = ({
  open,
  device,
  onCancel,
  onSubmit,
  confirmLoading = false,
}) => {
  const [form] = Form.useForm<StockUpdateFormValues>();

  useEffect(() => {
    if (open && device) {
      form.setFieldsValue({
        newAvailable: device.availableQuantity,
        reason: '',
      });
    }
  }, [open, device]);

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      onSubmit(values);
    } catch (_) {}
  };

  const handleCancel = () => {
    form.resetFields();
    onCancel();
  };

  if (!device) return null;

  const currentStatus = getStockStatus(device.availableQuantity, device.totalQuantity);

  return (
    <Modal
      title={
        <Space>
          <StockOutlined style={{ color: '#1890ff' }} />
          <span>Cập nhật tồn kho</span>
        </Space>
      }
      visible={open}
      onOk={handleOk}
      onCancel={handleCancel}
      okText="Cập nhật"
      cancelText="Huỷ"
      confirmLoading={confirmLoading}
      width={480}
      destroyOnClose
    >
      <Divider style={{ margin: '12px 0 16px' }} />

      {/* Thông tin thiết bị hiện tại */}
      <div style={{
        background: '#f5f5f5',
        borderRadius: 8,
        padding: '12px 16px',
        marginBottom: 20,
      }}>
        <Text strong style={{ display: 'block', marginBottom: 8 }}>{device.name}</Text>
        <Space size={16} wrap>
          <Text type="secondary" style={{ fontSize: 13 }}>
            Tổng SL: <Text strong>{device.totalQuantity}</Text>
          </Text>
          <Text type="secondary" style={{ fontSize: 13 }}>
            Hiện còn:{' '}
            <Text strong style={{ color: device.availableQuantity === 0 ? '#ff4d4f' : '#52c41a' }}>
              {device.availableQuantity}
            </Text>
          </Text>
          <Tag color={STOCK_STATUS_COLOR[currentStatus]}>
            {STOCK_STATUS_LABEL[currentStatus]}
          </Tag>
        </Space>
      </div>

      <Alert
        type="info"
        showIcon
        message="Lưu ý"
        description="Số lượng còn lại không được vượt quá tổng số lượng của thiết bị."
        style={{ marginBottom: 20 }}
      />

      <Form form={form} layout="vertical" requiredMark="optional">
        {/* Số lượng mới */}
        <Form.Item
          name="newAvailable"
          label="Số lượng còn lại (mới)"
          rules={[
            { required: true, message: 'Vui lòng nhập số lượng mới' },
            {
              validator: (_, value) => {
                if (value < 0) return Promise.reject('Số lượng không âm');
                if (value > device.totalQuantity)
                  return Promise.reject(`Không vượt quá tổng số lượng (${device.totalQuantity})`);
                return Promise.resolve();
              },
            },
          ]}
        >
          <InputNumber
            min={0}
            max={device.totalQuantity}
            style={{ width: '100%' }}
            addonAfter={`/ ${device.totalQuantity}`}
          />
        </Form.Item>

        {/* Lý do */}
        <Form.Item
          name="reason"
          label="Lý do cập nhật"
          rules={[
            { required: true, message: 'Vui lòng nhập lý do' },
            { min: 10, message: 'Lý do ít nhất 10 ký tự' },
            { max: 300, message: 'Tối đa 300 ký tự' },
          ]}
        >
          <TextArea
            rows={3}
            placeholder="VD: Nhập thêm 2 bộ từ kho dự trữ, 1 bộ gửi bảo dưỡng..."
            showCount
            maxLength={300}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default StockUpdateModal;