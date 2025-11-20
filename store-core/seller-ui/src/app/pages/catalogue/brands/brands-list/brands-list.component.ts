import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';

import {BrandService} from '../services/brand.service';
import {TranslateService} from '@ngx-translate/core';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {ShowcaseDialogComponent} from "../../../../shared/components/showcase-dialog/showcase-dialog.component";
import {ColumnMode} from "@swimlane/ngx-datatable";
import {ErrorService} from "../../../../shared/services/error.service";
import {SelectedStoreService} from "../../../../shared/services/selected-store.service";
import {BaseTable, PageT, StorePageRequest} from "../../../common/BaseTable";
import {Observable, of} from "rxjs";

@Component({
  selector: 'ngx-brands-list',
  standalone: false,
  templateUrl: './brands-list.component.html',
  styleUrls: ['./brands-list.component.scss']
})
export class BrandsListComponent extends BaseTable<any> implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  private isInitialized: boolean = false;

  constructor(
    private brandService: BrandService,
    private router: Router,
    selectedStoreService: SelectedStoreService,
    private dialogService: NbDialogService,
    private translate: TranslateService,
    errorService: ErrorService,
    private toastr: NbToastrService,
  ) {
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
    return this.brandService.getListOfBrands(request)
  }


  onEdit(row: any) {
    this.router.navigate(['pages/catalogue/brands/brand/', row.id]);
  }

  onDelete(row: any) {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      }
    })
      .onClose.subscribe(res => {
      if (res) {
        this.brandService.deleteBrand(this.params.store, row.id)
          .subscribe(data => {
            this.toastr.success(this.translate.instant('BRAND.BRAND_REMOVED'));
            super.trigger();
          }, err => {
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          });
      }
    });

  }
}
