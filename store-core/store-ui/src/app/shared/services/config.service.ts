import {Injectable} from '@angular/core';
import {Language} from '../models/Language';
import {TranslateService} from '@ngx-translate/core';
import {environment} from '../../../environments/environment';
import {HttpClient} from "@angular/common/http";
import {CrudService} from "./crud.service";


@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  languages = [];

  constructor(
    private httpClient: HttpClient,
    private crudService: CrudService,
    private translate: TranslateService,
  ) {
  }

  getListOfSupportedLanguages(store: string) {
    const params = {
      'store': store
    };

    return this.crudService.get(`/store-pod-gateway/merchant/api/v1/store/languages`, params)
  }

  getListOfGlobalLanguages(): Language[] {
    let langs: string[] = environment.client.language.array
    let languages: Language[] = [];
    langs.forEach(lang => {
      var l = new Language(0, lang, this.translate.instant('LANG.' + lang));
      languages.push(l);
    });

    //console.log('Global languages -> ' + JSON.stringify(languages));
    return languages;
  }

  getListOfCountries() {
    return this.httpClient.get<any[]>("assets/data/countries.json");
  }

  getListOfSupportedCurrency() {
    return this.httpClient.get<any[]>("assets/data/currencies.json");
  }

  getWeightAndSizes() {
    return this.httpClient.get<any>("assets/data/weightSizes.json");
  }
}
