import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService, UI_KIT_CONFIG} from '@cvhome-saas/ui-kit';

import type {AuditEventDto} from './admin-audit.service';
import type {UserCounts} from './uaa.models';

/** The overview screen's one read: what happened over the range, and how the realm is configured. */
export const ADMIN_DASHBOARD_API_PATH = '/api/v1/admin/dashboard';

export type DashboardRange = '24h' | '7d' | '30d';

export type PostureLevel = 'OK' | 'WARN' | 'RISK';

export interface DashboardBucket {
  readonly at: string;
  readonly success: number;
  readonly failure: number;
}

export interface RankedValue {
  readonly label: string;
  readonly value: number;
}

/**
 * One line of the security posture, computed from the realm's own data.
 *
 * `detail` is the number or name the check found; what it means is the console's to say, keyed by `id`.
 */
export interface PostureCheck {
  readonly id: string;
  readonly level: PostureLevel;
  readonly detail: string;
}

export interface RailCounts {
  readonly users: number;
  readonly roles: number;
  readonly clients: number;
  readonly identityProviders: number;
}

export interface Dashboard {
  readonly range: DashboardRange;
  readonly from: string;
  readonly to: string;
  readonly signIns: readonly DashboardBucket[];
  readonly signInsTotal: number;
  readonly signInFailures: number;
  readonly tokensIssued: number;
  readonly activeSessions: number;
  readonly users: UserCounts;
  readonly topClients: readonly RankedValue[];
  readonly recentFailures: readonly AuditEventDto[];
  readonly posture: readonly PostureCheck[];
  readonly counts: RailCounts;
}

@Injectable({providedIn: 'root'})
export class AdminDashboardService {
  private readonly crudService = inject(CrudService);
  private readonly base = `${inject(UI_KIT_CONFIG).uaaBasePath}${ADMIN_DASHBOARD_API_PATH}`;

  get(range: DashboardRange = '24h'): Observable<Dashboard> {
    return this.crudService.get(this.base, {range});
  }
}
