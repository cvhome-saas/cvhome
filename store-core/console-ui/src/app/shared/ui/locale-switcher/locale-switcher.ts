import {Component, ElementRef, Injector, afterNextRender, inject, input, model, viewChildren} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';
import type {ReferenceOption} from '@core/reference/reference-data.service';

/**
 * Which language the editor beside it is writing, and which languages already hold copy.
 *
 * A radio group rather than a tablist: the languages are not views of one record, they are values
 * of one field on it, and only one can be current. That keeps the whole strip to a single tab stop.
 *
 * The dot is redundant with the chip's own tooltip and with the "not translated" line the editor
 * shows, per the design system's rule that colour never carries a fact alone.
 *
 * **Promoted from the catalogue, where it was `app-locale-chips`.** Two other features had built the
 * same control by other means and both were worse: the product form's essentials step hand-rolled a
 * `.language-track` radiogroup keyed by *index* rather than by language code, and the store's home
 * section pressed `app-tab-switcher` into service, which announces a tablist for something that is
 * not one. Three implementations of "pick the language you are editing".
 *
 * **The arrow keys are new.** The catalogue's version set a roving `tabindex` and left a comment
 * saying the arrow behaviour came "for free" — it does not. `role="radio"` on a `<button>` buys the
 * announcement and none of the interaction; only a native `<input type="radio">` gets arrows from
 * the browser. So the strip had one tab stop and no way to reach the other languages from the
 * keyboard at all. WAI-ARIA's radiogroup pattern is implemented properly here, wrapping at both
 * ends, with Home and End.
 */
@Component({
  selector: 'app-locale-switcher',
  imports: [Icon, TranslocoDirective],
  template: `
    <!--
      The container listens but must not be focusable: a radiogroup's tab stop is its checked radio,
      and making the group itself focusable would add a second stop that does nothing. The rule
      cannot tell the two apart — the console toolbar carries the same disable for the same reason.
    -->
    <!-- eslint-disable-next-line @angular-eslint/template/interactive-supports-focus -->
    <div
      class="chips"
      role="radiogroup"
      [attr.aria-label]="label()"
      (keydown)="onKeydown($event)"
      *transloco="let t"
    >
      @for (language of languages(); track language.code; let index = $index) {
        <button
          #chip
          class="chip"
          type="button"
          role="radio"
          [class.current]="language.code === active()"
          [attr.aria-checked]="language.code === active()"
          [tabindex]="language.code === active() ? 0 : -1"
          [title]="
            filled().has(language.code)
              ? t('shared.localeSwitcher.translatedIn', {language: language.label})
              : t('shared.localeSwitcher.notTranslatedIn', {language: language.label})
          "
          (click)="choose(index)"
        >
          <!-- The code, not the name: four full language names do not fit and the codes are what
               the operator sees on every other language control in the console. -->
          <span class="code" dir="ltr">{{ language.code }}</span>
          @if (filled().has(language.code)) {
            <app-icon
              name="checkCircle"
              [size]="11"
              [label]="t('shared.localeSwitcher.translatedIn', {language: language.label})"
            />
          }
        </button>
      }
    </div>
  `,
  styleUrl: './locale-switcher.css',
})
export class LocaleSwitcher {
  private readonly injector = inject(Injector);

  readonly languages = input.required<readonly ReferenceOption[]>();
  readonly active = model.required<string>();
  /** Language codes that already hold copy. */
  readonly filled = input.required<ReadonlySet<string>>();
  readonly label = input.required<string>();

  private readonly chips = viewChildren<ElementRef<HTMLButtonElement>>('chip');

  protected choose(index: number): void {
    const language = this.languages()[index];
    if (language) {
      this.active.set(language.code);
    }
  }

  /**
   * The radiogroup pattern: arrows move *and* select, because a radio group has no separate
   * "focused but unchosen" state — that is what distinguishes it from the category tree, where
   * focus and selection are deliberately separate (see lessons.md).
   */
  protected onKeydown(event: KeyboardEvent): void {
    const count = this.languages().length;
    if (count === 0) {
      return;
    }
    const current = this.languages().findIndex((language) => language.code === this.active());
    const from = current >= 0 ? current : 0;
    let next: number;

    switch (event.key) {
      case 'ArrowRight':
      case 'ArrowDown':
        next = (from + 1) % count;
        break;
      case 'ArrowLeft':
      case 'ArrowUp':
        next = (from - 1 + count) % count;
        break;
      case 'Home':
        next = 0;
        break;
      case 'End':
        next = count - 1;
        break;
      default:
        return;
    }

    event.preventDefault();
    this.choose(next);
    // The chosen chip becomes the tab stop, so focus has to follow it or the next Tab leaves from
    // an element that is no longer focusable. `afterNextRender` because this app coalesces events —
    // `queueMicrotask` can land before the new `tabindex` is written (lessons.md).
    afterNextRender(() => this.chips()[next]?.nativeElement.focus(), {injector: this.injector});
  }
}
