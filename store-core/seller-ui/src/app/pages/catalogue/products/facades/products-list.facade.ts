import {Injectable, inject} from '@angular/core';
import {Router} from '@angular/router';
import {NbDialogService} from '@nebular/theme';
import {PageEvent} from '@swimlane/ngx-datatable';
import {ProductService} from '../services/product.service';
import {ShowcaseDialogComponent} from '../../../shared/components/showcase-dialog/showcase-dialog.component';
import {ErrorService} from '../../../shared/services/error.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {TableStateService} from '../../../shared/table/table-state.service';
import {StorePageRequest} from '../../../shared/table/table.types';
import {ReadableProduct} from '../models/product.model';

export interface ProductFilterPageRequest extends StorePageRequest {
  sku?: string;
  available?: string;
  categoryIds?: string;
  manufacturerId?: string;
}

@Injectable()
export class ProductsListFacade {
  private readonly productService = inject(ProductService);
  private readonly router = inject(Router);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly dialogService = inject(NbDialogService);
  private readonly errorService = inject(ErrorService);
  readonly tableState = inject(TableStateService<ReadableProduct, ProductFilterPageRequest>);

  init(): void {
    const store = this.selectedStoreService.currentSelectedStore();
    if (store) {
      this.tableState.patchParams({store: store.id.id} as ProductFilterPageRequest);
      this.loadPage();
    }
  }

  loadPage(): void {
    this.tableState.setLoading(true);
    this.productService.getListOfProducts(this.tableState.params()).subscribe({
      next: (page) => {
        this.tableState.setPage(page);
        this.tableState.setLoading(false);
      },
      error: (err) => {
        this.tableState.setLoading(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.tableState.patchParams({page: event.offset} as ProductFilterPageRequest);
    this.loadPage();
  }

  filter(): Partial<ProductFilterPageRequest> {
    const params = this.tableState.params();
    return {
      sku: params.sku || '',
      available: params.available || '',
      categoryIds: params.categoryIds || '',
      manufacturerId: params.manufacturerId || ''
    };
  }

  onFilterChange(filter: Partial<ProductFilterPageRequest>): void {
    this.tableState.patchParams(filter);
    this.loadPage();
  }

  resetFilters(): void {
    this.tableState.patchParams({sku: '', available: '', categoryIds: '', manufacturerId: ''});
    this.loadPage();
  }

  onEdit(row: ReadableProduct): void {
    this.router.navigate(['pages/catalogue/products/product/' + row.id]);
  }

  onDelete(row: ReadableProduct): void {
    this.dialogService.open(ShowcaseDialogComponent, {}).onClose.subscribe((res) => {
      if (res) {
        this.productService.deleteProduct(row.id).subscribe({
          next: () => {
            this.errorService.success('PRODUCT.PRODUCT_REMOVED');
            this.loadPage();
          },
          error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
        });
      }
    });
  }

  updateCell(rowIndex: number, field: 'quantity' | 'price' | 'available', value: string | boolean): void {
    const content = [...this.tableState.page().content];
    const row = {...content[rowIndex], [field]: value};
    content[rowIndex] = row;
    this.tableState.setPage({...this.tableState.page(), content});

    this.productService.updateProductFromTable(row.id, {
      available: row.available,
      price: `${row.price}`,
      quantity: row.quantity
    }).subscribe({
      next: () => this.errorService.success('PRODUCT.PRODUCT_UPDATED'),
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }
}
