import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {Router, provideRouter} from '@angular/router';
import {Observable, Subject, concat, of, throwError} from 'rxjs';

import {NOTIFICATION_PORT} from '@core/errors/notification.port';
import {ToastService} from '@shared/ui/toast/toast';
import {STORE_SETTINGS} from '@mocks/store-settings.fixture';
import type {DomainStatus, SettingsSectionKey, StoreSettings} from '@models/store-settings';

/**
 * A whole settings document.
 *
 * The fixture now covers only the sections still served from it, so the spec supplies the live
 * ones itself — which is the point: these values are the shape `MerchantStoreService.store()` maps
 * to, so a change to that mapping has to be reflected here to keep the spec passing.
 */
const SETTINGS: StoreSettings = {
  ...STORE_SETTINGS,
  storeName: 'Acme Supply Co.',
  branding: {
    logo: {name: 'acme-logo.svg', url: null},
    banner: {name: 'acme-banner-summer.jpg', url: null},
  },
  details: {
    name: 'Acme Supply Co.',
    supportEmail: 'help@acmesupply.co',
    supportPhone: '+1 (415) 555-0142',
    currency: 'USD',
    language: 'en',
    supportedLanguages: ['en', 'ar'],
    country: 'US',
    address: {
      address: '1180 Harrison St, Suite 400',
      city: 'San Francisco',
      postalCode: '94103',
      stateProvince: 'CA',
    },
    theme: 'BASIS',
    colorTheme: 'LIGHT',
    inBusinessSince: '2019-04-01',
    dimensionUnit: 'CM',
    weightUnit: 'KG',
    requireLoginForOrderPlacement: false,
    useCache: true,
    legalName: '',
    slug: '',
    category: '',
    timezone: '',
    taxNumber: '',
    shortDescription: '',
    published: false,
    maintenanceMode: false,
  },
  choices: {
    themes: ['BASIS', 'MODERN'],
    colorThemes: ['LIGHT', 'DARK'],
    languages: ['en', 'ar'],
    socialLinkProviders: ['FACEBOOK', 'X', 'TIKTOK', 'INSTAGRAM', 'GITHUB'],
  },
};
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

  private settings: StoreSettings = SETTINGS;
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

  resolve(value: StoreSettings = SETTINGS): void {
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

  /** The phone control's own `.field`, so an assertion cannot pick up the store name's error. */
  function phoneField(element: HTMLElement): HTMLElement {
    return element.querySelector('#support-phone')!.closest('.field') as HTMLElement;
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
      SETTINGS.storeName,
    );
    /*
     * No publish badge: nothing on the platform records a store's publish state, so the mockup's
     * green pill asserted something no service can answer. See lessons.md, "Store management — a
     * store has no published or maintenance state".
     */
    expect(element.querySelector('app-badge')).toBeNull();
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

    const requireLogin = element.querySelector<HTMLButtonElement>(
      '.storefront-flags [role="switch"]',
    )!;
    expect(requireLogin.getAttribute('aria-checked')).toBe('false');

    requireLogin.click();
    settle(fixture);

    expect(requireLogin.getAttribute('aria-checked')).toBe('true');
    expect(saveButton(element).disabled).toBeFalse();
  }));

  it('offers every country and every currency, not just the ones the store trades in', fakeAsync(() => {
    const {element} = load('details');

    /*
     * The platform serves neither list, so both are built from ISO codes plus `Intl`. The counts
     * are the registries': 249 countries, and however many currencies this runtime knows. What
     * matters is that neither is the store's own four-item supported set, which is what the
     * country select used to be reduced to.
     */
    const countries = element.querySelectorAll('#store-country option');
    expect(countries.length).toBe(249);
    expect(Array.from(countries).map((option) => option.getAttribute('value'))).toContain('JP');

    const currencies = element.querySelectorAll('#store-currency option');
    expect(currencies.length).toBeGreaterThan(100);
    expect(Array.from(currencies).map((option) => option.getAttribute('value'))).toContain('SAR');
  }));

  it('offers the default language as a name, and only from the supported set', fakeAsync(() => {
    const {element} = load('details');

    const options = Array.from(element.querySelectorAll('#store-language option'));
    expect(options.map((option) => option.getAttribute('value'))).toEqual(['ar', 'en']);
    // Named rather than coded, and ordered by that name: the select used to read "en".
    expect(options[0].textContent).toContain('Arabic');
    expect(options[1].textContent).toContain('English');
  }));

  it('ticks a supported language, marks the section dirty and sends the whole list', fakeAsync(() => {
    const {fixture, element} = load('details');

    const french = Array.from(element.querySelectorAll<HTMLInputElement>('.check input')).find(
      (box) => box.closest('label')?.textContent?.includes('French'),
    )!;
    expect(french.checked).toBeFalse();

    french.click();
    settle(fixture);

    expect(saveButton(element).disabled).toBeFalse();
    saveButton(element).click();
    settle(fixture);

    // Ordered as the list is offered — by name — so the saved value does not churn with clicks.
    expect(api.saves[0].patch['supportedLanguages']).toEqual(['ar', 'en', 'fr']);
  }));

  it('refuses a default language the store no longer supports', fakeAsync(() => {
    const {fixture, element} = load('details');

    const english = Array.from(element.querySelectorAll<HTMLInputElement>('.check input')).find(
      (box) => box.closest('label')?.textContent?.includes('English'),
    )!;
    english.click();
    settle(fixture);

    expect(saveButton(element).disabled).toBeTrue();
    expect(element.querySelector('.cross-field-error')?.textContent).toContain(
      'must be one of the supported languages',
    );
    expect(api.saves.length).toBe(0);
  }));

  it('rejects a phone number that is not one, and requires it', fakeAsync(() => {
    const {fixture, element} = load('details');

    type(element, '#support-phone', 'call the shop');
    settle(fixture);
    expect(saveButton(element).disabled).toBeTrue();

    type(element, '#support-phone', '');
    settle(fixture);
    expect(saveButton(element).disabled).toBeTrue();
    expect(phoneField(element).querySelector('app-field-error')?.textContent).toContain(
      'phone number',
    );

    type(element, '#support-phone', '+44 20 7946 0958');
    settle(fixture);
    expect(saveButton(element).disabled).toBeFalse();
  }));

  it('renders the fields the platform cannot store as disabled, and never submits them', fakeAsync(() => {
    const {fixture, element} = load('details');

    for (const id of ['#legal-name', '#store-slug', '#store-category', '#store-timezone', '#store-tax']) {
      expect(element.querySelector<HTMLInputElement>(id)!.disabled)
        .withContext(`${id} must stay disabled`)
        .toBeTrue();
    }

    type(element, '#store-name', 'Acme Supply Group');
    settle(fixture);
    saveButton(element).click();
    settle(fixture);

    const patch = api.saves[0].patch;
    expect(patch['name']).toBe('Acme Supply Group');
    for (const field of ['legalName', 'slug', 'category', 'timezone', 'taxNumber', 'published']) {
      expect(patch[field]).withContext(`${field} must not be submitted`).toBeUndefined();
    }
  }));

  it('offers no way to remove a logo, because the platform has none', fakeAsync(() => {
    const {element} = load('branding');

    const labels = Array.from(element.querySelectorAll('button')).map((button) =>
      (button.textContent ?? '').toLowerCase(),
    );
    expect(labels.some((label) => label.includes('remove'))).toBeFalse();
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

    expect(title()).toBe(SETTINGS.home.en!.title);

    const tabs = Array.from(
      element.querySelectorAll<HTMLButtonElement>('app-tab-switcher .tab'),
    );
    tabs.find((tab) => tab.textContent?.includes('AR'))!.click();
    settle(fixture);

    expect(title()).toBe(SETTINGS.home.ar!.title);
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
