import {Component, inject} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';
import {map} from 'rxjs';

import {SubscriptionFacade} from '@layouts/console-shell/billing/subscription.facade';
import {Icon} from '@shared/ui/icon/icon';

/**
 * Where the hosted checkout sends the browser back to.
 *
 * One component for both outcomes: they are the same page with a different verdict, and billing decides which by
 * choosing the return URL. Nothing is fetched — the subscription's real state is read by the console, not by a
 * landing page the payment provider redirected to, which can be reached by anyone typing the URL.
 *
 * It does **invalidate** what the console already holds, though, which is a different thing from trusting the URL:
 * `SubscriptionFacade` caches the subscription for the open store, and after a checkout that cache is stale by
 * definition. Dropping it means the billing page and the plan banner re-read from the server on the next render
 * rather than showing the plan the operator had a minute ago. The URL decides nothing; it only says "go and look
 * again".
 */
@Component({
  selector: 'app-subscription-outcome',
  imports: [Icon, RouterLink, TranslocoDirective],
  template: `
    <main class="outcome" [class.failed]="!succeeded()" *transloco="let t">
      <app-icon [name]="succeeded() ? 'checkCircle' : 'alertCircle'" />
      <h1>{{ t(succeeded() ? 'subscription.success.title' : 'subscription.fail.title') }}</h1>
      <p>{{ t(succeeded() ? 'subscription.success.copy' : 'subscription.fail.copy') }}</p>
      <!--
        Billing, not the dashboard: whatever just happened, the page that shows the result of it is
        the one the operator wants next — the new plan, or the plan they still do not have.
      -->
      <a class="action" routerLink="/subscription">{{ t('subscription.toBilling') }}</a>
      <a class="secondary" routerLink="/dashboard">{{ t('subscription.toConsole') }}</a>
    </main>
  `,
  styleUrl: './subscription-outcome.css',
})
export class SubscriptionOutcome {
  private readonly route = inject(ActivatedRoute);
  private readonly billing = inject(SubscriptionFacade);

  constructor() {
    this.billing.refresh();
  }

  protected readonly succeeded = toSignal(
    this.route.data.pipe(map((data) => data['succeeded'] === true)),
    {initialValue: false},
  );
}
