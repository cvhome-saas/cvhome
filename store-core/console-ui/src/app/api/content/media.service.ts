/** Console-native; not a port from seller-core. */
import {HttpEventType, type HttpEvent} from '@angular/common/http';
import {Injectable, inject} from '@angular/core';
import {Observable, filter, map} from 'rxjs';

import {CrudService} from '@cvhome-saas/ui-kit';
import type {PageT} from '@cvhome-saas/ui-kit';
import type {
  MediaAsset,
  MediaFolder,
  MediaKind,
  MediaUsage,
  PersistableMediaAsset,
} from '@models/content';
import {CONTENT_PRIVATE} from './content-api';

export interface MediaQuery {
  readonly folder: number | null;
  readonly kind: MediaKind | null;
  readonly q: string;
  readonly used: boolean | null;
  readonly page: number;
  readonly count: number;
}

/** What an upload reports as it goes: progress, then the stored assets. */
export type UploadEvent =
  | {readonly kind: 'progress'; readonly percent: number}
  | {readonly kind: 'done'; readonly assets: readonly MediaAsset[]};

/**
 * The media library. Uploads are multipart through the service (the platform has no presigned
 * URLs); the controller takes `@RequestParam("files") MultipartFile[]`, so every part is `files`.
 */
@Injectable({providedIn: 'root'})
export class MediaService {
  private readonly crudService = inject(CrudService);

  list(query: MediaQuery): Observable<PageT<MediaAsset>> {
    return this.crudService.get(`${CONTENT_PRIVATE}/media`, {
      folder: query.folder ?? undefined,
      kind: query.kind ?? undefined,
      q: query.q || undefined,
      used: query.used ?? undefined,
      page: query.page,
      count: query.count,
    });
  }

  get(id: number): Observable<MediaAsset> {
    return this.crudService.get(`${CONTENT_PRIVATE}/media/${id}`);
  }

  usage(id: number): Observable<readonly MediaUsage[]> {
    return this.crudService.get(`${CONTENT_PRIVATE}/media/${id}/usage`);
  }

  /** Progress events then the result; `HttpClient` sets the multipart boundary itself. */
  upload(files: readonly File[], folderId: number | null): Observable<UploadEvent> {
    const body = new FormData();
    for (const file of files) {
      body.append('files', file, file.name);
    }
    return this.crudService
      .request('POST', `${CONTENT_PRIVATE}/media`, body, {
        reportProgress: true,
        params: folderId === null ? {} : {folderId},
      })
      .pipe(
        map((event: HttpEvent<unknown>): UploadEvent | null => {
          if (event.type === HttpEventType.UploadProgress) {
            const total = event.total ?? 0;
            return {
              kind: 'progress',
              percent: total ? Math.round((event.loaded / total) * 100) : 0,
            };
          }
          if (event.type === HttpEventType.Response) {
            return {kind: 'done', assets: (event.body as MediaAsset[]) ?? []};
          }
          return null;
        }),
        filter((event): event is UploadEvent => event !== null),
      );
  }

  patch(id: number, body: PersistableMediaAsset): Observable<MediaAsset> {
    return this.crudService.patch(`${CONTENT_PRIVATE}/media/${id}`, body);
  }

  /** 409 `MEDIA.REFERENCED` while something uses it, unless forced. */
  delete(id: number, force = false): Observable<void> {
    return this.crudService.delete(`${CONTENT_PRIVATE}/media/${id}`, {force});
  }

  folders(): Observable<readonly MediaFolder[]> {
    return this.crudService.get(`${CONTENT_PRIVATE}/media/folders`);
  }

  createFolder(body: MediaFolder): Observable<MediaFolder> {
    return this.crudService.post(`${CONTENT_PRIVATE}/media/folders`, body);
  }

  renameFolder(id: number, body: MediaFolder): Observable<MediaFolder> {
    return this.crudService.patch(`${CONTENT_PRIVATE}/media/folders/${id}`, body);
  }

  /** A non-empty folder needs `moveTo` or answers 409 `MEDIA.FOLDER.NOT_EMPTY`. */
  deleteFolder(id: number, moveTo?: number): Observable<void> {
    return this.crudService.delete(`${CONTENT_PRIVATE}/media/folders/${id}`, {moveTo});
  }
}
