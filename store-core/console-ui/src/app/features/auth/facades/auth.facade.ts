import {computed, DestroyRef, Injectable, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {AbstractControl} from '@angular/forms';
import {Router} from '@angular/router';
import {TranslocoService} from '@jsverse/transloco';

import {ApiErrorService} from '@core/errors/api-error.service';
import {SERVER_ERROR_KEY} from '@core/errors/form-error.utils';
import {toApiError} from '@core/errors/problem-detail.parser';
import {AuthStory} from '@models/auth';
import type {CreateOrgRequest} from '@models/signup';
import {ToastService} from '@shared/ui/toast/toast';
import {ConsoleAuthApi} from '../services/auth.api.service';

/**
 * Where a new account lands.
 *
 * Sign-in, not the console. Signup creates the organization and its administrator but does **not** open a session —
 * uaa has never seen this browser. seller-ui sends people to `/pages` and lets the auth guard bounce them to uaa a
 * render later; going straight to the handoff says the same thing without the detour, and does not depend on a
 * console guard that this module has not wired yet.
 */
export const SIGN_UP_REDIRECT_PATH = '/sign-in';

/**
 * Root-provided on purpose: sign-in and sign-up are separate routes sharing this one facade, and it
 * holds no resource — nothing fetches on injection, so the page-provided rule's failure mode
 * (an eager load paid by whoever injects) cannot happen here.
 */
@Injectable({providedIn: 'root'})
export class AuthFacade {
  private readonly api = inject(ConsoleAuthApi);
  private readonly transloco = inject(TranslocoService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  /** True only while the request is in flight, so the button can say so and cannot be pressed twice. */
  readonly busy = signal(false);
  /**
   * Latched on success, so the button stays disabled across the tick between the account existing and the
   * router leaving the page. `SignUp` clears it on the way in — see `resetSubmission`.
   */
  readonly submitted = signal(false);

  /**
   * Forgets the last signup, so the page is submittable again.
   *
   * This facade is root-provided and `submitted` never falls back to false on its own, so without this a
   * visitor who creates an account, arrives at sign-in and then follows "Create account" — to sign a colleague
   * up, or because they mistyped the address — meets a permanently disabled button with no message.
   *
   * `busy` is deliberately not touched: it is cleared on both the success and the failure path, so it cannot
   * latch, and resetting it would re-enable the button underneath a request that is still in flight.
   */
  resetSubmission(): void {
    this.submitted.set(false);
  }

  /**
   * `computed()` rather than a static object: `AuthStory` renders once per page load, but
   * it must still follow a language switch like everything else, and a plain field would
   * freeze at whatever language was active when the facade (a root singleton) was built.
   */
  readonly signInStory = computed<AuthStory>(() => {
    this.transloco.activeLang();
    return {
      heading: this.transloco.translate('auth.signIn.heading'),
      copy: this.transloco.translate('auth.signIn.copy'),
      points: this.transloco.translate<string[]>([
        'auth.signIn.point1',
        'auth.signIn.point2',
        'auth.signIn.point3',
      ]),
    };
  });

  readonly signUpStory = computed<AuthStory>(() => {
    this.transloco.activeLang();
    return {
      heading: this.transloco.translate('auth.signUp.heading'),
      copy: this.transloco.translate('auth.signUp.copy'),
      points: this.transloco.translate<string[]>([
        'auth.signUp.point1',
        'auth.signUp.point2',
        'auth.signUp.point3',
      ]),
    };
  });

  /**
   * Creates the account, then sends the browser to sign in.
   *
   * The form is passed in so a server `fieldErrors[]` can be bound to the control that caused it — uaa answers a
   * duplicate email with a field error, and a toast saying "conflict" would leave the seller guessing which field.
   */
  createAccount(request: CreateOrgRequest, form: AbstractControl): void {
    if (this.busy()) {
      return;
    }
    this.busy.set(true);

    this.api.createAccount(request).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.busy.set(false);
        this.submitted.set(true);
        this.toast.success(this.transloco.translate('auth.signUp.created'));
        this.router.navigateByUrl(SIGN_UP_REDIRECT_PATH);
      },
      error: (error: unknown) => {
        this.busy.set(false);
        if (!this.bindTakenEmail(error, form)) {
          this.apiErrors.applyToForm(error, form);
        }
      },
    });
  }

  /**
   * Turns the one conflict this endpoint can actually produce into a message on the field that caused it.
   *
   * Signing up with an address that already exists answers `409 COMMON.DATA_INTEGRITY_VIOLATION` with **no**
   * `fieldErrors[]` — verified against the running stack. `applyToForm` would therefore fall through to a
   * toast reading "This changed somewhere else. Refresh and try again.", which is the right message for the
   * generic code and the wrong one for this form. seller-ui has the same problem: its facade names
   * `CUA.REGISTRATION.EMAIL_TAKEN` in a comment, but that code is never sent.
   *
   * Deliberately narrow — a 409 that *does* carry field errors, and every other status, goes the normal way.
   * The proper fix is a specific code and a field error from tenancy; see lessons.md, "Auth — a taken email
   * is indistinguishable from any other conflict".
   */
  private bindTakenEmail(error: unknown, form: AbstractControl): boolean {
    const apiError = toApiError(error);
    if (apiError.status !== 409 || apiError.fieldErrors.length > 0) {
      return false;
    }

    const email = form.get('user.emailAddress');
    if (!email) {
      return false;
    }

    email.setErrors({
      ...(email.errors ?? {}),
      [SERVER_ERROR_KEY]: {
        field: 'user.emailAddress',
        code: apiError.code,
        message: this.transloco.translate('auth.signUp.emailTaken'),
      },
    });
    email.markAsTouched();
    return true;
  }
}