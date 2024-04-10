import {Component, OnInit} from '@angular/core';
import {OrdersService} from "../services/orders.service";
import {Router} from "@angular/router";
import {TranslateService} from "@ngx-translate/core";
import {StorageService} from "../../shared/services/storage.service";
import {StoreService} from "../../store-management/services/store.service";
import {ManagerStoreId} from "../../../shared/domain/commons";
import {ColumnMode} from "@swimlane/ngx-datatable";
import {Page} from "../../shared/models/Page";

@Component({
  selector: 'ngx-order-list',
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.scss']
})
export class OrderListComponent implements OnInit {
  rows = [];
  columns = [{ name: 'Id' }];  ColumnMode = ColumnMode;
  page:Page = new Page();


  // source: LocalDataSource = new LocalDataSource();
  loadingList = false;
  settings = {};
  stores: Array<any> = [];
  selectedStore: String = '';
  paginator
  perPage = 20;
  currentPage = 0;
  totalCount;
  roles;
  // searchValue: string = '';
  isSuperAdmin: boolean;

  timeoutHandler: any;
  params = this.loadParams();

  constructor(
    private ordersService: OrdersService,
    private router: Router,
    //   // private mScrollbarService: MalihuScrollbarService,
    private translate: TranslateService,
    private storageService: StorageService,
    private storeService: StoreService,
  ) {
  }

  ngOnInit() {
    this.translate.onLangChange.subscribe((lang) => {
      this.params.lang = this.storageService.getLanguage();
    });
    // this.source.onChanged().subscribe((change) => {
    //   if (change.action == 'refresh' || change.action == 'load') {
    //     clearTimeout(this.timeoutHandler);
    //   } else {
    //     var time = 2000;
    //   }
    //   if (this.timeoutHandler) {
    //     clearTimeout(this.timeoutHandler);
    //   }
    //
    //   this.timeoutHandler = setTimeout(() => {
    //     if (change.action == 'filter') {
    //       change.filter.filters.map((a) => {
    //         if (a.field == "id") {
    //           this.params["id"] = a.search;
    //         } else if (a.field == "billingName") {
    //           this.params["name"] = a.search;
    //         } else if (a.field == "billingPhone") {
    //           this.params["phone"] = a.search;
    //         } else if (a.field == "billingEmail") {
    //           this.params["email"] = a.search;
    //         } else if (a.field == "orderStatus") {
    //           this.params["status"] = a.search;
    //         }
    //       });
    //
    //       this.getOrderList()
    //     }
    //   }, time);
    //
    // });
  }

  loadParams() {
    return {
      store: "",
      lang: this.storageService.getLanguage(),
      count: this.perPage,
      page: 0
    };
  }

  getOrderList() {
    this.params.page = this.currentPage;

    this.loadingList = true;
    this.ordersService.getOrders(this.params)
      .subscribe({
        next: (orders) => {
          this.loadingList = false;
          if (orders.orders && orders.orders.length !== 0) {
            // this.source.load(orders.orders);
           this.rows=orders.orders
            this.page.totalPages=orders.totalPages
            this.page.totalElements=orders.recordsTotal
            this.page.size=orders.numbers
          } else {
            this.rows=[];
          }
          this.totalCount = orders.recordsTotal;
        },
        error: (err) => {
          this.loadingList = false;
        },
      })
    this.setSettings();
  }

  setSettings() {
    var me = this;
    this.settings = {
      // mode: 'external',
      // hideSubHeader: false,
      actions: {
        columnTitle: this.translate.instant('ORDER.ACTIONS'),
        add: false,
        edit: false,
        delete: false,
        position: 'right',
        custom: [
          {
            name: 'view',
            title: '<i class="nb-edit"></i>'
          }
        ],
      },
      pager: {
        display: false
      },
      columns: {
        id: {
          title: this.translate.instant('COMMON.ID'),
          type: 'number',
          filterFunction(cell: any, search?: string): boolean {
            return true;
          }
        },
        billingName: {
          title: this.translate.instant('ORDER.CUSTOMER_NAME'),
          type: 'string',
          valuePrepareFunction: (customer, data) => {
            // console.log(data);
            return data.billing.firstName + ' ' + data.billing.lastName;
          },
          filterFunction(cell: any, search?: string): boolean {
            return true;
          }
        },
        billingPhone: {
          title: this.translate.instant('ORDER.CUSTOMER_PHONE'),
          type: 'string',
          valuePrepareFunction: (customer, data) => {
            // console.log(customer)
            return data.billing.phone;
          },
          filterFunction(cell: any, search?: string): boolean {
            return true;
          }
        },
        billingEmail: {
          title: this.translate.instant('ORDER.CUSTOMER_EMAIL'),
          type: 'string',
          valuePrepareFunction: (customer, data) => {
            // console.log(customer)
            return data.billing.email;
          },
          filterFunction(cell: any, search?: string): boolean {
            return true;
          }
        },
        total: {
          title: this.translate.instant('ORDER.TOTAL'),
          type: 'string',
          filter: false,
          valuePrepareFunction: (total) => {
            return total.value;
          }
        },
        datePurchased: {
          title: this.translate.instant('ORDER.ORDER_DATE'),
          type: 'string',
          filter: false,
          // valuePrepareFunction: (date) => {
          //   if (date) {
          //     return new DatePipe('en-GB').transform(date, 'yyyy-MM-dd');
          //   }
          // }
        },
        orderStatus: {
          title: this.translate.instant('ORDER.STATUS'),
          type: 'string',
          filterFunction(cell: any, search?: string): boolean {
            return true;
          },
          filter: {
            type: 'list',
            config: {
              selectText: this.translate.instant('ORDER.SHOWALL'),
              list: [
                {value: 'ORDERED', title: this.translate.instant('ORDER.ORDERED')},
                {value: 'PROCESSED', title: this.translate.instant('ORDER.PROCESSED')},
                {value: 'DELIVERED', title: this.translate.instant('ORDER.DELIVERED')},
                {value: 'REFUNDED', title: this.translate.instant('ORDER.REFUNDED')},
                {value: 'CANCELED', title: this.translate.instant('ORDER.CANCELED')},
              ]
            }
          }
        }
      },

    };

  }

  onSelectStore($event: ManagerStoreId) {
    this.params["store"] = $event.id;
    this.setPage({offset:0})
  }
  setPage(pageInfo){
    this.page.pageNumber = pageInfo.offset;
    this.getOrderList();
  }
}
