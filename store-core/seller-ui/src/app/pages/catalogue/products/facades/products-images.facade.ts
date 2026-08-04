import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ActivatedRoute} from '@angular/router';
import {zip} from 'rxjs';
import {ProductImageService} from '../services/product-image.service';
import {ApiErrorService} from '../../../../core/errors/api-error.service';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {ReadableImage} from '../models/product.model';

@Injectable()
export class ProductsImagesFacade {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly productImageService = inject(ProductImageService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);

  readonly images = signal<ReadableImage[]>(null);
  readonly loading = signal<boolean>(false);
  readonly loaded = signal<boolean>(false);
  readonly addImageUrl = signal<string>('');
  readonly deleteImageUrl = signal<string>('');

  private uniqueCode = '';

  init(destroyRef: DestroyRef): void {
    zip([this.selectedStoreService.current(), this.activatedRoute.parent.params])
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: ([, params]) => {
          this.uniqueCode = params['code'];
          this.addImageUrl.set(this.productImageService.addImageUrl(this.uniqueCode));
          this.deleteImageUrl.set(this.productImageService.removeImageUrl(this.uniqueCode));
          this.load();
        },
        error: (err) => this.apiErrors.notify(err)
      });
  }

  load(): void {
    this.loading.set(true);
    this.productImageService.getImages(this.uniqueCode).subscribe({
      next: (res) => {
        this.images.set(res);
        this.loading.set(false);
        this.loaded.set(true);
      },
      error: (err) => {
        this.loading.set(false);
        this.apiErrors.notify(err);
      }
    });
  }

  removeImage(imageId: string): void {
    this.loading.set(true);
    this.productImageService.removeImage(this.uniqueCode, imageId).subscribe({
      next: () => {
        this.load();
        this.notify.success('PRODUCT.PRODUCT_UPDATED');
      },
      error: (err) => {
        this.loading.set(false);
        this.apiErrors.notify(err);
      }
    });
  }

  updateImage(event: { id: string | number; position: number }): void {
    this.loading.set(true);
    this.productImageService.updateImage(this.uniqueCode, event).subscribe({
      next: () => this.load(),
      error: (err) => {
        this.loading.set(false);
        this.apiErrors.notify(err);
      }
    });
  }

  errorImage(event: string): void {
    this.notify.dangerKey('COMMON.' + event);
  }

  addedImage(): void {
    this.load();
    this.notify.success('PRODUCT.PRODUCT_UPDATED');
  }

  fileAdded(): void {
    this.load();
  }
}
