import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ProductService} from '../../../../catalogue/products/services/product.service';
import {ErrorService} from '../../../services/error.service';
import {ReadableProduct} from '../../../../catalogue/products/models/product.model';

@Injectable()
export class ProductAutocompleteFacade {
  private readonly productService = inject(ProductService);
  private readonly errorService = inject(ErrorService);

  readonly options = signal<ReadableProduct[]>([]);
  private firstOptions: ReadableProduct[] = [];
  private store = '';

  init(store: string, destroyRef: DestroyRef): void {
    this.store = store;
    this.productService.getListOfTinyProducts({store, page: 0, count: 20, name: ''})
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: (page) => {
          this.firstOptions = page.content;
          this.options.set(this.firstOptions);
        },
        error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
      });
  }

  search(name: string): void {
    if (!name) {
      this.resetToFirst();
      return;
    }
    this.productService.getListOfTinyProducts({store: this.store, page: 0, count: 20, name})
      .subscribe({
        next: (page) => this.options.set(page.content),
        error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
      });
  }

  resetToFirst(): void {
    this.options.set(this.firstOptions);
  }
}
