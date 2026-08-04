import {EntityBase, NamedEntityBase} from '../../../shared/models/entity.model';
import {ReadableManufacturer} from '../../brands/models/brand.model';
import {ReadableProductType} from '../../types/models/product-type.model';
import {ReadableCategory} from '../../categories/models/category.model';

/** Mirrors catalog-commons model/product/ProductDescription (extends NamedEntity) */
export type ProductDescription = NamedEntityBase;

/** Mirrors model/product/product/ProductSpecification */
export interface ProductSpecification {
  height?: number;
  weight?: number;
  length?: number;
  width?: number;
  model?: string;
  manufacturer?: string;
  dimensionUnitOfMeasure?: string;
  weightUnitOfMeasure?: string;
}

/** Mirrors model/product/ReadableImage -> Entity */
export interface ReadableImage {
  id?: number;
  imageName?: string;
  imageUrl?: string;
  externalUrl?: string;
  videoUrl?: string;
  imageType?: number;
  order?: number;
  defaultImage?: boolean;
}

/** Minimal surface of ReadableProduct -> ProductEntity -> Product -> Entity as
 *  actually consumed here (products list table, relationship/group summaries).
 *  The full DTO also carries attributes/variants/options/inventory — out of
 *  scope: this app only ever reads the fields below off list rows. */
export interface ReadableProduct {
  id?: number;
  sku?: string;
  available?: boolean;
  visible?: boolean;
  price?: number;
  quantity?: number;
  sortOrder?: number;
}

/** Mirrors model/product/product/definition/ProductDefinition -> Entity */
export interface ReadableProductDefinition {
  id?: number;                              // Entity
  visible?: boolean;                        // ProductDefinition
  shipeable?: boolean;
  virtual?: boolean;
  canBePurchased?: boolean;
  dateAvailable?: string;
  identifier?: string;
  sku?: string;
  productSpecifications?: ProductSpecification;
  sortOrder?: number;
  type?: ReadableProductType;               // ReadableProductDefinition
  categories?: ReadableCategory[];
  manufacturer?: ReadableManufacturer;
  description?: ProductDescription;
  images?: ReadableImage[];
  inventory?: {sku?: string; price?: string; quantity?: number};
  descriptions?: ProductDescription[];
}

/** Mirrors model/product/product/definition/PersistableProductDefinition -> Entity.
 *  NOTE: POST /private/product (v2) echoes back an Entity ({id} only);
 *  PUT /private/product/{id} (v2) returns void. */
export interface PersistableProductDefinition {
  id?: number;
  visible?: boolean;
  shipeable?: boolean;
  virtual?: boolean;
  canBePurchased?: boolean;
  dateAvailable?: string;
  identifier?: string;
  sku?: string;
  productSpecifications?: ProductSpecification;
  sortOrder?: number;
  descriptions?: ProductDescription[];
  properties?: unknown[];
  categories?: unknown[];
  type?: string;                            // unique code
  manufacturer?: string;                    // unique code
  price?: number;
  quantity?: number;
}

/** Mirrors model/product/LightPersistableProduct — body of the two PATCH
 *  endpoints that update a product without touching the full definition. */
export interface LightPersistableProduct {
  price?: string;
  available?: boolean;
  productShipeable?: boolean;
  quantity?: number;
}

/** Mirrors com.asrevo.cvhome.commons.domain.Entity — POST /private/product (v2)
 *  echoes just the new id. */
export type CreatedProductEntity = EntityBase;
