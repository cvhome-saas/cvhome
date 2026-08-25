'use client'
import {useEffect} from 'react';
import {useRouter, useSearchParams} from 'next/navigation';
import {AuthService} from '@store-front/services/auth-service';
import type {StoreContext} from '@store-front/types';
import {useThemeStates} from '@/shell/theme/theme-client-states';

export function CallbackClient({storeContext}: { storeContext: StoreContext }) {
    const router = useRouter();
    const code = useSearchParams().get('code');
    const {Redirecting} = useThemeStates();

    useEffect(() => {
        if (!code) return;
        AuthService.exchangeToken(storeContext, code)
            // The stored path already carries the locale prefix (/en/...), so use the plain Next router —
            // the locale-aware one would prefix it again (/en/en).
            .then(() => router.push(AuthService.getPostLoginRedirect()))
            .catch(error => {
                console.error('Failed to exchange token', error);
                router.push(`/${storeContext.locale}/login?error=true`);
            });
    }, [code, router, storeContext]);

    return <Redirecting reason="callback"/>;
}
