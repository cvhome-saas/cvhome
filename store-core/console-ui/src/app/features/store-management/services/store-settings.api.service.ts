import {Injectable, inject} from '@angular/core';
import {Observable, catchError, forkJoin, map, of, switchMap} from 'rxjs';

import {DnsCheckService, type CnameOutcome} from '@api/dns/dns-check.service';
import {MerchantRouterService} from '@api/merchant/router.service';
import {MerchantStoreService} from '@api/merchant/store.service';
import {PaymentConfigurationService} from '@api/payment/payment-configuration.service';
import {ManagerStoreService} from '@api/tenancy/manager-store.service';
import {SaasService, podHostname} from '@api/tenancy/saas.service';
import type {
  PersistablePaymentConfiguration,
  ReadablePaymentConfiguration,
} from '@models/payment';
import type {
  ManagerStoreDomain,
  PersistableMerchantStore,
  ReadableMerchantStore,
  StoreAddress,
} from '@models/merchant';
import {
  PAYMENT_TYPES_WITHOUT_CREDENTIALS,
  PAYMENT_TYPE_ICON,
  isPaymentType,
  type DomainStatus,
  type SettingsChoices,
  type SettingsSectionKey,
  type PaymentGatewayConfig,
  type StoreDetails,
  type StoreDomain,
  type StoreSettings,
} from '@models/store-settings';

/** What one section sends back. Keys are the section's own form controls. */
export type SectionPatch = Readonly<Record<string, unknown>>;

/**
 * A reference list that is guaranteed to contain what the store is already set to.
 *
 * A server list is what may be *chosen*, which is not the same as what has been chosen: values get
 * retired, and a lookup can fail outright. Either way a select whose options omit its own value shows
 * nothing, so the current value is appended when the list does not already carry it.
 */
function withPresent(options: readonly string[], current: string | undefined): readonly string[] {
  if (!current || options.includes(current)) {
    return options;
  }
  return [...options, current];
}

/**
 * What a CNAME lookup means for the panel.
 *
 * `no-record` and `no-such-domain` are both "not there yet" rather than wrong: a record that has been
 * added seconds ago looks exactly like one that never will be, and telling the operator their DNS is
 * broken while it propagates is the unhelpful reading.
 */

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
 * **Every section is live.** `store-settings.fixture` is gone, and so is the seam that used
 * to hold it — `saveSection` dispatches to a real endpoint for every key it accepts.
 */
@Injectable({providedIn: 'root'})
export class StoreSettingsApi {
  private readonly stores = inject(MerchantStoreService);
  private readonly tenancy = inject(ManagerStoreService);
  private readonly router = inject(MerchantRouterService);
  private readonly saas = inject(SaasService);
  private readonly dns = inject(DnsCheckService);
  private readonly payments = inject(PaymentConfigurationService);

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
   * The landing box the server last sent.
   *
   * A save has to send every language the box holds and every field on each description, not just
   * the three the console edits — `buildDescriptions` replaces the description it matches by
   * language, so a field left out is a field cleared. This is where the rest comes from.
   */

  /**
   * The payment configurations the server last sent.
   *
   * A save has to know which gateways already have a row, because `POST` and `PUT` are different
   * endpoints with different semantics and there is no upsert. Held rather than re-fetched.
   */
  private loadedPayments: readonly ReadablePaymentConfiguration[] = [];

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
      /*
       * Two legs on another pod, optional for the same reason as the rest: payment being down is not
       * a reason the store's own details cannot be edited.
       */
      paymentTypes: this.optional(this.payments.supportedTypes()),
      paymentConfigs: this.optionalList(this.payments.configs()),
    }).pipe(
      map((loaded) => {
        const {store, themes, colorThemes, socialLinkProviders, languages} = loaded;
        const {allocations, saas, pod} = loaded;
        const {paymentTypes, paymentConfigs} = loaded;
        this.loadedStore = store;
        this.loadedPayments = paymentConfigs;
        this.podTarget = podHostname(saas, pod);

        /*
         * The store's own address, which several sections need and only this one can work out. The
         * subdomain is the storefront; a custom domain is an alias to it, so the subdomain is what
         * a provider's allow-list should hold.
         */
        const domains = this.domains(allocations, store);
        const storefront = domains.find((entry) => entry.type === 'SUB_DOMAIN')?.hostname ?? null;
        const choices: SettingsChoices = {
          /*
           * The server offers only `Theme.getImplementedThemes()`, which deliberately omits the legacy
           * values (BASIS, MODERN, ELECTRONICS…) that all render the same storefront as DEFAULT. A store
           * already set to one of those must still show what it is on, so its current theme joins the
           * list — the same reason `languages` falls back below.
           */
          themes: withPresent(themes, store.theme),
          colorThemes: withPresent(colorThemes, store.colorTheme),
          socialLinkProviders,
          /*
           * The store's own supported languages, falling back to whatever it is already set to —
           * a select whose only option is the current value beats an empty one.
           */
          languages: languages.length > 0 ? languages : this.presentLanguages(store),
        };
        return {
          storeName: store.name,
          details: this.details(store),
          domains,
          podTarget: this.podTarget,
          payments: this.paymentGateways(paymentTypes, paymentConfigs, store.id, storefront),
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

      /*
       * One call per gateway, and which call depends on whether it already has a row: `POST` builds
       * a fresh entity, `PUT` merges into the stored one and 404s when there is nothing to merge
       * into. Sent together and waited on together, so a partial failure still reloads the truth.
       */
      case 'payments': {
        const writes = this.paymentWrites(patch);
        const applied: Observable<unknown> = writes.length > 0 ? forkJoin(writes) : of(null);
        return applied.pipe(switchMap(() => this.loadSettings()));
      }
    }
  }

  /** Removes the store. There is no undo, which is why the section asks the operator to type its name. */
  deleteStore(): Observable<void> {
    return this.stores.delete();
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

  /**
   * Where a gateway should post its events.
   *
   * Assembled rather than served, like the social-login callback and for the same reason: the route
   * exists, nothing hands it out. `PublicPaymentWebhookApi` maps
   * `POST /api/v1/public/webhook/{storeId}/{paymentType}`, and the pod's Caddyfile reaches the
   * payment service through `handle_path /payment*` — `handle_path`, which **strips** the segment,
   * so the prefix goes back on here. The payment type is the enum's own name in upper case, because
   * the controller binds it as a `PaymentType`.
   *
   * Answers the path alone when the storefront host is unknown: a fragment an operator can still
   * recognise beats a URL built on a guess.
   */
  private webhookUrl(storefront: string | null, storeId: string, paymentType: string): string {
    const path = `/payment/api/v1/public/webhook/${storeId}/${paymentType}`;
    return storefront ? `https://${storefront}${path}` : path;
  }

  /**
   * One card per payment type the platform declares, whether or not this store has configured it.
   *
   * `credentials` is `null` for the types whose `PaymentType.attrs` is empty — `COD` and
   * `MANUAL_TRANSFER` are a switch and nothing else — and that list is the console's, because the
   * attrs themselves are not on the wire. See lessons.md, "Store management — a payment type's
   * required attributes are not served".
   */
  private paymentGateways(
    types: readonly string[],
    configs: readonly ReadablePaymentConfiguration[],
    storeId: string,
    storefront: string | null,
  ): readonly PaymentGatewayConfig[] {
    const stored = new Map(configs.map((config) => [config.paymentType, config]));
    const known = types.length > 0 ? types : [...stored.keys()];

    return known.map((paymentType) => {
      const config = stored.get(paymentType);
      const carriesCredentials = !PAYMENT_TYPES_WITHOUT_CREDENTIALS.includes(paymentType);
      return {
        paymentType,
        icon: isPaymentType(paymentType) ? PAYMENT_TYPE_ICON[paymentType] : 'creditCard',
        enabled: config?.enabled ?? false,
        credentials: carriesCredentials
          ? {
              apiKey: config?.apiKey ?? '',
              secretKey: config?.secretKey ?? '',
              webhookSecret: config?.webhookSecret ?? '',
              webhookUrl: this.webhookUrl(storefront, storeId, paymentType),
            }
          : null,
        configured: config !== undefined,
      };
    });
  }

  /**
   * One write per gateway the operator changed, as create or update.
   *
   * Only the changed ones: sending all four would create empty rows for gateways the store has
   * never configured, and an empty `COD` row is indistinguishable from a configured one afterwards.
   * The comparison is against what the load returned, which is why the gateways are kept.
   */
  private paymentWrites(patch: SectionPatch): Observable<void>[] {
    const stored = new Map(this.loadedPayments.map((config) => [config.paymentType, config]));

    return Object.entries(patch).flatMap(([paymentType, raw]) => {
      const slice = this.slice(raw);
      const previous = stored.get(paymentType);
      const body: PersistablePaymentConfiguration = {
        paymentType,
        enabled: this.flag(slice['enabled'], false),
        apiKey: this.text(slice['apiKey'], ''),
        secretKey: this.text(slice['secretKey'], ''),
        webhookSecret: this.text(slice['webhookSecret'], ''),
      };

      if (!previous) {
        // Never configured, and still nothing to say: creating an empty row would be noise.
        const untouched =
          !body.enabled && !body.apiKey && !body.secretKey && !body.webhookSecret;
        return untouched ? [] : [this.payments.create(body)];
      }
      const unchanged =
        previous.enabled === body.enabled &&
        (previous.apiKey ?? '') === body.apiKey &&
        (previous.secretKey ?? '') === body.secretKey &&
        (previous.webhookSecret ?? '') === body.webhookSecret;
      return unchanged ? [] : [this.payments.update(paymentType, body)];
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

  /** A list of codes off a form control, narrowed to strings — `supportedLanguages` is the only one. */
  private codes(raw: unknown, fallback: readonly string[]): string[] {
    return Array.isArray(raw) ? raw.filter((entry): entry is string => typeof entry === 'string') : [...fallback];
  }
}
