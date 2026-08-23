'use client'
import {useEffect} from 'react';
import {useRouter} from 'next/navigation';
import {AuthService} from '@store-front/services/auth-service';
import type {StoreContext} from '@store-front/types';
import {useThemeStates} from '@/shell/theme/theme-client-states';

export function LoginRedirect({storeContext}: { storeContext: StoreContext }) {
    const router = useRouter();
    const {Redirecting} = useThemeStates();

    useEffect(() => {
        const run = async () => {
            try {
                const user = await AuthService.getMe(storeContext);
                if (user) {
                    router.push(`/${storeContext.locale}`);
                    return;
                }
                await AuthService.login(storeContext);
            } catch {
                // not logged in (401) or cua unreachable — either way, start the login flow
                AuthService.login(storeContext).catch(err => console.error('Failed to trigger login', err));
            }
        };
        run();
    }, [router, storeContext]);

    return <Redirecting reason="login"/>;
}
