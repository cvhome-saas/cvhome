import {
  booleanAttribute,
  Component,
  computed,
  forwardRef,
  input,
  linkedSignal,
  model,
  signal,
} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

/** What a figure may be as it is typed: digits, one separator, an optional leading sign. */
const PARTIAL = /^-?\d*(?:[.,]\d*)?$/;

/**
 * A number field with no browser chrome.
 *
 * **Why not `<input type="number">`.** Three reasons, in order of how much they cost:
 *
 * 1. **It discards what it cannot parse, silently.** A native number input whose content the browser
 *    considers malformed reports `value === ''` — so the form control goes `null` while the operator
 *    is still looking at their digits, and a "price is required" error appears under a field with a
 *    number visibly in it. This one keeps the text and the value in step, and refuses the keystroke
 *    that would make them disagree.
 * 2. **Spin buttons.** Nothing else in this console has them, they differ per engine, and they sit
 *    exactly where a currency or unit belongs.
 * 3. **The scroll wheel changes the value.** Scrolling past a focused price field silently edits the
 *    product.
 *
 * `inputmode="decimal"` still brings up the numeric keypad on a phone, which is the only thing
 * `type="number"` was really buying.
 *
 * **`null` is not zero.** An empty price means "not priced yet", which is a real state for a draft
 * and one `product-form.service.ts` already distinguishes; a control that coerced empty to `0` would
 * quietly publish a free product.
 */
@Component({
  selector: 'app-number-field',
  templateUrl: './number-field.html',
  styleUrl: './number-field.css',
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
    '[class.number-disabled]': 'isDisabled()',
    '[class.number-invalid]': 'invalid()',
    '[class.number-compact]': 'compact()',
    '[class.number-end]': "align() === 'end'",
  },
  providers: [{provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => NumberField), multi: true}],
})
export class NumberField implements ControlValueAccessor {
  readonly value = model<number | null>(null);
  /** Mirrors the native `id`, so an existing `<label for>` keeps working. */
  readonly id = input<string | null>(null);
  readonly ariaLabel = input<string | null>(null);
  readonly describedBy = input<string | null>(null);
  readonly placeholder = input('');
  readonly disabled = input(false);
  /** The consumer's `control.invalid && touched`. Draws the error frame; does not block typing. */
  readonly invalid = input(false);
  /** Shown before the figure — a currency. Not part of the value. */
  readonly prefix = input<string | null>(null);
  /** Shown after it — a unit. Not part of the value. */
  readonly suffix = input<string | null>(null);
  readonly min = input<number | null>(null);
  readonly max = input<number | null>(null);
  /** How many decimals the figure is rounded to on blur. `0` makes it an integer field. */
  readonly decimals = input<number | null>(null);
  /**
   * The size for a field inside a table cell: shorter, smaller type, tighter radius.
   *
   * A size on the component rather than a class on the host. The host is not the frame — the frame
   * is the shell inside this template — so a stylesheet outside could only ever draw a *second*
   * border around this one, which is what the products table did until this input existed.
   */
  readonly compact = input(false, {transform: booleanAttribute});
  /** `end` for a figure in a numeric column, where the digits should line up with the ones above. */
  readonly align = input<'start' | 'end'>('start');

  /**
   * What is in the box.
   *
   * Held separately from `value` because they are legitimately out of step mid-edit: `-`, `1.` and
   * an empty box are all things you type on the way to a number and none of them is one.
   */
  protected readonly text = linkedSignal<number | null, string>({
    source: this.value,
    computation: (value, previous) => {
      // What is already in the box wins when it means the same number: `1.`, `1.0` and `01` all
      // parse to 1, and rewriting them mid-keystroke would move the caret out from under the
      // operator. Only a value that genuinely differs redraws the text.
      if (previous !== undefined && parse(previous.value) === value) {
        return previous.value;
      }
      return value === null ? '' : String(value);
    },
  });

  private readonly formDisabled = signal(false);
  protected readonly isDisabled = computed(() => this.disabled() || this.formDisabled());

  private onChange: (value: number | null) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  /* --------------------------------------------------------------------------- CVA ---- */

  writeValue(value: number | null): void {
    // The text follows `value`, so this is the whole of it.
    //
    // It used to set the text itself, and *only* here — which meant a field driven by `[(value)]`
    // rather than by a form never had its text set at all, and the products table's inline editor
    // opened empty over a product that plainly had a price. The link between the two now lives in
    // `text` for both ways of driving the control.
    this.value.set(value ?? null);
  }

  registerOnChange(fn: (value: number | null) => void): void {
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
   * A character that cannot lead anywhere numeric is rejected outright — the box is restored to what
   * it held, so a stray letter never reaches the value and never clears the field. Everything the
   * regex allows is kept as text, and published only once it parses.
   */
  protected onInput(element: HTMLInputElement): void {
    const next = element.value;
    if (!PARTIAL.test(next)) {
      element.value = this.text();
      return;
    }
    this.text.set(next);

    const parsed = parse(next);
    if (parsed === this.value()) {
      return;
    }
    this.value.set(parsed);
    this.onChange(parsed);
  }

  /** Tidies the figure and commits it. `1.` becomes `1`; `007` becomes `7`. */
  protected onBlur(): void {
    const parsed = parse(this.text());
    const places = this.decimals();
    const rounded =
      parsed !== null && places !== null ? Number(parsed.toFixed(places)) : parsed;

    this.text.set(rounded === null ? '' : String(rounded));
    if (rounded !== this.value()) {
      this.value.set(rounded);
      this.onChange(rounded);
    }
    this.onTouched();
  }

  /**
   * The wheel is not an editing gesture.
   *
   * A native number input changes its value when the page scrolls under a focused field, which is
   * how a price gets edited by someone reading the rest of the form. Blurring is the least
   * surprising answer: the scroll continues and the field stops listening.
   */
  protected onWheel(element: HTMLInputElement): void {
    element.blur();
  }
}

/** `null` for anything that is not a finite number, including the empty box and a lone `-`. */
function parse(text: string): number | null {
  const normalised = text.replace(',', '.').trim();
  if (normalised === '' || normalised === '-' || normalised === '.') {
    return null;
  }
  const parsed = Number(normalised);
  return Number.isFinite(parsed) ? parsed : null;
}
