import {Injectable, computed, inject, linkedSignal, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {ContentCache} from '@api/content/content-cache';
import {ContentItemsService} from '@api/content/content-items.service';
import {ApiErrorService, EMPTY_PAGE} from '@cvhome-saas/ui-kit';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {
  CONTENT_STATUSES,
  NO_CONTENT_QUERY,
  type BulkAction,
  type ContentListQuery,
  type ContentListType,
  type ContentPage,
  type ContentRow,
  type ContentStatus,
  type TransitionAction,
} from '@models/content';
import type {TabItem} from '@shared/ui/tab-switcher/tab-switcher';
import {ToastService} from '@shared/ui/toast/toast';

export const CONTENT_PAGE_SIZE = 25;

/** What the status filter offers: "all" plus every status. */
export type StatusTab = 'all' | ContentStatus;

/** Which transitions a row in a given status may take — mirrors `ContentStatus.canTransitionTo`. */
const TRANSITIONS: Readonly<Record<ContentStatus, readonly TransitionAction[]>> = {
  DRAFT: ['publish', 'submit-review', 'archive'],
  REVIEW: ['publish', 'unpublish', 'archive'],
  SCHEDULED: ['publish', 'unpublish', 'archive'],
  PUBLISHED: ['unpublish', 'archive'],
  ARCHIVED: ['restore'],
};

/** A row queued for deletion, waiting on the confirm dialog. */
interface PendingDelete {
  readonly id: number;
  readonly title: string;
}

/**
 * One console list — pages, posts, banners, FAQ or policies. Provided by the list component, so each
 * tab holds its own filters and page.
 *
 * Follows `ProductsFacade`: filters and a page as signals, an `rxResource` keyed on all of them, a
 * `linkedSignal` last-good snapshot so the table does not blank between requests, and
 * `isLoading` / `error` / `retry()`.
 */
@Injectable()
export class ContentListFacade {
  private readonly api = inject(ContentItemsService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly cache = inject(ContentCache);

  readonly type = signal<ContentListType>('pages');
  readonly statusTab = signal<StatusTab>('all');
  readonly locale = signal<string | null>(null);
  readonly search = signal('');

  readonly query = computed<ContentListQuery>(() => ({
    ...NO_CONTENT_QUERY,
    status: this.statusTab() === 'all' ? null : (this.statusTab() as ContentStatus),
    locale: this.locale(),
    // A locale filter means "show me what still needs this language": the rows where it is missing
    // or only drafted.
    state: this.locale() ? 'MISSING' : null,
    q: this.search(),
  }));

  readonly pageIndex = linkedSignal<unknown, number>({
    source: () => [this.type(), this.query(), this.shell.currentStoreId()],
    computation: () => 0,
  });

  private readonly resource = rxResource({
    params: () => {
      this.cache.stamp();
      if (!this.shell.currentStoreId()) {
        return undefined;
      }
      return {type: this.type(), query: this.query(), page: this.pageIndex()};
    },
    stream: ({params}) =>
      this.api.list(params.type, params.query, {page: params.page, count: CONTENT_PAGE_SIZE}),
  });

  private readonly loaded = linkedSignal<ContentPage | undefined, ContentPage | undefined>({
    source: () => (this.resource.hasValue() ? this.resource.value() : undefined),
    computation: (incoming, previous) => incoming ?? previous?.value,
  });

  readonly isLoading = this.resource.isLoading;
  readonly error = computed(() => this.resource.error() as Error | undefined);
  readonly isEmpty = computed(() => this.loaded() === undefined);
  readonly busy = signal(false);

  readonly page = computed<ContentPage>(() => this.loaded() ?? (EMPTY_PAGE as ContentPage));
  readonly rows = computed<readonly ContentRow[]>(() => this.page().content);

  readonly filtered = computed(
    () => this.statusTab() !== 'all' || this.locale() !== null || this.search().trim() !== '',
  );

  readonly tabs = computed<readonly TabItem[]>(() => {
    this.transloco.activeLang();
    return [
      {key: 'all', label: this.transloco.translate('content.filter.all')},
      ...CONTENT_STATUSES.map((status) => ({
        key: status,
        label: this.transloco.translate(`content.status.${status}`),
      })),
    ];
  });

  readonly subtitle = computed(() => {
    this.transloco.activeLang();
    const page = this.page();
    if (!page.content.length) {
      return this.transloco.translate('content.list.none');
    }
    const digits = (value: number) => this.localeFormat.localizeNumber(value, 'decimal');
    const from = page.pageNumber * (page.size || CONTENT_PAGE_SIZE) + 1;
    return this.transloco.translate('content.list.range', {
      from: digits(from),
      to: digits(from + page.content.length - 1),
      total: digits(page.totalElements),
      count: page.totalElements,
    });
  });

  setType(type: ContentListType): void {
    if (this.type() !== type) {
      this.type.set(type);
      this.clearFilters();
      this.selected.set(new Set());
    }
  }

  clearFilters(): void {
    this.statusTab.set('all');
    this.locale.set(null);
    this.search.set('');
  }

  goToPage(page: number): void {
    this.pageIndex.set(page);
  }

  retry(): void {
    this.resource.reload();
  }

  /* -------------------------------------------------------------------- transitions ---- */

  transitionsFor(row: ContentRow): readonly TransitionAction[] {
    return TRANSITIONS[row.status] ?? [];
  }

  transition(row: ContentRow, action: TransitionAction): void {
    this.busy.set(true);
    this.api.transition(this.type(), row.id, action).subscribe({
      next: (saved) => {
        this.busy.set(false);
        this.cache.invalidate();
        this.toast.success(
          this.transloco.translate(`content.toast.${action}`, {
            title: row.title,
            status: this.transloco.translate(`content.status.${saved.status}`),
          }),
        );
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  /* ---------------------------------------------------------------------- selection ---- */

  readonly selected = signal<ReadonlySet<number>>(new Set());

  readonly allSelected = computed(() => {
    const rows = this.rows();
    return rows.length > 0 && rows.every((row) => this.selected().has(row.id));
  });

  toggleSelected(id: number): void {
    this.selected.update((current) => {
      const next = new Set(current);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  }

  toggleAll(): void {
    if (this.allSelected()) {
      this.selected.set(new Set());
    } else {
      this.selected.set(new Set(this.rows().map((row) => row.id)));
    }
  }

  clearSelection(): void {
    this.selected.set(new Set());
  }

  /** One action for every selected row; partial success is reported per row, never as a whole. */
  bulk(action: BulkAction): void {
    const ids = [...this.selected()];
    if (!ids.length) {
      return;
    }
    this.busy.set(true);
    this.api.bulk(this.type(), ids, action).subscribe({
      next: (results) => {
        this.busy.set(false);
        this.selected.set(new Set());
        this.cache.invalidate();
        const failed = results.filter((result) => !result.ok);
        if (failed.length) {
          this.toast.warning(
            this.transloco.translate('content.toast.bulkPartial', {
              ok: results.length - failed.length,
              failed: failed.length,
            }),
          );
        } else {
          this.toast.success(
            this.transloco.translate('content.toast.bulkDone', {count: results.length}),
          );
        }
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  /* ----------------------------------------------------------------------- deletion ---- */

  readonly pendingDelete = signal<PendingDelete | null>(null);

  askDelete(row: ContentRow): void {
    this.pendingDelete.set({id: row.id, title: row.title});
  }

  dismissDelete(): void {
    this.pendingDelete.set(null);
  }

  confirmDelete(): void {
    const pending = this.pendingDelete();
    this.pendingDelete.set(null);
    if (!pending) {
      return;
    }
    this.busy.set(true);
    this.api.delete(this.type(), pending.id, true).subscribe({
      next: () => {
        this.busy.set(false);
        this.selected.update((current) => {
          const next = new Set(current);
          next.delete(pending.id);
          return next;
        });
        this.cache.invalidate();
        this.toast.success(
          this.transloco.translate('content.toast.deleted', {title: pending.title}),
        );
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }
}
