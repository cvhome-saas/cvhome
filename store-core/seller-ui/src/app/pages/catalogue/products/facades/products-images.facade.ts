import {Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {zip} from 'rxjs';
import {ProductImageService} from '../services/product-image.service';
import {ErrorService} from '../../../shared/services/error.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';

@Injectable()
export class ProductsImagesFacade {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly productImageService = inject(ProductImageService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly toastr = inject(NbToastrService);
  private readonly translate = inject(TranslateService);
  private readonly errorService = inject(ErrorService);

  readonly images = signal<any>(null);
  readonly loading = signal<boolean>(false);
  readonly loaded = signal<boolean>(false);
  readonly addImageUrl = signal<string>('');
  readonly deleteImageUrl = signal<string>('');

  private uniqueCode = '';

  init(): void {
    zip([this.selectedStoreService.current(), this.activatedRoute.parent.params]).subscribe({
      next: ([, params]) => {
        this.uniqueCode = params['code'];
        this.addImageUrl.set(this.productImageService.addImageUrl(this.uniqueCode));
        this.deleteImageUrl.set(this.productImageService.removeImageUrl(this.uniqueCode));
        this.load();
      },
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
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
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  removeImage(imageId: string): void {
    this.loading.set(true);
    this.productImageService.removeImage(this.uniqueCode, imageId).subscribe({
      next: () => {
        this.load();
        this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_UPDATED'));
      },
      error: (err) => {
        this.loading.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  updateImage(event: any): void {
    this.loading.set(true);
    this.productImageService.updateImage(this.uniqueCode, event).subscribe({
      next: () => this.load(),
      error: (err) => {
        this.loading.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  errorImage(event: string): void {
    this.toastr.danger(this.translate.instant('COMMON.' + event));
  }

  addedImage(): void {
    this.load();
    this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_UPDATED'));
  }

  fileAdded(): void {
    this.load();
  }
}
