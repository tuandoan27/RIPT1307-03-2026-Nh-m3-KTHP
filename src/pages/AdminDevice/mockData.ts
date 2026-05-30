// ============================================================
// pages/admin/equipment/mockData.ts
// Types + mock data cho trang Quản lý thiết bị
// ============================================================

// ─── Types ───────────────────────────────────────────────────

export type StockStatus = 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';

export interface Equipment {
  id: string;
  name: string;
  description: string;
  imageUrl: string;
  totalQuantity: number;
  availableQuantity: number;
  createdAt: string;          // ISO date string
  is_deleted: boolean;
  hasApprovedRequest: boolean; // dùng để block xóa
}

export interface ActivityLog {
  id: string;
  action: 'CREATE_DEVICE' | 'UPDATE_DEVICE' | 'DELETE_DEVICE' | 'UPDATE_STOCK';
  deviceId: string;
  deviceName: string;
  performedBy: string;
  timestamp: string;
  note?: string;
}

export type StockFilter = 'ALL' | StockStatus;

// ─── Helpers ─────────────────────────────────────────────────

export const getStockStatus = (available: number, total: number): StockStatus => {
  if (available === 0) return 'OUT_OF_STOCK';
  if (available / total <= 0.3) return 'LOW_STOCK';
  return 'IN_STOCK';
};

export const STOCK_STATUS_LABEL: Record<StockStatus, string> = {
  IN_STOCK: 'Còn hàng',
  LOW_STOCK: 'Sắp hết',
  OUT_OF_STOCK: 'Hết hàng',
};

export const STOCK_STATUS_COLOR: Record<StockStatus, string> = {
  IN_STOCK: 'success',
  LOW_STOCK: 'warning',
  OUT_OF_STOCK: 'error',
};

// ─── Mock Equipment Data ──────────────────────────────────────

export const MOCK_EQUIPMENT: Equipment[] = [
  {
    id: 'EQ001',
    name: 'Máy chiếu Epson EB-X41',
    description: 'Máy chiếu độ phân giải XGA 3600 lumens, kết nối HDMI & VGA, phù hợp hội trường lớn.',
    imageUrl: 'https://placehold.co/80x80/1890ff/ffffff?text=Epson',
    totalQuantity: 5,
    availableQuantity: 3,
    createdAt: '2025-01-10T08:00:00Z',
    is_deleted: false,
    hasApprovedRequest: true,
  },
  {
    id: 'EQ002',
    name: 'Loa Bluetooth JBL PartyBox 110',
    description: 'Loa di động công suất 160W, chống nước IPX4, thời lượng pin 12 giờ.',
    imageUrl: 'https://placehold.co/80x80/52c41a/ffffff?text=JBL',
    totalQuantity: 8,
    availableQuantity: 6,
    createdAt: '2025-01-15T09:30:00Z',
    is_deleted: false,
    hasApprovedRequest: false,
  },
  {
    id: 'EQ003',
    name: 'Micro không dây Shure BLX288',
    description: 'Bộ 2 micro không dây dải tần UHF, tầm phủ 100m, dùng cho sự kiện & hội thảo.',
    imageUrl: 'https://placehold.co/80x80/722ed1/ffffff?text=Shure',
    totalQuantity: 4,
    availableQuantity: 1,
    createdAt: '2025-02-01T10:00:00Z',
    is_deleted: false,
    hasApprovedRequest: true,
  },
  {
    id: 'EQ004',
    name: 'Màn hình trình chiếu 100 inch',
    description: 'Màn chiếu kéo tay khung nhôm, tỉ lệ 4:3, bề mặt chống lóa matte white.',
    imageUrl: 'https://placehold.co/80x80/fa8c16/ffffff?text=Screen',
    totalQuantity: 3,
    availableQuantity: 0,
    createdAt: '2025-02-10T08:00:00Z',
    is_deleted: false,
    hasApprovedRequest: false,
  },
  {
    id: 'EQ005',
    name: 'Bàn ghế hội nghị (bộ 10)',
    description: 'Bàn dài 2.4m + 10 ghế nệm có bánh xe, thích hợp phòng họp và hội thảo nhỏ.',
    imageUrl: 'https://placehold.co/80x80/eb2f96/ffffff?text=Chair',
    totalQuantity: 6,
    availableQuantity: 4,
    createdAt: '2025-02-20T14:00:00Z',
    is_deleted: false,
    hasApprovedRequest: false,
  },
  {
    id: 'EQ006',
    name: 'Camera Sony ZV-E10',
    description: 'Máy ảnh mirrorless cảm biến APS-C, quay 4K, lý tưởng cho livestream và ghi hình sự kiện.',
    imageUrl: 'https://placehold.co/80x80/13c2c2/ffffff?text=Sony',
    totalQuantity: 2,
    availableQuantity: 2,
    createdAt: '2025-03-05T09:00:00Z',
    is_deleted: false,
    hasApprovedRequest: false,
  },
  {
    id: 'EQ007',
    name: 'Laptop Dell Inspiron 15 (đã xóa)',
    description: 'Thiết bị đã thanh lý — không nên hiển thị.',
    imageUrl: 'https://placehold.co/80x80/999999/ffffff?text=Dell',
    totalQuantity: 1,
    availableQuantity: 0,
    createdAt: '2024-06-01T00:00:00Z',
    is_deleted: true,             // ← sẽ bị lọc ra
    hasApprovedRequest: false,
  },
  {
    id: 'EQ008',
    name: 'Đèn LED Godox SL-60W',
    description: 'Đèn studio LED 60W 5600K, góc chiếu 120°, kèm softbox 60x60cm.',
    imageUrl: 'https://placehold.co/80x80/d4b106/ffffff?text=Godox',
    totalQuantity: 4,
    availableQuantity: 4,
    createdAt: '2025-03-18T11:00:00Z',
    is_deleted: false,
    hasApprovedRequest: false,
  },
];

// ─── Mock Activity Logs (in-memory store) ─────────────────────

export const MOCK_ACTIVITY_LOGS: ActivityLog[] = [
  {
    id: 'LOG001',
    action: 'CREATE_DEVICE',
    deviceId: 'EQ001',
    deviceName: 'Máy chiếu Epson EB-X41',
    performedBy: 'Admin',
    timestamp: '2025-01-10T08:05:00Z',
  },
  {
    id: 'LOG002',
    action: 'UPDATE_STOCK',
    deviceId: 'EQ003',
    deviceName: 'Micro không dây Shure BLX288',
    performedBy: 'Admin',
    timestamp: '2025-03-20T10:30:00Z',
    note: 'Nhập thêm 2 bộ từ kho dự trữ',
  },
];