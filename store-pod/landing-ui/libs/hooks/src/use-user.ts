import {useCallback, useEffect, useState} from "react";
import {AuthService} from "@store-front/services/auth-service";
import {AuthEventDetail, AuthEventType, AuthUser, StoreContext} from "@store-front/types";

export function useUser(storeContext: StoreContext) {
    const [user, setUser] = useState<AuthUser | null>(null);
    const [loading, setLoading] = useState(true);

    const fetchUser = useCallback(async () => {
        try {
            if (!sessionStorage.getItem('access_token')) {
                setUser(null);
                setLoading(false);
                return;
            }
        } catch {
            setUser(null);
            setLoading(false);
            return;
        }

        setLoading(true);
        try {
            const userData = await AuthService.getMe(storeContext);
            if (userData) setUser(userData); else {
                setUser(null);
            }
        } catch (error) {
            setUser(null);
        } finally {
            setLoading(false);
        }
    }, [storeContext]);

    useEffect(() => {
        fetchUser();

        const handleAuthChange = (event: Event) => {
            const customEvent = event as CustomEvent<AuthEventDetail>;
            if (customEvent.detail?.type === AuthEventType.LOGOUT) {
                setUser(null);
            } else {
                fetchUser();
            }
        };

        window.addEventListener('auth-change', handleAuthChange);
        return () => window.removeEventListener('auth-change', handleAuthChange);
    }, [fetchUser]);

    const login = () => AuthService.login(storeContext);
    const logout = () => AuthService.logout(storeContext);

    return {
        user,
        loading,
        login,
        logout,
        fetchUser
    };
}
