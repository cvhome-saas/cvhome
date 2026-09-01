import {getTranslations} from 'next-intl/server';
import {Link} from '@store-front/i18n/navigation';
import {linkHref, slidesAsBanners, type SectionRenderProps, type ThemeSectionRegistry} from '@store-front/theme';
import {Hero} from './Hero';
import {NewsletterSection} from './NewsletterSection';
import {DirectoryBoard, type Department} from '../components/DirectoryBoard';
import {floors} from '../components/floors';
import {ProductGrid} from '../components/ProductGrid';
import {ProductRail} from './ProductRail';
import {SectionHeading} from '../components/SectionHeading';

/**
 * furniture's layout-section registry — the building. The hero is the window onto the current floor;
 * `categories` sections are the building's directory board (the theme's signature piece), one row per
 * department in the merchant's own order; product sections are the numbered plates.
 */

async function HeroSection({ctx, section}: SectionRenderProps) {
    const tc = await getTranslations('COMMON');
    const slides = slidesAsBanners(section.items);
    const caption = section.text.heading ?? ctx.store.name;
    return (
        <div className="min-h-[15rem]">
            <Hero slides={slides} caption={caption} planCaption={tc('DEPARTMENT')}/>
        </div>
    );
}

async function CategoriesSection({ctx, section, data}: SectionRenderProps) {
    const tc = await getTranslations('COMMON');
    const t = await getTranslations('PAGE.HOME');
    const departments: Department[] = floors(data?.categories ? [...data.categories] : undefined)
        .map(c => ({code: c.code, name: c.description.name, href: `/category/${c.description.friendlyUrl}`, count: c.productCount ?? 0}));
    if (departments.length === 0) return null;
    const {store} = ctx;
    const year = store.inBusinessSince ? new Date(store.inBusinessSince).getFullYear() : NaN;
    const facts = [store.address?.city, Number.isNaN(year) ? undefined : t('SINCE', {year})].filter(Boolean) as string[];
    return (
        <DirectoryBoard
            title={section.text.title ?? store.name}
            facts={facts}
            departments={departments}
            headings={{floor: tc('FLOOR'), department: tc('DEPARTMENT'), items: tc('ITEMS')}}
            className="min-w-0"/>
    );
}

async function ProductsSection({ctx, section, data}: SectionRenderProps) {
    const t = await getTranslations('PAGE.HOME');
    const products = data?.products?.products ?? [];
    if (products.length === 0) return null;
    const title = section.text.title ?? data?.products?.title;
    const rail = section.variant === 'rail';
    return (
        <section className="min-w-0">
            {title && <SectionHeading title={title} meta={t('ITEMS_COUNT', {count: products.length})}/>}
            {rail
                ? <ProductRail products={products} storeContext={ctx.storeContext}/>
                : <ProductGrid products={products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>}
        </section>
    );
}

/** Trust badges as the building's printed state plates. */
function UspPlates({section}: SectionRenderProps) {
    const badges = (section.items ?? []).filter(badge => badge.text.title);
    if (badges.length === 0) return null;
    return (
        <section className="min-w-0">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <ul className="flex flex-wrap items-start justify-center gap-x-8 gap-y-4">
                {badges.map(badge => (
                    <li key={badge.id} className="flex max-w-52 flex-col items-center gap-1.5 text-center">
                        <span className="state-plate"><bdi dir="auto">{badge.text.title}</bdi></span>
                        {badge.text.body && (
                            <span className="text-xs text-muted-foreground"><bdi dir="auto">{badge.text.body}</bdi></span>
                        )}
                    </li>
                ))}
            </ul>
        </section>
    );
}

/** The promo as an enamel sign mounted across the landing. */
function PromoSign({section}: SectionRenderProps) {
    const message = section.text.message;
    if (!message) return null;
    const href = linkHref(section.props.link);
    return (
        <div className="enamel-flat flex min-h-14 flex-wrap items-center justify-center gap-x-6 gap-y-2 px-5 py-4 text-center">
            <span className="sign text-base"><bdi dir="auto">{message}</bdi></span>
            {section.text.cta && href !== '#' && (
                <Link prefetch={false} href={href} className="text-sm font-semibold underline underline-offset-4">
                    <bdi dir="auto">{section.text.cta}</bdi>
                </Link>
            )}
        </div>
    );
}

/** Questions on a ruled plate, brass hairlines between the rows. */
function FaqPlate({section, data}: SectionRenderProps) {
    const limit = typeof section.props.limit === 'number' ? section.props.limit : 5;
    const entries = (data?.faq?.groups ?? []).flatMap(group => group.entries).slice(0, limit);
    if (entries.length === 0) return null;
    return (
        <section className="mx-auto min-w-0 max-w-2xl">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <div className="rule-brass divide-y rounded-(--r-card) border px-5 py-2 [&>details]:border-inherit">
                {entries.map((entry, index) => (
                    <details key={index} className="group py-3">
                        <summary className="sign flex cursor-pointer list-none items-baseline justify-between gap-3 text-sm marker:hidden [&::-webkit-details-marker]:hidden">
                            <bdi dir="auto">{entry.question}</bdi>
                            <span aria-hidden className="text-muted-foreground transition-transform group-open:rotate-45">+</span>
                        </summary>
                        <div className="copy pt-2 text-sm text-muted-foreground"
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
                <div className="copy text-sm [&_a]:underline [&_a]:underline-offset-4"
                     dangerouslySetInnerHTML={{__html: section.text.body}}/>
            )}
        </div>
    );
}

export const layoutSections: ThemeSectionRegistry = {
    hero: {classic: HeroSection, carousel: HeroSection},
    categories: {grid: CategoriesSection, pills: CategoriesSection},
    products: {rail: ProductsSection, grid: ProductsSection},
    usp: {row: UspPlates},
    promo: {strip: PromoSign, card: PromoSign},
    faq: {accordion: FaqPlate},
    newsletter: {inline: NewsletterSection, boxed: NewsletterSection},
    richtext: {default: RichTextSection, centered: RichTextSection},
};
