import {Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {CategoryService} from '../services/category.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {StoreService} from '../../../store-management/services/store.service';
import {ErrorService} from '../../../shared/services/error.service';
import {ReadableMerchantStore} from '../../../store-management/models/store';
import {zip} from 'rxjs';
import {ReadableCategory} from '../models/category.model';

@Injectable()
export class CategoryDetailFacade {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly storeService = inject(StoreService);
  private readonly categoryService = inject(CategoryService);
  private readonly errorService = inject(ErrorService);

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
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  private loadStore(storeCode: string): void {
    this.storeService.getStore(storeCode).subscribe({
      next: (store) => this.store.set(store),
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
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
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }
}
