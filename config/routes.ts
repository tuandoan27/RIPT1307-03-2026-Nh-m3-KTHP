export default [
	{
		path: '/user',
		layout: false,
		routes: [
			{
				path: '/user/login',
				layout: false,
				name: 'login',
				component: './user/Login',
			},
			{
				path: '/user',
				redirect: '/user/login',
			},
		],
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
		],
	},

	{
		path: '/',
		redirect: '/admin/dashboard',
	},
	{
		path: '/403',
		component: './exception/403/403Page',
		layout: false,
	},
	{
		component: './exception/404',
	},
];