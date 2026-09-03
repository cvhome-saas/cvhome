import {DatePipe} from '@angular/common';
import {Component, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {Badge, BusyOverlay, Icon, KpiGrid, LoadError, PageHeader, Panel, RankedList, TabSwitcher, type TabItem} from '@cvhome-saas/ui-kit/ui';
import type {AuditEventDto} from '@cvhome-saas/ui-kit/uaa';

import {DashboardFacade} from './facades/dashboard.facade';

@Component({
  selector: 'app-dashboard',
  imports: [DatePipe, RouterLink, TranslocoDirective, PageHeader, Panel, KpiGrid, RankedList, TabSwitcher, BusyOverlay, LoadError, Badge, Icon],
  providers: [DashboardFacade],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class DashboardScreen {
  protected readonly facade = inject(DashboardFacade);

  protected rangeTabs(): readonly TabItem[] {
    return this.facade.rangeOptions().map(({key, label}) => ({key, label}));
  }

  /**
   * The same label the audit screen uses, so a failure reads the same in both places: dots become underscores
   * because the key is a path, and an unknown type falls back to its raw name.
   */
  protected label(event: AuditEventDto, t: (key: string) => string): string {
    const key = 'audit.event.' + event.eventType.replace(/\./g, '_');
    const translated = t(key);
    return translated === key ? event.eventType : translated;
  }

  /** Hours for a day's worth of buckets, dates for the longer ranges. */
  protected bucketFormat(): string {
    return this.facade.range() === '24h' ? 'HH:mm' : 'MMM d';
  }
}
