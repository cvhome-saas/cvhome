import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {CustomersService} from '../services/customer.service';
import {ErrorService} from '../../shared/services/error.service';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {SelectedStoreService} from "../../shared/services/selected-store.service";
import {BaseTable, PageT, StorePageRequest} from "../../common/BaseTable";
import {Observable, of} from "rxjs";

@Component({
  selector: 'ngx-list',
  standalone: false,
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent extends BaseTable<any> implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  private isInitialized: boolean = false;

  constructor(
    private customersService: CustomersService,
    public router: Router,
    errorService: ErrorService,
    selectedStoreService: SelectedStoreService
  ) {
    super(selectedStoreService,  errorService)
  }

  ngOnInit(): void {
    this.isInitialized = true;
    this.trigger();
  }

  override list(request: StorePageRequest): Observable<PageT<any>> {
    if (!super.params.store || !this.isInitialized) {
      return of();
    }
    return this.customersService.getCustomers(request)
  }

}
