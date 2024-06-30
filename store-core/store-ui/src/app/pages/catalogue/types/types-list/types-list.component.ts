import {Component, OnInit} from '@angular/core';
import {DomSanitizer} from '@angular/platform-browser';
import {TranslateService} from '@ngx-translate/core';
import {StorageService} from '../../../shared/services/storage.service';
import {StoreService} from '../../../store-management/services/store.service';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {Router} from '@angular/router';
import {TypesService} from '../services/types.service';
import {ShowcaseDialogComponent} from "../../../shared/components/showcase-dialog/showcase-dialog.component";
import {ColumnMode} from "@swimlane/ngx-datatable";
import {Page} from "../../../shared/models/Page";

@Component({
  selector: 'ngx-types-list',
  templateUrl: './types-list.component.html',
  styleUrls: ['./types-list.component.scss']
})
export class TypesListComponent implements OnInit {

  rows: any[] = [];
  page: Page = new Page();
  perPage = 15;
  loadingList = false;
  settings = {};
  params = this.loadParams();
  currentPage = 1;
  protected readonly ColumnMode = ColumnMode;

  constructor(
    private _sanitizer: DomSanitizer,
    private translate: TranslateService,
    private router: Router,
    private dialogService: NbDialogService,
    private toastr: NbToastrService,
    private storageService: StorageService,
    private storeService: StoreService,
    private typesService: TypesService) {
  }

  loadParams() {
    return {
      store: "",
      lang: this.translate.currentLang,
      count: this.perPage,
      page: 0
    };
  }

  ngOnInit(): void {
    this.translate.onLangChange.subscribe((lang) => {
      this.params.lang = lang.lang;
      this.getList();
    });
  }


  getList() {
    this.loadingList = true;
    this.params.page = this.currentPage - 1;
    this.typesService.getListOfTypes(this.params)
      .subscribe({
        next: (data) => {
          this.rows = data.list
          this.page.totalPages = data.totalPages
          this.page.totalElements = data.recordsTotal
          this.page.size = data.numbers
          this.loadingList = false;
        },
        error: (err) => {
          this.loadingList = false;
        }
      });

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

        this.typesService.deleteType(this.params.store, row.id)
          .subscribe(result => {
            this.toastr.success(this.translate.instant('OPTION.OPTION_REMOVED'));
            this.getList();
          });

      } else {
        // TODO navigate generic error
        // event.confirm.reject();
      }
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

  onEdit(row) {
    this.router.navigate(['/pages/catalogue/types/type/' + this.params.store + '-' + row.id]);
  }

}
