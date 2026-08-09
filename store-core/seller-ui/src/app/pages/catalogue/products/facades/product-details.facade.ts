import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ActivatedRoute} from '@angular/router';
import {ProductService} from 'seller-core/catalog';
import {ApiErrorService} from 'seller-core';
import {SelectedStoreService} from 'seller-core';
import {zip} from 'rxjs';
import {ReadableProductDefinition} from 'seller-core/catalog';

@Injectable()
export class ProductDetailsFacade {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly productService = inject(ProductService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly selectedStoreService = inject(SelectedStoreService);

  readonly product = signal<ReadableProductDefinition>({});
  readonly store = signal<string>('');

  init(destroyRef: DestroyRef): void {
    zip([this.selectedStoreService.current(), this.activatedRoute.params])
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: ([selectedStore, params]) => {
          this.store.set(selectedStore);
          const uniqueCode = params.code;
          if (uniqueCode) {
            this.loadProduct(uniqueCode);
          }
        },
        error: (err) => this.apiErrors.notify(err)
      });
  }

  private loadProduct(code: string): void {
    this.productService.getProductDefinitionById(code).subscribe({
      next: (product) => this.product.set(product),
      error: (err) => this.apiErrors.notify(err)
    });
  }
}
