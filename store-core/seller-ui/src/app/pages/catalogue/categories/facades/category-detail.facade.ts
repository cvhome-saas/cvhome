import {Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {CategoryService} from 'seller-core/catalog';
import {SelectedStoreService} from 'seller-core';
import {StoreService} from 'seller-core/stores';
import {ApiErrorService} from 'seller-core';
import {ReadableMerchantStore} from 'seller-core/stores';
import {zip} from 'rxjs';
import {ReadableCategory} from 'seller-core/catalog';

@Injectable()
export class CategoryDetailFacade {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly storeService = inject(StoreService);
  private readonly categoryService = inject(CategoryService);
  private readonly apiErrors = inject(ApiErrorService);

  readonly category = signal<ReadableCategory | null>(null);
  readonly store = signal<ReadableMerchantStore | null>(null);
  readonly loading = signal<boolean>(false);

  init(): void {
    this.loading.set(true);
    zip([this.selectedStoreService.current(), this.activatedRoute.params]).subscribe({
      next: ([selectedStore, params]) => {
        const id = params.id;
        if (selectedStore) {
          this.loadStore(selectedStore);
        }
        if (id) {
          this.loadCategory(id);
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.apiErrors.notify(err);
      }
    });
  }

  private loadStore(storeCode: string): void {
    this.storeService.getStore(storeCode).subscribe({
      next: (store) => this.store.set(store),
      error: (err) => this.apiErrors.notify(err)
    });
  }

  private loadCategory(id: string): void {
    this.categoryService.getCategoryById(id).subscribe({
      next: (category) => {
        this.category.set(category);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.apiErrors.notify(err);
      }
    });
  }
}
