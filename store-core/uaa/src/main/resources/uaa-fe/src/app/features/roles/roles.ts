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
  TabSwitcher,
  TableRow,
  TextField,
  TextareaField,
  type TabItem,
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
    TabSwitcher,
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

  /**
   * The filters as the kit's tab track: the same control the users, clients and audit screens use,
   * with the counts as its badges. This screen used to draw its own segmented buttons — the same
   * idea one shade off in height, radius and type — which is exactly the drift a shared control
   * exists to prevent.
   */
  protected filterTabs(t: (key: string) => string): readonly TabItem[] {
    return this.filters.map((filter) => ({
      key: filter,
      label: t('roles.filter.' + filter),
      badge: String(this.count(filter)),
    }));
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
