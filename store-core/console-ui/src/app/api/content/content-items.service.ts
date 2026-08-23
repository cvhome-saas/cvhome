/** Console-native; not a port from seller-core. */
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {PageRequest} from '@models/page';
import type {
  BulkAction,
  BulkResult,
  ContentListQuery,
  ContentListType,
  ContentPage,
  ContentTranslation,
  PersistableContent,
  PreviewToken,
  PublishRequest,
  ReadableRevision,
  SavedContent,
  TransitionAction,
} from '@models/content';
import {CONTENT_PRIVATE} from './content-api';

/**
 * The surface every workflow type shares — `WorkflowContentApi` on the server, one controller per
 * type under `/private/content/{pages|posts|banners|faq|policies}`. The type is a parameter here
 * rather than a class per type because the five differ only in their DTO, and the DTO is a generic.
 *
 * `?store=` (and `lang=`, the locale the console is editing in) are stamped by the request context;
 * the `lang` matters on writes: it is the "source" locale the publish gate and stale-marking use.
 */
@Injectable({providedIn: 'root'})
export class ContentItemsService {
  private readonly crudService = inject(CrudService);

  list(type: ContentListType, query: ContentListQuery, page: PageRequest): Observable<ContentPage> {
    return this.crudService.get(`${CONTENT_PRIVATE}/${type}`, {
      ...page,
      status: query.status ?? undefined,
      locale: query.locale ?? undefined,
      state: query.state ?? undefined,
      q: query.q || undefined,
    });
  }

  get<T>(type: ContentListType, id: number): Observable<T> {
    return this.crudService.get(`${CONTENT_PRIVATE}/${type}/${id}`);
  }

  create<B extends PersistableContent>(type: ContentListType, body: B): Observable<SavedContent> {
    return this.crudService.post(`${CONTENT_PRIVATE}/${type}`, body);
  }

  update<B extends PersistableContent>(
    type: ContentListType,
    id: number,
    body: B,
  ): Observable<SavedContent> {
    return this.crudService.put(`${CONTENT_PRIVATE}/${type}/${id}`, body);
  }

  delete(type: ContentListType, id: number, force = false): Observable<void> {
    return this.crudService.delete(`${CONTENT_PRIVATE}/${type}/${id}`, {force});
  }

  /** `publish` takes an optional window; a future `publishAt` schedules instead. */
  transition(
    type: ContentListType,
    id: number,
    action: TransitionAction,
    body: PublishRequest | null = null,
  ): Observable<SavedContent> {
    return this.crudService.post(`${CONTENT_PRIVATE}/${type}/${id}/${action}`, body);
  }

  /** Writes one locale without touching the others. */
  updateTranslation(
    type: ContentListType,
    id: number,
    locale: string,
    body: ContentTranslation,
  ): Observable<SavedContent> {
    return this.crudService.put(`${CONTENT_PRIVATE}/${type}/${id}/translations/${locale}`, body);
  }

  revisions(type: ContentListType, id: number): Observable<readonly ReadableRevision[]> {
    return this.crudService.get(`${CONTENT_PRIVATE}/${type}/${id}/revisions`);
  }

  restoreRevision(type: ContentListType, id: number, version: number): Observable<SavedContent> {
    return this.crudService.post(
      `${CONTENT_PRIVATE}/${type}/${id}/revisions/${version}/restore`,
      null,
    );
  }

  /** `{exists: true}` means the slug is free — the legacy "exists" pre-flight shape, kept on purpose. */
  slugAvailable(
    type: ContentListType,
    slug: string,
    excludeId?: number,
  ): Observable<{exists: boolean}> {
    return this.crudService.get(`${CONTENT_PRIVATE}/${type}/slug-available`, {slug, excludeId});
  }

  previewToken(type: ContentListType, id: number): Observable<PreviewToken> {
    return this.crudService.post(`${CONTENT_PRIVATE}/${type}/${id}/preview-token`, null);
  }

  /** 207: one result per id, never a whole-batch failure. */
  bulk(
    type: ContentListType,
    ids: readonly number[],
    action: BulkAction,
  ): Observable<readonly BulkResult[]> {
    return this.crudService.post(`${CONTENT_PRIVATE}/${type}/bulk`, {ids, action});
  }
}
