'use client'
import {type ReactNode, useEffect} from 'react';
import {useUser} from '@store-front/hooks/use-user';
import type {StoreContext} from '@store-front/types';
import {useThemeStates} from '@/shell/theme/theme-client-states';

/** Gate for /customer/**: sends unauthenticated shoppers to cua login and shows the theme's Redirecting state meanwhile. */
export function Secured({storeContext, children}: { storeContext: StoreContext; children: ReactNode }) {
    const {user, loading, login} = useUser(storeContext);
    const {Redirecting} = useThemeStates();

    useEffect(() => {
        if (!loading && !user) login();
    }, [user, loading, login]);

    if (loading || !user) return <Redirecting reason="login"/>;
    return <>{children}</>;
}
