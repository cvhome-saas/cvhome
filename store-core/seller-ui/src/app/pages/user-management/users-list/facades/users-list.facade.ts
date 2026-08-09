import {Injectable, inject, signal, DestroyRef} from '@angular/core';
import {Router} from '@angular/router';
import {NbDialogService} from '@nebular/theme';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {UserService} from 'seller-core';
import {ApiErrorService} from 'seller-core';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {SelectedStoreService} from 'seller-core';
import {TableStateService} from 'seller-core';
import {ShowcaseDialogComponent} from '../../../shared/components/showcase-dialog/showcase-dialog.component';
import {DatatablePageEvent} from 'seller-core';
import {StorePageRequest} from 'seller-core';
import {User} from 'seller-core';

@Injectable()
export class UsersListFacade {
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);
  private readonly dialogService = inject(NbDialogService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  readonly tableState = inject(TableStateService<User, StorePageRequest>);

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
        this.apiErrors.notify(err);
      }
    });
  }

  onPageChange(event: DatatablePageEvent): void {
    this.tableState.setParams({
      ...this.tableState.params(),
      page: event.offset
    });
    this.loadData();
  }

  onEdit(row: User): void {
    this.router.navigate(['pages/user-management/user/', row.id]);
  }

  onDelete(row: User): void {
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
            this.notify.success('USER_FORM.USER_REMOVED');
            this.loadData();
          },
          error: (err) => {
            this.apiErrors.notify(err);
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
