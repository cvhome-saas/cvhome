import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {ShowcaseDialogComponent} from "../../../shared/components/showcase-dialog/showcase-dialog.component";
import {ErrorService} from "../../../shared/services/error.service";
import {ContentService} from "../services/content.service";
import {SelectedStoreService} from "../../../shared/services/selected-store.service";
import {BaseTable, PageT, StorePageRequest} from "../../common/BaseTable";
import {Observable, of} from "rxjs";
import {map} from "rxjs/operators";

@Component({
  selector: 'page-table',
  standalone: false,
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.scss'],
})
export class PageComponent extends BaseTable<any> implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  private isInitialized: boolean = false;

  constructor(
    private contentService: ContentService,
    public router: Router,
    private dialogService: NbDialogService,
    private toastr: NbToastrService,
    errorService: ErrorService,
    translate: TranslateService,
    selectedStoreService: SelectedStoreService,
  ) {
    super(selectedStoreService, translate, errorService)
  }

  ngOnInit(): void {
    this.isInitialized = true;
    this.trigger();
  }

  override list(request: StorePageRequest): Observable<PageT<any>> {
    if (!super.params.store || !this.isInitialized) {
      return of();
    }
    return this.contentService.pages(request)
      .pipe(map(it => {
        const mappedX = {
          content: it.items,
          totalPages: it.totalPages,
          totalElements: it.recordsTotal,
          size: it.number,
          pageNumber: request.page
        };
        return mappedX;
      }));
  }


  addPages() {
    localStorage.setItem('contentpageid', '');
    this.router.navigate(['/pages/content/pages/add', this.params.store + "-"]);
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
        this.contentService.deleteContent(event.id, this.params.store)
          .subscribe({
            next: (data) => {
              this.toastr.success('Content page deleted successfully');
              this.trigger();
            },
            error: (err) => {
              this.errorService.error('ERROR.SYSTEM_ERROR', err);
            },
          })
      } else {
      }
    });

  }
}
