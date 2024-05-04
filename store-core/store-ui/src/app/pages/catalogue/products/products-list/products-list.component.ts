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
  editing = {};
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

  updateValue(event, cell, rowIndex) {
    let newValue = undefined;
    console.log(event.target.type)
    if (event.target.type == 'checkbox') {
      newValue = event.target.checked
    } else if (event.target.type == 'number') {
      newValue = event.target.value
    }
    if (newValue != undefined) {
      this.rows[rowIndex][cell] = newValue;
      this.updateRecord(this.rows[rowIndex])
      this.rows = [...this.rows];
    }
    this.editing[rowIndex + '-' + cell] = false;

  }

  updateRecord(newData) {
    const product = {
      available: newData.available,
      price: newData.price,
      quantity: newData.quantity
    };
    this.productService.updateProductFromTable(this.params.store, newData.id, product)
      .subscribe({
        next: (data) => {
          this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_UPDATED'));
        },
        error: (err) => {
          this.toastr.danger(this.translate.instant('PRODUCT.PRODUCT_UPDATED_ERROR'));
        }
      })
  }

  onEdit(row: any) {
    this.router.navigate(['pages/catalogue/products/product/' + this.params.store + "-" + row.id]);
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
