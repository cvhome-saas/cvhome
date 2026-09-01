import {getTranslations} from 'next-intl/server';
import {Link} from '@store-front/i18n/navigation';
import {linkHref, slidesAsBanners, type SectionRenderProps, type ThemeSectionRegistry} from '@store-front/theme';
import {Hero} from './Hero';
import {NewsletterSection} from './NewsletterSection';
import {ProductGrid} from '../components/ProductGrid';
import {ProductRail} from './ProductRail';
import {SectionHeading} from '../components/SectionHeading';

/**
 * grocery's layout-section registry — the floor. The hero is the entrance price board answering the
 * merchant's slider; product sections are the aisles: short runs as crate grids, long runs as shelves
 * (the rail), each under its hanging board. Everything else prints itself the way the floor already
 * speaks: stickers for state, aisle tiles for destinations, signage on boards.
 */

async function HeroSection({ctx, section}: SectionRenderProps) {
    const t = await getTranslations('PAGE.HOME');
    const {store} = ctx;
    const slides = slidesAsBanners(section.items);
    const year = store.inBusinessSince ? new Date(store.inBusinessSince).getFullYear() : NaN;
    const facts = [store.address?.city, Number.isNaN(year) ? undefined : t('SINCE', {year})].filter(Boolean) as string[];
    return <Hero slides={slides} storeName={section.text.heading ?? store.name} facts={facts}/>;
}

function ProductsSection({ctx, section, data}: SectionRenderProps) {
    const products = data?.products?.products ?? [];
    if (products.length === 0) return null;
    const title = section.text.title ?? data?.products?.title;
    const rail = section.variant === 'rail';
    return (
        <section className="min-w-0">
            {title && <SectionHeading title={title} subtitle={section.text.subtitle} meta={products.length}/>}
            {rail
                ? <ProductRail products={products} storeContext={ctx.storeContext}/>
                : <ProductGrid products={products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>}
        </section>
    );
}

/** Trust badges as outline stickers — state prints itself, straight in a text row. */
function UspStickers({section}: SectionRenderProps) {
    const badges = (section.items ?? []).filter(badge => badge.text.title);
    if (badges.length === 0) return null;
    return (
        <section className="min-w-0">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <ul className="flex flex-wrap items-start justify-center gap-x-8 gap-y-4">
                {badges.map(badge => (
                    <li key={badge.id} className="flex max-w-52 flex-col items-center gap-1.5 text-center">
                        <span className="sticker sticker-outline"><bdi dir="auto">{badge.text.title}</bdi></span>
                        {badge.text.body && (
                            <span className="text-xs text-muted-foreground"><bdi dir="auto">{badge.text.body}</bdi></span>
                        )}
                    </li>
                ))}
            </ul>
        </section>
    );
}

/** Categories as the aisle tiles the header already hangs. */
function CategoryAisles({section, data}: SectionRenderProps) {
    const categories = data?.categories ?? [];
    if (categories.length === 0) return null;
    return (
        <section className="min-w-0">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <div className="flex flex-wrap gap-3">
                {categories.map(category => (
                    <Link key={category.id} prefetch={false} href={`/category/${category.description?.friendlyUrl ?? category.code}`}
                          className="aisle-tile">
                        <bdi dir="auto">{category.description?.name ?? category.code}</bdi>
                        {typeof category.productCount === 'number' && category.productCount > 0 && (
                            <span className="count">{category.productCount}</span>
                        )}
                    </Link>
                ))}
            </div>
        </section>
    );
}

/** The promo as a run of price-board signage. */
function PromoBoard({section}: SectionRenderProps) {
    const message = section.text.message;
    if (!message) return null;
    const href = linkHref(section.props.link);
    return (
        <div className="board flex min-h-14 flex-wrap items-center justify-center gap-x-6 gap-y-2 px-5 py-4 text-center">
            <span className="signage text-lg uppercase"><bdi dir="auto">{message}</bdi></span>
            {section.text.cta && href !== '#' && (
                <Link prefetch={false} href={href} className="text-sm font-semibold underline underline-offset-4">
                    <bdi dir="auto">{section.text.cta}</bdi>
                </Link>
            )}
        </div>
    );
}

function FaqShelf({section, data}: SectionRenderProps) {
    const limit = typeof section.props.limit === 'number' ? section.props.limit : 5;
    const entries = (data?.faq?.groups ?? []).flatMap(group => group.entries).slice(0, limit);
    if (entries.length === 0) return null;
    return (
        <section className="mx-auto min-w-0 max-w-2xl">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <div className="crate divide-y divide-(--line) px-5 py-2">
                {entries.map((entry, index) => (
                    <details key={index} className="group py-3">
                        <summary className="flex cursor-pointer list-none items-baseline justify-between gap-3 font-semibold marker:hidden [&::-webkit-details-marker]:hidden">
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
                <div className="prose-grocery text-sm [&_a]:underline [&_a]:underline-offset-4"
                     dangerouslySetInnerHTML={{__html: section.text.body}}/>
            )}
        </div>
    );
}

export const layoutSections: ThemeSectionRegistry = {
    hero: {classic: HeroSection, carousel: HeroSection},
    products: {rail: ProductsSection, grid: ProductsSection},
    usp: {row: UspStickers},
    categories: {grid: CategoryAisles, pills: CategoryAisles},
    promo: {strip: PromoBoard, card: PromoBoard},
    faq: {accordion: FaqShelf},
    newsletter: {inline: NewsletterSection, boxed: NewsletterSection},
    richtext: {default: RichTextSection, centered: RichTextSection},
};
