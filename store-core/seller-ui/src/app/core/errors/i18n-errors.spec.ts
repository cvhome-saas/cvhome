import ar from '../../../../public/assets/i18n/ar.json';
import en from '../../../../public/assets/i18n/en.json';
import es from '../../../../public/assets/i18n/es.json';
import fr from '../../../../public/assets/i18n/fr.json';
import ru from '../../../../public/assets/i18n/ru.json';

/**
 * The fallback chain is `ERRORS.CODE.<code>` → `ERRORS.CATEGORY.<category>` → `ERRORS.GENERIC`, and it only
 * terminates if every locale actually has those keys. A key present in `en` and missing in `ar` shows the
 * seller a raw translation key, so this spec — not review — is what keeps the five files in step.
 */

type Json = Record<string, unknown>;

const LOCALES: ReadonlyArray<readonly [string, Json]> = [
  ['en', en as Json], ['ar', ar as Json], ['es', es as Json], ['fr', fr as Json], ['ru', ru as Json],
];

function flatten(value: unknown, prefix = ''): string[] {
  if (typeof value !== 'object' || value === null) {
    return [prefix];
  }
  return Object.entries(value as Json)
    .flatMap(([key, child]) => flatten(child, prefix ? `${prefix}.${key}` : key))
    .sort();
}

/** ngx-translate's interpolation, so a message that names a param can be checked against its siblings. */
function placeholders(text: string): string[] {
  return [...text.matchAll(/\{\{\s*([\w.]+)\s*}}/g)].map(m => m[1]).sort();
}

function errorsBlock(locale: Json): Json {
  return locale['ERRORS'] as Json;
}

function leaf(locale: Json, path: string): string | undefined {
  return path.split('.').reduce<unknown>((acc, key) =>
    (typeof acc === 'object' && acc !== null) ? (acc as Json)[key] : undefined, errorsBlock(locale)) as string | undefined;
}

describe('i18n ERRORS block', () => {

  const baseline = flatten(errorsBlock(en as Json));

  it('exists in every locale', () => {
    for (const [name, locale] of LOCALES) {
      expect(errorsBlock(locale)).withContext(`${name}.json has no ERRORS block`).toBeDefined();
    }
  });

  it('has identical key sets across all five locales', () => {
    for (const [name, locale] of LOCALES) {
      const keys = flatten(errorsBlock(locale));
      expect(keys).withContext(`${name}.json differs from en.json under ERRORS`).toEqual(baseline);
    }
  });

  it('terminates the fallback chain — GENERIC is present everywhere', () => {
    for (const [name, locale] of LOCALES) {
      const generic = leaf(locale, 'GENERIC');
      expect(typeof generic).withContext(`${name}.json is missing ERRORS.GENERIC`).toBe('string');
      expect(generic!.length).withContext(`${name}.json has an empty ERRORS.GENERIC`).toBeGreaterThan(0);
    }
  });

  it('covers all 13 server categories plus the two client ones', () => {
    const expected = [
      'VALIDATION', 'MALFORMED', 'CONVERSION', 'UNAUTHENTICATED', 'FORBIDDEN', 'NOT_FOUND', 'CONFLICT',
      'PAYLOAD_TOO_LARGE', 'UNPROCESSABLE', 'STORAGE', 'INTERNAL', 'REMOTE_SERVICE', 'TIMEOUT',
      'NETWORK', 'UNKNOWN',
    ].sort();

    for (const [name, locale] of LOCALES) {
      const categories = Object.keys((errorsBlock(locale)['CATEGORY'] ?? {}) as Json).sort();
      expect(categories).withContext(`${name}.json is missing a category`).toEqual(expected);
    }
  });

  it('uses the same interpolation params in every locale', () => {
    // A translator who drops {{sku}} leaves the message technically valid and materially useless.
    for (const key of baseline) {
      const expected = placeholders(leaf(en as Json, key) ?? '');
      for (const [name, locale] of LOCALES) {
        expect(placeholders(leaf(locale, key) ?? ''))
          .withContext(`${name}.json ERRORS.${key} has different params from en.json`)
          .toEqual(expected);
      }
    }
  });

  it('has no empty message anywhere', () => {
    for (const key of baseline) {
      for (const [name, locale] of LOCALES) {
        expect((leaf(locale, key) ?? '').trim().length)
          .withContext(`${name}.json ERRORS.${key} is empty`)
          .toBeGreaterThan(0);
      }
    }
  });

});
