import {Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {forkJoin, zip} from 'rxjs';
import {CategoryService} from '../../categories/services/category.service';
import {ProductService} from '../services/product.service';
import {ErrorService} from '../../../shared/services/error.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';

const PER_PAGE = 50; // ideally display all categories

@Injectable()
export class ProductToCategoryFacade {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly categoryService = inject(CategoryService);
  private readonly productService = inject(ProductService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly toastr = inject(NbToastrService);
  private readonly translate = inject(TranslateService);
  private readonly errorService = inject(ErrorService);

  readonly loading = signal<boolean>(false);
  readonly categories = signal<{ id: string; name: string }[]>([]);
  readonly selectedItems = signal<string[]>([]);

  private store = '';
  private uniqueCode = '';

  init(): void {
    zip([this.selectedStoreService.current(), this.activatedRoute.parent.params]).subscribe({
      next: ([selectedStore, params]) => {
        this.store = selectedStore;
        this.uniqueCode = params['code'];
        this.load();
      },
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
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
        this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_TO_CATEGORY_ADDED'));
        this.load();
      },
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  private removeProductFromCategory(categoryId: string): void {
    this.productService.removeProductFromCategory(this.uniqueCode, categoryId).subscribe({
      next: () => {
        this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_TO_CATEGORY_REMOVED'));
        this.load();
      },
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  private load(): void {
    this.loading.set(true);

    const params = {store: this.store, count: PER_PAGE, page: 0};
    const productCategories$ = this.categoryService.getCategoryByProductId(this.uniqueCode);
    const allCategories$ = this.categoryService.getListOfCategories(params);

    forkJoin([productCategories$, allCategories$]).subscribe({
      next: ([productCategories, allCategories]) => {
        this.selectedItems.set(productCategories.content.map((data: any) => data.id));
        this.categories.set(allCategories.content.map((value: any) => ({id: value.id, name: value.code})));
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }
}
