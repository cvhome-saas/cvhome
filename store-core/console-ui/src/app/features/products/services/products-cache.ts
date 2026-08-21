import {Injectable, signal} from '@angular/core';

/**
 * The one thing the product form needs to tell the product list.
 *
 * It exists because injecting `ProductsFacade` to say it was too expensive: the facade is
 * `providedIn: 'root'` and its `rxResource` starts the moment it is constructed, so opening
 * `/products/7` fetched a page of the products *list* nobody was looking at, purely because the form
 * held a reference in order to call one method after a save.
 *
 * A stamp rather than a method call, so the dependency points the right way — the list watches this,
 * the form bumps it, and neither knows about the other.
 */
@Injectable({providedIn: 'root'})
export class ProductsCache {
  /** Bumped after any write that changes what the list would show. */
  readonly stamp = signal(0);

  /** Called by the form after a save, so returning to the list does not show a stale row. */
  invalidate(): void {
    this.stamp.update((value) => value + 1);
  }
}
