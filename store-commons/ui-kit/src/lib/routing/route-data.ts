import type {Data} from '@angular/router';

/**
 * What a route in this console may carry in its `data`.
 *
 * Angular types `data` as `{[key: string]: any}`, so every read of it was a cast: four of them
 * across the title strategy and the shell facade, each independently asserting `as string |
 * undefined` about a key nothing checked the spelling of. A typo in `breadcrumKey` would have
 * compiled, produced an empty breadcrumb, and looked like a missing translation.
 *
 * Declaring the shape does not make Angular enforce it on the route table — `Route.data` stays
 * `Data` — but it does mean every *reader* goes through `routeData()`, and the fields are written
 * down in one place where a new one has to be added deliberately.
 */
export interface ConsoleRouteData {
  /** `route.*` key for the document title. Every console page supplies one. */
  readonly titleKey?: string;
  /** `route.*` key for the toolbar's last crumb. Console pages only. */
  readonly breadcrumbKey?: string;
  /** Which legal document `LegalPage` is rendering — `terms` or `privacy`. */
  readonly document?: 'terms' | 'privacy';
  /** Which outcome `SubscriptionOutcome` is rendering after a checkout. */
  readonly succeeded?: boolean;
}

/** Reads a route's data as the console's shape, in the one place the cast is made. */
export function routeData(data: Data): ConsoleRouteData {
  return data as ConsoleRouteData;
}
