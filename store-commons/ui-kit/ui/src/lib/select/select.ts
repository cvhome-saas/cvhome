import {
  Component,
  booleanAttribute,
  ElementRef,
  Injector,
  afterNextRender,
  computed,
  forwardRef,
  inject,
  input,
  model,
  signal,
  viewChild,
  viewChildren,
} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

import {Icon} from '../icon/icon';

/** One choice. `value` is what the form holds; `detail` is the second line, where there is one. */
export interface SelectOption {
  readonly value: string;
  readonly label: string;
  readonly detail?: string;
  readonly disabled?: boolean;
}

/** How long a type-ahead buffer survives, matching the tree's. */
const TYPEAHEAD_MS = 500;

/*
 * What the list will be, roughly, before it exists. Used only to decide which way to open, so an
 * approximation is enough — and it has to be one, because the element has no height until it has
 * rendered and the decision must be made in the same frame to avoid a visible jump.
 */
const OPTION_HEIGHT = 34;
const LIST_PADDING = 12;
const LIST_MAX_HEIGHT = 240;

let nextId = 0;

/**
 * A dropdown the console can actually theme.
 *
 * **Why not a native `<select>`.** `appearance: none` restyles the closed control and does nothing
 * whatsoever to the open list — that popup is drawn by the operating system, in the operating
 * system's colours, and cannot be reached by CSS at all. The console has three themes; two of them
 * are dark. Every native select in the catalogue and the product form rendered its options on a
 * white sheet, at a font size nothing else on the page uses, with a chevron from a different design
 * language than the eleven other controls beside it.
 *
 * **Why not `app-autocomplete`.** That component resolves a typed fragment against records the
 * server found, and its failure mode is "no matches". This one picks from a set that is already
 * known and closed — a brand, a unit of weight — where typing is a convenience and the list is the
 * contract. The ARIA differs accordingly: a `combobox` there, a `listbox` behind a button here.
 *
 * Follows `date-picker.ts` for the `ControlValueAccessor` and `autocomplete.ts` for the list, so
 * `formControlName` is a drop-in wherever a native select used to sit.
 */
@Component({
  selector: 'app-select',
  imports: [Icon],
  templateUrl: './select.html',
  styleUrl: './select.css',
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
    '[class.select-disabled]': 'isDisabled()',
    /* Reserves the room the clear button is drawn over. Without it a long option runs under it. */
    '[class.has-clear]': 'showClear()',
    '(keydown)': 'onKeydown($event)',
    '(focusout)': 'onFocusOut($event)',
  },
  providers: [{provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => Select), multi: true}],
})
export class Select implements ControlValueAccessor {
  private readonly injector = inject(Injector);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

  readonly value = model<string>('');
  readonly options = input<readonly SelectOption[]>([]);
  /** Mirrors the native `id`, so an existing `<label for>` keeps working. */
  readonly id = input<string | null>(null);
  readonly ariaLabel = input<string | null>(null);
  readonly labelledBy = input<string | null>(null);
  readonly describedBy = input<string | null>(null);
  /** Shown when nothing is chosen. Not an option — an empty value is expressed by `options`. */
  readonly placeholder = input('');
  readonly disabled = input(false);
  /** The consumer's `control.invalid && touched`. Draws the error frame; does not block choosing. */
  readonly invalid = input(false);
  /**
   * Offers a clear button once something is chosen.
   *
   * For a *filter*, where "no choice" is a real answer and getting back to it should not mean
   * hunting for the right option in a list. A form field should not use this: there, an empty value
   * is expressed by an option, so that the control always says what it holds.
   */
  readonly clearable = input(false, {transform: booleanAttribute});
  /** Names the clear button. Required for one to be drawn. */
  readonly clearLabel = input<string | null>(null);

  protected readonly listId = `select-list-${nextId++}`;
  protected readonly open = signal(false);
  /** Whether the list is drawn above the trigger rather than below it. See `placeList`. */
  protected readonly dropUp = signal(false);
  protected readonly activeIndex = signal(0);

  private readonly trigger = viewChild<ElementRef<HTMLButtonElement>>('trigger');
  private readonly optionRefs = viewChildren<ElementRef<HTMLElement>>('option');

  private readonly formDisabled = signal(false);
  private typeahead = '';
  private typeaheadAt = 0;

  private onChange: (value: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  protected readonly isDisabled = computed(() => this.disabled() || this.formDisabled());

  protected readonly selected = computed(() =>
    this.options().find((option) => option.value === this.value()),
  );

  /** What the trigger reads. The placeholder is styled differently, hence the separate flag. */
  protected readonly triggerLabel = computed(() => this.selected()?.label ?? this.placeholder());
  protected readonly isPlaceholder = computed(() => this.selected() === undefined);

  protected readonly activeOptionId = computed(() =>
    this.open() ? `${this.listId}-${this.activeIndex()}` : null,
  );

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

  /* ------------------------------------------------------------------------ opening ---- */

  protected toggle(): void {
    if (this.isDisabled()) {
      return;
    }
    if (this.open()) {
      this.close();
    } else {
      this.openList();
    }
  }

  private openList(): void {
    const index = this.options().findIndex((option) => option.value === this.value());
    this.activeIndex.set(index >= 0 ? index : this.firstEnabled());
    this.open.set(true);
    this.placeList();
    this.focusActive();
  }

  /**
   * Opens upward when there is no room below.
   *
   * The dimension-unit select sits at the foot of the pricing step, so a list that only ever drops
   * downward opened past the end of the page and its last options could not be reached at all.
   *
   * Measured against the viewport rather than the document: what matters is whether the operator can
   * see the list without scrolling, and the trigger's own rect is the only thing that answers that.
   * Checked once, on open — not on scroll, which would cost a listener per select for a case that
   * cannot arise while focus is inside the open list.
   */
  private placeList(): void {
    const trigger = this.trigger()?.nativeElement;
    if (!trigger) {
      return;
    }
    const rect = trigger.getBoundingClientRect();
    const below = window.innerHeight - rect.bottom;
    const above = rect.top;
    // The list's own cap, plus its padding — it has no height of its own until it renders.
    const wanted = Math.min(this.options().length * OPTION_HEIGHT + LIST_PADDING, LIST_MAX_HEIGHT);
    // Only flip when going up is actually better; a cramped field flips nothing.
    this.dropUp.set(below < wanted && above > below);
  }

  private close(restoreFocus = true): void {
    if (!this.open()) {
      return;
    }
    this.open.set(false);
    this.onTouched();
    if (restoreFocus) {
      // The trigger is the tab stop; leaving focus on a removed option would drop it to the body.
      afterNextRender(() => this.trigger()?.nativeElement.focus(), {injector: this.injector});
    }
  }

  /**
   * Closes when focus genuinely leaves the component.
   *
   * `relatedTarget` rather than a blur timer: the options are inside this host, so moving between
   * them fires `focusout` repeatedly and only the destination distinguishes a move within the list
   * from a departure. A `null` target is focus leaving the document entirely, which is not a reason
   * to close a list the operator may come back to.
   */
  protected onFocusOut(event: FocusEvent): void {
    const next = event.relatedTarget as Node | null;
    if (next && !this.host.nativeElement.contains(next)) {
      this.close(false);
    }
  }

  /** Whether the clear button has anything to clear. */
  protected readonly showClear = computed(
    () => this.clearable() && !!this.clearLabel() && this.value() !== '' && !this.isDisabled(),
  );

  /** Back to no choice, without opening the list. */
  protected clear(event: Event): void {
    // The trigger wraps this button, so a click here would otherwise also open the list.
    event.stopPropagation();
    this.value.set('');
    this.onChange('');
    this.onTouched();
  }

  protected choose(index: number): void {
    const option = this.options()[index];
    if (!option || option.disabled) {
      return;
    }
    this.value.set(option.value);
    this.onChange(option.value);
    this.close();
  }

  /* ----------------------------------------------------------------------- keyboard ---- */

  protected onKeydown(event: KeyboardEvent): void {
    if (this.isDisabled()) {
      return;
    }

    if (!this.open()) {
      // Closed: the keys that open, plus type-ahead that opens onto its match.
      switch (event.key) {
        case 'ArrowDown':
        case 'ArrowUp':
        case 'Enter':
        case ' ':
          event.preventDefault();
          this.openList();
          return;
        default:
          if (isPrintable(event)) {
            event.preventDefault();
            this.openList();
            this.seek(event.key);
          }
          return;
      }
    }

    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        this.step(1);
        break;
      case 'ArrowUp':
        event.preventDefault();
        this.step(-1);
        break;
      case 'Home':
        event.preventDefault();
        this.moveTo(this.firstEnabled());
        break;
      case 'End':
        event.preventDefault();
        this.moveTo(this.lastEnabled());
        break;
      case 'Enter':
      case ' ':
        event.preventDefault();
        this.choose(this.activeIndex());
        break;
      case 'Escape':
        event.preventDefault();
        // Escape abandons: the value is whatever it was before the list opened.
        this.close();
        break;
      case 'Tab':
        // Tab commits nothing and must still leave the field, so the list simply goes.
        this.close(false);
        break;
      default:
        if (isPrintable(event)) {
          event.preventDefault();
          this.seek(event.key);
        }
    }
  }

  /** Moves the active option, skipping disabled entries and wrapping at both ends. */
  private step(direction: 1 | -1): void {
    const options = this.options();
    if (options.length === 0) {
      return;
    }
    let index = this.activeIndex();
    for (let attempt = 0; attempt < options.length; attempt += 1) {
      index = (index + direction + options.length) % options.length;
      if (!options[index]?.disabled) {
        this.moveTo(index);
        return;
      }
    }
  }

  private moveTo(index: number): void {
    this.activeIndex.set(index);
    this.focusActive();
  }

  /**
   * Type-ahead over the option labels.
   *
   * The buffer accumulates while the operator keeps typing and resets after 500ms — the same window
   * the category tree uses, so the two behave alike. A repeated single character cycles through the
   * options that start with it, which is what a native select does and what anyone who has used one
   * expects.
   */
  private seek(key: string): void {
    const now = Date.now();
    const repeated = this.typeahead.length === 1 && this.typeahead === key.toLowerCase();
    this.typeahead = now - this.typeaheadAt > TYPEAHEAD_MS ? key.toLowerCase() : this.typeahead + key.toLowerCase();
    this.typeaheadAt = now;

    const options = this.options();
    const term = repeated ? key.toLowerCase() : this.typeahead;
    const from = repeated ? this.activeIndex() + 1 : this.activeIndex();

    for (let offset = 0; offset < options.length; offset += 1) {
      const index = (from + offset) % options.length;
      const option = options[index];
      if (option && !option.disabled && option.label.toLowerCase().startsWith(term)) {
        this.moveTo(index);
        return;
      }
    }
  }

  private firstEnabled(): number {
    const index = this.options().findIndex((option) => !option.disabled);
    return index >= 0 ? index : 0;
  }

  private lastEnabled(): number {
    const options = this.options();
    for (let index = options.length - 1; index >= 0; index -= 1) {
      if (!options[index]?.disabled) {
        return index;
      }
    }
    return 0;
  }

  /**
   * Focus follows the active option.
   *
   * Real focus rather than `aria-activedescendant` alone: the options are buttons, so the browser
   * scrolls them into view and the focus ring is the one the rest of the console draws. `afterNext
   * Render` because this app runs zone change detection with `eventCoalescing` — a microtask can
   * land before the option exists. `DatePicker` documents the same trap.
   */
  private focusActive(): void {
    afterNextRender(
      () => this.optionRefs()[this.activeIndex()]?.nativeElement.focus(),
      {injector: this.injector},
    );
  }
}

/** Whether a keystroke is a character to search with rather than a command. */
function isPrintable(event: KeyboardEvent): boolean {
  return event.key.length === 1 && !event.ctrlKey && !event.metaKey && !event.altKey;
}
