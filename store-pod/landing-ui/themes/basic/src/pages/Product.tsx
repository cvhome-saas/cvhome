import {getTranslations} from 'next-intl/server';
import type {PageProps, ProductData} from '@store-front/theme';
import {parseDescription} from '@store-front/services/description-view-util';
import {Accordion, AccordionContent, AccordionItem, AccordionTrigger} from '@store-front/ui/accordion';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {SectionHeading} from '../components/SectionHeading';
import {BuyBox, ProductRail} from '../client';

/** The entry at full size, its description and specifications as ruled folds, then the neighbouring entries. */
export async function Product({ctx, data}: PageProps<ProductData>) {
    const t = await getTranslations('PAGE.PRODUCT');
    const {product} = data;
    const html = parseDescription(product.description);
    const attributes = (product.attributes ?? []).filter(a => a.name && a.attributeValues?.length);
    return (
        <PageShell width={ctx.layout.container} className="flex flex-col gap-10 py-6 lg:gap-14 lg:py-8">
            <div className="flex flex-col gap-6">
                <Breadcrumbs items={data.breadcrumbs}/>
                <BuyBox product={product} storeContext={ctx.storeContext}/>
            </div>
            {(html || attributes.length > 0) && (
                <Accordion type="multiple" defaultValue={['details']} className="max-w-narrow border-t">
                    {html && (
                        <AccordionItem value="details">
                            <AccordionTrigger className="display py-4 text-lg hover:no-underline">{t('DETAILS')}</AccordionTrigger>
                            <AccordionContent>
                                <div className="prose-basic text-sm text-muted-foreground" dangerouslySetInnerHTML={{__html: html}}/>
                            </AccordionContent>
                        </AccordionItem>
                    )}
                    {attributes.length > 0 && (
                        <AccordionItem value="specs">
                            <AccordionTrigger className="display py-4 text-lg hover:no-underline">{t('SPECIFICATIONS')}</AccordionTrigger>
                            <AccordionContent>
                                <dl className="grid grid-cols-[max-content_1fr] text-sm">
                                    {attributes.map(a => (
                                        <div key={a.id} className="contents">
                                            <dt className="border-b py-2 pe-6 text-muted-foreground">{a.name}</dt>
                                            <dd className="border-b py-2 font-medium">{a.attributeValues.map(v => v.name || v.description || v.code).join(', ')}</dd>
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
                    <SectionHeading id="related" title={t('RELATED_PRODUCTS')} meta={data.related.length}/>
                    <ProductRail products={data.related} storeContext={ctx.storeContext}/>
                </section>
            )}
        </PageShell>
    );
}
