// ============================================================
// pages/admin/equipment/components/EquipmentFormModal.tsx
// Modal Thêm / Sửa thiết bị
// ============================================================

import React, { useEffect, useState } from 'react';
import {
  Modal, Form, Input, InputNumber, Upload, Button,
  Image, Typography, Space, Divider,
} from 'antd';
import {
  UploadOutlined,
  DeleteOutlined,
  PictureOutlined,
} from '@ant-design/icons';
import type { Equipment } from '../mockData';

const { TextArea } = Input;
const { Text } = Typography;

// ─── Props ────────────────────────────────────────────────────

interface EquipmentFormModalProps {
  open: boolean;
  mode: 'create' | 'edit';
  initialData?: Equipment | null;
  onCancel: () => void;
  onSubmit: (values: EquipmentFormValues, imagePreview: string) => void;
  confirmLoading?: boolean;
}

export interface EquipmentFormValues {
  name: string;
  description: string;
  totalQuantity: number;
}

// ─── Component ────────────────────────────────────────────────

const EquipmentFormModal: React.FC<EquipmentFormModalProps> = ({
  open,
  mode,
  initialData,
  onCancel,
  onSubmit,
  confirmLoading = false,
}) => {
  const [form] = Form.useForm<EquipmentFormValues>();
  const [imagePreview, setImagePreview] = useState<string>('');

  // Điền dữ liệu cũ khi edit
  useEffect(() => {
    if (open) {
      if (mode === 'edit' && initialData) {
        form.setFieldsValue({
          name: initialData.name,
          description: initialData.description,
          totalQuantity: initialData.totalQuantity,
        });
        setImagePreview(initialData.imageUrl || '');
      } else {
        form.resetFields();
        setImagePreview('');
      }
    }
  }, [open, mode, initialData]);

  // Đọc file ảnh → base64 preview (không upload server)
  const handleImageUpload = (file: File) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      if (e.target?.result) {
        setImagePreview(e.target.result as string);
      }
    };
    reader.readAsDataURL(file);
    return false; // ngăn antd tự upload
  };

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      onSubmit(values, imagePreview);
    } catch (_) {
      // validation failed — antd hiển thị lỗi tự động
    }
  };

  const handleCancel = () => {
    form.resetFields();
    setImagePreview('');
    onCancel();
  };

  return (
    <Modal
      title={
        <Space>
          <PictureOutlined style={{ color: '#1890ff' }} />
          <span>{mode === 'create' ? 'Thêm thiết bị mới' : 'Chỉnh sửa thiết bị'}</span>
        </Space>
      }
      visible={open}
      onOk={handleOk}
      onCancel={handleCancel}
      okText={mode === 'create' ? 'Thêm thiết bị' : 'Lưu thay đổi'}
      cancelText="Huỷ"
      confirmLoading={confirmLoading}
      width={560}
      destroyOnClose
    >
      <Divider style={{ margin: '12px 0 20px' }} />

      <Form form={form} layout="vertical" requiredMark="optional">
        {/* Ảnh preview + upload */}
        <Form.Item label="Ảnh thiết bị">
          <Space direction="vertical" style={{ width: '100%' }}>
            {/* Preview */}
            {imagePreview ? (
              <div style={{ position: 'relative', display: 'inline-block' }}>
                <Image
                  src={imagePreview}
                  alt="preview"
                  width={120}
                  height={120}
                  style={{ objectFit: 'cover', borderRadius: 8, border: '1px solid #f0f0f0' }}
                  preview={false}
                />
                <Button
                  size="small"
                  danger
                  type="text"
                  icon={<DeleteOutlined />}
                  style={{
                    position: 'absolute',
                    top: 4,
                    right: 4,
                    background: 'rgba(255,255,255,0.85)',
                    borderRadius: 4,
                  }}
                  onClick={() => setImagePreview('')}
                />
              </div>
            ) : (
              <div style={{
                width: 120,
                height: 120,
                borderRadius: 8,
                border: '1px dashed #d9d9d9',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                background: '#fafafa',
                flexDirection: 'column',
                gap: 4,
              }}>
                <PictureOutlined style={{ fontSize: 28, color: '#bfbfbf' }} />
                <Text type="secondary" style={{ fontSize: 11 }}>Chưa có ảnh</Text>
              </div>
            )}

            <Upload
              accept="image/*"
              showUploadList={false}
              beforeUpload={(file) => { handleImageUpload(file); return false; }}
            >
              <Button icon={<UploadOutlined />} size="small">
                {imagePreview ? 'Đổi ảnh' : 'Tải ảnh lên'}
              </Button>
            </Upload>
            <Text type="secondary" style={{ fontSize: 12 }}>
              Hỗ trợ JPG, PNG, WEBP · Tối đa 5MB
            </Text>
          </Space>
        </Form.Item>

        {/* Tên thiết bị */}
        <Form.Item
          name="name"
          label="Tên thiết bị"
          rules={[
            { required: true, message: 'Vui lòng nhập tên thiết bị' },
            { max: 100, message: 'Tối đa 100 ký tự' },
          ]}
        >
          <Input placeholder="VD: Máy chiếu Epson EB-X41" allowClear />
        </Form.Item>

        {/* Mô tả */}
        <Form.Item
          name="description"
          label="Mô tả"
          rules={[{ max: 500, message: 'Tối đa 500 ký tự' }]}
        >
          <TextArea
            rows={3}
            placeholder="Mô tả ngắn về thiết bị, thông số kỹ thuật..."
            showCount
            maxLength={500}
          />
        </Form.Item>

        {/* Số lượng */}
        <Form.Item
          name="totalQuantity"
          label="Tổng số lượng"
          rules={[
            { required: true, message: 'Vui lòng nhập số lượng' },
            { type: 'number', min: 1, message: 'Số lượng phải ≥ 1' },
          ]}
          extra={
            mode === 'edit'
              ? 'Thay đổi tổng số lượng sẽ tính lại số lượng còn lại tương ứng.'
              : undefined
          }
        >
          <InputNumber
            min={1}
            max={9999}
            style={{ width: '100%' }}
            placeholder="VD: 5"
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default EquipmentFormModal;