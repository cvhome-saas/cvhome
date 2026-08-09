import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {OrdersService} from 'seller-core/orders';
import {SelectedStoreService} from "seller-core";
import {TableStateService} from "seller-core";
import {Observable, tap} from "rxjs";
import {StorePageRequest, PageT} from "seller-core";
import {PageEvent} from "@swimlane/ngx-datatable";
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ReadableOrder} from "seller-core/orders";
import {ApiErrorService} from "seller-core";

export interface OrderFilterPageRequest extends StorePageRequest {
  phone?: string;
  email?: string;
  name?: string;
  status?: string;
}

@Injectable()
export class OrderListFacade {
  private readonly ordersService = inject(OrdersService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly apiErrors = inject(ApiErrorService);
  readonly tableState = inject(TableStateService<ReadableOrder, OrderFilterPageRequest>);

  readonly store = signal<string>('');

  init(destroyRef: DestroyRef): void {
    this.selectedStoreService.current()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: (store) => {
          this.store.set(store || '');
          if (store) {
            this.refresh();
          }
        },
        // Was errorService.handleError, i.e. a bare console.error: the seller saw an empty list
        // and no reason for it. GlobalErrorHandler still logs; this says why on screen.
        error: (err) => this.apiErrors.notify(err)
      });
  }

  onPageChange(event: PageEvent): void {
    this.tableState.patchParams({page: event.offset} as OrderFilterPageRequest);
    this.refresh();
  }

  onFilterChange(filters: Partial<OrderFilterPageRequest>): void {
    this.tableState.patchParams(filters as OrderFilterPageRequest);
    this.refresh();
  }

  filter(): Partial<OrderFilterPageRequest> {
    const params = this.tableState.params();
    return {
      phone: params.phone || '',
      email: params.email || '',
      name: params.name || '',
      status: params.status || ''
    };
  }

  private refresh(): void {
    const store = this.store();
    if (!store) return;

    const request: OrderFilterPageRequest = {...this.tableState.params(), store};
    this.loadOrders(request).subscribe(page => this.tableState.setPage(page));
  }

  private loadOrders(request: StorePageRequest): Observable<PageT<ReadableOrder>> {
    this.tableState.setLoading(true);
    return this.ordersService.getOrders(request).pipe(
      tap({
        next: () => this.tableState.setLoading(false),
        error: () => {
          this.tableState.setLoading(false);
        }
      })
    );
  }
}
