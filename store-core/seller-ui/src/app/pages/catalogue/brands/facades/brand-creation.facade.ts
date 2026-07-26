import {Injectable, inject, signal} from '@angular/core';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {StoreService} from '../../../store-management/services/store.service';
import {ErrorService} from '../../../shared/services/error.service';
import {Store} from '../../../store-management/models/store';
import {switchMap} from 'rxjs';

@Injectable()
export class BrandCreationFacade {
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly storeService = inject(StoreService);
  private readonly errorService = inject(ErrorService);

  readonly brand = signal<any>({});
  readonly store = signal<Store | null>(null);

  init(): void {
    this.selectedStoreService.current()
      .pipe(
        switchMap((selectedStore) => this.storeService.getStore(selectedStore))
      )
      .subscribe({
        next: (store) => this.store.set(store),
        error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
      });
  }
}
