import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {Router, provideRouter} from '@angular/router';
import {Observable, Subject, concat, of, throwError} from 'rxjs';

import {NOTIFICATION_PORT} from '@core/errors/notification.port';
import {ToastService} from '@shared/ui/toast/toast';
import {STORE_SETTINGS} from '@mocks/store-settings.fixture';
import type {DomainStatus, SettingsSectionKey, StoreSettings} from '@models/store-settings';
import {translocoTesting} from '@testing/transloco-testing';
import {StoreManagement} from './store-management';
import {
  StoreSettingsApi,
  type SectionPatch,
} from './services/store-settings.api.service';

/** Stands in for the real endpoints so the spec controls timing, failure and outcome. */
class FakeStoreSettingsApi {
  readonly saves: {key: SettingsSectionKey; patch: SectionPatch}[] = [];
  loads = 0;

  /** When set, loads hang until `resolve()` — used to observe the loading state. */
  deferred = false;
  pending: Subject<StoreSettings> | null = null;
  failure: Error | null = null;
  /** What the next save answers with. Defaults to the patch folded into the fixture. */
  saveFailure: Error | null = null;

  private settings: StoreSettings = STORE_SETTINGS;
  private verifyOutcome: DomainStatus = 'waiting';

  loadSettings(): Observable<StoreSettings> {
    this.loads += 1;
    if (this.failure) {
      return throwError(() => this.failure);
    }
    if (this.deferred) {
      this.pending = new Subject<StoreSettings>();
      return this.pending;
    }
    return of(this.settings);
  }

  saveSection(key: SettingsSectionKey, patch: SectionPatch): Observable<StoreSettings> {
    this.saves.push({key, patch});
    if (this.saveFailure) {
      return throwError(() => this.saveFailure);
    }
    // A new identity, as a real response would be, so the facade re-fills the forms.
    this.settings = {...this.settings};
    return of(this.settings);
  }

  verifyDomain(domain: string): Observable<DomainStatus> {
    if (!domain) {
      return of<DomainStatus>('unverified');
    }
    return concat(of<DomainStatus>('checking'), of(this.verifyOutcome));
  }

  nextVerify(outcome: DomainStatus): void {
    this.verifyOutcome = outcome;
  }

  resolve(value: StoreSettings = STORE_SETTINGS): void {
    this.pending?.next(value);
    this.pending?.complete();
    this.pending = null;
  }
}

/**
 * The page owns only its own content — the banner, rail and toolbar are covered by
 * `console-shell.spec.ts`.
 */
describe('StoreManagement', () => {
  let api: FakeStoreSettingsApi;
  let router: Router;

  beforeEach(async () => {
    api = new FakeStoreSettingsApi();
    await TestBed.configureTestingModule({
      imports: [StoreManagement, ...translocoTesting().imports],
      providers: [
        ...translocoTesting().providers,
        provideRouter([{path: 'store-management/:section', component: StoreManagement}]),
        {provide: StoreSettingsApi, useValue: api},
        // `ApiErrorService` needs somewhere to send what it cannot bind to a control.
        {provide: NOTIFICATION_PORT, useExisting: ToastService},
      ],
    }).compileComponents();
    router = TestBed.inject(Router);
  });

  /** Creates the page on a section and settles the initial request. */
  function load(section: SettingsSectionKey = 'branding'): {
    fixture: ComponentFixture<StoreManagement>;
    element: HTMLElement;
  } {
    const fixture = TestBed.createComponent(StoreManagement);
    fixture.componentRef.setInput('section', section);
    settle(fixture);
    return {fixture, element: fixture.nativeElement as HTMLElement};
  }

  /** Settles whatever the last interaction kicked off. */
  function settle(fixture: ComponentFixture<StoreManagement>): void {
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
  }

  function saveButton(element: HTMLElement): HTMLButtonElement {
    return element.querySelector<HTMLButtonElement>('.primary-action')!;
  }

  function type(element: HTMLElement, selector: string, value: string): void {
    const field = element.querySelector<HTMLInputElement>(selector)!;
    field.value = value;
    field.dispatchEvent(new Event('input'));
  }

  it('renders the branding section by default and none of the console chrome', fakeAsync(() => {
    const {element} = load();

    expect(element.querySelector('app-branding-section')).not.toBeNull();
    expect(element.querySelector('app-settings-nav')).not.toBeNull();
    expect(element.querySelector('app-details-section')).toBeNull();

    // Chrome belongs to the shell; a page must not grow its own.
    expect(element.querySelector('.toolbar')).toBeNull();
    expect(element.querySelector('.sidebar')).toBeNull();
    expect(element.querySelector('app-plan-banner')).toBeNull();
  }));

  it('loads the settings once and names the store under the title', fakeAsync(() => {
    const {element} = load();

    expect(api.loads).toBe(1);
    expect(element.querySelector('app-page-header')?.textContent).toContain(
      STORE_SETTINGS.storeName,
    );
    expect(element.querySelector('app-badge')?.textContent).toContain('Published');
  }));

  it('veils the page until the first response arrives', fakeAsync(() => {
    api.deferred = true;
    const fixture = TestBed.createComponent(StoreManagement);
    fixture.componentRef.setInput('section', 'branding');
    settle(fixture);
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('app-busy-overlay')?.getAttribute('aria-busy')).toBe('true');
    expect(element.querySelector('.settings-layout')).toBeNull();

    api.resolve();
    settle(fixture);

    expect(element.querySelector('app-busy-overlay')?.getAttribute('aria-busy')).toBe('false');
    expect(element.querySelector('.settings-layout')).not.toBeNull();
  }));

  it('swaps the card for the section the route names', fakeAsync(() => {
    const {fixture, element} = load('domain');

    expect(element.querySelector('app-domain-section')).not.toBeNull();
    expect(element.querySelector('app-branding-section')).toBeNull();

    fixture.componentRef.setInput('section', 'payments');
    settle(fixture);

    expect(element.querySelector('app-payments-section')).not.toBeNull();
    expect(element.querySelector('app-domain-section')).toBeNull();
  }));

  it('links every section rather than holding the choice in the component', fakeAsync(() => {
    const {element} = load();

    const links = Array.from(element.querySelectorAll<HTMLAnchorElement>('.nav-item[href]'));
    expect(links.length).toBe(8);
    expect(links.map((link) => link.getAttribute('href'))).toContain('/store-management/payments');
  }));

  it('keeps Save disabled until the section is dirty', fakeAsync(() => {
    const {fixture, element} = load('details');

    expect(saveButton(element).disabled).toBeTrue();

    type(element, '#store-name', 'Acme Supply Group');
    settle(fixture);

    expect(saveButton(element).disabled).toBeFalse();
  }));

  it('saves the active section and goes clean again', fakeAsync(() => {
    const {fixture, element} = load('details');

    type(element, '#store-name', 'Acme Supply Group');
    settle(fixture);
    saveButton(element).click();
    settle(fixture);

    expect(api.saves.length).toBe(1);
    expect(api.saves[0].key).toBe('details');
    expect(api.saves[0].patch['name']).toBe('Acme Supply Group');
    expect(saveButton(element).disabled).toBeTrue();
  }));

  it('keeps a failed save dirty, with the operator\'s text still in the field', fakeAsync(() => {
    const {fixture, element} = load('details');

    api.saveFailure = new Error('Unable to save.');
    type(element, '#store-name', 'Acme Supply Group');
    settle(fixture);
    saveButton(element).click();
    settle(fixture);

    expect(saveButton(element).disabled).toBeFalse();
    expect(element.querySelector<HTMLInputElement>('#store-name')!.value).toBe(
      'Acme Supply Group',
    );
  }));

  it('blocks Save and shows the error when a required field is cleared', fakeAsync(() => {
    const {fixture, element} = load('details');

    type(element, '#store-name', '');
    settle(fixture);

    expect(saveButton(element).disabled).toBeTrue();
    expect(element.querySelector('app-field-error')?.textContent).toContain('required');
    expect(api.saves.length).toBe(0);
  }));

  it('rejects a custom domain carrying a protocol or a path', fakeAsync(() => {
    const {fixture, element} = load('domain');

    type(element, '#custom-domain', 'https://foo/');
    settle(fixture);

    expect(saveButton(element).disabled).toBeTrue();
    expect(element.querySelector('app-field-error')?.textContent).toContain('bare host name');

    type(element, '#custom-domain', 'shop.example.com');
    settle(fixture);

    expect(saveButton(element).disabled).toBeFalse();
  }));

  it('flips a toggle and marks the section dirty', fakeAsync(() => {
    const {fixture, element} = load('details');

    const published = element.querySelector<HTMLButtonElement>('.switch-row [role="switch"]')!;
    expect(published.getAttribute('aria-checked')).toBe('true');

    published.click();
    settle(fixture);

    expect(published.getAttribute('aria-checked')).toBe('false');
    expect(saveButton(element).disabled).toBeFalse();
  }));

  it('collapses a gateway\'s credentials when it is switched off', fakeAsync(() => {
    const {fixture, element} = load('payments');

    expect(element.querySelectorAll('.provider-body').length).toBe(2);

    const stripe = element.querySelector<HTMLButtonElement>('.provider-card [role="switch"]')!;
    stripe.click();
    settle(fixture);

    expect(element.querySelectorAll('.provider-body').length).toBe(1);
  }));

  it('never renders a stored secret, only what it ends with', fakeAsync(() => {
    const {element} = load('payments');

    const masks = Array.from(element.querySelectorAll('.secret .mask')).map(
      (mask) => mask.textContent ?? '',
    );
    expect(masks.length).toBeGreaterThan(0);
    for (const mask of masks) {
      expect(mask).toContain('••••');
    }
    expect(element.querySelector('.secret input')).toBeNull();
  }));

  it('walks the domain through checking and then the outcome', fakeAsync(() => {
    const {fixture, element} = load('domain');
    const statusText = () => element.querySelector('.status-head strong')?.textContent?.trim();

    expect(statusText()).toBe('Waiting for DNS');

    api.nextVerify('verified');
    element.querySelector<HTMLButtonElement>('.domain-row .primary-action')!.click();
    settle(fixture);

    // Both emissions land synchronously here; the last one is what the panel settles on.
    expect(statusText()).toBe('Domain verified');
    expect(element.querySelector('.status')?.classList).toContain('green');
  }));

  it('swaps the home-page copy with the language track', fakeAsync(() => {
    const {fixture, element} = load('home');
    const title = () => element.querySelector<HTMLInputElement>('#home-title')!.value;

    expect(title()).toBe(STORE_SETTINGS.home.en!.title);

    const tabs = Array.from(
      element.querySelectorAll<HTMLButtonElement>('app-tab-switcher .tab'),
    );
    tabs.find((tab) => tab.textContent?.includes('AR'))!.click();
    settle(fixture);

    expect(title()).toBe(STORE_SETTINGS.home.ar!.title);
  }));

  it('surfaces a failed load with a retry that refetches', fakeAsync(() => {
    api.failure = new Error('Unable to reach store settings.');
    const fixture = TestBed.createComponent(StoreManagement);
    fixture.componentRef.setInput('section', 'branding');
    settle(fixture);
    const element = fixture.nativeElement as HTMLElement;

    const alert = element.querySelector('.load-error');
    expect(alert).not.toBeNull();
    expect(alert?.textContent).toContain('Unable to reach store settings.');

    api.failure = null;
    element.querySelector<HTMLButtonElement>('.load-error button')!.click();
    settle(fixture);

    expect(api.loads).toBe(2);
    expect(element.querySelector('.load-error')).toBeNull();
    expect(element.querySelector('app-branding-section')).not.toBeNull();
  }));

  it('navigates when a section is picked from the narrow tab track', fakeAsync(() => {
    const {fixture} = load();

    fixture.componentInstance['pickSection']('slider');
    settle(fixture);

    expect(router.url).toContain('/store-management/slider');
  }));
});
