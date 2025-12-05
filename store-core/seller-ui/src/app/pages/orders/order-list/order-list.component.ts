import { Component, OnInit } from '@angular/core';
import { OrdersService } from "../services/orders.service";
import { ColumnMode } from "@swimlane/ngx-datatable";
import { ErrorService } from "../../shared/services/error.service";
import { SelectedStoreService } from "../../shared/services/selected-store.service";
import { BaseTable, PageT, StorePageRequest } from "../../common/BaseTable";
import { Observable, of } from "rxjs";

export interface OrderFilterPageRequest extends StorePageRequest {
  phone?: string;
  email?: string;
  name?: string;
  status?: string;
}

@Component({
  selector: 'ngx-order-list',
  standalone: false,
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.scss']
})
export class OrderListComponent extends BaseTable<any> implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  private isInitialized: boolean = false;

  // Filter object for tracking user inputs
  filter = {
    phone: '',
    email: '',
    name: '',
    status: ''
  };

  constructor(
    private ordersService: OrdersService,
    errorService: ErrorService,
    selectedStoreService: SelectedStoreService
  ) {
    super(selectedStoreService, errorService)
  }

  ngOnInit(): void {
    this.isInitialized = true;
    this.trigger();
  }

  override list(request: OrderFilterPageRequest): Observable<PageT<any>> {
    if (!super.params.store || !this.isInitialized) {
      return of();
    }
    Object.assign(request, this.filter);
    return this.ordersService.getOrders(request);
  }
  onFilterChange(): void {
    this.trigger();
  }

  resetFilters(): void {
    this.filter = { phone: '', email: '', name: '', status: '' };
    this.trigger(); // Refresh the list
  }

}
