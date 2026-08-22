import {UpperCasePipe} from '@angular/common';
import {Component, inject, signal} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {ThemeService} from '@core/theme/theme.service';
import {Icon} from '@shared/ui/icon/icon';
import {SearchBox} from '@shared/ui/search-box/search-box';
import {ConsoleShellFacade} from '../../facades/console-shell.facade';

/** Breadcrumb, search, and the notification / theme / language / profile menus. */
@Component({
  selector: 'app-console-toolbar',
  imports: [SearchBox, Icon, RouterLink, TranslocoDirective, UpperCasePipe],
  template: `
    <header class="toolbar" *transloco="let t">
      <nav class="breadcrumb" [attr.aria-label]="t('shell.toolbar.breadcrumbNav')">
        <app-icon name="home" />
        <!--
          The root crumb is the dashboard, which does not exist for an account with no
          store — naming it there would place the operator inside a section the guards
          will not let them enter. In first run the page stands on its own.
        -->
        @if (shell.firstRun()) {
          <strong aria-current="page">{{ shell.pageLabel() }}</strong>
        } @else {
          <span>{{ t('shell.breadcrumb.dashboard') }}</span>
          @if (shell.pageLabel()) {
            <app-icon name="chevronRight" [flip]="true" />
            <strong aria-current="page">{{ shell.pageLabel() }}</strong>
          }
        }
      </nav>

      <!--
        TODO(lessons.md): this box searches nothing — there is no cross-entity search on the
        platform, and it is decorative in seller-ui too. See lessons.md, "Shell — no global search".
        It is the shared control rather than a hand-rolled one so the console has a single search
        field, but whether a box that cannot answer should be here at all is a product question the
        bell and the sidebar badge counts were both decided the other way on.
      -->
      <app-search-box
        class="search"
        [(value)]="searchTerm"
        [label]="t('shell.toolbar.search')"
        [placeholder]="t('shell.toolbar.searchPlaceholder')"
        [clearLabel]="t('shell.toolbar.clearSearch')"
      />

      <!--
        TODO(lessons.md): the notification bell, its unread count, the feed, "mark all read" and
        "view all" were all fixture. No notifications service exists — see lessons.md, "Shell — no
        notifications service". Removed rather than shown disabled: a bell is a promise that
        something will appear in it.
      -->

      <div class="menu">
        <button
          class="language-toggle"
          type="button"
          [attr.aria-label]="t('shell.toolbar.languageAriaLabel', {name: shell.language().name})"
          [attr.aria-expanded]="shell.openMenu() === 'language'"
          (click)="shell.toggleMenu('language')"
        >
          <app-icon name="globe" />
          <strong>{{ shell.language().code | uppercase }}</strong>
          <app-icon name="chevronDown" />
        </button>
        @if (shell.openMenu() === 'language') {
          <div class="popover language-menu">
            @for (option of shell.languages; track option.code) {
              <button
                type="button"
                [class.selected]="option.code === shell.language().code"
                (click)="shell.selectLanguage(option.code)"
              >
                <b>{{ option.code | uppercase }}</b>{{ option.name }}
              </button>
            }
          </div>
        }
      </div>

      <div class="menu">
        <button
          class="theme-toggle"
          type="button"
          [attr.aria-label]="t('shell.toolbar.themeAriaLabel', {name: t(theme.current().labelKey)})"
          [attr.aria-expanded]="shell.openMenu() === 'theme'"
          (click)="shell.toggleMenu('theme')"
        >
          <span class="swatch" aria-hidden="true"></span>
          <strong>{{ t(theme.current().labelKey) }}</strong>
          <app-icon name="chevronDown" />
        </button>
        @if (shell.openMenu() === 'theme') {
          <div class="popover theme-menu" role="menu">
            @for (option of theme.themes; track option.id) {
              <button
                type="button"
                role="menuitemradio"
                [attr.aria-checked]="option.id === theme.current().id"
                [class.selected]="option.id === theme.current().id"
                [attr.data-theme-preview]="option.id"
                (click)="theme.select(option.id); shell.closeMenus()"
              >
                <span class="swatch" aria-hidden="true"></span>
                <span class="theme-copy">
                  <strong>{{ t(option.labelKey) }}</strong>
                  <small>{{ t(option.hintKey) }}</small>
                </span>
                @if (option.id === theme.current().id) {
                  <app-icon name="check" />
                }
              </button>
            }
          </div>
        }
      </div>

      <span class="toolbar-divider" aria-hidden="true"></span>

      <div class="menu">
        <button
          class="profile-toggle"
          type="button"
          [attr.aria-expanded]="shell.openMenu() === 'profile'"
          (click)="shell.toggleMenu('profile')"
        >
          <span class="avatar" aria-hidden="true">{{ shell.user()?.initials ?? '' }}</span>
          <strong>{{ shell.user()?.name ?? t('shell.profile.loading') }}</strong>
          <app-icon name="chevronDown" />
        </button>
        @if (shell.openMenu() === 'profile') {
          <div class="popover profile-menu">
            <header>
              <strong>{{ shell.user()?.name ?? t('shell.profile.loading') }}</strong>
              @if (shell.user()?.email; as email) {
                <span>{{ email }}</span>
              }
            </header>
            <a routerLink="/profile" (click)="shell.closeMenus()">
              <app-icon name="user" />{{ t('shell.profile.profile') }}
            </a>
            <!--
              The "Settings" button beside this one is gone rather than routed. There is no
              console-wide settings page and there will not be: settings are per store and live at
              /store-management. A control that goes nowhere is the same promise the notification
              bell was.
            -->
            <hr />
            <a class="sign-out" routerLink="/external-logout-link"><app-icon name="signOut" />{{ t('shell.profile.logOut') }}</a>
          </div>
        }
      </div>
    </header>
  `,
  styleUrl: './console-toolbar.css',
})
export class ConsoleToolbar {
  /** Held so the box is a controlled field; nothing reads it — see the note in the template. */
  protected readonly searchTerm = signal('');

  protected readonly shell = inject(ConsoleShellFacade);
  protected readonly theme = inject(ThemeService);
}
