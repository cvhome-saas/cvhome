import Image from 'next/image';
import {Link} from '@store-front/i18n/navigation';
import type {SectionRenderProps} from '@store-front/theme';
import type {SectionItem} from '@store-front/types';
import {items, linkHref, mediaUrl} from '../support';
import {EmptyOrHint} from './shared';

const HEIGHTS: Record<string, string> = {sm: 'min-h-[32vh]', md: 'min-h-[46vh]', lg: 'min-h-[60vh]'};

function Slide({slide, tall, priority}: { slide: SectionItem; tall: string; priority?: boolean }) {
    const src = mediaUrl(slide.props);
    const href = linkHref(slide.props.link);
    const body = (
        <div className={`relative flex w-full flex-col items-start justify-end overflow-hidden ${tall}`}>
            {src && <Image src={src} alt={slide.text.heading ?? ''} fill priority={priority} className="object-cover"/>}
            {src && <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent"/>}
            <div className="relative z-10 p-6 lg:p-10">
                {slide.text.heading && (
                    <h2 className={`text-2xl font-semibold lg:text-4xl ${src ? 'text-white' : ''}`}>
                        <bdi dir="auto">{slide.text.heading}</bdi>
                    </h2>
                )}
                {slide.text.subheading && (
                    <p className={`mt-2 max-w-prose ${src ? 'text-white/85' : 'text-muted-foreground'}`}>
                        <bdi dir="auto">{slide.text.subheading}</bdi>
                    </p>
                )}
                {slide.text.cta && (
                    <span className="mt-4 inline-flex rounded-md bg-primary px-5 py-2.5 text-sm font-medium text-primary-foreground">
                        {slide.text.cta}
                    </span>
                )}
            </div>
        </div>
    );
    return href === '#' ? body : <Link href={href} className="block w-full">{body}</Link>;
}

/** Slides side-scroll with CSS snap — honest multi-slide rendering with zero client JS. */
function Slides({section, preview}: SectionRenderProps) {
    const slides = items(section);
    const tall = HEIGHTS[String(section.props.height)] ?? HEIGHTS.md;
    if (slides.length === 0 && !section.text.heading) {
        return <EmptyOrHint preview={preview} label="Hero — add slides or a heading"/>;
    }
    if (slides.length === 0) {
        return (
            <div className={`flex w-full flex-col items-center justify-center bg-muted px-6 text-center ${tall}`}>
                <h1 className="text-3xl font-semibold lg:text-5xl"><bdi dir="auto">{section.text.heading}</bdi></h1>
                {section.text.subheading && (
                    <p className="mt-3 max-w-prose text-muted-foreground"><bdi dir="auto">{section.text.subheading}</bdi></p>
                )}
            </div>
        );
    }
    if (slides.length === 1) return <Slide slide={slides[0]} tall={tall} priority/>;
    return (
        <div className="flex w-full snap-x snap-mandatory overflow-x-auto">
            {slides.map((slide, i) => (
                <div key={slide.id} className="w-full flex-none snap-start">
                    <Slide slide={slide} tall={tall} priority={i === 0}/>
                </div>
            ))}
        </div>
    );
}

export const heroFallback = {
    classic: Slides,
    split: Slides,
    carousel: Slides,
    minimal: Slides,
};
