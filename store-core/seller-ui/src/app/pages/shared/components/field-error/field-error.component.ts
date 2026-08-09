import {ChangeDetectionStrategy, Component, computed, inject, input} from '@angular/core';
import {toObservable, toSignal} from '@angular/core/rxjs-interop';
import {AbstractControl, ValidationErrors} from '@angular/forms';
import {switchMap} from 'rxjs';
import {TranslateService} from '@ngx-translate/core';
import {ApiErrorService} from 'seller-core';
import {SERVER_ERROR_KEY, serverErrorOf} from 'seller-core';

/** Client-validator key → i18n key, so `[messages]` is optional for the ordinary cases. */
const GENERIC_FIELD_MESSAGES: Readonly<Record<string, string>> = {
  required: 'ERRORS.FIELD.REQUIRED',
  pattern: 'ERRORS.FIELD.PATTERN',
  email: 'ERRORS.FIELD.EMAIL',
  minlength: 'ERRORS.FIELD.MINLENGTH',
  maxlength: 'ERRORS.FIELD.MAXLENGTH',
  min: 'ERRORS.FIELD.MIN',
  max: 'ERRORS.FIELD.MAX',
};

/**
 * Renders the errors on one control, replacing the hand-rolled `<div class="caption status-danger">` blocks
 * and their per-validator `@if` ladders.
 *
 * ```html
 * <input formControlName="name" fullWidth id="name" nbInput ngxControlStatus>
 * <ngx-field-error [control]="facade.formService.name"
 *                  [messages]="{required: 'POD_FORM.NAME_REQUIRED', pattern: 'POD_FORM.NAME_PATTERN'}"/>
 * ```
 *
 * `[messages]` overrides the text per validator; omit it and the `ERRORS.FIELD.*` defaults apply.
 */
@Component({
  selector: 'ngx-field-error',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (visible()) {
      <div class="caption status-danger mt-1">
        @for (message of visibleMessages(); track message) {
          <div>{{ message }}</div>
        }
      </div>
    }
  `,
})
export class FieldErrorComponent {

  readonly control = input.required<AbstractControl>();

  /** Per-validator overrides, e.g. `{required: 'POD_FORM.NAME_REQUIRED'}`. */
  readonly messages = input<Record<string, string>>({});

  private readonly apiErrors = inject(ApiErrorService);

  private readonly translate = inject(TranslateService);

  /**
   * `AbstractControl` is not signal-reactive, so its event stream is bridged into one and read by the
   * computeds below purely to make them recompute.
   *
   * `control.events` rather than `valueChanges` because of `TouchedChangeEvent`: every existing template
   * gates on `dirty || touched`, and a control the user blurred without typing emits nothing else.
   */
  private readonly controlEvent = toSignal(
    toObservable(this.control).pipe(switchMap(control => control.events)),
    {equal: () => false},
  );

  protected readonly visible = computed(() => {
    this.controlEvent();
    const control = this.control();
    // A server error is shown as soon as it arrives; `applyFieldErrors` marks the control touched so the
    // usual gate would pass anyway, but this makes it independent of that.
    return serverErrorOf(control) !== null || (control.invalid && (control.dirty || control.touched));
  });

  protected readonly visibleMessages = computed(() => {
    this.controlEvent();
    const control = this.control();

    // A server error is the authoritative statement about this value — it saw the whole request, while the
    // client validators only saw the field. It wins, and it suppresses the rest.
    const serverError = serverErrorOf(control);
    if (serverError) {
      return [this.apiErrors.fieldMessage(serverError)];
    }

    const errors: ValidationErrors | null = control.errors;
    if (!errors) {
      return [];
    }

    return Object.entries(errors)
      .filter(([key]) => key !== SERVER_ERROR_KEY)
      .map(([key, value]) => this.clientMessage(key, value))
      .filter((it): it is string => it !== null);
  });

  private clientMessage(key: string, value: unknown): string | null {
    const translationKey = this.messages()[key] ?? GENERIC_FIELD_MESSAGES[key];
    if (!translationKey) {
      return null;
    }
    const params = (typeof value === 'object' && value !== null) ? value as Record<string, unknown> : undefined;
    return this.translate.instant(translationKey, params);
  }
}
