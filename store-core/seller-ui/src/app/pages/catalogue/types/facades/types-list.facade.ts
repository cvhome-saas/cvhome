import {Injectable, inject, signal, DestroyRef} from '@angular/core';
import {Router} from '@angular/router';
import {TypesService} from '../services/types.service';
import {TranslateService} from '@ngx-translate/core';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {ShowcaseDialogComponent} from '../../../shared/components/showcase-dialog/showcase-dialog.component';
import {ErrorService} from '../../../shared/services/error.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {TableStateService} from '../../../shared/table/table-state.service';
import {StorePageRequest} from '../../../common/BaseTable';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';

@Injectable()
export class TypesListFacade {
  private readonly typesService = inject(TypesService);
  private readonly router = inject(Router);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly dialogService = inject(NbDialogService);
  private readonly translate = inject(TranslateService);
  private readonly errorService = inject(ErrorService);
  private readonly toastr = inject(NbToastrService);
  readonly tableState = inject(TableStateService<any, StorePageRequest>);

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

  onPageChange(event: any): void {
    this.tableState.setParams({
      ...this.tableState.params(),
      page: event.offset
    });
    this.loadPage();
  }

  onEdit(row: any): void {
    this.router.navigate(['/pages/catalogue/types/type/' + row.id]);
  }

  createType(): void {
    this.router.navigate(['/pages/catalogue/types/create-type']);
  }

  onDelete(row: any): void {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      }
    }).onClose.subscribe((res) => {
      if (res) {
        this.typesService.deleteType(row.id).subscribe({
          next: () => {
            this.toastr.success(this.translate.instant('OPTION.OPTION_REMOVED'));
            this.loadPage();
          },
          error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
        });
      }
    });
  }
}
