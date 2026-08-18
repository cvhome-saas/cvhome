import {Component, input} from '@angular/core';
import {AbstractControl} from '@angular/forms';

import {serverErrorOf} from '@core/errors/form-error.utils';

@Component({
  selector: 'app-field-error',
  template: `
    @if (message(); as text) {
      <small class="field-error" aria-live="polite">{{ text }}</small>
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
