import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ActivatedRoute} from '@angular/router';
import {forkJoin, zip} from 'rxjs';
import {CategoryService} from '../../categories/services/category.service';
import {ProductService} from '../services/product.service';
import {ApiErrorService} from '../../../../core/errors/api-error.service';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {ReadableCategory} from '../../categories/models/category.model';

const PER_PAGE = 50; // ideally display all categories

@Injectable()
export class ProductToCategoryFacade {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly categoryService = inject(CategoryService);
  private readonly productService = inject(ProductService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);

  readonly loading = signal<boolean>(false);
  readonly categories = signal<{ id: string; name: string }[]>([]);
  readonly selectedItems = signal<string[]>([]);

  private store = '';
  private uniqueCode = '';

  init(destroyRef: DestroyRef): void {
    zip([this.selectedStoreService.current(), this.activatedRoute.parent.params])
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: ([selectedStore, params]) => {
          this.store = selectedStore;
          this.uniqueCode = params['code'];
          this.load();
        },
        error: (err) => this.apiErrors.notify(err)
      });
  }

  onSelectionChange(newItems: string[]): void {
    const oldItems = this.selectedItems();
    if (newItems.length > oldItems.length) {
      const added = newItems.filter((it) => !oldItems.includes(it))[0];
      this.addProductToCategory(added);
    } else if (oldItems.length > newItems.length) {
      const removed = oldItems.filter((it) => !newItems.includes(it))[0];
      this.removeProductFromCategory(removed);
    }
  }

  private addProductToCategory(categoryId: string): void {
    this.productService.addProductToCategory(this.uniqueCode, categoryId).subscribe({
      next: () => {
        this.notify.success('PRODUCT.PRODUCT_TO_CATEGORY_ADDED');
        this.load();
      },
      error: (err) => this.apiErrors.notify(err)
    });
  }

  private removeProductFromCategory(categoryId: string): void {
    this.productService.removeProductFromCategory(this.uniqueCode, categoryId).subscribe({
      next: () => {
        this.notify.success('PRODUCT.PRODUCT_TO_CATEGORY_REMOVED');
        this.load();
      },
      error: (err) => this.apiErrors.notify(err)
    });
  }

  private load(): void {
    this.loading.set(true);

    const params = {store: this.store, count: PER_PAGE, page: 0};
    const productCategories$ = this.categoryService.getCategoryByProductId(this.uniqueCode);
    const allCategories$ = this.categoryService.getListOfCategories(params);

    forkJoin([productCategories$, allCategories$]).subscribe({
      next: ([productCategories, allCategories]) => {
        this.selectedItems.set(productCategories.content.map((data: ReadableCategory) => `${data.id}`));
        this.categories.set(allCategories.content.map((value: ReadableCategory) => ({id: `${value.id}`, name: value.code})));
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.apiErrors.notify(err);
      }
    });
  }
}
