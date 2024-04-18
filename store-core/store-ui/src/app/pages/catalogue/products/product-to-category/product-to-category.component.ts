import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';

import {CategoryService} from '../../categories/services/category.service';
import {ProductService} from '../services/product.service';
import {TranslateService} from '@ngx-translate/core';
import {StorageService} from '../../../shared/services/storage.service';
import {forkJoin} from 'rxjs';
import {Location} from '@angular/common';
import {NbToastrService} from "@nebular/theme";

@Component({
  selector: 'ngx-product-to-category',
  templateUrl: './product-to-category.component.html',
  styleUrls: ['./product-to-category.component.scss']
})
export class ProductToCategoryComponent implements OnInit {

  loading = false;
  uniqueCode: string;
  perPage: number = 50;//ideally display all category
  currentPage: number = 1;

  categories = [];
  selectedItems: string[] = [];

  params = this.loadParams();

  constructor(
    private translate: TranslateService,
    private categoryService: CategoryService,
    private storageService: StorageService,
    private productService: ProductService,
    private location: Location,
    private router: Router,
    private toastr: NbToastrService,
    private activatedRoute: ActivatedRoute) {

  }

  loadParams() {
    return {
      store: "",
      count: this.perPage,
      page: 0,
      lang: localStorage.getItem('lang')
    };
  }

  ngOnInit() {
    this.activatedRoute.parent.params.subscribe(it => {
      const split: string[] = it['code'].split("-");
      this.params.store = split[0];
      if (split.length == 2 && split[1] != "") {
        this.uniqueCode = split[1];
        this.load();
      }
    });

    //specify add image url to image component
    let el = document.getElementById('tabs');
    el.scrollIntoView();
  }

  private load() {
    this.loading = true;

    const p$ = this.categoryService.getCategoryByProductId(this.uniqueCode, this.params.store)
    const c$ = this.categoryService.getListOfCategories0(this.params)

    forkJoin([p$, c$])
      .subscribe(([p$, c$]) => {
        this.selectedItems = [];
        this.categories = [];
        p$.categories.forEach((data) => {
          this.selectedItems.push(data.id)
        });
        c$.categories.forEach((value) => {
          this.categories.push({'id': value.id, 'name': value.description.name})
        })
        this.loading = false;
      });

  }

  change($event: string[]) {
    const oldItems = this.selectedItems;
    const newItems = $event;
    if (newItems.length > oldItems.length) {
      // find new item
      this.onItemSelect(newItems.filter(it => !oldItems.includes(it))[0]);

    }
    if (oldItems.length > newItems.length) {
      // find removed item
      this.onItemDeSelect(oldItems.filter(it => !newItems.includes(it))[0]);
    }

  }

  onItemSelect(id: string) {
    this.addProductToCategory(this.params.store, this.uniqueCode, id)
  }

  onItemDeSelect(id: string) {
    this.removeProductFromCategory(this.params.store, this.uniqueCode, id)
  }

  addProductToCategory(store, productId, groupCode) {
    this.productService.addProductToCategory(store, productId, groupCode)
      .subscribe({
        next: () => {
          this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_TO_CATEGORY_ADDED'));
          this.load()
        },
        error: (err) => {
          this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_TO_CATEGORY_ADDED_ERROR'));
        }
      });
  }

  removeProductFromCategory(store, productId, groupCode) {
    this.productService.removeProductFromCategory(store, productId, groupCode)
      .subscribe({
        next: () => {
          this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_TO_CATEGORY_REMOVED'));
          this.load()
        },
        error: (err) => {
          this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_TO_CATEGORY_REMOVED_ERROR'));
        }
      });
  }


}
