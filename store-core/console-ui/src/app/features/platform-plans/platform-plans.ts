import {Component, computed, inject} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {BusyOverlay, DataTable, type TableColumn, TableRow, EmptyState, LoadError, NoticeBar, PageHeader, Panel} from '@cvhome-saas/ui-kit/ui';
import {PlatformPlansFacade} from './facades/platform-plans.facade';

/**
 * The plan catalogue — what the platform sells, at what price, with what ceilings.
 *
 * **A reference, not a pricing page.** The marketing site renders the same catalogue as cards, with
 * a featured plan and a yearly saving; an operator opening this wants to compare the rows and read
 * the numbers, so it is a table and it shows every plan at every interval rather than one interval
 * at a time.
 *
 * **Two columns make it more than a reference.** The subscriber count and the committed annual value
 * come from billing's plan statistics, so a row says not only what a plan costs but how many stores
 * are on it and what that is contracted to bring in. It was a price list while billing exposed no
 * aggregate at all; `/platform/billing` is where the rest of that money is read.
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
      /*
       * The two commercial columns, between the prices and the allowances: what the platform charges,
       * then how that is actually selling, then what it buys. They stay in place when the statistics
       * leg fails — the cells read as em dashes, which says "not known" rather than "none".
       */
      {
        key: 'subscribers',
        label: this.transloco.translate('platform.plans.column.subscribers'),
        width: 'minmax(5rem, 0.6fr)',
      },
      {
        key: 'recurring',
        label: this.transloco.translate('platform.plans.column.recurring'),
        width: 'minmax(7rem, 0.9fr)',
      },
      ...this.facade
        .entitlementRows()
        .map((row) => ({key: row.key, label: row.label, width: 'minmax(6rem, 0.9fr)'})),
    ];
  });
}
