import {Component, forwardRef} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

@Component({
  selector: 'app-duration-input',
  standalone: false,
  templateUrl: './duration-input.component.html',
  styleUrls: ['./duration-input.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DurationInputComponent),
      multi: true
    }
  ]
})
export class DurationInputComponent implements ControlValueAccessor {
  days: number = 0;
  hours: number = 0;
  minutes: number = 0;
  seconds: number = 0;

  onChange: any = () => {
  };
  onTouch: any = () => {
  };

  writeValue(value: string): void {
    if (value) {
      this.parseDuration(value);
    } else {
      this.reset();
    }
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouch = fn;
  }

  update() {
    let iso = 'P';
    if (this.days > 0) iso += this.days + 'D';

    let time = '';
    if (this.hours > 0) time += this.hours + 'H';
    if (this.minutes > 0) time += this.minutes + 'M';
    if (this.seconds > 0) time += this.seconds + 'S';

    if (time.length > 0) {
      iso += 'T' + time;
    }

    if (iso === 'P') iso = 'PT0S';

    this.onChange(iso);
    this.onTouch();
  }

  parseDuration(value: string) {
    const daysMatch = value.match(/(\d+)D/);
    const hoursMatch = value.match(/(\d+)H/);
    const minutesMatch = value.match(/(\d+)M/);
    const secondsMatch = value.match(/(\d+(\.\d+)?)S/);

    this.days = daysMatch ? parseInt(daysMatch[1], 10) : 0;
    this.hours = hoursMatch ? parseInt(hoursMatch[1], 10) : 0;
    this.minutes = minutesMatch ? parseInt(minutesMatch[1], 10) : 0;
    this.seconds = secondsMatch ? parseFloat(secondsMatch[1]) : 0;
  }

  reset() {
    this.days = 0;
    this.hours = 0;
    this.minutes = 0;
    this.seconds = 0;
  }
}
