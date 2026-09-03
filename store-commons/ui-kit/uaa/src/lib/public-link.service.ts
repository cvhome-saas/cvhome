import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService, UI_KIT_CONFIG} from '@cvhome-saas/ui-kit';
import type {AcceptedLink, LinkPreview} from './uaa.models';

/** Which one-time link a page is redeeming. Also the segment of the public path it lives under. */
export type LinkKind = 'INVITATION' | 'PASSWORD_RESET';

const PATHS: Readonly<Record<LinkKind, string>> = {
  INVITATION: '/api/v1/public/invitations',
  PASSWORD_RESET: '/api/v1/public/password-resets',
};

/**
 * The two public endpoints behind uaa's `/accept-invitation` and `/reset-password` pages.
 *
 * Anonymous by design: whoever holds the link may call them, so the preview answers nothing beyond
 * whose account it is and what the password must satisfy, and one `404` code covers a token that is
 * missing, expired, revoked or already spent. They sit on uaa's own origin — uaa serves the pages —
 * and are rate-limited there.
 */
@Injectable({providedIn: 'root'})
export class PublicLinkService {
  private readonly crudService = inject(CrudService);
  private readonly base = inject(UI_KIT_CONFIG).uaaBasePath;

  preview(kind: LinkKind, token: string): Observable<LinkPreview> {
    return this.crudService.get(`${this.base}${PATHS[kind]}/${encodeURIComponent(token)}`);
  }

  accept(kind: LinkKind, token: string, password: string): Observable<AcceptedLink> {
    return this.crudService.post(`${this.base}${PATHS[kind]}/${encodeURIComponent(token)}/accept`, {password});
  }
}
