import {NamedEntityBase} from '../../../shared/models/entity.model';

/** Mirrors catalog-commons model/category/CategoryDescription (extends NamedEntity) */
export type CategoryDescription = NamedEntityBase;

/** Mirrors Category -> Entity */
export interface CategoryRef {
  id?: number;
  code?: string;
  description?: CategoryDescription;
}

/** Mirrors ReadableCategory -> CategoryEntity -> Category -> Entity */
export interface ReadableCategory {
  id?: number;              // Entity
  code?: string;            // Category
  sortOrder?: number;       // CategoryEntity
  visible?: boolean;
  featured?: boolean;
  lineage?: string;
  depth?: number;
  parent?: CategoryRef;
  description?: CategoryDescription;
  productCount?: number;
  store?: string;
  descriptions?: CategoryDescription[];
  children?: ReadableCategory[];
}

/** Structural shape of @cluetec/ngcx-tree's nodeMoved output.
 *  Declared locally on purpose — not imported from the library. */
export interface CategoryTreeMoveEvent {
  node: {id: string};
  parent?: {id: string};
}

/** Mirrors PersistableCategory -> CategoryEntity -> Category -> Entity.
 *  NOTE: both POST /private/category and PUT /private/category/{id} echo this type back. */
export interface PersistableCategory {
  id?: number;
  code?: string;
  sortOrder?: number;
  visible?: boolean;
  featured?: boolean;
  lineage?: string;
  depth?: number;
  parent?: CategoryRef;
  descriptions?: CategoryDescription[];
  children?: PersistableCategory[];
}
