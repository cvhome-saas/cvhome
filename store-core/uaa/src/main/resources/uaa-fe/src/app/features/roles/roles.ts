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
  TableRow,
  TextField,
  type TableColumn,
} from '@cvhome-saas/ui-kit/ui';
import {PAGE_SIZE, RolesFacade} from './facades/roles.facade';

@Component({
  selector: 'app-roles',
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
  ],
  providers: [RolesFacade],
  templateUrl: './roles.html',
  styleUrl: './roles.css',
})
export class Roles {
  protected readonly facade = inject(RolesFacade);
  protected readonly pageSize = PAGE_SIZE;

  /**
   * Two columns. The row's actions moved into the detail pane, which is the point of the layout:
   * a list that only selects, and one place where a role is edited.
   */
  private readonly keys: readonly {key: string; width: string}[] = [
    {key: 'name', width: 'minmax(12rem, 2fr)'},
    {key: 'id', width: 'minmax(10rem, 1.4fr)'},
  ];

  /** Column headers are translated here rather than in the table, which takes plain strings. */
  protected columns(t: (key: string) => string): readonly TableColumn[] {
    return this.keys.map(({key, width}) => ({key, width, label: t('roles.column.' + key)}));
  }

}
