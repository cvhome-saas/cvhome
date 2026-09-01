import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, ReactiveFormsModule} from '@angular/forms';

import {TextareaField} from './textarea-field';

@Component({
  imports: [TextareaField, ReactiveFormsModule],
  template: `
    <app-textarea [formControl]="control" [rows]="rows()" [maxLength]="maxLength()" ariaLabel="Notes" />
  `,
})
class Host {
  readonly control = new FormControl('', {nonNullable: true});
  readonly rows = signal(4);
  readonly maxLength = signal<number | null>(null);
}

describe('TextareaField', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;
  let element: HTMLElement;

  const area = () => element.querySelector('textarea') as HTMLTextAreaElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({imports: [Host]}).compileComponents();
    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
    fixture.detectChanges();
    element = fixture.nativeElement as HTMLElement;
  });

  it('round-trips the control value', () => {
    host.control.setValue('Leave at reception');
    fixture.detectChanges();
    expect(area().value).toBe('Leave at reception');

    area().value = 'Ring the bell';
    area().dispatchEvent(new Event('input'));
    expect(host.control.value).toBe('Ring the bell');
  });

  it('takes its height from rows', () => {
    host.rows.set(8);
    fixture.detectChanges();
    expect(area().rows).toBe(8);
  });

  it('counts against the limit, below the box where the resize grip cannot cover it', () => {
    host.maxLength.set(160);
    host.control.setValue('Twelve chars');
    fixture.detectChanges();

    expect(element.querySelector('.textarea-counter')?.textContent?.trim()).toBe('12 / 160');
    expect(area().getAttribute('maxlength')).toBe('160');
  });

  it('reports touched on blur', () => {
    area().dispatchEvent(new Event('blur'));
    expect(host.control.touched).toBeTrue();
  });
});
