import {Component, computed, forwardRef, input, model, signal} from '@angular/core';
import {type ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

/**
 * The pill switch settings surfaces are built from.
 *
 * A real `<button role="switch">` rather than the mockup's styled `<div>`, so it is
 * reachable by keyboard and announces its state. With a `label` the whole row is the
 * control; without one it is just the switch, for card headers that name themselves.
 *
 * Two-way bound, so it composes with a signal — and a `ControlValueAccessor`, so
 * `formControlName` binds it directly inside a reactive form:
 *
 * ```html
 * <app-toggle
 *   label="Store is published"
 *   [checked]="form.controls.published.value"
 *   (checkedChange)="setFlag(form.controls.published, $event)"
 * />
 * ```
 */
@Component({
  selector: 'app-toggle',
  template: `
    <button
      class="toggle"
      type="button"
      role="switch"
      [attr.aria-checked]="checked()"
      [attr.aria-label]="label() ? null : name()"
      [disabled]="isDisabled()"
      (click)="flip()"
    >
      <span class="track" aria-hidden="true"><span class="knob"></span></span>

      @if (label()) {
        <span class="copy">
          <strong>{{ label() }}</strong>
          @if (description()) {
            <small>{{ description() }}</small>
          }
        </span>
      }
    </button>
  `,
  styleUrl: './toggle.css',
  providers: [{provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => Toggle), multi: true}],
})
export class Toggle implements ControlValueAccessor {
  readonly checked = model(false);
  /** Names the switch beside it. Absent for switches inside a heading that already names them. */
  readonly label = input<string | null>(null);
  readonly description = input<string | null>(null);
  /** What the switch is called for assistive tech when there is no visible `label`. */
  readonly name = input<string | null>(null);
  readonly disabled = input(false);

  private readonly formDisabled = signal(false);
  protected readonly isDisabled = computed(() => this.disabled() || this.formDisabled());
  private onChange: (value: boolean) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  protected flip(): void {
    const next = !this.checked();
    this.checked.set(next);
    this.onChange(next);
    this.onTouched();
  }

  writeValue(value: unknown): void {
    this.checked.set(value === true);
  }

  registerOnChange(fn: (value: boolean) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.formDisabled.set(isDisabled);
  }
}
