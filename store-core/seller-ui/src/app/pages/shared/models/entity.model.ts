/** Mirrors com.asrevo.cvhome.commons.domain.Entity */
export interface EntityBase {
  id?: number;
}

/** Mirrors ...store.core.model.entity.ShopEntity — language is a LanguageCode
 *  serialized to a plain string by LanguageCodeSerializer. */
export interface ShopEntityBase extends EntityBase {
  language?: string;
}

/** Mirrors ...store.core.model.catalog.NamedEntity */
export interface NamedEntityBase extends ShopEntityBase {
  name?: string;
  description?: string;
  friendlyUrl?: string;
  keyWords?: string;
  highlights?: string;
  metaDescription?: string;
  title?: string;
}

/** Mirrors ...store.core.model.entity.EntityExists */
export interface EntityExists {
  exists?: boolean;
}
