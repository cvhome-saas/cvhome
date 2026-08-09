import {Injectable, inject, signal} from '@angular/core';
import {HttpEventType} from '@angular/common/http';
import {Observable, catchError, filter, map, of} from 'rxjs';
import {CrudService} from 'seller-core';

export interface UploadItem {
  file: File;
  progress: number;
  error: boolean;
}

export type UploadResult =
  | {type: 'progress'; item: UploadItem}
  | {type: 'success'; item: UploadItem}
  | {type: 'error'; item: UploadItem; message: string};

@Injectable()
export class ImageUploadingFacade {
  private readonly crudService = inject(CrudService);

  readonly uploadQueue = signal<UploadItem[]>([]);

  uploadFile(file: File, url: string): Observable<UploadResult> {
    const item: UploadItem = {file, progress: 0, error: false};
    this.uploadQueue.update((queue) => [...queue, item]);

    const formData = new FormData();
    formData.append('file', file);

    return this.crudService.request('POST', url, formData, {reportProgress: true}).pipe(
      filter((event) => event.type === HttpEventType.UploadProgress || event.type === HttpEventType.Response),
      map((event): UploadResult => {
        if (event.type === HttpEventType.UploadProgress) {
          if (event.total) {
            item.progress = Math.round(100 * event.loaded / event.total);
          }
          return {type: 'progress', item};
        }
        item.progress = 100;
        return {type: 'success', item};
      }),
      catchError((err) => {
        item.error = true;
        return of<UploadResult>({type: 'error', item, message: err.message || 'Upload failed'});
      })
    );
  }

  removeFromQueue(item: UploadItem): void {
    this.uploadQueue.update((queue) => queue.filter((i) => i !== item));
  }
}
