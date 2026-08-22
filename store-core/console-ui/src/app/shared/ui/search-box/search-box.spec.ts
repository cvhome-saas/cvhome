import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';

import {SearchBox} from './search-box';

@Component({
  imports: [SearchBox],
  template: `
    <app-search-box
      [(value)]="term"
      [debounceMs]="immediate() ? 0 : 300"
      label="Search orders"
      placeholder="Name or email"
    />
  `,
})
class Host {
  readonly term = signal('');
  readonly immediate = signal(false);
}

describe('SearchBox', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;
  let element: HTMLElement;

  const input = () => element.querySelector('input') as HTMLInputElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({imports: [Host]}).compileComponents();
    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
    fixture.detectChanges();
    element = fixture.nativeElement as HTMLElement;
  });

  it('is labelled for a reader, not only placeholdered', () => {
    const label = element.querySelector('label') as HTMLLabelElement;
    expect(label.querySelector('.sr-only')?.textContent?.trim()).toBe('Search orders');
    expect(label.contains(input())).toBeTrue();
    expect(input().placeholder).toBe('Name or email');
  });

  it('keeps the browser’s own escape-to-clear', () => {
    expect(input().type).toBe('search');
  });

  /*
   * The two boxes this replaced listened to `change`, so a filter only applied on blur or Enter —
   * you could type a SKU, look at an unchanged table and conclude the filter was broken. Live, but
   * settled first, because both of these reach the server.
   */
  it('publishes the term once the typing stops', fakeAsync(() => {
    input().value = 'acm';
    input().dispatchEvent(new Event('input'));
    tick(150);
    expect(host.term()).withContext('mid-word, nothing sent yet').toBe('');

    input().value = 'acme';
    input().dispatchEvent(new Event('input'));
    tick(150);
    expect(host.term()).withContext('the first keystroke must not fire on its own timer').toBe('');

    tick(300);
    expect(host.term()).toBe('acme');
  }));

  it('publishes immediately when the caller filters in memory', fakeAsync(() => {
    host.immediate.set(true);
    fixture.detectChanges();

    input().value = 'acme';
    input().dispatchEvent(new Event('input'));
    expect(host.term()).toBe('acme');
    tick(300);
  }));

  it('takes a term set from outside — a filter restored from the URL', () => {
    host.term.set('restored');
    fixture.detectChanges();
    expect(input().value).toBe('restored');
  });

  it('keeps a latin term readable inside a right-to-left page', () => {
    expect(getComputedStyle(input()).unicodeBidi).toBe('plaintext');
  });
});
