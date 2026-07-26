import { Injectable, signal } from '@angular/core';
import { EMPTY_PAGE, PageT, StorePageRequest } from './table.types';

/**
 * Generic, state-only holder for list/table data.
 *
 * IMPORTANT: this service is intentionally NOT `providedIn: 'root'`.
 * Each feature must add it to its own component's `providers` array
 * so every list/table gets its own isolated instance:
 *
 *   @Component({
 *     ...
 *     providers: [OrderListFacade, TableStateService]
 *   })
 *
 * This service holds state only. It has no knowledge of HTTP, the
 * selected store, or any business rule. All orchestration (deciding
 * *when* to reload, building the request, calling the API, handling
 * errors) belongs in the feature's Facade — see order-list.facade.ts
 * for the reference implementation.
 */
@Injectable()
export class TableStateService<T, R = StorePageRequest> {
  private readonly _page = signal<PageT<T>>(EMPTY_PAGE as PageT<T>);
  private readonly _isLoading = signal<boolean>(false);
  private readonly _params = signal<R>({ page: 0, count: 10 } as R);

  readonly page = this._page.asReadonly();
  readonly isLoading = this._isLoading.asReadonly();
  readonly params = this._params.asReadonly();

  setPage(page: PageT<T>): void {
    this._page.set(page);
  }

  setLoading(isLoading: boolean): void {
    this._isLoading.set(isLoading);
  }

  setParams(params: R): void {
    this._params.set(params);
  }

  patchParams(patch: Partial<R>): void {
    this._params.update((current) => ({ ...current, ...patch }));
  }

  reset(defaultCount = 10): void {
    this._page.set(EMPTY_PAGE as PageT<T>);
    this._params.set({ page: 0, count: defaultCount } as R);
  }
}
