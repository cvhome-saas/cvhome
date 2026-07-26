import {Injectable} from '@angular/core';
import {OrdersService} from '../services/orders.service';
import {SelectedStoreService} from "../../shared/services/selected-store.service";
import {TableStateService} from "../../shared/table/table-state.service";
import {Observable, tap} from "rxjs";
import {StorePageRequest, PageT} from "../../common/BaseTable";
import {ErrorService} from "../../shared/services/error.service";
import {PageEvent} from "@swimlane/ngx-datatable";

export interface OrderFilterPageRequest extends StorePageRequest {
  phone?: string;
  email?: string;
  name?: string;
  status?: string;
}

@Injectable({providedIn: 'root'})
export class OrderListFacade {
  constructor(
    private readonly ordersService: OrdersService,
    private readonly selectedStoreService: SelectedStoreService,
    public tableState: TableStateService<any, OrderFilterPageRequest>,
    private readonly errorService: ErrorService
  ) {}

  init(): void {
    const store = this.selectedStoreService.currentSelectedStore();
    if (store) {
      const params = this.tableState.params();
      const request: StorePageRequest = { ...params, store: store.id.id };
      this.loadOrders(request).subscribe(page => this.tableState.setPage(page));
    }
  }

  onPageChange(event: PageEvent): void {
    this.tableState.patchParams({ page: event.offset } as OrderFilterPageRequest);
    this.init();
  }

  onFilterChange(filters: any): void {
    this.tableState.patchParams(filters as OrderFilterPageRequest);
    this.init();
  }

  filter(): any {
    const params = this.tableState.params();
    return {
      phone: params.phone || '',
      email: params.email || '',
      name: params.name || '',
      status: params.status || ''
    };
  }

  loadOrders(request: StorePageRequest): Observable<PageT<any>> {
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
