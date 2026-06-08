export default [

	// LOGIN
	{
		path: '/user/login',
		layout: false,
		component: './Login',
	},
	{
		path: '/user',
		redirect: '/user/login',
	},

	// AUTH (từ fe-sv)
	{
		path: '/login',
		layout: false,
		name: 'login',
		component: './user/Login',
		hideInMenu: true,
	},
	{
		path: '/register',
		layout: false,
		name: 'register',
		component: './user/Register',
		hideInMenu: true,
	},

	// ADMIN
	{
		path: '/admin',
		layout: false,
		component: '@/layouts/AdminLayout',
		routes: [
			{
				path: '/admin',
				redirect: '/admin/dashboard',
			},
			{
				path: '/admin/dashboard',
				component: './AdminDashboard',
			},
			{
				path: '/admin/equipment',
				component: './AdminDevice',
			},
			{
				path: '/admin/requests',
				component: './AdminRequests',
			},
			{
				path: '/admin/requests/:id',
				component: './AdminRequests/RequestDetail',
			},
			{
				path: '/admin/notifications',
				component: './AdminNotifications',
			},
			{
				path: '/admin/activity-logs',
				component: './AdminActivityLogs',
			},
			{
				path: '/admin/users',
				component: './AdminUsers',
			},
		],
	},

	// STUDENT PAGES (từ fe-sv)
	{
		path: '/home',
		name: 'Thiết Bị',
		component: './Home',
		icon: 'ShoppingCartOutlined',
	},
	{
		path: '/equipment/:id',
		name: 'Chi Tiết Thiết Bị',
		component: './EquipmentDetail',
		hideInMenu: true,
	},
	{
		path: '/my-requests',
		name: 'Yêu Cầu Của Tôi',
		component: './MyRequests',
		icon: 'FileTextOutlined',
	},
	{
		path: '/notifications',
		name: 'Thông Báo',
		component: './Notifications',
		icon: 'BellOutlined',
	},
	{
		path: '/profile',
		name: 'Hồ Sơ Cá Nhân',
		component: './Profile',
		icon: 'UserOutlined',
	},

	// ROOT
	{
		path: '/',
		redirect: '/login',
	},

	// EXCEPTION
	{
		path: '/403',
		component: './exception/403/403Page',
		layout: false,
	},
	{
		path: '/hold-on',
		component: './exception/DangCapNhat',
		layout: false,
	},
	{
		component: './exception/404',
	},
];
