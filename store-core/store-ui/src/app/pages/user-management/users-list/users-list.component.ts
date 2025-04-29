import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';

import {UserService} from '../../../shared/services/user.service';
import {TranslateService} from '@ngx-translate/core';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {ShowcaseDialogComponent} from "../../../shared/components/showcase-dialog/showcase-dialog.component";
import {NbDialogService, NbToastrService} from "@nebular/theme";
import {ErrorService} from "../../../shared/services/error.service";
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {BaseTable, PageT, StorePageRequest} from "../../common/BaseTable";
import {Observable, of} from "rxjs";
import {map} from "rxjs/operators";


@Component({
  selector: 'ngx-users-list',
  standalone: false,
  templateUrl: './users-list.component.html',
  styleUrls: ['./users-list.component.scss']
})
export class UsersListComponent extends BaseTable<any> implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  private isInitialized: boolean = false;

  constructor(
    private userService: UserService,
    private router: Router,
    private dialogService: NbDialogService,
    private toastr: NbToastrService,
    translate: TranslateService,
    errorService: ErrorService,
    selectedStoreService: SelectedStoreService
  ) {
    super(selectedStoreService, translate, errorService)
  }

  ngOnInit() {
    this.isInitialized = true;
    this.trigger();
  }

  override list(request: StorePageRequest): Observable<PageT<any>> {
    if (!super.params.store || !this.isInitialized) {
      return of();
    }
    return this.userService.getUsersList(request)
      .pipe(map(it => {
        const mappedX = {
          content: it.data,
          totalPages: 1,
          totalElements: it.data.length,
          size: it.data.length,
          pageNumber: request.page
        };
        return mappedX;
      }));
  }


  onEdit(event) {
    this.router.navigate(['pages/user-management/user/', event.id]);
  }

  onDelete(event) {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      }
    }).onClose.subscribe(res => {
      if (res) {
        this.userService.deleteUser(event.id, this.params.store)
          .subscribe(data => {
            this.toastr.success(this.translate.instant('USER_FORM.USER_REMOVED'));
            this.trigger();
          }, err => {
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          });
      }
    });
  }


  createUser() {
    this.router.navigate(['/pages/user-management/create-user/', this.params.store])
  }
}
