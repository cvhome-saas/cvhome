import Image from 'next/image';
import {Link} from '@store-front/i18n/navigation';
import {productsModel, type SectionRenderProps} from '@store-front/theme';
import type {Product} from '@store-front/types';
import {primaryImage, productHref} from '@store-front/services/product-presenter';
import {Price} from '@store-front/ui/price';
import {AspectBox} from '@store-front/ui/aspect-box';
import {EmptyOrHint, SectionHeading} from './shared';

function Card({product}: { product: Product }) {
    const image = primaryImage(product);
    return (
        <Link href={productHref(product)} className="group block min-w-0">
            <AspectBox ratio="1/1" className="overflow-hidden rounded-md bg-muted">
                <Image src={image.src} alt={image.alt} fill className="object-cover transition-transform duration-300 group-hover:scale-105"/>
            </AspectBox>
            <div className="mt-2 truncate text-sm font-medium">
                <bdi dir="auto">{product.description?.name}</bdi>
            </div>
            <Price finalPrice={product.finalPrice} originalPrice={product.originalPrice}
                   discounted={product.discounted} size="sm"/>
        </Link>
    );
}

function Products({section, data, preview}: SectionRenderProps) {
    // through productsModel like every themed override, so the title fallback and trims cannot drift
    const model = productsModel(section, data);
    if (model.count === 0) {
        return <EmptyOrHint preview={preview} label="Products — no products in this source yet"/>;
    }
    const products = model.products;
    const rail = section.variant === 'rail';
    return (
        <div>
            <SectionHeading title={model.title && <bdi dir="auto">{model.title}</bdi>}
                            subtitle={model.subtitle && <bdi dir="auto">{model.subtitle}</bdi>}/>
            {rail ? (
                <div className="flex snap-x gap-4 overflow-x-auto pb-2">
                    {products.map(p => <div key={p.id} className="w-44 flex-none snap-start lg:w-56"><Card product={p}/></div>)}
                </div>
            ) : (
                <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
                    {products.map(p => <Card key={p.id} product={p}/>)}
                </div>
            )}
        </div>
    );
}

export const productsFallback = {rail: Products, grid: Products};
