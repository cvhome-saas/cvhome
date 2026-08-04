export interface ProductFormTabConfig {
  titleKey: string;
  route: string;
}

export const PRODUCT_FORM_TABS: ProductFormTabConfig[] = [
  {titleKey: 'COMPONENTS.PRODUCTS_IMAGES', route: 'images'},
  {titleKey: 'COMPONENTS.PRODUCT_TO_CATEGORY', route: 'category'},
  {titleKey: 'COMPONENTS.PRODUCT_RELATED', route: 'related'},
  {titleKey: 'COMPONENTS.PRODUCTS_DISCOUNT', route: 'discount'}
];
