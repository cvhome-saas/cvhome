'use client'
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import {useDir} from '@store-front/i18n/use-dir';
import type {Banner} from '@store-front/types';
import {BannerImage} from '@store-front/ui/banner-image';
import {Swiper, SwiperSlide} from '@store-front/ui/swiper';

/**
 * Homepage hero from the store's CMS banners. Swiper needs `dir` explicitly (it does not read CSS direction) and
 * is re-keyed on dir so a locale switch re-initialises it.
 */
export function Hero({slides, autoplay = 5}: {
    slides: Banner[];
    /** Seconds between slides, or `false` for a still hero — the builder's hero fields. */
    autoplay?: number | false;
}) {
    const t = useTranslations('PAGE.HOME');
    const dir = useDir();
    const shown = slides.filter(s => s.desktopUrl);
    if (shown.length === 0) return null;
    return (
        <section aria-roledescription="carousel" className="starter-hero">
            <Swiper key={dir} dir={dir} loop={shown.length > 1}
                    pagination={{clickable: true}} autoplay={shown.length > 1 && autoplay !== false ? {delay: autoplay * 1000, disableOnInteraction: true} : false}
                    a11y={{enabled: true}} className="aspect-[16/7] w-full bg-muted sm:aspect-[21/8]">
                {shown.map((s, i) => (
                    <SwiperSlide key={s.id} aria-label={t('HERO_SLIDE', {index: i + 1, total: shown.length})}>
                        <BannerImage banner={s} priority={i === 0} sizes="100vw" className="object-cover"/>
                        {(s.title || s.subtitle || s.ctaLabel) && (
                            <div className="absolute inset-0 flex flex-col items-start justify-end gap-2 bg-gradient-to-t from-black/60 via-black/20 to-transparent p-6 sm:p-10">
                                {s.title && <h2 className="text-2xl font-semibold text-white sm:text-4xl"><bdi dir="auto">{s.title}</bdi></h2>}
                                {s.subtitle && <p className="max-w-prose text-sm text-white/85 sm:text-base"><bdi dir="auto">{s.subtitle}</bdi></p>}
                                {s.ctaLabel && s.target?.value && (
                                    <Link prefetch={false} href={s.target.value}
                                          className="mt-1 inline-flex rounded-md bg-white px-4 py-2 text-sm font-medium text-neutral-900">
                                        <bdi dir="auto">{s.ctaLabel}</bdi>
                                    </Link>
                                )}
                            </div>
                        )}
                    </SwiperSlide>
                ))}
            </Swiper>
        </section>
    );
}
