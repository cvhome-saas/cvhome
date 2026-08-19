import {Injectable, inject} from '@angular/core';
import {Observable, catchError, forkJoin, map, of, switchMap} from 'rxjs';

import {DnsCheckService, type CnameOutcome} from '@api/dns/dns-check.service';
import {MerchantRouterService} from '@api/merchant/router.service';
import {MerchantStoreService} from '@api/merchant/store.service';
import {ManagerStoreService} from '@api/tenancy/manager-store.service';
import {SaasService, podHostname} from '@api/tenancy/saas.service';
import {STORE_SETTINGS, type FixtureSections} from '@mocks/store-settings.fixture';
import type {
  ManagerStoreDomain,
  PersistableMerchantStore,
  ReadableMerchantStore,
  StoreAddress,
} from '@models/merchant';
import {
  SOCIAL_LINK_FALLBACK_ICON,
  SOCIAL_LINK_ICON,
  isSocialLinkProvider,
  type BrandingSettings,
  type DomainStatus,
  type HomePageCopy,
  type LocaleCode,
  type SettingsChoices,
  type SettingsSectionKey,
  type SliderSlide,
  type SocialLinkSetting,
  type StoreDetails,
  type StoreDomain,
  type StoreSettings,
} from '@models/store-settings';

/** What one section sends back. Keys are the section's own form controls. */
export type SectionPatch = Readonly<Record<string, unknown>>;

/**
 * What a CNAME lookup means for the panel.
 *
 * `no-record` and `no-such-domain` are both "not there yet" rather than wrong: a record that has been
 * added seconds ago looks exactly like one that never will be, and telling the operator their DNS is
 * broken while it propagates is the unhelpful reading.
 */
/** The order the five providers are shown in when the reference call is the leg that failed. */
const SOCIAL_LINK_ORDER = ['INSTAGRAM', 'FACEBOOK', 'X', 'TIKTOK', 'GITHUB'] as const;

const DOMAIN_OUTCOME: Readonly<Record<CnameOutcome, DomainStatus>> = {
  'points-here': 'verified',
  'points-elsewhere': 'failed',
  'no-record': 'waiting',
  'no-such-domain': 'waiting',
};

/**
 * The store's settings.
 *
 * The assembly point for the whole page: it reads each section from the pod that owns it and maps
 * the wire DTOs onto the view models the sections bind to. Save answers with the whole document
 * rather than the patch it was given, because the endpoints answer `void` and the page needs to
 * show what the server actually kept.
 *
 * **Migration state.** Details, branding, domain, social links and the slider are live. Home,
 * social login and payments still read from `@mocks/store-settings.fixture`, and are taken over in
 * the commits that follow. `fixtureSections()` is the seam, and it shrinks to nothing.
 */
@Injectable({providedIn: 'root'})
export class StoreSettingsApi {
  private readonly stores = inject(MerchantStoreService);
  private readonly tenancy = inject(ManagerStoreService);
  private readonly router = inject(MerchantRouterService);
  private readonly saas = inject(SaasService);
  private readonly dns = inject(DnsCheckService);

  /**
   * The last store the server sent.
   *
   * A save has to `PUT` a whole `PersistableMerchantStore`: the facade behind it maps every field
   * onto the entity, so sending only what the operator touched would blank the rest. The form owns
   * fifteen of the store's fields and the record has more than that, so the untouched remainder
   * has to come from somewhere — this is that somewhere.
   */
  private loadedStore: ReadableMerchantStore | null = null;

  /**
   * The hostname a custom domain has to CNAME to, from the last load.
   *
   * Held because `verifyDomain` needs it and the check is a user action rather than part of the load —
   * re-fetching two endpoints on every click to rebuild a string that has not changed would be waste.
   * `null` means the pod lookup failed, and the section says so rather than checking against nothing.
   */
  private podTarget: string | null = null;

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
      allocations: this.optionalList(this.router.allocations()),
      /*
       * The two halves of the CNAME target. Optional for different reasons: `saas-properties` is
       * public and should always answer, while `store-pod-by-store-id` is refused outright for a
       * suspended or archived store. Either being absent costs the section its CNAME instructions,
       * not the page.
       */
      saas: this.optionalOne(this.saas.saasProperties()),
      pod: this.optionalOne(this.saas.storePod()),
    }).pipe(
      map(({store, themes, colorThemes, socialLinkProviders, languages, allocations, saas, pod}) => {
        this.loadedStore = store;
        this.podTarget = podHostname(saas, pod);
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
          domains: this.domains(allocations, store),
          podTarget: this.podTarget,
          socialLinks: this.socialLinks(store, socialLinkProviders),
          slides: this.slides(store),
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

      /*
       * The whole set is sent every time and the server replaces rather than merges
       * (`MerchantStoreServiceImpl.updateSocialLinks` does a bare `setSocialLinks`), so an emptied
       * field genuinely clears that provider's link — which is the only way to remove one.
       */
      case 'social':
        return this.stores
          .updateSocialLinks(this.socialLinksBody(patch))
          .pipe(switchMap(() => this.loadSettings()));

      /*
       * Adding a domain, not editing one. The section's field is an input to an action: the router
       * has no update, only allocate and remove, so *Save changes* here means "point this hostname
       * at the store". Removal is its own per-row action.
       */
      case 'domain': {
        const domain = this.text(patch['customDomain'], '').trim();
        if (!domain) {
          return this.loadSettings();
        }
        return this.router.allocate(domain).pipe(switchMap(() => this.loadSettings()));
      }

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

  /** Removes a hostname from the store. The subdomain cannot be removed — the section does not offer it. */
  removeDomain(domain: string): Observable<StoreSettings> {
    return this.router.remove(domain).pipe(switchMap(() => this.loadSettings()));
  }

  /**
   * Looks a custom domain's CNAME up, in the operator's own browser.
   *
   * Answers `null` when there is nothing to compare against — the pod lookup is refused for a
   * suspended or archived store, so the console does not know what the record should say and will
   * not claim the operator's DNS is wrong. A lookup that *fails* throws rather than resolving to
   * `failed`: "we could not check" and "your record is wrong" are different answers and the facade
   * shows different things for them.
   */
  verifyDomain(domain: string): Observable<DomainStatus | null> {
    const target = this.podTarget;
    if (!domain || !target) {
      return of(null);
    }
    return this.dns.checkCname(domain, target).pipe(map((outcome) => DOMAIN_OUTCOME[outcome]));
  }

  /** Uploads one slide and answers with the document; the pod names the file, so a reload is the only way to learn it. */
  addSlide(file: File): Observable<StoreSettings> {
    return this.stores.addSliderImage(file).pipe(switchMap(() => this.loadSettings()));
  }

  /**
   * Replaces the slider with exactly this list.
   *
   * There is no delete-slide and no reorder endpoint — both are expressed by sending the list you
   * want, which is why the section hands over the whole thing. `priority` is renumbered from zero
   * here rather than in the component, so the contract lives next to the call that relies on it.
   */
  saveSlides(slides: readonly SliderSlide[]): Observable<StoreSettings> {
    const store = this.requireLoadedStore();
    return this.stores
      .updateSliderImages({
        ...this.storeBody(store),
        sliderImages: slides.map((slide, index) => ({priority: index, name: slide.name})),
      })
      .pipe(switchMap(() => this.loadSettings()));
  }

  /** A reference list whose absence degrades one select rather than the page. */
  private optional(source: Observable<string[]>): Observable<readonly string[]> {
    return source.pipe(catchError(() => of<string[]>([])));
  }

  /** The same, for a list that is not of strings. */
  private optionalList<T>(source: Observable<T[]>): Observable<readonly T[]> {
    return source.pipe(catchError(() => of<T[]>([])));
  }

  /** The same, for a single record whose absence costs one block rather than the page. */
  private optionalOne<T>(source: Observable<T>): Observable<T | null> {
    return source.pipe(catchError(() => of(null)));
  }

  /**
   * The store's hostnames, resolved to what a browser would type.
   *
   * `GET /allocates` is the authority. `ReadableMerchantStore.storeDomains` carries the same records
   * and is used only as a fallback: it comes off the entity, while the router's answer comes from the
   * routing map the edge actually reads, and it is the routing map that decides whether a hostname
   * works. The subdomain sorts first, then the custom domains alphabetically — the endpoint answers
   * with a `Set`, so any order it arrives in is an accident.
   */
  private domains(
    allocations: readonly ManagerStoreDomain[],
    store: ReadableMerchantStore,
  ): readonly StoreDomain[] {
    const records = allocations.length > 0 ? allocations : (store.storeDomains ?? []);
    return [...records]
      .map((record) => ({
        domain: record.domain,
        type: record.domainType === 'SUB_DOMAIN' ? ('SUB_DOMAIN' as const) : ('CUSTOM_DOMAIN' as const),
        hostname: this.hostnameOf(record),
      }))
      .sort((left, right) => {
        if (left.type !== right.type) {
          return left.type === 'SUB_DOMAIN' ? -1 : 1;
        }
        return left.domain.localeCompare(right.domain);
      });
  }

  /**
   * A stored domain as a reachable hostname.
   *
   * A custom domain is stored whole. A subdomain is stored as its label alone and is served at
   * `{label}.{alis}-{pod}.{apex}` — seller-ui's `generateDomain()`, which is the only place that rule
   * was ever written down. Without the pod target there is no hostname to give, and `null` says so.
   */
  private hostnameOf(record: ManagerStoreDomain): string | null {
    if (record.domainType !== 'SUB_DOMAIN') {
      return record.domain;
    }
    return this.podTarget ? `${record.domain}.${this.podTarget}` : null;
  }

  /**
   * One row per provider the platform supports, whether or not the store has filled it in.
   *
   * The row list is the server's provider enum rather than the store's saved links: a provider with no
   * link still needs a field to type one into. A provider the store has a link for but the enum no
   * longer lists is kept as well, so an existing link is never silently dropped from the form — and
   * therefore never silently cleared by the next save, which sends the whole set.
   */
  private socialLinks(
    store: ReadableMerchantStore,
    providers: readonly string[],
  ): readonly SocialLinkSetting[] {
    const saved = new Map((store.socialLinks ?? []).map((link) => [link.provider, link.url]));
    const known = providers.length > 0 ? providers : [...SOCIAL_LINK_ORDER];
    const all = [...new Set([...known, ...saved.keys()])];
    return all.map((provider) => ({
      provider,
      icon: isSocialLinkProvider(provider) ? SOCIAL_LINK_ICON[provider] : SOCIAL_LINK_FALLBACK_ICON,
      url: saved.get(provider) ?? '',
    }));
  }

  /** The carousel, in priority order. `name` is the pod's UUID for the file, not a title. */
  private slides(store: ReadableMerchantStore): readonly SliderSlide[] {
    return [...(store.sliderImages ?? [])]
      .sort((left, right) => left.priority - right.priority)
      .map((slide) => ({priority: slide.priority, name: slide.name, url: slide.url ?? null}));
  }

  /**
   * The social-links save body.
   *
   * `PUT /private/store/social-links` takes a whole `PersistableMerchantStore` and reads only
   * `getSocialLinks()` off it, so the rest is carried through from the loaded store rather than
   * invented. An empty field is dropped rather than sent as `{provider, url: ''}` — the set is the
   * links that exist, and a link to nowhere is not one.
   */
  private socialLinksBody(patch: SectionPatch): PersistableMerchantStore {
    const store = this.requireLoadedStore();
    const links = Object.entries(patch)
      .map(([provider, url]) => ({provider, url: this.text(url, '').trim()}))
      .filter((link) => link.url.length > 0);
    return {...this.storeBody(store), socialLinks: links};
  }

  /**
   * The parts of the store every marketing save has to carry unchanged.
   *
   * All three of these endpoints take the same store-shaped body, and the populator behind the plain
   * update reads every field off it — so a body missing `name` or `email` would blank them if the
   * server ever routed one of these through it. Sending the loaded values costs nothing and removes
   * the question.
   */
  private storeBody(store: ReadableMerchantStore): PersistableMerchantStore {
    return {
      id: store.id,
      org: store.org,
      name: store.name,
      email: store.email,
      phone: store.phone,
      currency: store.currency,
      defaultLanguage: store.defaultLanguage,
      supportedLanguages: store.supportedLanguages,
      countryIsoCode: store.countryIsoCode,
      address: store.address,
      socialLinks: store.socialLinks,
      sliderImages: store.sliderImages?.map((slide) => ({priority: slide.priority, name: slide.name})),
      storeDomains: store.storeDomains,
    };
  }

  private requireLoadedStore(): ReadableMerchantStore {
    if (!this.loadedStore) {
      throw new Error('Cannot save store settings before the store has loaded.');
    }
    return this.loadedStore;
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
      storeDomains: store.storeDomains,
      socialLinks: store.socialLinks,
      sliderImages: store.sliderImages?.map((slide) => ({priority: slide.priority, name: slide.name})),

      name: this.text(patch['name'], store.name ?? ''),
      email: this.text(patch['supportEmail'], store.email ?? ''),
      phone: this.text(patch['supportPhone'], store.phone ?? ''),
      currency: this.text(patch['currency'], store.currency ?? ''),
      defaultLanguage: this.text(patch['language'], store.defaultLanguage ?? ''),
      /*
       * Sent whole, and the server takes it as an addition rather than a replacement: the populator
       * does `target.getLanguages().add(...)` per entry and never removes. Ticking a language on
       * works; ticking one off is accepted and silently ignored — which is why the section says so
       * next to the field rather than letting the operator find out from the next reload. See
       * lessons.md, "Store management — a supported language can be added but never removed".
       */
      supportedLanguages: this.codes(patch['supportedLanguages'], store.supportedLanguages ?? []),
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
      case 'domain':
      case 'social':
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

  private slice(raw: unknown): Readonly<Record<string, unknown>> {
    return typeof raw === 'object' && raw !== null ? (raw as Record<string, unknown>) : {};
  }

  private text(raw: unknown, fallback: string): string {
    return typeof raw === 'string' ? raw : fallback;
  }

  private flag(raw: unknown, fallback: boolean): boolean {
    return typeof raw === 'boolean' ? raw : fallback;
  }

  /** A list of codes off a form control, narrowed to strings — `supportedLanguages` is the only one. */
  private codes(raw: unknown, fallback: readonly string[]): string[] {
    return Array.isArray(raw) ? raw.filter((entry): entry is string => typeof entry === 'string') : [...fallback];
  }
}
