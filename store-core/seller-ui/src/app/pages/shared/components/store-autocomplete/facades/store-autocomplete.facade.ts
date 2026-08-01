import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {StoreService} from '../../../services/store.service';
import {ErrorService} from '../../../services/error.service';
import {ManagerStoreId, ManagerStore} from '../../../models/commons';

@Injectable()
export class StoreAutocompleteFacade {
  private readonly storeService = inject(StoreService);
  private readonly errorService = inject(ErrorService);

  readonly stores = signal<ManagerStore[]>([]);

  init(destroyRef: DestroyRef): void {
    this.storeService.list()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: (page) => this.stores.set(page.content),
        error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
      });
  }

  resolveSelection(current: ManagerStoreId | undefined): ManagerStoreId | undefined {
    const stores = this.stores();
    if (stores.length === 0) return current;

    if (current === undefined) {
      return stores[0].id;
    }
    const match = stores.find((store) => store.id.id === current.id);
    return match ? match.id : undefined;
  }
}
