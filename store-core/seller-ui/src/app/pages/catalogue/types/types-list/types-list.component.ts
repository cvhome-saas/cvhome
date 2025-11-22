import {Component, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {Router} from '@angular/router';
import {TypesService} from '../services/types.service';
import {ShowcaseDialogComponent} from "../../../../shared/components/showcase-dialog/showcase-dialog.component";
import {ColumnMode} from "@swimlane/ngx-datatable";
import {ErrorService} from "../../../../shared/services/error.service";
import {SelectedStoreService} from "../../../../shared/services/selected-store.service";
import {BaseTable, PageT, StorePageRequest} from "../../../common/BaseTable";
import {Observable, of} from "rxjs";

@Component({
  selector: 'ngx-types-list',
  standalone: false,
  templateUrl: './types-list.component.html',
  styleUrls: ['./types-list.component.scss']
})
export class TypesListComponent extends BaseTable<any> implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  private isInitialized: boolean = false;

  constructor(
    private translate: TranslateService,
    private router: Router,
    private dialogService: NbDialogService,
    private toastr: NbToastrService,
    errorService: ErrorService,
    selectedStoreService: SelectedStoreService,
    private typesService: TypesService) {
    super(selectedStoreService, errorService)
  }


  ngOnInit(): void {
    this.isInitialized = true;
    this.trigger();
  }

  override list(request: StorePageRequest): Observable<PageT<any>> {
    if (!super.params.store || !this.isInitialized) {
      return of();
    }
    return this.typesService.getListOfTypes(request)
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

        this.typesService.deleteType( row.id)
          .subscribe(result => {
            this.toastr.success(this.translate.instant('OPTION.OPTION_REMOVED'));
            this.trigger();
          }, err => {
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          });

      } else {
        // TODO navigate generic error
        // event.confirm.reject();
      }
    });
  }

  onEdit(row) {
    this.router.navigate(['/pages/catalogue/types/type/' + row.id]);
  }

}
