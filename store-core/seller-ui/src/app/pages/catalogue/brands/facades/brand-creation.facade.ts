import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {StoreService} from '../../../store-management/services/store.service';
import {ErrorService} from '../../../shared/services/error.service';
import {ReadableMerchantStore} from '../../../store-management/models/store';
import {switchMap} from 'rxjs';
import {ReadableManufacturer} from '../models/brand.model';

@Injectable()
export class BrandCreationFacade {
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly storeService = inject(StoreService);
  private readonly errorService = inject(ErrorService);

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
        error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
      });
  }
}
