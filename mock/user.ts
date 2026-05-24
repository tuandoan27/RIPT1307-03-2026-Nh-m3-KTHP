import { Request, Response } from 'express';

const waitTime = (time: number = 100) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(true);
    }, time);
  });
};

async function getFakeCaptcha(req: Request, res: Response) {
  await waitTime(2000);
  return res.json('captcha-xxx');
}

const { ANT_DESIGN_PRO_ONLY_DO_NOT_USE_IN_YOUR_PRODUCTION } = process.env;

/**
 * 当前用户的权限，如果为空代表没登录
 * current user access， if is '', user need login
 * 如果是 pro 的预览，默认是有权限的
 */
let access = ANT_DESIGN_PRO_ONLY_DO_NOT_USE_IN_YOUR_PRODUCTION === 'site' ? 'admin' : '';

const getAccess = () => {
  return access;
};

// 代码中会兼容本地 service mock 以及部署站点的静态数据
export default {
  // 支持值为 Object 和 Array
  'GET /api/currentUser': (req: Request, res: Response) => {
    if (!getAccess()) {
      res.status(401).send({
        data: {
          isLogin: false,
        },
        errorCode: '401',
        errorMessage: '请先登录！',
        success: true,
      });
      return;
    }
    res.send({
      name: 'Serati Ma',
      avatar: 'https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png',
      userid: '00000001',
      email: 'antdesign@alipay.com',
      signature: '海纳百川，有容乃大',
      title: '交互专家',
      group: '蚂蚁金服－某某某事业群－某某平台部－某某技术部－UED',
      tags: [
        {
          key: '0',
          label: '很有想法的',
        },
        {
          key: '1',
          label: '专注设计',
        },
        {
          key: '2',
          label: '辣~',
        },
        {
          key: '3',
          label: '大长腿',
        },
        {
          key: '4',
          label: '川妹子',
        },
        {
          key: '5',
          label: '海纳百川',
        },
      ],
      notifyCount: 12,
      unreadCount: 11,
      country: 'China',
      access: getAccess(),
      geographic: {
        province: {
          label: '浙江省',
          key: '330000',
        },
        city: {
          label: '杭州市',
          key: '330100',
        },
      },
      address: '西湖区工专路 77 号',
      phone: '0752-268888888',
    });
  },
  // GET POST 可省略
  'GET /api/users': [
    {
      key: '1',
      name: 'John Brown',
      age: 32,
      address: 'New York No. 1 Lake Park',
    },
    {
      key: '2',
      name: 'Jim Green',
      age: 42,
      address: 'London No. 1 Lake Park',
    },
    {
      key: '3',
      name: 'Joe Black',
      age: 32,
      address: 'Sidney No. 1 Lake Park',
    },
  ],
  'POST /api/login/account': async (req: Request, res: Response) => {
    const { password, username, type } = req.body;
    await waitTime(2000);
    if (password === 'ant.design' && username === 'admin') {
      res.send({
        status: 'ok',
        type,
        currentAuthority: 'admin',
      });
      access = 'admin';
      return;
    }
    if (password === 'ant.design' && username === 'user') {
      res.send({
        status: 'ok',
        type,
        currentAuthority: 'user',
      });
      access = 'user';
      return;
    }
    if (type === 'mobile') {
      res.send({
        status: 'ok',
        type,
        currentAuthority: 'admin',
      });
      access = 'admin';
      return;
    }

    res.send({
      status: 'error',
      type,
      currentAuthority: 'guest',
    });
    access = 'guest';
  },
  'POST /api/login': async (req: Request, res: Response) => {
    const { email, password } = req.body;
    await waitTime(1500);
    if (password === '123456' && email === 'student@ptit.edu.vn') {
      res.send({
        status: 'ok',
        token: 'mock-jwt-token-student-xyz',
        role: 'student',
        user: {
          id: '1',
          fullName: 'Nguyễn Văn A',
          studentId: 'K20CT001',
          email: 'student@ptit.edu.vn',
        },
      });
      access = 'student';
      return;
    }
    if (password === '123456' && email === 'admin@ptit.edu.vn') {
      res.send({
        status: 'ok',
        token: 'mock-jwt-token-admin-xyz',
        role: 'admin',
        user: {
          id: '2',
          fullName: 'Admin PTIT',
          email: 'admin@ptit.edu.vn',
        },
      });
      access = 'admin';
      return;
    }

    res.send({
      status: 'error',
      message: 'Email hoặc mật khẩu không chính xác!',
    });
    access = '';
  },
  'POST /api/login/outLogin': (req: Request, res: Response) => {
    access = '';
    res.send({ data: {}, success: true });
  },
  'POST /api/register': (req: Request, res: Response) => {
    res.send({ status: 'ok', currentAuthority: 'user', success: true });
  },
  'GET /api/500': (req: Request, res: Response) => {
    res.status(500).send({
      timestamp: 1513932555104,
      status: 500,
      error: 'error',
      message: 'error',
      path: '/base/category/list',
    });
  },
  'GET /api/404': (req: Request, res: Response) => {
    res.status(404).send({
      timestamp: 1513932643431,
      status: 404,
      error: 'Not Found',
      message: 'No message available',
      path: '/base/category/list/2121212',
    });
  },
  'GET /api/403': (req: Request, res: Response) => {
    res.status(403).send({
      timestamp: 1513932555104,
      status: 403,
      error: 'Unauthorized',
      message: 'Unauthorized',
      path: '/base/category/list',
    });
  },
  'GET /api/401': (req: Request, res: Response) => {
    res.status(401).send({
      timestamp: 1513932555104,
      status: 401,
      error: 'Unauthorized',
      message: 'Unauthorized',
      path: '/base/category/list',
    });
  },

  'GET  /api/login/captcha': getFakeCaptcha,

  // ===== EQUIPMENT RENTAL SYSTEM =====
  'GET /api/equipment': (req: Request, res: Response) => {
    res.send({
      data: [
        {
          id: '1',
          name: 'Laptop Dell XPS 15',
          description: 'Laptop cao cấp cho đồ án',
          image: 'https://via.placeholder.com/300x200?text=Laptop+XPS',
          totalQuantity: 5,
          availableQuantity: 2,
          isDeleted: false,
          status: 'available',
        },
        {
          id: '2',
          name: 'Máy Chiếu Epson',
          description: 'Máy chiếu 1080p cho tiết học',
          image: 'https://via.placeholder.com/300x200?text=Projector',
          totalQuantity: 3,
          availableQuantity: 1,
          isDeleted: false,
          status: 'available',
        },
        {
          id: '3',
          name: 'Bộ Vi Xử Lý Raspberry Pi',
          description: 'Bo mạch nhúng cho IoT',
          image: 'https://via.placeholder.com/300x200?text=RaspberryPi',
          totalQuantity: 10,
          availableQuantity: 0,
          isDeleted: false,
          status: 'unavailable',
        },
      ],
      success: true,
    });
  },
  'GET /api/equipment/:id': (req: Request, res: Response) => {
    res.send({
      data: {
        id: req.params.id,
        name: 'Laptop Dell XPS 15',
        description: 'Laptop cao cấp với CPU Intel i7, RAM 16GB, SSD 512GB. Phù hợp cho các đồ án lập trình và thiết kế đồ họa.',
        image: 'https://via.placeholder.com/500x400?text=Laptop+XPS',
        totalQuantity: 5,
        availableQuantity: 2,
        isDeleted: false,
      },
      success: true,
    });
  },
  'GET /api/equipment/:id/booked-dates': (req: Request, res: Response) => {
    res.send({
      data: [
        { date: '2026-05-25', bookedCount: 2 },
        { date: '2026-05-26', bookedCount: 3 },
      ],
      success: true,
    });
  },
  'GET /api/equipment/:id/check-overlap': (req: Request, res: Response) => {
    res.send({
      overlapCount: 1,
      success: true,
    });
  },
  'POST /api/equipment/:id/borrow-request': async (req: Request, res: Response) => {
    await waitTime(1000);
    res.send({
      data: { id: 'BR-' + Date.now() },
      success: true,
      message: 'Gửi yêu cầu mượn thành công',
    });
  },
  'GET /api/my-requests': (req: Request, res: Response) => {
    res.send({
      data: [
        {
          id: '1',
          equipmentName: 'Laptop Dell XPS 15',
          borrowDate: '2026-05-27',
          returnDate: '2026-05-30',
          createdDate: '2026-05-24 10:00',
          status: 'APPROVED',
          history: [
            { status: 'PENDING', date: '2026-05-24 10:00', user: 'System', note: 'Yêu cầu được tạo' },
            { status: 'APPROVED', date: '2026-05-24 14:30', user: 'Trần Quốc Anh', note: 'Phê duyệt yêu cầu' },
          ],
        },
      ],
      success: true,
    });
  },
  'GET /api/profile': (req: Request, res: Response) => {
    if (!getAccess()) {
      res.status(401).send({ error: 'Unauthorized' });
      return;
    }
    res.send({
      data: {
        id: '1',
        fullName: 'Nguyễn Văn A',
        studentId: 'K20CT001',
        email: 'student@ptit.edu.vn',
        penaltyPoints: 3,
        locked: false,
      },
      success: true,
    });
  },
  'GET /api/penalty-history': (req: Request, res: Response) => {
    res.send({
      data: [
        { id: '1', reason: 'Trả thiết bị hư hỏng', points: 2, date: '2026-05-14' },
        { id: '2', reason: 'Trả thiết bị quá hạn 2 ngày', points: 1, date: '2026-05-04' },
      ],
      success: true,
    });
  },
  'POST /api/change-password': async (req: Request, res: Response) => {
    await waitTime(1000);
    res.send({
      success: true,
      message: 'Đổi mật khẩu thành công',
    });
  },
  'GET /api/notifications': (req: Request, res: Response) => {
    res.send({
      data: [
        {
          id: '1',
          title: 'Yêu cầu được phê duyệt',
          content: 'Yêu cầu mượn Laptop Dell XPS 15 của bạn đã được phê duyệt.',
          type: 'success',
          read: false,
          createdDate: '2026-05-24 14:30',
          relatedUrl: '/my-requests',
        },
      ],
      success: true,
    });
  },
  'PUT /api/notifications/:id/read': (req: Request, res: Response) => {
    res.send({ success: true });
  },
  'PUT /api/notifications/mark-all-read': (req: Request, res: Response) => {
    res.send({ success: true });
  },
};
