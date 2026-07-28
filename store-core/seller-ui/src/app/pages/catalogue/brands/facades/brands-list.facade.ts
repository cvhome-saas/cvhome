import {DestroyRef, inject, Injectable, signal} from '@angular/core';
import {Router} from '@angular/router';
import {BrandService} from '../services/brand.service';
import {NbDialogService} from '@nebular/theme';
import {ShowcaseDialogComponent} from '../../../shared/components/showcase-dialog/showcase-dialog.component';
import {ErrorService} from '../../../shared/services/error.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {TableStateService} from '../../../shared/table/table-state.service';
import {StorePageRequest} from '../../../common/BaseTable';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';

@Injectable()
export class BrandsListFacade {
  private readonly brandService = inject(BrandService);
  private readonly router = inject(Router);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly dialogService = inject(NbDialogService);
  private readonly errorService = inject(ErrorService);
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

    this.brandService.getListOfBrands(req).subscribe({
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
    this.router.navigate(['pages/catalogue/brands/brand/', row.id]);
  }

  createBrand(): void {
    this.router.navigate(['/pages/catalogue/brands/create-brand']);
  }

  onDelete(row: any): void {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      }
    }).onClose.subscribe((res) => {
      if (res) {
        this.brandService.deleteBrand(row.id).subscribe({
          next: () => {
            this.errorService.success('BRAND.BRAND_REMOVED');
            this.loadPage();
          },
          error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
        });
      }
    });
  }
}
