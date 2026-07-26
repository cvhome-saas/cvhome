import {Component, OnInit, inject} from '@angular/core';
import {OrderListFacade} from '../facades/order-list.facade';
import {TableStateService} from '../../shared/table/table-state.service';
import {ColumnMode} from "@swimlane/ngx-datatable";

@Component({
  selector: 'ngx-order-list',
  standalone: false,
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.scss'],
  providers: [OrderListFacade, TableStateService]
})
export class OrderListComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  perPageSize = 10;

  protected readonly facade = inject(OrderListFacade);

  filter = { ...this.facade.filter() };

  get tableState() {
    return this.facade.tableState;
  }

  ngOnInit(): void {
    this.facade.init();
  }

  onPageChange(event: any): void {
    this.facade.onPageChange(event);
  }

  onFilterChange(): void {
    this.facade.onFilterChange(this.filter);
  }

  resetFilters(): void {
    this.filter = {phone: '', email: '', name: '', status: ''};
    this.facade.onFilterChange(this.filter);
  }
}
