'use client'
import {useTranslations} from 'next-intl';
import {useDir} from '@store-front/i18n/use-dir';
import type {Banner} from '@store-front/types';
import {BannerImage} from '@store-front/ui/banner-image';
import {Swiper, SwiperSlide} from '@store-front/ui/swiper';

/**
 * Homepage hero from the store's CMS banners. Swiper needs `dir` explicitly (it does not read CSS
 * direction) and is re-keyed on dir so a locale switch re-initialises it.
 */
export function Hero({slides}: { slides: Banner[] }) {
    const t = useTranslations('PAGE.HOME');
    const dir = useDir();
    if (slides.length === 0) return null;
    return (
        <section aria-roledescription="carousel" className="jewellery-hero">
                        <Swiper key={dir} dir={dir} loop={slides.length > 1}
                    pagination={{clickable: true}} autoplay={slides.length > 1 ? {delay: 5000, disableOnInteraction: true} : false}
                    a11y={{enabled: true}} className="aspect-[16/7] w-full bg-muted sm:aspect-[21/8]">
                {slides.map((s, i) => (
                    <SwiperSlide key={s.id} aria-label={t('HERO_SLIDE', {index: i + 1, total: slides.length})}>
                        <BannerImage banner={s} priority={i === 0} sizes="100vw" className="object-cover"/>
                    </SwiperSlide>
                ))}
            </Swiper>
        </section>
    );
}
