import {AfterViewInit, Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {NbToastrService} from "@nebular/theme";
import {ErrorService} from "../../../../shared/services/error.service";
import {ColumnMode} from "@swimlane/ngx-datatable";
import {Page} from "../../../../shared/models/Page";
import {ProductGroupsService} from "../../products-groups/services/product-groups.service";

@Component({
  selector: 'ngx-product-to-category',
  standalone: false,
  templateUrl: './product-related.component.html',
  styleUrls: ['./product-related.component.scss']
})
export class ProductRelatedComponent implements OnInit, AfterViewInit {

  product: string;
  perPage = 50;
  params: any;
  products: Array<any> = [];
  rows: Array<any> = [];
  store: string;
  action: string;
  page: Page = new Page();
  protected readonly ColumnMode = ColumnMode;

  constructor(
    private productGroupsService: ProductGroupsService,
    private activatedRoute: ActivatedRoute,
    private errorService: ErrorService,
    private toastr: NbToastrService,
    private translate: TranslateService) {
    this.params = this.loadParams();
  }

  ngOnInit() {
  }


  ngAfterViewInit(): void {
    this.activatedRoute.parent.params.subscribe(it => {
      const split: string[] = it['code'].split("-");
      this.params.store = split[0];
      if (split.length == 2 && split[1] != "") {
        this.product = split[1];
        this.getProductByCode();
      }
    });

  }


  loadParams() {
    return {
      store: "",
      count: this.perPage,
      page: 0
    };
  }

  getProductByCode() {
    this.productGroupsService.getRelatedProduct(this.product, this.params)
      .subscribe(it => {
        this.rows = it.content;
        this.page.totalPages = 1
        this.page.totalElements = this.rows.length
        this.page.size = this.rows.length
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }

  onItemSelect(item: any) {
    this.productGroupsService.addProductToRelated(this.params.store, this.product, item.id)
      .subscribe({
        next: (data) => {
          this.rows.push(item)
          this.rows = this.rows.map(it => it);
          this.page.totalPages = 1
          this.page.totalElements = this.rows.length
          this.page.size = this.rows.length
          this.toastr.success(this.translate.instant('PRODUCT_GROUP.PRODUCT_ADDED'))
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        },
      });
  }

  onItemDeSelect(item) {
    this.productGroupsService.removeProductFromRelated(this.params.store, this.product, item.id)
      .subscribe({
        next: (data) => {
          this.rows = this.rows.filter(it => it.id != item.id)
          this.page.totalPages = 1
          this.page.totalElements = this.rows.length
          this.page.size = this.rows.length
          this.toastr.success(this.translate.instant('PRODUCT_GROUP.PRODUCT_REMOVED'))
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        },
      });
  }

  setPage(pageInfo) {
    this.page.pageNumber = pageInfo.offset;
    this.getProductByCode();
  }


}
