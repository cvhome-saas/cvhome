import {AfterViewInit, Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {NbToastrService} from "@nebular/theme";
import {ErrorService} from "../../../shared/services/error.service";
import {ColumnMode} from "@swimlane/ngx-datatable";
import {Page} from "../../../shared/models/Page";
import {ProductRelationshipService} from "./services/product-relationship.service";
import {zip} from "rxjs";
import {SelectedStoreService} from "../../../shared/services/selected-store.service";

@Component({
  selector: 'ngx-product-to-category',
  standalone: false,
  templateUrl: './product-related.component.html',
  styleUrls: ['./product-related.component.scss']
})
export class ProductRelatedComponent implements OnInit, AfterViewInit {

  product: string;
  perPageSize = 50;
  params: any;
  rows: Array<any> = [];
  store: string;
  page: Page = new Page();
  protected readonly ColumnMode = ColumnMode;

  constructor(
    private productRelationshipService: ProductRelationshipService,
    private activatedRoute: ActivatedRoute,
    private errorService: ErrorService,
    private toastr: NbToastrService,
    private translate: TranslateService,
    private selectedStoreService: SelectedStoreService,
  ) {
    this.params = {store: '', count: this.perPageSize, page: 0};
  }

  ngOnInit() {
  }

  ngAfterViewInit(): void {
    zip([this.selectedStoreService.current(), this.activatedRoute.parent.params]).subscribe({
      next: ([selectedStore, params]) => {
        this.params.store = selectedStore;
        this.product = params['code'];
        this.loadRelationships();
      }
    });
  }

  loadRelationships() {
    this.productRelationshipService.getRelationships(this.product)
      .subscribe({
        next: (group) => {
          this.rows = group.products || [];
          this.page.totalPages = 1;
          this.page.totalElements = this.rows.length;
          this.page.size = this.rows.length;
        },
        error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err),
      });
  }

  onItemSelect(item: any) {
    this.productRelationshipService.addProduct(this.product, item.id)
      .subscribe({
        next: () => {
          this.rows = [...this.rows, item];
          this.page.totalPages = 1;
          this.page.totalElements = this.rows.length;
          this.page.size = this.rows.length;
          this.toastr.success(this.translate.instant('PRODUCT_GROUP.PRODUCT_ADDED'));
        },
        error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err),
      });
  }

  onItemDeSelect(item: any) {
    this.productRelationshipService.removeProduct(this.product, item.id)
      .subscribe({
        next: () => {
          this.rows = this.rows.filter(it => it.id !== item.id);
          this.page.totalPages = 1;
          this.page.totalElements = this.rows.length;
          this.page.size = this.rows.length;
          this.toastr.success(this.translate.instant('PRODUCT_GROUP.PRODUCT_REMOVED'));
        },
        error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err),
      });
  }

  setPage(pageInfo: any) {
    this.page.pageNumber = pageInfo.offset;
  }
}
