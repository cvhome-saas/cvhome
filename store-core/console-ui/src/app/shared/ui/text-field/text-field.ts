import {Component, computed, forwardRef, input, model, signal} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

import {Icon} from '@shared/ui/icon/icon';

/** The types this covers. Anything numeric belongs in `app-number-field`, which is a different job. */
export type TextFieldType = 'text' | 'email' | 'url' | 'tel' | 'search' | 'password';

/**
 * Where an asynchronous "is this taken?" check has got to.
 *
 * One vocabulary because the console drew it three ways: the catalogue and the product form used a
 * pending dot then a tick or a cross, create-store used a spinning clock and a tick with no cross
 * at all, and the social-links section only ever showed the tick. Same question, three answers.
 */
export type UniquenessCheck = 'idle' | 'pending' | 'free' | 'taken';

/**
 * The console's text input.
 *
 * Every form in the app used to write `<input class="control">` and rely on a `.control` rule from
 * whichever stylesheet its feature had copied — five of them, drifted (see `form-field.ts`). This
 * is that control, once, with the three behaviours the copies kept re-implementing badly:
 *
 * **The reveal toggle.** `auth.css` hand-drew a password eye with absolute positioning and
 * `right: .55rem`, which put it on the wrong side of the field in Arabic. Here it is a real button
 * in the flow of the field, so direction takes care of itself, and it reports its own state.
 *
 * **The uniqueness indicator.** See `UniquenessCheck`. Rendered inside the field rather than beside
 * it, so a slow answer cannot reflow the form.
 *
 * **Direction.** `dir="auto"` guesses from the first strong character, which is wrong for exactly
 * the values this console holds — a SKU, a slug, a domain, an email are Latin data that must not
 * flip inside an Arabic page, and `unicode-bidi: plaintext` is what keeps a mixed string readable.
 * `latin` is the opt-in for those; the default follows the page.
 *
 * Follows `number-field.ts` for the `ControlValueAccessor` and shares its metrics, because a name
 * and a price sit side by side in every form here.
 */
@Component({
  selector: 'app-text-field',
  imports: [Icon],
  templateUrl: './text-field.html',
  styleUrl: './text-field.css',
  host: {
    '[class.text-disabled]': 'isDisabled()',
    '[class.text-invalid]': 'invalid()',
  },
  providers: [{provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => TextField), multi: true}],
})
export class TextField implements ControlValueAccessor {
  readonly value = model<string>('');
  readonly type = input<TextFieldType>('text');
  /** Mirrors the native `id`, so an existing `<label for>` keeps working. */
  readonly id = input<string | null>(null);
  readonly ariaLabel = input<string | null>(null);
  readonly describedBy = input<string | null>(null);
  readonly placeholder = input('');
  readonly disabled = input(false);
  /** The consumer's `control.invalid && touched`. Draws the error frame; does not block typing. */
  readonly invalid = input(false);
  readonly autocomplete = input<string | null>(null);
  readonly inputmode = input<string | null>(null);
  /** Shown before the value — a scheme, a currency, an `@`. Not part of the value. */
  readonly prefix = input<string | null>(null);
  /** Shown after it — a domain suffix, a unit. Not part of the value. */
  readonly suffix = input<string | null>(null);
  /**
   * Caps the length and shows a counter. The counter is the reason this is not just `maxlength` on
   * the call site: a limit the operator cannot see is a limit they hit mid-word.
   */
  readonly maxLength = input<number | null>(null);
  /** Latin data inside a page that may be right-to-left — a SKU, a slug, a domain, an email. */
  readonly latin = input(false);
  /** Progress of an asynchronous uniqueness check, when the field has one. */
  readonly check = input<UniquenessCheck>('idle');
  /** What a screen reader is told while `check` is pending, free or taken. */
  readonly checkLabel = input<string | null>(null);
  /** Names the reveal button on a password field. Required for one to be drawn accessibly. */
  readonly revealLabel = input<string | null>(null);

  protected readonly revealed = signal(false);

  private readonly formDisabled = signal(false);
  protected readonly isDisabled = computed(() => this.disabled() || this.formDisabled());

  /** `password` becomes `text` while revealed; everything else is itself. */
  protected readonly inputType = computed(() =>
    this.type() === 'password' && this.revealed() ? 'text' : this.type(),
  );

  protected readonly isPassword = computed(() => this.type() === 'password');
  protected readonly counter = computed(() => {
    const limit = this.maxLength();
    return limit === null ? null : `${this.value().length} / ${limit}`;
  });

  private onChange: (value: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  /* --------------------------------------------------------------------------- CVA ---- */

  writeValue(value: string | null): void {
    this.value.set(value ?? '');
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.formDisabled.set(isDisabled);
  }

  /* ------------------------------------------------------------------------ editing ---- */

  protected onInput(element: HTMLInputElement): void {
    this.value.set(element.value);
    this.onChange(element.value);
  }

  protected onBlur(): void {
    this.onTouched();
  }

  protected toggleReveal(): void {
    this.revealed.update((shown) => !shown);
  }
}
