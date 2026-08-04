import {Injectable, inject, signal, DestroyRef} from '@angular/core';
import {Router} from '@angular/router';
import {ProductGroupsService} from '../services/product-groups.service';
import {NbDialogService} from '@nebular/theme';
import {ShowcaseDialogComponent} from '../../../shared/components/showcase-dialog/showcase-dialog.component';
import {ErrorService} from '../../../shared/services/error.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {TableStateService} from '../../../shared/table/table-state.service';
import {StorePageRequest} from '../../../shared/table/table.types';
import {DatatablePageEvent} from '../../../shared/table/table-events';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ReadableProductGroup} from '../models/product-group.model';

@Injectable()
export class GroupsListFacade {
  private readonly productGroupsService = inject(ProductGroupsService);
  private readonly router = inject(Router);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly dialogService = inject(NbDialogService);
  private readonly errorService = inject(ErrorService);
  readonly tableState = inject(TableStateService<ReadableProductGroup, StorePageRequest>);

  readonly store = signal<string>('');

  private readonly recommendedCodes = ['NEWLY_ADDED', 'RECOMMENDED', 'FEATURED_ITEMS', 'HOME_PAGE'];

  init(destroyRef:DestroyRef): void {
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

    this.productGroupsService.getListOfProductGroups(req).subscribe({
      next: (res) => {
        const content = res.content ? [...res.content] : [];
        const missingCodes = this.recommendedCodes.filter((code) => !content.some((e) => e.code === code));
        missingCodes.forEach((code) => {
          content.unshift({code});
        });

        const transformed = {
          ...res,
          size: content.length,
          totalElements: content.length,
          content
        };

        this.tableState.setPage(transformed);
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

  createGroup(): void {
    this.router.navigate(['/pages/catalogue/products-groups/create-products-group']);
  }

  onCreate(event: ReadableProductGroup): void {
    this.router.navigate(['/pages/catalogue/products-groups/create-products-group'], {
      queryParams: {code: event.code}
    });
  }

  onEdit(row: ReadableProductGroup): void {
    this.router.navigate([`/pages/catalogue/products-groups/update-products-group/${row.code}`]);
  }

  onDelete(row: ReadableProductGroup): void {
    this.dialogService.open(ShowcaseDialogComponent, {}).onClose.subscribe((res) => {
      if (res) {
        this.productGroupsService.removeProductGroup(row.code).subscribe({
          next: () => {
            this.loadPage();
          },
          error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
        });
      }
    });
  }
}
