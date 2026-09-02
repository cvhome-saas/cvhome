import {Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, ReactiveFormsModule} from '@angular/forms';

import {DurationField} from './duration-field';

const UNITS = [
  {value: 'd', label: 'days'},
  {value: 'h', label: 'hours'},
  {value: 'm', label: 'minutes'},
  {value: 's', label: 'seconds'},
];

@Component({
  imports: [DurationField, ReactiveFormsModule],
  template: `<app-duration-field [formControl]="control" [unitOptions]="units" amountLabel="TTL" />`,
})
class Host {
  readonly control = new FormControl<string | null>(null);
  readonly units = UNITS;
}

describe('DurationField', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;

  beforeEach(async () => {
    await TestBed.configureTestingModule({imports: [Host]}).compileComponents();
    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
    fixture.detectChanges();
  });

  function shown(): {amount: string; unit: string} {
    const element = fixture.nativeElement as HTMLElement;
    return {
      amount: element.querySelector<HTMLInputElement>('.number-input')!.value,
      unit: element.querySelector<HTMLElement>('app-select')!.textContent!.trim(),
    };
  }

  function type(text: string): void {
    const input = (fixture.nativeElement as HTMLElement).querySelector<HTMLInputElement>('.number-input')!;
    input.value = text;
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
  }

  it('reads a duration in the largest unit that divides evenly', () => {
    host.control.setValue('P30D');
    fixture.detectChanges();
    expect(shown()).toEqual({amount: '30', unit: 'days'});
  });

  it('does not lose the remainder of a mixed duration', () => {
    host.control.setValue('PT1H30M');
    fixture.detectChanges();
    expect(shown()).toEqual({amount: '90', unit: 'minutes'});
  });

  it('reads PT30M as minutes', () => {
    host.control.setValue('PT30M');
    fixture.detectChanges();
    expect(shown()).toEqual({amount: '30', unit: 'minutes'});
  });

  it('shows nothing for an absent or unparseable value', () => {
    host.control.setValue(null);
    fixture.detectChanges();
    expect(shown().amount).toBe('');

    host.control.setValue('half an hour');
    fixture.detectChanges();
    expect(shown().amount).toBe('');
  });

  it('writes back the shortest ISO spelling', () => {
    host.control.setValue('PT30M');
    fixture.detectChanges();
    type('120');
    expect(host.control.value).toBe('PT2H');
  });

  /*
   * Loading a form must not edit it. Re-emitting the parsed value on write would rewrite the
   * payload of a form nobody touched, which is only ever noticed after it has been saved.
   */
  it('does not touch the control just for displaying it', () => {
    host.control.setValue('PT0.5S');
    fixture.detectChanges();
    expect(host.control.value).toBe('PT0.5S');
    expect(host.control.dirty).toBe(false);
  });

  it('clears to null rather than PT0S, which is a different answer', () => {
    host.control.setValue('PT30M');
    fixture.detectChanges();
    type('');
    expect(host.control.value).toBeNull();
  });
});
