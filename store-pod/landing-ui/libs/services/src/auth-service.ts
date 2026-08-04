import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {apiFetch, get} from "./http-utils";
import {generateCodeChallenge, generateCodeVerifier} from "./pkce-utils";
import {AuthEventType, AuthUser} from "@store-front/types";

/** The subset of the OAuth2 token response this client uses. */
export interface TokenResponse {
    access_token?: string;
    id_token?: string;
    refresh_token?: string;
}

export class AuthService {

    static async login(context: StoreContext): Promise<void> {
        const verifier = generateCodeVerifier();
        sessionStorage.setItem('code_verifier', verifier);
        const challenge = await generateCodeChallenge(verifier);


        const redirectUri = `${window.location.origin}/${context.locale}/callback`;
        const clientId = context.store;

        const authUrl = new URL(`${window.location.origin}/cua/oauth2/authorize`);
        authUrl.searchParams.append('response_type', 'code');
        authUrl.searchParams.append('client_id', clientId);
        authUrl.searchParams.append('redirect_uri', redirectUri);
        authUrl.searchParams.append('scope', 'openid');
        authUrl.searchParams.append('code_challenge', challenge);
        authUrl.searchParams.append('code_challenge_method', 'S256');
        authUrl.searchParams.append('prompt', 'login');
        authUrl.searchParams.append('store', context.store);
        authUrl.searchParams.append('lang', context.locale);

        this.setPostLoginRedirect()
        window.location.href = authUrl.toString();
    }

    static setPostLoginRedirect(p?: string) {
        const currentPath = p ? p : window.location.pathname + window.location.search;
        sessionStorage.setItem('postLoginRedirect', currentPath);
    }

    static getPostLoginRedirect() {
        const redirectPath = sessionStorage.getItem('postLoginRedirect') ?? '/';
        sessionStorage.removeItem('postLoginRedirect');
        return redirectPath
    }


    /**
     * Must fail, and the caller must tell the two failures apart: a 401 means the shopper really is
     * signed out, while a 502 or a network error means cua is unreachable. Collapsing them is what made
     * a cua outage look like a logout and reopen the login dialog in a loop.
     */
    static async getMe(context: StoreContext): Promise<AuthUser> {
        return apiFetch<AuthUser>(`${storeBaseServiceUrl('cua', context)}/api/v1/auth/me`, get());
    }

    static async exchangeToken(context: StoreContext, code: string): Promise<TokenResponse> {
        const verifier = sessionStorage.getItem('code_verifier');
        if (!verifier) {
            throw new Error('Code verifier not found');
        }

        const baseUrl = storeBaseServiceUrl('cua', context);
        const redirectUri = `${window.location.origin}/${context.locale}/callback`;
        const clientId = context.store;

        const tokenUrl = `${baseUrl}/oauth2/token`;
        const params = new URLSearchParams();
        params.append('grant_type', 'authorization_code');
        params.append('code', code);
        params.append('redirect_uri', redirectUri);
        params.append('client_id', clientId);
        params.append('code_verifier', verifier);

        // The token endpoint is plain OAuth2, so a failure carries `{error: "invalid_grant"}` rather than
        // our problem body — apiFetch synthesises CLIENT.HTTP_<status> for it, which is still typed.
        const result = await apiFetch<TokenResponse>(tokenUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: params.toString(),
        });

        if (result && result.access_token) {
            sessionStorage.setItem('access_token', result.access_token);
            if (result.id_token) {
                sessionStorage.setItem('id_token', result.id_token);
            }
            if (result.refresh_token) {
                sessionStorage.setItem('refresh_token', result.refresh_token);
            }
            sessionStorage.removeItem('code_verifier');
            window.dispatchEvent(new CustomEvent('auth-change', {detail: {type: AuthEventType.LOGIN}}));
        }
        return result;
    }

    static async logout(context: StoreContext): Promise<void> {
        // Retrieve the id_token, if any
        const idToken = sessionStorage.getItem('id_token');

        // Clean up local tokens immediately
        sessionStorage.removeItem('access_token');
        sessionStorage.removeItem('id_token');
        sessionStorage.removeItem('refresh_token');

        if (idToken) {
            // Build the logout URL for the auth server
            const logoutUrl = new URL(`${window.location.origin}/cua/connect/logout`);
            logoutUrl.searchParams.append('id_token_hint', idToken);
            const post_redirect_uri = window.location.origin + "/" + context.locale
            logoutUrl.searchParams.append('post_logout_redirect_uri', post_redirect_uri);
            logoutUrl.searchParams.append('lang', context.locale);

            // Redirect the browser to perform server‑side logout
            window.location.href = logoutUrl.toString();
        } else {
            // If no id_token, simply notify the app of the logout
            window.dispatchEvent(new CustomEvent('auth-change', {detail: {type: AuthEventType.LOGOUT}}));
        }
    }
}
