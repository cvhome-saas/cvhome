import {Injectable, inject, signal, DestroyRef} from '@angular/core';
import {Router} from '@angular/router';
import {TypesService} from '../services/types.service';
import {NbDialogService} from '@nebular/theme';
import {ShowcaseDialogComponent} from '../../../shared/components/showcase-dialog/showcase-dialog.component';
import {ErrorService} from '../../../shared/services/error.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {TableStateService} from '../../../shared/table/table-state.service';
import {StorePageRequest} from '../../../shared/table/table.types';
import {DatatablePageEvent} from '../../../shared/table/table-events';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ReadableProductType} from '../models/product-type.model';

@Injectable()
export class TypesListFacade {
  private readonly typesService = inject(TypesService);
  private readonly router = inject(Router);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly dialogService = inject(NbDialogService);
  private readonly errorService = inject(ErrorService);
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
        error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
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
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
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
            this.errorService.success('OPTION.OPTION_REMOVED');
            this.loadPage();
          },
          error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
        });
      }
    });
  }
}
