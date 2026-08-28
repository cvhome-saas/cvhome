import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {SaasProperties} from '@models/merchant';
import type {Pod} from '@models/pod';

/**
 * Where a store physically is, and what the platform's apex is called — the two halves of the
 * hostname a custom domain has to CNAME to.
 *
 * `saasProperties` answers `{alis, domain}` (`alis` is the platform's own spelling of *alias*) and
 * `storePod` answers the pod, whose `shortenPodId` is the middle of that hostname. Neither is useful
 * alone; `podHostname()` below is the only reason either is fetched.
 */
const TENANCY_API_BASE = '/tenancy/api/v1';

@Injectable({providedIn: 'root'})
export class SaasService {
  private readonly crudService = inject(CrudService);

  /** Public, and genuinely public: two strings that are the same for every tenant. */
  saasProperties(): Observable<SaasProperties> {
    return this.crudService.get(`${TENANCY_API_BASE}/saas/public/saas-properties`);
  }

  /**
   * The pod hosting the current store.
   *
   * Refused for a suspended or archived store — `RouterApi` calls `requireOperable` before answering —
   * so a failure here is a rule about the store rather than a fault, and the domain section degrades to
   * hiding the CNAME target rather than failing the page.
   */
  storePod(): Observable<Pod> {
    return this.crudService.get(`${TENANCY_API_BASE}/router/store-pod-by-store-id`);
  }
}

/**
 * The hostname a custom domain must CNAME to: `{alis}-{shortenPodId}.{domain}`.
 *
 * It is assembled on the client because
 * no endpoint answers it — the two halves come from two services on two different tiers, and neither
 * knows about the other. Returns `null` rather than a half-built hostname when either leg is missing,
 * so the console can say "we could not work out your CNAME target" instead of printing `undefined-.`.
 */
export function podHostname(saas: SaasProperties | null, pod: Pod | null): string | null {
  if (!saas?.alis || !saas.domain || !pod?.shortenPodId) {
    return null;
  }
  return `${saas.alis}-${pod.shortenPodId}.${saas.domain}`;
}
