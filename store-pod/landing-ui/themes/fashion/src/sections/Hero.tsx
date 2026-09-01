import type {PageContext} from '@store-front/theme';
import type {Banner} from '@store-front/types';
import {cn} from '@store-front/ui/lib/utils';
import {PosterImage} from '../components/PosterImage';

/**
 * A slider image pasted as a big peeling poster. No carousel: each of the merchant's images gets its own
 * place on the wall — the first one beside the name sheet, the rest pasted in a row under it.
 */
export function SlidePoster({slide, index, total, storeName, priority, className, ratio = '4 / 3'}: {
    slide: Banner; index: number; total: number; storeName: string; priority?: boolean; className?: string; ratio?: string;
}) {
    // the ratio rides a variable so a `lg:aspect-auto` on the same element can still win (an inline style could not be overridden)
    return (
        <figure className={cn('sheet sheen peel relative min-w-0 overflow-hidden aspect-(--ratio)', className)} style={{'--ratio': ratio} as React.CSSProperties}>
            <PosterImage src={slide.desktopUrl ?? ''} mobileSrc={slide.mobileUrl ?? undefined} alt={slide.altText ?? ''} title={storeName} tone="faint" meta={`${String(index + 1).padStart(2, '0')} / ${String(total).padStart(2, '0')}`}
                         sizes="(max-width: 1024px) 100vw, 40vw" priority={priority}/>
        </figure>
    );
}

/** The store's name sheet: H1 in poster caps over the city and founding year as printed facts. */
export function HeadlineSheet({ctx, heading, subheading, className}: {
    ctx: PageContext; heading?: string; subheading?: string; className?: string;
}) {
    const {store} = ctx;
    const year = store.inBusinessSince ? new Date(store.inBusinessSince).getFullYear() : undefined;
    const facts = [store.address?.city, year && !Number.isNaN(year) ? String(year) : undefined].filter(Boolean) as string[];
    return (
        <div className={cn('sheet sheen peel flex min-w-0 flex-col gap-5 p-5 sm:p-7 lg:p-8', className)}>
            {facts.length > 0 && <p className="font-display text-xs uppercase tracking-[0.2em] text-muted-foreground sm:text-sm">{facts.join(' · ')}</p>}
            <h1 className="text-start font-display text-5xl uppercase leading-[0.88] [overflow-wrap:anywhere] sm:text-6xl lg:text-[clamp(3rem,5vw,5.25rem)]"><bdi dir="auto">{heading ?? store.name}</bdi></h1>
            {subheading && <p className="mt-auto max-w-prose text-sm text-muted-foreground"><bdi dir="auto">{subheading}</bdi></p>}
        </div>
    );
}
