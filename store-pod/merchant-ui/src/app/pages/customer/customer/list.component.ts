import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {CustomersService} from '../services/customer.service';
import {TranslateService} from '@ngx-translate/core';
import {ErrorService} from '../../../shared/services/error.service';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {SelectedStoreService} from "../../../shared/services/selected-store.service";
import {BaseTable, PageT, StorePageRequest} from "../../common/BaseTable";
import {Observable, of} from "rxjs";
import {map} from "rxjs/operators";

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
    translate: TranslateService,
    errorService: ErrorService,
    selectedStoreService: SelectedStoreService
  ) {
    super(selectedStoreService, translate, errorService)
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
      .pipe(map(it => {
        const mappedX = {
          content: it.customers,
          totalPages: it.totalPages,
          totalElements: it.recordsTotal,
          size: it.number,
          pageNumber: request.page
        };
        return mappedX;
      }));
  }

}
