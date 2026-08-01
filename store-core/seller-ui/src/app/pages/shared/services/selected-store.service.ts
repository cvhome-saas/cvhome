import {Injectable, inject} from '@angular/core';
import {StoreService} from './store.service';
import {Store} from '../models/commons';
import {map, Observable, of, tap} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SelectedStoreService {
  public static SELECTED_STORE_ID_KEY = "Selected-Store-Id";
  private readonly storeService = inject(StoreService);
  private _stores: Store[] | undefined = undefined;

  public select(storeId: string): void {
    const selectedStore = this._stores.find(it => it.id.id == storeId);
    if (selectedStore) {
      localStorage.setItem(SelectedStoreService.SELECTED_STORE_ID_KEY, JSON.stringify(selectedStore));
    }
  }

  public currentSelectedStore(): Store | undefined {
    const currentStoreStr = localStorage.getItem(SelectedStoreService.SELECTED_STORE_ID_KEY);
    if (currentStoreStr) {
      const parsedStore = JSON.parse(currentStoreStr);
      if (this._stores && this._stores.some(store => store.id.id === parsedStore.id.id)) {
        return parsedStore;
      }
      return undefined;
    }
    return undefined;
  }

  public selectFirstStore(): Store | undefined {
    if (this._stores && this._stores.length > 0) {
      this.select(this._stores[0].id.id);
      return this._stores[0];
    }
    return undefined;
  }

  current(): Observable<string> {
    return this.getAllStores().pipe(map(() => {
      const current: Store | undefined = this.currentSelectedStore();
      if (current) {
        return current.id.id;
      }
      return this.selectFirstStore()?.id.id;
    }));
  }

  getStore(store: string): Store | undefined {
    return this._stores.find(it => it.id.id == store);
  }

  newStore(store: Store) {
    this._stores.push(store);
  }

  private getAllStores(): Observable<Store[]> {
    if (this._stores == undefined) {
      return this.storeService.list()
        .pipe(map(it => it.content), tap((it: Store[]) => this._stores = it));
    } else {
      return of(this._stores);
    }

  }
}
