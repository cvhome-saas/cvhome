import {DestroyRef, inject, Injectable, signal} from '@angular/core';
import {CustomersService} from 'seller-core/customers';
import {SelectedStoreService} from 'seller-core';
import {TableStateService} from 'seller-core';
import {ApiErrorService} from 'seller-core';
import {StorePageRequest} from 'seller-core';
import {PageEvent} from '@swimlane/ngx-datatable';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ReadableCustomer} from 'seller-core/orders';

@Injectable()
export class CustomerListFacade {
  private readonly customersService = inject(CustomersService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly apiErrors = inject(ApiErrorService);
  readonly tableState = inject(TableStateService<ReadableCustomer, StorePageRequest>);

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
        error: (err) => this.apiErrors.notify(err)
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
        this.apiErrors.notify(err);
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
