import {computed, Injectable, inject, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {AuthStory, SignUpRequest} from '@models/auth';
import {ConsoleAuthApi} from '../services/auth.api.service';

@Injectable({providedIn: 'root'})
export class AuthFacade {
  private readonly api = inject(ConsoleAuthApi);
  private readonly transloco = inject(TranslocoService);

  readonly passwordVisible = signal(false);
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

  createAccount(request: SignUpRequest): void {
    this.api.createAccount(request).subscribe(() => this.submitted.set(true));
  }
}
