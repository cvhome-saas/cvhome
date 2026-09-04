import {AdminIdpService, IDP_API_BASE} from '@cvhome-saas/ui-kit/uaa';

/**
 * How a shopper signs in to this store, administered by its own merchant.
 *
 * The same client the platform console uses, pointed at cua instead of uaa: the contract is
 * identical because it is the same service behind both. Provided by the page rather than the
 * application, so the platform's own provider screen is untouched.
 *
 * Unlike the three-preset screen this replaces, a provider configured here can be a generic OIDC or
 * OAuth2 one whose endpoints the merchant types. The server refuses any that is not public HTTPS or
 * that resolves inside its own network, and rations `test` per store — it is the one call that makes
 * the server fetch a URL on demand.
 */
export const IDENTITY_PROVIDERS_API_PATH = '/spg/cua/api/v1/private/identity-providers';

export const IDENTITY_PROVIDERS_PROVIDERS = [
  {provide: IDP_API_BASE, useValue: IDENTITY_PROVIDERS_API_PATH},
  AdminIdpService,
] as const;
