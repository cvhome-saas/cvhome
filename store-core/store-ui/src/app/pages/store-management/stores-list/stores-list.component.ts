import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';

import {StoreService} from '../services/store.service';
import {TranslateService} from '@ngx-translate/core';
import {SecurityService} from '../../shared/services/security.service';
import {DomSanitizer} from '@angular/platform-browser';
import {StorageService} from '../../shared/services/storage.service';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {ShowcaseDialogComponent} from "../../shared/components/showcase-dialog/showcase-dialog.component";
import {Page} from "../../shared/models/Page";
import {ColumnMode} from "@swimlane/ngx-datatable";

@Component({
  selector: 'ngx-stores-list',
  templateUrl: './stores-list.component.html',
  styleUrls: ['./stores-list.component.scss']
})
export class StoresListComponent implements OnInit {
  store;
  loadingList = false;
  page: Page = new Page();
  rows = [];
  perPage = 10;
  currentPage = 1;
  params = this.loadParams();
  protected readonly ColumnMode = ColumnMode;

  constructor(
    private storeService: StoreService,
    private storageService: StorageService,
    private router: Router,
    private toastr: NbToastrService,
    private dialogService: NbDialogService,
    private translate: TranslateService,
    private securityService: SecurityService,
    private _sanitizer: DomSanitizer
  ) {
  }

  ngOnInit() {
    this.getList();
    this.translate.onLangChange.subscribe((lang) => {
      this.params.lang = lang.lang;
      this.getList();
    });
  }

  loadParams() {
    return {
      lang: this.translate.currentLang,
      count: this.perPage,
      page: 0
    };
  }

  setPage(pageInfo) {
    this.page.pageNumber = pageInfo.offset;
    this.getList();
  }

  getList() {
    this.loadingList = true;
    this.storeService.getListOfStores(this.params)
      .subscribe({
        next: (data) => {
          this.rows = data.content
          this.page.totalPages = data.totalPages
          this.page.totalElements = data.totalElements
          this.page.size = data.numberOfElements
          this.loadingList = false;
        },
        error: (err) => {
          this.loadingList = false;
        },
      });

  }

  onEdit(row) {
    this.router.navigate(['pages/store-management/store/', row.code]);
  }

  onDelete(row) {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      }
    })
      .onClose.subscribe(res => {
      if (res) {
        this.storeService.deleteStore(row.code)
          .subscribe(data => {
            this.toastr.success(this.translate.instant('USER_FORM.USER_REMOVED'));
            this.getList();
          });
      }
    });

  }

}
