import {getTranslations} from 'next-intl/server';
import type {PageProps, ProductData} from '@store-front/theme';
import {parseDescription} from '@store-front/services/description-view-util';
import {CategoryService} from '@store-front/services/category-service';
import {findFloor} from '../components/floors';
import {Link} from '@store-front/i18n/navigation';
import {cn} from '@store-front/ui/lib/utils';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {SectionHeading} from '../components/SectionHeading';
import {BuyBox, ProductRail} from '../client';
import {PlateKey, type KeyRow} from '../sections/PlateKey';

/**
 * One piece, plated. The buy column sits beside the views; underneath, the maker's own facts are printed
 * as the plate key — numbered rows, each with a brass leader running from the figure to its value — so
 * dimensions and materials are read straight off the page instead of hidden behind an accordion. It is
 * titled the product record rather than "specifications", because merchants routinely write their own
 * specifications list into the description and two headings of the same name side by side reads as a bug.
 */
export async function Product({ctx, data}: PageProps<ProductData>) {
    const t = await getTranslations('PAGE.PRODUCT');
    const {product} = data;
    const tc = await getTranslations('COMMON');
    const html = parseDescription(product.description);
    const attributes = (product.attributes ?? []).filter(a => a.name && a.attributeValues?.length);

    // Which floor this piece stands on. The number is the department's place in the merchant's own
    // category order — the same one the directory board and the department plate printed.
    const tree = await CategoryService.getCategories(ctx.storeContext);
    const floor = findFloor(tree?.content, data.breadcrumbs.map(b => b.name));

    // The plate key carries what the MERCHANT filled in — on a furniture store the dimensions, materials,
    // finish and care. It deliberately does not repeat the record the buy box already prints two hundred
    // pixels above (maker, catalogue number, floor, availability): leaders that run to a fact the reader
    // has just read add length, not information. With nothing listed it says so in one line.
    const keyRows: KeyRow[] = attributes.map(a => ({
        label: a.name,
        value: a.attributeValues.map(v => v.name || v.description || v.code).join(', '),
    }));

    return (
        <PageShell width={ctx.layout.container} className="flex flex-col gap-10 py-6 lg:gap-16 lg:py-10">
            <Breadcrumbs items={data.breadcrumbs}/>
            <BuyBox product={product} storeContext={ctx.storeContext} floorTag={(floor || keyRows.length === 0) && (
                <div className="rule-brass mt-1 flex flex-col gap-4 border-t pt-5">
                    {floor && (
                        <Link prefetch={false} href={`/category/${floor.category.description.friendlyUrl}`}
                              className="group flex items-center gap-5">
                            <span aria-hidden className="floor-no text-3xl text-muted-foreground">{floor.number}</span>
                            <span className="flex flex-1 flex-col gap-0.5">
                                <span className="sign text-[0.5625rem] text-muted-foreground">{tc('FLOOR')}</span>
                                <span className="sign text-[0.6875rem] group-hover:underline">{floor.category.description.name}</span>
                            </span>
                        </Link>
                    )}
                    {keyRows.length === 0 && <p className="text-sm text-muted-foreground">{t('NO_RECORD')}</p>}
                </div>
            )}/>

            {/* The record only takes a column when the merchant has actually filled one. With nothing
                listed, the note rides in the buy box beside the rest of the record and the description
                runs the full measure — a heading over one grey line is the same dead measure the board
                was fixed for. */}
            {(keyRows.length > 0 || html) && (
                <div className={cn('grid gap-10 lg:gap-14', keyRows.length > 0 && html && 'lg:grid-cols-[1.05fr_1fr]')}>
                    {keyRows.length > 0 && (
                        <section aria-labelledby="plate-key">
                            <SectionHeading id="plate-key" as="h2" title={tc('PRODUCT_RECORD')} className="mb-5 lg:mb-6"/>
                            <PlateKey rows={keyRows}/>
                        </section>
                    )}
                    {html && (
                        // Alone, the description keeps its own reading measure and the plate head's rule
                        // stops with it — a hairline running on past the last word is the dead measure
                        // again, wearing a heading.
                        <section aria-labelledby="details" className={cn(keyRows.length === 0 && 'lg:max-w-[72ch]')}>
                            <SectionHeading id="details" as="h2" title={t('DETAILS')} className="mb-5 lg:mb-6"/>
                            <div className="copy" dangerouslySetInnerHTML={{__html: html}}/>
                        </section>
                    )}
                </div>
            )}

            {data.related.length > 0 && (
                <section aria-labelledby="related">
                    <SectionHeading id="related" as="h2" title={t('RELATED_PRODUCTS')}/>
                    <ProductRail products={data.related} storeContext={ctx.storeContext}/>
                </section>
            )}
        </PageShell>
    );
}
