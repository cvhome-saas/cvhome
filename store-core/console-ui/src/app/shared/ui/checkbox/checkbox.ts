import {
  Component,
  ElementRef,
  booleanAttribute,
  effect,
  input,
  model,
  viewChild,
} from '@angular/core';

import {Icon} from '@shared/ui/icon/icon';

/**
 * One member of a set the operator picks several of.
 *
 * **Not `app-toggle`.** A switch is an independent setting turned on or off — "require login at
 * checkout" — and it announces itself as one. These are *selections* from a group: which languages
 * a storefront publishes in, which categories a product belongs to. The roles are different, and a
 * screen reader reading "switch, Electronics, on" for a category membership is telling the operator
 * something slightly untrue.
 *
 * **Drawn, not tinted.** A native checkbox with `accent-color` colours only its *checked* state and
 * leaves the unchecked one to the platform — which, against either dark theme, is a solid dark
 * square indistinguishable from a selected one. lessons.md records that finding from the catalogue;
 * this is the control that stops it recurring. The box is a real `<input type="checkbox">` kept
 * visually hidden so the platform's own semantics, keyboard handling and form participation stay
 * intact, with a drawn box beside it.
 */
@Component({
  selector: 'app-checkbox',
  imports: [Icon],
  template: `
    <label class="checkbox" [class.disabled]="disabled()">
      <input
        #box
        class="native"
        type="checkbox"
        [disabled]="disabled()"
        [attr.aria-describedby]="describedBy()"
        (change)="flip($event)"
      />
      <span class="box" aria-hidden="true">
        @if (checked()) {
          <app-icon name="check" [size]="11" />
        }
      </span>
      <span class="copy" [class.sr-only]="hideLabel()" dir="auto">{{ label() }}</span>
    </label>
  `,
  styleUrl: './checkbox.css',
  host: {'[style.--checkbox-depth]': 'depth()'},
})
export class Checkbox {
  readonly checked = model(false);
  readonly label = input.required<string>();
  readonly disabled = input(false, {transform: booleanAttribute});
  readonly describedBy = input<string | null>(null);
  /** Keep the label for assistive tech but draw only the box — a table's row selector. */
  readonly hideLabel = input(false, {transform: booleanAttribute});
  /**
   * How far the row is indented, in levels.
   *
   * For a set that is a hierarchy — the product form's category picker — where the indent is the
   * only thing carrying the parentage and a second tree would be a restructuring control rather
   * than a membership one.
   */
  readonly depth = input(0);

  private readonly box = viewChild<ElementRef<HTMLInputElement>>('box');

  constructor() {
    /*
     * The signal writes the DOM, rather than a `[checked]` binding doing it.
     *
     * A property binding only writes when the expression differs from what it last *wrote* — and a
     * click changes the DOM behind its back. So tick a box, have the facade reject the change and
     * reset the model to what it was, and the binding sees no difference, writes nothing, and the
     * box stays visibly ticked against a model that says otherwise. Caught by a spec that did
     * exactly that. `app-toggle` never had the problem because a `<button aria-checked>` has no DOM
     * state of its own to fall out of step.
     */
    effect(() => {
      const box = this.box()?.nativeElement;
      if (box) {
        box.checked = this.checked();
      }
    });
  }

  protected flip(event: Event): void {
    this.checked.set((event.target as HTMLInputElement).checked);
  }
}
