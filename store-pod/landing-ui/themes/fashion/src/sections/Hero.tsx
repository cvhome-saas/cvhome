import {Link} from '@store-front/i18n/navigation';
import type {PageContext} from '@store-front/theme';
import type {Banner} from '@store-front/types';
import {cn} from '@store-front/ui/lib/utils';
import {PosterImage} from '../components/PosterImage';

/**
 * A slider image pasted as a big peeling poster. No carousel: each of the merchant's images gets its own
 * place on the wall — the first one beside the name sheet, every further slide pasted under it.
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

/** A pasted call to action: the day-glo primary, and paper strips for the secondary destinations. */
export interface WallAction {
    label: string;
    href: string;
}

/**
 * The store's name sheet: H1 in poster caps over the city and founding year as printed facts, the
 * merchant's CTA as the day-glo strip and their slide links as pasted paper strips — the sheet sells,
 * it does not only announce.
 */
export function HeadlineSheet({ctx, heading, subheading, cta, strips = [], className}: {
    ctx: PageContext; heading?: string; subheading?: string; cta?: WallAction; strips?: WallAction[]; className?: string;
}) {
    const {store} = ctx;
    const year = store.inBusinessSince ? new Date(store.inBusinessSince).getFullYear() : undefined;
    const facts = [store.address?.city, year && !Number.isNaN(year) ? String(year) : undefined].filter(Boolean) as string[];
    const tilts = ['[--tilt:0.8deg]', '[--tilt:-0.7deg]', '[--tilt:0.5deg]'];
    return (
        <div className={cn('sheet sheen peel flex min-w-0 flex-col gap-5 p-5 sm:p-7 lg:p-8', className)}>
            {facts.length > 0 && <p className="font-display text-xs uppercase tracking-[0.2em] text-muted-foreground sm:text-sm">{facts.join(' · ')}</p>}
            <h1 className="text-start font-display text-5xl uppercase leading-[0.88] [overflow-wrap:anywhere] sm:text-6xl lg:text-[clamp(3rem,5vw,5.25rem)]"><bdi dir="auto">{heading ?? store.name}</bdi></h1>
            {subheading && <p className="max-w-prose text-sm text-muted-foreground"><bdi dir="auto">{subheading}</bdi></p>}
            {(cta || strips.length > 0) && (
                <div className="mt-auto flex flex-wrap gap-2 pt-2">
                    {cta && (
                        <Link prefetch={false} href={cta.href} className="glo h-11 px-5 text-base [--tilt:0deg]">
                            <bdi dir="auto">{cta.label}</bdi>
                        </Link>
                    )}
                    {strips.slice(0, 3).map((strip, index) => (
                        <Link key={strip.href + strip.label} prefetch={false} href={strip.href}
                              className={cn('strip strip-hover h-11 text-sm', tilts[index % tilts.length])}>
                            <bdi dir="auto">{strip.label}</bdi>
                        </Link>
                    ))}
                </div>
            )}
        </div>
    );
}
