import type {StoreSettings} from '@models/store-settings';

/**
 * The sections of the settings page still served from this file.
 *
 * Narrowed as each section moves to its real endpoint, so the type itself records how much of the
 * page is still make-believe. When this reaches `never` the file goes.
 */
export type FixtureSections = Pick<
  StoreSettings,
  'socialLogin' | 'payments'
>;

/** The host the mockup's callback URLs are built on. Goes with the social-login section. */
const STORE_CUSTOM_DOMAIN = 'shop.acmesupply.co';

/**
 * Acme Supply Co.'s settings, as the `Store Management` mockup shows them.
 *
 * Retargeted onto the real provider enums: the mockup's LinkedIn and YouTube links, its
 * Apple sign-in and its Tap Payments gateway have no representation in `SocialProvider`,
 * `cua`'s `SocialProvider` or `PaymentType`, so they are gone; GitHub, which those enums do
 * carry, is here instead.
 *
 * Home-page copy covers both console locales — the console's own locale list is the only
 * list of languages a storefront can be authored in, so there is no separate one here.
 */
export const STORE_SETTINGS: FixtureSections = {
  socialLogin: [
    {
      providerId: 'GOOGLE',
      icon: 'google',
      appId: '8841027-acme.apps.googleusercontent.com',
      appSecret: {endsWith: '4f2a', lastRotated: 'Jul 22, 2026'},
      callbackUrl: `https://${STORE_CUSTOM_DOMAIN}/auth/google/callback`,
      enabled: true,
    },
    {
      providerId: 'FACEBOOK',
      icon: 'facebook',
      appId: '772019448120336',
      appSecret: {endsWith: '9bd1', lastRotated: 'May 3, 2026'},
      callbackUrl: `https://${STORE_CUSTOM_DOMAIN}/auth/facebook/callback`,
      enabled: true,
    },
    {
      providerId: 'GITHUB',
      icon: 'github',
      appId: '',
      appSecret: {endsWith: null, lastRotated: null},
      callbackUrl: `https://${STORE_CUSTOM_DOMAIN}/auth/github/callback`,
      enabled: false,
    },
  ],

  payments: [
    {
      paymentType: 'STRIPE',
      icon: 'creditCard',
      enabled: true,
      credentials: {
        apiKey: 'pk_live_51Nq8xR2eZvKY…9tQm',
        secretKey: {endsWith: '7c31', lastRotated: '12 days ago'},
        webhookSecret: {endsWith: 'a0e5', lastRotated: '12 days ago'},
        webhookUrl: `https://${STORE_CUSTOM_DOMAIN}/webhooks/stripe`,
      },
    },
    {
      paymentType: 'PAYPAL',
      icon: 'creditCard',
      enabled: true,
      credentials: {
        apiKey: 'AV8x_2fLq0Bd7Jm…KpN1',
        secretKey: {endsWith: '55ab', lastRotated: '3 months ago — consider rotating'},
        webhookSecret: {endsWith: '31f7', lastRotated: '3 months ago'},
        webhookUrl: `https://${STORE_CUSTOM_DOMAIN}/webhooks/paypal`,
      },
    },
    {
      paymentType: 'COD',
      icon: 'dollar',
      enabled: true,
      credentials: null,
    },
    {
      paymentType: 'MANUAL_TRANSFER',
      icon: 'building',
      enabled: false,
      credentials: null,
    },
  ],
};
