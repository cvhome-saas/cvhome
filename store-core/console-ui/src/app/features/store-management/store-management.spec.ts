import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {Router, provideRouter} from '@angular/router';
import {Observable, Subject, of, throwError} from 'rxjs';

import {DnsCheckService, type CnameOutcome} from '@api/dns/dns-check.service';
import {NOTIFICATION_PORT} from '@cvhome-saas/ui-kit';
import {ToastService} from '@cvhome-saas/ui-kit/ui';
import type {
  DomainStatus,
  SettingsSectionKey,
  StoreSettings,
} from '@models/store-settings';

/**
 * A whole settings document.
 *
 * The fixture now covers only the sections still served from it, so the spec supplies the live
 * ones itself — which is the point: these values are the shape `MerchantStoreService.store()` maps
 * to, so a change to that mapping has to be reflected here to keep the spec passing.
 */
const SETTINGS: StoreSettings = {
  storeName: 'Acme Supply Co.',
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
  domains: [
    {domain: 'acme-supply', type: 'SUB_DOMAIN', hostname: 'acme-supply.myshop-p1.example.io'},
    {domain: 'shop.acmesupply.co', type: 'CUSTOM_DOMAIN', hostname: 'shop.acmesupply.co'},
  ],
  podTarget: 'myshop-p1.example.io',
  payments: [
    {
      paymentType: 'STRIPE',
      icon: 'creditCard',
      enabled: true,
      credentials: {
        apiKey: 'pk_live_51',
        secretKey: 'sk_live_7c31',
        webhookSecret: 'whsec_a0e5',
        webhookUrl:
          'https://acme.example.io/payment/api/v1/public/webhook/65f023632bc46470c104b76f/STRIPE',
      },
      configured: true,
    },
    {paymentType: 'COD', icon: 'dollar', enabled: false, credentials: null, configured: false},
  ],
  choices: {
    themes: ['BASIS', 'MODERN'],
    colorThemes: ['LIGHT', 'DARK'],
    languages: ['en', 'ar'],
    socialLinkProviders: ['FACEBOOK', 'X', 'TIKTOK', 'INSTAGRAM', 'GITHUB'],
  },
};
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {ConsoleApi} from '@layouts/console-shell/services/console.api.service';
import {CONSOLE_STORES_FAKE, FakeConsoleApi} from '@testing/console-api.fake';
import {translocoTesting} from '@testing/transloco-testing';
import {StoreManagement} from './store-management';
import {
  StoreSettingsApi,
  type SectionPatch,
} from './services/store-settings.api.service';

/**
 * Stands in for the public DNS resolver the custom-domain field validates against.
 *
 * A fake rather than `provideHttpClientTesting`: the field's async validator is what the domain
 * tests are about, and a testing backend that never answers would leave it pending forever — which
 * looks exactly like the field refusing the domain.
 */
class FakeDnsCheckService {
  readonly looked: {domain: string; target: string}[] = [];
  outcome: CnameOutcome = 'points-here';
  failure: Error | null = null;

  checkCname(domain: string, target: string): Observable<CnameOutcome> {
    this.looked.push({domain, target});
    return this.failure ? throwError(() => this.failure) : of(this.outcome);
  }
}

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

  readonly verified: string[] = [];
  readonly removedDomains: string[] = [];
  readonly addedSlides: string[] = [];
  readonly savedSlides: string[][] = [];
  verifyFailure: Error | null = null;

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

  /**
   * One emission, not two. The real service answers a single verdict — `checking` is the facade's
   * own state while the lookup is in flight, not something the DNS resolver reports — and `null`
   * when there was nothing to compare against.
   */
  verifyDomain(domain: string): Observable<DomainStatus | null> {
    this.verified.push(domain);
    if (!domain) {
      return of(null);
    }
    return this.verifyFailure ? throwError(() => this.verifyFailure) : of(this.verifyOutcome);
  }

  removeDomain(domain: string): Observable<StoreSettings> {
    this.removedDomains.push(domain);
    this.settings = {
      ...this.settings,
      domains: this.settings.domains.filter((entry) => entry.domain !== domain),
    };
    return of(this.settings);
  }

  addSlide(file: File): Observable<StoreSettings> {
    this.addedSlides.push(file.name);
    return of(this.settings);
  }

  nextVerify(outcome: DomainStatus): void {
    this.verifyOutcome = outcome;
  }

  /** Overrides part of the document before the page loads, for a state the default fixture lacks. */
  settingsWith(overrides: Partial<StoreSettings>): void {
    this.settings = {...this.settings, ...overrides};
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
  let dns: FakeDnsCheckService;
  let router: Router;

  beforeEach(async () => {
    api = new FakeStoreSettingsApi();
    dns = new FakeDnsCheckService();
    await TestBed.configureTestingModule({
      imports: [StoreManagement, ...translocoTesting().imports],
      providers: [
        ...translocoTesting().providers,
        provideRouter([{path: 'store-management/:section', component: StoreManagement}]),
        {provide: StoreSettingsApi, useValue: api},
        {provide: DnsCheckService, useValue: dns},
        /*
         * The settings resource is keyed on the open store, so the shell has to have one — without
         * it `params` is undefined and the page never loads at all.
         */
        {provide: ConsoleApi, useValue: Object.assign(new FakeConsoleApi(), {stores: CONSOLE_STORES_FAKE})},
        // `ApiErrorService` needs somewhere to send what it cannot bind to a control.
        {provide: NOTIFICATION_PORT, useExisting: ToastService},
      ],
    }).compileComponents();
    router = TestBed.inject(Router);
  });

  /** Creates the page on a section and settles the initial request. */
  function load(section: SettingsSectionKey = 'details'): {
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
    // A second round: the page-provided facade's resource is driven by a component effect, which
    // only issues its request during change detection — one flush later than a root instance did.
    tick();
    fixture.detectChanges();
  }

  /** The add field's re-check button — the ghost action beside it. */
  function checkButton(element: HTMLElement): HTMLButtonElement {
    return element.querySelector('#custom-domain')!.closest('.domain-row')!
      .querySelector<HTMLButtonElement>('.ghost-action')!;
  }

  function saveButton(element: HTMLElement): HTMLButtonElement {
    return element.querySelector<HTMLButtonElement>('.primary-action')!;
  }

  /*
   * Controls are addressed by the form control they are bound to, not by a DOM id.
   *
   * They used to be `#store-name`, `#support-phone` and so on, because each field wrote its own
   * `<input id>`. `app-form-field` associates the control by containment instead, so there is no id
   * to query — and `formControlName` is the better handle anyway: it is the thing that identifies
   * the control to the form, and it does not change when the markup does.
   */
  function control(element: HTMLElement, name: string): HTMLInputElement {
    // A `#id` for the controls that keep one — the per-provider credential fields, where
    // `formControlName="apiKey"` appears once per enabled gateway and would not say which.
    const selector = name.startsWith('#')
      ? name
      : `app-text-field[formcontrolname="${name}"] input, textarea[formcontrolname="${name}"]`;
    return element.querySelector<HTMLInputElement>(selector)!;
  }

  /**
   * One of the supported-language boxes, by the language it names.
   *
   * A checkbox, not a switch: this is a selection from a set, and a screen reader announcing
   * "switch, Arabic, on" for membership of a list is telling the operator something slightly
   * untrue. Drawn rather than tinted, because a native box on a dark theme reads as checked when
   * it is not — lessons.md records that from the catalogue.
   */
  function languageToggle(element: HTMLElement, language: string): HTMLInputElement {
    return Array.from(
      element.querySelectorAll<HTMLInputElement>('.check-grid input[type="checkbox"]'),
    ).find((box) => box.closest('app-checkbox')?.textContent?.includes(language))!;
  }

  /** The named field's own wrapper, so an assertion cannot pick up a neighbour's error. */
  function field(element: HTMLElement, name: string): HTMLElement {
    return element
      .querySelector(`app-text-field[formcontrolname="${name}"]`)!
      .closest('app-form-field') as HTMLElement;
  }

  function type(element: HTMLElement, name: string, value: string): void {
    const input = control(element, name);
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  /**
   * Opens a themed select and returns what it offers.
   *
   * `app-select` draws a listbox behind a button, so its options exist only while it is open —
   * which is the whole reason it replaced the native control: an operating-system dropdown cannot
   * be themed, and in two of three themes it opened a white sheet.
   */
  function optionsOf(fixture: ComponentFixture<unknown>, element: HTMLElement, name: string): string[] {
    const select = element.querySelector(`app-select[formcontrolname="${name}"]`)!;
    select.querySelector<HTMLButtonElement>('.select-trigger')!.click();
    fixture.detectChanges();
    return Array.from(select.querySelectorAll('.option-label')).map(
      (option) => option.textContent?.trim() ?? '',
    );
  }

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
    fixture.componentRef.setInput('section', 'details');
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
    // Three sections plus the storefront builder's own route. Appearance is the content hub's, and
    // sign-in providers left for a page of their own when the three-preset section became a manager.
    expect(links.length).toBe(4);
    expect(links.map((link) => link.getAttribute('href'))).toContain('/store-management/payments');
    expect(links.map((link) => link.getAttribute('href'))).toContain('/store-management/builder');
  }));

  it('keeps Save disabled until the section is dirty', fakeAsync(() => {
    const {fixture, element} = load('details');

    expect(saveButton(element).disabled).toBeTrue();

    type(element, 'name', 'Acme Supply Group');
    settle(fixture);

    expect(saveButton(element).disabled).toBeFalse();
  }));

  it('saves the active section and goes clean again', fakeAsync(() => {
    const {fixture, element} = load('details');

    type(element, 'name', 'Acme Supply Group');
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
    type(element, 'name', 'Acme Supply Group');
    settle(fixture);
    saveButton(element).click();
    settle(fixture);

    expect(saveButton(element).disabled).toBeFalse();
    expect(control(element, 'name').value).toBe(
      'Acme Supply Group',
    );
  }));

  it('blocks Save and shows the error when a required field is cleared', fakeAsync(() => {
    const {fixture, element} = load('details');

    type(element, 'name', '');
    settle(fixture);

    expect(saveButton(element).disabled).toBeTrue();
    expect(element.querySelector('app-field-error')?.textContent).toContain('required');
    expect(api.saves.length).toBe(0);
  }));

  it('keeps the host name out of a pasted URL rather than rejecting it', fakeAsync(() => {
    const {fixture, element} = load('domain');

    // What an operator actually pastes out of the address bar.
    type(element, 'customDomain', 'https://Shop.Example.com:8443/collections/new?a=1');
    settle(fixture);

    expect(control(element, 'customDomain').value).toBe(
      'shop.example.com',
    );
    // The DNS record follows the field, so it names the host the CNAME will be for.
    expect(element.querySelectorAll('.dns-row > *')[1].textContent?.trim()).toBe(
      'shop.example.com',
    );

    fixture.detectChanges();
    tick(600);
    settle(fixture);
    expect(saveButton(element).disabled).toBeFalse();
  }));

  it('still refuses something that is not a host name at all', fakeAsync(() => {
    const {fixture, element} = load('domain');

    type(element, 'customDomain', 'not a domain');
    settle(fixture);

    expect(saveButton(element).disabled).toBeTrue();
    expect(checkButton(element).disabled).toBeTrue();
    expect(element.querySelector('app-field-error')?.textContent).toContain('host name');
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
    const {fixture, element} = load('details');

    /*
     * The platform serves neither list, so both are built from ISO codes plus `Intl`. The counts
     * are the registries': 249 countries, and however many currencies this runtime knows. What
     * matters is that neither is the store's own four-item supported set, which is what the
     * country select used to be reduced to.
     */
    const countries = optionsOf(fixture, element, 'country');
    expect(countries.length).toBe(249);
    expect(countries.join('|')).toContain('Japan');

    const currencies = optionsOf(fixture, element, 'currency');
    expect(currencies.length).toBeGreaterThan(100);
    expect(currencies.join('|')).toContain('SAR');
  }));

  it('offers the default language as a name, and only from the supported set', fakeAsync(() => {
    const {fixture, element} = load('details');

    // Named rather than coded, and ordered by that name: the select used to read "en".
    expect(optionsOf(fixture, element, 'language')).toEqual(['Arabic', 'English']);
  }));

  it('ticks a supported language, marks the section dirty and sends the whole list', fakeAsync(() => {
    const {fixture, element} = load('details');

    const french = languageToggle(element, 'French');
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

    languageToggle(element, 'English').click();
    settle(fixture);

    expect(saveButton(element).disabled).toBeTrue();
    expect(element.querySelector('.cross-field-error')?.textContent).toContain(
      'must be one of the supported languages',
    );
    expect(api.saves.length).toBe(0);
  }));

  it('rejects a phone number that is not one, and requires it', fakeAsync(() => {
    const {fixture, element} = load('details');

    type(element, 'supportPhone', 'call the shop');
    settle(fixture);
    expect(saveButton(element).disabled).toBeTrue();

    type(element, 'supportPhone', '');
    settle(fixture);
    expect(saveButton(element).disabled).toBeTrue();
    expect(field(element, 'supportPhone').querySelector('app-field-error')?.textContent).toContain(
      'phone number',
    );

    type(element, 'supportPhone', '+44 20 7946 0958');
    settle(fixture);
    expect(saveButton(element).disabled).toBeFalse();
  }));

  it('renders the fields the platform cannot store as disabled, and never submits them', fakeAsync(() => {
    const {fixture, element} = load('details');

    for (const name of ['legalName', 'slug', 'category', 'timezone', 'taxNumber']) {
      expect(control(element, name).disabled)
        .withContext(`${name} must stay disabled`)
        .toBeTrue();
    }

    type(element, 'name', 'Acme Supply Group');
    settle(fixture);
    saveButton(element).click();
    settle(fixture);

    const patch = api.saves[0].patch;
    expect(patch['name']).toBe('Acme Supply Group');
    for (const field of ['legalName', 'slug', 'category', 'timezone', 'taxNumber', 'published']) {
      expect(patch[field]).withContext(`${field} must not be submitted`).toBeUndefined();
    }
  }));

  it('collapses a gateway\'s credentials when it is switched off', fakeAsync(() => {
    const {fixture, element} = load('payments');

    // Only Stripe is on, and only Stripe carries credentials — COD declares no attrs.
    expect(element.querySelectorAll('.provider-body').length).toBe(1);

    const stripe = element.querySelector<HTMLButtonElement>('.provider-card [role="switch"]')!;
    stripe.click();
    settle(fixture);

    expect(element.querySelectorAll('.provider-body').length).toBe(0);
  }));

  it('masks a secret it has been given, and reveals it on request', fakeAsync(() => {
    const {fixture, element} = load('payments');

    /*
     * The value is in the field because the API sends it decrypted; the mask is a courtesy against
     * shoulder-surfing, not a claim that the console cannot see it. That is why this asserts on the
     * host class rather than on the rendered characters — there is nothing to hide from the DOM.
     */
    const field = element.querySelector('app-secret-field')!;
    expect(field.classList).toContain('masked');
    expect(element.querySelector<HTMLInputElement>('#secret-key-STRIPE')!.value).toBe('sk_live_7c31');

    field.querySelector<HTMLButtonElement>('.text-action')!.click();
    settle(fixture);

    expect(field.classList).not.toContain('masked');
  }));

  it('sends a gateway\'s credentials as an update when it already had a row', fakeAsync(() => {
    const {fixture, element} = load('payments');

    type(element, '#api-key-STRIPE', 'pk_live_rotated');
    settle(fixture);
    saveButton(element).click();
    settle(fixture);

    expect(api.saves[0].key).toBe('payments');
    const patch = api.saves[0].patch as Record<string, Record<string, unknown>>;
    expect(patch['STRIPE']['apiKey']).toBe('pk_live_rotated');
    // Sent whole: the secret the operator did not touch still has to go, or PUT would blank it.
    expect(patch['STRIPE']['secretKey']).toBe('sk_live_7c31');
  }));

  it('checks the typed domain on its own, without being asked', fakeAsync(() => {
    const {fixture, element} = load('domain');

    // Nothing to say before anything is typed.
    expect(element.querySelector('.status')).toBeNull();
    expect(dns.looked.length).toBe(0);

    type(element, 'customDomain', 'shop.acmesupply.co');
    settle(fixture);
    fixture.detectChanges();
    tick(600);
    settle(fixture);

    expect(dns.looked.map((lookup) => lookup.domain)).toEqual(['shop.acmesupply.co']);
    expect(element.querySelector('.status-head strong')?.textContent?.trim()).toBe(
      'Points at this store',
    );
    expect(element.querySelector('.status')?.classList).toContain('green');
  }));

  it('says nothing about an allocated domain until a re-check finds something', fakeAsync(() => {
    const {fixture, element} = load('domain');
    const row = () => element.querySelector('.domain-row.info-row')!;

    /*
     * No "not checked" badge. A domain is only allocated once its CNAME was confirmed, so a
     * permanent unchecked state would have been the console doubting its own rule.
     */
    expect(row().querySelector('app-badge')).toBeNull();

    // A re-check is different: DNS can change after the fact, and that is worth reporting.
    api.nextVerify('failed');
    row().querySelector<HTMLButtonElement>('.ghost-action')!.click();
    settle(fixture);
    fixture.detectChanges();
    tick(400);
    settle(fixture);

    expect(row().querySelector('app-badge')?.textContent?.trim()).toBe('Points somewhere else');
  }));

  it('leaves no verdict behind when the re-check could not be made', fakeAsync(() => {
    const {fixture, element} = load('domain');
    const row = () => element.querySelector('.domain-row.info-row')!;

    api.verifyFailure = new Error('offline');
    row().querySelector<HTMLButtonElement>('.ghost-action')!.click();
    settle(fixture);
    fixture.detectChanges();
    tick(400);
    settle(fixture);

    expect(row().querySelector('app-badge')).toBeNull();
  }));

  it('lists every allocated domain and removes one', fakeAsync(() => {
    const {fixture, element} = load('domain');

    // The subdomain is the store's address and is not removable; only the custom row offers it.
    const rows = element.querySelectorAll('.domain-row.info-row');
    expect(rows.length).toBe(1);
    expect(rows[0].textContent).toContain('shop.acmesupply.co');

    rows[0].querySelector<HTMLButtonElement>('.icon-action.danger')!.click();
    settle(fixture);

    expect(api.removedDomains).toEqual(['shop.acmesupply.co']);
    expect(element.querySelectorAll('.domain-row.info-row').length).toBe(0);
  }));

  it('adds a domain once its CNAME is confirmed to point here', fakeAsync(() => {
    const {fixture, element} = load('domain');

    type(element, 'customDomain', 'store.example.com');
    settle(fixture);

    // The check is debounced, and Save stays out of reach while it is in flight.
    expect(saveButton(element).disabled).toBeTrue();
    fixture.detectChanges();
    tick(600);
    settle(fixture);

    expect(dns.looked).toEqual([
      {domain: 'store.example.com', target: 'myshop-p1.example.io'},
    ]);
    expect(saveButton(element).disabled).toBeFalse();

    saveButton(element).click();
    settle(fixture);

    expect(api.saves.length).toBe(1);
    expect(api.saves[0].key).toBe('domain');
    expect(api.saves[0].patch['customDomain']).toBe('store.example.com');
  }));

  it('refuses a domain whose CNAME points somewhere else', fakeAsync(() => {
    const {fixture, element} = load('domain');

    dns.outcome = 'points-elsewhere';
    type(element, 'customDomain', 'store.example.com');
    settle(fixture);
    fixture.detectChanges();
    tick(600);
    settle(fixture);

    expect(saveButton(element).disabled).toBeTrue();
    expect(element.querySelector('.status-head strong')?.textContent?.trim()).toBe(
      'Points somewhere else',
    );

    // And once the record is corrected, the same value goes through on a re-check.
    dns.outcome = 'points-here';
    checkButton(element).click();
    settle(fixture);
    fixture.detectChanges();
    tick(600);
    settle(fixture);

    expect(saveButton(element).disabled).toBeFalse();
  }));

  it('refuses a domain with no CNAME at all', fakeAsync(() => {
    const {fixture, element} = load('domain');

    dns.outcome = 'no-record';
    type(element, 'customDomain', 'store.example.com');
    settle(fixture);
    fixture.detectChanges();
    tick(600);
    settle(fixture);

    expect(saveButton(element).disabled).toBeTrue();
    expect(element.querySelector('.status-head strong')?.textContent?.trim()).toBe('No record yet');
  }));

  it('lets a domain through when the resolver itself cannot be reached', fakeAsync(() => {
    const {fixture, element} = load('domain');

    /*
     * A resolver we could not reach says nothing about the operator's DNS. Blocking here would make
     * the field unusable on any network that filters dns.google, so it warns and allows.
     */
    dns.failure = new Error('offline');
    type(element, 'customDomain', 'store.example.com');
    settle(fixture);
    fixture.detectChanges();
    tick(600);
    settle(fixture);

    expect(saveButton(element).disabled).toBeFalse();
    expect(element.querySelector('.field-warning')?.textContent).toContain('could not reach');
  }));

  it('reloads the whole document when the open store changes', fakeAsync(() => {
    const {fixture, element} = load('details');

    expect(api.loads).toBe(1);
    expect(control(element, 'name').value).toBe('Acme Supply Co.');

    api.settingsWith({
      storeName: 'Acme Outlet - West',
      details: {...SETTINGS.details, name: 'Acme Outlet - West'},
    });
    TestBed.inject(ConsoleShellFacade).selectStore(CONSOLE_STORES_FAKE[1].id);
    settle(fixture);

    /*
     * Without a `params` on the resource this stayed on the first store's settings while the
     * request context had already moved to the second — so the next save would have written one
     * store's values onto the other.
     */
    expect(api.loads).toBe(2);
    expect(control(element, 'name').value).toBe('Acme Outlet - West');
  }));

  it('surfaces a failed load with a retry that refetches', fakeAsync(() => {
    api.failure = new Error('Unable to reach store settings.');
    const fixture = TestBed.createComponent(StoreManagement);
    fixture.componentRef.setInput('section', 'details');
    settle(fixture);
    const element = fixture.nativeElement as HTMLElement;

    const alert = element.querySelector('app-load-error');
    expect(alert).not.toBeNull();
    expect(alert?.textContent).toContain('Unable to reach store settings.');

    api.failure = null;
    element.querySelector<HTMLButtonElement>('app-load-error button')!.click();
    settle(fixture);

    expect(api.loads).toBe(2);
    expect(element.querySelector('app-load-error')).toBeNull();
    expect(element.querySelector('app-details-section')).not.toBeNull();
  }));

  it('navigates when a section is picked from the narrow tab track', fakeAsync(() => {
    const {fixture} = load();

    fixture.componentInstance['pickSection']('slider');
    settle(fixture);

    expect(router.url).toContain('/store-management/slider');
  }));
});
