import type {Banner, SectionItem} from '@store-front/types';

/**
 * The layout hero's inline slides, dressed as the `Banner` shape every theme's slider already renders.
 * Themes built their heroes against CMS banners; the builder stores slides as section items with a
 * server-injected `mediaUrl`. One adapter keeps those heroes untouched instead of teaching each one a
 * second slide type. Items without a resolved media URL are dropped — a slide is its image.
 */
export function slidesAsBanners(items: readonly SectionItem[] | null | undefined): Banner[] {
    return (items ?? [])
        .filter(item => typeof item.props.mediaUrl === 'string')
        .map((item, index): Banner => ({
            id: index,
            placement: null,
            position: index,
            servedLocale: null,
            title: item.text.heading ?? null,
            subtitle: item.text.subheading ?? null,
            body: null,
            ctaLabel: item.text.cta ?? null,
            target: null,
            desktopUrl: item.props.mediaUrl as string,
            mobileUrl: null,
            altText: item.text.heading ?? '',
            theme: null,
            startsAt: null,
            endsAt: null,
        }));
}
