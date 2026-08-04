import {EntityBase, NamedEntityBase} from '../../shared/models/entity.model';

/** Mirrors merchant-commons content/model/content/common/ContentDescription (extends NamedEntity) */
export type ContentDescription = NamedEntityBase;

/** Mirrors merchant-commons content/model/content/common/Content -> Entity */
export interface ContentRef {
  id?: number;
  code?: string;
  visible?: boolean;
  contentType?: string;
}

/** Mirrors merchant-commons content/model/content/box/ReadableContentBox -> Content -> Entity */
export interface ReadableContentBox extends ContentRef {
  description?: ContentDescription;
  descriptions?: ContentDescription[];
}

/** Mirrors merchant-commons content/model/content/box/PersistableContentBox -> ContentBox -> Content -> Entity */
export interface PersistableContentBox extends ContentRef {
  descriptions?: ContentDescription[];
}

/** Mirrors merchant-commons content/model/content/page/ReadableContentPage -> ContentPage -> Content -> Entity */
export interface ReadableContentPage extends ContentRef {
  linkToMenu?: boolean;
  description?: ContentDescription;
  path?: string;
  descriptions?: ContentDescription[];
}

/** Mirrors merchant-commons content/model/content/page/PersistableContentPage -> ContentPage -> Content -> Entity */
export interface PersistableContentPage extends ContentRef {
  linkToMenu?: boolean;
  descriptions?: ContentDescription[];
}

/** Mirrors merchant-commons content/model/content/ContentImage -> ContentPath -> ContentName
 *  -> Content (the package-local Content with name/contentType — NOT common/Content). */
export interface ContentFileItem {
  name?: string;
  contentType?: string;
  path?: string;
}

/** Mirrors merchant-commons content/model/content/ContentFolder */
export interface ContentFolder {
  content?: ContentFileItem[];
  path?: string;
}

/** Mirrors com.asrevo.cvhome.commons.domain.Entity — POST /private/content/box
 *  and POST /private/content/page each echo just the new id. */
export type CreatedContentEntity = EntityBase;
