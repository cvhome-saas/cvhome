import {Injectable, inject} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {KNOWN_TOTAL_MODULES, type OrderTotal, totalLabel} from '@models/checkout';

/**
 * A total's name — "Subtotal", "Shipping" — in the reader's language.
 *
 * The same shape as `StatusLabel`, and for the same reason. Total modules are the server's: a store
 * can add one, `title` is null on every total the running stack sends, and Transloco throws on a
 * missing key. The modules checkout itself defines (`OrderTotalType`) are translated; anything else
 * is humanized from `module` or the tail of `code`, which is what the console did for all of them
 * before this existed.
 */
@Injectable({providedIn: 'root'})
export class TotalLabel {
  private readonly transloco = inject(TranslocoService);

  /** Reads `activeLang` so a caller's `computed` re-runs on a language change. */
  label(total: OrderTotal): string {
    this.transloco.activeLang();
    // The server's own title wins: a store that named the line meant it.
    if (total.title) {
      return total.title;
    }
    const module = (total.module ?? total.code?.split('.').pop() ?? '').toLowerCase();
    return KNOWN_TOTAL_MODULES.has(module) ? this.transloco.translate(`orderTotal.${module}`) : totalLabel(total);
  }
}
