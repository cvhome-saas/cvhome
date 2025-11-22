import {Component, OnInit} from '@angular/core';
import {ProductService} from "../services/product.service";
import {NbDialogService, NbToastrService} from "@nebular/theme";
import {TranslateService} from "@ngx-translate/core";
import {Router} from "@angular/router";
import {ColumnMode} from "@swimlane/ngx-datatable";
import {ShowcaseDialogComponent} from "../../../shared/components/showcase-dialog/showcase-dialog.component";
import {ErrorService} from "../../../shared/services/error.service";
import {SelectedStoreService} from "../../../shared/services/selected-store.service";
import {BaseTable, PageT, StorePageRequest} from "../../../common/BaseTable";
import {Observable, of} from "rxjs";

@Component({
  selector: 'ngx-products-list',
  standalone: false,
  templateUrl: './products-list.component.html',
  styleUrls: ['./products-list.component.scss']
})
export class ProductsListComponent extends BaseTable<any> implements OnInit {
  editing = {};
  protected readonly ColumnMode = ColumnMode;
  private isInitialized: boolean = false;

  constructor(
    private productService: ProductService,
    private dialogService: NbDialogService,
    errorService: ErrorService,
    private translate: TranslateService,
    private toastr: NbToastrService,
    private router: Router,
    selectedStoreService: SelectedStoreService
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
    return this.productService.getListOfProducts(request)
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
      this.page.content[rowIndex][cell] = newValue;
      this.updateRecord(this.page.content[rowIndex])
      this.page.content = [...this.page.content];
    }
    this.editing[rowIndex + '-' + cell] = false;

  }

  updateRecord(newData) {
    const product = {
      available: newData.available,
      price: newData.price,
      quantity: newData.quantity
    };
    this.productService.updateProductFromTable( newData.id, product)
      .subscribe({
        next: (data) => {
          this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_UPDATED'));
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      })
  }

  onEdit(row: any) {
    this.router.navigate(['pages/catalogue/products/product/' + row.id]);
  }

  onDelete(row: any) {
    this.dialogService.open(ShowcaseDialogComponent, {})
      .onClose.subscribe(res => {
      if (res) {
        this.productService.deleteProduct(row.id)
          .subscribe(result => {
            this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_REMOVED'));
            this.trigger();
            // event.confirm.resolve();
          }, err => {
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          });
      } else {
      }
    });
  }
}
