import {getTranslations} from 'next-intl/server';
import type {PageProps, ProductData} from '@store-front/theme';
import {parseDescription} from '@store-front/services/description-view-util';
import {Accordion, AccordionContent, AccordionItem, AccordionTrigger} from '@store-front/ui/accordion';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {SectionHeading} from '../components/SectionHeading';
import {BuyBox, ProductRail} from '../client';

export async function Product({ctx, data}: PageProps<ProductData>) {
    const t = await getTranslations('PAGE.PRODUCT');
    const {product} = data;
    const html = parseDescription(product.description);
    const attributes = (product.attributes ?? []).filter(a => a.name && a.attributeValues?.length);
    return (
        <PageShell width={ctx.layout.container} className="flex flex-col gap-10 py-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <BuyBox product={product} storeContext={ctx.storeContext}/>
            {(html || attributes.length > 0) && (
                <Accordion type="multiple" defaultValue={['details']} className="max-w-narrow">
                    {html && (
                        <AccordionItem value="details">
                            <AccordionTrigger className="text-base font-semibold">{t('DETAILS')}</AccordionTrigger>
                            <AccordionContent>
                                <div className="text-sm leading-relaxed text-muted-foreground [&_a]:underline [&_p]:mb-3 [&_ul]:list-disc [&_ul]:ps-5" dangerouslySetInnerHTML={{__html: html}}/>
                            </AccordionContent>
                        </AccordionItem>
                    )}
                    {attributes.length > 0 && (
                        <AccordionItem value="specs">
                            <AccordionTrigger className="text-base font-semibold">{t('SPECIFICATIONS')}</AccordionTrigger>
                            <AccordionContent>
                                <dl className="grid grid-cols-[max-content_1fr] gap-x-6 gap-y-2 text-sm">
                                    {attributes.map(a => (
                                        <div key={a.id} className="contents">
                                            <dt className="text-muted-foreground">{a.name}</dt>
                                            <dd>{a.attributeValues.map(v => v.name || v.description || v.code).join(', ')}</dd>
                                        </div>
                                    ))}
                                </dl>
                            </AccordionContent>
                        </AccordionItem>
                    )}
                </Accordion>
            )}
            {data.related.length > 0 && (
                <section aria-labelledby="related">
                    <SectionHeading title={<span id="related">{t('RELATED_PRODUCTS')}</span>}/>
                    <ProductRail products={data.related} storeContext={ctx.storeContext}/>
                </section>
            )}
        </PageShell>
    );
}
