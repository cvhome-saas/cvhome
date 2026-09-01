import {TranslocoService} from '@jsverse/transloco';

import {ProblemFieldError} from './problem-detail.model';

/**
 * Turning a backend code into words, for the two places that have to do it.
 *
 * `ApiErrorService` owned this chain and still does for toasts, but the *field* message is rendered by
 * `app-field-error`, which was reading `fieldError.message` — the server's own English — straight out of the
 * problem body. So a code the console had translated still showed up untranslated wherever it mattered most:
 * bound to the control that caused it. Arabic got English.
 *
 * The obvious fix, injecting `ApiErrorService` into `app-field-error`, drags `NOTIFICATION_PORT` into every
 * spec that renders a form — fifteen of them, none of which toast anything. A plain function taking the
 * service the component already injects keeps one implementation without moving a dependency into the shared
 * control tier.
 */

/** Backend codes are dotted; translation keys are flat. `CONTENT.SLUG.DUPLICATE` → `CONTENT_SLUG_DUPLICATE`. */
export function codeToKey(code: string): string {
  return `errors.code.${code.replace(/\./g, '_')}`;
}

/**
 * The console's own words for a code, or `null` when it has none.
 *
 * Read out of the loaded bundle rather than through `translate`, because the strict missing handler throws in
 * development — and a code with no message of its own is the normal case, not a broken translation. The bundle
 * answers `{}`, or nothing at all in a spec that never loaded one, before the active language has arrived.
 */
export function translatedCode(transloco: TranslocoService, code: string,
                               params?: Record<string, unknown>): string | null {
  const key = codeToKey(code);
  const bundle: Record<string, unknown> | undefined = transloco.getTranslation(transloco.getActiveLang());
  return bundle?.[key] === undefined ? null : transloco.translate(key, params);
}

/**
 * One field error in words: our translation of its code, else the server's message.
 *
 * The server's `message` is bean validation's default text — English, and phrased for a developer — so it is
 * the fallback rather than the first choice.
 */
export function fieldErrorMessage(transloco: TranslocoService, fieldError: ProblemFieldError): string | null {
  return translatedCode(transloco, fieldError.code, fieldError.params) ?? fieldError.message ?? null;
}
