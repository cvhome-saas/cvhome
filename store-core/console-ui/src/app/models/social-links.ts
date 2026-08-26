import type {IconName} from '@models/ui';

/**
 * What the console knows about the social providers a storefront footer links to.
 *
 * This lived in `store-settings` while the merchant record owned the store's appearance. Social links
 * are the content service's now, so the knowledge moved with them rather than leaving the content
 * feature importing from the settings model of a service that no longer stores any of this.
 *
 * The provider itself is handled as the server's free string, not the narrowed union: the row list comes
 * from `GET /public/social-links-providers`, so a member added to `SocialProvider` reaches the console
 * before the console knows about it. Everything here therefore falls back rather than throws — the same
 * known-set discipline `shared/i18n/status-label.ts` applies to statuses — and an unknown provider still
 * gets a field that saves.
 */

/** `SocialProvider` in `store-commons/commons`. */
export type SocialLinkProvider = 'FACEBOOK' | 'X' | 'TIKTOK' | 'INSTAGRAM' | 'GITHUB';

/**
 * The mark each provider is drawn with.
 *
 * A lookup rather than a field on the record, because `SocialLink` carries only a provider and a URL —
 * the icon is the console's decision.
 */
export const SOCIAL_LINK_ICON: Readonly<Record<SocialLinkProvider, IconName>> = {
  FACEBOOK: 'facebook',
  X: 'xSocial',
  TIKTOK: 'tiktok',
  INSTAGRAM: 'instagram',
  GITHUB: 'github',
};

/** For a provider the console has not been taught yet. */
export const SOCIAL_LINK_FALLBACK_ICON: IconName = 'link';

/** Brand names. Kept translated so a right-to-left reader still sees them presented consistently — the names themselves do not change across languages. */
export const SOCIAL_LINK_LABEL_KEY: Readonly<Record<SocialLinkProvider, string>> = {
  FACEBOOK: 'content.branding.socialProvider.facebook',
  X: 'content.branding.socialProvider.x',
  TIKTOK: 'content.branding.socialProvider.tiktok',
  INSTAGRAM: 'content.branding.socialProvider.instagram',
  GITHUB: 'content.branding.socialProvider.github',
};

/**
 * The hosts a profile link for each provider may live on.
 *
 * A Facebook field holding a TikTok URL is not a typo the storefront can recover from — it renders the
 * link under a Facebook mark and sends shoppers somewhere else entirely. Nothing server-side checks
 * this: `SocialLink` is a provider and a free string, so the console is the only place the pairing can
 * be enforced.
 *
 * The alternates are the ones these companies still serve rather than every domain they have ever
 * owned: `twitter.com` because a decade of links point at it and X redirects them, `fb.com` because it
 * is Facebook's own short domain. Subdomains are accepted against each entry (`m.facebook.com`,
 * `www.instagram.com`), which is why the check is a suffix match rather than equality.
 */
export const SOCIAL_LINK_HOSTS: Readonly<Record<SocialLinkProvider, readonly string[]>> = {
  FACEBOOK: ['facebook.com', 'fb.com'],
  X: ['x.com', 'twitter.com'],
  TIKTOK: ['tiktok.com'],
  INSTAGRAM: ['instagram.com'],
  GITHUB: ['github.com'],
};

export function isSocialLinkProvider(value: string): value is SocialLinkProvider {
  return value in SOCIAL_LINK_ICON;
}

export function socialLinkIcon(provider: string): IconName {
  return isSocialLinkProvider(provider) ? SOCIAL_LINK_ICON[provider] : SOCIAL_LINK_FALLBACK_ICON;
}

/**
 * The host a provider's link is expected on, for the field's own message.
 *
 * The first entry rather than all of them: the message is telling an operator what to type, and
 * "use facebook.com" is advice where "use facebook.com or fb.com" is a quiz.
 */
export function expectedSocialHost(provider: string): string | null {
  return isSocialLinkProvider(provider) ? SOCIAL_LINK_HOSTS[provider][0] : null;
}

/** What is wrong with a link, or `null` if nothing is. */
export type SocialLinkProblem = 'url' | 'host' | 'profile';

/**
 * Whether a URL is a profile page on the provider whose row it was typed into.
 *
 * Empty is not a problem — a blank row is how a link is removed. A provider the console does not know
 * gets the shape check and nothing more, because there is no host list to check it against.
 *
 * The scheme is optional throughout. Requiring `https://` made *Save changes* impossible on a store
 * that had ever set a link without one, which made the section unusable before a character was typed.
 * Whatever is entered is stored verbatim; the console does not rewrite it.
 */
export function socialLinkProblem(provider: string, url: string): SocialLinkProblem | null {
  const value = url.trim();
  if (!value) {
    return null;
  }
  if (!/^(https?:\/\/)?[^\s/]+\.[^\s]+$/.test(value)) {
    return 'url';
  }
  if (!isSocialLinkProvider(provider)) {
    return null;
  }

  const withoutScheme = value.replace(/^[a-z][a-z0-9+.-]*:\/\//i, '');
  const [host, ...rest] = withoutScheme.split('/');
  const bare = host.toLowerCase().replace(/:\d+$/, '');

  // Suffix match, so `m.facebook.com` and `www.instagram.com` are the same site.
  const onProvider = SOCIAL_LINK_HOSTS[provider].some((known) => bare === known || bare.endsWith(`.${known}`));
  if (!onProvider) {
    return 'host';
  }
  // `facebook.com` on its own is the site, not a page on it.
  return rest.join('/').replace(/[?#].*$/, '').length > 0 ? null : 'profile';
}
