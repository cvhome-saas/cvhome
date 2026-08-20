import {Component, inject, input, model} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';
import type {IconName} from '@shared/ui/icon/icon-paths';

/** One entry in a section rail. `key` is both the route segment and the active-state handle. */
export interface NavSection {
  readonly key: string;
  readonly labelKey: string;
  readonly icon: IconName;
  /** Draws the amber dot: this section has something waiting on the operator. */
  readonly attention?: boolean;
}

/**
 * A vertical, router-bound section rail: one link per section, collapsible to an icon strip.
 *
 * Promoted out of store management, where it was written as a page-local component on the argument
 * that "nothing else in the console has that shape". Billing has that shape. The only thing that was
 * page-specific was the base path and the footer, so both are now inputs — the rail itself was
 * always general.
 *
 * Each section is a real `routerLink` to `[basePath, key]`, so a tab is linkable, survives a reload
 * and answers the back button.
 *
 * It collapses to an icon strip because a tall rail beside a taller card wastes width on a laptop,
 * and the section being edited can end up scrolled away from the list it came from. Collapsed, the
 * icons keep their tooltips and their accessible names, so nothing is lost but the words.
 *
 * Anything projected is placed under a rule at the foot of the rail — store management uses it for
 * the storefront builder.
 */
@Component({
  selector: 'app-section-nav',
  imports: [Icon, RouterLink, TranslocoDirective],
  template: `
    <nav
      class="settings-nav"
      [class.collapsed]="collapsed()"
      [attr.aria-label]="label() ?? t('shared.sectionNav.ariaLabel')"
      *transloco="let t"
    >
      <div class="nav-head">
        <p class="nav-heading">{{ heading() ?? t('shared.sectionNav.heading') }}</p>
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
          [routerLink]="[basePath(), section.key]"
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
              [title]="t('shared.sectionNav.needsAttention')"
              [attr.aria-label]="t('shared.sectionNav.needsAttention')"
            ></span>
          }
        </a>
      }

      <ng-content />
    </nav>
  `,
  styleUrl: './section-nav.css',
})
export class SectionNav {
  private readonly transloco = inject(TranslocoService);

  readonly sections = input.required<readonly NavSection[]>();
  readonly active = input.required<string>();
  /** The route the section key is appended to, e.g. `/store-management`. */
  readonly basePath = input.required<string>();
  readonly label = input<string>();
  readonly heading = input<string>();

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
      this.collapsed() ? 'shared.sectionNav.expand' : 'shared.sectionNav.collapse',
    );
  }
}
