// Shared types entry point for the shared types package.
export * from "./description";
export * from "./bread-crumb";
export * from "./constant";

// Re-export with aliases to avoid name collisions across modules
export type { CategoryPage, Category as CategoryEntity } from "./category";
export type {
  ProductGroupPage,
  ProductGroup,
  Product,
  ProductSpecifications,
  ProductPrice,
  Image,
  Manufacturer,
  Category as ProductCategory,
  Parent
} from "./product-groups";
export type { Cart, Total as CartTotal } from "./cart";
export * from "./content";
export * from "./country";
export type { Order, Total as OrderTotal, TotalItem, Address as OrderAddress, ProductItem } from "./order";
export type { Store, Address as StoreAddress, ImageFile, ReadableAudit, SliderImage, SocialLink } from "./store";
export { Theme } from "./store";

export * from "./color-schema";
export * from "./store-context";
export * from "./params";
export * from "./checkout-cart";
export * from "./checkout-constants";
