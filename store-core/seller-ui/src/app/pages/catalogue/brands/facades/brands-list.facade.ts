import {DestroyRef, inject, Injectable, signal} from '@angular/core';
import {Router} from '@angular/router';
import {BrandService} from 'seller-core/catalog';
import {NbDialogService} from '@nebular/theme';
import {ShowcaseDialogComponent} from '../../../shared/components/showcase-dialog/showcase-dialog.component';
import {ApiErrorService} from 'seller-core';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {SelectedStoreService} from 'seller-core';
import {TableStateService} from 'seller-core';
import {StorePageRequest} from 'seller-core';
import {DatatablePageEvent} from 'seller-core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ReadableManufacturer} from 'seller-core/catalog';

@Injectable()
export class BrandsListFacade {
  private readonly brandService = inject(BrandService);
  private readonly router = inject(Router);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly dialogService = inject(NbDialogService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);
  readonly tableState = inject(TableStateService<ReadableManufacturer, StorePageRequest>);

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

    this.brandService.getListOfBrands(req).subscribe({
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

  onEdit(row: ReadableManufacturer): void {
    this.router.navigate(['pages/catalogue/brands/brand/', row.id]);
  }

  createBrand(): void {
    this.router.navigate(['/pages/catalogue/brands/create-brand']);
  }

  onDelete(row: ReadableManufacturer): void {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      }
    }).onClose.subscribe((res) => {
      if (res) {
        this.brandService.deleteBrand(row.id).subscribe({
          next: () => {
            this.notify.success('BRAND.BRAND_REMOVED');
            this.loadPage();
          },
          error: (err) => this.apiErrors.notify(err)
        });
      }
    });
  }
}
