'use client'
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import {ArrowDownIcon} from 'lucide-react';
import {useDir} from '@store-front/i18n/use-dir';
import type {Banner} from '@store-front/types';
import {cn} from '@store-front/ui/lib/utils';
import {Swiper, SwiperSlide} from '@store-front/ui/swiper';

/**
 * The cover: the merchant's slider as a ruled stage (21:9 desktop, 4:3 mobile, height-capped) with numbered page stubs
 * at its bottom-end, and the title block — a flat field of the merchant primary — overlapping its
 * bottom-start: the store name at up to 6rem (about six times body), the store's real facts, SHOP NOW. With no slider image
 * the banner stands in; with neither, the title block alone is the cover, full width, still finished.
 * Swiper needs `dir` explicitly and is re-keyed on dir so a locale switch re-initialises it.
 */
export function Hero({slides, banner, storeName, facts, anchor}: {
    slides: Banner[]; banner?: Banner; storeName: string; facts: string[]; anchor?: string;
}) {
    const t = useTranslations('PAGE.HOME');
    const dir = useDir();
    const hasImage = slides.length > 0 || !!banner?.desktopUrl;
    // the stage never swallows the first viewport: 4:3 on phones, 21:9 on desktop, both capped so the name, the action
    // and the first running head stay in reach
    const stage = 'aspect-[4/3] max-h-[40vh] w-full bg-muted sm:aspect-[16/9] sm:max-h-[50vh] lg:aspect-[21/9] lg:max-h-[58vh]';
    return (
        <section aria-roledescription={slides.length > 1 ? 'carousel' : undefined} aria-label={storeName} className="cover relative">
            {slides.length > 0 ? (
                <div className="relative border">
                    <Swiper key={dir} dir={dir} loop={slides.length > 1}
                            pagination={{clickable: true, renderBullet: (i, cls) => `<button type="button" class="${cls}" aria-label="${t('HERO_SLIDE', {index: i + 1, total: slides.length})}">${i + 1}</button>`}}
                            autoplay={slides.length > 1 ? {delay: 6000, disableOnInteraction: true} : false}
                            a11y={{enabled: true}} className={stage}>
                        {slides.map((s, i) => (
                            <SwiperSlide key={s.id} aria-label={t('HERO_SLIDE', {index: i + 1, total: slides.length})}>
                                <Image src={s.desktopUrl ?? ''} alt={s.altText ?? ''} fill priority={i === 0} sizes="(max-width: 1344px) 100vw, 1344px" className="object-cover"/>
                            </SwiperSlide>
                        ))}
                    </Swiper>
                </div>
            ) : banner?.desktopUrl ? (
                <div className={cn('relative border', stage)}>
                    <Image src={banner.desktopUrl} alt={banner.altText ?? ''} fill priority sizes="(max-width: 1344px) 100vw, 1344px" className="object-cover"/>
                </div>
            ) : null}

            <div className={cn('plate relative z-10 flex flex-col gap-4 p-5 sm:p-6 lg:gap-5 lg:p-8',
                hasImage ? 'lg:absolute lg:bottom-0 lg:start-0 lg:w-[min(40rem,48%)]' : 'min-h-[45vh] justify-end lg:min-h-[52vh]')}>
                <h1 className="display display-black text-5xl leading-[0.9] [overflow-wrap:anywhere] sm:text-6xl lg:text-[clamp(3.5rem,6vw,6rem)]">
                    <bdi dir="auto">{storeName}</bdi>
                </h1>
                {facts.length > 0 && <p className="text-sm tabular-nums opacity-85 sm:text-base">{facts.join(' · ')}</p>}
                {anchor && (
                    <div className="flex flex-wrap gap-2 pt-1">
                        <a href={`#${anchor}`}
                           className="inline-flex h-11 items-center gap-2 rounded-control bg-primary-foreground px-5 text-base font-semibold text-primary transition-opacity duration-(--motion-fast) hover:opacity-90 focus-visible:outline-primary-foreground">
                            {t('SHOP_NOW')}<ArrowDownIcon className="size-4"/>
                        </a>
                    </div>
                )}
            </div>
        </section>
    );
}
