import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {ShowcaseDialogComponent} from "../../../shared/components/showcase-dialog/showcase-dialog.component";
import {ColumnMode} from "@swimlane/ngx-datatable";
import {ErrorService} from "../../../shared/services/error.service";
import {ContentService} from "../services/content.service";
import {SelectedStoreService} from "../../../shared/services/selected-store.service";
import {BaseTable, PageT, StorePageRequest} from "../../common/BaseTable";
import {Observable, of} from "rxjs";
import {map} from "rxjs/operators";

@Component({
  selector: 'boxes-table',
  standalone: false,
  templateUrl: './boxes.component.html',
  styleUrls: ['./boxes.component.scss'],
})
export class BoxesComponent extends BaseTable<any> implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  private isInitialized: boolean = false;

  constructor(
    private contentService: ContentService,
    public router: Router,
    errorService: ErrorService,
    translate: TranslateService,
    private toastr: NbToastrService,
    selectedStoreService: SelectedStoreService,
    private dialogService: NbDialogService) {
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
    return this.contentService.getBoxes(request)
  }

  addBoxes() {
    this.router.navigate(['/pages/content/boxes/add', this.params.store]);
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
        this.contentService.deleteContent(event.id, this.params.store)
          .subscribe({
            next: (data) => {
              this.toastr.success(this.translate.instant('CONTENT.BOX_DELETED'));
              this.trigger();
            },
            error: (err) => {
              this.errorService.error('ERROR.SYSTEM_ERROR', err);
            }
          });
      } else {
      }
    });
  }

}
