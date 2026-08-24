import {Component, booleanAttribute, input} from '@angular/core';
import {AbstractControl} from '@angular/forms';

import {FieldError} from './field-error';

let nextId = 0;

/**
 * A label, the control it names, an optional hint, and the one message when something is wrong.
 *
 * **What this replaces.** Five stylesheets each defined `.field`, `.field-label`, `.field-hint`,
 * `.required` and `.control` — catalogue's and product-form's `editor-card.css` (near-identical
 * copies of each other), store-management's `settings-card.css`, create-store's page stylesheet and
 * auth's, which styled bare elements and had no wrapper at all. They had drifted: two used
 * `.55rem .7rem` padding and two `0.6rem 0.75rem`, textareas were `5.5rem` tall in one place and
 * `6.5rem` in another, and only create-store had ever drawn an invalid state. A field is the most
 * repeated object in this console after a button; it gets one definition, for the same reason the
 * action classes were consolidated into `styles.css`.
 *
 * **The control is projected, not owned.** A form field wraps whatever the form needs —
 * `app-text-field`, `app-select`, `app-number-field`, `app-toggle`, `app-rich-text` — so this
 * component contributes the label, the layout and the message, and each control keeps its own
 * behaviour. Passing `control` is what lets it render the error and mark the label as required
 * without the call site repeating either.
 *
 * ```html
 * <app-form-field [label]="t('storeSettings.details.name')" [control]="form.controls.name" required>
 *   <app-text-field formControlName="name" />
 * </app-form-field>
 * ```
 *
 * The projected control is a descendant of the `<label>`, so it is implicitly associated and needs
 * no `for`/`id` pair. `controlId` is there for the controls that render their own `<input>` and
 * want an explicit association anyway.
 */
@Component({
  selector: 'app-form-field',
  imports: [FieldError],
  template: `
    <!-- The control is projected in, so it becomes a descendant of this label at runtime and is
         implicitly associated. controlId is only for a control that renders its own input and
         wants the explicit for/id pair as well. -->
    <label class="field" [attr.for]="controlId()">
      <span class="field-label">
        {{ label() }}
        @if (required()) {
          <span class="required" aria-hidden="true">*</span>
        }
      </span>

      <ng-content />

      @if (control(); as bound) {
        <app-field-error [control]="bound" [fallback]="fallback()" />
      }

      @if (hint()) {
        <p class="field-hint">{{ hint() }}</p>
      }
    </label>
  `,
  styleUrl: './form-field.css',
  host: {'[class.field-wide]': 'wide()'},
})
export class FormField {
  readonly label = input.required<string>();
  /**
   * The control this field names. Optional only so a field can wrap something that is not a form
   * control at all — a read-only value, a pair of buttons — without inventing one.
   */
  readonly control = input<AbstractControl | null>(null);
  /** Drawn as an asterisk, `aria-hidden` because the control's own `required` is what a reader needs. */
  readonly required = input(false, {transform: booleanAttribute});
  /** A line under the control saying what is expected, or what the platform will do with it. */
  readonly hint = input<string | null>(null);
  /** A message for this field in particular; `shared.validation.*` covers the rest. */
  readonly fallback = input('');
  /** Spans every column of a `.field-grid`. */
  readonly wide = input(false, {transform: booleanAttribute});
  /** Set when the projected control renders its own `<input id>` and wants an explicit label link. */
  readonly controlId = input<string | null>(null);

  protected readonly id = `field-${nextId++}`;
}
