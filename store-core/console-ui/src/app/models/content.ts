/**
 * Content as the content pod stores it, verified against
 * `content-commons/model/content/{common,box,page}` and `content-service/api/v1/ContentApi`.
 *
 * Nothing was ported from seller-core here. Its `LandingPageContent` was typed from what
 * seller-ui happened to send rather than from a Java contract — its own doc comment said so —
 * and the three endpoints it sent it to are not mapped by any controller. See lessons.md,
 * "Store management — the landing-page endpoints seller-ui calls do not exist".
 */

/**
 * Mirrors `content/model/content/common/ContentDescription` → `NamedEntity`.
 *
 * One language's copy for a piece of content. `name` is the heading, `description` the body, and
 * `keyWords` a comma-separated string rather than a list — the console splits and joins it at the
 * edge so the form can hold real tags.
 */
export interface ContentDescription {
  readonly id?: number;
  /** ISO language code: the same `en` / `ar` the console runs in. */
  readonly language?: string;
  readonly name?: string;
  readonly description?: string;
  readonly metaDescription?: string;
  readonly keyWords?: string;
  readonly title?: string;
  readonly friendlyUrl?: string;
  readonly highlights?: string;
}

/**
 * Mirrors `content/model/content/box/ReadableContentBox` → `Content` → `Entity`.
 *
 * A box rather than a page, because a box is exactly a code plus per-language copy, while
 * `ContentPage` adds `linkToMenu` — a storefront navigation concern that means nothing for the
 * home page's own text.
 */
export interface ReadableContentBox {
  readonly id?: number;
  readonly code?: string;
  readonly visible?: boolean;
  readonly contentType?: string;
  readonly descriptions?: readonly ContentDescription[];
}

/** Mirrors `content/model/content/box/PersistableContentBox`. */
export interface PersistableContentBox {
  readonly id?: number;
  readonly code: string;
  readonly visible: boolean;
  readonly descriptions: readonly ContentDescription[];
}

/** Mirrors `commons` `EntityExists` — what `…/box/{code}/exists` answers with. */
export interface ContentExists {
  readonly exists: boolean;
}
