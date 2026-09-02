/** Console-native; not a port from seller-core. */
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@cvhome-saas/ui-kit';
import type {FaqGroup, FaqReorder} from '@models/content';
import {CONTENT_PRIVATE} from './content-api';

/** FAQ groups and the atomic reorder; entries themselves go through `ContentItemsService` ('faq'). */
@Injectable({providedIn: 'root'})
export class FaqService {
  private readonly crudService = inject(CrudService);

  groups(): Observable<readonly FaqGroup[]> {
    return this.crudService.get(`${CONTENT_PRIVATE}/faq/groups`);
  }

  createGroup(body: FaqGroup): Observable<FaqGroup> {
    return this.crudService.post(`${CONTENT_PRIVATE}/faq/groups`, body);
  }

  updateGroup(id: number, body: FaqGroup): Observable<FaqGroup> {
    return this.crudService.put(`${CONTENT_PRIVATE}/faq/groups/${id}`, body);
  }

  deleteGroup(id: number): Observable<void> {
    return this.crudService.delete(`${CONTENT_PRIVATE}/faq/groups/${id}`);
  }

  /** Moves entries between groups and positions in one write; touched groups are renumbered 0..n. */
  reorder(moves: readonly FaqReorder[]): Observable<void> {
    return this.crudService.patch(`${CONTENT_PRIVATE}/faq/reorder`, moves);
  }
}
