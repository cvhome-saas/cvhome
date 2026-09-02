/** A social provider a store has enabled, as cua's public `/api/v1/public/social-logins` returns it. */
export interface SocialLogin {
    /** `google`, `facebook`, `github` — for the icon and the translated label. */
    providerId: string;
    /** The provider's display name. */
    name: string;
    /** `{storeId}.{providerId}` — the tail of `/cua/oauth2/authorization/…`. */
    registrationId: string;
}
