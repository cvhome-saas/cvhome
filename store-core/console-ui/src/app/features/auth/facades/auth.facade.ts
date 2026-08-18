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

@Injectable({providedIn: 'root'})
export class AuthFacade {
  private readonly api = inject(ConsoleAuthApi);
  private readonly transloco = inject(TranslocoService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toasts = inject(ToastService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly passwordVisible = signal(false);
  /** True only while the request is in flight, so the button can say so and cannot be pressed twice. */
  readonly submitting = signal(false);
  readonly submitted = signal(false);

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
    if (this.submitting()) {
      return;
    }
    this.submitting.set(true);

    this.api.createAccount(request).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.submitting.set(false);
        this.submitted.set(true);
        this.toasts.success(this.transloco.translate('auth.signUp.created'));
        this.router.navigateByUrl(SIGN_UP_REDIRECT_PATH);
      },
      error: (error: unknown) => {
        this.submitting.set(false);
        this.apiErrors.applyToForm(error, form);
      },
    });
  }
}
