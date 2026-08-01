import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {OrdersService} from '../services/orders.service';
import {SelectedStoreService} from "../../shared/services/selected-store.service";
import {TableStateService} from "../../shared/table/table-state.service";
import {Observable, tap} from "rxjs";
import {StorePageRequest, PageT} from "../../shared/table/table.types";
import {ErrorService} from "../../shared/services/error.service";
import {PageEvent} from "@swimlane/ngx-datatable";
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ReadableOrder} from "../models/order.model";

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
  private readonly errorService = inject(ErrorService);
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
        error: (err) => this.errorService.handleError(err)
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
        error: (err) => {
          this.tableState.setLoading(false);
          this.errorService.handleError(err);
        }
      })
    );
  }
}
