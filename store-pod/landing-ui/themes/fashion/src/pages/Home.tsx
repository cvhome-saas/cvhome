import type {HomeData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {SectionHeading} from '../components/SectionHeading';
import {ProductGrid} from '../components/ProductGrid';
import {ProductCard} from '../components/ProductCard';
import {HeadlineSheet, SlidePoster} from '../sections/Hero';
import {EmptyState} from '../states/EmptyState';

/**
 * The wall. Hero stretch: name sheet · first slider image as a big poster · one product poster. Then one
 * stretch per product group, each led (when the merchant gave enough images) by the next slider image as a
 * wide poster. Slider images left over after the groups get a stretch of their own, so every merchant image
 * is pasted somewhere.
 */
export async function Home({ctx, data}: PageProps<HomeData>) {
    const {store} = ctx;
    const slides = data.hero.slides;
    const [first, ...restGroups] = data.groups;
    const heroProducts = first?.products.slice(0, 1) ?? [];
    const firstRest = first?.products.slice(1) ?? [];
    // slides[0] is in the hero; slides[1..] lead the groups after the first; whatever is left is pasted at the end
    const usedSlides = 1 + Math.min(Math.max(slides.length - 1, 0), restGroups.length);
    const leftover = slides.slice(usedSlides);
    const grid = ctx.layout.productGrid;
    const leadGrid = {base: 2 as const, sm: 3, lg: 4, xl: 6};

    return (
        <>
            <PageShell width={ctx.layout.container} className="pt-5 lg:pt-8">
                <section aria-label={store.name} className="wall grid grid-cols-2 gap-3 sm:gap-4 lg:grid-cols-12 lg:items-stretch">
                    <HeadlineSheet ctx={ctx} data={data} className="col-span-2 lg:col-span-5 [--tilt:-0.7deg]"/>
                    {slides[0] && (
                        <SlidePoster slide={slides[0]} index={0} total={slides.length} storeName={store.name} priority
                                     ratio={heroProducts.length ? '4 / 5' : '16 / 10'} className={heroProducts.length ? 'col-span-1 lg:col-span-5 lg:h-full lg:aspect-auto [--tilt:0.8deg]' : 'col-span-2 lg:col-span-7 lg:h-full lg:aspect-auto [--tilt:0.8deg]'}/>
                    )}
                    {heroProducts.map(p => (
                        <div key={p.id} className={slides[0] ? 'col-span-1 lg:col-span-2 [--tilt:-1deg]' : 'col-span-2 sm:col-span-1 lg:col-span-3 [--tilt:-1deg]'}>
                            <ProductCard product={p} storeContext={ctx.storeContext} priority className="h-full"/>
                        </div>
                    ))}
                    {!slides[0] && !heroProducts.length && data.hero.banner?.path && (
                        <figure className="sheet sheen peel relative col-span-2 aspect-[21/9] overflow-hidden lg:col-span-7">
                            {/* eslint-disable-next-line @next/next/no-img-element */}
                            <img src={data.hero.banner.path} alt="" className="size-full object-cover"/>
                        </figure>
                    )}
                </section>
            </PageShell>

            {data.groups.length === 0 && (
                <PageShell className="py-section"><EmptyState kind="listing"/></PageShell>
            )}

            {first && (
                <PageShell width={ctx.layout.container} className="pt-section">
                    <section aria-labelledby={`group-${first.code}`} className="min-w-0 scroll-mt-24">
                        <SectionHeading id={`group-${first.code}`} title={first.title} meta={first.products.length}/>
                        {firstRest.length > 0 && <ProductGrid products={firstRest} storeContext={ctx.storeContext} grid={grid}/>}
                    </section>
                </PageShell>
            )}

            {restGroups.map((group, i) => {
                const slide = slides[i + 1];
                return (
                    <PageShell key={group.code} width={ctx.layout.container} className="pt-section">
                        <section aria-labelledby={`group-${group.code}`} className="min-w-0 scroll-mt-24">
                            <SectionHeading id={`group-${group.code}`} title={group.title} meta={group.products.length}/>
                            <ProductGrid products={group.products} storeContext={ctx.storeContext} grid={slide ? leadGrid : grid}
                                         lead={slide && <SlidePoster slide={slide} index={i + 1} total={slides.length} storeName={store.name} ratio="auto" className="h-full [--tilt:0.6deg]"/>}/>
                        </section>
                    </PageShell>
                );
            })}

            {leftover.length > 0 && (
                <PageShell width={ctx.layout.container} className="pt-section">
                    <ul className="wall grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3" aria-label={store.name}>
                        {leftover.map((s, i) => (
                            <li key={`${s.url}-${i}`} className="min-w-0">
                                <SlidePoster slide={s} index={usedSlides + i} total={slides.length} storeName={store.name} ratio="4 / 3"/>
                            </li>
                        ))}
                    </ul>
                </PageShell>
            )}
            <div className="h-section" aria-hidden/>
        </>
    );
}
