import {Injectable, computed, inject, signal} from '@angular/core';

import {snapshot} from '@cvhome-saas/ui-kit';
import {AdminClientService} from '@cvhome-saas/ui-kit/uaa';

export const PAGE_SIZE = 20;

/**
 * The client registry, as a list.
 *
 * **Three fields, and that is the whole endpoint.** `GET /` answers `ClientSummary` —
 * `{id, clientId, clientName}` — so the design's Type, Protocol, Last token and Status columns
 * cannot be drawn without a request per row. See lessons.md, "Clients — the list carries three
 * fields".
 *
 * Editing is a route, not a pane: `ClientDetails` has five groups of settings and two open maps
 * behind it, which is a page. Everything about one client lives in `@features/client-form`.
 */
@Injectable()
export class ClientsFacade {
  private readonly clients = inject(AdminClientService);

  readonly pageIndex = signal(0);

  private readonly page = snapshot(
    () => ({page: this.pageIndex(), count: PAGE_SIZE}),
    (query) => this.clients.list(query.page, query.count),
  );

  readonly isLoading = this.page.isLoading;
  readonly error = this.page.error;
  readonly isEmpty = this.page.isEmpty;
  readonly reload = () => this.page.reload();

  readonly rows = computed(() => this.page.value()?.content ?? []);
  readonly totalElements = computed(() => this.page.value()?.totalElements ?? 0);
  readonly totalPages = computed(() => this.page.value()?.totalPages ?? 0);

  goToPage(page: number): void {
    this.pageIndex.set(page);
  }
}
