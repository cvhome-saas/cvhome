import {Component, computed, inject} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {DataTable, type TableColumn} from '@shared/ui/data-table/data-table';
import {TableRow} from '@shared/ui/data-table/table-row';
import {EmptyState} from '@shared/ui/empty-state/empty-state';
import {LoadError} from '@shared/ui/load-error/load-error';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {Panel} from '@shared/ui/panel/panel';
import {PlatformPlansFacade} from './facades/platform-plans.facade';

/**
 * The plan catalogue — what the platform sells, at what price, with what ceilings.
 *
 * **A reference, not a pricing page.** The marketing site renders the same catalogue as cards, with
 * a featured plan and a yearly saving; an operator opening this wants to compare the rows and read
 * the numbers, so it is a table and it shows every plan at every interval rather than one interval
 * at a time.
 *
 * It is also the *only* billing screen the platform console can have. Every other endpoint billing
 * exposes is store-scoped and refuses a super admin — see lessons.md, "Platform — a store's
 * subscription cannot be read by an operator" and "Platform — no view of stores billing has cut off".
 */
@Component({
  selector: 'app-platform-plans',
  imports: [BusyOverlay, DataTable, EmptyState, LoadError, NoticeBar, PageHeader, Panel, TableRow, TranslocoDirective],
  providers: [PlatformPlansFacade],
  templateUrl: './platform-plans.html',
  styleUrl: './platform-plans.css',
})
export class PlatformPlans {
  private readonly transloco = inject(TranslocoService);

  protected readonly facade = inject(PlatformPlansFacade);

  /**
   * Plan, both prices, then one column per entitlement.
   *
   * Built from the facade's rows rather than a constant, because the entitlement half is as long as
   * `ENTITLEMENT_ORDER` and its labels follow the reader's language.
   */
  protected readonly columns = computed<readonly TableColumn[]>(() => {
    this.transloco.activeLang();
    return [
      {key: 'plan', label: this.transloco.translate('platform.plans.column.plan'), width: 'minmax(11rem, 1.6fr)'},
      {key: 'monthly', label: this.transloco.translate('platform.plans.column.monthly'), width: 'minmax(6rem, 0.8fr)'},
      {key: 'yearly', label: this.transloco.translate('platform.plans.column.yearly'), width: 'minmax(6rem, 0.8fr)'},
      ...this.facade
        .entitlementRows()
        .map((row) => ({key: row.key, label: row.label, width: 'minmax(6rem, 0.9fr)'})),
    ];
  });
}
