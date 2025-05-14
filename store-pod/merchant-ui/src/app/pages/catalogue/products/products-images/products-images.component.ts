import {AfterViewInit, Component, EventEmitter, OnInit, Output} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {ProductImageService} from '../services/product-image.service';
import {Location} from '@angular/common';
import {ErrorService} from "../../../../shared/services/error.service";
import {zip} from "rxjs";
import {SelectedStoreService} from "../../../../shared/services/selected-store.service";

@Component({
  selector: 'ngx-products-images',
  standalone: false,
  templateUrl: './products-images.component.html',
  styleUrls: ['./products-images.component.css']
})
export class ProductsImagesComponent implements OnInit, AfterViewInit {

  // product: any;
  images: any;
  id: any;
  loaded = false;
  loading = false;
  store: string;
  uniqueCode: string;

  @Output() refreshProduct = new EventEmitter<string>();


  // loading = true;
  addImageUrl = '';
  removeImageUrl = '';

  constructor(
    private toastr: NbToastrService,
    private translate: TranslateService,
    private productImageService: ProductImageService,
    private location: Location,
    private errorService: ErrorService,
    private activatedRoute: ActivatedRoute,
    private selectedStoreService: SelectedStoreService
  ) {


  }

  ngOnInit() {
    zip([this.selectedStoreService.current(), this.activatedRoute.parent.params]).subscribe({
      next: (([selectedStore, params]) => {
        this.store = selectedStore
        this.uniqueCode = params['code'];
        this.load();
        this.addImageUrl = this.productImageService.addImageUrl(this.store, this.uniqueCode)
        this.removeImageUrl = this.productImageService.removeImageUrl(this.store, this.uniqueCode)
      })
    })

  }

  load() {
    this.loading = true;
    this.productImageService.getImages(this.store, this.uniqueCode)
      .subscribe(res => {
        this.images = res;
        this.loading = false;
        this.loaded = true;
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
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
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
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
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
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

  ngAfterViewInit(): void {
    if (this.location.path().includes('images')) {
      const el: HTMLElement = document.getElementById('tabs');
      el.scrollIntoView();
    }
  }
}
