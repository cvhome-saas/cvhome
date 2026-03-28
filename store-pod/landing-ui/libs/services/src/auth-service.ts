import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {handleResponse} from "./http-utils";
import {generateCodeChallenge, generateCodeVerifier} from "./pkce-utils";
import {AuthEventType, User} from "@store-front/types";

export class AuthService {

	static async login(context: StoreContext): Promise<void> {
		const verifier = generateCodeVerifier();
		sessionStorage.setItem('code_verifier', verifier);
		const challenge = await generateCodeChallenge(verifier);



		const redirectUri = `${window.location.origin}/callback`;
		const clientId = context.store;

		const authUrl = new URL(`${window.location.origin}/cua/oauth2/authorize`);
		authUrl.searchParams.append('response_type', 'code');
		authUrl.searchParams.append('client_id', clientId);
		authUrl.searchParams.append('redirect_uri', redirectUri);
		authUrl.searchParams.append('scope', 'openid');
		authUrl.searchParams.append('code_challenge', challenge);
		authUrl.searchParams.append('code_challenge_method', 'S256');

		window.location.href = authUrl.toString();
	}

	static async getMe(context: StoreContext): Promise<User | undefined> {
		const url = `${storeBaseServiceUrl('cua',context)}/api/v1/auth/me`;
		const response = await fetch(url, {
			headers: {
				'Accept': 'application/json',
				'Authorization': `Bearer ${sessionStorage.getItem('access_token')}`,
			},
		});
		return handleResponse<User>(response);
	}

	static async exchangeToken(context: StoreContext, code: string): Promise<any> {
		const verifier = sessionStorage.getItem('code_verifier');
		if (!verifier) {
			throw new Error('Code verifier not found');
		}

		const baseUrl = storeBaseServiceUrl('cua', context);
		const redirectUri = `${window.location.origin}/callback`;
		const clientId = context.store;

		const tokenUrl = `${baseUrl}/oauth2/token`;
		const params = new URLSearchParams();
		params.append('grant_type', 'authorization_code');
		params.append('code', code);
		params.append('redirect_uri', redirectUri);
		params.append('client_id', clientId);
		params.append('code_verifier', verifier);

		const response = await fetch(tokenUrl, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: params.toString(),
		});

		const result = await handleResponse<any>(response);
		if (result && result.access_token) {
			sessionStorage.setItem('access_token', result.access_token);
			if (result.refresh_token) {
				sessionStorage.setItem('refresh_token', result.refresh_token);
			}
			sessionStorage.removeItem('code_verifier');
			window.dispatchEvent(new CustomEvent('auth-change', {detail: {type: AuthEventType.LOGIN}}));
		}
		return result;
	}

	static async logout(): Promise<void> {
		sessionStorage.removeItem('access_token');
		sessionStorage.removeItem('refresh_token');
		window.dispatchEvent(new CustomEvent('auth-change', {detail: {type: AuthEventType.LOGOUT}}));
	}


}
