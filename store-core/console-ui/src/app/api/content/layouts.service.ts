/** Console-native; not a port from seller-core. */
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@cvhome-saas/ui-kit';
import type {
  LayoutDocument,
  LayoutRevisionRow,
  LayoutSection,
  PageKind,
  PublishedLayout,
  ReadableLayout,
  SavedSection,
} from '@models/layout';

import {CONTENT_PRIVATE} from './content-api';

/**
 * The storefront builder's write surface: one layout document per page, edited as a draft and published
 * whole. Every mutating call carries the `draftVersion` the builder loaded (`baseVersion`); the server
 * answers a stale one with a 409 and the builder reloads rather than clobbering a parallel save.
 */
@Injectable({providedIn: 'root'})
export class LayoutsService {
  private readonly crudService = inject(CrudService);

  private readonly base = `${CONTENT_PRIVATE}/layouts`;

  get(page: PageKind): Observable<ReadableLayout> {
    return this.crudService.get(`${this.base}/${page}`);
  }

  save(page: PageKind, document: LayoutDocument, baseVersion: number): Observable<ReadableLayout> {
    return this.crudService.put(`${this.base}/${page}`, {document, baseVersion});
  }

  publish(page: PageKind, baseVersion: number): Observable<PublishedLayout> {
    return this.crudService.post(`${this.base}/${page}/publish`, {baseVersion});
  }

  discard(page: PageKind, baseVersion: number): Observable<ReadableLayout> {
    return this.crudService.post(`${this.base}/${page}/discard`, {baseVersion});
  }

  revisions(page: PageKind): Observable<LayoutRevisionRow[]> {
    return this.crudService.get(`${this.base}/${page}/revisions`);
  }

  restore(page: PageKind, version: number): Observable<ReadableLayout> {
    return this.crudService.post(`${this.base}/${page}/revisions/${version}/restore`, null);
  }

  /** A 30-minute token the storefront accepts to render this page's draft (`?preview=<token>`). */
  previewToken(page: PageKind): Observable<{token: string}> {
    return this.crudService.post(`${this.base}/${page}/preview-token`, null);
  }

  // ------------------------------------------------------------------------------------ saved sections

  sectionPresets(): Observable<SavedSection[]> {
    return this.crudService.get(`${this.base}/section-presets`);
  }

  saveSectionPreset(name: string, section: LayoutSection): Observable<SavedSection> {
    return this.crudService.post(`${this.base}/section-presets`, {name, section});
  }

  deleteSectionPreset(id: number): Observable<void> {
    return this.crudService.delete(`${this.base}/section-presets/${id}`);
  }
}
