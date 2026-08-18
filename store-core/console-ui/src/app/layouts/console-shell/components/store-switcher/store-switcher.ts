import {Component, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';
import {ConsoleShellFacade} from '../../facades/console-shell.facade';

/** Store picker at the foot of the sidebar, with its default-store menu. */
@Component({
  selector: 'app-store-switcher',
  imports: [Icon, RouterLink, TranslocoDirective],
  template: `
    <section class="store-switcher" [attr.aria-label]="t('shell.store.ariaLabel')" *transloco="let t">
      <header>
        <span class="nav-group nav-label">{{ t('shell.store.title') }}</span>
        <div class="menu nav-label">
          <button
            class="store-menu-toggle"
            type="button"
            [attr.aria-label]="t('shell.store.optionsAriaLabel')"
            [attr.aria-expanded]="shell.openMenu() === 'store'"
            (click)="shell.toggleMenu('store')"
          >
            <app-icon name="ellipsisH" />
          </button>
          @if (shell.openMenu() === 'store') {
            <div class="popover store-menu">
              <p class="popover-title">{{ t('shell.store.defaultStore') }}</p>
              <button type="button" (click)="shell.pinStore()">
                <app-icon name="pin" />{{ t('shell.store.setCurrentAsDefault') }}
              </button>
              <p class="popover-note">{{ t('shell.store.opensOnLogin', {name: shell.defaultStore()}) }}</p>
              <hr />
              <button type="button" [attr.aria-pressed]="shell.reordering()" (click)="shell.toggleReorder()">
                <app-icon name="sort" />{{ shell.reordering() ? t('shell.store.doneReordering') : t('shell.store.reorderStores') }}
              </button>
              <button type="button"><app-icon name="list" />{{ t('shell.store.manageAllStores') }}</button>
            </div>
          }
        </div>
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

      <ul class="store-list" [class.reordering]="shell.reordering()">
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
                @if (store.id === shell.defaultStoreId()) {
                  <app-icon name="pin" />
                }
                @if (current) {
                  <app-icon name="check" />
                }
              </span>
            </button>

            @if (shell.reordering()) {
              <span class="store-move nav-label">
                <button
                  type="button"
                  [disabled]="index === 0"
                  [attr.aria-label]="t('shell.store.moveUp', {name: store.name})"
                  (click)="shell.moveStore(store.id, -1)"
                >
                  <app-icon name="arrowUp" />
                </button>
                <button
                  type="button"
                  [disabled]="index === count - 1"
                  [attr.aria-label]="t('shell.store.moveDown', {name: store.name})"
                  (click)="shell.moveStore(store.id, 1)"
                >
                  <app-icon name="arrowDown" />
                </button>
              </span>
            }
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
