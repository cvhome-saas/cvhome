import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {SelectedStoreService} from 'seller-core';
import {StoreService} from 'seller-core/stores';
import {ApiErrorService} from 'seller-core';
import {ReadableMerchantStore} from 'seller-core/stores';
import {switchMap} from 'rxjs';
import {ReadableManufacturer} from 'seller-core/catalog';

@Injectable()
export class BrandCreationFacade {
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly storeService = inject(StoreService);
  private readonly apiErrors = inject(ApiErrorService);

  readonly brand = signal<ReadableManufacturer>({});
  readonly store = signal<ReadableMerchantStore | null>(null);

  init(destroyRef: DestroyRef): void {
    this.selectedStoreService.current()
      .pipe(
        switchMap((selectedStore) => this.storeService.getStore(selectedStore)),
        takeUntilDestroyed(destroyRef)
      )
      .subscribe({
        next: (store) => this.store.set(store),
        error: (err) => this.apiErrors.notify(err)
      });
  }
}
