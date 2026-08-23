import {getTranslations} from 'next-intl/server';
import type {HomeData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {SectionHeading} from '../components/SectionHeading';
import {ProductGrid} from '../components/ProductGrid';
import {TagButton} from '../components/TagButton';
import {HeroFrame} from '../sections/Hero';
import {ProductRail} from '../sections/ProductRail';
import {EmptyState} from '../states/EmptyState';

/**
 * First viewport: the store's name in straight quotes as the display headline, tag CTAs to the top
 * categories, and the merchant's slider images in a plate frame. Then one shelf per product group.
 */
export async function Home({ctx, data}: PageProps<HomeData>) {
    const t = await getTranslations('PAGE.HOME');
    const tc = await getTranslations('COMMON');
    const {store} = ctx;
    const hasSlides = data.hero.slides.length > 0;
    const firstGroup = data.groups[0];
    return (
        <>
            <PageShell width={ctx.layout.container} className="pt-6 lg:pt-10">
                <section className="grid grid-cols-[minmax(0,1fr)] gap-5 lg:grid-cols-[minmax(0,9fr)_minmax(0,11fr)] lg:grid-rows-[auto_minmax(0,1fr)] lg:gap-x-10 lg:gap-y-6" aria-label={store.name}>
                    <h1 className="order-1 min-w-0 font-display text-4xl font-bold uppercase leading-[0.9] tracking-tight [overflow-wrap:anywhere] sm:text-6xl lg:col-start-1 lg:row-start-1 lg:text-6xl">
                        <span className="q" dir="auto"><bdi>{store.name}</bdi></span>
                    </h1>
                    {hasSlides && <div className="order-2 min-w-0 lg:col-start-2 lg:row-span-2 lg:row-start-1"><HeroFrame slides={data.hero.slides}/></div>}
                    <div className="order-3 flex min-w-0 flex-col gap-5 lg:col-start-1 lg:row-start-2 lg:self-start">
                        {data.hero.banner?.path && !hasSlides && (
                            <div className="plate relative aspect-[21/9] overflow-hidden bg-muted">
                                {/* eslint-disable-next-line @next/next/no-img-element */}
                                <img src={data.hero.banner.path} alt="" className="size-full object-cover"/>
                            </div>
                        )}
                        {firstGroup && (
                            <div className="flex flex-wrap gap-2">
                                <TagButton asChild size="lg"><a href={`#group-${firstGroup.code}`}><span className="q">{t('SHOP_NOW')}</span></a></TagButton>
                                {data.groups.slice(1, 3).map(g => (
                                    <a key={g.code} href={`#group-${g.code}`} className="plate inline-flex h-12 items-center px-4 font-display text-base font-semibold uppercase tracking-wide hover:bg-foreground hover:text-background"><span className="q">{g.title}</span></a>
                                ))}
                            </div>
                        )}
                        <dl className="inline-flex w-fit flex-wrap gap-px border border-foreground bg-foreground font-mono text-[0.65rem] uppercase tracking-wide">
                            <div className="flex gap-2 bg-background px-2 py-1"><dt className="text-muted-foreground">{tc('LANGUAGE_LABEL')}</dt><dd>{ctx.locale.toUpperCase()}</dd></div>
                            {store.currency && <div className="flex gap-2 bg-background px-2 py-1"><dt className="text-muted-foreground">{tc('CURRENCY')}</dt><dd>{store.currency}</dd></div>}
                            {data.groups.length > 0 && <div className="flex gap-2 bg-background px-2 py-1"><dt className="text-muted-foreground">{tc('ITEMS')}</dt><dd className="tabular-nums">{String(data.groups.reduce((n, g) => n + g.products.length, 0)).padStart(2, '0')}</dd></div>}
                        </dl>
                    </div>
                </section>
                <div className="hazard mt-8 h-3 border-y border-foreground lg:mt-12" aria-hidden/>
            </PageShell>

            {data.groups.length === 0 && (
                <PageShell className="py-section"><EmptyState kind="listing"/></PageShell>
            )}
            {data.groups.map(group => (
                <PageShell key={group.code} width={ctx.layout.container} className="py-section">
                    <section aria-labelledby={`group-${group.code}`} className="min-w-0">
                        <SectionHeading id={`group-${group.code}`} title={group.title} meta={String(group.products.length).padStart(2, '0')}/>
                        {group.code === 'HOME_PAGE'
                            ? <ProductGrid products={group.products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>
                            : <ProductRail products={group.products} storeContext={ctx.storeContext}/>}
                    </section>
                </PageShell>
            ))}
        </>
    );
}
