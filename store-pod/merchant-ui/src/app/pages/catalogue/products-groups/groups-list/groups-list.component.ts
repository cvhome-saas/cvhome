import {Component, OnInit} from '@angular/core';

import {NbDialogService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {ProductGroupsService} from '../services/product-groups.service';
import {Router} from '@angular/router';
import {ShowcaseDialogComponent} from "../../../../shared/components/showcase-dialog/showcase-dialog.component";
import {ColumnMode} from "@swimlane/ngx-datatable";
import {ErrorService} from "../../../../shared/services/error.service";
import {SelectedStoreService} from "../../../../shared/services/selected-store.service";
import {BaseTable, PageT, StorePageRequest} from "../../../common/BaseTable";
import {Observable, of} from "rxjs";
import {map} from "rxjs/operators";

@Component({
  selector: 'ngx-groups-list',
  standalone: false,
  templateUrl: './groups-list.component.html',
  styleUrls: ['./groups-list.component.scss']
})
export class GroupsListComponent extends BaseTable<any> implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  private isInitialized: boolean = false;

  constructor(
    private dialogService: NbDialogService,
    private productGroupsService: ProductGroupsService,
    errorService: ErrorService,
    selectedStoreService: SelectedStoreService,
    private router: Router) {

    super(selectedStoreService,  errorService);
  }

  ngOnInit(): void {
    this.isInitialized = true;
    this.trigger();
  }

  override list(request: StorePageRequest): Observable<PageT<any>> {
    if (!super.params.store || !this.isInitialized) {
      return of();
    }
    return this.productGroupsService.getListOfProductGroups(request)
  }

  onEdit(row: any) {
    this.router.navigate([`/pages/catalogue/products-groups/update-products-group/${row.code}`]);
  }

  onDelete(row: any) {
    this.dialogService.open(ShowcaseDialogComponent, {})
      .onClose.subscribe(res => {
      if (res) {
        this.productGroupsService.removeProductGroup(this.params.store, row.code)
          .subscribe(result => {
            this.trigger();
          }, err => {
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          });
      }
    });

  }
}
