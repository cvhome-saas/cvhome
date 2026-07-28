import {Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {ProductService} from '../services/product.service';
import {ErrorService} from '../../../shared/services/error.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {zip} from 'rxjs';

@Injectable()
export class ProductDetailsFacade {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly productService = inject(ProductService);
  private readonly errorService = inject(ErrorService);
  private readonly selectedStoreService = inject(SelectedStoreService);

  readonly product = signal<any>({});
  readonly store = signal<string>('');

  init(): void {
    zip([this.selectedStoreService.current(), this.activatedRoute.params]).subscribe({
      next: ([selectedStore, params]) => {
        this.store.set(selectedStore);
        const uniqueCode = params.code;
        if (uniqueCode) {
          this.loadProduct(uniqueCode);
        }
      },
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  private loadProduct(code: string): void {
    this.productService.getProductDefinitionById(code).subscribe({
      next: (product) => this.product.set(product),
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }
}
