'use client'
import {useTranslations} from 'next-intl';
import {ArrowDownIcon} from 'lucide-react';
import {useDir} from '@store-front/i18n/use-dir';
import type {Banner} from '@store-front/types';
import {cn} from '@store-front/ui/lib/utils';
import {BannerImage} from '@store-front/ui/banner-image';
import {Swiper, SwiperSlide} from '@store-front/ui/swiper';

/**
 * The floor entrance: the price board (a flat field of the merchant primary — store name at signage
 * scale, the store's real facts, SHOP NOW) answering the merchant's slider across one seam. With no
 * slider the banner stands in; with neither, the board alone is the entrance, full width, still finished.
 * Swiper needs `dir` explicitly and is re-keyed on dir so a locale switch re-initialises it.
 */
export function Hero({slides, banner, storeName, facts, anchor, cta, autoplay = 6}: {
    slides: Banner[]; banner?: Banner; storeName: string; facts: string[]; anchor?: string;
    /** The builder hero's own call to action; wins over the legacy in-page anchor. */
    cta?: {label: string; href: string};
    /** Seconds between slides, or `false` for a still slider — the builder's hero fields. */
    autoplay?: number | false;
}) {
    const t = useTranslations('PAGE.HOME');
    const dir = useDir();
    const hasImage = slides.length > 0 || !!banner?.desktopUrl;
    // the stage never swallows the first viewport: the first rail's crates stay in reach below it
    const stage = 'stage relative w-full overflow-hidden rounded-card border-2 bg-card aspect-[4/3] max-h-[38vh] sm:aspect-[16/9] sm:max-h-[46vh] lg:max-h-[56vh]';
    return (
        <section aria-roledescription={slides.length > 1 ? 'carousel' : undefined} aria-label={storeName}
                 className={cn('grid gap-3', hasImage && 'lg:grid-cols-[2fr_3fr] lg:items-stretch')}>
            <div className={cn('board relative flex min-w-0 flex-col justify-end gap-4 p-5 sm:p-6 lg:gap-5 lg:p-8',
                hasImage ? 'min-h-[15rem]' : 'min-h-[42vh]')}>
                <h1 className="signage text-5xl [overflow-wrap:anywhere] sm:text-6xl lg:text-[clamp(3.5rem,5.5vw,6rem)]">
                    <bdi dir="auto">{storeName}</bdi>
                </h1>
                {facts.length > 0 && <p className="text-sm font-semibold tabular-nums opacity-85 sm:text-base">{facts.join(' · ')}</p>}
                {(cta || anchor) && (
                    <div className="flex flex-wrap gap-2 pt-1">
                        <a href={cta ? cta.href : `#${anchor}`}
                           className="signage inline-flex h-12 items-center gap-2 rounded-control bg-primary-foreground px-5 text-lg text-primary transition-opacity duration-(--motion-fast) hover:opacity-90 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-foreground">
                            {cta ? <bdi dir="auto">{cta.label}</bdi> : t('SHOP_NOW')}{!cta && <ArrowDownIcon className="size-5"/>}
                        </a>
                    </div>
                )}
            </div>

            {slides.length > 0 ? (
                <div className="min-w-0">
                    <Swiper key={dir} dir={dir} loop={slides.length > 1}
                            pagination={{clickable: true}}
                            autoplay={slides.length > 1 && autoplay !== false ? {delay: autoplay * 1000, disableOnInteraction: true} : false}
                            a11y={{enabled: true}} className={stage}>
                        {slides.map((s, i) => (
                            <SwiperSlide key={s.id} aria-label={t('HERO_SLIDE', {index: i + 1, total: slides.length})}>
                                <BannerImage banner={s} priority={i === 0} sizes="(max-width: 1344px) 100vw, 820px" className="object-cover"/>
                            </SwiperSlide>
                        ))}
                    </Swiper>
                </div>
            ) : banner?.desktopUrl ? (
                <div className={cn(stage, 'min-w-0')}>
                    <BannerImage banner={banner} priority sizes="(max-width: 1344px) 100vw, 820px" className="object-cover"/>
                </div>
            ) : null}
        </section>
    );
}
