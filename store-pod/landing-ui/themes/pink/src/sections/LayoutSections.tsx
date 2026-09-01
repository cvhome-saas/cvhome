import {getTranslations} from 'next-intl/server';
import {Link} from '@store-front/i18n/navigation';
import {linkHref, slidesAsBanners, type SectionRenderProps, type ThemeSectionRegistry} from '@store-front/theme';
import {Hero} from './Hero';
import {NewsletterSection} from './NewsletterSection';
import {ProductGrid} from '../components/ProductGrid';
import {ProductRail} from './ProductRail';
import {SectionHeading} from '../components/SectionHeading';

/**
 * pink's layout-section registry — the issue. The hero is the cover: the name set as the masthead with
 * the merchant's slider bleeding off the end edge. The old cover's contents lines were the group list;
 * they return once hero data can see the page's anchored sections — a hand-faked list would print dead
 * links and zero counts, worse than the quiet cover.
 */

function HeroSection({ctx, section}: SectionRenderProps) {
    const slides = slidesAsBanners(section.items);
    return <Hero slides={slides} storeName={section.text.heading ?? ctx.store.name} lines={[]}/>;
}

async function ProductsSection({ctx, section, data}: SectionRenderProps) {
    const t = await getTranslations('PAGE.HOME');
    const products = data?.products?.products ?? [];
    if (products.length === 0) return null;
    const title = section.text.title ?? data?.products?.title;
    const rail = section.variant === 'rail';
    return (
        <section className="min-w-0">
            {title && (
                <SectionHeading title={title} subtitle={section.text.subtitle}
                                action={<span className="dim figure text-sm">{t('ITEMS_COUNT', {count: products.length})}</span>}/>
            )}
            {rail
                ? <ProductRail products={products} storeContext={ctx.storeContext}/>
                : <ProductGrid products={products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>}
        </section>
    );
}

/** Trust badges as the issue's printed flags. */
function UspFlags({section}: SectionRenderProps) {
    const badges = (section.items ?? []).filter(badge => badge.text.title);
    if (badges.length === 0) return null;
    return (
        <section className="min-w-0">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <ul className="flex flex-wrap items-start justify-center gap-x-8 gap-y-4">
                {badges.map(badge => (
                    <li key={badge.id} className="flex max-w-52 flex-col items-center gap-1.5 text-center">
                        <span className="flag"><bdi dir="auto">{badge.text.title}</bdi></span>
                        {badge.text.body && (
                            <span className="dim text-xs"><bdi dir="auto">{badge.text.body}</bdi></span>
                        )}
                    </li>
                ))}
            </ul>
        </section>
    );
}

/** Categories as the issue's contents: numbered lines, each the address of a department. */
function CategoryContents({section, data}: SectionRenderProps) {
    const categories = data?.categories ?? [];
    if (categories.length === 0) return null;
    return (
        <section className="mx-auto min-w-0 max-w-2xl">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <ul className="flex flex-col gap-2.5">
                {categories.map((category, index) => (
                    <li key={category.id}>
                        <Link prefetch={false} href={`/category/${category.description?.friendlyUrl ?? category.code}`}
                              className="group flex flex-wrap items-baseline gap-x-3 gap-y-1">
                            <span aria-hidden className="figure text-sm">{String(index + 1).padStart(2, '0')}</span>
                            <span className="display text-xl underline-offset-4 group-hover:underline sm:text-2xl">
                                <bdi dir="auto">{category.description?.name ?? category.code}</bdi>
                            </span>
                            {typeof category.productCount === 'number' && category.productCount > 0 && (
                                <span className="figure dim text-sm">{category.productCount}</span>
                            )}
                        </Link>
                    </li>
                ))}
            </ul>
        </section>
    );
}

/** The promo as a flooded band — the field owns the region whole. */
function PromoFlood({section}: SectionRenderProps) {
    const message = section.text.message;
    if (!message) return null;
    const href = linkHref(section.props.link);
    return (
        <div className="flood tone flex min-h-14 flex-wrap items-center justify-center gap-x-6 gap-y-2 px-5 py-4 text-center">
            <span className="band-copy"><bdi dir="auto">{message}</bdi></span>
            {section.text.cta && href !== '#' && (
                <Link prefetch={false} href={href} className="text-sm font-bold underline underline-offset-4">
                    <bdi dir="auto">{section.text.cta}</bdi>
                </Link>
            )}
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
            <div className="plate grid-cols-1">
                {entries.map((entry, index) => (
                    <details key={index} className="group cell border-0 px-4 py-3">
                        <summary className="flex cursor-pointer list-none items-baseline justify-between gap-3 font-bold marker:hidden [&::-webkit-details-marker]:hidden">
                            <bdi dir="auto">{entry.question}</bdi>
                            <span aria-hidden className="dim transition-transform group-open:rotate-45">+</span>
                        </summary>
                        <div className="prose-issue pt-2 text-sm text-muted-foreground"
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
                <div className="prose-issue text-sm [&_a]:underline [&_a]:underline-offset-4"
                     dangerouslySetInnerHTML={{__html: section.text.body}}/>
            )}
        </div>
    );
}

export const layoutSections: ThemeSectionRegistry = {
    usp: {row: UspFlags},
    categories: {grid: CategoryContents, pills: CategoryContents},
    promo: {strip: PromoFlood, card: PromoFlood},
    faq: {accordion: FaqPlate},
    newsletter: {inline: NewsletterSection, boxed: NewsletterSection},
    richtext: {default: RichTextSection, centered: RichTextSection},
    hero: {classic: HeroSection, carousel: HeroSection},
    products: {rail: ProductsSection, grid: ProductsSection},
};
