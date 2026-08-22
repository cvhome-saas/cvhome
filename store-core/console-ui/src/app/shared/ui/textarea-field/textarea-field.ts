import {Component, booleanAttribute, computed, forwardRef, input, model, signal} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

/**
 * Several lines of plain text.
 *
 * The counterpart to `app-text-field`, and deliberately not part of it: a textarea resizes, wraps,
 * and carries a row count, and folding those into the single-line control would have meant three
 * inputs that only apply half the time.
 *
 * **Plain text, not prose.** A seller's description — the thing that ends up on a storefront with
 * headings and links in it — belongs in `app-rich-text`. This is for a meta description, a note, an
 * address: content where markup would be noise and a limit matters more than formatting.
 */
@Component({
  selector: 'app-textarea',
  templateUrl: './textarea-field.html',
  styleUrl: './textarea-field.css',
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
    '[class.textarea-disabled]': 'isDisabled()',
    '[class.textarea-invalid]': 'invalid()',
  },
  providers: [{provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => TextareaField), multi: true}],
})
export class TextareaField implements ControlValueAccessor {
  readonly value = model<string>('');
  /** Mirrors the native `id`, so an existing `<label for>` keeps working. */
  readonly id = input<string | null>(null);
  readonly ariaLabel = input<string | null>(null);
  readonly describedBy = input<string | null>(null);
  readonly placeholder = input('');
  readonly disabled = input(false, {transform: booleanAttribute});
  /** The consumer's `control.invalid && touched`. Draws the error frame; does not block typing. */
  readonly invalid = input(false, {transform: booleanAttribute});
  readonly rows = input(4);
  /** Caps the length and shows a counter — a limit the operator cannot see is one they hit mid-word. */
  readonly maxLength = input<number | null>(null);
  /** Latin data inside a page that may be right-to-left. */
  readonly latin = input(false, {transform: booleanAttribute});
  /**
   * Grows with what is typed, from `rows` upward, instead of scrolling inside a fixed box.
   *
   * For an authoring surface — landing copy, a description — where the operator is reading back
   * what they wrote. `rows` stays the floor, and stays the whole story on an engine without
   * `field-sizing`, so nothing is lost where it is unsupported.
   */
  readonly autoGrow = input(false, {transform: booleanAttribute});
  /**
   * A length that is advised rather than enforced — an SEO meta description wants 50–160
   * characters and is still savable at 12.
   *
   * The counter turns amber outside the window and says nothing about it inside; a recommendation
   * that blocks a save is not a recommendation. Unlike `maxLength` it does not cap typing, and when
   * there is no `maxLength` the upper bound becomes what the counter counts toward.
   */
  readonly recommendedMin = input<number | null>(null);
  readonly recommendedMax = input<number | null>(null);

  private readonly formDisabled = signal(false);
  protected readonly isDisabled = computed(() => this.disabled() || this.formDisabled());

  protected readonly counter = computed(() => {
    const limit = this.maxLength() ?? this.recommendedMax();
    return limit === null ? null : `${this.value().length} / ${limit}`;
  });

  /** True once the text is outside the advised window. Silent while the field is still empty. */
  protected readonly offRecommendation = computed(() => {
    const length = this.value().length;
    if (length === 0) {
      return false;
    }
    const min = this.recommendedMin();
    const max = this.recommendedMax();
    return (min !== null && length < min) || (max !== null && length > max);
  });

  private onChange: (value: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;

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

  protected onInput(element: HTMLTextAreaElement): void {
    this.value.set(element.value);
    this.onChange(element.value);
  }

  protected onBlur(): void {
    this.onTouched();
  }
}
