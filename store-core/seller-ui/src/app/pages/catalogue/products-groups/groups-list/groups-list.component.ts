import {Component, OnInit} from '@angular/core';

import {NbDialogService} from '@nebular/theme';
import {ProductGroupsService} from '../services/product-groups.service';
import {Router} from '@angular/router';
import {ShowcaseDialogComponent} from "../../../shared/components/showcase-dialog/showcase-dialog.component";
import {ColumnMode} from "@swimlane/ngx-datatable";
import {ErrorService} from "../../../shared/services/error.service";
import {SelectedStoreService} from "../../../shared/services/selected-store.service";
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
  private recommendedCodes = ["NEWLY_ADDED", "RECOMMENDED", "FEATURED_ITEMS", "HOME_PAGE"];

  constructor(
    private dialogService: NbDialogService,
    private productGroupsService: ProductGroupsService,
    errorService: ErrorService,
    selectedStoreService: SelectedStoreService,
    private router: Router) {

    super(selectedStoreService, errorService);
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
      .pipe(map(it => {
        const missingCodes = this.recommendedCodes.filter(code => !it.content.some(e => e.code === code));
        missingCodes.forEach(code => {
          it.content.unshift({
            code: code
          })
        });
        return {
          ...it,
          "size": it.content.length,
          "totalElements": it.content.length,
          "content": it.content
        }
      }));
  }

  onEdit(row: any) {
    this.router.navigate([`/pages/catalogue/products-groups/update-products-group/${row.code}`]);
  }

  onCreate(event) {
    this.router.navigate([`/pages/catalogue/products-groups/create-products-group`], {queryParams: {code: event.code}});
  }

  onDelete(row: any) {
    this.dialogService.open(ShowcaseDialogComponent, {})
      .onClose.subscribe(res => {
      if (res) {
        this.productGroupsService.removeProductGroup(row.code)
          .subscribe(result => {
            this.trigger();
          }, err => {
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          });
      }
    });

  }
}
