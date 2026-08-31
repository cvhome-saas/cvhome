'use client'
import {useState} from 'react';
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import {ChevronLeftIcon, ChevronRightIcon} from 'lucide-react';
import {useDir} from '@store-front/i18n/use-dir';
import type {Banner} from '@store-front/types';
import {Swiper, SwiperSlide, type SwiperApi} from '@store-front/ui/swiper';

/**
 * The store's CMS banners in a plate frame with an NN / NN counter and plate arrows — a crate window,
 * not a carousel with dots. Swiper is re-keyed on `dir` so RTL swipes the right way.
 */
export function HeroFrame({slides}: { slides: Banner[] }) {
    const t = useTranslations('PAGE.HOME');
    const dir = useDir();
    const [index, setIndex] = useState(0);
    const [swiper, setSwiper] = useState<SwiperApi | null>(null);
    if (slides.length === 0) return null;
    const pad = (n: number) => String(n).padStart(2, '0');
    return (
        <section aria-roledescription="carousel" className="plate relative isolate min-w-0 overflow-hidden">
            <Swiper key={dir} dir={dir} loop={slides.length > 1} onSwiper={setSwiper} onSlideChange={s => setIndex(s.realIndex)}
                    autoplay={slides.length > 1 ? {delay: 6000, disableOnInteraction: true} : false} a11y={{enabled: true}} className="aspect-[4/3] w-full bg-muted lg:aspect-[16/9]">
                {slides.map((s, i) => (
                    <SwiperSlide key={s.id} aria-label={t('HERO_SLIDE', {index: i + 1, total: slides.length})}>
                        <Image src={s.desktopUrl ?? ''} alt={s.altText ?? ''} fill priority={i === 0} sizes="(max-width: 1024px) 100vw, 55vw" className="object-cover"/>
                    </SwiperSlide>
                ))}
            </Swiper>
            <div className="hazard-soft absolute inset-x-0 bottom-0 z-10 h-2" aria-hidden/>
            <div className="absolute bottom-4 end-3 z-10 flex items-stretch font-mono text-xs">
                <span className="plate flex items-center px-2 tabular-nums" dir="ltr">{pad(index + 1)} / {pad(slides.length)}</span>
                {slides.length > 1 && (
                    <>
                        <button type="button" onClick={() => swiper?.slidePrev()} aria-label={t('HERO_SLIDE', {index: ((index - 1 + slides.length) % slides.length) + 1, total: slides.length})} className="plate -ms-px flex size-8 items-center justify-center hover:bg-foreground hover:text-background"><ChevronLeftIcon className="size-4 rtl:rotate-180"/></button>
                        <button type="button" onClick={() => swiper?.slideNext()} aria-label={t('HERO_SLIDE', {index: ((index + 1) % slides.length) + 1, total: slides.length})} className="tag -ms-px flex size-8 items-center justify-center rounded-none !pe-0 hover:bg-primary-hover"><ChevronRightIcon className="size-4 rtl:rotate-180"/></button>
                    </>
                )}
            </div>
        </section>
    );
}
