import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ContentService} from 'seller-core/content';
import {ApiErrorService} from 'seller-core';
import {ContentFileItem} from 'seller-core/content';

@Injectable()
export class ImageBrowserFacade {
  private readonly contentService = inject(ContentService);
  private readonly apiErrors = inject(ApiErrorService);

  readonly loading = signal<boolean>(false);
  readonly images = signal<ContentFileItem[]>([]);

  init(destroyRef: DestroyRef): void {
    this.loading.set(true);
    this.contentService.images()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: (data) => {
          this.images.set(data.content);
          this.loading.set(false);
        },
        error: (err) => {
          this.loading.set(false);
          this.apiErrors.notify(err);
        }
      });
  }
}
