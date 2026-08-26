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
 * The picture panel of the masthead: the merchant's slider printed inside a ruled frame, never full-bleed
 * — the menu has to stay in the first viewport. Multiple slides get numbered page stubs along the bottom
 * edge, the way a folded menu numbers its faces. Swiper needs `dir` explicitly (it does not read CSS
 * direction) and is re-keyed on dir so a locale switch re-initialises it.
 */
export function Hero({slides}: { slides: SliderImage[] }) {
    const t = useTranslations('PAGE.HOME');
    const dir = useDir();
    if (slides.length === 0) return null;
    return (
        <div className="sheet crop relative aspect-[3/1] w-full min-w-0 border border-foreground sm:aspect-[21/9] lg:aspect-[4/3]" aria-roledescription="carousel">
            <Swiper key={dir} dir={dir} modules={[Pagination, Autoplay, A11y]} loop={slides.length > 1}
                    pagination={slides.length > 1 ? {clickable: true, renderBullet: (i, cls) => `<button class="${cls}" type="button">${i + 1}</button>`} : false}
                    autoplay={slides.length > 1 ? {delay: 6000, disableOnInteraction: true} : false}
                    a11y={{enabled: true}} className="size-full bg-muted">
                {slides.map((s, i) => (
                    <SwiperSlide key={`${s.url}-${i}`} aria-label={t('HERO_SLIDE', {index: i + 1, total: slides.length})}>
                        <Image src={s.url} alt="" fill priority={i === 0} sizes="(max-width: 1024px) 100vw, 40vw" className="object-cover"/>
                    </SwiperSlide>
                ))}
            </Swiper>
        </div>
    );
}
