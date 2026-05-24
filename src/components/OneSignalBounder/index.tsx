import { initOneSignal } from '@/services/base/api';
import { AppModules } from '@/services/base/constant';
import { currentRole, oneSignalClient, oneSignalRole } from '@/utils/ip';
import { useEffect, useState } from 'react';
import OneSignal from 'react-onesignal';

const OneSignalBounder = (props: { children: React.ReactNode }) => {
	const [oneSignalId, setOneSignalId] = useState<string | null | undefined>();
	const token = localStorage.getItem('token');
	const iframeSource = AppModules[oneSignalRole].url;

	const getUserIdOnesignal = async () => {
		if (!!oneSignalClient) {
			await OneSignal.init({
				appId: oneSignalClient,
			});
			const id = await OneSignal.getUserId();
			setOneSignalId(id);
		}
	};

	/** Show Popup center screen */
	const showPopup = (url: string, w: number = 600, h: number = 400) => {
		// Fixes dual-screen position                             Most browsers      Firefox
		const dualScreenLeft = window.screenLeft !== undefined ? window.screenLeft : window.screenX;
		const dualScreenTop = window.screenTop !== undefined ? window.screenTop : window.screenY;

		const width = window.innerWidth
			? window.innerWidth
			: document.documentElement.clientWidth
			? document.documentElement.clientWidth
			: screen.width;
		const height = window.innerHeight
			? window.innerHeight
			: document.documentElement.clientHeight
			? document.documentElement.clientHeight
			: screen.height;

		const systemZoom = width / window.screen.availWidth;
		const left = (width - w) / 2 / systemZoom + dualScreenLeft;
		const top = (height - h) / 2 / systemZoom + dualScreenTop;
		window.open(
			url,
			'_blank',
			`scrollbars=yes,
					width=${w / systemZoom}, 
					height=${h / systemZoom}, 
					top=${height}, 
					left=${left}
					`,
		);
	};

	/** Nhận message từ trang handle OneSignal */
	const receiveMessage = (e: any) => {
		if (iframeSource?.includes(e.origin)) {
			if (e.data === false) {
				showPopup(`${iframeSource}notification/subscribe`);
			} else if (e.data) setOneSignalId(e.data);
		}
	};

	useEffect(() => {
		// Nếu đây là trang handle OneSignal
		if (oneSignalRole.valueOf() === currentRole.valueOf()) getUserIdOnesignal();
		else if (iframeSource) {
			// window.addEventListener('message', receiveMessage, false);
			// showPopup(`${iframeSource}notification/subscribe`, 1, 1);
		}
	}, []);

	/**
	 * Init OneSignal playerId with auth User
	 * Sử dụng token từ localStorage thay vì OIDC auth context
	 */
	useEffect(() => {
		if (oneSignalId) {
			if (token) {
				try {
					initOneSignal({ playerId: oneSignalId });
				} catch (er) {
					console.log(er);
				}
			}
		}
	}, [oneSignalId, token]);

	return <>{props.children}</>;
};

export default OneSignalBounder;
