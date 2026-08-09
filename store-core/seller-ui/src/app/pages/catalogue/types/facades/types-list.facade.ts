import {Injectable, inject, signal, DestroyRef} from '@angular/core';
import {Router} from '@angular/router';
import {TypesService} from 'seller-core/catalog';
import {NbDialogService} from '@nebular/theme';
import {ShowcaseDialogComponent} from '../../../shared/components/showcase-dialog/showcase-dialog.component';
import {ApiErrorService} from 'seller-core';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {SelectedStoreService} from 'seller-core';
import {TableStateService} from 'seller-core';
import {StorePageRequest} from 'seller-core';
import {DatatablePageEvent} from 'seller-core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ReadableProductType} from 'seller-core/catalog';

@Injectable()
export class TypesListFacade {
  private readonly typesService = inject(TypesService);
  private readonly router = inject(Router);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly dialogService = inject(NbDialogService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);
  readonly tableState = inject(TableStateService<ReadableProductType, StorePageRequest>);

  readonly store = signal<string>('');

  init(destroyRef: DestroyRef): void {
    this.selectedStoreService.current()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: (store) => {
          this.store.set(store || '');
          if (store) {
            this.loadPage();
          }
        },
        error: (err) => this.apiErrors.notify(err)
      });
  }

  loadPage(): void {
    const currentStore = this.store();
    if (!currentStore) return;

    this.tableState.setLoading(true);
    const req: StorePageRequest = {
      ...this.tableState.params(),
      store: currentStore
    };

    this.typesService.getListOfTypes(req).subscribe({
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
    this.loadPage();
  }

  onEdit(row: ReadableProductType): void {
    this.router.navigate(['/pages/catalogue/types/type/' + row.id]);
  }

  createType(): void {
    this.router.navigate(['/pages/catalogue/types/create-type']);
  }

  onDelete(row: ReadableProductType): void {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      }
    }).onClose.subscribe((res) => {
      if (res) {
        this.typesService.deleteType(row.id).subscribe({
          next: () => {
            this.notify.success('OPTION.OPTION_REMOVED');
            this.loadPage();
          },
          error: (err) => this.apiErrors.notify(err)
        });
      }
    });
  }
}
