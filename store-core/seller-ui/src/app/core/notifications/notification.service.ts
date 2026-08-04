import {inject, Injectable} from '@angular/core';
import {NbGlobalPhysicalPosition, NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';

/** Extra context for a toast. `duration` is milliseconds; 0 keeps it until dismissed. */
export interface NotificationOptions {
  readonly duration?: number;
  readonly title?: string;
}

/**
 * The one wrapper around `NbToastrService`.
 *
 * Nebular is the only toast stack that actually works here: `app.config.ts` provides
 * `NbToastrModule.forRoot()`, nothing provides ngx-toastr's `ToastrModule.forRoot()`, and
 * `ngx-toastr/toastr.css` is imported in no stylesheet — so the ngx-toastr path throws
 * `NullInjectorError` the moment it is used. Nebular also inherits the app theme and its RTL handling,
 * which matters because Arabic ships.
 *
 * The asymmetry in these signatures is deliberate:
 *
 * - `success` / `info` / `warning` take an **i18n key**, matching the 60 existing `.success('X.Y')` calls
 *   verbatim so they migrate without being rewritten.
 * - `danger` takes an **already-resolved string**, because `ApiErrorService` owns the code → category →
 *   generic resolution chain and nothing else should be building error copy.
 */
@Injectable({providedIn: 'root'})
export class NotificationService {

  private readonly toastr = inject(NbToastrService);

  private readonly translate = inject(TranslateService);

  /** @param key an i18n key, not a message */
  success(key: string, params?: Record<string, unknown>, options?: NotificationOptions): void {
    this.toastr.success(this.translate.instant(key, params), options?.title, this.config(options));
  }

  /** @param key an i18n key, not a message */
  info(key: string, params?: Record<string, unknown>, options?: NotificationOptions): void {
    this.toastr.info(this.translate.instant(key, params), options?.title, this.config(options));
  }

  /** @param key an i18n key, not a message */
  warning(key: string, params?: Record<string, unknown>, options?: NotificationOptions): void {
    this.toastr.warning(this.translate.instant(key, params), options?.title, this.config(options));
  }

  /**
   * @param message an already-translated message. Call this through `ApiErrorService.notify` rather than
   *                directly, so every error the seller reads has gone through the same chain.
   */
  danger(message: string, options?: NotificationOptions): void {
    this.toastr.danger(message, options?.title, this.config(options));
  }

  /**
   * Red toast for a message this app owns, not one a backend sent — "fill the required fields", "that code
   * is already used". Kept separate from {@link danger} so the distinction stays visible: `danger` takes
   * resolved text because only `ApiErrorService` may build error copy from a response.
   *
   * @param key an i18n key, not a message
   */
  dangerKey(key: string, params?: Record<string, unknown>, options?: NotificationOptions): void {
    this.toastr.danger(this.translate.instant(key, params), options?.title, this.config(options));
  }

  private config(options?: NotificationOptions) {
    return {
      duration: options?.duration ?? 5000,
      position: NbGlobalPhysicalPosition.TOP_RIGHT,
    };
  }
}
