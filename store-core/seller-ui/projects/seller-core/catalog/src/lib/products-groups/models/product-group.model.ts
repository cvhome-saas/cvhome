import {NamedEntityBase} from 'seller-core';

/** Mirrors catalog-commons model/product/group/ReadableProductGroupDescription (extends NamedEntity) */
export type ReadableProductGroupDescription = NamedEntityBase;

/** Mirrors PersistableProductGroupDescription (extends NamedEntity) */
export type PersistableProductGroupDescription = NamedEntityBase;

/** Minimal shape of a product as embedded in a product group's `products` list
 *  and returned by the product search/autocomplete endpoints. The full
 *  ReadableProduct DTO is out of scope here — see catalogue/products (Step 5). */
export interface ProductGroupItem {
  id?: number;
  sku?: string;
}

/** Mirrors ReadableProductGroup -> ProductGroup */
export interface ReadableProductGroup {
  id?: number;
  code?: string;
  active?: boolean;
  description?: ReadableProductGroupDescription;
  descriptions?: ReadableProductGroupDescription[];
  parentProduct?: ProductGroupItem;
  products?: ProductGroupItem[];
}

/** Mirrors PersistableProductGroup -> ProductGroup */
export interface PersistableProductGroup {
  id?: number;
  code?: string;
  active?: boolean;
  descriptions?: PersistableProductGroupDescription[];
  parentProductId?: number;
  productIds?: number[];
}
