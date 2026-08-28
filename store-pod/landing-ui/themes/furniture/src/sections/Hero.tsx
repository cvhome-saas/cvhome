'use client'
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import {Swiper, SwiperSlide} from 'swiper/react';
import {A11y, Pagination} from 'swiper/modules';
import 'swiper/css';
import 'swiper/css/pagination';
import {useDir} from '@store-front/i18n/use-dir';
import type {Banner} from '@store-front/types';
import {PlanPlate} from '../components/PlanPlate';

/**
 * The window beside the directory board: the store's CMS banners, cut square in a hairline frame
 * with a ruled caption plate in the lower corner. Swiper needs `dir` explicitly (it does not read CSS
 * direction) and is re-keyed on dir so a locale switch re-initialises it.
 *
 * With no slider image the window does not collapse and does not show a grey box — it shows the drawn
 * department plate instead, and the first viewport is still finished.
 *
 * The window is a grid cell that stretches to the directory board's height, so its size is only final
 * after the board has laid out. Swiper measures once on mount and would hold a stale width — leaving the
 * slide track short of the frame, so the window renders half empty — unless it observes its container and
 * its parents. Hence `observer` / `observeParents` / `resizeObserver`.
 *
 * The slides do not advance on their own. This theme authorises exactly one moving thing — a figure that
 * rolls when it changes — and an unpausable 6-second carousel is both a second motion and content that
 * moves for longer than five seconds with no way to stop it (WCAG 2.2.2). The pagination gives a reader
 * every slide on demand.
 */
export function Hero({slides, caption, planCaption}: { slides: Banner[]; caption: string; planCaption: string }) {
    const t = useTranslations('PAGE.HOME');
    const dir = useDir();
    return (
        <section aria-roledescription={slides.length > 1 ? 'carousel' : undefined}
                 className="window relative h-full min-h-[16rem] w-full">
            {slides.length === 0 ? (
                <div className="terrazzo flex size-full items-center justify-center p-6 text-foreground">
                    <PlanPlate caption={planCaption} label={planCaption}/>
                </div>
            ) : (
                <Swiper key={dir} dir={dir} modules={[Pagination, A11y]} loop={slides.length > 1}
                        observer observeParents observeSlideChildren resizeObserver
                        pagination={{clickable: true}}
                        a11y={{enabled: true}} className="size-full">
                    {slides.map((s, i) => (
                        <SwiperSlide key={s.id} aria-label={t('HERO_SLIDE', {index: i + 1, total: slides.length})}>
                            <div className="relative size-full">
                                <Image src={s.desktopUrl ?? ''} alt={s.altText ?? ''} fill priority={i === 0} sizes="(max-width: 1024px) 100vw, 55vw" className="object-cover"/>
                            </div>
                        </SwiperSlide>
                    ))}
                </Swiper>
            )}
            <p className="sign pointer-events-none absolute bottom-0 start-0 z-10 max-w-[85%] rounded-e-card bg-background/95 px-3 py-2 text-[0.6875rem] text-foreground shadow-sm">
                {caption}
            </p>
        </section>
    );
}
