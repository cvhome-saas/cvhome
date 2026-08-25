import {Component, input} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {AuthStory as AuthStoryModel} from '@models/auth';
import {Icon} from '@shared/ui/icon/icon';

@Component({
  selector: 'app-auth-story',
  imports: [Icon, RouterLink, TranslocoDirective],
  template: `
    <section class="auth-story" aria-labelledby="auth-heading" *transloco="let t">
      <div class="auth-glow" aria-hidden="true"></div>
      <a class="auth-brand" routerLink="/" [attr.aria-label]="t('auth.backToHome')">
        <app-icon name="arrowLeft" [flip]="true" />
        {{ brandName }}
      </a>

      <div class="auth-message">
        <p class="eyebrow">{{ t('auth.freeTrialEyebrow') }}</p>
        <h1 id="auth-heading">{{ story().heading }}</h1>
        <p>{{ story().copy }}</p>
        <ul>
          @for (point of story().points; track point) {
            <li><app-icon name="check" />{{ point }}</li>
          }
        </ul>
      </div>

      <p class="trust">{{ t('auth.trustNote') }}</p>
    </section>
  `,
  styleUrl: './auth-story.css',
})
export class AuthStory {
  readonly story = input.required<AuthStoryModel>();

  // Brand name, not translated — same as Stripe/PayPal elsewhere in the app.
  protected readonly brandName = 'cvhome';
}
