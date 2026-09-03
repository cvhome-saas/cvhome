import {Injectable, computed, inject, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {snapshot} from '@cvhome-saas/ui-kit';
import type {KpiDatum} from '@cvhome-saas/ui-kit';
import type {RankedItem} from '@cvhome-saas/ui-kit/ui';
import {AdminDashboardService, type Dashboard, type DashboardRange, type PostureCheck} from '@cvhome-saas/ui-kit/uaa';

/**
 * The overview.
 *
 * **Every figure comes from the one read.** No tile computes a number the server did not send, and none is
 * carried over from a previous range — a stale figure beside a fresh one is how a dashboard lies.
 *
 * **The posture lines are translated by id, with the server's finding as a parameter.** The check knows what
 * it counted; only the console knows how to say it.
 */
@Injectable()
export class DashboardFacade {
  private readonly dashboard = inject(AdminDashboardService);
  private readonly transloco = inject(TranslocoService);

  readonly range = signal<DashboardRange>('24h');

  private readonly loaded = snapshot(
    () => ({range: this.range()}),
    ({range}) => this.dashboard.get(range),
  );

  readonly isLoading = this.loaded.isLoading;
  readonly error = this.loaded.error;
  readonly reload = () => this.loaded.reload();
  readonly data = computed<Dashboard | null>(() => this.loaded.value() ?? null);

  readonly rangeOptions = computed(() => {
    this.transloco.activeLang();
    return (['24h', '7d', '30d'] as const).map((key) => ({key, label: this.transloco.translate(`dashboard.range.${key}`)}));
  });

  readonly kpis = computed<readonly KpiDatum[]>((): readonly KpiDatum[] => {
    const data = this.data();
    this.transloco.activeLang();
    if (!data) {
      return [];
    }
    const t = (key: string, params?: object) => this.transloco.translate(`dashboard.kpi.${key}`, params);
    return [
      {
        label: t('signIns'),
        value: String(data.signInsTotal),
        icon: 'signIn' as const,
        tone: 'blue' as const,
        flag: t('failures', {count: data.signInFailures}),
      },
      {label: t('tokens'), value: String(data.tokensIssued), icon: 'lock' as const, tone: 'violet' as const},
      {label: t('sessions'), value: String(data.activeSessions), icon: 'users' as const, tone: 'green' as const},
      {
        label: t('users'),
        value: String(data.users.total),
        icon: 'user' as const,
        tone: 'slate' as const,
        flag: t('locked', {count: data.users.locked}),
      },
    ];
  });

  readonly topClients = computed<readonly RankedItem[]>(() =>
    (this.data()?.topClients ?? []).map(({label, value}) => ({label, value})),
  );

  /** The chart's columns, each scaled against the busiest bucket so the tallest bar is full height. */
  readonly chart = computed(() => {
    const buckets = this.data()?.signIns ?? [];
    const peak = Math.max(1, ...buckets.map((b) => b.success + b.failure));
    return buckets.map((bucket) => ({
      at: bucket.at,
      success: bucket.success,
      failure: bucket.failure,
      successHeight: (bucket.success / peak) * 100,
      failureHeight: (bucket.failure / peak) * 100,
    }));
  });

  readonly posture = computed(() => {
    this.transloco.activeLang();
    return (this.data()?.posture ?? []).map((check: PostureCheck) => ({
      ...check,
      text: this.transloco.translate(`dashboard.posture.${check.id}`, {detail: check.detail}),
      tone: check.level === 'OK' ? ('green' as const) : check.level === 'WARN' ? ('amber' as const) : ('red' as const),
    }));
  });

  readonly failures = computed(() => this.data()?.recentFailures ?? []);

  setRange(range: string): void {
    this.range.set(range as DashboardRange);
  }
}
