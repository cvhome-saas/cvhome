import {Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {zip} from 'rxjs';
import {Page} from '../../../shared/models/Page';
import {ProductRelationshipService} from '../product-related/services/product-relationship.service';
import {ErrorService} from '../../../shared/services/error.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';

@Injectable()
export class ProductRelatedFacade {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly productRelationshipService = inject(ProductRelationshipService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly toastr = inject(NbToastrService);
  private readonly translate = inject(TranslateService);
  private readonly errorService = inject(ErrorService);

  readonly store = signal<string>('');
  readonly rows = signal<any[]>([]);
  readonly page = signal<Page>(new Page());

  private product = '';

  init(): void {
    zip([this.selectedStoreService.current(), this.activatedRoute.parent.params]).subscribe({
      next: ([selectedStore, params]) => {
        this.store.set(selectedStore);
        this.product = params['code'];
        this.loadRelationships();
      },
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  private loadRelationships(): void {
    this.productRelationshipService.getRelationships(this.product).subscribe({
      next: (group) => this.setRows(group.products || []),
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  onItemSelect(item: any): void {
    this.productRelationshipService.addProduct(this.product, item.id).subscribe({
      next: () => {
        this.setRows([...this.rows(), item]);
        this.toastr.success(this.translate.instant('PRODUCT_GROUP.PRODUCT_ADDED'));
      },
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  onItemDeSelect(item: any): void {
    this.productRelationshipService.removeProduct(this.product, item.id).subscribe({
      next: () => {
        this.setRows(this.rows().filter((it) => it.id !== item.id));
        this.toastr.success(this.translate.instant('PRODUCT_GROUP.PRODUCT_REMOVED'));
      },
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  onPageChange(pageInfo: any): void {
    this.page.update((current) => ({...current, pageNumber: pageInfo.offset}));
  }

  private setRows(rows: any[]): void {
    this.rows.set(rows);
    this.page.update((current) => ({
      ...current,
      totalPages: 1,
      totalElements: rows.length,
      size: rows.length
    }));
  }
}
