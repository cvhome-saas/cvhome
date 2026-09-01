/**
 * PKCE (RFC 7636) on the Web Crypto API. `crypto.getRandomValues` and `crypto.subtle.digest` are native in every
 * browser the storefront targets (see storefront/package.json `browserslist`), so no library is needed — the
 * crypto-js this replaced was 73 KB (23 KB brotli) in every shopper's first-load bundle for two calls.
 */

/** Base64url without padding (RFC 4648 §5), as the code_challenge/verifier alphabet requires. */
export function base64url(bytes: Uint8Array): string {
    let binary = '';
    for (const b of bytes) binary += String.fromCharCode(b);
    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

/** 32 random bytes → 43 base64url characters, inside the RFC's 43–128 range. */
export function generateCodeVerifier(): string {
    return base64url(crypto.getRandomValues(new Uint8Array(32)));
}

/** `S256`: base64url(SHA-256(ASCII(verifier))). */
export async function generateCodeChallenge(verifier: string): Promise<string> {
    const hash = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(verifier));
    return base64url(new Uint8Array(hash));
}
