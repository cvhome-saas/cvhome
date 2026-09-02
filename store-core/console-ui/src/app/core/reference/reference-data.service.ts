import {Injectable, computed, inject} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import type {ReferenceOption} from '@cvhome-saas/ui-kit';

/**
 * Re-exported so the call sites that already import it from here keep working. The shape itself
 * moved to the kit, where `app-locale-switcher` names it; the fetching stays here, because which
 * countries and currencies exist is an application concern.
 */
export type {ReferenceOption};

/**
 * Every ISO 3166-1 alpha-2 code currently assigned — 249 of them.
 *
 * Codes only, deliberately: the names come from `Intl.DisplayNames`, so this list does not have to
 * be translated, maintained in two languages, or re-shipped when a country is renamed. seller-ui
 * carried a 50 KB `assets/data/countries.json` of English names instead, which is why its country
 * select could only ever read English. Every code here resolves under `Intl`, and this list is a
 * superset of that file's 237.
 */
const ISO_3166_ALPHA2: readonly string[] = (
  'AD AE AF AG AI AL AM AO AQ AR AS AT AU AW AX AZ BA BB BD BE BF BG BH BI BJ BL BM BN BO BQ BR ' +
  'BS BT BV BW BY BZ CA CC CD CF CG CH CI CK CL CM CN CO CR CU CV CW CX CY CZ DE DJ DK DM DO DZ ' +
  'EC EE EG EH ER ES ET FI FJ FK FM FO FR GA GB GD GE GF GG GH GI GL GM GN GP GQ GR GS GT GU GW ' +
  'GY HK HM HN HR HT HU ID IE IL IM IN IO IQ IR IS IT JE JM JO JP KE KG KH KI KM KN KP KR KW KY ' +
  'KZ LA LB LC LI LK LR LS LT LU LV LY MA MC MD ME MF MG MH MK ML MM MN MO MP MQ MR MS MT MU MV ' +
  'MW MX MY MZ NA NC NE NF NG NI NL NO NP NR NU NZ OM PA PE PF PG PH PK PL PM PN PR PS PT PW PY ' +
  'QA RE RO RS RU RW SA SB SC SD SE SG SH SI SJ SK SL SM SN SO SR SS ST SV SX SY SZ TC TD TF TG ' +
  'TH TJ TK TL TM TN TO TR TT TV TW TZ UA UG UM US UY UZ VA VC VE VG VI VN VU WF WS YE YT ZA ZM ZW'
).split(' ');

/**
 * The languages a storefront can be published in.
 *
 * A constant because the platform offers no endpoint for it: `GET /store/languages` answers with
 * the languages *this store* has turned on, and nothing anywhere lists the ones it could turn on.
 * These five are seller-ui's `environment.client.language.array`, which was the only place the set
 * was written down. See lessons.md, "Store management — no reference lists for countries, currencies
 * or storefront languages".
 */
export const STOREFRONT_LANGUAGES: readonly string[] = ['en', 'fr', 'ar', 'es', 'ru'];

/**
 * Reference lists the platform does not serve — countries, currencies, storefront languages.
 *
 * All three are ISO registries rather than platform data, so they are built from the codes plus
 * `Intl`: the browser already ships the names, in every language it supports, and it updates them
 * with itself. The console's job is only to pick the codes.
 *
 * Every list is a `computed` over `activeLang()`, so switching the console to Arabic re-labels the
 * selects in place — which a shipped JSON of English names cannot do.
 *
 * Not to be confused with `OrdersService.countries()`, which answers with the countries a *store
 * ships to* — a handful, and a different question. See lessons.md, "Orders — checkout's country
 * list is the store's supported set".
 */
@Injectable({providedIn: 'root'})
export class ReferenceDataService {
  private readonly transloco = inject(TranslocoService);

  /** Every country, by name in the reader's language. */
  readonly countries = computed<readonly ReferenceOption[]>(() =>
    this.byLabel(this.named(ISO_3166_ALPHA2, 'region')),
  );

  /**
   * Every ISO 4217 currency, as `SAR · Saudi Riyal`.
   *
   * Code-first and ordered by code, because a merchant knows the code and is scanning for it —
   * the name is confirmation. Countries are the other way round for the same reason.
   */
  readonly currencies = computed<readonly ReferenceOption[]>(() =>
    currencyCodes().map((code) => ({code, label: `${code} · ${this.name(code, 'currency')}`})),
  );

  /** The languages a storefront may be published in, by name in the reader's language. */
  readonly storefrontLanguages = computed<readonly ReferenceOption[]>(() =>
    this.byLabel(this.named(STOREFRONT_LANGUAGES, 'language')),
  );

  /** One language code as a name — for a code outside `STOREFRONT_LANGUAGES`. */
  languageName(code: string): string {
    return this.name(code, 'language');
  }

  /** The language these lists are named in, for a caller building its own `Intl.Collator`. */
  activeLang(): string {
    return this.lang();
  }

  /**
   * A list with the stored value folded in.
   *
   * A store may hold a code the registry no longer lists, and a select whose options omit the value
   * bound to it silently shows something other than what is stored. The stored code is kept — named
   * if `Intl` can name it, bare if it cannot — rather than the field lying about what it holds.
   */
  withCurrent(options: readonly ReferenceOption[], current: string): readonly ReferenceOption[] {
    if (!current || options.some((option) => option.code === current)) {
      return options;
    }
    return [{code: current, label: current}, ...options];
  }

  private named(codes: readonly string[], type: 'region' | 'language'): ReferenceOption[] {
    return codes.map((code) => ({code, label: this.name(code, type)}));
  }

  private byLabel(options: ReferenceOption[]): readonly ReferenceOption[] {
    const collator = new Intl.Collator(this.lang());
    return options.sort((left, right) => collator.compare(left.label, right.label));
  }

  /** `Intl.DisplayNames` throws on a malformed code and returns nothing for an unknown one; both fall back to the code, which is still the truth. */
  private name(code: string, type: 'region' | 'language' | 'currency'): string {
    try {
      return new Intl.DisplayNames([this.lang()], {type}).of(code) ?? code;
    } catch {
      return code;
    }
  }

  private lang(): string {
    return this.transloco.activeLang();
  }
}

/**
 * The currency codes `Intl` knows about.
 *
 * `supportedValuesOf` has been in every browser the console targets since 2022, but it is a lookup
 * on the runtime rather than a language feature, so a failure is answered with an empty list and
 * `withCurrent` still shows the store what it is set to.
 */
function currencyCodes(): readonly string[] {
  try {
    return Intl.supportedValuesOf('currency');
  } catch {
    return [];
  }
}
