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
import {existsSync} from 'node:fs';

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
