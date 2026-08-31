import {computed, DestroyRef, Injectable, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {AbstractControl} from '@angular/forms';
import {Router} from '@angular/router';
import {TranslocoService} from '@jsverse/transloco';

import {ApiErrorService} from '@core/errors/api-error.service';
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
   * The form is passed in so a server `fieldErrors[]` can be bound to the control that caused it — a duplicate
   * address is the most likely way this call fails, and it comes back naming `user.emailAddress`.
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
        /*
         * No special case for the taken address any more. `bindTakenEmail` used to read *any* fieldless 409 on
         * this call as "already registered", because tenancy answered a duplicate with the generic
         * `COMMON.DATA_INTEGRITY_VIOLATION` — which an over-long address also produced, so the guess was
         * sometimes a lie that sent someone to sign in to an account that did not exist. Signup now answers
         * `CONTROL_PLANE.SIGNUP.EMAIL_TAKEN` with a `user.emailAddress` field error, and every validation
         * failure names its field too, so the ordinary path binds all of them.
         */
        this.apiErrors.applyToForm(error, form);
      },
    });
  }
}