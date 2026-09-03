import {DatePipe} from '@angular/common';
import {Component, inject} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {
  Badge,
  BusyOverlay,
  DataTable,
  EmptyState,
  Icon,
  LoadError,
  PageHeader,
  Pagination,
  Panel,
  SearchBox,
  TabSwitcher,
  TableRow,
  Toggle,
  type TabItem,
  type TableColumn,
} from '@cvhome-saas/ui-kit/ui';
import type {AuditEventDto} from '@cvhome-saas/ui-kit/uaa';

import {AuditFacade} from './facades/audit.facade';

@Component({
  selector: 'app-audit',
  imports: [
    DatePipe,
    TranslocoDirective,
    PageHeader,
    Panel,
    BusyOverlay,
    LoadError,
    EmptyState,
    DataTable,
    TableRow,
    Badge,
    Icon,
    SearchBox,
    TabSwitcher,
    Pagination,
    Toggle,
  ],
  providers: [AuditFacade],
  templateUrl: './audit.html',
  styleUrl: './audit.css',
})
export class Audit {
  protected readonly facade = inject(AuditFacade);

  private readonly keys: readonly {key: string; width: string}[] = [
    {key: 'when', width: 'minmax(9rem, 0.9fr)'},
    {key: 'event', width: 'minmax(11rem, 1.4fr)'},
    {key: 'actor', width: 'minmax(9rem, 1fr)'},
    {key: 'target', width: 'minmax(9rem, 1fr)'},
    {key: 'source', width: 'minmax(7rem, 0.8fr)'},
  ];

  protected columns(t: (key: string) => string): readonly TableColumn[] {
    return this.keys.map(({key, width}) => ({key, width, label: t('audit.column.' + key)}));
  }

  protected categoryTabs(t: (key: string) => string): readonly TabItem[] {
    return this.facade.categoryOptions().map(({key, label}) => ({key, label: label || t('audit.category.all')}));
  }

  protected rangeTabs(): readonly TabItem[] {
    return this.facade.rangeOptions().map(({key, label}) => ({key, label}));
  }

  /**
   * The event type as words when we have them, and the raw name when we do not — a new event type must still
   * read on this screen the day it ships, before anyone has written its label.
   *
   * The dots become underscores because the key is a path: `user.login` and `user.login.failed` cannot both
   * exist in a nested dictionary, one being a leaf where the other needs a branch.
   */
  protected label(event: AuditEventDto, t: (key: string, params?: object) => string): string {
    const key = 'audit.event.' + event.eventType.replace(/\./g, '_');
    const translated = t(key);
    return translated === key ? event.eventType : translated;
  }
}
