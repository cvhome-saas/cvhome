/**
 * Wire DTOs of the content service (`store-pod/content`), private console API.
 *
 * Console-native: written against the new content platform, not ported from seller-core. The full
 * set (pages, posts, banners, FAQ, policies, menus, media) lands with the Content module; what is
 * here is what store management already needs.
 */

/** How complete one locale of an item is. */
export type TranslationState = 'MISSING' | 'DRAFT' | 'TRANSLATED' | 'STALE';

/**
 * One locale of a content item — maps 1:1 onto the server's `content_description` row (`title` is
 * the row's `name`, `body` its `description`). Which fields matter depends on the item type.
 */
export interface ContentTranslation {
  readonly id?: number;
  readonly language: string;
  readonly state?: TranslationState;
  readonly title?: string;
  readonly body?: string;
  readonly excerpt?: string;
  readonly friendlyUrl?: string;
  readonly metaTitle?: string;
  readonly metaDescription?: string;
  /** Comma-separated search keywords. Stored and read back by the new service. */
  readonly keywords?: string;
  readonly altText?: string;
  readonly ctaLabel?: string;
  readonly subtitle?: string;
}

/**
 * A store-level text fragment the storefront reads by code (the legacy "content box"). No workflow:
 * always live, `visible` toggles it.
 */
export interface Snippet {
  readonly id?: number;
  readonly code?: string;
  readonly visible: boolean;
  readonly translations: readonly ContentTranslation[];
}
