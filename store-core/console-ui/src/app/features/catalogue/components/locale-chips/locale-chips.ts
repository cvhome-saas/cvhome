import {Component, input, output} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';
import type {ReferenceOption} from '@core/reference/reference-data.service';

/**
 * Which language the editor beside it is writing, and which languages already hold copy.
 *
 * A radio group rather than a tablist: the languages are not views of one record, they are values
 * of one field on it, and only one can be current. That also gives the arrow-key behaviour for
 * free and keeps the whole strip to a single tab stop.
 *
 * The dot is redundant with the chip's own tooltip and with the "not translated" line the editor
 * shows, per the design system's rule that colour never carries a fact alone.
 */
@Component({
  selector: 'app-locale-chips',
  imports: [Icon, TranslocoDirective],
  template: `
    <div class="chips" role="radiogroup" [attr.aria-label]="label()" *transloco="let t">
      @for (language of languages(); track language.code) {
        <button
          class="chip"
          type="button"
          role="radio"
          [class.current]="language.code === active()"
          [attr.aria-checked]="language.code === active()"
          [tabindex]="language.code === active() ? 0 : -1"
          [title]="
            translated().has(language.code)
              ? t('catalogue.copy.translatedIn', {language: language.label})
              : t('catalogue.copy.notTranslatedIn', {language: language.label})
          "
          (click)="picked.emit(language.code)"
        >
          <!-- The code, not the name: four full language names do not fit and the codes are what
               the operator sees on every other language control in the console. -->
          <span class="code" dir="ltr">{{ language.code }}</span>
          @if (translated().has(language.code)) {
            <app-icon
              name="checkCircle"
              [size]="11"
              [label]="t('catalogue.copy.translatedIn', {language: language.label})"
            />
          }
        </button>
      }
    </div>
  `,
  styleUrl: './locale-chips.css',
})
export class LocaleChips {
  readonly languages = input.required<readonly ReferenceOption[]>();
  readonly active = input.required<string>();
  /** Language codes that already hold a name. */
  readonly translated = input.required<ReadonlySet<string>>();
  readonly label = input.required<string>();

  readonly picked = output<string>();
}
