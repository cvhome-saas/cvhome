'use client'
import {Link} from "@/i18n/navigation";
import {Product} from "@/types/product-groups";
import {StoreContext} from "@/types/store-context";
import ProductItemButtonGroup from "@/componantes/ProductItem/ProductItemButtonGroup";
import Image from 'next/image';
import {useTranslations} from "next-intl";
import {Card, CardContent, CardFooter, CardHeader, CardTitle} from "@/components/ui/card";
import {Badge} from "@/components/ui/badge";

export default function ProductItem({storeContext, product}: { storeContext: StoreContext, product: Product }) {

    const imageUrl = product.image?.imageUrl || '/placeholder-image.png';
    const t = useTranslations('COMPONENTS.PRODUCT');
    const imageAlt = product.description?.name || product.image?.imageName || 'Product image';
    const isAvailable = product.available && product.quantity > 0

    return (
        <Card
            className="m-10 w-full max-w-xs overflow-hidden shadow-lg transition-transform duration-300 hover:scale-105 hover:shadow-2xl">
            <CardContent className="p-0">
                <div className="relative h-60 overflow-hidden group">
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
            </CardContent>
            <CardHeader>
                {product.description && (
                    <CardTitle className="line-clamp-1">
                        <Link prefetch={false} href={`/product/${product.description.friendlyUrl}`}
                              className="hover:underline hover:text-primary transition-colors duration-300">
                            {product.description.name}
                        </Link>
                    </CardTitle>
                )}
                <Badge variant={isAvailable ? "default" : "destructive"} className="w-fit">
                    {isAvailable ? t('IN_STOCK') : t('OUT_OF_STOCK')}
                </Badge>
            </CardHeader>
            <CardFooter>
                <p className="text-2xl font-extrabold text-foreground">
                    {product.productPrice?.finalPrice || '$?.??'}
                </p>
            </CardFooter>
        </Card>
    );
};