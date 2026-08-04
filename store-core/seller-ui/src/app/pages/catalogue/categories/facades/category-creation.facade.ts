import {Injectable, inject, signal} from '@angular/core';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {StoreService} from '../../../store-management/services/store.service';
import {ApiErrorService} from '../../../../core/errors/api-error.service';
import {ReadableMerchantStore} from '../../../store-management/models/store';
import {switchMap} from 'rxjs';
import {ReadableCategory} from '../models/category.model';

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
