import {Injectable, computed, effect, inject, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {forkJoin, of} from 'rxjs';

import {ApiErrorService, snapshot} from '@cvhome-saas/ui-kit';
import {
  AdminAuditService,
  type AuditCategory,
  type AuditEventDto,
  type AuditOutcome,
  type AuditSearch,
} from '@cvhome-saas/ui-kit/uaa';

const PAGE_SIZE = 50;

export const CATEGORIES: readonly AuditCategory[] = ['AUTHENTICATION', 'ADMIN', 'TOKENS', 'SECURITY'];

export type RangeKey = '24h' | '7d' | '30d' | 'all';

/**
 * The audit log: a filtered page of events, and one event read closely.
 *
 * **Filters are the query, not a client-side sieve.** Everything narrows server-side and the page resets to
 * the first — filtering to a page 7 that no longer exists is how a screen shows an empty log and looks broken.
 *
 * **Live is polling, deliberately.** Ten seconds, only on the first page and only while the tab is visible:
 * an audit log people watch during an incident is worth a request; one behind a background tab is not.
 */
@Injectable()
export class AuditFacade {
  private readonly audit = inject(AdminAuditService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly transloco = inject(TranslocoService);

  readonly page = signal(0);
  readonly query = signal('');
  readonly category = signal<AuditCategory | null>(null);
  readonly outcome = signal<AuditOutcome | null>(null);
  readonly range = signal<RangeKey>('24h');
  readonly actor = signal<string | null>(null);
  readonly ip = signal<string | null>(null);
  readonly selected = signal<AuditEventDto | null>(null);
  readonly live = signal(false);

  /** What the current filters mean as a query — the same object the export URL is built from. */
  readonly search = computed<AuditSearch>(() => ({
    q: this.query() || null,
    category: this.category() ? [this.category() as AuditCategory] : undefined,
    outcome: this.outcome(),
    actor: this.actor(),
    ip: this.ip(),
    from: fromOf(this.range()),
  }));

  private readonly loaded = snapshot(
    () => ({page: this.page(), search: this.search(), tick: this.tick()}),
    ({page, search}) => forkJoin({events: this.audit.search(page, PAGE_SIZE, search), types: of(null)}),
  );

  readonly isLoading = this.loaded.isLoading;
  readonly error = this.loaded.error;
  readonly reload = () => this.loaded.reload();
  readonly rows = computed<readonly AuditEventDto[]>(() => this.loaded.value()?.events.content ?? []);
  readonly total = computed(() => this.loaded.value()?.events.totalElements ?? 0);
  readonly totalPages = computed(() => this.loaded.value()?.events.totalPages ?? 0);
  readonly pageSize = PAGE_SIZE;

  /** A counter the reload depends on: bumping it is how polling asks for the same page again. */
  private readonly tick = signal(0);

  constructor() {
    effect((onCleanup) => {
      if (!this.live()) {
        return;
      }
      const timer = setInterval(() => {
        if (document.visibilityState === 'visible' && this.page() === 0) {
          this.tick.update((n) => n + 1);
        }
      }, 10_000);
      onCleanup(() => clearInterval(timer));
    });
  }

  readonly exportUrl = computed(() => this.audit.exportUrl(this.search()));

  readonly categoryOptions = computed(() => {
    this.transloco.activeLang();
    return [
      {key: 'all', label: this.transloco.translate('audit.category.all')},
      ...CATEGORIES.map((key) => ({key, label: this.transloco.translate(`audit.category.${key}`)})),
    ];
  });

  readonly rangeOptions = computed(() => {
    this.transloco.activeLang();
    return (['24h', '7d', '30d', 'all'] as const).map((key) => ({key, label: this.transloco.translate(`audit.range.${key}`)}));
  });

  setQuery(value: string): void {
    this.query.set(value);
    this.page.set(0);
  }

  setCategory(key: string): void {
    this.category.set(key === 'all' ? null : (key as AuditCategory));
    this.page.set(0);
  }

  setRange(key: string): void {
    this.range.set(key as RangeKey);
    this.page.set(0);
  }

  toggleOutcome(value: AuditOutcome): void {
    this.outcome.update((held) => (held === value ? null : value));
    this.page.set(0);
  }

  /** The pivots: everything one actor did, everything from one address. */
  pivotToActor(event: AuditEventDto): void {
    this.actor.set(event.actorName ?? event.actorId);
    this.ip.set(null);
    this.page.set(0);
    this.selected.set(null);
  }

  pivotToIp(event: AuditEventDto): void {
    this.ip.set(event.ip);
    this.actor.set(null);
    this.page.set(0);
    this.selected.set(null);
  }

  clearPivots(): void {
    this.actor.set(null);
    this.ip.set(null);
    this.page.set(0);
  }

  readonly hasPivot = computed(() => !!this.actor() || !!this.ip());

  select(event: AuditEventDto): void {
    this.selected.set(event);
  }

  dismiss(): void {
    this.selected.set(null);
  }

  goTo(page: number): void {
    this.page.set(page);
  }

  /** The changed fields of one event, as before/after pairs a table can render. */
  changes(event: AuditEventDto): readonly {field: string; before: string; after: string}[] {
    const before = event.before ?? {};
    const after = event.after ?? {};
    const fields = new Set([...Object.keys(before), ...Object.keys(after)]);
    return [...fields].sort().map((field) => ({
      field,
      before: format(before[field]),
      after: format(after[field]),
    }));
  }

  message(failure: unknown): string {
    return this.apiErrors.messageFor(failure);
  }
}

function fromOf(range: RangeKey): string | null {
  if (range === 'all') {
    return null;
  }
  const hours = range === '24h' ? 24 : range === '7d' ? 24 * 7 : 24 * 30;
  return new Date(Date.now() - hours * 3600_000).toISOString();
}

function format(value: unknown): string {
  if (value === null || value === undefined) {
    return '—';
  }
  return typeof value === 'object' ? JSON.stringify(value) : String(value);
}
