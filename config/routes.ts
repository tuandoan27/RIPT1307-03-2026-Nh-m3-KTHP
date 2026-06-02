export default [
	// ==================== AUTH ====================
	{
		path: '/login',
		layout: false,
		name: 'login',
		component: './user/Login',
	},
	{
		path: '/register',
		layout: false,
		name: 'register',
		component: './user/Register',
	},
	{
		path: '/user',
		layout: false,
		routes: [
			{
				path: '/user/login',
				name: 'login',
				component: './user/Login',
			},
			{
				path: '/user',
				redirect: '/user/login',
			},
		],
	},

	// ==================== STUDENT PAGES ====================
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

	// ==================== REDIRECT ====================
	{
		path: '/',
		redirect: '/login',
	},

	// ==================== EXCEPTION ====================
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