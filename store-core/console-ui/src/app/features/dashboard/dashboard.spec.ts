import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Observable, Subject, of, throwError} from 'rxjs';

import type {DashboardSnapshot} from '@models/dashboard';
import type {DateRangeValue} from '@shared/ui/date-range-picker/date-range-picker';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {ConsoleApi} from '@layouts/console-shell/services/console.api.service';
import {CONSOLE_STORES_FAKE, FakeConsoleApi} from '@testing/console-api.fake';
import {translocoTesting} from '@testing/transloco-testing';
import {Dashboard} from './dashboard';
import {DashboardApi} from './services/dashboard.api.service';

function snapshot(orders: number): DashboardSnapshot {
  return {
    kpis: [
      {id: 'orders', labelKey: 'test.orders', value: `${orders}`, icon: 'shoppingCart', tone: 'blue', delta: '5.8%', trend: 'up'},
      // Two of the four tiles have no source at all; the page has to render that, not hide it.
      {id: 'revenue', labelKey: 'test.revenue', value: null, icon: 'dollar', tone: 'slate', flagKey: 'dashboard.kpi.unavailable'},
    ],
    attention: [
      {labelKey: 'test.awaitingFulfilment', detailKey: 'test.notYetShipped', count: '5', icon: 'clock', tone: 'red'},
    ],
    orderStatuses: [{label: 'Delivered', value: orders, tone: 'green'}],
    products: [{sku: 'ACME-HDPH-01', orders: 48}],
    customerSplit: [
      {label: 'DE', value: 62, tone: 'green'},
      {label: 'US', value: 38, tone: 'slate'},
    ],
  };
}

/** Stands in for the real endpoint so the spec controls timing and failure. */
class FakeDashboardApi {
  readonly requests: DateRangeValue[] = [];
  /** When set, requests hang until `resolve()` — used to observe the loading state. */
  deferred = false;
  pending: Subject<DashboardSnapshot> | null = null;
  failure: Error | null = null;
  next = snapshot(312);

  loadSnapshot(range: DateRangeValue): Observable<DashboardSnapshot> {
    this.requests.push(range);
    if (this.failure) {
      return throwError(() => this.failure);
    }
    if (this.deferred) {
      this.pending = new Subject<DashboardSnapshot>();
      return this.pending;
    }
    return of(this.next);
  }

  resolve(value: DashboardSnapshot): void {
    this.pending?.next(value);
    this.pending?.complete();
    this.pending = null;
  }
}

/**
 * The page owns only its own content — the banner, rail and toolbar are covered by
 * `console-shell.spec.ts`.
 */
describe('Dashboard', () => {
  let api: FakeDashboardApi;

  beforeEach(async () => {
    api = new FakeDashboardApi();
    await TestBed.configureTestingModule({
      imports: [Dashboard, ...translocoTesting().imports],
      providers: [provideRouter([]),
        // The page header names the open store, which comes from the shell.
        {provide: ConsoleApi, useValue: Object.assign(new FakeConsoleApi(), {stores: CONSOLE_STORES_FAKE})}, {provide: DashboardApi, useValue: api}, ...translocoTesting().providers],
    }).compileComponents();
  });

  /** Creates the page and settles the initial request. */
  function load(): {fixture: ComponentFixture<Dashboard>; element: HTMLElement} {
    const fixture = TestBed.createComponent(Dashboard);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    return {fixture, element: fixture.nativeElement as HTMLElement};
  }

  it('renders its widgets and none of the console chrome', fakeAsync(() => {
    const {element} = load();

    expect(element.querySelector('app-kpi-grid')).not.toBeNull();
    expect(element.querySelector('app-action-list')).not.toBeNull();
    expect(element.querySelector('app-donut-chart')).not.toBeNull();
    expect(element.querySelector('app-bar-chart')).not.toBeNull();
    expect(element.querySelector('app-ranked-list')).not.toBeNull();

    // Chrome belongs to the shell; a page must not grow its own.
    expect(element.querySelector('.toolbar')).toBeNull();
    expect(element.querySelector('.sidebar')).toBeNull();
    expect(element.querySelector('app-plan-banner')).toBeNull();
  }));

  it('shows an em dash and says so for a figure with no source, never a zero', fakeAsync(() => {
    const {element} = load();
    const cards = [...element.querySelectorAll('app-kpi-card')] as HTMLElement[];
    const revenue = cards.find((card) => card.textContent?.includes('test.revenue'))
      ?? cards[cards.length - 1];

    // Revenue has no endpoint on the whole platform; a 0 here would be a claim about money.
    expect(revenue.querySelector('.kpi-value')?.textContent?.trim()).toBe('—');
    expect(revenue.textContent).toContain('Not available yet');
  }));

  it('requests data for the selected period on load', fakeAsync(() => {
    load();

    expect(api.requests.length).toBe(1);
    expect(api.requests[0].from).toBeTruthy();
    expect(api.requests[0].to).toBeTruthy();
  }));

  it('veils the widgets until the first response arrives', fakeAsync(() => {
    api.deferred = true;
    const fixture = TestBed.createComponent(Dashboard);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('app-busy-overlay')?.getAttribute('aria-busy')).toBe('true');
    expect(element.querySelector('.busy-veil')).not.toBeNull();
    expect(element.querySelector('app-kpi-grid')).toBeNull();

    api.resolve(snapshot(312));
    tick();
    fixture.detectChanges();

    expect(element.querySelector('app-busy-overlay')?.getAttribute('aria-busy')).toBe('false');
    expect(element.querySelector('.busy-veil')).toBeNull();
    expect(element.querySelector('app-kpi-grid')).not.toBeNull();
  }));

  it('refetches when the open store changes, so figures cannot outlive their store', fakeAsync(() => {
    load();
    const shell = TestBed.inject(ConsoleShellFacade);
    expect(api.requests.length).toBe(1);

    shell.selectStore(CONSOLE_STORES_FAKE[1].id);
    tick();

    // The store id never reaches the request body — the request context stamps it — but the page is
    // a reading of one store and must ask again when that changes.
    expect(api.requests.length).toBe(2);
  }));

  it('refetches when the reporting period changes, and re-renders from the response', fakeAsync(() => {
    const {fixture, element} = load();

    expect(element.querySelector('.kpi-value')?.textContent?.trim()).toBe('312');

    api.next = snapshot(999);
    fixture.componentInstance['facade'].dateRange.set({
      from: new Date(2026, 0, 1),
      to: new Date(2026, 0, 15),
    });
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    expect(api.requests.length).toBe(2);
    expect(api.requests[1].from?.getMonth()).toBe(0);
    expect(element.querySelector('.kpi-value')?.textContent?.trim()).toBe('999');
  }));

  it('keeps the previous figures on screen while the next period loads', fakeAsync(() => {
    const {fixture, element} = load();
    expect(element.querySelector('.kpi-value')?.textContent?.trim()).toBe('312');

    api.deferred = true;
    fixture.componentInstance['facade'].dateRange.set({
      from: new Date(2026, 1, 1),
      to: new Date(2026, 1, 10),
    });
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    // Veiled, but still readable — the layout must not collapse mid-request.
    expect(element.querySelector('.busy-veil')).not.toBeNull();
    expect(element.querySelector('.kpi-value')?.textContent?.trim()).toBe('312');

    api.resolve(snapshot(450));
    tick();
    fixture.detectChanges();

    expect(element.querySelector('.kpi-value')?.textContent?.trim()).toBe('450');
  }));

  it('blocks interaction with the veiled content', fakeAsync(() => {
    api.deferred = true;
    const fixture = TestBed.createComponent(Dashboard);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('.busy-content')?.hasAttribute('inert')).toBeTrue();
  }));

  it('surfaces a failed request with a retry that refetches', fakeAsync(() => {
    api.failure = new Error('Unable to load dashboard metrics.');
    const fixture = TestBed.createComponent(Dashboard);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;

    const alert = element.querySelector('[role="alert"]');
    expect(alert).not.toBeNull();
    expect(alert?.textContent).toContain('Unable to load dashboard metrics.');

    api.failure = null;
    (element.querySelector('[role="alert"] button') as HTMLButtonElement).click();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    expect(element.querySelector('[role="alert"]')).toBeNull();
    expect(element.querySelector('app-kpi-grid')).not.toBeNull();
  }));

  it('uses the custom date range picker in its page header', fakeAsync(() => {
    const {element} = load();

    expect(element.querySelector('app-page-header app-date-range-picker')).not.toBeNull();
    expect(element.querySelector('.date-picker')).toBeNull();
  }));

  it('keeps the page header above the metrics so the date picker is not hidden', fakeAsync(() => {
    const {element} = load();

    const pageHeader = element.querySelector('app-page-header') as HTMLElement;
    const kpis = element.querySelector('app-kpi-grid') as HTMLElement;

    expect(Number(getComputedStyle(pageHeader).zIndex)).toBeGreaterThan(
      Number(getComputedStyle(kpis).zIndex),
    );
  }));
});
