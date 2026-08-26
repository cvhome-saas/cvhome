'use client'
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import {Swiper, SwiperSlide} from 'swiper/react';
import {A11y, Autoplay, Pagination} from 'swiper/modules';
import 'swiper/css';
import 'swiper/css/pagination';
import {useDir} from '@store-front/i18n/use-dir';
import type {Banner} from '@store-front/types';
import {cn} from '@store-front/ui/lib/utils';
import {StarMark} from '../components/Marks';

export type CoverLine = { id: string; title: string; count: number; href: string };

/**
 * The cover. A flooded, screentoned field carrying the store's name at cover scale, the issue's real cover
 * lines under it, and the merchant's first slider image bleeding off the end edge. With no slider image
 * the cover is type-only and still finished — a magazine cover is a headline before it is a photograph.
 * Swiper needs `dir` explicitly (it does not read CSS direction) and is re-keyed on dir so a locale switch
 * re-initialises it.
 */
export function Hero({slides, storeName, lines, actionHref}: {
    slides: Banner[]; storeName: string; lines: CoverLine[]; actionHref?: string;
}) {
    const t = useTranslations('PAGE.HOME');
    const dir = useDir();
    const hasSlides = slides.length > 0;
    return (
        <section className="flood tone-light hair border-b-2" aria-labelledby="cover-title">
            <div className={cn('grid items-stretch gap-0 px-gutter lg:px-0',
                hasSlides ? 'cover-grid' : 'mx-auto max-w-content')}>
                <div className={cn('flex flex-col justify-center gap-6 py-10 lg:py-16', hasSlides && 'lg:col-start-2 lg:pe-12')}>
                    <h1 id="cover-title" className="display text-4xl sm:text-5xl lg:text-6xl">{storeName}</h1>
                    {lines.length > 0 && (
                        <ul className="flex flex-col gap-2.5">
                            {lines.map((line, i) => (
                                <li key={line.id}>
                                    <a href={line.href} className="group flex flex-wrap items-baseline gap-x-3 gap-y-1">
                                        <span aria-hidden className="figure text-sm">{String(i + 1).padStart(2, '0')}</span>
                                        <span className="display text-xl underline-offset-4 group-hover:underline sm:text-2xl">{line.title}</span>
                                        <span className="figure text-sm">{t('ITEMS_COUNT', {count: line.count})}</span>
                                    </a>
                                </li>
                            ))}
                        </ul>
                    )}
                    {actionHref && (
                        <a href={actionHref}
                           className="ink-field cover-line inline-flex w-fit items-center gap-2 rounded-control px-6 py-3 text-base transition-transform duration-(--motion-fast) hover:-translate-y-0.5">
                            <StarMark className="size-3.5" aria-hidden/>{t('SHOP_NOW')}
                        </a>
                    )}
                </div>
                {hasSlides && (
                    <div className="hair -mx-gutter min-w-0 overflow-hidden border-t-2 lg:col-start-3 lg:mx-0 lg:border-s-2 lg:border-t-0">
                        <Swiper key={dir} dir={dir} modules={[Pagination, Autoplay, A11y]} loop={slides.length > 1}
                                pagination={{clickable: true}}
                                autoplay={slides.length > 1 ? {delay: 5000, disableOnInteraction: true} : false}
                                a11y={{enabled: true}} className="!h-full !w-full">
                            {slides.map((s, i) => (
                                <SwiperSlide key={s.id} aria-label={t('HERO_SLIDE', {index: i + 1, total: slides.length})}
                                             className="relative !h-auto min-h-64 self-stretch lg:min-h-[30rem]">
                                    <Image src={s.desktopUrl ?? ''} alt={s.altText ?? ''} fill priority={i === 0} sizes="(max-width: 1024px) 100vw, 40vw" className="object-cover"/>
                                </SwiperSlide>
                            ))}
                        </Swiper>
                    </div>
                )}
            </div>
        </section>
    );
}
