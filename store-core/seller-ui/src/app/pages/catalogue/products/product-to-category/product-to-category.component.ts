import {AfterViewInit, Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';

import {CategoryService} from '../../categories/services/category.service';
import {ProductService} from '../services/product.service';
import {TranslateService} from '@ngx-translate/core';
import {forkJoin, zip} from 'rxjs';
import {NbToastrService} from "@nebular/theme";
import {ErrorService} from "../../../../shared/services/error.service";
import {SelectedStoreService} from "../../../../shared/services/selected-store.service";

@Component({
  selector: 'ngx-product-to-category',
  standalone:false,
  templateUrl: './product-to-category.component.html',
  styleUrls: ['./product-to-category.component.scss']
})
export class ProductToCategoryComponent implements OnInit, AfterViewInit {

  loading: boolean = false;
  uniqueCode: string;
  perPage: number = 50;//ideally display all category

  categories: any[] = [];
  selectedItems: string[] = [];

  params :any;

  constructor(
    private translate: TranslateService,
    private categoryService: CategoryService,
    private errorService: ErrorService,
    private productService: ProductService,
    private toastr: NbToastrService,
    private activatedRoute: ActivatedRoute,
    private selectedStoreService:SelectedStoreService
    ) {
    this.params = this.loadParams();

  }

  loadParams() {
    return {
      store: "",
      count: this.perPage,
      page: 0,
    };
  }

  ngOnInit() {
    zip([this.selectedStoreService.current(), this.activatedRoute.parent.params]).subscribe({
      next: (([selectedStore, params]) => {
        this.params.store = selectedStore
        this.uniqueCode = params['code'];
        this.load();
      })
    })
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
    this.productService.addProductToCategory( productId, groupCode)
      .subscribe({
        next: () => {
          this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_TO_CATEGORY_ADDED'));
          this.load()
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
  }

  removeProductFromCategory(store, productId, groupCode) {
    this.productService.removeProductFromCategory( productId, groupCode)
      .subscribe({
        next: () => {
          this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_TO_CATEGORY_REMOVED'));
          this.load()
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
  }

  ngAfterViewInit(): void {
    const el: HTMLElement = document.getElementById('tabs');
    el.scrollIntoView();
  }

  private load() {
    this.loading = true;

    const p$ = this.categoryService.getCategoryByProductId(this.uniqueCode)
    const c$ = this.categoryService.getListOfCategories(this.params)

    forkJoin([p$, c$])
      .subscribe(([p$, c$]) => {
        this.selectedItems = [];
        this.categories = [];
        p$.content.forEach((data) => {
          this.selectedItems.push(data.id)
        });
        c$.content.forEach((value) => {
          this.categories.push({'id': value.id, 'name': value.code})
        })
        this.loading = false;
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
        this.loading = false;
      });

  }

}
