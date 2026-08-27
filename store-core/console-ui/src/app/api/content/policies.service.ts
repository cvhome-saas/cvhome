/** Console-native; not a port from seller-core. */
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {
  PolicyCompliance,
  PublishPolicyVersionRequest,
  ReadablePolicyVersion,
  SavedContent,
} from '@models/content';
import {CONTENT_PRIVATE} from './content-api';

/** Versions and compliance; the policy head itself goes through `ContentItemsService`. */
@Injectable({providedIn: 'root'})
export class PoliciesService {
  private readonly crudService = inject(CrudService);

  compliance(): Observable<readonly PolicyCompliance[]> {
    return this.crudService.get(`${CONTENT_PRIVATE}/policies/compliance`);
  }

  versions(id: number): Observable<readonly ReadablePolicyVersion[]> {
    return this.crudService.get(`${CONTENT_PRIVATE}/policies/${id}/versions`);
  }

  version(id: number, version: number): Observable<ReadablePolicyVersion> {
    return this.crudService.get(`${CONTENT_PRIVATE}/policies/${id}/versions/${version}`);
  }

  /** Publishes the head (cutting a version when its text changed) and annotates the live version. */
  publishVersion(id: number, body: PublishPolicyVersionRequest): Observable<ReadablePolicyVersion> {
    return this.crudService.post(`${CONTENT_PRIVATE}/policies/${id}/publish-version`, body);
  }

  /** Copies an old version's text back onto the head as its draft text. */
  restoreText(id: number, version: number): Observable<SavedContent> {
    return this.crudService.post(
      `${CONTENT_PRIVATE}/policies/${id}/versions/${version}/restore-text`,
      null,
    );
  }
}
