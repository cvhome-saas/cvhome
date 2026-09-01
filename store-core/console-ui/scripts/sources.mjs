/**
 * Where the console's code lives, now that half of it is a library.
 *
 * Every lint script here answers a question of the form "is this thing in the app referenced, or is
 * this reference satisfied" — and after the ui-kit extraction neither half can answer alone. The
 * error stack moved to the library while the `errors.*` copy it names stayed in `src/locale`, so a
 * scan of `src` reported those keys as unused; the token layer moved while most reads stayed, so a
 * scan of `src` reported every token as undefined. One list, imported by all four, so the next thing
 * to move only has to be added once.
 */
import {existsSync, readFileSync} from 'node:fs';

const KIT = '../../store-commons/ui-kit';

/** Directories holding first-party source, app first. Missing entry points are skipped. */
export const SOURCE_DIRS = [
  'src/app',
  ...['src', 'ui', 'theme', 'i18n', 'forms', 'uaa'].map((d) => `${KIT}/${d}`),
].filter((dir) => existsSync(dir));

/** The same, for checks that also want `src/environments` and `src/locale`. */
export const SOURCE_ROOTS = [
  'src',
  ...['src', 'ui', 'theme', 'i18n', 'forms', 'uaa'].map((d) => `${KIT}/${d}`),
].filter((dir) => existsSync(dir));

/**
 * A locale's full dictionary: the kit's copy with the app's laid over the top, exactly as
 * `withKitCopy` composes them at runtime.
 *
 * Both halves are needed for either question to be answerable. `shared.*` and `errors.*` are defined
 * in the library and read from both sides; the app's own namespaces are defined here and read only
 * here. Checking one file against one tree reports the other half as broken.
 */
export function dictionary(locale) {
  const app = JSON.parse(readFileSync(`src/locale/${locale}.json`, 'utf8'));
  const kit = JSON.parse(readFileSync(`${KIT}/i18n/src/lib/dictionaries/${locale}.json`, 'utf8'));
  return merge(kit, app);
}

function merge(base, override) {
  const out = {...base};
  for (const [key, value] of Object.entries(override)) {
    out[key] =
      isObject(out[key]) && isObject(value) ? merge(out[key], value) : value;
  }
  return out;
}

const isObject = (v) => typeof v === 'object' && v !== null && !Array.isArray(v);
