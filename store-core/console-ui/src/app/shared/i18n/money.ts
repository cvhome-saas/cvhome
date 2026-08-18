import {Injectable, inject} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import type {OrderTotal} from '@models/checkout';

/**
 * An amount, in the reader's locale.
 *
 * Amounts were formatted with a bare `Intl.NumberFormat(undefined, …)`, which resolves to the
 * *browser's* locale rather than the console's: an operator reading the console in Arabic still got
 * English digit grouping and symbol placement, because nothing told `Intl` otherwise. This reads
 * the active language, so a total is written the way the rest of the page is.
 *
 * The server's own `text` still wins where it exists — it is the store's formatting decision, and
 * on the running stack it is null on every total, which is why any of this is needed.
 */
@Injectable({providedIn: 'root'})
export class Money {
  private readonly transloco = inject(TranslocoService);
  private readonly locale = inject(TranslocoLocaleService);

  /** Reads `activeLang` so a caller's `computed` re-runs when the language changes. */
  format(value: number | null | undefined, currency: string | null | undefined, text?: string | null): string {
    this.transloco.activeLang();
    if (text) {
      return text;
    }
    if (value === undefined || value === null) {
      return '—';
    }
    if (!currency) {
      return this.locale.localizeNumber(value, 'decimal');
    }
    try {
      return this.locale.localizeNumber(value, 'currency', undefined, {currency});
    } catch {
      // An unknown ISO code would otherwise throw and take the page with it.
      return `${currency} ${value}`;
    }
  }

  /** The same, for a total straight off an order. */
  total(total: OrderTotal | undefined, currency: string | null | undefined): string {
    return this.format(total?.value, currency, total?.text);
  }
}
