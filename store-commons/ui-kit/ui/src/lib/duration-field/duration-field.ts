import {Component, forwardRef, input, model, signal} from '@angular/core';
import {NG_VALUE_ACCESSOR, type ControlValueAccessor} from '@angular/forms';

import {NumberField} from '../number-field/number-field';
import {Select, type SelectOption} from '../select/select';

/** Seconds in each grain the field offers, largest first — the order the parser tries them in. */
const UNITS = [
  {unit: 'd', seconds: 86400},
  {unit: 'h', seconds: 3600},
  {unit: 'm', seconds: 60},
  {unit: 's', seconds: 1},
] as const;

/** The grain a duration is shown in. */
export type DurationUnit = (typeof UNITS)[number]['unit'];

/**
 * An ISO-8601 duration as a number and a unit.
 *
 * Java serializes a `Duration` as `PT30M` / `P30D`, so that is what the wire carries and what a form
 * control has to hold. Neither end of that is a field: a text input asking an operator to type
 * `PT30M` is a format quiz, and the four-box `days/hours/minutes/seconds` row it replaced makes the
 * reader add up four numbers to answer "how long is this token good for".
 *
 * **Reading picks the largest unit that divides evenly**, so `PT1H30M` reads as `90 minutes` rather
 * than dropping the half hour, and `P30D` reads as `30 days` rather than `2592000 seconds`. Every
 * grain here is a whole number of seconds, so the round-trip is exact for any duration that is —
 * and a fractional-second duration, which `Duration` permits and no token setting uses, falls back
 * to seconds rounded, with the original string kept until the operator actually edits the field.
 */
@Component({
  selector: 'app-duration-field',
  imports: [NumberField, Select],
  template: `
    <app-number-field
      class="duration-amount"
      [value]="amount()"
      [id]="id()"
      [min]="0"
      [decimals]="0"
      [disabled]="isDisabled()"
      [invalid]="invalid()"
      [ariaLabel]="amountLabel()"
      (valueChange)="setAmount($event)"
    />
    <app-select
      class="duration-unit"
      [value]="unit()"
      [options]="unitOptions()"
      [disabled]="isDisabled()"
      [invalid]="invalid()"
      [ariaLabel]="unitLabel()"
      (valueChange)="setUnit($event)"
    />
  `,
  styleUrl: './duration-field.css',
  host: {'[attr.id]': 'null'},
  providers: [{provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => DurationField), multi: true}],
})
export class DurationField implements ControlValueAccessor {
  readonly id = input<string | null>(null);
  readonly invalid = input(false);
  readonly disabled = input(false);
  /** `[{value: 'm', label: 'minutes'}, …]` — the labels are the consumer's, so they translate. */
  readonly unitOptions = input.required<readonly SelectOption[]>();
  readonly amountLabel = input<string | null>(null);
  readonly unitLabel = input<string | null>(null);

  protected readonly amount = model<number | null>(null);
  protected readonly unit = signal<DurationUnit>('m');
  protected readonly isDisabled = signal(false);

  /**
   * What was written in, kept verbatim until an edit replaces it.
   *
   * Re-emitting a parsed value on load would rewrite `PT0.5S` to `PT1S` in the payload of a form
   * nobody touched — a silent edit, and the kind that is only noticed after it has been saved.
   */
  private original: string | null = null;

  private onChange: (value: string | null) => void = () => {};
  private onTouched: () => void = () => {};

  writeValue(value: string | null): void {
    this.original = value;
    const seconds = parseIso(value);
    if (seconds === null) {
      this.amount.set(null);
      this.unit.set('m');
      return;
    }
    const grain = UNITS.find((candidate) => seconds % candidate.seconds === 0) ?? UNITS[UNITS.length - 1];
    this.amount.set(Math.round(seconds / grain.seconds));
    this.unit.set(grain.unit);
  }

  registerOnChange(fn: (value: string | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(disabled: boolean): void {
    this.isDisabled.set(disabled);
  }

  protected setAmount(value: number | null): void {
    this.amount.set(value);
    this.emit();
  }

  protected setUnit(value: string): void {
    this.unit.set((UNITS.find((candidate) => candidate.unit === value) ?? UNITS[2]).unit);
    this.emit();
  }

  /** An empty amount is `null`, not `PT0S`: "not set" and "expires immediately" are different answers. */
  private emit(): void {
    const amount = this.amount();
    if (amount === null || !Number.isFinite(amount)) {
      this.original = null;
      this.onChange(null);
      this.onTouched();
      return;
    }
    const seconds = UNITS.find((candidate) => candidate.unit === this.unit())?.seconds ?? 60;
    this.original = formatIso(Math.max(0, Math.round(amount)) * seconds);
    this.onChange(this.original);
    this.onTouched();
  }
}

/** Total seconds in an ISO-8601 duration, or `null` when there is nothing to read. */
function parseIso(value: string | null | undefined): number | null {
  if (!value) return null;
  const match = /^P(?:(\d+(?:\.\d+)?)D)?(?:T(?:(\d+(?:\.\d+)?)H)?(?:(\d+(?:\.\d+)?)M)?(?:(\d+(?:\.\d+)?)S)?)?$/.exec(
    value.trim().toUpperCase(),
  );
  if (!match) return null;
  const [, days, hours, minutes, seconds] = match;
  if (!days && !hours && !minutes && !seconds) return null;
  return (
    Number(days ?? 0) * 86400 + Number(hours ?? 0) * 3600 + Number(minutes ?? 0) * 60 + Number(seconds ?? 0)
  );
}

/** The shortest ISO-8601 spelling of a whole number of seconds. */
function formatIso(total: number): string {
  if (total === 0) return 'PT0S';
  if (total % 86400 === 0) return `P${total / 86400}D`;
  const hours = Math.floor(total / 3600);
  const minutes = Math.floor((total % 3600) / 60);
  const seconds = total % 60;
  let out = 'PT';
  if (hours) out += `${hours}H`;
  if (minutes) out += `${minutes}M`;
  if (seconds) out += `${seconds}S`;
  return out;
}
