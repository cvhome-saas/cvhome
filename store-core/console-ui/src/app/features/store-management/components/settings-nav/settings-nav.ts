import {Component, input} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';
import type {SettingsSection, SettingsSectionKey} from '@models/store-settings';

/**
 * The settings rail: one link per section, then a way out to the storefront builder.
 *
 * Page-local rather than a shared primitive — it is a vertical tab list bound to the router,
 * not a generic list of links, and nothing else in the console has that shape.
 *
 * Each section is a real `routerLink` to `/store-management/:section`, so a tab is
 * linkable, survives a reload and answers the back button.
 */
@Component({
  selector: 'app-settings-nav',
  imports: [Icon, RouterLink, TranslocoDirective],
  template: `
    <nav class="settings-nav" [attr.aria-label]="label() ?? t('storeSettings.nav.ariaLabel')" *transloco="let t">
      <p class="nav-heading">{{ t('storeSettings.nav.heading') }}</p>

      @for (section of sections(); track section.key) {
        <a
          class="nav-item"
          [class.active]="section.key === active()"
          [routerLink]="['/store-management', section.key]"
          [attr.aria-current]="section.key === active() ? 'page' : null"
        >
          <app-icon [name]="section.icon" />
          <span class="nav-copy">{{ t(section.labelKey) }}</span>
          @if (section.attention) {
            <!-- Colour is not the signal: the dot is titled, and the section it marks says why. -->
            <span
              class="nav-dot"
              [title]="t('storeSettings.nav.needsAttention')"
              [attr.aria-label]="t('storeSettings.nav.needsAttention')"
            ></span>
          }
        </a>
      }

      <hr />

      <!--
        The storefront builder is a route of its own and not built yet. A disabled item with a
        reason beats a link that goes nowhere.
      -->
      <span class="nav-item builder" [title]="t('storeSettings.nav.builderUnavailable')">
        <app-icon name="layoutGrid" />
        <span class="nav-copy">{{ t('storeSettings.nav.homePageBuilder') }}</span>
        <app-icon name="arrowUpRight" [flip]="true" />
      </span>
    </nav>
  `,
  styleUrl: './settings-nav.css',
})
export class SettingsNav {
  readonly sections = input.required<readonly SettingsSection[]>();
  readonly active = input.required<SettingsSectionKey>();
  readonly label = input<string>();
}
