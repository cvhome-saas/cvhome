import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ProductService} from '../services/product.service';
import {ErrorService} from "../../../../shared/services/error.service";

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
  ) {
  }

  ngOnInit() {
    this.activatedRoute.params.subscribe(it => {
      const split: string[] = it['code'].split("-");
      this.store = split[0];
      if (split.length == 2 && split[1] != "") {
        this.uniqueCode = split[1];

        this.productService.getProductDefinitionById(this.store, this.uniqueCode)
          .subscribe(res => {
            this.product = res;
          }, err => {
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          });
      }
    });
  }

  route(link) {
    this.router.navigate(['pages/catalogue/products/' + this.product.sku + '/' + link]);
  }

}
