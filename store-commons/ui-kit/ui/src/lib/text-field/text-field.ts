import {Component, booleanAttribute, computed, forwardRef, input, model, signal} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

import {Icon} from '../icon/icon';
import type {IconName} from '@cvhome-saas/ui-kit';

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
    /*
     * The id belongs to the control this draws, not to the host.
     *
     * A static or bound `id` on a component element lands in the DOM *as well as* matching the
     * `id` input, so the host and the inner control ended up carrying the same id — invalid HTML,
     * and `<label for>` then resolves to the host, which is not a labelable element, so the
     * association silently does not happen. Found by probing where the id actually went; four of
     * these six components had shipped with it.
     */
    '[attr.id]': 'null',
    '[class.text-disabled]': 'isDisabled()',
    '[class.text-invalid]': 'invalid()',
    '[class.text-mono]': 'mono()',
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
  readonly disabled = input(false, {transform: booleanAttribute});
  /** The consumer's `control.invalid && touched`. Draws the error frame; does not block typing. */
  readonly invalid = input(false, {transform: booleanAttribute});
  readonly autocomplete = input<string | null>(null);
  readonly inputmode = input<string | null>(null);
  /** Shown before the value — a scheme, a currency, an `@`. Not part of the value. */
  readonly prefix = input<string | null>(null);
  /**
   * A glyph before the value — an envelope on a support address, a pin on a street.
   *
   * Decoration, so it is `aria-hidden`: the field already has a label, and "envelope, Support
   * email" is worse than "Support email".
   */
  readonly icon = input<IconName | null>(null);
  /** Shown after it — a domain suffix, a unit. Not part of the value. */
  readonly suffix = input<string | null>(null);
  /**
   * Caps the length and shows a counter. The counter is the reason this is not just `maxlength` on
   * the call site: a limit the operator cannot see is a limit they hit mid-word.
   */
  readonly maxLength = input<number | null>(null);
  /** Latin data inside a page that may be right-to-left — a SKU, a slug, a domain, an email. */
  readonly latin = input(false, {transform: booleanAttribute});
  /**
   * Sets the value in a monospaced face — an API key, an app id, a token.
   *
   * An input rather than a class the caller adds, because a class on the host cannot reach the
   * inner `<input>`: Angular scopes styles by the defining component, so a rule written outside
   * this template matches the host and stops there (lessons.md, "The design pass — encapsulation,
   * and three more things a native control hid").
   */
  readonly mono = input(false, {transform: booleanAttribute});
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

  /**
   * A keystroke.
   *
   * **`onChange` before `value.set`, and the order is load-bearing.** Setting a `model()` emits its
   * output synchronously, so a host listening to `(valueChange)` runs *inside* this method — and
   * the domain field does exactly that, normalising `https://Shop.Example.com:8443/x` down to a
   * bare hostname and writing it back to the control. With the writes the other way round, that
   * normalised value was immediately overwritten by the raw one on the next line, and the field
   * kept whatever had been pasted. Caught by a store-management spec, not by looking at it.
   */
  protected onInput(element: HTMLInputElement): void {
    this.onChange(element.value);
    this.value.set(element.value);
  }

  protected onBlur(): void {
    this.onTouched();
  }

  protected toggleReveal(): void {
    this.revealed.update((shown) => !shown);
  }
}
