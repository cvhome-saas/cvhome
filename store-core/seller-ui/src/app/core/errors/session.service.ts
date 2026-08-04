import {inject, Injectable, PLATFORM_ID} from '@angular/core';
import {isPlatformBrowser} from '@angular/common';
import {environment} from '../../../environments/environment';
import {ApiError} from './api-error';

/**
 * Owns the one thing about errors that is genuinely global: an expired session.
 *
 * Shared by the interceptor and the route guard so there is a single redirect, with a one-shot latch. A
 * dashboard fires several requests in parallel, and a session expiring mid-flight yields N simultaneous
 * 401s — without the latch that is N assignments to `window.location.href`, which browsers handle by
 * cancelling navigations and can leave the page in limbo.
 *
 * Deliberately says nothing about 403. A seller who lacks a permission is authenticated; sending them to a
 * login they are already past is a loop, not a fix.
 */
@Injectable({providedIn: 'root'})
export class SessionService {

  private readonly platformId = inject(PLATFORM_ID);

  private redirecting = false;

  /**
   * @param error the failure that revealed the expiry, logged so the redirect is traceable
   * @param returnTo where to come back to; defaults to the current URL
   */
  onUnauthenticated(error?: ApiError, returnTo?: string): void {
    // SSR has no window and no session to expire; touching either would break the server build.
    if (!isPlatformBrowser(this.platformId) || this.redirecting) {
      return;
    }
    this.redirecting = true;

    if (error) {
      console.warn('Session expired, redirecting to login', error.toLogContext());
    }

    const target = returnTo ?? window.location.pathname + window.location.search;
    window.location.href = `${environment.LOGIN_URL}?redirectTo=${encodeURIComponent(target)}`;
  }

  /** Test seam: the latch is process-wide by design, so a spec needs a way to release it. */
  reset(): void {
    this.redirecting = false;
  }
}
