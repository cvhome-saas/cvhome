import {Component, OnInit} from '@angular/core';
import {OrdersService} from "../services/orders.service";
import {ColumnMode} from "@swimlane/ngx-datatable";
import {ErrorService} from "../../../shared/services/error.service";
import {SelectedStoreService} from "../../../shared/services/selected-store.service";
import {BaseTable, PageT, StorePageRequest} from "../../common/BaseTable";
import {Observable, of} from "rxjs";

@Component({
  selector: 'ngx-order-list',
  standalone: false,
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.scss']
})
export class OrderListComponent extends BaseTable<any> implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  private isInitialized: boolean = false;

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

  override list(request: StorePageRequest): Observable<PageT<any>> {
    if (!super.params.store || !this.isInitialized) {
      return of();
    }
    return this.ordersService.getOrders(request)
  }

}
