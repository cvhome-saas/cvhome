import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Observable, Subject, of, throwError} from 'rxjs';

import type {PlanView} from '@models/billing';
import {translocoTesting} from '@testing/transloco-testing';
import {Marketing} from './marketing';
import {MarketingApi} from './services/marketing.api.service';

const CATALOG: PlanView[] = [
  {
    id: {id: 'plan-free'}, code: 'FREE', displayName: 'Free', description: 'A store to try the platform with.',
    tier: 0,
    prices: [{id: {id: 'p-free-m'}, amount: {currency: {code: 'USD'}, minorUnits: 0}, interval: 'MONTH', trialDays: 0}],
    entitlements: {
      MAX_PRODUCTS: {key: 'MAX_PRODUCTS', limitValue: 25, flagValue: null},
      CUSTOM_DOMAIN: {key: 'CUSTOM_DOMAIN', limitValue: null, flagValue: false},
    },
  },
  {
    id: {id: 'plan-basic'}, code: 'BASIC', displayName: 'Basic', description: 'For a store finding its feet.',
    tier: 10,
    prices: [
      {id: {id: 'p-basic-m'}, amount: {currency: {code: 'USD'}, minorUnits: 1000}, interval: 'MONTH', trialDays: 14},
      {id: {id: 'p-basic-y'}, amount: {currency: {code: 'USD'}, minorUnits: 10000}, interval: 'YEAR', trialDays: 14},
    ],
    entitlements: {CUSTOM_DOMAIN: {key: 'CUSTOM_DOMAIN', limitValue: null, flagValue: true}},
  },
  {
    id: {id: 'plan-pro'}, code: 'PRO', displayName: 'Pro', description: 'For a store that is growing.',
    tier: 20,
    prices: [
      {id: {id: 'p-pro-m'}, amount: {currency: {code: 'USD'}, minorUnits: 3000}, interval: 'MONTH', trialDays: 0},
      {id: {id: 'p-pro-y'}, amount: {currency: {code: 'USD'}, minorUnits: 30000}, interval: 'YEAR', trialDays: 0},
    ],
    entitlements: {ANALYTICS: {key: 'ANALYTICS', limitValue: null, flagValue: true}},
  },
];

/** Stands in for billing's public catalog so the spec controls timing and failure. */
class FakeMarketingApi {
  calls = 0;
  pending: Subject<PlanView[]> | null = null;
  failure = false;

  plans(): Observable<PlanView[]> {
    this.calls++;
    if (this.failure) {
      return throwError(() => new Error('billing is down'));
    }
    return this.pending ?? of(CATALOG);
  }
}

describe('Marketing', () => {
  let fixture: ComponentFixture<Marketing>;
  let api: FakeMarketingApi;

  function setup(): void {
    api = new FakeMarketingApi();
    const transloco = translocoTesting();
    TestBed.configureTestingModule({
      imports: [Marketing, ...(transloco.imports as never[])],
      providers: [provideRouter([]), ...transloco.providers, {provide: MarketingApi, useValue: api}],
    });
    fixture = TestBed.createComponent(Marketing);
  }

  function planNames(): string[] {
    return Array.from(fixture.nativeElement.querySelectorAll('#pricing .plan h3')).map(
      (node) => (node as HTMLElement).textContent?.trim() ?? '',
    );
  }

  beforeEach(setup);

  it('renders the plans billing publishes, cheapest first', () => {
    fixture.detectChanges();

    expect(api.calls).toBe(1);
    expect(planNames()).toEqual(['Free', 'Basic', 'Pro']);
    const prices = Array.from(fixture.nativeElement.querySelectorAll('#pricing .price strong'));
    expect(prices.map((node) => (node as HTMLElement).textContent?.trim())).toEqual(['$0', '$10', '$30']);
  });

  it('renders the section without plans while the catalog is in flight', fakeAsync(() => {
    api.pending = new Subject<PlanView[]>();
    fixture.detectChanges();

    // The heading and the billing toggle are prerendered content and must be there with no data at all.
    expect(fixture.nativeElement.querySelector('#pricing .section-heading h2')).toBeTruthy();
    expect(planNames()).toEqual([]);
    expect(fixture.nativeElement.querySelector('#pricing [aria-busy="true"]')).toBeTruthy();

    api.pending.next(CATALOG);
    api.pending.complete();
    tick();
    fixture.detectChanges();

    expect(planNames()).toEqual(['Free', 'Basic', 'Pro']);
  }));

  it('switches interval from the catalog rather than re-fetching or discounting locally', () => {
    fixture.detectChanges();

    fixture.nativeElement.querySelectorAll('#pricing .billing button')[1].click();
    fixture.detectChanges();

    // Yearly prices come from the catalog's YEAR price, and the monthly-only free plan drops out.
    expect(planNames()).toEqual(['Basic', 'Pro']);
    const prices = Array.from(fixture.nativeElement.querySelectorAll('#pricing .price strong'));
    expect(prices.map((node) => (node as HTMLElement).textContent?.trim())).toEqual(['$100', '$300']);
    expect(api.calls).toBe(1);
  });

  it('states the saving the catalog actually gives, not a fixed claim', () => {
    fixture.detectChanges();
    // Nothing to compare against on the monthly view.
    expect(fixture.nativeElement.querySelector('#pricing .plan-saving')).toBeNull();

    fixture.nativeElement.querySelectorAll('#pricing .billing button')[1].click();
    fixture.detectChanges();

    const savings = Array.from(fixture.nativeElement.querySelectorAll('#pricing .plan-saving')).map(
      (node) => (node as HTMLElement).textContent?.trim(),
    );
    // $100/yr vs 12 × $10/mo is 17%. The toggle no longer claims a flat 20%.
    expect(savings).toEqual(['Save 17% against monthly', 'Save 17% against monthly']);
    expect(fixture.nativeElement.querySelectorAll('#pricing .billing button')[1].textContent).not.toContain('20');
  });

  it('says prices are unavailable rather than showing none when billing fails', () => {
    api.failure = true;
    fixture.detectChanges();

    expect(planNames()).toEqual([]);
    expect(fixture.nativeElement.querySelector('#pricing [role="status"]')?.textContent).toContain('unavailable');
  });

  it('labels a ceiling with its number and an omitted one as unlimited', () => {
    fixture.detectChanges();

    const free = fixture.nativeElement.querySelectorAll('#pricing .plan')[0] as HTMLElement;
    expect(free.textContent).toContain('Up to 25 products');
    // FREE does not grant a custom domain, so it is not listed at all.
    expect(free.textContent).not.toContain('Custom domain');

    const basic = fixture.nativeElement.querySelectorAll('#pricing .plan')[1] as HTMLElement;
    expect(basic.textContent).toContain('Unlimited products');
    expect(basic.textContent).toContain('Custom domain');
  });

  it('names the trial in the call to action only when the price grants one', () => {
    fixture.detectChanges();

    const actions = Array.from(fixture.nativeElement.querySelectorAll('#pricing .plan a.button')).map(
      (node) => (node as HTMLElement).textContent?.trim(),
    );
    expect(actions).toEqual(['Start free', 'Start 14-day trial', 'Choose this plan']);
  });

  it('does not pretend the contact form sends anything', () => {
    fixture.detectChanges();

    const submit = fixture.nativeElement.querySelector('#contact button.submit') as HTMLButtonElement;
    expect(submit.disabled).toBeTrue();
    expect(fixture.nativeElement.querySelector('#contact .form-note')?.textContent)
      .toContain('not connected yet');
  });
});
