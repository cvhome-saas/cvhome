import CryptoJS from 'crypto-js';

export function generateCodeVerifier(): string {
    const wordCount = 32 / 4;
    const words = CryptoJS.lib.WordArray.random(wordCount * 4);
    return CryptoJS.enc.Base64url.stringify(words);
}

export async function generateCodeChallenge(verifier: string): Promise<string> {
    const hash = CryptoJS.SHA256(verifier);
    return CryptoJS.enc.Base64url.stringify(hash);
}
