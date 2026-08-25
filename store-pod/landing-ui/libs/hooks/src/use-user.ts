import {useEffect, useState} from "react";
import {AuthService} from "@store-front/services/auth-service";
import {AuthEventDetail, AuthEventType, AuthUser, StoreContext} from "@store-front/types";

export function useUser(storeContext: StoreContext) {
    const [user, setUser] = useState<AuthUser | null>(null);
    const [loading, setLoading] = useState(true);

    const fetchUser = async () => {
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
    };

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
    }, [storeContext.store, storeContext.locale]);

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
