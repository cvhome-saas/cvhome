export type Dir = 'ltr' | 'rtl';

const RTL_LOCALES: ReadonlySet<string> = new Set(['ar']);

export const getDirection = (locale: string): Dir => RTL_LOCALES.has(locale) ? 'rtl' : 'ltr';
export const isRtl = (locale: string): boolean => getDirection(locale) === 'rtl';
export const isLtr = (locale: string): boolean => !isRtl(locale);
/** nextjs-toast-notify position that sits on the reading-end side. */
export const toastDirection = (locale: string) => isRtl(locale) ? 'bottom-left' : 'bottom-right';
