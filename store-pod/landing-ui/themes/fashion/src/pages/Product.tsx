import {getTranslations} from 'next-intl/server';
import type {PageProps, ProductData} from '@store-front/theme';
import {parseDescription} from '@store-front/services/description-view-util';
import {Accordion, AccordionContent, AccordionItem, AccordionTrigger} from '@store-front/ui/accordion';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {SectionHeading} from '../components/SectionHeading';
import {ProductGrid} from '../components/ProductGrid';
import {BuyBox} from '../sections/BuyBox';

/** Trail · the poster and its spec sheet · a details sheet (description, specifications) · related posters. */
export async function Product({ctx, data}: PageProps<ProductData>) {
    const t = await getTranslations('PAGE.PRODUCT');
    const {product} = data;
    const html = parseDescription(product.description);
    const attributes = (product.attributes ?? []).filter(a => a.name && a.attributeValues?.length);
    return (
        <PageShell width={ctx.layout.container} className="flex flex-col gap-8 py-6 lg:gap-12 lg:py-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <BuyBox product={product} storeContext={ctx.storeContext}/>
            {(html || attributes.length > 0) && (
                <div className="sheet w-full max-w-narrow p-5 [--tilt:-0.4deg] sm:p-7">
                    <Accordion type="multiple" defaultValue={['details']}>
                        {html && (
                            <AccordionItem value="details" className="rule">
                                <AccordionTrigger className="font-display text-lg uppercase tracking-wide hover:no-underline">{t('DETAILS')}</AccordionTrigger>
                                <AccordionContent>
                                    <div className="max-w-[68ch] text-sm leading-relaxed [&_a]:underline [&_p]:mb-3 [&_ul]:list-disc [&_ul]:ps-5" dangerouslySetInnerHTML={{__html: html}}/>
                                </AccordionContent>
                            </AccordionItem>
                        )}
                        {attributes.length > 0 && (
                            <AccordionItem value="specs" className="rule">
                                <AccordionTrigger className="font-display text-lg uppercase tracking-wide hover:no-underline">{t('SPECIFICATIONS')}</AccordionTrigger>
                                <AccordionContent>
                                    <dl className="grid grid-cols-[max-content_1fr] gap-x-6 gap-y-2 text-sm">
                                        {attributes.map(a => (
                                            <div key={a.id} className="contents">
                                                <dt className="text-xs uppercase tracking-wide text-muted-foreground">{a.name}</dt>
                                                <dd>{a.attributeValues.map(v => v.name || v.description || v.code).join(', ')}</dd>
                                            </div>
                                        ))}
                                    </dl>
                                </AccordionContent>
                            </AccordionItem>
                        )}
                    </Accordion>
                </div>
            )}
            {data.related.length > 0 && (
                <section aria-labelledby="related" className="min-w-0">
                    <SectionHeading id="related" title={t('RELATED_PRODUCTS')} meta={data.related.length}/>
                    <ProductGrid products={data.related} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>
                </section>
            )}
        </PageShell>
    );
}
