import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {Router, provideRouter} from '@angular/router';
import {Observable, Subject, of, throwError} from 'rxjs';

import {ApiError} from '@core/errors/api-error';
import {NOTIFICATION_PORT} from '@core/errors/notification.port';
import type {CreateOrgRequest, ReadableUser} from '@models/signup';
import {translocoTesting} from '@testing/transloco-testing';
import {ConsoleAuthApi} from '../services/auth.api.service';
import {SIGN_UP_REDIRECT_PATH} from '../facades/auth.facade';
import {SignUp} from './sign-up';

const CREATED: ReadableUser = {
  id: 'u1', firstName: 'Ada', lastName: 'Lovelace', emailAddress: 'ada@example.com',
  userName: 'ada@example.com', org: 'org-1', store: null, active: true, roles: ['ORG_ADMIN'],
};

class FakeAuthApi {
  requests: CreateOrgRequest[] = [];
  pending: Subject<ReadableUser> | null = null;
  error: unknown = null;

  createAccount(request: CreateOrgRequest): Observable<ReadableUser> {
    this.requests.push(request);
    if (this.error) {
      return throwError(() => this.error);
    }
    return this.pending ?? of(CREATED);
  }
}

describe('SignUp', () => {
  let fixture: ComponentFixture<SignUp>;
  let api: FakeAuthApi;
  let router: Router;
  let toasts: {messages: string[]; danger(text: string): void};

  beforeEach(() => {
    api = new FakeAuthApi();
    toasts = {
      messages: [] as string[],
      danger(text: string) { this.messages.push(text); },
    };
    const transloco = translocoTesting();
    TestBed.configureTestingModule({
      imports: [SignUp, ...(transloco.imports as never[])],
      providers: [
        provideRouter([]),
        ...transloco.providers,
        {provide: ConsoleAuthApi, useValue: api},
        // `ApiErrorService` toasts whatever it could not bind to a control; the spec asserts on the form,
        // so the port only has to exist. Captured rather than stubbed so an unexpected toast is visible.
        {provide: NOTIFICATION_PORT, useValue: toasts},
      ],
    });
    fixture = TestBed.createComponent(SignUp);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  function set(name: string, value: string): void {
    // The control is inside `app-text-field` now, so the binding is on the component and the
    // element to type into is the input it draws.
    const input = fixture.nativeElement.querySelector(
      `app-text-field[formControlName="${name}"] input`,
    ) as HTMLInputElement;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  function fill(overrides: Record<string, string> = {}): void {
    const values: Record<string, string> = {
      firstName: 'Ada', lastName: 'Lovelace', emailAddress: 'ada@example.com',
      password: 'correct horse', repeatPassword: 'correct horse', ...overrides,
    };
    for (const [name, value] of Object.entries(values)) {
      set(name, value);
    }
    fixture.detectChanges();
  }

  function submit(): void {
    (fixture.nativeElement.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  it('posts the wire shape tenancy expects, unmapped', () => {
    fill();
    submit();

    expect(api.requests).toEqual([
      {
        user: {
          firstName: 'Ada', lastName: 'Lovelace', emailAddress: 'ada@example.com',
          password: 'correct horse', repeatPassword: 'correct horse',
        },
      },
    ]);
  });

  it('does not submit an incomplete form', () => {
    fill({lastName: ''});
    submit();

    expect(api.requests).toEqual([]);
  });

  it('blocks submit and says so when the two passwords differ', () => {
    fill({repeatPassword: 'something else'});
    submit();

    expect(api.requests).toEqual([]);
    expect(fixture.nativeElement.textContent).toContain('do not match');
  });

  it('goes to sign-in once the account exists — signup does not open a session', () => {
    const navigate = spyOn(router, 'navigateByUrl');
    fill();
    submit();

    expect(navigate).toHaveBeenCalledWith(SIGN_UP_REDIRECT_PATH);
  });

  it('cannot be submitted twice while the request is in flight', fakeAsync(() => {
    // The success path navigates; this spec is about the in-flight window, so keep the router out of it.
    spyOn(router, 'navigateByUrl');
    api.pending = new Subject<ReadableUser>();
    fill();
    submit();
    submit();

    expect(api.requests.length).toBe(1);
    expect((fixture.nativeElement.querySelector('button.auth-submit') as HTMLButtonElement).disabled).toBeTrue();

    api.pending.next(CREATED);
    api.pending.complete();
    tick();
  }));

  it('enforces a password minimum, because the server enforces none', () => {
    fill({password: 'short', repeatPassword: 'short'});
    submit();

    expect(api.requests).toEqual([]);
    expect(fixture.nativeElement.textContent).toContain('at least 8 characters');
  });

  it('refuses a password from the top of every breach list', () => {
    fill({password: 'password123', repeatPassword: 'password123'});
    submit();

    expect(api.requests).toEqual([]);
    expect(fixture.nativeElement.textContent).toContain('most common passwords');
  });

  it('refuses a password built out of the name typed above it', () => {
    fill({password: 'lovelace99', repeatPassword: 'lovelace99'});
    submit();

    expect(api.requests).toEqual([]);
    expect(fixture.nativeElement.textContent).toContain('must not contain your name');
  });

  it('refuses a password built out of the email local part', () => {
    fill({
      emailAddress: 'countess@example.com',
      password: 'countess-1815',
      repeatPassword: 'countess-1815',
    });
    submit();

    expect(api.requests).toEqual([]);
    expect(fixture.nativeElement.textContent).toContain('must not contain your name');
  });

  it('caps the email at the 50 characters manager_org.email can hold', () => {
    // 51 characters. The organization row is inserted first, so this address cannot sign up at all — and the
    // 409 it would come back with is the one `bindTakenEmail` reads as "already registered".
    const tooLong = `${'a'.repeat(39)}@example.com`;
    expect(tooLong.length).toBe(51);

    fill({emailAddress: tooLong});
    submit();

    expect(api.requests).toEqual([]);
    // The length message, not "Enter a valid email address." — the address is perfectly well formed.
    expect(fixture.nativeElement.textContent).toContain('at most 50 characters');
  });

  it('caps a name at the 50 characters uaa stores', () => {
    fill({firstName: 'A'.repeat(51)});
    submit();

    expect(api.requests).toEqual([]);
    expect(fixture.nativeElement.textContent).toContain('at most 50 characters');
  });

  it('trims the name and address, and rejects a name that was only spaces', () => {
    fill({firstName: '   '});
    submit();

    expect(api.requests).toEqual([]);
    expect(fixture.nativeElement.textContent).toContain('This field is required.');

    // The passwords keep their whitespace: a space is a character like any other in a secret.
    fill({firstName: '  Ada  ', emailAddress: '  ada@example.com  ', password: ' pass phrase ',
      repeatPassword: ' pass phrase '});
    submit();

    expect(api.requests).toEqual([
      {
        user: {
          firstName: 'Ada', lastName: 'Lovelace', emailAddress: 'ada@example.com',
          password: ' pass phrase ', repeatPassword: ' pass phrase ',
        },
      },
    ]);
  });

  it('lets the visitor fix the email a server error landed on', () => {
    spyOn(router, 'navigateByUrl');
    api.error = new ApiError({
      code: 'COMMON.DATA_INTEGRITY_VIOLATION',
      category: 'CONFLICT',
      status: 409,
    });
    fill();
    submit();

    const email = fixture.nativeElement.querySelector('[formControlName="emailAddress"]')
      .closest('label') as HTMLElement;
    expect(email.textContent).toContain('already uses this email');

    // A server error is not a validator: without `clearServerErrorsOnChange` nothing removes it, and the
    // visitor types a new address only to watch the message stay and the form refuse to submit again.
    api.error = null;
    set('emailAddress', 'ada2@example.com');
    fixture.detectChanges();
    expect(email.textContent).not.toContain('already uses this email');

    submit();
    expect(api.requests.length).toBe(2);
    expect(api.requests[1].user.emailAddress).toBe('ada2@example.com');
  });

  it('is submittable again for a second account in the same browser session', () => {
    spyOn(router, 'navigateByUrl');
    fill();
    submit();
    expect(api.requests.length).toBe(1);

    // `AuthFacade` is a root singleton and `submitted` latches, so a second visit to the page would otherwise
    // find the button permanently disabled.
    const second = TestBed.createComponent(SignUp);
    second.detectChanges();
    const button = second.nativeElement.querySelector('button.auth-submit') as HTMLButtonElement;
    expect(button.disabled).toBeFalse();
  });

  it('blames the email when tenancy answers a bare conflict', () => {
    // What the running stack actually returns for an address that already exists: a generic
    // COMMON.DATA_INTEGRITY_VIOLATION with no fieldErrors at all.
    api.error = new ApiError({
      code: 'COMMON.DATA_INTEGRITY_VIOLATION',
      category: 'CONFLICT',
      status: 409,
    });
    fill();
    submit();

    const email = fixture.nativeElement.querySelector('[formControlName="emailAddress"]')
      .closest('label') as HTMLElement;
    expect(email.textContent).toContain('already uses this email');
    // Not the generic "This changed somewhere else. Refresh and try again." toast.
    expect(toasts.messages).toEqual([]);
  });

  it('lands a server field error on the control that caused it', () => {
    // What uaa answers when the email is already registered.
    api.error = new ApiError({
      code: 'UAA.USER.EMAIL_TAKEN',
      category: 'CONFLICT',
      status: 409,
      fieldErrors: [{field: 'user.emailAddress', message: 'That email is already registered.', code: 'UAA.USER.EMAIL_TAKEN'}],
    });
    fill();
    submit();

    const email = fixture.nativeElement.querySelector('[formControlName="emailAddress"]')
      .closest('label') as HTMLElement;
    expect(email.textContent).toContain('already registered');
    // Bound to the field, not shouted as a toast — the whole point of `applyToForm`.
    expect(toasts.messages).toEqual([]);
  });
});
