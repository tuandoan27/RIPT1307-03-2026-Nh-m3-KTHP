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

	// ROOT
	{
		path: '/',
		redirect: '/user/login',
	},

	// EXCEPTION
	{
		path: '/403',
		component: './exception/403/403Page',
		layout: false,
	},
	{
		component: './exception/404',
	},
];