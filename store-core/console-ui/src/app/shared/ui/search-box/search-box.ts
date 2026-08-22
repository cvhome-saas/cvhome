import {Component, input, model} from '@angular/core';

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
        (input)="value.set(input.value)"
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

  protected readonly id = `search-${nextId++}`;
}
