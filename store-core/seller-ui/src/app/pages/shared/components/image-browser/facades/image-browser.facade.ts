import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ContentService} from '../../../../content/services/content.service';
import {ErrorService} from '../../../services/error.service';
import {ContentFileItem} from '../../../../content/models/content.model';

@Injectable()
export class ImageBrowserFacade {
  private readonly contentService = inject(ContentService);
  private readonly errorService = inject(ErrorService);

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
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
  }
}
