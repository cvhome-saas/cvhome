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

  protected readonly columns: readonly TableColumn[] = [
    {key: 'name', label: 'Role', width: 'minmax(14rem, 2fr)'},
    {key: 'id', label: 'Id', width: 'minmax(12rem, 1.4fr)'},
    {key: 'actions', label: '', width: '11rem'},
  ];

  /** Column headers are translated here rather than in the table, which takes plain strings. */
  protected translatedColumns(t: (key: string) => string): readonly TableColumn[] {
    return this.columns.map((column) => ({
      ...column,
      label: column.key === 'actions' ? '' : t(`roles.column.${column.key}`),
    }));
  }

}
