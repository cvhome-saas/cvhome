import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService, UI_KIT_CONFIG, type SpringPage} from '@cvhome-saas/ui-kit';

/**
 * The audit log, read.
 *
 * **Lists go out comma-joined**, not as repeated parameters: `CrudService` appends one value per key, and
 * Spring binds `type=user.login,user.logout` into the list either way. No event type contains a comma.
 *
 * **The export is an address, not a call.** The browser has to fetch it itself for the file to land in the
 * downloads folder under its own name, and the session cookie is what authorises it — so this service builds
 * the URL and the caller navigates to it.
 */
export const ADMIN_AUDIT_API_PATH = '/api/v1/admin/audit';

export type AuditOutcome = 'SUCCESS' | 'FAILURE';

export type AuditCategory = 'AUTHENTICATION' | 'ADMIN' | 'TOKENS' | 'SECURITY';

export type AuditActorType = 'USER' | 'CLIENT' | 'SYSTEM' | 'ANONYMOUS';

export interface AuditEventDto {
  readonly id: number;
  readonly occurredAt: string;
  readonly eventType: string;
  /** Null for a row written by an older build whose type this one no longer knows. It still reads. */
  readonly category: AuditCategory | null;
  readonly outcome: AuditOutcome;
  readonly reasonCode: string | null;
  readonly actorType: AuditActorType;
  readonly actorId: string | null;
  readonly actorName: string | null;
  readonly targetType: string | null;
  readonly targetId: string | null;
  readonly targetName: string | null;
  readonly clientId: string | null;
  readonly ip: string | null;
  readonly userAgent: string | null;
  readonly before: Record<string, unknown> | null;
  readonly after: Record<string, unknown> | null;
  readonly detail: string | null;
  readonly traceId: string | null;
}

export interface AuditTypeDto {
  readonly type: string;
  readonly category: AuditCategory;
}

/** Every filter is optional; they combine with AND, and an empty search means "the newest events". */
export interface AuditSearch {
  readonly type?: readonly string[];
  readonly category?: readonly AuditCategory[];
  readonly actor?: string | null;
  readonly target?: string | null;
  readonly clientId?: string | null;
  readonly outcome?: AuditOutcome | null;
  readonly ip?: string | null;
  readonly q?: string | null;
  readonly from?: string | null;
  readonly to?: string | null;
}

@Injectable({providedIn: 'root'})
export class AdminAuditService {
  private readonly crudService = inject(CrudService);
  private readonly base = `${inject(UI_KIT_CONFIG).uaaBasePath}${ADMIN_AUDIT_API_PATH}`;

  search(page: number, size: number, search: AuditSearch = {}): Observable<SpringPage<AuditEventDto>> {
    return this.crudService.get(this.base, {...toParams(search), page, size});
  }

  findOne(id: number): Observable<AuditEventDto> {
    return this.crudService.get(`${this.base}/${id}`);
  }

  types(): Observable<readonly AuditTypeDto[]> {
    return this.crudService.get(`${this.base}/types`);
  }

  /** The address of the CSV for this query. Navigate to it; do not fetch it. */
  exportUrl(search: AuditSearch = {}): string {
    const params = toParams(search);
    const query = Object.entries(params)
      .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
      .join('&');
    const path = `${this.crudService.getBaseUrl()}${this.base}/export`;
    return query ? `${path}?${query}` : path;
  }
}

function toParams(search: AuditSearch): Record<string, string> {
  const params: Record<string, string> = {};
  if (search.type?.length) {
    params['type'] = search.type.join(',');
  }
  if (search.category?.length) {
    params['category'] = search.category.join(',');
  }
  const singles: readonly [string, string | null | undefined][] = [
    ['actor', search.actor],
    ['target', search.target],
    ['clientId', search.clientId],
    ['outcome', search.outcome],
    ['ip', search.ip],
    ['q', search.q?.trim()],
    ['from', search.from],
    ['to', search.to],
  ];
  for (const [key, value] of singles) {
    if (value) {
      params[key] = value;
    }
  }
  return params;
}
