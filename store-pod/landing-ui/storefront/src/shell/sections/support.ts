import type {LayoutSectionData, SectionItem} from '@store-front/types';

/** A `link` prop as the builder stores it. */
export interface SectionLink {
    type?: 'url' | 'category' | 'product';
    value?: string;
}

/** Locale-relative href for a stored link; `#` when it points nowhere. */
export function linkHref(raw: unknown): string {
    const link = (raw ?? {}) as SectionLink;
    if (!link.value) return '#';
    switch (link.type) {
        case 'category':
            return `/category/${link.value}`;
        case 'product':
            return `/product/${link.value}`;
        default:
            return link.value;
    }
}

export const mediaUrl = (props: Record<string, unknown>): string | undefined =>
    typeof props.mediaUrl === 'string' ? props.mediaUrl : undefined;

export const items = (section: LayoutSectionData): SectionItem[] => section.items ?? [];

export const num = (value: unknown, fallback: number): number =>
    typeof value === 'number' && Number.isFinite(value) ? value : fallback;
