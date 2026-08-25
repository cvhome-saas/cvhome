import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';

import {translocoTesting} from '@testing/transloco-testing';
import {DateRangePicker} from './date-range-picker';

const JULY_RANGE = {
  from: new Date(2026, 6, 5),
  to: new Date(2026, 7, 4),
};

/** Long enough to clear the post-commit dwell before the panel closes itself. */
const AFTER_COMMIT = 200;

function click(element: Element | null): void {
  (element as HTMLButtonElement).click();
}

function dateButton(element: HTMLElement, key: string): HTMLButtonElement {
  return element.querySelector(`[data-date="${key}"]`) as HTMLButtonElement;
}

function dialog(element: HTMLElement): Element | null {
  return element.querySelector('[role="dialog"]');
}

function openPicker(fixture: ComponentFixture<DateRangePicker>): HTMLElement {
  const element = fixture.nativeElement as HTMLElement;
  click(element.querySelector('[aria-haspopup="dialog"]'));
  fixture.detectChanges();
  tick();
  return element;
}

function labelledButton(element: HTMLElement, text: string): HTMLButtonElement | undefined {
  return [...element.querySelectorAll('button')].find((candidate) =>
    candidate.textContent?.includes(text),
  );
}

describe('DateRangePicker', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DateRangePicker, ...translocoTesting().imports],
      providers: [...translocoTesting().providers],
    }).compileComponents();
  });

  it('opens and closes the panel', fakeAsync(() => {
    const fixture = TestBed.createComponent(DateRangePicker);
    fixture.detectChanges();
    const element = openPicker(fixture);

    expect(dialog(element)).not.toBeNull();

    document.dispatchEvent(new KeyboardEvent('keydown', {key: 'Escape'}));
    fixture.detectChanges();

    expect(dialog(element)).toBeNull();
  }));

  it('moves focus into the calendar on open and back to the trigger on close', fakeAsync(() => {
    const fixture = TestBed.createComponent(DateRangePicker);
    fixture.componentInstance.value.set(JULY_RANGE);
    // Focus only lands on a rendered cell, so the component must be in the document.
    document.body.appendChild(fixture.nativeElement);
    fixture.detectChanges();
    const element = openPicker(fixture);

    expect(document.activeElement).toBe(dateButton(element, '2026-07-05'));

    document.dispatchEvent(new KeyboardEvent('keydown', {key: 'Escape'}));
    fixture.detectChanges();

    expect(document.activeElement).toBe(element.querySelector('[aria-haspopup="dialog"]'));
    fixture.nativeElement.remove();
  }));

  it('applies the range as soon as the end date is picked, with no confirmation step', fakeAsync(() => {
    const fixture = TestBed.createComponent(DateRangePicker);
    fixture.componentInstance.value.set(JULY_RANGE);
    fixture.detectChanges();
    const element = openPicker(fixture);

    click(dateButton(element, '2026-07-12'));
    fixture.detectChanges();

    // Half a range must not commit anything yet.
    expect(fixture.componentInstance.value().from?.getDate()).toBe(5);
    expect(dialog(element)).not.toBeNull();

    click(dateButton(element, '2026-07-20'));
    fixture.detectChanges();

    expect(fixture.componentInstance.value().from?.getDate()).toBe(12);
    expect(fixture.componentInstance.value().to?.getDate()).toBe(20);

    tick(AFTER_COMMIT);
    fixture.detectChanges();

    expect(dialog(element)).toBeNull();
  }));

  it('restarts the selection when a date before the pending start is picked', fakeAsync(() => {
    const fixture = TestBed.createComponent(DateRangePicker);
    fixture.componentInstance.value.set(JULY_RANGE);
    fixture.detectChanges();
    const element = openPicker(fixture);

    click(dateButton(element, '2026-08-10'));
    fixture.detectChanges();
    click(dateButton(element, '2026-08-05'));
    fixture.detectChanges();

    // The earlier date becomes the new start rather than silently swapping the ends,
    // because the second click would otherwise apply a range never chosen deliberately.
    expect(fixture.componentInstance.value()).toEqual(JULY_RANGE);
    expect(dialog(element)).not.toBeNull();

    click(dateButton(element, '2026-08-12'));
    fixture.detectChanges();

    expect(fixture.componentInstance.value().from?.getDate()).toBe(5);
    expect(fixture.componentInstance.value().to?.getDate()).toBe(12);
    tick(AFTER_COMMIT);
  }));

  it('supports a single-day range', fakeAsync(() => {
    const fixture = TestBed.createComponent(DateRangePicker);
    fixture.componentInstance.value.set(JULY_RANGE);
    fixture.detectChanges();
    const element = openPicker(fixture);

    click(dateButton(element, '2026-07-15'));
    fixture.detectChanges();
    click(dateButton(element, '2026-07-15'));
    fixture.detectChanges();

    expect(fixture.componentInstance.value().from?.getDate()).toBe(15);
    expect(fixture.componentInstance.value().to?.getDate()).toBe(15);
    tick(AFTER_COMMIT);
  }));

  it('discards a half-finished selection when dismissed', fakeAsync(() => {
    const fixture = TestBed.createComponent(DateRangePicker);
    fixture.componentInstance.value.set(JULY_RANGE);
    fixture.detectChanges();
    const element = openPicker(fixture);

    click(dateButton(element, '2026-07-12'));
    fixture.detectChanges();
    document.dispatchEvent(new KeyboardEvent('keydown', {key: 'Escape'}));
    fixture.detectChanges();

    expect(fixture.componentInstance.value()).toEqual(JULY_RANGE);
    expect(dialog(element)).toBeNull();
  }));

  it('renders no clear, apply or cancel actions', fakeAsync(() => {
    const fixture = TestBed.createComponent(DateRangePicker);
    fixture.detectChanges();
    const element = openPicker(fixture);

    expect(labelledButton(element, 'Apply')).toBeUndefined();
    expect(labelledButton(element, 'Cancel')).toBeUndefined();
    expect(labelledButton(element, 'Clear')).toBeUndefined();
  }));

  it('does not render preset range shortcuts', fakeAsync(() => {
    const fixture = TestBed.createComponent(DateRangePicker);
    fixture.detectChanges();
    const element = openPicker(fixture);

    const text = dialog(element)?.textContent ?? '';
    expect(text).not.toContain('Today');
    expect(text).not.toContain('Last 7 days');
    expect(text).not.toContain('Last 30 days');
  }));

  it('marks the days between the endpoints as one continuous bar', fakeAsync(() => {
    const fixture = TestBed.createComponent(DateRangePicker);
    fixture.componentInstance.value.set({from: new Date(2026, 6, 10), to: new Date(2026, 6, 13)});
    fixture.detectChanges();
    const element = openPicker(fixture);

    expect(dateButton(element, '2026-07-10').classList).toContain('bar-start');
    expect(dateButton(element, '2026-07-11').classList).toContain('in-bar');
    expect(dateButton(element, '2026-07-12').classList).toContain('in-bar');
    expect(dateButton(element, '2026-07-13').classList).toContain('bar-end');
    expect(dateButton(element, '2026-07-14').classList).not.toContain('in-bar');
  }));

  it('disables dates outside min and max', fakeAsync(() => {
    const fixture = TestBed.createComponent(DateRangePicker);
    fixture.componentInstance.value.set(JULY_RANGE);
    fixture.componentRef.setInput('min', new Date(2026, 6, 10));
    fixture.componentRef.setInput('max', new Date(2026, 6, 20));
    fixture.detectChanges();
    const element = openPicker(fixture);

    expect(dateButton(element, '2026-07-09').disabled).toBeTrue();
    expect(dateButton(element, '2026-07-15').disabled).toBeFalse();
    expect(dateButton(element, '2026-07-21').disabled).toBeTrue();
  }));
});
