import {NamedEntityBase} from 'seller-core';

/** Mirrors catalog-commons model/manufacturer/ManufacturerDescription (extends NamedEntity) */
export type ManufacturerDescription = NamedEntityBase;

/** Mirrors ReadableManufacturer -> ManufacturerEntity -> Manufacturer -> Entity */
export interface ReadableManufacturer {
  id?: number;          // Entity
  code?: string;        // Manufacturer
  order?: number;       // ManufacturerEntity (int)
  description?: ManufacturerDescription;
  descriptions?: ManufacturerDescription[];
}

/** Mirrors PersistableManufacturer -> ManufacturerEntity -> Manufacturer -> Entity.
 *  NOTE: POST /private/manufacturer echoes this type back; PUT returns void. */
export interface PersistableManufacturer {
  id?: number;
  code?: string;
  order?: number;
  descriptions?: ManufacturerDescription[];
}
