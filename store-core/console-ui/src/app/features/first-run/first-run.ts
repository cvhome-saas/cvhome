import {Component, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {Badge} from '@shared/ui/badge/badge';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {Icon} from '@shared/ui/icon/icon';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {Panel} from '@shared/ui/panel/panel';
import {ProgressTrack} from '@shared/ui/progress-track/progress-track';
import {ToastService} from '@shared/ui/toast/toast';
import {FirstRunFacade} from './facades/first-run.facade';

/**
 * The console's getting-started page: what an operator sees before their first store exists.
 *
 * It renders into `ConsoleShell` like every other console page, but under a rail the shell
 * has disabled — this page and store creation are the only two places a store-less account
 * can be, and `requiresStore` sends them back here from anywhere else.
 *
 * The one live decision is the trial gate: store provisioning is a trial entitlement, so
 * "Create store" stays disabled, and says why, until the trial is started.
 */
@Component({
  selector: 'app-first-run',
  imports: [
    Badge,
    BusyOverlay,
    Icon,
    NoticeBar,
    PageHeader,
    Panel,
    ProgressTrack,
    RouterLink,
    TranslocoDirective,
  ],
  templateUrl: './first-run.html',
  styleUrl: './first-run.css',
})
export class FirstRun {
  protected readonly facade = inject(FirstRunFacade);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);

  protected readonly heading = this.facade.heading;
  protected readonly notice = this.facade.notice;
  protected readonly hero = this.facade.hero;
  protected readonly stepBadge = this.facade.stepBadge;
  protected readonly steps = this.facade.steps;
  protected readonly progressPct = this.facade.progressPct;
  protected readonly feature = this.facade.feature;
  protected readonly guides = this.facade.guides;
  protected readonly nextUp = this.facade.nextUp;
  protected readonly limits = this.facade.limits;

  protected readonly trialPending = this.facade.trialPending;
  protected readonly createDisabled = this.facade.createDisabled;

  protected readonly isLoading = this.facade.isLoading;
  protected readonly isEmpty = this.facade.isEmpty;
  protected readonly error = this.facade.error;

  /**
   * The walkthroughs and the plan links have nowhere to go yet. A toast naming what was
   * asked for beats a dead control, and matches how the rest of the console handles a
   * section that is not built — see `createStore.notAvailable`.
   */
  protected notAvailable(what: string): void {
    this.toast.info(this.transloco.translate('firstRun.notAvailable', {what}));
  }
}
