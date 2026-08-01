import {EntityBase, NamedEntityBase} from '../../../shared/models/entity.model';

/** Mirrors catalog-commons model/product/type/ProductTypeDescription (extends NamedEntity) */
export type ProductTypeDescription = NamedEntityBase;

/** Mirrors ReadableProductType -> ProductTypeEntity -> Entity */
export interface ReadableProductType {
  id?: number;
  code?: string;
  allowAddToCart?: boolean;
  visible?: boolean;
  description?: ProductTypeDescription;
  descriptions?: ProductTypeDescription[];
}

/** Mirrors PersistableProductType -> ProductTypeEntity -> Entity */
export interface PersistableProductType {
  id?: number;
  code?: string;
  allowAddToCart?: boolean;
  visible?: boolean;
  descriptions?: ProductTypeDescription[];
}

/** Mirrors com.asrevo.cvhome.commons.domain.Entity — POST /private/product/type
 *  echoes just the new id, not the full type. */
export type CreatedEntity = EntityBase;
