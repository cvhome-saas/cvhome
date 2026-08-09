import {Injectable, inject, signal} from '@angular/core';
import {SelectedStoreService} from 'seller-core';
import {StoreService} from 'seller-core/stores';
import {ApiErrorService} from 'seller-core';
import {ReadableMerchantStore} from 'seller-core/stores';
import {switchMap} from 'rxjs';
import {ReadableCategory} from 'seller-core/catalog';

@Injectable()
export class CategoryCreationFacade {
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly storeService = inject(StoreService);
  private readonly apiErrors = inject(ApiErrorService);

  readonly category = signal<ReadableCategory>({});
  readonly store = signal<ReadableMerchantStore | null>(null);

  init(): void {
    this.selectedStoreService.current()
      .pipe(
        switchMap((selectedStore) => this.storeService.getStore(selectedStore))
      )
      .subscribe({
        next: (store) => this.store.set(store),
        error: (err) => this.apiErrors.notify(err)
      });
  }
}
