import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ProductService} from '../services/product.service';
import {ErrorService} from "../../../../shared/services/error.service";
import {SelectedStoreService} from "../../../../shared/services/selected-store.service";
import {zip} from "rxjs";

@Component({
  selector: 'ngx-product-details',
  standalone: false,
  templateUrl: './product-details.component.html',
  styleUrls: ['./product-details.component.scss']
})
export class ProductDetailsComponent implements OnInit {
  product: any = {};
  store: string;
  action: any = 'save'
  uniqueCode: string;//identifier fromroute

  constructor(
    private activatedRoute: ActivatedRoute,
    private productService: ProductService,
    private errorService: ErrorService,
    private router: Router,
    private selectedStoreService: SelectedStoreService
  ) {
  }

  ngOnInit() {
    zip([this.selectedStoreService.current(), this.activatedRoute.params])
      .subscribe({
        next: ([selectedStore, params]) => {
          this.store = selectedStore;
          this.uniqueCode = params.code
          if (this.uniqueCode) {
            this.loadContent();
          }
        },
        error: (err) => {
        },
        complete: () => {
        }
      });

  }

  route(link) {
    this.router.navigate(['pages/catalogue/products/' + this.product.sku + '/' + link]);
  }

  loadContent() {
    this.productService.getProductDefinitionById(this.store, this.uniqueCode)
      .subscribe(res => {
        this.product = res;
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }
}
