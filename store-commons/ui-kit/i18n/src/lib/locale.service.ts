import {DOCUMENT} from '@angular/common';
import {effect, inject, Injectable} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {TranslocoService} from '@jsverse/transloco';

// Declared in `@models/locale` so a model may name a locale without importing upward; re-exported
// here because this is where the service that switches them lives and where callers look.
import {CONSOLE_LOCALES, type ConsoleLocale, type LocaleCode} from '@cvhome-saas/ui-kit';

export type {ConsoleLocale, LocaleCode};
export {CONSOLE_LOCALES};

function localeOf(code: string): ConsoleLocale {
  return CONSOLE_LOCALES.find((locale) => locale.code === code) ?? CONSOLE_LOCALES[0];
}

/**
 * The active language and the document attributes that follow it.
 *
 * Switching is instant — `select()` hands off to Transloco, which re-renders every
 * `transloco` pipe/directive in place. There is no navigation and no bundle swap, unlike
 * the old build-time `/ar/...` subpaths.
 */
@Injectable({providedIn: 'root'})
export class LocaleService {
  private readonly document = inject(DOCUMENT);
  private readonly transloco = inject(TranslocoService);

  /** The active locale, reactive to language changes. */
  readonly current = toSignal(this.transloco.langChanges$, {
    initialValue: this.transloco.getActiveLang(),
  });

  readonly locales = CONSOLE_LOCALES;

  constructor() {
    // The pre-paint script in index.html already set these for the first render; this
    // keeps them in sync on every later switch.
    effect(() => {
      const locale = localeOf(this.current());
      this.document.documentElement.lang = locale.code;
      this.document.documentElement.dir = locale.dir;
    });
  }

  currentLocale(): ConsoleLocale {
    return localeOf(this.current());
  }

  /**
   * `TranslocoService.setActiveLang()` only flips which language is active — it does not
   * load it. Every `*transloco` directive on screen happens to trigger its own load as a
   * side effect of `langChanges$`, which is why the switch usually looks instant, but
   * anything that translates imperatively (`transloco.translate()` inside a `computed()`,
   * as the console's facades do) can run before that load resolves. A `computed()` that
   * throws mid-evaluation never retries once its only tracked dependency (`activeLang()`)
   * stops changing, so a call to `translate()` during the gap doesn't just show a flash of
   * missing text — the facade holding it stays stuck until the next full navigation.
   * Loading before switching closes the gap: nothing observes the new language until its
   * dictionary is already in place.
   */
  select(code: LocaleCode): void {
    this.transloco.load(code).subscribe({
      next: () => this.transloco.setActiveLang(code),
      error: () => this.transloco.setActiveLang(code),
    });
  }
}
