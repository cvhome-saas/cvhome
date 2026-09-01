import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';

import {EmptyState} from './empty-state';

@Component({
  imports: [EmptyState],
  template: `
    <app-empty-state [message]="message()" [title]="title()" icon="search">
      @if (withAction()) {
        <button type="button" class="secondary-action">Clear filters</button>
      }
    </app-empty-state>
  `,
})
class Host {
  readonly message = signal('No orders match those filters.');
  readonly title = signal<string | null>(null);
  readonly withAction = signal(false);
}

describe('EmptyState', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({imports: [Host]}).compileComponents();
    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
    fixture.detectChanges();
    element = fixture.nativeElement as HTMLElement;
  });

  it('announces itself, because it replaces content that was there a moment ago', () => {
    const region = element.querySelector('app-empty-state') as HTMLElement;
    expect(region.getAttribute('role')).toBe('status');
  });

  it('states the reason, and shows a heading only when given one', () => {
    expect(element.querySelector('.empty-copy')?.textContent?.trim())
      .toBe('No orders match those filters.');
    expect(element.querySelector('.empty-title')).toBeNull();

    host.title.set('Nothing here yet');
    fixture.detectChanges();
    expect(element.querySelector('.empty-title')?.textContent?.trim()).toBe('Nothing here yet');
  });

  it('offers a way out only when the caller projects one', () => {
    // "Nothing yet" is a state to describe; "nothing matched" is a filter to clear. The difference
    // is whether there is an action, not which component is used.
    expect(element.querySelector('button')).toBeNull();

    host.withAction.set(true);
    fixture.detectChanges();
    expect(element.querySelector('button')?.textContent?.trim()).toBe('Clear filters');
  });
});
