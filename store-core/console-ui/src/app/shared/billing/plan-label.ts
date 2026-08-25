import {Injectable, inject} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

/**
 * The plan codes the console has words for.
 *
 * A list rather than a blind `translate()`, because the missing-key handler is strict: a plan
 * billing adds tomorrow would otherwise throw on render. Unknown codes fall back to the server's
 * English display name, which is wrong-language but readable — the failure mode a catalogue that
 * can grow at runtime has to have.
 */
const TRANSLATED_PLAN_CODES: readonly string[] = ['FREE', 'BASIC', 'PRO'];

/**
 * Names a plan in the reader's language.
 *
 * Its own service rather than a method on `SubscriptionFacade`, because naming a plan has nothing to do
 * with a store's subscription — and because the plan dialog needs it. Reaching for the facade there
 * pulled the console shell, the store directory and the tenancy client into a presentational dialog;
 * this needs Transloco and nothing else.
 *
 * Billing's `displayName` is authored once, in English, and is the same string for every locale — so
 * an Arabic console read "This store is on Free". The catalogue's `code` is the stable handle, so
 * the console translates on that.
 */
@Injectable({providedIn: 'root'})
export class PlanLabel {
  private readonly transloco = inject(TranslocoService);

  /** Reads `activeLang` so a caller's `computed` re-runs when the language changes. */
  of(code: string | null | undefined, displayName: string | null | undefined): string {
    this.transloco.activeLang();
    if (code && TRANSLATED_PLAN_CODES.includes(code)) {
      return this.transloco.translate(`billing.planName.${code}`);
    }
    return displayName ?? code ?? '—';
  }
}
