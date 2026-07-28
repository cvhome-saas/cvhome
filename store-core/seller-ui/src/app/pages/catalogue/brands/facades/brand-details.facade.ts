import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ActivatedRoute} from '@angular/router';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {StoreService} from '../../../store-management/services/store.service';
import {BrandService} from '../services/brand.service';
import {ErrorService} from '../../../shared/services/error.service';
import {Store} from '../../../store-management/models/store';
import {zip} from 'rxjs';

@Injectable()
export class BrandDetailsFacade {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly storeService = inject(StoreService);
  private readonly brandService = inject(BrandService);
  private readonly errorService = inject(ErrorService);

  readonly brand = signal<any>(null);
  readonly store = signal<Store | null>(null);

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
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  private loadStore(storeCode: string): void {
    this.storeService.getStore(storeCode).subscribe({
      next: (store) => this.store.set(store),
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  private loadBrand(id: string): void {
    this.brandService.getBrandById(id).subscribe({
      next: (brand) => this.brand.set(brand),
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }
}
