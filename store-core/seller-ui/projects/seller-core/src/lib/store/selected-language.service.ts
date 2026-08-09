import {Injectable, inject} from '@angular/core';
import {ConfigService} from './config.service';
import {Language} from '../models/Language';
import {BrowserStorage} from '../platform/browser-storage';

@Injectable({
  providedIn: 'root'
})
export class SelectedLanguageService {
  public static LANGUAGE_ID_KEY = "lang";
  private readonly configService = inject(ConfigService);
  private readonly storage = inject(BrowserStorage);
  private _languages: Language[] = [];

  private ensureInit(): void {
    if (this._languages.length) return;
    this._languages = this.configService.getListOfGlobalLanguages();
    if (this._languages.length && (this.readCurrent() == undefined || this.readCurrent() == '')) {
      this.select(this._languages[0].code)
    }
  }

  public select(lang: string): void {
    this.ensureInit();
    this.storage.setItem(SelectedLanguageService.LANGUAGE_ID_KEY, lang);
  }

  current(): string | undefined {
    this.ensureInit();
    return this.readCurrent();
  }

  private readCurrent(): string | undefined {
    return this.storage.getItem(SelectedLanguageService.LANGUAGE_ID_KEY) ?? undefined;
  }

  languages(): string[] {
    this.ensureInit();
    return this._languages.map(l => l.code);
  }
}
