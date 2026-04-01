'use client'

import {useEffect} from 'react';
import {useRouter, useSearchParams} from 'next/navigation';
import {AuthService} from "@store-front/services/auth-service";
import {StoreContext} from "@store-front/types";

export default function CallbackClient({storeContext}: { storeContext: StoreContext }) {
	const router = useRouter();
	const searchParams = useSearchParams();
	const code = searchParams.get('code');

	useEffect(() => {
		if (code) {
			const exchange = async () => {
				try {
					await AuthService.exchangeToken(storeContext, code);
					router.push('/');
				} catch (error) {
					console.error('Failed to exchange token', error);
					router.push('/login?error=true');
				}
			};
			exchange();
		}
	}, [code, router, storeContext]);

	return (
		<div className="flex min-h-screen items-center justify-center">
			<div className="text-center">
				<h2 className="text-2xl font-semibold mb-4">Authenticating...</h2>
				<div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
			</div>
		</div>
	);
}
