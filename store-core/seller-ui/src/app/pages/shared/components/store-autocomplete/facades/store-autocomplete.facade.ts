import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ManagerStoreService} from 'seller-core';
import {ApiErrorService} from 'seller-core';
import {ManagerStore} from 'seller-core';

@Injectable()
export class StoreAutocompleteFacade {
  private readonly storeService = inject(ManagerStoreService);
  private readonly apiErrors = inject(ApiErrorService);

  readonly stores = signal<ManagerStore[]>([]);

  init(destroyRef: DestroyRef): void {
    this.storeService.list()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: (page) => this.stores.set(page.content),
        error: (err) => this.apiErrors.notify(err)
      });
  }

  resolveSelection(current: string | undefined): string | undefined {
    const stores = this.stores();
    if (stores.length === 0) return current;

    if (current === undefined) {
      return stores[0].id;
    }
    const match = stores.find((store) => store.id === current);
    return match ? match.id : undefined;
  }
}
