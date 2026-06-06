export default [

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

	
	// ==================== REDIRECT ====================
	{
		path: '/',
		redirect: '/user/login',
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