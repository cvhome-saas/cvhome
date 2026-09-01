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

  private readonly keys: readonly {key: string; width: string}[] = [
    {key: 'clientId', width: 'minmax(12rem, 1.6fr)'},
    {key: 'clientName', width: 'minmax(12rem, 1.6fr)'},
    {key: 'actions', width: '15rem'},
  ];

  protected columns(t: (key: string) => string): readonly TableColumn[] {
    return this.keys.map(({key, width}) => ({
      key,
      width,
      label: key === 'actions' ? '' : t(`clients.column.${key}`),
    }));
  }

  /** The comma-separated hint under a list field, built from what the server says it accepts. */
  protected allowed(values: readonly string[]): string {
    return values.join(', ');
  }
}
