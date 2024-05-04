import {Component} from '@angular/core';
import {CrudService} from '../../shared/services/crud.service';
import {Router} from '@angular/router';
import {NbDialogService, NbToastrService} from '@nebular/theme';
// import { ShowcaseDialogComponent } from '../../shared/components/showcase-dialog/showcase-dialog.component';
import {StoreService} from '../../store-management/services/store.service';
import {StorageService} from '../../shared/services/storage.service';
import {TranslateService} from '@ngx-translate/core';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {Page} from "../../shared/models/Page";
import {ShowcaseDialogComponent} from "../../store-manager/shared/showcase-dialog/showcase-dialog.component";

@Component({
  selector: 'page-table',
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.scss'],
})
export class PageComponent {
  rows = [];
  page: Page = new Page();

  perPage = 10;
  currentPage = 1;
  params = {
    lang: this.storageService.getLanguage(),
    store: "",
    count: this.perPage,
    page: 0
  };
  loadingList = false;
  protected readonly ColumnMode = ColumnMode;

  constructor(
    private crudService: CrudService,
    public router: Router,
    private dialogService: NbDialogService,
    private toastr: NbToastrService,
    private storeService: StoreService,
    private storageService: StorageService,
    private translate: TranslateService
  ) {

    this.translate.onLangChange.subscribe((lang) => {
      this.params.lang = this.storageService.getLanguage();
      this.getPages()
    });
  }

  getPages() {
    this.loadingList = true;
    this.params.page = this.currentPage - 1;
    this.crudService.get('/store/api/v1/private/content/pages', this.params)
      .subscribe({
        next: (data) => {
          this.rows = data.items
          this.page.totalPages = data.totalPages
          this.page.totalElements = data.recordsTotal
          this.page.size = data.numbers
          this.loadingList = false;
        },
        error: (err) => {
          this.loadingList = false;
        }
      })
  }


  addPages() {
    localStorage.setItem('contentpageid', '');
    this.router.navigate(['/pages/content/pages/add', this.params.store + "-"]);
  }

  onSelectStore(e) {
    this.params.store = e.id;
    this.setPage({offset: 0});
  }

  setPage(pageInfo) {
    this.page.pageNumber = pageInfo.offset;
    this.getPages();
  }

  onEdit(event) {
    this.router.navigate(['/pages/content/pages/add/' + this.params.store + "-" + event.code]);
  }

  onDelete(event) {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      }
    }).onClose.subscribe(res => {
      if (res) {
        this.loadingList = true;
        this.crudService.delete('/store/api/v1/private/content/' + event.id + '?id=' + event.id + "&store=" + this.params.store)
          .subscribe({
            next: (data) => {
              this.loadingList = false;
              this.toastr.success('Content page deleted successfully');
              this.getPages();
            },
            error: (err) => {
              this.loadingList = false;
            },
          })
      } else {
        this.loadingList = false;
      }
    });

  }
}
