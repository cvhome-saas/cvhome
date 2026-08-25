import {Component, computed, forwardRef, input, model, signal} from '@angular/core';
import {NG_VALUE_ACCESSOR, type ControlValueAccessor} from '@angular/forms';

/**
 * A date-and-time control whose value is an ISO instant (`2026-08-15T09:00:00Z`) or `''`.
 *
 * The native `datetime-local` input, framed like the console's text field: it is the one control
 * every platform renders with its own picker, it respects the reader's locale, and the console's own
 * calendar popover is date-only. The value round-trips through the browser's local zone and is
 * stored as UTC, which is what the content service's `publishAt` expects.
 */
@Component({
  selector: 'app-date-time-field',
  template: `
    <input
      class="dt-input"
      type="datetime-local"
      [id]="id()"
      [value]="local()"
      [min]="minLocal()"
      [disabled]="disabled()"
      [attr.aria-label]="ariaLabel()"
      [attr.aria-invalid]="invalid() || null"
      (input)="onInput($event)"
      (blur)="touched()"
    />
  `,
  styleUrl: './date-time-field.css',
  providers: [
    {provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => DateTimeField), multi: true},
  ],
})
export class DateTimeField implements ControlValueAccessor {
  readonly value = model<string>('');
  readonly id = input<string | null>(null);
  readonly ariaLabel = input<string | null>(null);
  readonly min = input<string>('');
  readonly disabled = input(false);
  readonly invalid = input(false);

  private readonly formDisabled = signal(false);
  private onChange: (value: string) => void = () => {};
  protected touched: () => void = () => {};

  protected readonly local = computed(() => toLocal(this.value()));
  protected readonly minLocal = computed(() => toLocal(this.min()));

  protected onInput(event: Event): void {
    const raw = (event.target as HTMLInputElement).value;
    const iso = raw ? new Date(raw).toISOString() : '';
    this.value.set(iso);
    this.onChange(iso);
  }

  writeValue(value: string | null): void {
    this.value.set(value ?? '');
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.touched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.formDisabled.set(isDisabled);
  }
}

/** ISO instant → the `YYYY-MM-DDTHH:mm` the native input wants, in the browser's zone. */
function toLocal(iso: string): string {
  if (!iso) {
    return '';
  }
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) {
    return '';
  }
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}
