import {Component, computed, inject, input, model} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {Icon} from '../icon/icon';
import type {IconName} from '../icon/icon-paths';

/** One entry in a section rail. `key` is both the route segment and the active-state handle. */
export interface NavSection {
  readonly key: string;
  readonly labelKey: string;
  readonly icon: IconName;
  /** Draws the amber dot: this section has something waiting on the operator. */
  readonly attention?: boolean;
  /** A count shown at the end of the row (items in the section); hidden when the rail is collapsed. */
  readonly count?: string | null;
  /**
   * An i18n key for a heading this section sits under.
   *
   * Consecutive sections sharing a group render beneath one heading; omit it and the rail is the
   * flat list it has always been. Grouping rather than a second input because a rail is one list
   * with dividers in it — modelling it as a list of lists would make every existing caller wrap
   * itself in a single group to say nothing.
   */
  readonly group?: string;
  /**
   * A section the product does not have yet.
   *
   * Rendered, but not a link and not focusable as one: it names a place the operator will
   * eventually go without pretending they can go there now. `disabledHintKey` says why — an
   * unexplained dead row reads as a bug.
   */
  readonly disabled?: boolean;
  readonly disabledHintKey?: string;
}

/** A run of sections under one heading. Built by {@link SectionNav} from `NavSection.group`. */
interface NavGroup {
  readonly heading: string | null;
  readonly items: readonly NavSection[];
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
      [class.flat]="flat()"
      [attr.aria-label]="label() ?? t('shared.sectionNav.ariaLabel')"
      *transloco="let t"
    >
      <div class="nav-head">
        <!--
          An explicit empty heading renders none: a rail whose sections already carry group headings
          has nothing left for a title to say, and a blank one still takes its line.
        -->
        @if (heading() !== '') {
          <p class="nav-heading">{{ heading() ?? t('shared.sectionNav.heading') }}</p>
        } @else {
          <span class="nav-heading-spacer"></span>
        }
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

      @for (group of groups(); track group.heading) {
        @if (group.heading && !collapsed()) {
          <p class="nav-group">{{ t(group.heading) }}</p>
        }

        @for (section of group.items; track section.key) {
          @if (section.disabled) {
            <!--
              A span, not a link: there is nowhere to go. aria-disabled rather than omitting it
              from the tree, because the point of the row is to say the place exists.
            -->
            <span
              class="nav-item disabled"
              aria-disabled="true"
              [attr.title]="section.disabledHintKey ? t(section.disabledHintKey) : t(section.labelKey)"
            >
              <app-icon [name]="section.icon" />
              <span class="nav-copy">{{ t(section.labelKey) }}</span>
            </span>
          } @else {
            <a
              class="nav-item"
              [class.active]="section.key === active()"
              [routerLink]="[basePath(), section.key]"
              [attr.aria-current]="section.key === active() ? 'page' : null"
              [attr.title]="collapsed() ? t(section.labelKey) : null"
            >
              <app-icon [name]="section.icon" />
              <!--
                Hidden by width rather than removed, so the accessible name survives the collapse and
                the label has something to animate from when it comes back.
              -->
              <span class="nav-copy">{{ t(section.labelKey) }}</span>
              @if (section.count) {
                <span class="nav-count">{{ section.count }}</span>
              }
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
        }
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

  /**
   * Drops the rail's own surface.
   *
   * The rail paints a bordered card because it is normally dropped straight onto a page. Where the
   * host already provides one — uaa's shell wraps it with a brand block in the same card — that
   * border draws a second box inside the first, which reads as a mistake rather than as depth.
   */
  readonly flat = input(false);

  /**
   * The sections, folded into runs that share a `group`.
   *
   * Consecutive rather than sorted: the caller's order is the rail's order, and re-ordering it here
   * would silently move a section away from the one it was written next to.
   */
  protected readonly groups = computed<readonly NavGroup[]>(() => {
    const out: NavGroup[] = [];
    for (const section of this.sections()) {
      const heading = section.group ?? null;
      const last = out[out.length - 1];
      if (last && last.heading === heading) {
        (last.items as NavSection[]).push(section);
      } else {
        out.push({heading, items: [section]});
      }
    }
    return out;
  });

  protected toggleLabel(): string {
    return this.transloco.translate(
      this.collapsed() ? 'shared.sectionNav.expand' : 'shared.sectionNav.collapse',
    );
  }
}
