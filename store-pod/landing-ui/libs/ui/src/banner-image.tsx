import Image, {getImageProps} from 'next/image';
import type {Banner} from '@store-front/types';

/** Below Tailwind's `sm` breakpoint the banner's mobile rendition is served. */
const MOBILE_MEDIA = '(max-width: 639px)';

/**
 * A CMS banner image with art direction: phones receive the merchant's mobile rendition instead of
 * downloading the desktop banner. Images are `unoptimized` in this app (no srcset), so the only way
 * to shrink the mobile payload is the `<picture>`/`<source media>` split — `sizes` alone does nothing.
 *
 * Renders `fill`, so the parent must be positioned (every theme already wraps banners in a
 * `relative` aspect container). Without a distinct mobile rendition this is exactly `next/image`,
 * keeping its `priority` preload for the LCP slide.
 */
export function BannerImage({banner, priority = false, sizes, className}: {
    banner: Pick<Banner, 'desktopUrl' | 'mobileUrl' | 'altText'>;
    priority?: boolean;
    sizes?: string;
    className?: string;
}) {
    const desktopSrc = banner.desktopUrl ?? banner.mobileUrl ?? '';
    const alt = banner.altText ?? '';
    if (!banner.mobileUrl || banner.mobileUrl === desktopSrc) {
        return <Image src={desktopSrc} alt={alt} fill priority={priority} sizes={sizes} className={className}/>;
    }
    const {props} = getImageProps({src: desktopSrc, alt, fill: true, priority, sizes, className});
    return (
        <picture>
            <source media={MOBILE_MEDIA} srcSet={banner.mobileUrl}/>
            {/* eslint-disable-next-line @next/next/no-img-element -- next/image cannot art-direct; props come from getImageProps */}
            <img {...props}/>
        </picture>
    );
}
