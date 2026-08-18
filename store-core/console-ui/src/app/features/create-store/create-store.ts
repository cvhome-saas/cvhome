import {Component, computed, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {Badge} from '@shared/ui/badge/badge';
import {Icon} from '@shared/ui/icon/icon';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {Panel} from '@shared/ui/panel/panel';
import {ProgressTrack} from '@shared/ui/progress-track/progress-track';
import {ToastService} from '@shared/ui/toast/toast';
import type {NextStepLink} from '@models/create-store';
import {CreateStoreFacade} from './facades/create-store.facade';

/**
 * Provisions a new store: identity, hosting region and plan up front, then a simulated
 * run that walks through the checklist and lands on a live confirmation.
 *
 * There is no store-creation endpoint yet, so the "provisioning" is `CreateStoreFacade`
 * animating a scripted checklist — the same fixture-backed trade every other console page
 * makes, just with a timer standing in for a job queue.
 */
@Component({
  selector: 'app-create-store',
  imports: [Badge, Icon, PageHeader, Panel, ProgressTrack, ReactiveFormsModule, RouterLink, TranslocoDirective],
  templateUrl: './create-store.html',
  styleUrl: './create-store.css',
})
export class CreateStore {
  protected readonly facade = inject(CreateStoreFacade);
  /** Read only for whether this is the account's first store — the page owns nothing else of the shell. */
  protected readonly shell = inject(ConsoleShellFacade);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly router = inject(Router);

  protected readonly heading = computed(() => {
    this.transloco.activeLang();
    return this.transloco.translate('createStore.title');
  });

  protected submit(): void {
    this.facade.start();
  }

  protected followNextStep(step: NextStepLink): void {
    if (step.route) {
      this.router.navigateByUrl(step.route);
      return;
    }
    this.toast.info(this.transloco.translate('createStore.notAvailable', {what: this.transloco.translate(step.labelKey)}));
  }
}
