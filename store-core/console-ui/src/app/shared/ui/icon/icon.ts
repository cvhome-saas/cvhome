import {Component, computed, input} from '@angular/core';

import {ICON_PATHS, IconName} from './icon-paths';

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
      [class.rtl-flip]="flip()"
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
  readonly flip = input(false);
  /** Paints the glyph solid instead of drawing it as an outline — stars, status dots and the like. */
  readonly filled = input(false);
  protected readonly path = computed(() => ICON_PATHS[this.name()]);
}
