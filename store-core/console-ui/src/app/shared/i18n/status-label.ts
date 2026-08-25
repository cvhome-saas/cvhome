import {Injectable, inject} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {KNOWN_STATUSES, humanizeStatus} from '@models/orders';

/**
 * A server status, in the reader's language.
 *
 * The server owns these enums — `OrderStatus`, `PaymentStatus`, `InventoryStatus` — and Transloco
 * is configured to throw on a missing key, so looking one up blind would let a value added
 * server-side take the whole page down. Every value the console knows is listed in
 * `KNOWN_STATUSES`; those are translated, and anything else is humanized from the enum name.
 *
 * The three enums share one `status.*` namespace: where they overlap — `PROCESSING`, `CANCELLED` —
 * they mean the same thing to the person reading the screen, and two keys would eventually drift
 * into two different words for it.
 */
@Injectable({providedIn: 'root'})
export class StatusLabel {
  private readonly transloco = inject(TranslocoService);

  /**
   * Reads `activeLang` so a caller's `computed` re-runs on a language change — the language is a
   * dependency of the answer, not of the call.
   */
  label(status: string | null | undefined): string {
    this.transloco.activeLang();
    if (!status) {
      return '—';
    }
    return KNOWN_STATUSES.has(status) ? this.transloco.translate(`status.${status}`) : humanizeStatus(status);
  }
}
