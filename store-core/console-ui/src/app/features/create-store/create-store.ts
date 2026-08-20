import {Component, computed, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {Badge} from '@shared/ui/badge/badge';
import {DatePicker} from '@shared/ui/date-picker/date-picker';
import {FieldError} from '@shared/ui/form-field/field-error';
import {Icon} from '@shared/ui/icon/icon';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {Panel} from '@shared/ui/panel/panel';
import {ProgressTrack} from '@shared/ui/progress-track/progress-track';
import {Toggle} from '@shared/ui/toggle/toggle';
import {ToastService} from '@shared/ui/toast/toast';
import {dateKey} from '@core/i18n/calendar';
import type {NextStepLink} from '@models/create-store';
import {CreateStoreFacade} from './facades/create-store.facade';

/**
 * Provisions a new store: the whole store identity up front, then the real provisioning run
 * watched by polling tenancy's row.
 *
 * The form collects everything the pod requires — email, phone, theme, colour theme, currency,
 * default and supported languages, and a country — rather than the four fields it once posted.
 * Provisioning is asynchronous, so anything this form omits that the pod demands cannot surface as
 * a validation error on this page; it surfaces as a store that failed to build. See
 * `CreateStoreFacade`.
 */
@Component({
  selector: 'app-create-store',
  imports: [
    Badge,
    DatePicker,
    FieldError,
    Icon,
    PageHeader,
    Panel,
    ProgressTrack,
    ReactiveFormsModule,
    RouterLink,
    Toggle,
    TranslocoDirective,
  ],
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

  /**
   * Today, as the picker's upper bound. A store cannot have been in business since tomorrow, and
   * the bound is cheaper than a validator that has to explain itself after the fact.
   */
  protected readonly today = todayKey();

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

/** `YYYY-MM-DD` for today, evaluated once per page load rather than on every change detection. */
function todayKey(): string {
  return dateKey(new Date());
}
