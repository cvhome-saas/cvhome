import {Component} from '@angular/core';
import {CrudService} from '../../shared/services/crud.service';
import {Router} from '@angular/router';
import {StoreService} from '../../store-management/services/store.service';
import {StorageService} from '../../shared/services/storage.service';
import {TranslateService} from '@ngx-translate/core';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {ShowcaseDialogComponent} from "../../shared/components/showcase-dialog/showcase-dialog.component";
import {Page} from "../../shared/models/Page";
import {ColumnMode} from "@swimlane/ngx-datatable";

@Component({
  selector: 'boxes-table',
  templateUrl: './boxes.component.html',
  styleUrls: ['./boxes.component.scss'],
})
export class BoxesComponent {
  page: Page = new Page();
  rows = [];
  perPage = 10;
  currentPage = 1;
  loadingList = false;
  params = this.loadParams();
  protected readonly ColumnMode = ColumnMode;

  constructor(
    private crudService: CrudService,
    public router: Router,
    private storeService: StoreService,
    private storageService: StorageService,
    private translate: TranslateService,
    private toastr: NbToastrService,
    private dialogService: NbDialogService) {
  }


  loadParams() {
    return {
      store: "",
      lang: "_all",
      count: this.perPage,
      page: 0
    };
  }

  getBox() {
    this.loadingList = true;
    this.params.page = this.currentPage - 1;
    this.crudService.get('/store/api/v1/private/content/boxes', this.params)
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
      });
  }

  addBoxes() {
    this.router.navigate(['/pages/content/boxes/add', this.params.store]);
  }

  setPage(pageInfo) {
    this.page.pageNumber = pageInfo.offset;
    this.getBox();
  }

  onSelectStore(e) {
    this.params.store = e.id;
    this.setPage({offset: 0});
  }

  onEdit(event) {
    this.router.navigate(['/pages/content/boxes/add', this.params.store + "-" + event.code]);
  }

  onDelete(event) {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      },
    }).onClose.subscribe(res => {
      if (res) {
        this.loadingList = true;
        this.crudService.delete('/store/api/v1/private/content/' + event.id + '?id=' + event.id + '&store=' + this.params.store)
          .subscribe({
            next: (data) => {
              this.loadingList = false;
              this.toastr.success(this.translate.instant('CONTENT.BOX_DELETED'));
              this.getBox();
            },
            error: (err) => {
              this.loadingList = false;
            }
          });
      } else {
        this.loadingList = false;
      }
    });
  }

}
