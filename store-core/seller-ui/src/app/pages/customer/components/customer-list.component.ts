import {Component, OnInit} from '@angular/core';
import {CustomerListFacade} from '../facades/customer-list.facade';
import {TableStateService} from '../../shared/table/table-state.service';
import {ColumnMode} from "@swimlane/ngx-datatable";

@Component({
  selector: 'ngx-list',
  standalone: false,
  templateUrl: './customer-list.component.html',
  styleUrls: ['./customer-list.component.scss'],
  providers: [CustomerListFacade, TableStateService]
})
export class CustomerListComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  perPageSize = 10;

  constructor(
    private facade: CustomerListFacade,
    public tableState: TableStateService<any>
  ) {}

  ngOnInit(): void {
    this.facade.init();
  }

  onPageChange(event: any): void {
    this.facade.onPageChange(event);
  }
}
