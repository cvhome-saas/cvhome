export interface User {
    tokenValue: string;
    issuedAt: string;
    expiresAt: string;
    headers: {
        kid: string;
        alg: string;
    };
    claims: {
        sub: string;
        aud: string[];
        nbf: string;
        scope: string[];
        iss: string;
        exp: string;
        iat: string;
        jti: string;
        givenName?: string;
        familyName?: string;
        name?: string;
        picture?: string;
        email?: string;
        emailVerified?: boolean;
    };
    audience: string[];
    id: string;
    issuer: string;
    notBefore: string;
    subject: string;
}
