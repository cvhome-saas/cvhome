import type {StoreSettings} from '@models/store-settings';

/** The default subdomain every store is served from, before any custom domain. */
export const STORE_SUBDOMAIN = 'acme-supply.myshop.io';

/** Placeholder store name. Store settings are still fixture-backed until their own module. */
const STORE_NAME = 'Acme Supply Co.';

/** The custom domain the mockup is mid-way through verifying. */
export const STORE_CUSTOM_DOMAIN = 'shop.acmesupply.co';

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
export const STORE_SETTINGS: StoreSettings = {
  // Kept in step with the shell's switcher, so the page and the rail name the same store.
  storeName: STORE_NAME,

  branding: {
    logo: {name: 'acme-logo.svg', url: null},
    banner: {name: 'acme-banner-summer.jpg', url: null},
    bannerProgress: 100,
  },

  home: {
    en: {
      title: 'Everything your workplace runs on',
      text: 'Acme Supply Co. stocks over 12,000 office, warehouse and janitorial products with next-day delivery across the region. Bulk pricing applies automatically at checkout.',
      metaDescription:
        'Office, warehouse and janitorial supplies with next-day regional delivery and automatic bulk pricing for business accounts.',
      tags: ['office supplies', 'b2b', 'bulk pricing'],
    },
    ar: {
      title: 'كل ما يحتاجه مكان عملك',
      text: 'توفر شركة أكمي أكثر من ١٢٠٠٠ منتج للمكاتب والمستودعات والنظافة مع التوصيل في اليوم التالي.',
      metaDescription: 'مستلزمات المكاتب والمستودعات والنظافة مع توصيل سريع وأسعار الجملة التلقائية.',
      tags: ['مستلزمات مكتبية', 'أسعار الجملة'],
    },
  },

  domains: [
    {
      domain: STORE_SUBDOMAIN,
      type: 'SUB_DOMAIN',
      status: 'verified',
      record: null,
    },
    {
      domain: STORE_CUSTOM_DOMAIN,
      type: 'CUSTOM_DOMAIN',
      status: 'waiting',
      record: {type: 'CNAME', name: 'shop', value: 'edge.myshop.io', ttl: 3600},
    },
  ],

  socialLinks: [
    {provider: 'INSTAGRAM', icon: 'instagram', url: 'instagram.com/acmesupply'},
    {provider: 'FACEBOOK', icon: 'facebook', url: 'facebook.com/acmesupplyco'},
    {provider: 'X', icon: 'xSocial', url: 'x.com/acmesupply'},
    {provider: 'TIKTOK', icon: 'tiktok', url: ''},
    {provider: 'GITHUB', icon: 'github', url: ''},
  ],

  slides: [
    {
      id: 'slide-1',
      priority: 1,
      name: 'Back to work sale — 20% off',
      meta: '1600×640 · 248 KB · JPG',
      link: '/collections/back-to-work',
      state: 'LIVE',
      url: null,
    },
    {
      id: 'slide-2',
      priority: 2,
      name: 'New: ergonomic seating range',
      meta: '1600×640 · 312 KB · JPG',
      link: '/collections/seating',
      state: 'LIVE',
      url: null,
    },
    {
      id: 'slide-3',
      priority: 3,
      name: 'Bulk paper — pallet pricing',
      meta: '1600×640 · 190 KB · PNG',
      link: '/products/copy-paper-pallet',
      state: 'SCHEDULED',
      url: null,
    },
  ],

  details: {
    name: STORE_NAME,
    legalName: 'Acme Supply Holdings LLC',
    slug: 'acme-supply',
    category: 'Business & industrial supplies',
    supportEmail: 'help@acmesupply.co',
    supportPhone: '+1 (415) 555-0142',
    currency: 'USD — US Dollar',
    language: 'English (EN)',
    timezone: '(GMT−08:00) America/Los_Angeles',
    taxNumber: 'US-93-4471209',
    address: '1180 Harrison St, Suite 400, San Francisco, CA 94103',
    shortDescription: 'Workplace supplies for growing teams.',
    published: true,
    maintenanceMode: false,
  },

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
