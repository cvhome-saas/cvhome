import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {DomSanitizer} from '@angular/platform-browser';

import {CategoryService} from '../services/category.service';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {ShowcaseDialogComponent} from "../../../../shared/components/showcase-dialog/showcase-dialog.component";
import {ColumnMode} from "@swimlane/ngx-datatable";
import {ErrorService} from "../../../../shared/services/error.service";
import {SelectedStoreService} from "../../../../shared/services/selected-store.service";
import {BaseTable, PageT, StorePageRequest} from "../../../common/BaseTable";
import {Observable, of} from "rxjs";
import {map} from "rxjs/operators";

@Component({
  selector: 'ngx-categories-list',
  standalone: false,
  templateUrl: './categories-list.component.html',
  styleUrls: ['./categories-list.component.scss']
})
export class CategoriesListComponent extends BaseTable<any> implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  private isInitialized: boolean = false;

  constructor(
    private categoryService: CategoryService,
    private router: Router,
    private _sanitizer: DomSanitizer,
    selectedStoreService: SelectedStoreService,
    errorService: ErrorService,
    private dialogService: NbDialogService,
    translate: TranslateService,
    private toastr: NbToastrService
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
    return this.categoryService.getListOfCategories(request)
  }

  onEdit(event) {
    this.router.navigate(['pages/catalogue/categories/category/', this.params.store + "-" + event.id]);
  }

  onDelete(event) {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      }
    })
      .onClose.subscribe(res => {
      if (res) {
        this.categoryService.deleteCategory(event.id, event.store)
          .subscribe(data => {
            this.toastr.success(this.translate.instant('CATEGORY_FORM.CATEGORY_REMOVED'));
            this.trigger();
          }, err => {
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          });
      }
    });
  }

  createCategory() {
    this.router.navigate(['pages/catalogue/categories/create-category']);
  }

}
