import {Injectable} from '@angular/core';
import {ConfigService} from './config.service';
import {Language} from '../models/Language';
import {CookieService} from "ngx-cookie-service";

@Injectable({
  providedIn: 'root'
})
export class SelectedLanguageService {
  public static LANGUAGE_ID_KEY = "lang";
  private _languages: Language[] = [];

  constructor(private configService: ConfigService, private cookieService: CookieService) {
    this._languages = this.configService.getListOfGlobalLanguages();
    if (this.current() == undefined || this.current() == '') {
      this.select(this._languages[0].code)
    }
  }

  public select(lang: string): void {
    this.cookieService.set(SelectedLanguageService.LANGUAGE_ID_KEY, lang);
  }

  current(): string | undefined {
    return this.cookieService.get(SelectedLanguageService.LANGUAGE_ID_KEY);
  }

  languages(): string[] {
    return this._languages.map(l => l.code);
  }
}
