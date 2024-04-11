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
  columns = [{name: 'Id'}];
  protected readonly ColumnMode = ColumnMode;
  page: Page = new Page();


  // source: LocalDataSource = new LocalDataSource();
  loadingList = false;
  settings = {};
  stores: Array<any> = [];
  perPage = 20;
  currentPage = 0;
  totalCount;
  roles;
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
    this.ordersService.getOrders(this.params.store, this.params)
      .subscribe({
        next: (orders) => {
          this.loadingList = false;
          if (orders.orders && orders.orders.length !== 0) {
            // this.source.load(orders.orders);
            this.rows = orders.orders
            this.page.totalPages = orders.totalPages
            this.page.totalElements = orders.recordsTotal
            this.page.size = orders.numbers
          } else {
            this.rows = [];
          }
          this.totalCount = orders.recordsTotal;
        },
        error: (err) => {
          this.loadingList = false;
        },
      })
  }

  onSelectStore($event: ManagerStoreId) {
    this.params.store = $event.id;
    this.setPage({offset: 0})
  }

  setPage(pageInfo) {
    this.page.pageNumber = pageInfo.offset;
    this.getOrderList();
  }
}
