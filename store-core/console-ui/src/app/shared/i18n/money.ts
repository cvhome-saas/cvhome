import {Injectable, inject} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import type {OrderTotal} from '@models/checkout';

/** How the currency is written beside the figure. Mirrors `Intl.NumberFormatOptions`. */
export type CurrencyDisplay = 'symbol' | 'narrowSymbol' | 'code' | 'name';

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
  format(
    value: number | null | undefined,
    currency: string | null | undefined,
    text?: string | null,
    display: CurrencyDisplay = 'symbol',
  ): string {
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
      return this.locale.localizeNumber(value, 'currency', undefined, {
        currency,
        currencyDisplay: display,
      });
    } catch {
      // An unknown ISO code would otherwise throw and take the page with it.
      return `${currency} ${value}`;
    }
  }

  /**
   * An amount with its currency as an ISO code — `٣٠٫٠٠ USD`, `USD 30.00`.
   *
   * For account-level money: plan prices and invoices. The default `symbol` display is right for a
   * storefront figure but wrong here for two reasons. It does not localize — Arabic renders USD as
   * `US$` and GBP as `UK£`, Latin script stranded in a right-to-left line, and `narrowSymbol` gives
   * the same. And a bare `$` is ambiguous on a platform whose whole premise is trading in several
   * markets at once.
   *
   * The code is also already this product's currency vocabulary: the merchant picked it from a
   * select that reads `SAR · Saudi Riyal`, so it is the form they chose it in.
   */
  account(value: number | null | undefined, currency: string | null | undefined): string {
    return this.format(value, currency, null, 'code');
  }

  /** The same, for a total straight off an order. */
  total(total: OrderTotal | undefined, currency: string | null | undefined): string {
    return this.format(total?.value, currency, total?.text);
  }
}
