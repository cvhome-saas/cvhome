import {getTranslations} from 'next-intl/server';
import type {PageProps, ProductData} from '@store-front/theme';
import {parseDescription} from '@store-front/services/description-view-util';
import {Accordion, AccordionContent, AccordionItem, AccordionTrigger} from '@store-front/ui/accordion';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {SectionHeading} from '../components/SectionHeading';
import {BuyBox} from '../sections/BuyBox';
import {ProductRail} from '../sections/ProductRail';

/** The product tag at full scale. Bottom padding keeps the mobile counter bar off the content. */
export async function Product({ctx, data}: PageProps<ProductData>) {
    const t = await getTranslations('PAGE.PRODUCT');
    const {product} = data;
    const html = parseDescription(product.description);
    const attributes = (product.attributes ?? []).filter(a => a.name && a.attributeValues?.length);
    return (
        <PageShell width={ctx.layout.container} className="flex flex-col gap-10 py-6 pb-28 lg:py-8 lg:pb-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <BuyBox product={product} storeContext={ctx.storeContext} details={(html || attributes.length > 0) && (
                <Accordion type="multiple" defaultValue={['details']} className="mt-2 border-t-2">
                    {html && (
                        <AccordionItem value="details" className="border-b-2">
                            <AccordionTrigger className="signage text-lg">{t('DETAILS')}</AccordionTrigger>
                            <AccordionContent>
                                <div className="prose-grocery text-sm text-muted-foreground [&_a]:underline" dangerouslySetInnerHTML={{__html: html}}/>
                            </AccordionContent>
                        </AccordionItem>
                    )}
                    {attributes.length > 0 && (
                        <AccordionItem value="specs" className="border-b-2">
                            <AccordionTrigger className="signage text-lg">{t('SPECIFICATIONS')}</AccordionTrigger>
                            <AccordionContent>
                                <dl className="grid grid-cols-[max-content_1fr] gap-x-6 gap-y-2 text-sm">
                                    {attributes.map(a => (
                                        <div key={a.id} className="contents">
                                            <dt className="font-semibold text-muted-foreground">{a.name}</dt>
                                            <dd>{a.attributeValues.map(v => v.name || v.description || v.code).join(', ')}</dd>
                                        </div>
                                    ))}
                                </dl>
                            </AccordionContent>
                        </AccordionItem>
                    )}
                </Accordion>
            )}/>
            {data.related.length > 0 && (
                <section aria-labelledby="related">
                    <SectionHeading id="related" title={t('RELATED_PRODUCTS')}/>
                    <ProductRail products={data.related} storeContext={ctx.storeContext}/>
                </section>
            )}
        </PageShell>
    );
}
