'use client'
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import {Swiper, SwiperSlide} from 'swiper/react';
import {A11y, Autoplay, Pagination} from 'swiper/modules';
import 'swiper/css';
import 'swiper/css/pagination';
import {useDir} from '@store-front/i18n/use-dir';
import type {SliderImage} from '@store-front/types';

/**
 * Homepage hero from the merchant's slider images. Swiper needs `dir` explicitly (it does not read CSS
 * direction) and is re-keyed on dir so a locale switch re-initialises it.
 */
export function Hero({slides}: { slides: SliderImage[] }) {
    const t = useTranslations('PAGE.HOME');
    const dir = useDir();
    if (slides.length === 0) return null;
    return (
        <section aria-roledescription="carousel" className="jewellery-hero">
            <Swiper key={dir} dir={dir} modules={[Pagination, Autoplay, A11y]} loop={slides.length > 1}
                    pagination={{clickable: true}} autoplay={slides.length > 1 ? {delay: 5000, disableOnInteraction: true} : false}
                    a11y={{enabled: true}} className="aspect-[16/7] w-full bg-muted sm:aspect-[21/8]">
                {slides.map((s, i) => (
                    <SwiperSlide key={`${s.url}-${i}`} aria-label={t('HERO_SLIDE', {index: i + 1, total: slides.length})}>
                        <Image src={s.url} alt="" fill priority={i === 0} sizes="100vw" className="object-cover"/>
                    </SwiperSlide>
                ))}
            </Swiper>
        </section>
    );
}
