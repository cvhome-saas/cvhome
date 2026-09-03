import {Component, inject} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {
  Badge,
  BusyOverlay,
  DataTable,
  EmptyState,
  Icon,
  KpiGrid,
  LoadError,
  NoticeBar,
  PageHeader,
  Pagination,
  Panel,
  SearchBox,
  TabSwitcher,
  TableRow,
  Toggle,
  type TableColumn,
} from '@cvhome-saas/ui-kit/ui';
import type {ClientSummary, ClientType} from '@cvhome-saas/ui-kit/uaa';

import {ClientsFacade, PAGE_SIZE} from './facades/clients.facade';

@Component({
  selector: 'app-clients',
  imports: [
    RouterLink,
    TranslocoDirective,
    PageHeader,
    Panel,
    NoticeBar,
    BusyOverlay,
    LoadError,
    EmptyState,
    Pagination,
    DataTable,
    TableRow,
    Icon,
    Badge,
    KpiGrid,
    TabSwitcher,
    SearchBox,
    Toggle,
  ],
  providers: [ClientsFacade],
  templateUrl: './clients.html',
  styleUrl: './clients.css',
})
export class Clients {
  private readonly router = inject(Router);
  protected readonly facade = inject(ClientsFacade);
  protected readonly pageSize = PAGE_SIZE;

  /** The five columns the design draws, now that the row carries them, and a chevron. */
  private readonly keys: readonly {key: string; width: string}[] = [
    {key: 'client', width: 'minmax(13rem, 2fr)'},
    {key: 'type', width: 'minmax(6rem, 0.8fr)'},
    {key: 'grants', width: 'minmax(9rem, 1.2fr)'},
    {key: 'secret', width: 'minmax(8rem, 1fr)'},
    {key: 'status', width: 'minmax(6rem, 0.8fr)'},
  ];

  protected columns(t: (key: string) => string): readonly TableColumn[] {
    return [
      ...this.keys.map(({key, width}) => ({key, width, label: t('clients.column.' + key)})),
      {key: 'go', width: '2rem', label: ''},
    ];
  }

  protected typeTone(type: ClientType): 'cyan' | 'violet' | 'slate' {
    switch (type) {
      case 'MACHINE':
        return 'cyan';
      case 'CONFIDENTIAL':
        return 'violet';
      default:
        return 'slate';
    }
  }

  protected when(value: string | null): string {
    return value ? new Date(value).toLocaleDateString() : '—';
  }

  /** A secret expiring within thirty days is worth a tone; a public client has none to expire. */
  protected secretTone(row: ClientSummary): 'amber' | 'red' | 'slate' {
    if (!row.clientSecretExpiresAt) {
      return 'slate';
    }
    const days = (new Date(row.clientSecretExpiresAt).getTime() - Date.now()) / 86_400_000;
    return days < 0 ? 'red' : days < 30 ? 'amber' : 'slate';
  }

  /**
   * A client is a page, not a pane: `ClientDetails` is five groups of settings and two open maps.
   *
   * The enable switch sits inside the row, so a click that landed on it must not also open the page.
   * Read off the event rather than stopped on a wrapper — `stopPropagation` on a `<span>` is a click
   * handler on a non-interactive element, which the template linter is right to refuse.
   */
  protected open(event: Event, client: ClientSummary): void {
    if ((event.target as HTMLElement).closest('.cell.status')) {
      return;
    }
    void this.router.navigate(['/clients', client.id]);
  }
}
