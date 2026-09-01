/*
 * @cvhome-saas/ui-kit — the primary entry point.
 *
 * Application infrastructure: configuration, the HTTP client, the error stack, auth, platform
 * access, table types and routing helpers. Everything here is free of any one application's
 * vocabulary; anything that knows about stores, orders or the console's routes belongs in the
 * consumer.
 *
 * The design system lives in the secondary entry points: `/ui`, `/theme`, `/i18n`, `/forms`.
 */

export * from './lib/platform/browser-storage';
