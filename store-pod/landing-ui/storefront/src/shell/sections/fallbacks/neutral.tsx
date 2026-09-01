import type {ComponentType} from 'react';
import Image from 'next/image';
import {Link} from '@store-front/i18n/navigation';
import {Gift, Headset, RotateCcw, ShieldCheck, Sparkles, Star, Truck} from 'lucide-react';
import type {SectionChrome} from '@store-front/ui/sections/compose';
import {SectionHeading} from './shared';

/**
 * The neutral chrome: the shell's voice for every composable section kind, expressed as the same
 * primitives a theme supplies. Undesigned on purpose, like `default-search-page` — tokens only, so
 * it takes on the active theme's type, colour and radius. A theme replaces the voice by passing its
 * own chrome to `sectionsFromChrome`; it can never replace the semantics.
 */

const ICONS: Record<string, ComponentType<{className?: string; 'aria-hidden'?: boolean}>> = {
    truck: Truck, shield: ShieldCheck, refresh: RotateCcw, star: Star, headset: Headset, gift: Gift,
};

export const neutralChrome: SectionChrome = {
    Heading: ({title, subtitle, meta}) => <SectionHeading title={title} subtitle={subtitle} meta={meta}/>,

    Badge: ({icon, title, body}) => {
        const Icon = ICONS[icon] ?? Sparkles;
        return (
            <span className="flex max-w-52 flex-col items-center gap-1.5 p-3 text-center">
                <Icon aria-hidden className="size-6 text-muted-foreground"/>
                <span className="text-sm font-medium"><bdi dir="auto">{title}</bdi></span>
                {body && <span className="text-xs text-muted-foreground"><bdi dir="auto">{body}</bdi></span>}
            </span>
        );
    },

    NavToken: ({label, count, href}) => (
        <Link prefetch={false} href={href}
              className="inline-flex items-center gap-2 rounded-full border px-4 py-2 text-sm transition-colors hover:bg-muted">
            <bdi dir="auto">{label}</bdi>
            {count !== undefined && <span className="text-xs tabular-nums text-muted-foreground">{count}</span>}
        </Link>
    ),

    Band: ({message, action, backgroundSrc}) => (
        <div className="relative flex min-h-12 flex-wrap items-center justify-center gap-x-6 gap-y-2 overflow-hidden px-5 py-3 text-center">
            {backgroundSrc && (
                <>
                    <Image src={backgroundSrc} alt="" fill className="object-cover"/>
                    <span aria-hidden className="absolute inset-0 bg-black/45"/>
                </>
            )}
            <span className={`relative text-sm font-semibold ${backgroundSrc ? 'text-white' : ''}`}>
                <bdi dir="auto">{message}</bdi>
            </span>
            {action && (
                <Link prefetch={false} href={action.href}
                      className={`relative text-sm font-medium underline underline-offset-4 ${backgroundSrc ? 'text-white' : ''}`}>
                    <bdi dir="auto">{action.label}</bdi>
                </Link>
            )}
        </div>
    ),

    Panel: ({children, center}) => (
        <div className={`rounded-lg border px-5 py-3 ${center ? 'p-8 text-center' : 'divide-y'}`}>{children}</div>
    ),

    Quote: ({quote, author}) => (
        <figure className="rounded-lg border p-5">
            <blockquote className="text-sm leading-relaxed"><bdi dir="auto">“{quote}”</bdi></blockquote>
            {author && (
                <figcaption className="mt-3 text-xs text-muted-foreground"><bdi dir="auto">{author}</bdi></figcaption>
            )}
        </figure>
    ),

    MediaFigure: ({src, alt, caption, href, contained}) => {
        const figure = (
            <figure>
                <div className={`relative w-full overflow-hidden rounded-md bg-muted ${contained ? 'aspect-[4/3]' : 'aspect-[21/9]'}`}>
                    <Image src={src!} alt={alt} fill className={contained ? 'object-contain' : 'object-cover'}/>
                </div>
                {caption && (
                    <figcaption className="mt-2 text-center text-sm text-muted-foreground">
                        <bdi dir="auto">{caption}</bdi>
                    </figcaption>
                )}
            </figure>
        );
        return href ? <Link prefetch={false} href={href} className="block">{figure}</Link> : figure;
    },

    BrandLabel: ({src, name, href}) => {
        const label = (
            <span className="flex w-28 flex-col items-center gap-1.5 opacity-80">
                {src && (
                    <span className="relative block h-10 w-full">
                        <Image src={src} alt={name ?? ''} fill className="object-contain"/>
                    </span>
                )}
                {name && <span className="text-xs text-muted-foreground"><bdi dir="auto">{name}</bdi></span>}
            </span>
        );
        return href ? <Link prefetch={false} href={href}>{label}</Link> : label;
    },

    VideoFrame: ({player}) => (
        <div className="relative aspect-video w-full overflow-hidden rounded-md bg-muted">{player}</div>
    ),

    PostCard: ({href, title, excerpt, imageSrc}) => (
        <Link prefetch={false} href={href} className="group block">
            {imageSrc && (
                <span className="relative mb-3 block aspect-[3/2] overflow-hidden rounded-md bg-muted">
                    <Image src={imageSrc} alt={title} fill className="object-cover transition-transform duration-300 group-hover:scale-105"/>
                </span>
            )}
            <span className="block font-medium group-hover:underline"><bdi dir="auto">{title}</bdi></span>
            {excerpt && <span className="mt-1 line-clamp-2 block text-sm text-muted-foreground"><bdi dir="auto">{excerpt}</bdi></span>}
        </Link>
    ),

    form: {
        input: 'h-10 min-w-0 flex-1 rounded-md border bg-background px-3 text-sm',
        button: 'h-10 rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground',
    },
    panelTitleClass: 'text-lg font-semibold',
    proseClass: 'leading-relaxed',
    summaryClass: 'text-sm font-medium',
};
