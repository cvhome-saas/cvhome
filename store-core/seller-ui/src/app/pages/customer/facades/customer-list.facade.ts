import {DestroyRef, inject, Injectable, signal} from '@angular/core';
import {CustomersService} from '../services/customer.service';
import {SelectedStoreService} from '../../shared/services/selected-store.service';
import {TableStateService} from '../../shared/table/table-state.service';
import {ErrorService} from '../../shared/services/error.service';
import {StorePageRequest} from '../../shared/table/table.types';
import {PageEvent} from '@swimlane/ngx-datatable';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';

@Injectable()
export class CustomerListFacade {
  private readonly customersService = inject(CustomersService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly errorService = inject(ErrorService);
  readonly tableState = inject(TableStateService<any, StorePageRequest>);

  readonly store = signal<string>('');

  init(destroyRef: DestroyRef): void {
    this.selectedStoreService.current()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: (store) => {
          this.store.set(store || '');
          if (store) {
            this.loadPage();
          }
        },
        error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
      });
  }

  loadPage(): void {
    const currentStore = this.store();
    if (!currentStore) return;

    this.tableState.setLoading(true);
    const request: StorePageRequest = {...this.tableState.params(), store: currentStore};

    this.customersService.getCustomers(request).subscribe({
      next: (page) => {
        this.tableState.setPage(page);
        this.tableState.setLoading(false);
      },
      error: (err) => {
        this.tableState.setLoading(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.tableState.setParams({
      ...this.tableState.params(),
      page: event.offset
    });
    this.loadPage();
  }
}
