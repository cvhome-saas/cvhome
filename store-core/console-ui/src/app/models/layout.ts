/** Console-native; not a port from seller-core. */

/**
 * The storefront builder's wire types: the layout document as the content service stores it
 * (`/private/content/layouts/**`) and the theme manifest as the storefront serves it
 * (`/api/theme-manifest`). The document mirrors `content-commons` `model/layout`; the manifest mirrors
 * landing-ui's section catalogue merged with the active theme's registry.
 */

export type PageKind = 'HOME';

export interface LayoutStyle {
  spacing?: 'none' | 'sm' | 'md' | 'lg' | null;
  width?: 'content' | 'wide' | 'full' | null;
  tone?: 'default' | 'muted' | 'inverse' | null;
}

export interface LayoutVisibility {
  hidden?: boolean | null;
  devices?: readonly ('desktop' | 'tablet' | 'mobile')[] | null;
}

/** `{field: {locale: value}}` — the builder edits one locale at a time via the language switcher. */
export type LocalizedText = Record<string, Record<string, string>>;

export interface LayoutItem {
  id: string;
  props: Record<string, unknown>;
  text: LocalizedText;
}

export interface LayoutSection {
  id: string;
  kind: string;
  variant?: string | null;
  props: Record<string, unknown>;
  items?: LayoutItem[] | null;
  text: LocalizedText;
  style?: LayoutStyle | null;
  visibility?: LayoutVisibility | null;
  anchor?: string | null;
  /** A locked section cannot be moved, removed, duplicated or dragged until unlocked. */
  locked?: boolean | null;
}

export interface LayoutDocument {
  schemaVersion: number;
  page: PageKind;
  sections: LayoutSection[];
}

export interface LayoutMeta {
  draftVersion: number;
  publishedVersion: number | null;
  publishedAt: string | null;
  dirty: boolean;
}

export interface ReadableLayout {
  draft: LayoutDocument;
  meta: LayoutMeta;
}

export interface LayoutFieldError {
  field: string;
  code: string;
  message: string;
}

export interface PublishedLayout {
  meta: LayoutMeta;
  warnings: LayoutFieldError[];
}

export interface LayoutRevisionRow {
  version: number;
  publishedAt: string | null;
  publishedBy: string | null;
}

export interface SavedSection {
  id: number;
  name: string;
  kind: string;
  section: LayoutSection;
  createdAt: string | null;
}

// --------------------------------------------------------------------------------------- theme manifest

export type ManifestFieldType =
  | 'text' | 'textarea' | 'richtext' | 'select' | 'media' | 'toggle' | 'range' | 'color' | 'link'
  | 'ref:product-group' | 'ref:category' | 'ref:faq' | 'ref:post-category' | 'product-source';

export interface ManifestLabel {
  en: string;
  ar: string;
}

export interface ManifestField {
  key: string;
  type: ManifestFieldType;
  label: ManifestLabel;
  localized?: boolean;
  min?: number;
  max?: number;
  step?: number;
  options?: {value: string; label: ManifestLabel}[];
  visibleIf?: {key: string; equals: unknown};
}

export interface ManifestVariant {
  id: string;
  label: ManifestLabel;
  source: 'theme' | 'fallback';
  exclusive?: boolean;
}

export interface ManifestKind {
  kind: string;
  label: ManifestLabel;
  icon: string;
  variants: ManifestVariant[];
  fields: ManifestField[];
  itemFields?: ManifestField[];
  itemLabel?: ManifestLabel;
  maxItems?: number;
}

export interface ManifestPreset {
  id: string;
  label: ManifestLabel;
  kind: string;
  variant: string;
  props?: Record<string, unknown>;
  items?: {props?: Record<string, unknown>; text?: LocalizedText}[];
  text?: LocalizedText;
  style?: LayoutStyle;
}

export interface ThemeManifest {
  themeId: string;
  kinds: ManifestKind[];
  presets: ManifestPreset[];
}
