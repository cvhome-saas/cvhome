import {Injectable} from '@angular/core';
import {CustomersService} from '../services/customer.service';
import {SelectedStoreService} from "../../shared/services/selected-store.service";
import {TableStateService} from "../../shared/table/table-state.service";
import {Observable, tap} from "rxjs";
import {StorePageRequest, PageT} from "../../common/BaseTable";
import {ErrorService} from "../../shared/services/error.service";
import {PageEvent} from "@swimlane/ngx-datatable";

@Injectable({providedIn: 'root'})
export class CustomerListFacade {
  constructor(
    private customersService: CustomersService,
    private selectedStoreService: SelectedStoreService,
    private tableState: TableStateService<any>,
    private errorService: ErrorService
  ) {}

  init(): void {
    const store = this.selectedStoreService.currentSelectedStore();
    if (store) {
      const params = this.tableState.params();
      const request: StorePageRequest = { ...params, store: store.id.id };
      this.loadCustomers(request).subscribe(page => this.tableState.setPage(page));
    }
  }

  onPageChange(event: PageEvent): void {
    this.tableState.patchParams({ page: event.offset });
    this.init();
  }

  loadCustomers(request: StorePageRequest): Observable<PageT<any>> {
    this.tableState.setLoading(true);
    return this.customersService.getCustomers(request).pipe(
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
