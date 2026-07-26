import {Injectable, inject, signal, DestroyRef} from '@angular/core';
import {Router} from '@angular/router';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {UserService} from '../../../shared/services/user.service';
import {ErrorService} from '../../../shared/services/error.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {TableStateService} from '../../../shared/table/table-state.service';
import {ShowcaseDialogComponent} from '../../../shared/components/showcase-dialog/showcase-dialog.component';

@Injectable()
export class UsersListFacade {
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);
  private readonly dialogService = inject(NbDialogService);
  private readonly toastr = inject(NbToastrService);
  private readonly translate = inject(TranslateService);
  private readonly errorService = inject(ErrorService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  readonly tableState = inject(TableStateService);

  readonly store = signal<string>('');

  init(destroyRef: DestroyRef): void {
    this.selectedStoreService.current()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe((store) => {
        this.store.set(store || '');
        this.tableState.setParams({
          ...this.tableState.params(),
          store: store || ''
        });
        if (store) {
          this.loadData();
        } else {
          this.tableState.setPage({ content: [], totalElements: 0, pageNumber: 0, size: 0, totalPages: 0 });
        }
      });
  }

  loadData(): void {
    const params = this.tableState.params();
    if (!params.store) {
      return;
    }
    this.tableState.setLoading(true);
    this.userService.getUsersList(params).subscribe({
      next: (res) => {
        this.tableState.setPage(res);
        this.tableState.setLoading(false);
      },
      error: (err) => {
        this.tableState.setLoading(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  onPageChange(event: any): void {
    this.tableState.setParams({
      ...this.tableState.params(),
      page: event.offset
    });
    this.loadData();
  }

  onEdit(row: any): void {
    this.router.navigate(['pages/user-management/user/', row.id]);
  }

  onDelete(row: any): void {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      }
    }).onClose.subscribe((res) => {
      if (res) {
        const store = this.store();
        this.userService.deleteUser(row.id, store).subscribe({
          next: () => {
            this.toastr.success(this.translate.instant('USER_FORM.USER_REMOVED'));
            this.loadData();
          },
          error: (err) => {
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          }
        });
      }
    });
  }

  createUser(): void {
    const store = this.store();
    if (store) {
      this.router.navigate(['/pages/user-management/create-user/', store]);
    }
  }
}
