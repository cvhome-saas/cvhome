import {Component, computed, input} from '@angular/core';

import {ICON_PATHS, IconName} from './icon-paths';

/**
 * The glyphs that point somewhere, and so mirror when the page does.
 *
 * `chevronDown` is deliberately absent: it means "expand" everywhere it is used here, and down is
 * down in both directions. So are `sort` and `arrowUp`/`arrowDown` for the same reason.
 */
const DIRECTIONAL_ICONS: ReadonlySet<IconName> = new Set<IconName>([
  'arrowRight',
  'arrowLeft',
  'arrowUpRight',
  'chevronLeft',
  'chevronRight',
  'signIn',
  'signOut',
  'undo',
  'send',
  'share',
  'externalLink',
  'panelLeftClose',
  'panelLeftOpen',
]);

@Component({
  selector: 'app-icon',
  template: `
    <svg
      [attr.width]="size()"
      [attr.height]="size()"
      viewBox="0 0 24 24"
      [attr.fill]="filled() ? 'currentColor' : 'none'"
      stroke="currentColor"
      [attr.stroke-width]="filled() ? 1 : 1.8"
      stroke-linecap="round"
      stroke-linejoin="round"
      [attr.aria-hidden]="label() ? null : 'true'"
      [attr.aria-label]="label() || null"
      [class.rtl-flip]="mirrors()"
    >
      <path [attr.d]="path()" />
    </svg>
  `,
  styles: `
    :host {
      display: inline-flex;
      inline-size: var(--icon-size, 1em);
      block-size: var(--icon-size, 1em);
      flex: 0 0 auto;
    }

    svg {
      display: block;
      inline-size: 100%;
      block-size: 100%;
    }

    :host-context([dir='rtl']) .rtl-flip {
      transform: scaleX(-1);
    }
  `,
})
export class Icon {
  readonly name = input.required<IconName>();
  readonly label = input<string | null>(null);
  readonly size = input(20);
  /**
   * Whether the glyph mirrors in a right-to-left page.
   *
   * Defaults from the glyph itself rather than from the call site. It used to default to `false`,
   * so every arrow and chevron had to remember to opt in — and nineteen did while five did not,
   * including both date pickers' previous/next month chevrons, which meant the console shipped a
   * calendar whose arrows pointed the wrong way in Arabic on every page that has a date filter.
   * A direction is a property of the glyph; asking each of fifty-two call sites to know that was
   * never going to hold.
   *
   * Still an input, because a glyph can be used for something other than direction — a chevron
   * that means "expand" points down and mirrors nothing.
   */
  readonly flip = input<boolean | undefined>(undefined);
  /** Paints the glyph solid instead of drawing it as an outline — stars, status dots and the like. */
  readonly filled = input(false);
  protected readonly path = computed(() => ICON_PATHS[this.name()]);
  protected readonly mirrors = computed(() => this.flip() ?? DIRECTIONAL_ICONS.has(this.name()));
}
