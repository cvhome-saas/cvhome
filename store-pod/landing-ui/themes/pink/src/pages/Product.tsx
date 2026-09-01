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
        <PageShell width={ctx.layout.container} className="flex flex-col gap-8 py-6 lg:py-10">
            <Breadcrumbs items={data.breadcrumbs}/>
            <BuyBox product={product} storeContext={ctx.storeContext} details={
                (html || attributes.length > 0) ? (
                    <Accordion type="multiple" defaultValue={['details']} className="hair mt-2 border-t-2">
                        {html && (
                            <AccordionItem value="details" className="hair border-b">
                                <AccordionTrigger className="cover-line py-4 text-base hover:no-underline">{t('DETAILS')}</AccordionTrigger>
                                <AccordionContent>
                                    <div className="text-sm leading-relaxed [&_a]:underline [&_p]:mb-3 [&_ul]:list-disc [&_ul]:ps-5" dangerouslySetInnerHTML={{__html: html}}/>
                                </AccordionContent>
                            </AccordionItem>
                        )}
                        {attributes.length > 0 && (
                            <AccordionItem value="specs" className="hair border-b">
                                <AccordionTrigger className="cover-line py-4 text-base hover:no-underline">{t('SPECIFICATIONS')}</AccordionTrigger>
                                <AccordionContent>
                                    <dl className="text-sm">
                                        {attributes.map(a => (
                                            <div key={a.id} className="hair flex flex-wrap justify-between gap-x-6 gap-y-1 border-b py-2 last:border-b-0">
                                                <dt className="cover-line text-muted-foreground">{a.name}</dt>
                                                <dd className="font-medium">{a.attributeValues.map(v => v.name || v.description || v.code).join(', ')}</dd>
                                            </div>
                                        ))}
                                    </dl>
                                </AccordionContent>
                            </AccordionItem>
                        )}
                    </Accordion>
                ) : undefined
            }/>
            {data.related.length > 0 && (
                <section aria-labelledby="related" className="mt-4">
                    <SectionHeading title={<span id="related">{t('RELATED_PRODUCTS')}</span>}/>
                    <ProductRail products={data.related} storeContext={ctx.storeContext}/>
                </section>
            )}
        </PageShell>
    );
}
