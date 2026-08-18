import {isPlatformServer} from '@angular/common';
import {InjectionToken, inject, Injectable, PLATFORM_ID} from '@angular/core';
import {SelectedStoreService} from '../store-context/selected-store.service';

export interface RequestContextProvider { params(explicitStore?: string): Record<string, string>; }
export const REQUEST_CONTEXT = new InjectionToken<RequestContextProvider>('REQUEST_CONTEXT', {providedIn: 'root', factory: () => inject(SelectedStoreRequestContext)});
@Injectable({providedIn: 'root'})
export class SelectedStoreRequestContext implements RequestContextProvider {
  private readonly stores = inject(SelectedStoreService);
  private readonly platformId = inject(PLATFORM_ID);

  params(explicitStore?: string): Record<string, string> {
    const store = explicitStore ? this.stores.getStore(explicitStore) : this.stores.currentSelectedStore();
    if (!store) {
      // On the server there is no browser storage to resolve a selection from, so silently sending the
      // request unscoped would query the wrong tenant's data instead of failing. Before BrowserStorage
      // guarded the underlying localStorage read, that case threw a ReferenceError; this keeps the same
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
