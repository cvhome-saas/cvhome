import {Component, computed, forwardRef, input, model, signal} from '@angular/core';
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
  readonly disabled = input(false);
  /** The consumer's `control.invalid && touched`. Draws the error frame; does not block typing. */
  readonly invalid = input(false);
  readonly rows = input(4);
  /** Caps the length and shows a counter — a limit the operator cannot see is one they hit mid-word. */
  readonly maxLength = input<number | null>(null);
  /** Latin data inside a page that may be right-to-left. */
  readonly latin = input(false);

  private readonly formDisabled = signal(false);
  protected readonly isDisabled = computed(() => this.disabled() || this.formDisabled());

  protected readonly counter = computed(() => {
    const limit = this.maxLength();
    return limit === null ? null : `${this.value().length} / ${limit}`;
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
