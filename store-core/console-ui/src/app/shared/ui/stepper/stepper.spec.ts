import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';

import {translocoTesting} from '@testing/transloco-testing';
import {Stepper, type StepItem} from './stepper';

const STEPS: readonly StepItem[] = [
  {key: 'essentials', label: 'Essentials', meta: 'SKU and name', complete: true},
  {key: 'media', label: 'Media', meta: 'Save the product first', disabled: true},
  {key: 'pricing', label: 'Pricing', complete: false},
  {key: 'organize', label: 'Organize'},
];

@Component({
  imports: [Stepper],
  template: `
    <app-stepper
      [steps]="steps()"
      [active]="active()"
      label="Product setup steps"
      completeLabel="Complete"
      (stepSelected)="chosen.push($event)"
    />
  `,
})
class Host {
  readonly steps = signal<readonly StepItem[]>(STEPS);
  readonly active = signal('essentials');
  readonly chosen: string[] = [];
}

describe('Stepper', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Host, ...translocoTesting().imports],
      providers: [...translocoTesting().providers],
    }).compileComponents();

    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
    fixture.detectChanges();
    element = fixture.nativeElement as HTMLElement;
  });

  function steps(): HTMLButtonElement[] {
    return [...element.querySelectorAll<HTMLButtonElement>('.step')];
  }

  it('is a list of steps, not a tablist', () => {
    /*
     * The distinction is the reason this component exists. A tablist says "alternative views of one
     * thing, pick one"; a stepper says "stages of one task, and some are not available yet" — which
     * a tab cannot express at all.
     */
    expect(element.querySelector('[role="tablist"]')).toBeNull();
    expect(element.querySelector('ol')).not.toBeNull();
  });

  it('marks exactly one step as current', () => {
    const current = steps().filter((step) => step.getAttribute('aria-current') === 'step');

    expect(current.length).toBe(1);
    expect(current[0].textContent).toContain('Essentials');
  });

  it('keeps the number visible on a completed step', () => {
    /*
     * An earlier draft swapped the number for a tick, so a completed step announced no position at
     * all — "Media" instead of "step 2, Media". Completion is a separate mark on the label.
     */
    expect(steps()[0].querySelector('.marker')?.textContent?.trim()).toBe('1');
    expect(steps()[0].querySelector('.done')).not.toBeNull();
  });

  it('labels the completion mark, because a tick and a colour are not a label', () => {
    const done = steps()[0].querySelector('.done svg');

    expect(done?.getAttribute('aria-label')).toBe('Complete');
  });

  it('shows no completion mark on a step that is not done', () => {
    expect(steps()[2].querySelector('.done')).toBeNull();
    expect(steps()[3].querySelector('.done')).toBeNull();
  });

  it('refuses a step that is not reachable yet, and says why', () => {
    expect(steps()[1].disabled).toBe(true);
    expect(steps()[1].textContent).toContain('Save the product first');

    steps()[1].click();
    expect(host.chosen).toEqual([]);
  });

  it('lets a reachable step be opened out of order', () => {
    // The rail is navigation, not a one-way conveyor: fixing the SKU from step 4 should not mean
    // walking back through the steps between.
    steps()[3].click();
    steps()[0].click();

    expect(host.chosen).toEqual(['organize', 'essentials']);
  });

  it('follows the active step when it changes from outside', () => {
    host.active.set('pricing');
    fixture.detectChanges();

    const current = steps().filter((step) => step.getAttribute('aria-current') === 'step');
    expect(current[0].textContent).toContain('Pricing');
  });
});
