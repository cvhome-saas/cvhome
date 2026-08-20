import {Component, inject, input, model} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

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
 *
 * It collapses to an icon strip. Eight sections is a tall rail beside a card that is often taller
 * still, and on a laptop the section being edited can end up scrolled away from the list it came
 * from; folding the rail hands that width back to the work. Collapsed, the icons keep their tooltips
 * and their accessible names, so nothing is lost but the words.
 */
@Component({
  selector: 'app-settings-nav',
  imports: [Icon, RouterLink, TranslocoDirective],
  template: `
    <nav
      class="settings-nav"
      [class.collapsed]="collapsed()"
      [attr.aria-label]="label() ?? t('storeSettings.nav.ariaLabel')"
      *transloco="let t"
    >
      <div class="nav-head">
        <p class="nav-heading">{{ t('storeSettings.nav.heading') }}</p>
        <button
          class="nav-toggle"
          type="button"
          [attr.aria-expanded]="!collapsed()"
          [attr.aria-label]="toggleLabel()"
          [title]="toggleLabel()"
          (click)="collapsed.set(!collapsed())"
        >
          <app-icon [name]="collapsed() ? 'panelLeftOpen' : 'panelLeftClose'" [flip]="true" />
        </button>
      </div>

      @for (section of sections(); track section.key) {
        <a
          class="nav-item"
          [class.active]="section.key === active()"
          [routerLink]="['/store-management', section.key]"
          [attr.aria-current]="section.key === active() ? 'page' : null"
          [title]="collapsed() ? t(section.labelKey) : null"
        >
          <app-icon [name]="section.icon" />
          <!--
            Hidden by width rather than removed, so the accessible name survives the collapse and
            the label has something to animate from when it comes back.
          -->
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
  private readonly transloco = inject(TranslocoService);

  readonly sections = input.required<readonly SettingsSection[]>();
  readonly active = input.required<SettingsSectionKey>();
  readonly label = input<string>();

  /**
   * Whether the rail is folded to icons.
   *
   * A `model` rather than internal state: the grid column that holds the rail belongs to the page,
   * so the page has to know the width to animate to. The rail owns the decision, the page owns the
   * layout.
   */
  readonly collapsed = model(false);

  protected toggleLabel(): string {
    return this.transloco.translate(
      this.collapsed() ? 'storeSettings.nav.expand' : 'storeSettings.nav.collapse',
    );
  }
}
