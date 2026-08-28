import {InjectionToken} from '@angular/core';

/**
 * What scopes a request to a tenant.
 *
 * A token with an interface rather than a concrete service, because resolving "which store am I looking
 * at" needs the store list, and the store list arrives over HTTP through `CrudService` — which is what
 * asks this question in the first place. Keeping only the contract here lets the implementation live in
 * the api tier, where fetching belongs, without `core/` depending upwards on it.
 *
 * The app provides `SelectedStoreRequestContext` (`@api/tenancy/selected-store-request-context.ts`).
 */
export interface RequestContextProvider {
  params(explicitStore?: string): Record<string, string>;
}

export const REQUEST_CONTEXT = new InjectionToken<RequestContextProvider>('REQUEST_CONTEXT');
