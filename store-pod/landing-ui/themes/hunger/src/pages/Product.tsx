import {getTranslations} from 'next-intl/server';
import type {PageProps, ProductData} from '@store-front/theme';
import {parseDescription} from '@store-front/services/description-view-util';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {SectionHeading} from '../components/SectionHeading';
import {BuyBox} from '../sections/BuyBox';
import {ProductRail} from '../sections/ProductRail';

/**
 * One dish written out. Details and attributes are printed open, not hidden behind an accordion — a menu
 * that makes you tap to read what is in a dish is a menu nobody orders from.
 */
export async function Product({ctx, data}: PageProps<ProductData>) {
    const t = await getTranslations('PAGE.PRODUCT');
    const {product} = data;
    const html = parseDescription(product.description);
    const attributes = (product.attributes ?? []).filter(a => a.name && a.attributeValues?.length);
    return (
        <PageShell width={ctx.layout.container} className="flex flex-col gap-8 py-6">
            <Breadcrumbs items={data.breadcrumbs}/>
            <BuyBox product={product} storeContext={ctx.storeContext}/>

            {(html || attributes.length > 0) && (
                <div className="grid gap-8 border-t-2 border-foreground pt-6 lg:grid-cols-[minmax(0,2fr)_minmax(0,1fr)] lg:gap-12">
                    {html && (
                        <section aria-labelledby="details">
                            <h2 id="details" className="press mb-3 border-b border-foreground pb-1 text-lg">{t('DETAILS')}</h2>
                            <div className="prose-hunger text-sm" dangerouslySetInnerHTML={{__html: html}}/>
                        </section>
                    )}
                    {attributes.length > 0 && (
                        <section aria-labelledby="specs">
                            <h2 id="specs" className="press mb-3 border-b border-foreground pb-1 text-lg">{t('SPECIFICATIONS')}</h2>
                            <dl className="text-sm">
                                {attributes.map(a => (
                                    <div key={a.id} className="flex items-baseline gap-2 border-b border-border py-1.5">
                                        <dt className="press shrink-0 text-xs tracking-wide text-muted-foreground">{a.name}</dt>
                                        <span aria-hidden className="leader"/>
                                        <dd className="shrink-0 font-medium">{a.attributeValues.map(v => v.name || v.description || v.code).join(', ')}</dd>
                                    </div>
                                ))}
                            </dl>
                        </section>
                    )}
                </div>
            )}

            {data.related.length > 0 && (
                <section aria-labelledby="related" className="pt-2">
                    <SectionHeading title={<span id="related">{t('RELATED_PRODUCTS')}</span>}/>
                    <ProductRail products={data.related} storeContext={ctx.storeContext}/>
                </section>
            )}
        </PageShell>
    );
}
