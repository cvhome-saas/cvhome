import {Injectable} from '@angular/core';
import {Observable, concat, delay, of, throwError} from 'rxjs';

import {STORE_SETTINGS} from '@mocks/store-settings.fixture';
import type {
  DomainStatus,
  HomePageCopy,
  LocaleCode,
  SettingsSectionKey,
  StoreDetails,
  StoreSettings,
} from '@models/store-settings';

/** Round-trip the mock pretends to take, so loading states are actually exercised. */
const MIN_LATENCY_MS = 450;
const MAX_LATENCY_MS = 900;

/**
 * Fraction of requests that fail, for exercising the error path by hand. Kept at 0 so the
 * app is predictable; the error path is covered by a failing stub in the spec instead.
 */
const FAILURE_RATE = 0;

/** How long a DNS check pretends to take before it answers. */
const VERIFY_MS = 1200;

/** What one section sends back. Keys are the section's own form controls. */
export type SectionPatch = Readonly<Record<string, unknown>>;

/**
 * The store's settings.
 *
 * Stands in for the merchant, cua and payment endpoints: it takes time, and it holds the
 * mutated settings between calls, so the page is written against the same contract a server
 * would satisfy — save returns the whole document, not the patch it was given.
 */
@Injectable({providedIn: 'root'})
export class StoreSettingsApi {
  /**
   * The stored document. Mutable because saving has to stick for the session — reloading
   * the page after a save must show what was saved, not the fixture.
   */
  private settings: StoreSettings = STORE_SETTINGS;

  /**
   * Checks made against the custom domain so far.
   *
   * A real check answers from DNS. This one answers "not there yet" the first time and
   * "verified" after that, so both outcomes are reachable from the *Verify* button without
   * depending on a coin flip.
   */
  private verifyAttempts = 0;

  loadSettings(): Observable<StoreSettings> {
    return this.respond(() => this.settings);
  }

  /**
   * Applies one section's form value and answers with the whole document.
   *
   * Merging here rather than in the page keeps the facade honest: it sends what the operator
   * typed and re-reads the result, exactly as it will against HTTP.
   */
  saveSection(key: SettingsSectionKey, patch: SectionPatch): Observable<StoreSettings> {
    return this.respond(() => {
      this.settings = this.merge(key, patch);
      return this.settings;
    });
  }

  /**
   * Walks a domain check through its visible states: `checking` first, then whatever the
   * lookup found. Both emissions are real values on one stream, so the panel needs no
   * timer of its own.
   */
  verifyDomain(domain: string): Observable<DomainStatus> {
    if (!domain) {
      return of<DomainStatus>('unverified');
    }
    this.verifyAttempts += 1;
    const outcome: DomainStatus = this.verifyAttempts > 1 ? 'verified' : 'waiting';

    return concat(
      of<DomainStatus>('checking'),
      of(outcome).pipe(delay(VERIFY_MS)),
    );
  }

  /** Latency and the failure knob, in one place, so every call behaves the same way. */
  private respond(produce: () => StoreSettings): Observable<StoreSettings> {
    const latency = MIN_LATENCY_MS + Math.random() * (MAX_LATENCY_MS - MIN_LATENCY_MS);

    if (Math.random() < FAILURE_RATE) {
      return throwError(() => new Error('Unable to reach store settings.')).pipe(delay(latency));
    }

    return of(produce()).pipe(delay(latency));
  }

  /**
   * Folds a section's form value into the document.
   *
   * Each branch reads only the controls its own section owns, which is what keeps the form
   * shape and the DTO shape checkable against each other in one place.
   */
  private merge(key: SettingsSectionKey, patch: SectionPatch): StoreSettings {
    const current = this.settings;

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

      case 'details': {
        // `storeName` is the same fact as `details.name` — the header would otherwise keep
        // naming the store by whatever it was called before the rename.
        const details = {...current.details, ...this.detailsPatch(patch)};
        return {...current, storeName: details.name, details};
      }

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

      /*
       * Branding and the slider are uploads and reordering, which have no endpoint yet. Their
       * forms hold nothing, so there is nothing to fold in — the page's toasts say so.
       */
      case 'branding':
      case 'slider':
        return current;
    }
  }

  /** Every locale the form carries copy for, whether or not the fixture shipped one. */
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
      /*
       * An empty language stays absent rather than becoming an empty translation, so the
       * section's "untranslated languages fall back to English" notice keeps telling the truth.
       */
      if (copy.title || copy.text || copy.metaDescription || copy.tags.length > 0) {
        home[code as LocaleCode] = copy;
      }
    }

    return home;
  }

  /**
   * The custom domain is the only editable one — the subdomain is issued by the platform.
   * Retyping it drops the verification, because a new host has never been checked.
   */
  private domainsPatch(patch: SectionPatch): StoreSettings['domains'] {
    const typed = this.text(patch['customDomain'], '');

    return this.settings.domains.map((entry) => {
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

  private detailsPatch(patch: SectionPatch): Partial<StoreDetails> {
    const current = this.settings.details;
    return {
      name: this.text(patch['name'], current.name),
      legalName: this.text(patch['legalName'], current.legalName),
      slug: this.text(patch['slug'], current.slug),
      category: this.text(patch['category'], current.category),
      supportEmail: this.text(patch['supportEmail'], current.supportEmail),
      supportPhone: this.text(patch['supportPhone'], current.supportPhone),
      currency: this.text(patch['currency'], current.currency),
      language: this.text(patch['language'], current.language),
      timezone: this.text(patch['timezone'], current.timezone),
      taxNumber: this.text(patch['taxNumber'], current.taxNumber),
      address: this.text(patch['address'], current.address),
      shortDescription: this.text(patch['shortDescription'], current.shortDescription),
      published: this.flag(patch['published'], current.published),
      maintenanceMode: this.flag(patch['maintenanceMode'], current.maintenanceMode),
    };
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
