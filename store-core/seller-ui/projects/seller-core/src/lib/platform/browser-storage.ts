import {isPlatformBrowser} from '@angular/common';
import {inject, Injectable, PLATFORM_ID} from '@angular/core';

@Injectable({providedIn: 'root'})
export class BrowserStorage {
  private readonly platformId = inject(PLATFORM_ID);
  getItem(key: string): string | null { return isPlatformBrowser(this.platformId) ? localStorage.getItem(key) : null; }
  setItem(key: string, value: string): void { if (isPlatformBrowser(this.platformId)) localStorage.setItem(key, value); }
}
