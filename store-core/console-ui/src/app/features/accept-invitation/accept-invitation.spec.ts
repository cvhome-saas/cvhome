import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Observable, of, throwError} from 'rxjs';

import {OrgMemberService} from '@api/tenancy/org-member.service';
import {NOTIFICATION_PORT} from '@core/errors/notification.port';
import type {Invitation} from '@models/users';
import {translocoTesting} from '@testing/transloco-testing';
import {AcceptInvitation} from './accept-invitation';

const ACCEPTED: Invitation = {
  id: 'inv-1',
  orgId: '21f023932bc66470c104b76f',
  email: 'newbie@example.com',
  role: 'STORE_ADMIN',
  status: 'ACCEPTED',
  expiresAt: '2026-09-01T00:00:00Z',
  createdAt: '2026-08-22T00:00:00Z',
  createdBy: 'org1-admin',
};

class FakeOrgMemberService {
  readonly accepted: string[] = [];
  failure: Error | null = null;

  accept(token: string): Observable<Invitation> {
    this.accepted.push(token);
    return this.failure ? throwError(() => this.failure) : of(ACCEPTED);
  }
}

describe('AcceptInvitation', () => {
  let members: FakeOrgMemberService;
  let fixture: ComponentFixture<AcceptInvitation>;

  beforeEach(async () => {
    members = new FakeOrgMemberService();
    await TestBed.configureTestingModule({
      imports: [AcceptInvitation, ...translocoTesting().imports],
      providers: [
        provideRouter([]),
        {provide: OrgMemberService, useValue: members},
        {provide: NOTIFICATION_PORT, useValue: {danger: () => undefined}},
        ...translocoTesting().providers,
      ],
    }).compileComponents();
  });

  function load(token?: string): HTMLElement {
    fixture = TestBed.createComponent(AcceptInvitation);
    fixture.componentRef.setInput('token', token);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  function press(host: HTMLElement, label: RegExp): void {
    const button = Array.from(host.querySelectorAll('button')).find((candidate) =>
      label.test(candidate.textContent ?? ''),
    ) as HTMLButtonElement;
    button.click();
    tick();
    fixture.detectChanges();
  }

  /*
   * The whole reason this is a button and not an effect. The token is single-use and is burned on
   * the first success, so accepting on load would turn a refresh, a prefetch or a link preview into
   * "this invitation has already been used".
   */
  it('sends nothing until the invitee presses the button', fakeAsync(() => {
    const host = load('a-token');

    expect(members.accepted).toEqual([]);

    press(host, /Accept invitation/);

    expect(members.accepted).toEqual(['a-token']);
  }));

  it('confirms the join and offers the console', fakeAsync(() => {
    const host = load('a-token');
    press(host, /Accept invitation/);

    expect(host.textContent).toContain('You are in');
    expect(host.textContent).toContain('Open the console');
  }));

  /* Pressing twice would burn a second token that does not exist. */
  it('does not accept the same invitation twice', fakeAsync(() => {
    const host = load('a-token');
    press(host, /Accept invitation/);
    fixture.componentInstance['facade'].accept('a-token');
    tick();

    expect(members.accepted).toEqual(['a-token']);
  }));

  /*
   * Rendered on the page rather than raised as a toast: a toast dismisses itself, and this is the
   * entire content of the screen for someone who has just been told their link does not work.
   */
  it('explains a refused invitation on the page, and says what to do next', fakeAsync(() => {
    members.failure = new Error('this invitation has already been used');
    const host = load('a-token');
    press(host, /Accept invitation/);

    expect(host.textContent).toContain('cannot be used');
    // The server's own message, resolved through ApiErrorService, stays on the page rather than
    // being raised as a toast that would dismiss itself.
    expect(host.querySelector('app-load-error')).not.toBeNull();
    expect(host.textContent).toContain('Ask whoever invited you');
  }));

  /* A retry is offered because a network failure is survivable; a burned token simply refuses again. */
  it('offers a retry that sends the same token again', fakeAsync(() => {
    members.failure = new Error('the network is down');
    const host = load('a-token');
    press(host, /Accept invitation/);

    members.failure = null;
    press(host, /Try again/);

    expect(members.accepted).toEqual(['a-token', 'a-token']);
    expect(host.textContent).toContain('You are in');
  }));

  it('says so when the link carries no token at all, without calling the server', fakeAsync(() => {
    const host = load(undefined);

    expect(members.accepted).toEqual([]);
    expect(host.textContent).toContain('missing its invitation token');
  }));
});
