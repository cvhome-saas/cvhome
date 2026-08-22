import {Component, inject, input} from '@angular/core';
import {AbstractControl} from '@angular/forms';
import {TranslocoService} from '@jsverse/transloco';

import {serverErrorOf} from '@core/errors/form-error.utils';
import {validationMessage} from '@shared/forms/validation-messages';

/**
 * One control's validation message, shown once the operator has been near the field.
 *
 * **It styles itself.** It used to carry no stylesheet at all, and `.field-error` was styled by
 * nobody — Angular's emulated encapsulation stamps this element with *this* component's attribute,
 * so a `.field-error` rule written in a page's stylesheet never matched it. Every validation
 * message in the console therefore rendered as plain body text: same colour as the label above it,
 * no spacing, nothing to mark it as a problem. Putting the rule here fixes every use site at once,
 * and is the only place it can live.
 *
 * **Where the words come from**, in order:
 *
 * 1. the server's own message, when a `fieldErrors[]` entry has been bound to this control;
 * 2. `fallback`, when the field has something specific to say;
 * 3. `shared.validation.*`, keyed on the validator that failed — see `validation-messages.ts`.
 *
 * Step 3 is new and is why `fallback` is now optional: a field with nothing special to say no
 * longer needs a key of its own, and thirty of them had one saying "Required".
 *
 * **`fallback` still wins over the map**, which is the other way round from how this was first
 * written. Two specs caught it immediately: the custom-domain field says "Enter a valid host name"
 * and the support-phone field names the phone number, and the map replaced both with "This is not
 * a valid value." A call site that bothered to write a sentence knows more about the field than a
 * table keyed on validator names does. The map is for removing boilerplate, not for overriding
 * intent.
 */
@Component({
  selector: 'app-field-error',
  template: `
    @if (message(); as text) {
      <small class="field-error" aria-live="polite">{{ text }}</small>
    }
  `,
  styles: `
    .field-error {
      display: block;
      margin-block-start: 0.35rem;
      color: var(--chart-5-foreground);
      font-size: var(--text-2xs);
      font-weight: 500;
      line-height: 1.45;
    }
  `,
})
export class FieldError {
  private readonly transloco = inject(TranslocoService);

  readonly control = input.required<AbstractControl>();
  /** A sentence for this field in particular, when the shared vocabulary is too general. */
  readonly fallback = input('');

  /**
   * A method, not a `computed`.
   *
   * Everything it reads — `control.dirty`, `control.errors`, `control.invalid` — is ordinary
   * mutable form state, not a signal, so a `computed` would evaluate once and cache a message that
   * could never change. lessons.md records the same trap costing the rich-text editor its
   * direction. Called from the template, this re-runs with change detection, which is exactly when
   * a control's validity can have moved.
   */
  protected message(): string | null {
    const control = this.control();
    if (!(control.dirty || control.touched)) {
      return null;
    }

    const server = serverErrorOf(control);
    if (server) {
      return server.message ?? null;
    }
    if (!control.invalid) {
      return null;
    }

    const specific = this.fallback();
    if (specific) {
      return specific;
    }

    const known = validationMessage(control.errors);
    return known ? this.transloco.translate(known.key, known.params) : null;
  }
}
