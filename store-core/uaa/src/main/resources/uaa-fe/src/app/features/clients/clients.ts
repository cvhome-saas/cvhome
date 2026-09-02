import {Component, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {
  BusyOverlay,
  ConfirmDialog,
  DataTable,
  EmptyState,
  FormField,
  LoadError,
  NoticeBar,
  PageHeader,
  Pagination,
  Panel,
  SetPasswordDialog,
  TableRow,
  TextField,
  type TableColumn,
} from '@cvhome-saas/ui-kit/ui';

import type {ClientDetails, ClientSummary} from '@cvhome-saas/ui-kit/uaa';

import {ClientsFacade, PAGE_SIZE} from './facades/clients.facade';

@Component({
  selector: 'app-clients',
  imports: [
    ReactiveFormsModule,
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
    FormField,
    TextField,
    ConfirmDialog,
    SetPasswordDialog,
  ],
  providers: [ClientsFacade],
  templateUrl: './clients.html',
  styleUrl: './clients.css',
})
export class Clients {
  protected readonly facade = inject(ClientsFacade);
  protected readonly pageSize = PAGE_SIZE;

  /**
   * Two columns: the row selects, and the pane is where a client is edited.
   *
   * The design draws five — Type, Protocol, Last token, Status — and `ClientSummary` carries none of
   * them. See lessons.md, "Clients — the list carries three fields".
   */
  private readonly keys: readonly {key: string; width: string}[] = [
    {key: 'clientId', width: 'minmax(11rem, 1.6fr)'},
    {key: 'clientName', width: 'minmax(9rem, 1.2fr)'},
  ];

  protected columns(t: (key: string) => string): readonly TableColumn[] {
    return this.keys.map(({key, width}) => ({key, width, label: t('clients.column.' + key)}));
  }

  /**
   * The pane holds a full `ClientDetails`; the rotate and delete dialogs key on the list's
   * `ClientSummary`. The three fields they need are the three the summary has.
   */
  protected asSummary(client: ClientDetails): ClientSummary {
    return {id: client.id, clientId: client.clientId, clientName: client.clientName};
  }

  /** The comma-separated hint under a list field, built from what the server says it accepts. */
  protected allowed(values: readonly string[]): string {
    return values.join(', ');
  }
}
