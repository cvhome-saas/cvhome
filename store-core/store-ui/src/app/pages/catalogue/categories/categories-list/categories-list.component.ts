import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {DomSanitizer} from '@angular/platform-browser';

import {CategoryService} from '../services/category.service';
import {ButtonRenderComponent} from './button-render.component';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {ProductService} from '../../products/services/product.service';
import {StorageService} from '../../../shared/services/storage.service';
import {ListingService} from '../../../shared/services/listing.service';
import {ShowcaseDialogComponent} from "../../../store-manager/shared/showcase-dialog/showcase-dialog.component";
import {Page} from "../../../shared/models/Page";
import {ColumnMode} from "@swimlane/ngx-datatable";

@Component({
  selector: 'ngx-categories-list',
  templateUrl: './categories-list.component.html',
  styleUrls: ['./categories-list.component.scss']
})
export class CategoriesListComponent implements OnInit {
  listingService: ListingService;
  loadingList = false;
  loading: boolean = false;
  categories = [];
  settings = {};
  rows = [];

  // paginator
  perPage = 25;
  currentPage = 1; //start base
  totalCount;
  roles;
  searchValue: string = '';

  // request params
  params = this.loadParams();

  constructor(
    private categoryService: CategoryService,
    private router: Router,
    private _sanitizer: DomSanitizer,
    private dialogService: NbDialogService,
    private translate: TranslateService,
    private toastr: NbToastrService,
    private productService: ProductService,
    private storageService: StorageService,
  ) {
    this.roles = JSON.parse(localStorage.getItem('roles'));
    this.listingService = new ListingService();
  }

  loadParams() {
    return {
      lang: this.storageService.getLanguage(),
      store: "",
      count: this.perPage,
      page: 1
    };
  }


  /** */


  ngOnInit() {
    this.translate.onLangChange.subscribe((lang) => {
      this.params.lang = this.storageService.getLanguage();
      this.getCategories();
    });
  }

  // creating array of categories include children
  //specific to category
  getChildren(node) {
    node.name = node.description.name;
    if (node.children && node.children.length !== 0) {
      this.categories.push(node);
      node.children.forEach((el) => {
        this.getChildren(el);
      });
    } else {
      this.categories.push(node);
    }
  }


  getCategories() {
    this.loadingList = true;
    var page = this.currentPage - 1;
    this.params.page = page;
    this.categoryService.getListOfCategories(this.params)
      .subscribe({
          next: (data) => {
            data.categories.forEach((el) => {
              el.name = el.description.name;
              this.getChildren(el);

            });
            this.rows = data.categories
            this.page.totalPages = data.totalPages
            this.page.totalElements = data.recordsTotal
            this.page.size = data.numbers
            this.loadingList = false;
          },
          error: (err) => {
            this.loadingList = false;
          }
        }
      );
  }

  setSettings() {
    this.settings = {
      actions: {
        columnTitle: '',
        add: false,
        edit: false,
        delete: false,
        position: 'right',
        sort: true,
        custom: [
          {name: 'details', title: '<i class="nb-edit"></i>'},
          {name: 'remove', title: this._sanitizer.bypassSecurityTrustHtml('<i class="nb-trash"></i>')}
        ],
      },
      pager: {
        display: false
      },
      columns: {
        id: {
          filter: false,
          title: this.translate.instant('COMMON.ID'),
          type: 'number',
        },
        store: {
          title: this.translate.instant('STORE.MERCHANT_STORE'),
          type: 'string',
          filter: false,
        },
        name: {
          title: this.translate.instant('CATEGORY.CATEGORY_NAME'),
          type: 'string',
          filter: true
        },
        code: {
          title: this.translate.instant('COMMON.CODE'),
          type: 'string',
          filter: false,
        },
        parent: {
          title: this.translate.instant('CATEGORY.PARENT'),
          type: 'string',
          filter: false,
          valuePrepareFunction: (parent) => {
            return parent ? parent.code : 'root';
          }
        },
        visible: {
          filter: false,
          title: this.translate.instant('COMMON.VISIBLE'),
          type: 'custom',
          renderComponent: ButtonRenderComponent,
          defaultValue: false,
        },
      },
    };
  }


  onEdit(event) {
    this.router.navigate(['pages/catalogue/categories/category/', this.params.store + "-" + event.id]);
  }

  onDelete(event) {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: "",
        text: ""
      }
    })
      .onClose.subscribe(res => {
      if (res) {
        this.categoryService.deleteCategory(event.data.id,"")
          .subscribe(data => {
            this.toastr.success(this.translate.instant('CATEGORY_FORM.CATEGORY_REMOVED'));
            this.getCategories();
          });
      }
    });
  }

  createCategory() {
    this.router.navigate(['pages/catalogue/categories/create-category']);
  }


  page: Page = new Page();

  onSelectStore(e) {
    this.params.store = e.id;
    this.setPage({offset: 0});
  }

  setPage(pageInfo) {
    this.page.pageNumber = pageInfo.offset;
    this.getCategories();
  }

  protected readonly ColumnMode = ColumnMode;
}
