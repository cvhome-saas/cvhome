import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ActivatedRoute} from '@angular/router';
import {zip} from 'rxjs';
import {EMPTY_PAGE, PageT} from '../../../shared/table/table.types';
import {ProductRelationshipService} from '../product-related/services/product-relationship.service';
import {ApiErrorService} from '../../../../core/errors/api-error.service';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {DatatablePageEvent} from '../../../shared/table/table-events';
import {ProductGroupItem} from '../../products-groups/models/product-group.model';

@Injectable()
export class ProductRelatedFacade {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly productRelationshipService = inject(ProductRelationshipService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);

  readonly store = signal<string>('');
  readonly rows = signal<ProductGroupItem[]>([]);
  readonly page = signal<PageT<never>>(EMPTY_PAGE);

  private product = '';

  init(destroyRef: DestroyRef): void {
    zip([this.selectedStoreService.current(), this.activatedRoute.parent.params])
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: ([selectedStore, params]) => {
          this.store.set(selectedStore);
          this.product = params['code'];
          this.loadRelationships();
        },
        error: (err) => this.apiErrors.notify(err)
      });
  }

  private loadRelationships(): void {
    this.productRelationshipService.getRelationships(this.product).subscribe({
      next: (group) => this.setRows(group.products || []),
      error: (err) => this.apiErrors.notify(err)
    });
  }

  onItemSelect(item: ProductGroupItem): void {
    this.productRelationshipService.addProduct(this.product, item.id).subscribe({
      next: () => {
        this.setRows([...this.rows(), item]);
        this.notify.success('PRODUCT_GROUP.PRODUCT_ADDED');
      },
      error: (err) => this.apiErrors.notify(err)
    });
  }

  onItemDeSelect(item: ProductGroupItem): void {
    this.productRelationshipService.removeProduct(this.product, item.id).subscribe({
      next: () => {
        this.setRows(this.rows().filter((it) => it.id !== item.id));
        this.notify.success('PRODUCT_GROUP.PRODUCT_REMOVED');
      },
      error: (err) => this.apiErrors.notify(err)
    });
  }

  onPageChange(pageInfo: DatatablePageEvent): void {
    this.page.update((current) => ({...current, pageNumber: pageInfo.offset}));
  }

  private setRows(rows: ProductGroupItem[]): void {
    this.rows.set(rows);
    this.page.update((current) => ({
      ...current,
      totalPages: 1,
      totalElements: rows.length,
      size: rows.length
    }));
  }
}
