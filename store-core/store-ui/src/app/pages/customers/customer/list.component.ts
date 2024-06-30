import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {StorageService} from '../../shared/services/storage.service';
import {CustomersService} from '../services/customer.service';
import {StoreService} from '../../store-management/services/store.service';
import {TranslateService} from '@ngx-translate/core';
import {ErrorService} from '../../shared/services/error.service';
import {NbToastrService} from "@nebular/theme";
import {Page} from "../../shared/models/Page";
import {ColumnMode} from "@swimlane/ngx-datatable";

@Component({
  selector: 'ngx-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent implements OnInit {
  page: Page = new Page();
  rows = [];
  settings = {};
  loadingList = false;
  perPage = 10;
  currentPage = 0;
  totalCount;
  params = this.loadParams();
  protected readonly ColumnMode = ColumnMode;

  constructor(
    private customersService: CustomersService,
    public router: Router,
    private toastr: NbToastrService,
    private storageService: StorageService,
    private storeService: StoreService,
    private translate: TranslateService,
    private errorService: ErrorService
  ) {
    this.translate.onLangChange.subscribe((lang) => {
      this.params.lang = lang.lang;
      this.getCustomers();
    });
  }

  ngOnInit() {
  }


  loadParams() {
    return {
      store: "",
      lang: this.translate.currentLang,
      count: this.perPage,
      page: 0
    };
  }

  getCustomers() {
    this.params.page = this.currentPage;

    this.loadingList = true;
    this.customersService.getCustomers(this.params)
      .subscribe({
        next: (data) => {
          this.rows = data.customers
          this.page.totalPages = data.totalPages
          this.page.totalElements = data.recordsTotal
          this.page.size = data.numbers
          this.loadingList = false;
          this.totalCount = data.totalPages;
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
          this.loadingList = false;

        }
      });

    this.setSettings();
  }

  setSettings() {
    this.settings = {

      actions: {
        columnTitle: this.translate.instant('ORDER.ACTIONS'),
        add: false,
        edit: false,
        delete: false,
        position: 'right',
        custom: [
          {
            name: 'edit',
            title: '<i class="nb-edit"></i>'
          },
          {
            name: 'delete',
            title: '<i class="nb-trash"></i>'
          }
        ]
      },
      pager: {
        display: false
      },
      columns: {
        id: {
          title: this.translate.instant('COMMON.ID'),
          type: 'number',
          filter: false
        },
        storeCode: {
          title: this.translate.instant('STORE.MERCHANT_STORE'),
          type: 'string'
        },
        firstName: {
          title: this.translate.instant('ORDER_FORM.FIRST_NAME'),
          type: 'string',
          filter: false
        },
        lastName: {
          title: this.translate.instant('ORDER_FORM.LAST_NAME'),
          type: 'string',
        },
        emailAddress: {
          title: this.translate.instant('USER_FORM.EMAIL_ADDRESS'),
          type: 'string'
        }
      },
    };
  }


  addCustomer() {
    localStorage.setItem('customerid', '');
    this.router.navigate(['/pages/customer/add']);
  }

  setPage(pageInfo) {
    this.page.pageNumber = pageInfo.offset;
    this.getCustomers();
  }

  onSelectStore(e) {
    this.params.store = e.id;
    this.setPage({offset: 0});
  }

}
