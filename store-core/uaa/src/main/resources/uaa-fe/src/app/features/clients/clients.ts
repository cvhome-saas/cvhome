import {Component, inject} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {
  BusyOverlay,
  DataTable,
  EmptyState,
  Icon,
  LoadError,
  NoticeBar,
  PageHeader,
  Pagination,
  Panel,
  TableRow,
  type TableColumn,
} from '@cvhome-saas/ui-kit/ui';

import type {ClientSummary} from '@cvhome-saas/ui-kit/uaa';

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
  ],
  providers: [ClientsFacade],
  templateUrl: './clients.html',
  styleUrl: './clients.css',
})
export class Clients {
  private readonly router = inject(Router);
  protected readonly facade = inject(ClientsFacade);
  protected readonly pageSize = PAGE_SIZE;

  /**
   * Two columns and a chevron.
   *
   * The design draws five — Type, Protocol, Last token, Status — and `ClientSummary` carries none of
   * them. See lessons.md, "Clients — the list carries three fields".
   */
  private readonly keys: readonly {key: string; width: string}[] = [
    {key: 'clientId', width: 'minmax(11rem, 1.6fr)'},
    {key: 'clientName', width: 'minmax(9rem, 1.2fr)'},
  ];

  protected columns(t: (key: string) => string): readonly TableColumn[] {
    return [
      ...this.keys.map(({key, width}) => ({key, width, label: t('clients.column.' + key)})),
      {key: 'go', width: '2rem', label: ''},
    ];
  }

  /** A client is a page, not a pane: `ClientDetails` is five groups of settings and two open maps. */
  protected open(client: ClientSummary): void {
    void this.router.navigate(['/clients', client.id]);
  }
}
