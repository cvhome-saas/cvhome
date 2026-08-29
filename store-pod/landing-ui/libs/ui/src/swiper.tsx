'use client'

import {
    Children,
    type CSSProperties,
    type HTMLAttributes,
    type ReactElement,
    type ReactNode,
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState,
} from 'react';
import {useTranslations} from 'next-intl';
import {ChevronLeftIcon, ChevronRightIcon} from 'lucide-react';
import {cn} from './lib/utils';

type Direction = 'ltr' | 'rtl';
type SlideConfig = {slidesPerView?: number; spaceBetween?: number};

export interface SwiperApi {
    slideNext: () => void;
    slidePrev: () => void;
}

interface SwiperProps {
    children: ReactNode;
    className?: string;
    dir?: Direction;
    loop?: boolean;
    navigation?: boolean;
    pagination?: boolean | {clickable?: boolean; renderBullet?: (index: number, className: string) => string};
    autoplay?: false | {delay?: number; disableOnInteraction?: boolean};
    a11y?: {enabled?: boolean};
    slidesPerView?: number;
    spaceBetween?: number;
    breakpoints?: Record<number, SlideConfig>;
    onSwiper?: (api: SwiperApi) => void;
    onSlideChange?: (state: {realIndex: number}) => void;
    observer?: boolean;
    observeParents?: boolean;
    observeSlideChildren?: boolean;
    resizeObserver?: boolean;
}

type CarouselStyle = CSSProperties & { [key: `--carousel-${string}`]: string | number };

function cssConfig({slidesPerView, spaceBetween}: Required<SlideConfig>) {
    const visibleSlides = Math.max(slidesPerView, 0.1);
    const gap = Math.max(spaceBetween, 0);
    const occupiedGaps = Math.max(visibleSlides - 1, 0);
    return {
        basis: `calc((100% - ${gap * occupiedGaps}px) / ${visibleSlides})`,
        gap: `${gap}px`,
    };
}

/**
 * The small subset of Swiper the themes use, implemented with native scrolling and scroll snap.
 *
 * It deliberately keeps Swiper's public class names so existing theme control styling remains valid,
 * but it does not clone slides, synchronously measure every card, or ship Swiper's layout engine.
 */
export function Swiper({
    children,
    className,
    dir = 'ltr',
    loop = false,
    navigation = false,
    pagination = false,
    autoplay = false,
    slidesPerView = 1,
    spaceBetween = 0,
    breakpoints,
    onSwiper,
    onSlideChange,
}: SwiperProps) {
    const t = useTranslations('COMMON');
    const viewportRef = useRef<HTMLDivElement>(null);
    const interactedRef = useRef(false);
    const [activeIndex, setActiveIndex] = useState(0);
    const slides = Children.toArray(children);
    const count = slides.length;

    const configAt = useCallback((point: number): Required<SlideConfig> => {
        const entries = Object.entries(breakpoints ?? {})
            .map(([width, config]) => [Number(width), config] as const)
            .filter(([width]) => width <= point)
            .sort(([a], [b]) => a - b);
        const selected = entries[entries.length - 1]?.[1];
        return {
            slidesPerView: selected?.slidesPerView ?? slidesPerView,
            spaceBetween: selected?.spaceBetween ?? spaceBetween,
        };
    }, [breakpoints, slidesPerView, spaceBetween]);

    const style = useMemo<CarouselStyle>(() => {
        const base = cssConfig({slidesPerView, spaceBetween});
        const sm = cssConfig(configAt(640));
        const lg = cssConfig(configAt(1024));
        const xl = cssConfig(configAt(1280));
        const wide = cssConfig(configAt(1400));
        return {
            '--carousel-basis': base.basis,
            '--carousel-gap': base.gap,
            '--carousel-basis-sm': sm.basis,
            '--carousel-gap-sm': sm.gap,
            '--carousel-basis-lg': lg.basis,
            '--carousel-gap-lg': lg.gap,
            '--carousel-basis-xl': xl.basis,
            '--carousel-gap-xl': xl.gap,
            '--carousel-basis-wide': wide.basis,
            '--carousel-gap-wide': wide.gap,
        };
    }, [configAt, slidesPerView, spaceBetween]);

    const goTo = useCallback((index: number, manual = true) => {
        if (count === 0) return;
        const next = loop ? (index + count) % count : Math.min(Math.max(index, 0), count - 1);
        const element = viewportRef.current?.children.item(next);
        if (!(element instanceof HTMLElement)) return;
        if (manual && autoplay && autoplay.disableOnInteraction !== false) interactedRef.current = true;
        element.scrollIntoView({behavior: 'smooth', block: 'nearest', inline: 'start'});
    }, [autoplay, count, loop]);

    const api = useMemo<SwiperApi>(() => ({
        slideNext: () => goTo(activeIndex + 1),
        slidePrev: () => goTo(activeIndex - 1),
    }), [activeIndex, goTo]);

    useEffect(() => onSwiper?.(api), [api, onSwiper]);
    useEffect(() => onSlideChange?.({realIndex: activeIndex}), [activeIndex, onSlideChange]);

    useEffect(() => {
        const viewport = viewportRef.current;
        if (!viewport || count < 2) return;
        const observer = new IntersectionObserver(entries => {
            const visible = entries
                .filter(entry => entry.isIntersecting)
                .sort((a, b) => b.intersectionRatio - a.intersectionRatio)[0];
            if (!(visible?.target instanceof HTMLElement)) return;
            const index = Number(visible.target.dataset.carouselIndex);
            if (Number.isInteger(index)) setActiveIndex(index);
        }, {root: viewport, threshold: [0.51, 0.75, 0.99]});
        for (const slide of viewport.children) observer.observe(slide);
        return () => observer.disconnect();
    }, [count]);

    useEffect(() => {
        if (!autoplay || count < 2 || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
        const delay = Math.max(5000, autoplay.delay ?? 5000);
        const timer = window.setInterval(() => {
            if (document.hidden || interactedRef.current) return;
            goTo(activeIndex + 1, false);
        }, delay);
        return () => window.clearInterval(timer);
    }, [activeIndex, autoplay, count, goTo]);

    const numbered = typeof pagination === 'object' && typeof pagination.renderBullet === 'function';
    const previousDisabled = !loop && activeIndex === 0;
    const nextDisabled = !loop && activeIndex === count - 1;

    return (
        <div className={cn('swiper', className)} dir={dir} style={style}>
            <div ref={viewportRef} className="swiper-wrapper">
                {slides.map((slide, index) => (
                    <div key={(slide as ReactElement).key ?? index} className="swiper-slide" data-carousel-index={index}>
                        {slide}
                    </div>
                ))}
            </div>
            {navigation && count > 1 && (
                <>
                    <button type="button" className={cn('swiper-button-prev', previousDisabled && 'swiper-button-disabled')}
                            disabled={previousDisabled} aria-label={t('PREVIOUS')} onClick={api.slidePrev}>
                        <ChevronLeftIcon aria-hidden/>
                    </button>
                    <button type="button" className={cn('swiper-button-next', nextDisabled && 'swiper-button-disabled')}
                            disabled={nextDisabled} aria-label={t('NEXT')} onClick={api.slideNext}>
                        <ChevronRightIcon aria-hidden/>
                    </button>
                </>
            )}
            {pagination && count > 1 && (
                <div className="swiper-pagination">
                    {slides.map((slide, index) => (
                        <button key={(slide as ReactElement).key ?? index} type="button"
                                className="swiper-pagination-target"
                                aria-label={(slide as ReactElement<HTMLAttributes<HTMLElement>>).props['aria-label']
                                    ?? t('CAROUSEL_SLIDE', {index: index + 1, total: count})}
                                aria-current={index === activeIndex ? 'true' : undefined}
                                onClick={() => goTo(index)}>
                            <span className={cn('swiper-pagination-bullet', index === activeIndex && 'swiper-pagination-bullet-active')}
                                  data-numbered={numbered || undefined} aria-hidden>
                                {numbered ? index + 1 : null}
                            </span>
                        </button>
                    ))}
                </div>
            )}
        </div>
    );
}

/** A semantic slide boundary retained for source compatibility with existing themes. */
export function SwiperSlide({className, ...props}: HTMLAttributes<HTMLDivElement>) {
    return <div className={className} role="group" {...props}/>;
}
