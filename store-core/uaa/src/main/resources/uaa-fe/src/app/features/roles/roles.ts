import {Component, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {
  Badge,
  BusyOverlay,
  Checkbox,
  ConfirmDialog,
  DataTable,
  EmptyState,
  FormDialog,
  FormField,
  Icon,
  LoadError,
  NoticeBar,
  PageHeader,
  Pagination,
  Panel,
  SearchBox,
  Select,
  TableRow,
  TextField,
  TextareaField,
  type TableColumn,
} from '@cvhome-saas/ui-kit/ui';
import {PAGE_SIZE, RolesFacade, type RoleFilter} from './facades/roles.facade';

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
    FormDialog,
    FormField,
    TextField,
    TextareaField,
    Select,
    Checkbox,
    Badge,
    SearchBox,
    ConfirmDialog,
    Icon,
  ],
  providers: [RolesFacade],
  templateUrl: './roles.html',
  styleUrl: './roles.css',
})
export class Roles {
  protected readonly facade = inject(RolesFacade);
  protected readonly pageSize = PAGE_SIZE;
  protected readonly filters: readonly RoleFilter[] = ['all', 'system', 'custom'];

  /**
   * Five columns, as the design draws them, and a pencil. The row opens the dialog; there is nowhere
   * else a role is edited.
   */
  private readonly keys: readonly {key: string; width: string}[] = [
    {key: 'name', width: 'minmax(16rem, 3fr)'},
    {key: 'scope', width: 'minmax(7rem, 1fr)'},
    {key: 'users', width: 'minmax(4rem, 0.6fr)'},
    {key: 'perms', width: 'minmax(4rem, 0.6fr)'},
    {key: 'type', width: 'minmax(6rem, 0.8fr)'},
  ];

  /** Column headers are translated here rather than in the table, which takes plain strings. */
  protected columns(t: (key: string) => string): readonly TableColumn[] {
    return [
      ...this.keys.map(({key, width}) => ({key, width, label: t('roles.column.' + key)})),
      {key: 'go', width: '2rem', label: ''},
    ];
  }

  protected count(filter: RoleFilter): number {
    switch (filter) {
      case 'system':
        return this.facade.systemCount();
      case 'custom':
        return this.facade.customCount();
      default:
        return this.facade.all().length;
    }
  }
}
