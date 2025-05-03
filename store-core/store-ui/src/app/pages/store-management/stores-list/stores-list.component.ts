import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';

import {StoreService} from '../services/store.service';
import {TranslateService} from '@ngx-translate/core';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {ShowcaseDialogComponent} from "../../../shared/components/showcase-dialog/showcase-dialog.component";
import {ColumnMode} from "@swimlane/ngx-datatable";
import {ErrorService} from "../../../shared/services/error.service";
import {BaseTable, PageT, StorePageRequest} from "../../common/BaseTable";
import {Observable, of} from "rxjs";
import {map} from "rxjs/operators";

@Component({
  selector: 'ngx-stores-list',
  standalone: false,
  templateUrl: './stores-list.component.html',
  styleUrls: ['./stores-list.component.scss']
})
export class StoresListComponent extends BaseTable<any> implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  private isInitialized: boolean = false;

  constructor(
    private storeService: StoreService,
    private router: Router,
    private toastr: NbToastrService,
    private dialogService: NbDialogService,
    translate: TranslateService,
    errorService: ErrorService
  ) {
    super(null, translate, errorService);
  }

  ngOnInit() {
    this.isInitialized = true;
    this.trigger();
  }

  override list(request: StorePageRequest): Observable<PageT<any>> {
    if (!this.isInitialized) {
      return of();
    }
    return this.storeService.getListOfStores(this.params)
  }


  onEdit(row) {
    this.router.navigate(['pages/store-management/store/', row.id]);
  }

  onDelete(row) {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      }
    })
      .onClose.subscribe(res => {
      if (res) {
        this.storeService.deleteStore(row.id)
          .subscribe(data => {
            this.toastr.success(this.translate.instant('USER_FORM.USER_REMOVED'));
            this.trigger();
          }, err => {
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          });
      }
    });

  }

}
