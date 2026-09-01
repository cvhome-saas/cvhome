import type {LayoutSectionData, SectionItem} from '@store-front/types';

export {linkHref, type SectionLink} from '@store-front/theme';

export const mediaUrl = (props: Record<string, unknown>): string | undefined =>
    typeof props.mediaUrl === 'string' ? props.mediaUrl : undefined;

export const items = (section: LayoutSectionData): SectionItem[] => section.items ?? [];

export const num = (value: unknown, fallback: number): number =>
    typeof value === 'number' && Number.isFinite(value) ? value : fallback;
