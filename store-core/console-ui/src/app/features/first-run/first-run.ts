import {Component, computed, inject, signal} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {PlanDialog} from '@layouts/billing/plan-dialog/plan-dialog';
import {Badge} from '@shared/ui/badge/badge';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {Icon} from '@shared/ui/icon/icon';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {Panel} from '@shared/ui/panel/panel';
import {ProgressTrack} from '@shared/ui/progress-track/progress-track';
import {ToastService} from '@shared/ui/toast/toast';
import {VideoDialog} from '@shared/ui/video-dialog/video-dialog';
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
    PlanDialog,
    ProgressTrack,
    RouterLink,
    TranslocoDirective,
    VideoDialog,
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
   * Booking a call is still unbuilt — there is no scheduling service behind it, and unlike the
   * plans and the guides there is no existing endpoint or page to point it at. A toast naming what
   * was asked for beats a dead control; see `createStore.notAvailable` for the same treatment.
   */
  protected notAvailable(what: string): void {
    this.toast.info(this.transloco.translate('firstRun.notAvailable', {what}));
  }

  /** Whether the plan comparison is showing. The catalog is not fetched until this first flips. */
  protected readonly plansOpen = signal(false);

  /** Whether the walkthrough player is showing. */
  protected readonly videoOpen = signal(false);

  protected readonly videoTitle = computed(() => this.feature()?.title ?? '');
  protected readonly videoSrc = computed(() => this.feature()?.src ?? null);
  protected readonly videoPoster = computed(() => this.feature()?.poster ?? null);
  protected readonly videoCopy = computed(() => this.feature()?.copy ?? null);

  /**
   * Choosing a plan from here has nowhere to go *yet*, and for a reason worth stating: a
   * subscription belongs to a store, and this page is the one place in the console where no store
   * exists. The catalog is real and readable; the checkout it would open is not addressable until
   * the first store is provisioned. See lessons.md, "Shell — no plan selection at store creation".
   */
  protected planChosen(): void {
    this.plansOpen.set(false);
    this.toast.info(this.transloco.translate('firstRun.planAfterStore'));
  }
}
