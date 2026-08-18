import {Component, inject} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';
import {map} from 'rxjs';

import {Icon} from '@shared/ui/icon/icon';

/**
 * Where the hosted checkout sends the browser back to.
 *
 * One component for both outcomes: they are the same page with a different verdict, and billing decides which by
 * choosing the return URL. Nothing is fetched — the subscription's real state is read by the console, not by a
 * landing page the payment provider redirected to, which can be reached by anyone typing the URL.
 */
@Component({
  selector: 'app-subscription-outcome',
  imports: [Icon, RouterLink, TranslocoDirective],
  template: `
    <main class="outcome" [class.failed]="!succeeded()" *transloco="let t">
      <app-icon [name]="succeeded() ? 'checkCircle' : 'alertCircle'" />
      <h1>{{ t(succeeded() ? 'subscription.success.title' : 'subscription.fail.title') }}</h1>
      <p>{{ t(succeeded() ? 'subscription.success.copy' : 'subscription.fail.copy') }}</p>
      <a class="action" routerLink="/dashboard">{{ t('subscription.toConsole') }}</a>
      @if (!succeeded()) {
        <a class="secondary" href="/#pricing">{{ t('subscription.backToPricing') }}</a>
      }
    </main>
  `,
  styleUrl: './subscription-outcome.css',
})
export class SubscriptionOutcome {
  private readonly route = inject(ActivatedRoute);

  protected readonly succeeded = toSignal(
    this.route.data.pipe(map((data) => data['succeeded'] === true)),
    {initialValue: false},
  );
}
