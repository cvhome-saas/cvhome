import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {DomSanitizer} from '@angular/platform-browser';

import {BrandService} from '../services/brand.service';
import {TranslateService} from '@ngx-translate/core';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {StorageService} from '../../../shared/services/storage.service';
import {ShowcaseDialogComponent} from "../../../shared/components/showcase-dialog/showcase-dialog.component";
import {Page} from "../../../shared/models/Page";
import {ColumnMode} from "@swimlane/ngx-datatable";

@Component({
  selector: 'ngx-brands-list',
  templateUrl: './brands-list.component.html',
  styleUrls: ['./brands-list.component.scss']
})
export class BrandsListComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  page: Page = new Page();
  rows = [];
  loadingList = false;
  settings = {};
  searchValue: string = '';
  // paginator
  perPage = 25;
  currentPage = 1;

  // request params
  params = {
    lang: this.storageService.getLanguage(),
    store: "",
    count: this.perPage,
    page: 0
  };

  constructor(
    private brandService: BrandService,
    private router: Router,
    private _sanitizer: DomSanitizer,
    private dialogService: NbDialogService,
    private translate: TranslateService,
    private storageService: StorageService,
    private toastr: NbToastrService,
  ) {
  }

  ngOnInit() {
    this.translate.onLangChange.subscribe((lang) => {
      this.params.lang = this.storageService.getLanguage();
      this.getList();
    });
  }

  getList() {
    this.params.page = this.currentPage - 1;
    this.loadingList = true;
    this.brandService.getListOfBrands(this.params)
      .subscribe(data => {
        this.rows = data.manufacturers
        this.page.totalPages = data.totalPages
        this.page.totalElements = data.recordsTotal
        this.page.size = data.numbers

        this.loadingList = false;
      });
  }

  onSelectStore(e) {
    this.params.store = e.id;
    this.setPage({offset: 0});
  }

  setPage(pageInfo) {
    this.page.pageNumber = pageInfo.offset;
    this.getList();
  }


  route(event) {
    switch (event.action) {
      case 'details':
        break;
      case 'remove':
    }
  }

  onEdit(row: any) {
    this.router.navigate(['pages/catalogue/brands/brand/', this.params.store + '-' + row.id]);
  }

  onDelete(row: any) {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      }
    })
      .onClose.subscribe(res => {
      if (res) {
        this.brandService.deleteBrand(this.params.store, row.id)
          .subscribe(data => {
            this.toastr.success(this.translate.instant('BRAND.BRAND_REMOVED'));
            this.getList();
          });
      }
    });

  }
}
