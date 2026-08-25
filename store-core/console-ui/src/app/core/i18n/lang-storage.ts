import {DOCUMENT, isPlatformBrowser} from '@angular/common';
import {inject, Injectable, InjectionToken, PLATFORM_ID, REQUEST} from '@angular/core';
import type {PersistStorage} from '@jsverse/transloco-persist-lang';

export const LANG_COOKIE = 'cvhome.console.lang';

/**
 * Where the active language is persisted. A cookie, not `localStorage`: SSR renders the
 * page in the visitor's language up front, and a client-side correction after the fact
 * would rewrite every translated text node during hydration.
 */
export const LANG_STORAGE = new InjectionToken<PersistStorage>('LANG_STORAGE');

function readCookie(cookieHeader: string, name: string): string | null {
  const match = cookieHeader.match(new RegExp(`(?:^|;\\s*)${name}=([^;]+)`));
  return match ? decodeURIComponent(match[1]) : null;
}

/** Reads/writes the language cookie on `document.cookie`. */
@Injectable({providedIn: 'root'})
export class BrowserLangStorage implements PersistStorage {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly document = inject(DOCUMENT);

  getItem(key: string): string | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }
    return readCookie(this.document.cookie, key);
  }

  setItem(key: string, value: string): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    const oneYear = 60 * 60 * 24 * 365;
    this.document.cookie = `${key}=${encodeURIComponent(value)}; Path=/; Max-Age=${oneYear}; SameSite=Lax`;
  }

  removeItem(key: string): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    this.document.cookie = `${key}=; Path=/; Max-Age=0; SameSite=Lax`;
  }
}

/**
 * Reads the language cookie off the incoming request during SSR. Writing is a no-op — the
 * response has already started by the time Transloco resolves a language, so the cookie
 * can only be set from the browser.
 */
@Injectable({providedIn: 'root'})
export class ServerLangStorage implements PersistStorage {
  private readonly request = inject(REQUEST, {optional: true});

  getItem(key: string): string | null {
    const cookieHeader = this.request?.headers.get('cookie') ?? '';
    return readCookie(cookieHeader, key);
  }

  setItem(): void {
    /* No response to attach a Set-Cookie to at this point in the render. */
  }

  removeItem(): void {
    /* See setItem. */
  }
}
