export default [
	// ==================== AUTH ====================
	{
		path: '/user',
		layout: false,
		routes: [
			{
				path: '/user/login',
				name: 'login',
				component: './user/Login', // có sẵn trong base
			},
			// Thêm register khi đã tạo file src/pages/user/Register/index.tsx
			// {
			// 	path: '/user/register',
			// 	name: 'register',
			// 	component: './user/Register',
			// },
			{
				path: '/user',
				redirect: '/user/login',
			},
		],
	},


	// ==================== REDIRECT ====================
	{
		path: '/',
		redirect: '/user/login',
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