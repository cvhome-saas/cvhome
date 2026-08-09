import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ActivatedRoute} from '@angular/router';
import {SelectedStoreService} from 'seller-core';
import {StoreService} from 'seller-core/stores';
import {BrandService} from 'seller-core/catalog';
import {ApiErrorService} from 'seller-core';
import {ReadableMerchantStore} from 'seller-core/stores';
import {zip} from 'rxjs';
import {ReadableManufacturer} from 'seller-core/catalog';

@Injectable()
export class BrandDetailsFacade {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly storeService = inject(StoreService);
  private readonly brandService = inject(BrandService);
  private readonly apiErrors = inject(ApiErrorService);

  readonly brand = signal<ReadableManufacturer | null>(null);
  readonly store = signal<ReadableMerchantStore | null>(null);

  init(destroyRef: DestroyRef): void {
    zip([this.selectedStoreService.current(), this.activatedRoute.params])
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
      next: ([selectedStore, params]) => {
        const id = params.id;
        if (selectedStore) {
          this.loadStore(selectedStore);
        }
        if (id) {
          this.loadBrand(id);
        }
      },
      error: (err) => this.apiErrors.notify(err)
    });
  }

  private loadStore(storeCode: string): void {
    this.storeService.getStore(storeCode).subscribe({
      next: (store) => this.store.set(store),
      error: (err) => this.apiErrors.notify(err)
    });
  }

  private loadBrand(id: string): void {
    this.brandService.getBrandById(id).subscribe({
      next: (brand) => this.brand.set(brand),
      error: (err) => this.apiErrors.notify(err)
    });
  }
}
