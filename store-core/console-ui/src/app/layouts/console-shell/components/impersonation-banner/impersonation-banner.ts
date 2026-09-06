import {Component, computed, inject, signal} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {DestroyRef} from '@angular/core';
import {interval, startWith} from 'rxjs';

import {Icon} from '@cvhome-saas/ui-kit/ui';
import {ConsoleShellFacade} from '../../facades/console-shell.facade';

/** How often the countdown re-reads the clock. Minute granularity is what the copy shows. */
const TICK_MS = 30_000;

/**
 * The bar that says the session is somebody else.
 *
 * **Not dismissible.** The failure mode being designed against is an operator forgetting they are
 * acting as a merchant and doing something as them; a bar that can be closed is a bar that will be.
 * It names the merchant, the store, the mode and the time left, and carries the only control that
 * ends it — the gateway's ceiling ends it too, whatever this shows.
 *
 * Rendered above the plan banner and counted into `--banner-h` by the shell, so the layout never
 * disagrees with what is on screen.
 */
@Component({
  selector: 'app-impersonation-banner',
  imports: [Icon, TranslocoDirective],
  template: `
    @if (shell.impersonation(); as acting) {
      <aside class="impersonation-banner" [class.write]="acting.mode === 'write'" role="status" *transloco="let t">
        <app-icon name="signIn" [flip]="true" />
        <p>
          <strong>{{ t('shell.impersonation.actingAs', {name: acting.actingAs}) }}</strong>
          <span class="detail">
            {{ t('shell.impersonation.store', {store: storeName()}) }}
            · {{ t('shell.impersonation.mode.' + acting.mode) }}
            · {{ remainingLabel() }}
          </span>
        </p>
        <button class="secondary-action end" type="button" (click)="shell.endImpersonation()">
          {{ t('shell.impersonation.end') }} <app-icon name="signOut" [flip]="true" />
        </button>
      </aside>
    }
  `,
  styleUrl: './impersonation-banner.css',
})
export class ImpersonationBanner {
  protected readonly shell = inject(ConsoleShellFacade);
  private readonly transloco = inject(TranslocoService);
  private readonly destroyRef = inject(DestroyRef);

  /** Re-evaluated on a timer so the minutes left move without a reload. */
  private readonly now = signal(Date.now());

  constructor() {
    interval(TICK_MS)
      .pipe(startWith(0), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.now.set(Date.now()));
  }

  /** The store's name where the rail knows it, its id where it does not yet. */
  protected readonly storeName = computed(() => {
    const acting = this.shell.impersonation();
    if (!acting) {
      return '';
    }
    return this.shell.stores().find((store) => store.id === acting.storeId)?.name ?? acting.storeId;
  });

  protected readonly remainingLabel = computed(() => {
    this.transloco.activeLang();
    const acting = this.shell.impersonation();
    if (!acting) {
      return '';
    }
    const minutes = Math.max(0, Math.ceil((Date.parse(acting.expiresAt) - this.now()) / 60_000));
    return this.transloco.translate('shell.impersonation.remaining', {count: minutes});
  });
}
