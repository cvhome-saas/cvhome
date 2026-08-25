import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';

import {Checkbox} from './checkbox';

@Component({
  imports: [Checkbox],
  template: `<app-checkbox [label]="label()" [(checked)]="on" [depth]="depth()" [disabled]="off()" />`,
})
class Host {
  readonly label = signal('Arabic');
  readonly on = signal(false);
  readonly depth = signal(0);
  readonly off = signal(false);
}

describe('Checkbox', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;
  let element: HTMLElement;

  const box = () => element.querySelector('input[type="checkbox"]') as HTMLInputElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({imports: [Host]}).compileComponents();
    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
    fixture.detectChanges();
    element = fixture.nativeElement as HTMLElement;
  });

  it('is a checkbox, not a switch — these are selections from a set', () => {
    expect(box().type).toBe('checkbox');
    expect(element.querySelector('[role="switch"]')).toBeNull();
  });

  it('is named by its label through the wrapping element', () => {
    const label = element.querySelector('label') as HTMLLabelElement;
    expect(label.contains(box())).toBeTrue();
    expect(label.textContent).toContain('Arabic');
  });

  it('round-trips the selection', () => {
    expect(box().checked).toBeFalse();
    box().click();
    expect(host.on()).toBeTrue();
  });

  /*
   * A `[checked]` binding only writes when the expression differs from what it last wrote, and a
   * click changes the DOM behind its back — so a model reset to the value it held before the click
   * wrote nothing, and the box stayed visibly ticked against a model that said otherwise. That is
   * what a rejected save looks like from the operator's side.
   */
  it('follows the model back when a change is undone', () => {
    box().click();
    fixture.detectChanges();

    host.on.set(false);
    fixture.detectChanges();

    expect(box().checked).toBeFalse();
  });

  /*
   * The reason this is drawn rather than tinted. `accent-color` colours the checked state and
   * leaves the unchecked one to the platform, which against either dark theme is a solid dark
   * square — indistinguishable from selected. lessons.md records it from the catalogue.
   */
  it('draws both states, so unchecked cannot read as checked', () => {
    const drawn = element.querySelector('.box') as HTMLElement;
    const unchecked = getComputedStyle(drawn).backgroundColor;

    box().click();
    fixture.detectChanges();

    expect(getComputedStyle(drawn).backgroundColor).not.toBe(unchecked);
  });

  it('keeps the real control reachable rather than hiding it from the platform', () => {
    // `display: none` would take it out of the tab order and the accessibility tree.
    expect(getComputedStyle(box()).display).not.toBe('none');
  });

  it('indents for a set that is a hierarchy', () => {
    const row = element.querySelector('.checkbox') as HTMLElement;
    const flat = getComputedStyle(row).paddingInlineStart;

    host.depth.set(3);
    fixture.detectChanges();

    expect(getComputedStyle(row).paddingInlineStart).not.toBe(flat);
  });

  it('refuses the click when disabled', () => {
    host.off.set(true);
    fixture.detectChanges();
    expect(box().disabled).toBeTrue();
  });
});
