'use client'
import {Link} from "@/i18n/navigation";
import {Product} from "@/types/product-groups";
import {StoreContext} from "@/types/store-context";
import ProductItemButtonGroup from "@/componantes/ProductItem/ProductItemButtonGroup";
import Image from 'next/image';
import {useTranslations} from "next-intl";

export default function ProductItem({storeContext, product}: { storeContext: StoreContext, product: Product }) {

    const imageUrl = product.image?.imageUrl || '/placeholder-image.png';
    const t = useTranslations('COMPONENTS.PRODUCT');
    const imageAlt = product.description?.name || product.image?.imageName || 'Product image';
    const isAvailable = product.available && product.quantity > 0;
    const availabilityText = isAvailable ? t('IN_STOCK') : t('OUT_OF_STOCK');
    const availabilityClass = isAvailable ? "text-success" : "text-error";

    return (
        <div
            className="m-10 flex w-full max-w-xs flex-col overflow-hidden rounded-lg border border-border bg-background shadow-lg transition-transform duration-300 hover:scale-105 hover:shadow-2xl">
            <div className="relative mx-3 mt-3 flex h-60 overflow-hidden rounded-xl group">
                <Image
                    fill
                    style={{objectFit: 'cover'}}
                    className="transition-transform duration-300 group-hover:scale-110"
                    src={imageUrl}
                    alt={imageAlt}
                    sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 25vw"
                />
                <div className="absolute inset-0 z-10">
                    <ProductItemButtonGroup storeContext={storeContext} product={product}/>
                </div>
            </div>
            <div className="mt-4 px-5 pb-0">
                {
                    product.description &&
                    <Link prefetch={false} href={"/product/" + product.description.friendlyUrl}>
                    <span
                        className="text-xl font-bold tracking-tight text-foreground hover:underline hover:text-primary transition-colors duration-300 line-clamp-1">
                        {product.description?.name || 'Product Name'}
                    </span>
                    </Link>
                }

                <div className="mt-2 mb-2">
                    <span className={`text-sm font-medium ${availabilityClass}`}>
                        {availabilityText}
                    </span>
                </div>

                <div className="mt-3 mb-5">
                    <p className="text-2xl font-extrabold text-foreground">{product.productPrice?.finalPrice || '$?.??'}</p>
                </div>
            </div>
        </div>
    );
};