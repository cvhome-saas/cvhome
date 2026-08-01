import {Injectable, inject} from '@angular/core';
import {Language} from '../models/Language';
import {TranslateService} from '@ngx-translate/core';
import {environment} from '../../../../environments/environment';
import {CrudService} from "./crud.service";
import {Observable} from 'rxjs';

/** Mirrors the wire shape of List<LanguageCode> as returned by
 *  MerchantStoreApi#supportedLanguages — a record with only `code`
 *  (no custom LanguageCode serializer is applied on this endpoint). */
export interface SupportedLanguageCode {
  code: string;
}


@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  private readonly crudService = inject(CrudService);
  private readonly translate = inject(TranslateService);

  languages = [];

  getListOfSupportedLanguages(store: string): Observable<SupportedLanguageCode[]> {
    const params = {
      'store': store
    };

    return this.crudService.get(`/spg/merchant/api/v1/store/languages`, params)
  }

  getListOfGlobalLanguages(): Language[] {
    const langs: string[] = environment.client.language.array
    const languages: Language[] = [];
    langs.forEach(lang => {
      const l = new Language(0, lang, this.translate.instant('LANG.' + lang));
      languages.push(l);
    });

    return languages;
  }

  getListOfCountries() {
    return this.crudService.get("assets/data/countries.json");
  }

  getListOfSupportedCurrency() {
    return this.crudService.get("assets/data/currencies.json");
  }

  getWeightAndSizes() {
    return this.crudService.get("assets/data/weightSizes.json");
  }
}
