import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {Component} from '@angular/core';

import {dateKey} from '@cvhome-saas/ui-kit/i18n';
import {kitTranslocoTesting} from '@cvhome-saas/ui-kit/i18n';
import {DatePicker} from './date-picker';

/** Long enough to clear the post-commit dwell before the panel closes itself. */
const AFTER_COMMIT = 200;

function click(element: Element | null | undefined): void {
  (element as HTMLButtonElement).click();
}

function trigger(element: HTMLElement): HTMLButtonElement {
  return element.querySelector('[aria-haspopup="dialog"]') as HTMLButtonElement;
}

function dialog(element: HTMLElement): Element | null {
  return element.querySelector('[role="dialog"]');
}

function dayButton(element: HTMLElement, key: string): HTMLButtonElement | null {
  return element.querySelector(`[data-date="${key}"]`);
}

function actionButton(element: HTMLElement, text: string): HTMLButtonElement | undefined {
  return [...element.querySelectorAll<HTMLButtonElement>('.dp1-action')].find((candidate) =>
    candidate.textContent?.includes(text),
  );
}

function open(fixture: ComponentFixture<unknown>): HTMLElement {
  const element = fixture.nativeElement as HTMLElement;
  click(trigger(element));
  fixture.detectChanges();
  tick();
  return element;
}

describe('DatePicker', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DatePicker, ...kitTranslocoTesting().imports],
      providers: [...kitTranslocoTesting().providers],
    }).compileComponents();
  });

  function make(value = '', inputs: Record<string, unknown> = {}): ComponentFixture<DatePicker> {
    const fixture = TestBed.createComponent(DatePicker);
    fixture.componentRef.setInput('value', value);
    for (const [name, input] of Object.entries(inputs)) {
      fixture.componentRef.setInput(name, input);
    }
    fixture.detectChanges();
    return fixture;
  }

  it('opens on the month of the value it already holds', fakeAsync(() => {
    const fixture = make('2021-03-17');
    const element = open(fixture);

    // The chosen day is reachable without paging, which a picker that always opened on today
    // would not manage for a store that has traded since 2021.
    expect(dayButton(element, '2021-03-17')).not.toBeNull();
    expect(dayButton(element, '2021-03-17')?.getAttribute('aria-pressed')).toBe('true');
  }));

  it('writes the chosen day as a LocalDate and closes', fakeAsync(() => {
    const fixture = make('2026-05-10');
    const element = open(fixture);

    click(dayButton(element, '2026-05-21'));
    fixture.detectChanges();
    tick(AFTER_COMMIT);
    fixture.detectChanges();

    expect(fixture.componentInstance.value()).toBe('2026-05-21');
    expect(dialog(element)).toBeNull();
  }));

  it('refuses a day past the max', fakeAsync(() => {
    const fixture = make('2026-05-10', {max: '2026-05-15'});
    const element = open(fixture);

    expect(dayButton(element, '2026-05-21')?.disabled).toBeTrue();
    click(dayButton(element, '2026-05-21'));
    tick(AFTER_COMMIT);

    expect(fixture.componentInstance.value()).toBe('2026-05-10');
  }));

  it('clears back to unset, because the fields it serves are optional', fakeAsync(() => {
    const fixture = make('2026-05-10');
    const element = open(fixture);

    click(actionButton(element, 'Clear'));
    fixture.detectChanges();
    tick(AFTER_COMMIT);

    expect(fixture.componentInstance.value()).toBe('');
  }));

  it('offers Today only when the bounds allow it', fakeAsync(() => {
    const element = open(make('', {max: '2000-01-01'}));
    expect(actionButton(element, 'Today')?.disabled).toBeTrue();
  }));

  it('closes on Escape without choosing', fakeAsync(() => {
    const fixture = make('2026-05-10');
    const element = open(fixture);

    document.dispatchEvent(new KeyboardEvent('keydown', {key: 'Escape'}));
    fixture.detectChanges();
    tick(AFTER_COMMIT);

    expect(dialog(element)).toBeNull();
    expect(fixture.componentInstance.value()).toBe('2026-05-10');
  }));

  it('pages to the next month when an arrow key walks off the grid', fakeAsync(() => {
    const fixture = make('2026-05-31');
    const element = open(fixture);

    dayButton(element, '2026-05-31')?.dispatchEvent(
      new KeyboardEvent('keydown', {key: 'ArrowRight', bubbles: true}),
    );
    fixture.detectChanges();
    tick();

    expect(dayButton(element, '2026-06-01')).not.toBeNull();
  }));

  it('uses local time, so a date never shifts a day for a reader east of Greenwich', () => {
    // `toISOString().slice(0, 10)` is the bug this pins: it converts to UTC first.
    const midnight = new Date(2026, 0, 1);
    expect(dateKey(midnight)).toBe('2026-01-01');
  });
});

@Component({
  imports: [DatePicker, ReactiveFormsModule],
  template: `<app-date-picker [formControl]="control" />`,
})
class HostComponent {
  readonly control = new FormControl('2026-02-09', {nonNullable: true});
}

describe('DatePicker as a form control', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HostComponent, ...kitTranslocoTesting().imports],
      providers: [...kitTranslocoTesting().providers],
    }).compileComponents();
  });

  it('reads and writes through the reactive control it replaced an input for', fakeAsync(() => {
    const fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
    const element = open(fixture);

    expect(dayButton(element, '2026-02-09')?.getAttribute('aria-pressed')).toBe('true');

    click(dayButton(element, '2026-02-20'));
    fixture.detectChanges();
    tick(AFTER_COMMIT);

    expect(fixture.componentInstance.control.value).toBe('2026-02-20');
    // A date the operator picked is a change they made, so the form knows the section is dirty.
    expect(fixture.componentInstance.control.dirty).toBeTrue();
  }));

  it('follows the control into a disabled state', fakeAsync(() => {
    const fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
    fixture.componentInstance.control.disable();
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(trigger(element).disabled).toBeTrue();

    click(trigger(element));
    fixture.detectChanges();
    tick();
    expect(dialog(element)).toBeNull();
  }));
});
