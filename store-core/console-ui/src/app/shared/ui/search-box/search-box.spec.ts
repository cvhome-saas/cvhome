import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';

import {SearchBox} from './search-box';

@Component({
  imports: [SearchBox],
  template: `<app-search-box [(value)]="term" label="Search orders" placeholder="Name or email" />`,
})
class Host {
  readonly term = signal('');
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

  it('publishes the term as it is typed', () => {
    input().value = 'acme';
    input().dispatchEvent(new Event('input'));
    expect(host.term()).toBe('acme');
  });

  it('takes a term set from outside — a filter restored from the URL', () => {
    host.term.set('restored');
    fixture.detectChanges();
    expect(input().value).toBe('restored');
  });

  it('keeps a latin term readable inside a right-to-left page', () => {
    expect(getComputedStyle(input()).unicodeBidi).toBe('plaintext');
  });
});
