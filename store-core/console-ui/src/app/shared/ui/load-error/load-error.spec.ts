import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';

import {translocoTesting} from '@testing/transloco-testing';
import {LoadError} from './load-error';

@Component({
  imports: [LoadError],
  template: `<app-load-error [message]="message()" [label]="label()" (retry)="retries.set(retries() + 1)" />`,
})
class Host {
  readonly message = signal('Orders could not be loaded.');
  readonly label = signal<string | null>(null);
  readonly retries = signal(0);
}

describe('LoadError', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;
  let element: HTMLElement;

  const retryButton = () => element.querySelector('button') as HTMLButtonElement;

  beforeEach(async () => {
    const transloco = translocoTesting();
    await TestBed.configureTestingModule({
      imports: [Host, ...(transloco.imports as never[])],
      providers: transloco.providers,
    }).compileComponents();
    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
    fixture.detectChanges();
    element = fixture.nativeElement as HTMLElement;
  });

  it('says what failed, and interrupts to do it', () => {
    // `alert`, not `status`: this is something that has just gone wrong, not a description of what
    // is on screen. All nine of the hand-written blocks it replaced announced assertively.
    const notice = element.querySelector('app-notice-bar') as HTMLElement;
    expect(notice.getAttribute('role')).toBe('alert');
    expect(notice.textContent).toContain('Orders could not be loaded.');
  });

  it('takes its retry wording from the shared vocabulary, not the dashboard namespace', () => {
    // Nine features borrowed `dashboard.tryAgain`; the key is now `shared.actions.retry`.
    expect(retryButton().textContent?.trim()).toBe('Try again');
  });

  it('lets a caller name the retry when it means something particular', () => {
    host.label.set('Check again');
    fixture.detectChanges();
    expect(retryButton().textContent?.trim()).toBe('Check again');
  });

  it('asks the page to reload rather than reloading anything itself', () => {
    retryButton().click();
    retryButton().click();
    expect(host.retries()).toBe(2);
  });
});
