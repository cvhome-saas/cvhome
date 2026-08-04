import {Injectable, PLATFORM_ID, inject} from '@angular/core';
import {Router} from '@angular/router';
import {isPlatformBrowser} from '@angular/common';
import {SignUpService} from '../../../service/sign-up.service';
import {SignUpForm} from '../../../domain/types';
import {ApiError} from '../../../../core/errors/api-error';
import {ApiErrorService} from '../../../../core/errors/api-error.service';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {SignUpFormService} from '../services/sign-up-form.service';
import {SIGN_UP_REDIRECT_DELAY_MS, SIGN_UP_REDIRECT_PATH} from '../constants/sign-up-form.constants';

@Injectable()
export class SignUpFormFacade {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly router = inject(Router);
  private readonly signUpService = inject(SignUpService);
  private readonly notifications = inject(NotificationService);
  private readonly apiErrors = inject(ApiErrorService);
  readonly formService = inject(SignUpFormService);

  signUp(): void {
    if (this.formService.form.invalid) return;

    this.signUpService.signUp(this.formService.form.value as unknown as SignUpForm).subscribe({
      next: () => {
        this.notifications.success('SIGN_UP.SUCCESS');
        this.redirectAfterDelay();
      },
      // Was a hardcoded English "Failed to Register! / Please fill all required fields", which guessed at
      // the cause. The backend says exactly what went wrong — CUA.REGISTRATION.EMAIL_TAKEN or
      // USERNAME_TAKEN — and applyToForm puts it on the control that caused it.
      error: (error: ApiError) => this.apiErrors.applyToForm(error, this.formService.form)
    });
  }

  private redirectAfterDelay(): void {
    setTimeout(() => {
      if (isPlatformBrowser(this.platformId)) {
        this.router.navigate([SIGN_UP_REDIRECT_PATH]);
      }
    }, SIGN_UP_REDIRECT_DELAY_MS);
  }
}
