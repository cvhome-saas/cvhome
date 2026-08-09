import {Injectable, inject} from '@angular/core';
import {Language} from '../models/Language';
import {TranslateService} from '@ngx-translate/core';
import {CrudService} from "../http/crud.service";
import {SELLER_CORE_CONFIG} from '../config/seller-core.config';
import {Observable} from 'rxjs';

/** Mirrors the wire shape of List<LanguageCode> as returned by
 *  MerchantStoreApi#supportedLanguages — a record with only `code`
 *  (no custom LanguageCode serializer is applied on this endpoint). */
export interface SupportedLanguageCode {
  code: string;
}

/** Static reference data served from public/assets/data/countries.json —
 *  same shape as reference-commons' ReadableCountry/ReadableZone. */
export interface ReferenceCountry {
  id?: number;
  code?: string;
  supported?: boolean;
  name?: string;
  zones?: ReferenceZone[];
}

export interface ReferenceZone {
  id?: number;
  countryCode?: string;
  code?: string;
  name?: string;
}

/** Static reference data served from public/assets/data/currencies.json. */
export interface ReferenceCurrency {
  id?: number;
  currency?: string;
  supported?: boolean;
  code?: string;
  name?: string;
  new?: boolean;
}

/** Static reference data served from public/assets/data/weightSizes.json. */
export interface WeightAndSizes {
  weights: string[];
  measures: string[];
}


@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  private readonly crudService = inject(CrudService);
  private readonly translate = inject(TranslateService);
  private readonly config = inject(SELLER_CORE_CONFIG);

  languages = [];

  getListOfSupportedLanguages(store: string): Observable<SupportedLanguageCode[]> {
    const params = {
      'store': store
    };

    return this.crudService.get(`/spg/merchant/api/v1/store/languages`, params)
  }

  getListOfGlobalLanguages(): Language[] {
    const langs = this.config.languages.available;
    const languages: Language[] = [];
    langs.forEach(lang => {
      const l = new Language(0, lang, this.translate.instant('LANG.' + lang));
      languages.push(l);
    });

    return languages;
  }

  getListOfCountries(): Observable<ReferenceCountry[]> {
    return this.crudService.get("assets/data/countries.json");
  }

  getListOfSupportedCurrency(): Observable<ReferenceCurrency[]> {
    return this.crudService.get("assets/data/currencies.json");
  }

  getWeightAndSizes(): Observable<WeightAndSizes> {
    return this.crudService.get("assets/data/weightSizes.json");
  }
}
