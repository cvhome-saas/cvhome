import {Component, computed, inject, input} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import type {LocaleState, TranslationState} from '@models/content';

/**
 * The per-language chips of a content row: green when translated, amber when stale, grey when a
 * draft, outlined when the language is missing altogether. Each chip is titled with its state, so
 * colour is never the only signal.
 */
@Component({
  selector: 'app-locale-badges',
  template: `
    <span class="badges" role="list">
      @for (chip of chips(); track chip.code) {
        <span
          class="chip"
          role="listitem"
          [class.translated]="chip.state === 'TRANSLATED'"
          [class.stale]="chip.state === 'STALE'"
          [class.draft]="chip.state === 'DRAFT'"
          [class.missing]="chip.state === 'MISSING'"
          [title]="chip.title"
          [attr.aria-label]="chip.title"
          dir="ltr"
        >{{ chip.code.toUpperCase() }}</span>
      }
    </span>
  `,
  styleUrl: './locale-badges.css',
})
export class LocaleBadges {
  private readonly transloco = inject(TranslocoService);

  /** The item's translation states. */
  readonly locales = input.required<readonly LocaleState[]>();
  /** The store's languages; any of them missing from `locales` is drawn as MISSING. */
  readonly expected = input<readonly string[]>([]);

  protected readonly chips = computed(() => {
    this.transloco.activeLang();
    const present = new Map(this.locales().map((l) => [l.code, l.state]));
    const codes = [...new Set([...this.expected(), ...present.keys()])];
    return codes.map((code) => {
      const state: TranslationState = present.get(code) ?? 'MISSING';
      return {code, state, title: this.transloco.translate(`content.translationState.${state}`, {code: code.toUpperCase()})};
    });
  });
}
