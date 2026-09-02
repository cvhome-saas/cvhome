import {InjectionToken} from '@angular/core';

/**
 * What scopes a request to a tenant.
 *
 * A token with an interface rather than a concrete service, because resolving "which store am I looking
 * at" needs the store list, and the store list arrives over HTTP through `CrudService` — which is what
 * asks this question in the first place. Keeping only the contract here lets the implementation live in
 * the api tier, where fetching belongs, without `core/` depending upwards on it.
 *
 * console-ui provides `SelectedStoreRequestContext` (`@api/tenancy/selected-store-request-context.ts`)
 * through `withRequestContext(...)`.
 */
export interface RequestContextProvider {
  params(explicitStore?: string): Record<string, string>;
}

/**
 * Defaults to adding nothing.
 *
 * The token had no default while it lived in console-ui, where a provider is always registered. A
 * library cannot assume that: uaa's console has no selected store and never will, and without a
 * default every `CrudService` call there would fail at injection rather than simply carrying no
 * tenant parameters. An app that has stores overrides it and behaves exactly as before.
 */
export const REQUEST_CONTEXT = new InjectionToken<RequestContextProvider>('REQUEST_CONTEXT', {
  providedIn: 'root',
  factory: () => ({params: () => ({})}),
});
