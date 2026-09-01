import Image from 'next/image';
import {Link} from '@store-front/i18n/navigation';
import {heroModel, type HeroSlide, type SectionRenderProps} from '@store-front/theme';
import {EmptyOrHint} from './shared';

const HEIGHTS: Record<string, string> = {sm: 'min-h-[32vh]', md: 'min-h-[46vh]', lg: 'min-h-[60vh]'};

function Slide({slide, tall, priority}: { slide: HeroSlide; tall: string; priority?: boolean }) {
    const body = (
        <div className={`relative flex w-full flex-col items-start justify-end overflow-hidden ${tall}`}>
            {slide.src && <Image src={slide.src} alt={slide.heading ?? ''} fill priority={priority} className="object-cover"/>}
            {slide.src && <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent"/>}
            <div className="relative z-10 p-6 lg:p-10">
                {slide.heading && (
                    <h2 className={`text-2xl font-semibold lg:text-4xl ${slide.src ? 'text-white' : ''}`}>
                        <bdi dir="auto">{slide.heading}</bdi>
                    </h2>
                )}
                {slide.subheading && (
                    <p className={`mt-2 max-w-prose ${slide.src ? 'text-white/85' : 'text-muted-foreground'}`}>
                        <bdi dir="auto">{slide.subheading}</bdi>
                    </p>
                )}
                {slide.cta && slide.href && (
                    <span className="mt-4 inline-flex rounded-md bg-primary px-5 py-2.5 text-sm font-medium text-primary-foreground">
                        <bdi dir="auto">{slide.cta}</bdi>
                    </span>
                )}
            </div>
        </div>
    );
    return slide.href ? <Link href={slide.href} className="block w-full">{body}</Link> : body;
}

/**
 * Slides side-scroll with CSS snap — honest multi-slide rendering with zero client JS. Consumes
 * {@link heroModel} like every themed hero, so the CTA-dedupe and link semantics cannot drift.
 */
function Slides({section, preview}: SectionRenderProps) {
    const model = heroModel(section);
    const tall = HEIGHTS[model.height] ?? HEIGHTS.md;
    if (model.slides.length === 0 && !model.heading) {
        return <EmptyOrHint preview={preview} label="Hero — add slides or a heading"/>;
    }
    if (model.slides.length === 0) {
        return (
            <div className={`flex w-full flex-col items-center justify-center bg-muted px-6 text-center ${tall}`}>
                <h1 className="text-3xl font-semibold lg:text-5xl"><bdi dir="auto">{model.heading}</bdi></h1>
                {model.subheading && (
                    <p className="mt-3 max-w-prose text-muted-foreground"><bdi dir="auto">{model.subheading}</bdi></p>
                )}
                {model.cta && (
                    <Link href={model.cta.href}
                          className="mt-5 inline-flex rounded-md bg-primary px-5 py-2.5 text-sm font-medium text-primary-foreground">
                        <bdi dir="auto">{model.cta.label}</bdi>
                    </Link>
                )}
            </div>
        );
    }
    if (model.slides.length === 1) return <Slide slide={model.slides[0]} tall={tall} priority/>;
    return (
        <div className="flex w-full snap-x snap-mandatory overflow-x-auto">
            {model.slides.map((slide, i) => (
                <div key={i} className="w-full flex-none snap-start">
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
