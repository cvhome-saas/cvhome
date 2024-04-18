import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';

import {CategoryService} from '../../categories/services/category.service';
import {ProductService} from '../services/product.service';
import {TranslateService} from '@ngx-translate/core';
import {StorageService} from '../../../shared/services/storage.service';
import {forkJoin} from 'rxjs';
import {Location} from '@angular/common';

@Component({
  selector: 'ngx-product-to-category',
  templateUrl: './product-to-category.component.html',
  styleUrls: ['./product-to-category.component.scss']
})
export class ProductToCategoryComponent implements OnInit {

  loaded = false;
  loading = false;
  uniqueCode: string;
  perPage: number = 50;//ideally display all category
  currentPage: number = 1;

  dropdownList = [];
  categories = [];
  selectedItems = [];
  dropdownSettings = {};
  categoryLoading = false;
  filteredCategories = [];

  params = this.loadParams();

  constructor(
    private translate: TranslateService,
    private categoryService: CategoryService,
    private storageService: StorageService,
    private productService: ProductService,
    private location: Location,
    private router: Router,
    private activatedRoute: ActivatedRoute) {
    //
    // this.dropdownSettings = {
    //   singleSelection: false,
    //   idField: 'id',
    //   textField: 'name',
    //   enableCheckAll: false,
    //   searchPlaceholderText: 'Search by code',
    //   // unSelectAllText: 'UnSelect All',
    //   itemsShowLimit: 10,
    //   allowSearchFilter: true,
    //   allowRemoteDataSearch: true
    //
    // };
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

    this.load();

    //specify add image url to image component
    let el = document.getElementById('tabs');
    el.scrollIntoView();
  }

  private load() {
    this.loading = true;

    const p$ = this.categoryService.getCategoryByProductId(this.uniqueCode, this.params.store)
    const c$ = this.categoryService.getListOfCategories(this.params)

    forkJoin([p$, c$])
      .subscribe(([p$, c$]) => {
        p$.categories.forEach((data) => {
          this.selectedItems.push({'id': data.id, 'name': data.code})
        });
        c$.categories.forEach((value) => {
          this.getChildren(value);

        })
        this.loading = false;
      });

  }


  getChildren(node) {

    if (node.children && node.children.length !== 0) {
      this.categories.push({'id': node.id, 'name': node.description.name})
      node.children.forEach((el) => {
        this.getChildren(el);
      });
    } else {
      this.categories.push({'id': node.id, 'name': node.description.name})
    }
  }

  onFilterChange(e) {
    //console.log(e);
    if (e.length > 2) {
      this.params["name"] = e;
      this.categoryService.filterCategory(this.params, "").subscribe(res => {
        }, error => {

        }
      )
      // this.getList();
    }
  }

  onItemSelect(item: any) {
    this.addProductToCategory(this.params.store, this.uniqueCode, item.id)
  }

  onItemDeSelect(item: any) {
    this.removeProductFromCategory(this.params.store, this.uniqueCode, item.id)
  }

  addProductToCategory(store, productId, groupCode) {
    this.productService.addProductToCategory(store, productId, groupCode)
      .subscribe(res => {
        this.load();
      });
  }

  removeProductFromCategory(store, productId, groupCode) {
    this.productService.removeProductFromCategory(store, productId, groupCode)
      .subscribe(res => {
        this.load();
      });
  }


}
