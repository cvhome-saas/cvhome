import {Injectable, inject} from '@angular/core';
import {Observable, catchError, delay, forkJoin, map, of, switchMap} from 'rxjs';

import {MerchantStoreService} from '@api/merchant/store.service';
import {ManagerStoreService} from '@api/tenancy/manager-store.service';
import {STORE_SETTINGS, type FixtureSections} from '@mocks/store-settings.fixture';
import type {
  PersistableMerchantStore,
  ReadableMerchantStore,
  StoreAddress,
} from '@models/merchant';
import type {
  BrandingSettings,
  DomainStatus,
  HomePageCopy,
  LocaleCode,
  SettingsChoices,
  SettingsSectionKey,
  StoreDetails,
  StoreSettings,
} from '@models/store-settings';

/** What one section sends back. Keys are the section's own form controls. */
export type SectionPatch = Readonly<Record<string, unknown>>;

/** How long a DNS check pretends to take, for the sections still on the fixture. */
const VERIFY_MS = 1200;

/**
 * The store's settings.
 *
 * The assembly point for the whole page: it reads each section from the pod that owns it and maps
 * the wire DTOs onto the view models the sections bind to. Save answers with the whole document
 * rather than the patch it was given, because the endpoints answer `void` and the page needs to
 * show what the server actually kept.
 *
 * **Migration state.** Details and branding are live. Home, domain, social links, slider, social
 * login and payments still read from `@mocks/store-settings.fixture`, and are taken over in the
 * commits that follow. `fixtureSections()` is the seam, and it shrinks to nothing.
 */
@Injectable({providedIn: 'root'})
export class StoreSettingsApi {
  private readonly stores = inject(MerchantStoreService);
  private readonly tenancy = inject(ManagerStoreService);

  /**
   * The last store the server sent.
   *
   * A save has to `PUT` a whole `PersistableMerchantStore`: the facade behind it maps every field
   * onto the entity, so sending only what the operator touched would blank the rest. The form owns
   * fifteen of the store's fields and the record has more than that, so the untouched remainder
   * has to come from somewhere — this is that somewhere.
   */
  private loadedStore: ReadableMerchantStore | null = null;

  private verifyAttempts = 0;

  /**
   * Everything the page shows, in one pass.
   *
   * The three reference lists are each optional. They are `public/` lookups on tenancy while the
   * store itself comes from the merchant pod, so one being down says nothing about the other — and
   * a select that falls back to showing only the current value is still a working page, whereas a
   * failed `forkJoin` is a blank one. The store itself is *not* optional: without it there is
   * nothing to render.
   */
  loadSettings(): Observable<StoreSettings> {
    return forkJoin({
      store: this.stores.store(),
      themes: this.optional(this.tenancy.themes()),
      colorThemes: this.optional(this.tenancy.colorThemes()),
      socialLinkProviders: this.optional(this.tenancy.socialLinkProviders()),
      languages: this.optional(this.stores.supportedLanguages()),
    }).pipe(
      map(({store, themes, colorThemes, socialLinkProviders, languages}) => {
        this.loadedStore = store;
        const choices: SettingsChoices = {
          themes,
          colorThemes,
          socialLinkProviders,
          /*
           * The store's own supported languages, falling back to whatever it is already set to —
           * a select whose only option is the current value beats an empty one.
           */
          languages: languages.length > 0 ? languages : this.presentLanguages(store),
        };
        return {
          ...this.fixtureSections(),
          storeName: store.name,
          branding: this.branding(store),
          details: this.details(store),
          choices,
        };
      }),
    );
  }

  /**
   * Applies one section and answers with the whole document.
   *
   * The endpoints answer `void`, so the reload is what produces the document — and it is not
   * merely bookkeeping: it is how the page learns what the server normalised, rejected or filled
   * in, rather than echoing the operator's own input back at them.
   */
  saveSection(key: SettingsSectionKey, patch: SectionPatch): Observable<StoreSettings> {
    switch (key) {
      case 'details':
        return this.stores.update(this.persistable(patch)).pipe(switchMap(() => this.loadSettings()));

      /*
       * Branding is uploads, which have already been sent by the time Save is reachable; the
       * slider is the same. Neither form carries a control, so there is nothing to submit — a
       * reload is the honest answer.
       */
      case 'branding':
      case 'slider':
        return this.loadSettings();

      // TODO(lessons.md): still fixture-backed until their own commits in this module.
      default:
        return this.saveFixtureSection(key, patch);
    }
  }

  /** Removes the store. There is no undo, which is why the section asks the operator to type its name. */
  deleteStore(): Observable<void> {
    return this.stores.delete();
  }

  uploadLogo(file: File): Observable<StoreSettings> {
    return this.stores.addLogo(file).pipe(switchMap(() => this.loadSettings()));
  }

  uploadBanner(file: File): Observable<StoreSettings> {
    return this.stores.addBanner(file).pipe(switchMap(() => this.loadSettings()));
  }

  /**
   * Walks a domain check through its visible states. Still simulated — the real DoH lookup
   * arrives with the domain section.
   */
  verifyDomain(domain: string): Observable<DomainStatus> {
    if (!domain) {
      return of<DomainStatus>('unverified');
    }
    this.verifyAttempts += 1;
    const outcome: DomainStatus = this.verifyAttempts > 1 ? 'verified' : 'waiting';
    return of<DomainStatus>('checking').pipe(
      switchMap(() => of<DomainStatus>('checking')),
      switchMap(() => of(outcome).pipe(delay(VERIFY_MS))),
    );
  }

  /** A reference list whose absence degrades one select rather than the page. */
  private optional(source: Observable<string[]>): Observable<readonly string[]> {
    return source.pipe(catchError(() => of<string[]>([])));
  }

  private presentLanguages(store: ReadableMerchantStore): readonly string[] {
    const languages = store.supportedLanguages ?? [];
    if (languages.length > 0) {
      return languages;
    }
    return store.defaultLanguage ? [store.defaultLanguage] : [];
  }

  /**
   * The store's marketing images.
   *
   * `ReadableImage` is a name and a path, and the path is where the pod put the file — which is
   * not necessarily a URL this browser can reach. The section renders a lettermark when the image
   * fails to load rather than assuming it will. See lessons.md, "Orders — the store's logo URL is
   * not reachable from the browser", which is the same gap seen from the invoice.
   *
   */
  private branding(store: ReadableMerchantStore): BrandingSettings {
    return {
      logo: store.logo?.name ? {name: store.logo.name, url: store.logo.path ?? null} : null,
      banner: store.banner?.name ? {name: store.banner.name, url: store.banner.path ?? null} : null,
    };
  }

  private details(store: ReadableMerchantStore): StoreDetails {
    const address = store.address;
    return {
      name: store.name ?? '',
      supportEmail: store.email ?? '',
      supportPhone: store.phone ?? '',
      currency: store.currency ?? '',
      language: store.defaultLanguage ?? '',
      supportedLanguages: store.supportedLanguages ?? [],
      country: store.countryIsoCode ?? address?.country ?? '',
      address: {
        address: address?.address ?? '',
        city: address?.city ?? '',
        postalCode: address?.postalCode ?? '',
        stateProvince: address?.stateProvince ?? '',
      },
      theme: store.theme ?? '',
      colorTheme: store.colorTheme ?? '',
      inBusinessSince: store.inBusinessSince ?? '',
      dimensionUnit: store.dimension ?? '',
      weightUnit: store.weight ?? '',
      requireLoginForOrderPlacement: store.requireLoginForOrderPlacement ?? false,
      useCache: store.useCache ?? false,

      /*
       * Nothing on the platform stores these, so they read as empty and the section renders them
       * disabled. They are not dropped from the model: the design asks for them, and an empty
       * disabled field with a reason beside it is a more honest answer than a missing one.
       * TODO(lessons.md): see lessons.md, "Store management — six designed store fields do not
       * exist" and "Store management — a store has no published or maintenance state".
       */
      legalName: '',
      slug: '',
      category: '',
      timezone: '',
      taxNumber: '',
      shortDescription: '',
      published: false,
      maintenanceMode: false,
    };
  }

  /**
   * The form's values folded onto the store the server last sent.
   *
   * Only the controls the form owns are read; everything else is carried through untouched. The
   * unbacked controls never arrive here at all — `sectionValueOf` reads `value`, which omits
   * disabled controls — so there is nothing to filter out.
   */
  private persistable(patch: SectionPatch): PersistableMerchantStore {
    const store = this.loadedStore;
    if (!store) {
      throw new Error('Cannot save store details before the store has loaded.');
    }

    const address: StoreAddress = {
      ...store.address,
      address: this.text(patch['addressLine'], store.address?.address ?? ''),
      city: this.text(patch['city'], store.address?.city ?? ''),
      postalCode: this.text(patch['postalCode'], store.address?.postalCode ?? ''),
      stateProvince: this.text(patch['stateProvince'], store.address?.stateProvince ?? ''),
      country: this.text(patch['country'], store.countryIsoCode ?? store.address?.country ?? ''),
    };

    return {
      id: store.id,
      org: store.org,
      template: store.template,
      currencyFormatNational: store.currencyFormatNational,
      supportedLanguages: store.supportedLanguages,
      storeDomains: store.storeDomains,
      socialLinks: store.socialLinks,
      sliderImages: store.sliderImages?.map((slide) => ({priority: slide.priority, name: slide.name})),

      name: this.text(patch['name'], store.name ?? ''),
      email: this.text(patch['supportEmail'], store.email ?? ''),
      phone: this.text(patch['supportPhone'], store.phone ?? ''),
      currency: this.text(patch['currency'], store.currency ?? ''),
      defaultLanguage: this.text(patch['language'], store.defaultLanguage ?? ''),
      countryIsoCode: address.country,
      theme: this.text(patch['theme'], store.theme ?? ''),
      colorTheme: this.text(patch['colorTheme'], store.colorTheme ?? ''),
      /* `LocalDate` — an empty control must be omitted, not sent as `''`, which will not parse. */
      inBusinessSince: this.text(patch['inBusinessSince'], store.inBusinessSince ?? '') || undefined,
      dimension: this.text(patch['dimensionUnit'], store.dimension ?? ''),
      weight: this.text(patch['weightUnit'], store.weight ?? ''),
      requireLoginForOrderPlacement: this.flag(
        patch['requireLoginForOrderPlacement'],
        store.requireLoginForOrderPlacement ?? false,
      ),
      useCache: this.flag(patch['useCache'], store.useCache ?? false),
      address,
    };
  }

  /* ---- the shrinking fixture half ---------------------------------------------------------- */

  /** The sections not yet migrated, held between saves so the page behaves as it did. */
  private fixture: FixtureSections = STORE_SETTINGS;

  private fixtureSections(): FixtureSections {
    return {
      home: this.fixture.home,
      domains: this.fixture.domains,
      socialLinks: this.fixture.socialLinks,
      slides: this.fixture.slides,
      socialLogin: this.fixture.socialLogin,
      payments: this.fixture.payments,
    };
  }

  private saveFixtureSection(key: SettingsSectionKey, patch: SectionPatch): Observable<StoreSettings> {
    this.fixture = this.mergeFixture(key, patch);
    return this.loadSettings();
  }

  private mergeFixture(key: SettingsSectionKey, patch: SectionPatch): FixtureSections {
    const current = this.fixture;

    switch (key) {
      case 'home':
        return {...current, home: {...current.home, ...this.homePatch(patch)}};

      case 'domain':
        return {...current, domains: this.domainsPatch(patch)};

      case 'social':
        return {
          ...current,
          socialLinks: current.socialLinks.map((link) => ({
            ...link,
            url: this.text(patch[link.provider], link.url),
          })),
        };

      case 'social-login':
        return {
          ...current,
          socialLogin: current.socialLogin.map((config) => {
            const slice = this.slice(patch[config.providerId]);
            return {
              ...config,
              enabled: this.flag(slice['enabled'], config.enabled),
              appId: this.text(slice['appId'], config.appId),
            };
          }),
        };

      case 'payments':
        return {
          ...current,
          payments: current.payments.map((gateway) => {
            const slice = this.slice(patch[gateway.paymentType]);
            return {
              ...gateway,
              enabled: this.flag(slice['enabled'], gateway.enabled),
              credentials: gateway.credentials && {
                ...gateway.credentials,
                apiKey: this.text(slice['apiKey'], gateway.credentials.apiKey),
              },
            };
          }),
        };

      case 'branding':
      case 'slider':
      case 'details':
        return current;
    }
  }

  private homePatch(patch: SectionPatch): Partial<Record<LocaleCode, HomePageCopy>> {
    const home: Partial<Record<LocaleCode, HomePageCopy>> = {};

    for (const [code, raw] of Object.entries(patch)) {
      const slice = this.slice(raw);
      const copy: HomePageCopy = {
        title: this.text(slice['title'], ''),
        text: this.text(slice['text'], ''),
        metaDescription: this.text(slice['metaDescription'], ''),
        tags: Array.isArray(slice['tags']) ? (slice['tags'] as readonly string[]) : [],
      };
      if (copy.title || copy.text || copy.metaDescription || copy.tags.length > 0) {
        home[code as LocaleCode] = copy;
      }
    }

    return home;
  }

  private domainsPatch(patch: SectionPatch): FixtureSections['domains'] {
    const typed = this.text(patch['customDomain'], '');

    return this.fixture.domains.map((entry) => {
      if (entry.type !== 'CUSTOM_DOMAIN' || entry.domain === typed) {
        return entry;
      }
      this.verifyAttempts = 0;
      return {
        ...entry,
        domain: typed,
        status: 'unverified' as DomainStatus,
        record: entry.record && {...entry.record, name: typed.split('.')[0]},
      };
    });
  }

  private slice(raw: unknown): Readonly<Record<string, unknown>> {
    return typeof raw === 'object' && raw !== null ? (raw as Record<string, unknown>) : {};
  }

  private text(raw: unknown, fallback: string): string {
    return typeof raw === 'string' ? raw : fallback;
  }

  private flag(raw: unknown, fallback: boolean): boolean {
    return typeof raw === 'boolean' ? raw : fallback;
  }
}
