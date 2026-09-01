import {Link} from '@store-front/i18n/navigation';
import {linkHref, slidesAsBanners, type SectionRenderProps, type ThemeSectionRegistry} from '@store-front/theme';
import {NewsletterSection} from './NewsletterSection';
import {HeroFrame} from './Hero';
import {ProductGrid} from '../components/ProductGrid';
import {ProductRail} from './ProductRail';
import {SectionHeading} from '../components/SectionHeading';

/**
 * beauty's layout-section registry. Besides the catalogue variants it registers a theme-exclusive hero
 * variant, `editorial` — proof of the mechanism: the manifest offers it on beauty stores only, and a
 * store that later switches theme falls back to the kind's default variant instead of breaking.
 */

function HeroSection({section}: SectionRenderProps) {
    const slides = slidesAsBanners(section.items);
    if (slides.length === 0) return null;
    return <HeroFrame slides={slides}/>;
}

/** Exclusive: the first slide as a full plate with the copy set beside it, magazine-opening style. */
function HeroEditorial({section}: SectionRenderProps) {
    const slides = slidesAsBanners(section.items);
    const lead = slides[0];
    return (
        <div className="grid gap-6 lg:grid-cols-[3fr_2fr] lg:items-center">
            {lead && (
                <div className="plate relative aspect-[4/3] overflow-hidden bg-muted">
                    {/* eslint-disable-next-line @next/next/no-img-element -- single art-directed still, no srcset */}
                    <img src={lead.desktopUrl ?? ''} alt={lead.altText ?? ''}
                         className="absolute inset-0 size-full object-cover"/>
                </div>
            )}
            <div className="flex flex-col gap-3">
                {section.text.heading && (
                    <h2 className="font-display text-3xl leading-tight lg:text-5xl">
                        <bdi dir="auto">{section.text.heading}</bdi>
                    </h2>
                )}
                {section.text.subheading && (
                    <p className="max-w-prose text-muted-foreground"><bdi dir="auto">{section.text.subheading}</bdi></p>
                )}
            </div>
        </div>
    );
}

function ProductsSection({ctx, section, data}: SectionRenderProps) {
    const products = data?.products?.products ?? [];
    if (products.length === 0) return null;
    const title = section.text.title ?? data?.products?.title;
    const rail = section.variant === 'rail';
    return (
        <section>
            {title && <SectionHeading title={title} className="mb-4"/>}
            {rail
                ? <ProductRail products={products} storeContext={ctx.storeContext}/>
                : <ProductGrid products={products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>}
        </section>
    );
}


/** Trust badges as inked plates — structure drawn, never tinted. */
function UspPlates({section}: SectionRenderProps) {
    const badges = (section.items ?? []).filter(badge => badge.text.title);
    if (badges.length === 0) return null;
    return (
        <section className="min-w-0">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <ul className="flex flex-wrap items-stretch justify-center gap-3">
                {badges.map(badge => (
                    <li key={badge.id} className="plate flex max-w-56 flex-col gap-1 px-4 py-3 text-center">
                        <span className="font-display text-sm uppercase tracking-wide"><bdi dir="auto">{badge.text.title}</bdi></span>
                        {badge.text.body && (
                            <span className="text-xs text-muted-foreground"><bdi dir="auto">{badge.text.body}</bdi></span>
                        )}
                    </li>
                ))}
            </ul>
        </section>
    );
}

/** Categories as plate links: ink boxes that invert on hover. */
function CategoryPlates({section, data}: SectionRenderProps) {
    const categories = data?.categories ?? [];
    if (categories.length === 0) return null;
    return (
        <section className="min-w-0">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <div className="flex flex-wrap gap-3">
                {categories.map(category => (
                    <Link key={category.id} prefetch={false} href={`/category/${category.description?.friendlyUrl ?? category.code}`}
                          className="plate inline-flex items-center gap-2 px-4 py-2.5 font-display text-sm uppercase tracking-wide transition-colors hover:bg-foreground hover:text-background">
                        <bdi dir="auto">{category.description?.name ?? category.code}</bdi>
                        {typeof category.productCount === 'number' && category.productCount > 0 && (
                            <span className="text-xs tabular-nums opacity-70">{category.productCount}</span>
                        )}
                    </Link>
                ))}
            </div>
        </section>
    );
}

/** The promo as the taped-off band: primary field between hazard stripes. */
function PromoBand({section}: SectionRenderProps) {
    const message = section.text.message;
    if (!message) return null;
    const href = linkHref(section.props.link);
    return (
        <div className="bg-primary text-primary-foreground">
            <div aria-hidden className="hazard-on-primary h-1.5 opacity-60"/>
            <div className="flex min-h-11 flex-wrap items-center justify-center gap-x-6 gap-y-2 px-5 py-3 text-center">
                <span className="font-display text-base uppercase tracking-wide"><bdi dir="auto">{message}</bdi></span>
                {section.text.cta && href !== '#' && (
                    <Link prefetch={false} href={href} className="text-sm underline underline-offset-4">
                        <bdi dir="auto">{section.text.cta}</bdi>
                    </Link>
                )}
            </div>
            <div aria-hidden className="hazard-on-primary h-1.5 opacity-60"/>
        </div>
    );
}

function FaqPlate({section, data}: SectionRenderProps) {
    const limit = typeof section.props.limit === 'number' ? section.props.limit : 5;
    const entries = (data?.faq?.groups ?? []).flatMap(group => group.entries).slice(0, limit);
    if (entries.length === 0) return null;
    return (
        <section className="mx-auto min-w-0 max-w-2xl">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <div className="plate divide-y divide-foreground px-5 py-2">
                {entries.map((entry, index) => (
                    <details key={index} className="group py-3">
                        <summary className="flex cursor-pointer list-none items-baseline justify-between gap-3 font-display text-sm uppercase tracking-wide marker:hidden [&::-webkit-details-marker]:hidden">
                            <bdi dir="auto">{entry.question}</bdi>
                            <span aria-hidden className="text-muted-foreground transition-transform group-open:rotate-45">+</span>
                        </summary>
                        <div className="pt-2 text-sm leading-relaxed text-muted-foreground"
                             dangerouslySetInnerHTML={{__html: entry.answer}}/>
                    </details>
                ))}
            </div>
        </section>
    );
}

function RichTextSection({section}: SectionRenderProps) {
    if (!section.text.body && !section.text.title) return null;
    return (
        <div className={section.variant === 'centered' ? 'mx-auto max-w-prose text-center' : 'max-w-prose'}>
            {section.text.title && <SectionHeading title={section.text.title}/>}
            {section.text.body && (
                // CMS-authored HTML, sanitized by the content service on write.
                <div className="text-sm leading-relaxed [&_a]:underline [&_a]:underline-offset-4 [&_p+p]:mt-3"
                     dangerouslySetInnerHTML={{__html: section.text.body}}/>
            )}
        </div>
    );
}

export const layoutSections: ThemeSectionRegistry = {
    usp: {row: UspPlates},
    categories: {grid: CategoryPlates, pills: CategoryPlates},
    promo: {strip: PromoBand, card: PromoBand},
    faq: {accordion: FaqPlate},
    newsletter: {inline: NewsletterSection, boxed: NewsletterSection},
    richtext: {default: RichTextSection, centered: RichTextSection},

    hero: {classic: HeroSection, carousel: HeroSection, editorial: HeroEditorial},
    products: {rail: ProductsSection, grid: ProductsSection},
};
