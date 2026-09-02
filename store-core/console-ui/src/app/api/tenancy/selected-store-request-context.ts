/** Console-native; not a port from seller-core. */
import {isPlatformServer} from '@angular/common';
import {Injectable, PLATFORM_ID, inject} from '@angular/core';

import type {RequestContextProvider} from '@cvhome-saas/ui-kit';
import {SelectedStoreService} from '@api/tenancy/selected-store.service';

/**
 * Scopes every request to the store the console is currently working in.
 *
 * Lives in the api tier rather than in `core/http` because it reads account state that is fetched over
 * HTTP; `core/` holds only the `REQUEST_CONTEXT` contract it satisfies.
 */
@Injectable({providedIn: 'root'})
export class SelectedStoreRequestContext implements RequestContextProvider {
  private readonly stores = inject(SelectedStoreService);
  private readonly platformId = inject(PLATFORM_ID);

  params(explicitStore?: string): Record<string, string> {
    const store = explicitStore ? this.stores.getStore(explicitStore) : this.stores.currentSelectedStore();
    if (!store) {
      // On the server there is no browser storage to resolve a selection from, so silently sending the
      // request unscoped would query the wrong tenant's data instead of failing. This keeps that
      // fail-loud contract with a message that says why.
      if (isPlatformServer(this.platformId)) {
        throw new Error(
          'No store context available during server-side rendering — CrudService cannot scope this ' +
          'request. Restrict the route to RenderMode.Client, or pass an explicit store.');
      }
      return {};
    }
    return {store: store.id, pod: store.podId.id};
  }
}
