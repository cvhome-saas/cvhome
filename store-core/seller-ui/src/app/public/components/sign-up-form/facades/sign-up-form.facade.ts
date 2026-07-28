import {Injectable, PLATFORM_ID, inject} from '@angular/core';
import {Router} from '@angular/router';
import {isPlatformBrowser} from '@angular/common';
import {SignUpService} from '../../../service/sign-up.service';
import {SignUpForm} from '../../../domain/types';
import {PublicNotificationService} from '../../../shared/services/public-notification.service';
import {SignUpFormService} from '../services/sign-up-form.service';
import {SIGN_UP_REDIRECT_DELAY_MS, SIGN_UP_REDIRECT_PATH} from '../constants/sign-up-form.constants';

@Injectable()
export class SignUpFormFacade {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly router = inject(Router);
  private readonly signUpService = inject(SignUpService);
  private readonly notificationService = inject(PublicNotificationService);
  readonly formService = inject(SignUpFormService);

  signUp(): void {
    if (this.formService.form.invalid) return;

    this.signUpService.signUp(this.formService.form.value as unknown as SignUpForm).subscribe({
      next: () => {
        this.notificationService.success('Successfully Register!', 'You will be redirected to login page');
        this.redirectAfterDelay();
      },
      error: () => {
        this.notificationService.error('Failed to Register!', 'Please fill all required fields');
      }
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
