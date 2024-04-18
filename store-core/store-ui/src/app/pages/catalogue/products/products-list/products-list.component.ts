import {Component, OnInit} from '@angular/core';
import {ProductService} from "../services/product.service";
import {NbDialogService, NbToastrService} from "@nebular/theme";
import {StoreService} from "../../../../shared/service/store.service";
import {TranslateService} from "@ngx-translate/core";
import {StorageService} from "../../../shared/services/storage.service";
import {Router} from "@angular/router";
import {Page} from "../../../shared/models/Page";
import {ColumnMode} from "@swimlane/ngx-datatable";
import {ShowcaseDialogComponent} from "../../../store-manager/shared/showcase-dialog/showcase-dialog.component";

@Component({
  selector: 'ngx-products-list',
  templateUrl: './products-list.component.html',
  styleUrls: ['./products-list.component.scss']
})
export class ProductsListComponent implements OnInit {
  page: Page = new Page();
  rows = [];
  loadingList = false;
  loading: boolean = false;
  perPage = 20;
  currentPage = 1;
  params = this.loadParams();
  protected readonly ColumnMode = ColumnMode;

  constructor(
    private productService: ProductService,
    private dialogService: NbDialogService,
    private storeService: StoreService,
    private translate: TranslateService,
    private storageService: StorageService,
    private toastr: NbToastrService,
    private router: Router
  ) {
  }

  //
  loadParams() {
    return {
      store: "",
      lang: this.storageService.getLanguage(),
      count: this.perPage,
      origin: "admin", //does not load attributes in listing
      page: 0
    };
  }

  ngOnInit() {
    this.translate.onLangChange.subscribe((lang) => {
      this.params.lang = this.storageService.getLanguage();
      this.getList();
    });
  }

  getList() {
    const startFrom = this.currentPage - 1;
    this.params.page = startFrom;
    this.loadingList = true;
    this.productService.getListOfProducts(this.params)
      .subscribe({
        next: (data) => {
          this.rows = data.products
          this.page.totalPages = data.totalPages
          this.page.totalElements = data.recordsTotal
          this.page.size = data.numbers
          this.loadingList = false;
        },
        error: (err) => {
          this.loadingList = false;
        }
      });
  }


  onSelectStore(e) {
    this.params.store = e.id;
    this.setPage({offset: 0});
  }

  setPage(pageInfo) {
    this.page.pageNumber = pageInfo.offset;
    this.getList();
  }


  onEdit(row: any) {
    //  updateRecord(event) {
    //    const product = {
    //      available: event.newData.available,
    //      price: event.newData.price,
    //      quantity: event.newData.quantity
    //    };
    //    event.confirm.resolve(event.newData);
    //    this.productService.updateProductFromTable(event.newData.id, product)
    //      .subscribe(res => {
    //        event.confirm.resolve(event.newData);
    //        this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_UPDATED'));
    //      }, error => {
    //        console.log(error.error.message);
    //      });
    //  }
    //
  }

  onDelete(row: any) {
    this.dialogService.open(ShowcaseDialogComponent, {})
      .onClose.subscribe(res => {
      if (res) {
        this.productService.deleteProduct(row.id)
          .subscribe(result => {
            this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_REMOVED'));
            this.getList();
            // event.confirm.resolve();
          });
      } else {
      }
    });
  }
}
