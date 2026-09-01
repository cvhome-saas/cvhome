/**
 * The languages the console itself is published in.
 *
 * At the bottom tier because `models/store-settings.ts` names a `ConsoleLocale` when it describes
 * which locales a piece of copy has been written in, and a model may not reach up into `@core`.
 * `@core/i18n/locale.service` re-exports these and remains where the *service* lives.
 */
export type LocaleCode = 'en' | 'ar';

export interface ConsoleLocale {
  readonly code: LocaleCode;
  readonly label: string;
  readonly dir: 'ltr' | 'rtl';
}

export const CONSOLE_LOCALES: readonly ConsoleLocale[] = [
  {code: 'en', label: 'English', dir: 'ltr'},
  {code: 'ar', label: 'العربية', dir: 'rtl'},
];
