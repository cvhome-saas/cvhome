import {getTranslations} from 'next-intl/server';
import type {PageProps, ProductData} from '@store-front/theme';
import {parseDescription} from '@store-front/services/description-view-util';
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
        <PageShell width={ctx.layout.container} className="flex flex-col gap-8 py-6 lg:gap-10 lg:py-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <BuyBox product={product} storeContext={ctx.storeContext}/>
            {(html || attributes.length > 0) && (
                <section className="grid grid-cols-[minmax(0,1fr)] gap-6 lg:grid-cols-[minmax(0,11fr)_minmax(0,9fr)] lg:gap-8">
                    {html && (
                        <div>
                            <SectionHeading as="h2" title={t('DETAILS')}/>
                            <div className="max-w-prose font-mono text-sm leading-relaxed [&_a]:underline [&_li]:mb-1 [&_ol]:list-decimal [&_ol]:ps-5 [&_p]:mb-3 [&_ul]:list-disc [&_ul]:ps-5" dangerouslySetInnerHTML={{__html: html}}/>
                        </div>
                    )}
                    {attributes.length > 0 && (
                        <div>
                            <SectionHeading as="h2" title={t('SPECIFICATIONS')}/>
                            <dl className="plate grid grid-cols-[max-content_1fr] font-mono text-xs uppercase tracking-wide">
                                {attributes.map((a, i) => (
                                    <div key={a.id} className="contents">
                                        <dt className={`border-e border-foreground px-3 py-1.5 text-muted-foreground ${i < attributes.length - 1 ? 'border-b' : ''}`}>{a.name}</dt>
                                        <dd className={`px-3 py-1.5 ${i < attributes.length - 1 ? 'border-b border-foreground' : ''}`}>{a.attributeValues.map(v => v.name || v.description || v.code).join(', ')}</dd>
                                    </div>
                                ))}
                            </dl>
                        </div>
                    )}
                </section>
            )}
            {data.related.length > 0 && (
                <section aria-labelledby="related" className="min-w-0">
                    <SectionHeading id="related" title={t('RELATED_PRODUCTS')} meta={String(data.related.length).padStart(2, '0')}/>
                    <ProductRail products={data.related} storeContext={ctx.storeContext}/>
                </section>
            )}
        </PageShell>
    );
}
