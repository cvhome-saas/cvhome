import {Component, DestroyRef, effect, inject, input, model, signal} from '@angular/core';

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

  private readonly destroyRef = inject(DestroyRef);
  private pending: ReturnType<typeof setTimeout> | null = null;
  /** What is in the box right now, which is ahead of `value` while the operator is still typing. */
  private readonly typed = signal<string | null>(null);

  constructor() {
    this.destroyRef.onDestroy(() => this.clear());
    // A value set from outside — a filter restored from the URL, or cleared by a button — lands in
    // the box directly and must not be echoed back out as though it had been typed.
    effect(() => {
      this.value();
      this.typed.set(null);
      this.clear();
    });
  }

  protected onInput(next: string): void {
    this.typed.set(next);
    this.clear();
    const wait = this.debounceMs();
    if (wait <= 0) {
      this.value.set(next);
      return;
    }
    this.pending = setTimeout(() => {
      this.pending = null;
      if (this.typed() !== null) {
        this.value.set(this.typed()!);
      }
    }, wait);
  }

  private clear(): void {
    if (this.pending !== null) {
      clearTimeout(this.pending);
      this.pending = null;
    }
  }
}
