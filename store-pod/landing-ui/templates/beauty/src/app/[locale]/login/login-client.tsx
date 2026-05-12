'use client'

import {useEffect} from 'react';
import {useRouter} from 'next/navigation';
import {AuthService} from "@store-front/services/auth-service";
import {StoreContext} from "@store-front/types";

export default function LoginRedirect({storeContext}: { storeContext: StoreContext }) {
    const router = useRouter();

    useEffect(() => {
        const checkLoginAndRedirect = async () => {
            try {
                const user = await AuthService.getMe(storeContext);
                if (user) {
                    router.push('/');
                    return;
                }
                await AuthService.login(storeContext);
            } catch (error) {
                console.error('Failed to handle login redirect', error);
                // Still try to login if getMe fails (e.g. not logged in)
                AuthService.login(storeContext).catch(err => {
                    console.error('Failed to trigger login', err);
                });
            }
        };
        checkLoginAndRedirect();
    }, [router, storeContext]);

    return (
        <div className="flex min-h-screen items-center justify-center">
            <div className="text-center">
                <h2 className="text-2xl font-semibold mb-4">Redirecting to login...</h2>
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
            </div>
        </div>
    );
}
