import {Component, computed, DestroyRef, inject, input, linkedSignal, model} from '@angular/core';

import {Icon} from '@shared/ui/icon/icon';

let nextId = 0;

/**
 * The search box that sits in a list panel's header.
 *
 * Orders and products each wrote their own — `.order-search` and `.filter` — and they disagreed on
 * every measurement: 15rem against 14.5rem, `--card` against `--input`, a padding-derived height
 * against an explicit `2.25rem`, and a `.5rem` gap against `.45rem`. Two tables one click apart in
 * the nav, with visibly different search fields.
 *
 * **Not `app-text-field` with an icon.** A search box is not a form control: it has no label above
 * it, it is not bound to a `FormGroup`, its value is page state rather than something to be saved,
 * and it carries a clear affordance a form field must not. `type="search"` also brings the browser's
 * own escape-to-clear, which is worth keeping.
 */
@Component({
  selector: 'app-search-box',
  imports: [Icon],
  template: `
    <label class="search-box">
      <span class="sr-only">{{ label() }}</span>
      <app-icon name="search" />
      <input
        #input
        class="search-input"
        type="search"
        autocomplete="off"
        [id]="id"
        [value]="value()"
        [placeholder]="placeholder()"
        [attr.aria-describedby]="describedBy()"
        (input)="onInput(input.value)"
      />

      <!--
        Inside the label, so it is part of the box rather than something beside it. WebKit's own
        cancel button is suppressed in the stylesheet: it is drawn at a size and colour from a
        different design language, and it exists on no other engine.
      -->
      @if (showClear()) {
        <button
          class="search-clear"
          type="button"
          [attr.aria-label]="clearLabel()"
          (click)="clear(input)"
        >
          <app-icon name="x" />
        </button>
      }
    </label>
  `,
  styleUrl: './search-box.css',
  host: {'[style.--search-width]': 'width()'},
})
export class SearchBox {
  readonly value = model<string>('');
  /** Names the field for a screen reader. The placeholder is not a label. */
  readonly label = input.required<string>();
  readonly placeholder = input('');
  readonly describedBy = input<string | null>(null);
  /**
   * Names the clear button. Required for one to be drawn — a control whose only label is an icon
   * is unusable to a screen reader, and there is no sensible default here: what it clears is the
   * caller's word for what the box searches.
   */
  readonly clearLabel = input<string | null>(null);
  /**
   * Overrides the intrinsic width, for a header with room for more or less. A custom property
   * rather than a class, because the width is the one thing a host legitimately decides and a
   * selector written from outside cannot reach into this template — see lessons.md, "The design pass —
   * encapsulation, and three more things a native control hid".
   */
  readonly width = input<string | null>(null);

  /**
   * How long the operator has to stop typing before the term is published.
   *
   * The two boxes this replaced listened to `change`, so a filter only applied on blur or Enter —
   * you could type a SKU, look at an unchanged table, and conclude the filter was broken. Listening
   * to `input` instead is live, but a filter that reaches the server cannot fire per keystroke, so
   * the term settles first. `0` publishes immediately, for a box that filters in memory.
   */
  readonly debounceMs = input(300);

  protected readonly id = `search-${nextId++}`;

  /**
   * What is in the box right now, which is not what `value` holds while the term is settling.
   *
   * The clear button has to appear on the first keystroke rather than after the debounce, or it
   * arrives a third of a second late and moves under a cursor already on its way to it. Linked
   * rather than plain, so a term restored from the URL is reflected here without having been typed.
   */
  private readonly typed = linkedSignal(() => this.value());
  protected readonly showClear = computed(() => !!this.clearLabel() && this.typed() !== '');

  private readonly destroyRef = inject(DestroyRef);
  private pending: ReturnType<typeof setTimeout> | null = null;

  constructor() {
    this.destroyRef.onDestroy(() => this.stopTimer());
  }

  /**
   * A keystroke restarts the clock; the timer carries the value it was started with.
   *
   * **No mirror signal and no effect.** The first version kept "what is in the box right now" in a
   * separate signal and had an `effect` on `value` reset it, so that a value set from outside would
   * not be echoed back out as though it had been typed. Neither is needed: the timer closes over
   * the value it was started with, and a later external set simply wins the race it should win.
   * Writing to a signal from inside an effect to coordinate with a timer was more machinery than
   * the problem has, and the kind that is hard to reason about when it misbehaves.
   */
  protected onInput(next: string): void {
    this.typed.set(next);
    this.stopTimer();
    const wait = this.debounceMs();
    if (wait <= 0) {
      this.value.set(next);
      return;
    }
    this.pending = setTimeout(() => {
      this.pending = null;
      this.value.set(next);
    }, wait);
  }

  /**
   * Clearing is immediate, and does not wait out the debounce.
   *
   * A debounce exists so a filter is not sent per keystroke; pressing a clear button is one
   * deliberate act, and a list that keeps its old filter for another 300ms after it reads as
   * broken.
   */
  protected clear(element: HTMLInputElement): void {
    this.stopTimer();
    element.value = '';
    this.typed.set('');
    this.value.set('');
    element.focus();
  }

  private stopTimer(): void {
    if (this.pending !== null) {
      clearTimeout(this.pending);
      this.pending = null;
    }
  }
}
