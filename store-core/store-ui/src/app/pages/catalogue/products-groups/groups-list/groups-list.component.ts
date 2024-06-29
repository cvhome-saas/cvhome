import {Component, OnInit} from '@angular/core';

import {NbDialogService, NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {ProductGroupsService} from '../services/product-groups.service';
import {StorageService} from '../../../shared/services/storage.service';
import {Router} from '@angular/router';
import {ShowcaseDialogComponent} from "../../../shared/components/showcase-dialog/showcase-dialog.component";
import {ColumnMode} from "@swimlane/ngx-datatable";
import {Page} from "../../../shared/models/Page";

@Component({
  selector: 'ngx-groups-list',
  templateUrl: './groups-list.component.html',
  styleUrls: ['./groups-list.component.scss']
})
export class GroupsListComponent implements OnInit {
  page: Page = new Page();
  rows = [];
  loadingList = false;
  params = this.loadParams();

  constructor(
    private dialogService: NbDialogService,
    private translate: TranslateService,
    private productGroupsService: ProductGroupsService,
    private storageService: StorageService,
    private toastr: NbToastrService,
    private router: Router) {
  }

  loadParams() {
    return {
      lang: this.storageService.getLanguage(),
      store: ""
    };
  }

  ngOnInit() {
    this.translate.onLangChange.subscribe((lang) => {
      this.params.lang = this.storageService.getLanguage();
      this.getList();
    });
  }

  setPage(pageInfo) {
    this.page.pageNumber = pageInfo.offset;
    this.getList();
  }

  getList() {
    this.loadingList = true;
    this.productGroupsService.getListOfProductGroups(this.params).subscribe(res => {
      this.loadingList = false;
      this.rows = res
      this.page.totalPages = 1
      this.page.totalElements = res.length
      this.page.size = res.length
      this.loadingList = false;
    });

  }


  onSelectStore(e) {
    this.params.store = e.id;
    this.getList();
  }


  protected readonly ColumnMode = ColumnMode;

  onEdit(row: any) {
    this.router.navigate([`/pages/catalogue/products-groups/update-products-group/${this.params.store}-${row.code}`]);
  }

  onDelete(row: any) {
    this.dialogService.open(ShowcaseDialogComponent, {})
      .onClose.subscribe(res => {
      if (res) {
        this.productGroupsService.removeProductGroup(this.params.store, row.code)
          .subscribe(result => {
            this.getList();
          });
      }
    });

  }
}
