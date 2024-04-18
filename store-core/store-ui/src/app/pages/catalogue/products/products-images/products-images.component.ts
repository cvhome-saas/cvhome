import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {ProductImageService} from '../services/product-image.service';
import {ProductService} from '../services/product.service';
import {Location} from '@angular/common';

@Component({
  selector: 'ngx-products-images',
  templateUrl: './products-images.component.html',
  styleUrls: ['./products-images.component.css']
})
export class ProductsImagesComponent implements OnInit {

  // product: any;
  images: any;
  id: any;
  loaded = false;
  loading = false;
  store: string;
  uniqueCode: string;

  @Output() refreshProduct = new EventEmitter<string>();


  // loading = true;
  addImageUrlComponent = '';//add image url to be used by uploader

  constructor(
    private toastr: NbToastrService,
    private translate: TranslateService,
    private productImageService: ProductImageService,
    private productService: ProductService,
    private location: Location,
    private router: Router,
    private activatedRoute: ActivatedRoute) {


  }

  ngOnInit() {
    this.activatedRoute.parent.params.subscribe(it => {
      const split: string[] = it['code'].split("-");
      this.store = split[0];
      if (split.length == 2 && split[1] != "") {
        this.uniqueCode = split[1];
        this.load();
      }
    });


    // this.id = this.productService.getProductIdRoute(this.router, this.location);
    // this.load();
    // //specify add image url to image component
    // this.addImageUrlComponent = this.productImageService.addImageUrl(this.id);
    // //this only happens when /images, not when default
    // if (this.location.path().includes('images')) {
    //   let el = document.getElementById('tabs');
    //   el.scrollIntoView();
    // }
  }

  load() {
    this.loading = true;
    this.productImageService.getImages(this.store, this.uniqueCode)
      .subscribe(res => {
        this.images = res;
        this.loading = false;
        this.loaded = true;
      });
  }

  /** image component */
  removeImage(event) {
    this.loading = true;
    this.productImageService.removeImage(this.id, event)
      .subscribe({
        next: (data) => {
          this.load();
          this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_UPDATED'));
        },
        error: (err) => {
          this.toastr.danger(err.error.message);
          this.loading = false;
        },
      });
  }

  updateImage(event) {
    this.loading = true;
    this.productImageService.updateImage(this.id, event)
      .subscribe({
        next: (data) => {
          this.load();
        },
        error: (err) => {
          this.toastr.danger(err.error.message);
          this.loading = false;

        },
      });

  }

  errorImage(event) {
    this.toastr.danger(this.translate.instant('COMMON.' + event));
  }

  addedImage(event) {
    this.load();
    this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_UPDATED'));

  }

  fileAdded(e) {
    this.load();
  }
}
