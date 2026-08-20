import {TestBed, fakeAsync, tick} from '@angular/core/testing';
import {NEVER} from 'rxjs';
import {provideRouter} from '@angular/router';

import {SubscriptionService} from '@api/billing/subscription.service';
import {ConsoleApi} from '@layouts/console-shell/services/console.api.service';
import {NOTIFICATION_PORT} from '@core/errors/notification.port';
import {
  FakeSubscriptionService,
  activeSubscription,
  trialingSubscription,
} from '@testing/billing.fake';
import {CONSOLE_STORES_FAKE, FakeConsoleApi} from '@testing/console-api.fake';

/** The store the console opens by default in these specs — every call below is scoped to it. */
const STORE = CONSOLE_STORES_FAKE[0].id;
import {translocoTesting} from '@testing/transloco-testing';
import {Billing} from './billing';

describe('Billing', () => {
  let billing: FakeSubscriptionService;

  beforeEach(async () => {
    localStorage.removeItem('cvhome.console.store');
    billing = new FakeSubscriptionService();
    await TestBed.configureTestingModule({
      imports: [Billing, ...translocoTesting().imports],
      providers: [
        provideRouter([]),
        {provide: SubscriptionService, useValue: billing},
        {provide: ConsoleApi, useValue: Object.assign(new FakeConsoleApi(), {stores: CONSOLE_STORES_FAKE})},
        {provide: NOTIFICATION_PORT, useValue: {danger: () => undefined, success: () => undefined}},
        ...translocoTesting().providers,
      ],
    }).compileComponents();
  });

  function page() {
    const fixture = TestBed.createComponent(Billing);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    return {fixture, element: fixture.nativeElement as HTMLElement};
  }

  it('shows the plan billing reports, with its allowances', fakeAsync(() => {
    billing.subscription = {
      ...activeSubscription(),
      entitlements: {
        MAX_PRODUCTS: {key: 'MAX_PRODUCTS', limitValue: 500, flagValue: null},
        ANALYTICS: {key: 'ANALYTICS', limitValue: null, flagValue: false},
      },
    };
    const {element} = page();

    expect(element.querySelector('.plan-name')?.textContent).toContain('Pro');
    const rows = [...element.querySelectorAll('.allowance-row')];
    expect(rows.length).toBeGreaterThan(0);
    // A ceiling shows its number; a withheld capability is drawn as withheld, not omitted.
    expect(element.textContent).toContain('500');
    expect(rows.some((row) => row.classList.contains('withheld'))).toBeTrue();
  }));

  it('does not claim unlimited allowances for a subscription with no plan', fakeAsync(() => {
    // An empty entitlement map read through the catalogue's absent-means-unlimited rule told a
    // merchant they had unlimited everything. No plan means nothing to report.
    billing.subscription = {...activeSubscription(), planCode: null, planDisplayName: null, entitlements: {}};
    const {element} = page();

    expect(element.querySelectorAll('.allowance-row').length).toBe(0);
    expect(element.textContent).not.toContain('Unlimited');
  }));

  it('reports a store billing has never seen as a state, not a failure', fakeAsync(() => {
    billing.subscription = null;
    const {element} = page();

    expect(element.querySelector('.load-error')).toBeNull();
    expect(element.querySelector('.empty-note')).not.toBeNull();
  }));

  it('offers resume rather than cancel once renewal is already off', fakeAsync(() => {
    billing.subscription = {...activeSubscription(), cancelAtPeriodEnd: true};
    const {element} = page();

    const labels = [...element.querySelectorAll('.plan-actions button')].map((b) => b.textContent ?? '');
    expect(labels.some((label) => label.includes('Resume'))).toBeTrue();
    expect(labels.some((label) => label.includes('Cancel'))).toBeFalse();
  }));

  it('sends a store with no card on file to checkout rather than changing the plan', fakeAsync(() => {
    billing.subscription = trialingSubscription();
    const {fixture} = page();
    /*
     * `checkout` is stubbed to never emit, which is what keeps the assertion off `window.location`.
     * Leaving the console is a direct location assignment — the same thing `SessionService` does to
     * reach the login page — and `window.location.assign` is not spy-able. Never emitting means the
     * navigation is simply never reached, and the call itself is what this pins.
     */
    const checkout = spyOn(billing, 'checkout').and.returnValue(NEVER);
    const changePlan = spyOn(billing, 'changePlan').and.callThrough();

    // `providerLinked: false` — nothing to charge yet, so the first paid plan has to collect a card.
    fixture.componentInstance['facade'].choosePlan('price-pro-m');
    tick();

    expect(checkout).toHaveBeenCalledWith(STORE, 'price-pro-m');
    expect(changePlan).not.toHaveBeenCalled();
  }));

  it('changes the plan directly once a card is already on file', fakeAsync(() => {
    // `providerLinked: true` — a second change prorates against the stored card instead of asking again.
    billing.subscription = activeSubscription();
    const {fixture} = page();
    const checkout = spyOn(billing, 'checkout').and.returnValue(NEVER);
    const changePlan = spyOn(billing, 'changePlan').and.callThrough();

    fixture.componentInstance['facade'].choosePlan('price-basic-m');
    tick();

    expect(changePlan).toHaveBeenCalled();
    expect(checkout).not.toHaveBeenCalled();
  }));
});
