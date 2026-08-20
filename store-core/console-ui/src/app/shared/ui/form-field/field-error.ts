import {Component, input} from '@angular/core';
import {AbstractControl} from '@angular/forms';

import {serverErrorOf} from '@core/errors/form-error.utils';

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
 * The colour and size are the ones `.cross-field-error` uses for a message about two fields, so a
 * form that shows both says them in one voice.
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
  readonly control = input.required<AbstractControl>();
  readonly fallback = input('');

  protected message(): string | null {
    const control = this.control();
    if (!(control.dirty || control.touched)) {
      return null;
    }
    return serverErrorOf(control)?.message ?? (control.invalid ? this.fallback() : null);
  }
}
