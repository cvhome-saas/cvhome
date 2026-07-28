import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ContentService} from '../../../../content/services/content.service';
import {ErrorService} from '../../../services/error.service';

export interface BrowsableImage {
  path: string;
  name: string;
  contentType?: number;
}

@Injectable()
export class ImageBrowserFacade {
  private readonly contentService = inject(ContentService);
  private readonly errorService = inject(ErrorService);

  readonly loading = signal<boolean>(false);
  readonly images = signal<BrowsableImage[]>([]);

  init(destroyRef: DestroyRef): void {
    this.loading.set(true);
    this.contentService.images()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: (data: any) => {
          this.images.set(data.content);
          this.loading.set(false);
        },
        error: (err) => {
          this.loading.set(false);
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
  }
}
