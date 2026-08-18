import {Component, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';
import {ConsoleShellFacade} from '../../facades/console-shell.facade';

/** Store picker at the foot of the sidebar. */
@Component({
  selector: 'app-store-switcher',
  imports: [Icon, RouterLink, TranslocoDirective],
  template: `
    <section class="store-switcher" [attr.aria-label]="t('shell.store.ariaLabel')" *transloco="let t">
      <header>
        <span class="nav-group nav-label">{{ t('shell.store.title') }}</span>
        <!--
          TODO(lessons.md): the options menu held "set as default", "reorder stores" and "manage all
          stores". The first two need a user-preferences endpoint that does not exist — see lessons.md,
          "Shell — no user-preferences endpoint" — and the third had no destination. Removed rather than
          left as controls that quietly do nothing.
        -->
      </header>

      @if (shell.storesLoading() && !shell.stores().length) {
        <p class="store-placeholder nav-label">{{ t('shell.store.loading') }}</p>
      } @else if (shell.firstRun()) {
        <!-- An empty list reads as a failure; naming the absence reads as a state. -->
        <p class="store-empty">
          <span class="store-mark" aria-hidden="true"><app-icon name="building" /></span>
          <span class="nav-label">{{ t('shell.store.noStoreYet') }}</span>
        </p>
      }

      <ul class="store-list">
        @for (store of shell.stores(); track store.id; let index = $index, count = $count) {
          @let current = store.id === shell.currentStoreId();
          <li [class.secondary]="!current">
            <button
              class="store"
              type="button"
              [class.current]="current"
              [attr.title]="shell.navCollapsed() ? store.name : null"
              [attr.aria-current]="current ? 'true' : null"
              (click)="shell.selectStore(store.id)"
            >
              <span class="store-mark" aria-hidden="true"><app-icon name="building" /></span>
              <span class="store-name nav-label">{{ store.name }}</span>
              <span class="store-flags nav-label" aria-hidden="true">
                @if (store.provisioningState === 'IN_PROGRESS_PROVISIONING' || store.provisioningState === 'NOT_STARTED_PROVISIONING') {
                  <app-icon name="clock" />
                } @else if (store.provisioningState === 'FAILED_PROVISIONING') {
                  <app-icon name="alertCircle" />
                }
                @if (current) {
                  <app-icon name="check" />
                }
              </span>
            </button>

          </li>
        }
      </ul>

      <a class="create-store" routerLink="/store-management/create" (click)="shell.closeMobileNav()">
        <app-icon name="plus" /><span class="nav-label">{{ t('shell.store.createStore') }}</span>
      </a>
    </section>
  `,
  styleUrl: './store-switcher.css',
})
export class StoreSwitcher {
  protected readonly shell = inject(ConsoleShellFacade);
}
