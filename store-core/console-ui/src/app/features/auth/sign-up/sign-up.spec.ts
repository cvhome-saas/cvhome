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
    const input = fixture.nativeElement.querySelector(`[formControlName="${name}"]`) as HTMLInputElement;
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
