export default [
	// USER
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

			// Dashboard
			{
				path: '/admin/dashboard',
				component: './AdminDashboard',
			},

			// Equipment / Device
			{
				path: '/admin/equipment',
				component: './AdminDevice',
			},

			// Requests (Admin)
			{
				path: '/admin/requests',
				component: './AdminRequests',
			},
			{
				path: '/admin/requests/:id',
				component: './AdminRequests/RequestDetail',
			},

			// Notifications (Admin)
			{
				path: '/admin/notifications',
				component: './AdminNotifications',
			},

			// Activity Logs (Admin)
			{
				path: '/admin/activity-logs',
				component: './AdminActivityLogs',
			},

			// Users (Admin)
			{
				path: '/admin/users',
				component: './AdminUsers',
			},
		],
	},

	// ROOT
	{
		path: '/',
		redirect: '/admin/dashboard',
	},

	// ERROR
	{
		path: '/403',
		component: './exception/403/403Page',
		layout: false,
	},
	{
		component: './exception/404',
	},
];