/**
 * Ported from the shapes `content-service`'s `ContentApi` actually serves, rather than from
 * seller-core: its `LandingPageContent` was typed from what the frontend happened to send, against
 * three endpoints (`/private/content/any/{code}`, `PUT /private/content/{code}`,
 * `POST /private/content`) that no controller maps. See lessons.md, "Store management — the
 * landing-page endpoints seller-ui calls do not exist".
 *
 * A **content box** is a fragment of storefront copy: a `code` that identifies it, and one
 * description per language. `ContentPage` is the other kind and is deliberately not modelled — it
 * adds `linkToMenu`, a storefront navigation concern that means nothing for home-page copy.
 */

/**
 * One language's copy, as `content/model/content/common/ContentDescription` (→ `NamedEntity` →
 * `ShopEntity` → `Entity`) declares it.
 *
 * Every field here exists on the wire. Not every field survives a round trip, and the difference is
 * not guessable from the shape — see `keyWords`.
 */
export interface ContentDescription {
  readonly id?: number;
  /** `LanguageCode`, flattened to its code by the serializer on `ShopEntity`. */
  readonly language: string;
  /** The headline. seller-ui used this as the landing page's title, and so does the console. */
  readonly name?: string;
  /** The body copy. */
  readonly description?: string;
  readonly metaDescription?: string;
  readonly title?: string;
  /** `SEF_URL` on the entity. Carried through untouched; the console does not edit it. */
  readonly friendlyUrl?: string;
  /**
   * **Never stored and never returned.** `ContentFacadeImpl.buildDescriptions` does not set
   * `metatagKeywords`, and `ReadableContentBoxPopulator.populateDescription` does not read it, so a
   * value sent here is dropped in silence. Declared because it is on the DTO and its absence from
   * both mappers is the finding, not the field. See lessons.md, "Store management — a content
   * description's keywords are dropped by both mappers".
   */
  readonly keyWords?: string;
}

/** `ReadableContentBox`. `description` is only populated when one language is asked for; the private endpoint asks for all. */
export interface ReadableContentBox {
  readonly id: number;
  readonly code: string;
  readonly visible: boolean;
  readonly contentType?: string;
  readonly descriptions?: readonly ContentDescription[];
}

/**
 * `PersistableContentBox`, as `POST /private/content/box` and `PUT /private/content/box/{id}` take it.
 *
 * `descriptions` is not optional in practice: `buildDescriptions` iterates it without a null check,
 * so a body that omits it is a 500 rather than a validation error. And it must carry **every**
 * language the box has — the entity's `@OneToMany` has no `orphanRemoval`, so a language left out of
 * the list is not deleted, it is merely forgotten by this request and comes back on the next read.
 */
export interface PersistableContentBox {
  readonly id?: number;
  readonly code: string;
  readonly visible: boolean;
  readonly descriptions: readonly ContentDescription[];
}

/** What `POST /private/content/box` answers with: the new box's id and nothing else. */
export interface ContentEntityId {
  readonly id: number;
}
