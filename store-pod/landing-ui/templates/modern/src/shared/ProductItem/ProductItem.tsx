'use client'
import {Link} from "@/i18n/navigation";
import {Product} from "@/types/product-groups";
import {StoreContext} from "@/types/store-context";
import {Dispatch, SetStateAction, useCallback, useMemo, useState} from "react";
import {ProductDetails} from "@/shared/ProductDetails/ProductDetails";
import {useTranslations} from "next-intl";
import {Dialog, DialogContent} from "@/components/ui/dialog";
import {Button} from "@/components/ui/button";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/ui/tooltip";
import {ExternalLink, Eye, ShoppingCart} from "lucide-react";
import Image from 'next/image';
import {Badge} from "@/components/ui/badge";
import {useCart} from "@store-front/hooks/use-cart";

export default function ProductItem({storeContext, product}: { storeContext: StoreContext, product: Product }) {

    const t = useTranslations('COMPONENTS.PRODUCT');

    const imageUrl = useMemo(() => product.image?.imageUrl || '/placeholder-image.png', [product.image?.imageUrl]);
    const imageAlt = useMemo(
        () => product.description?.name || product.image?.imageName || 'Product image',
        [product.description?.name, product.image?.imageName]
    );

    const name = product.description?.name ?? "";
    const href = product.description ? `/product/${product.description.friendlyUrl}` : undefined;

    const isAvailable = product.available && (product.quantity ?? 0) > 0;
    const priceText = product.productPrice?.finalPrice || '$?.??';

    return (
        <article className="group w-full">
            <div className="relative overflow-hidden rounded-2xl border border-border bg-background">
                <div className="relative aspect-[4/5] w-full bg-secondary">
                    <Image
                        fill
                        src={imageUrl}
                        alt={imageAlt}
                        className="object-cover transition-transform duration-700 ease-out group-hover:scale-[1.03]"
                        sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 25vw"
                        priority={false}
                    />

                    <div
                        className="pointer-events-none absolute inset-0 bg-linear-to-t from-black/35 via-black/0 to-black/0 opacity-0 transition-opacity duration-500 group-hover:opacity-100"/>

                    <ProductItemActionBox storeContext={storeContext} product={product} href={href}/>

                    <div className="absolute start-3 top-3">
                        <Badge
                            variant={isAvailable ? "secondary" : "destructive"}
                            className="rounded-full bg-background/85 text-foreground backdrop-blur"
                        >
                            {isAvailable ? t('IN_STOCK') : t('OUT_OF_STOCK')}
                        </Badge>
                    </div>
                </div>

                <div className="p-4">
                    <div className="flex items-start justify-between gap-3">
                        <div className="min-w-0">
                            {href ? (
                                <Link
                                    prefetch={false}
                                    href={href}
                                    className="block text-sm font-medium tracking-wide text-foreground underline-offset-4 hover:underline"
                                >
                                    <span className="line-clamp-2">{name}</span>
                                </Link>
                            ) : (
                                <p className="text-sm font-medium tracking-wide text-foreground line-clamp-2">
                                    {name}
                                </p>
                            )}
                        </div>

                        <div className="shrink-0 text-end">
                            <p className="text-sm font-semibold text-foreground">{priceText}</p>
                        </div>
                    </div>
                </div>
            </div>
        </article>
    );
}

function ProductItemActionBox({storeContext, product, href}: {
    storeContext: StoreContext,
    product: Product,
    href?: string
}) {
    const t = useTranslations('COMPONENTS.PRODUCT');
    const {addToCart} = useCart(storeContext);

    const [showQuickView, setShowQuickView] = useState(false);

    const isAddToCartDisabled = !product.available || product.quantity == null || product.quantity < 1;

    const handleAddToCart = useCallback(async () => {
        await addToCart(product.sku, 1);
    }, [addToCart, product.sku]);

    return (
        <>
            <ProductQuickView storeContext={storeContext} product={product} open={showQuickView}
                              setOpen={setShowQuickView}/>
            <div
                className="absolute inset-0 flex flex-col justify-between p-3 opacity-0 transition-opacity duration-300 group-hover:opacity-100">
                <div className="flex self-end gap-2">
                    <TooltipProvider>
                        <Tooltip>
                            <TooltipTrigger asChild>
                                <Button
                                    variant="outline"
                                    size="icon"
                                    className="rounded-full bg-background/85 backdrop-blur"
                                    onClick={() => setShowQuickView(true)}
                                >
                                    <Eye className="size-5"/>
                                    <span className="sr-only">{t('QUICK_VIEW')}</span>
                                </Button>
                            </TooltipTrigger>
                            <TooltipContent><p>{t('QUICK_VIEW')}</p></TooltipContent>
                        </Tooltip>
                        {href && (
                            <Tooltip>
                                <TooltipTrigger asChild>
                                    <Button variant="outline" size="icon"
                                            className="rounded-full bg-background/85 backdrop-blur" asChild>
                                        <Link href={href}>
                                            <ExternalLink className="size-5"/>
                                            <span className="sr-only">{t('VIEW_DETAILS')}</span>
                                        </Link>
                                    </Button>
                                </TooltipTrigger>
                                <TooltipContent><p>{t('VIEW_DETAILS')}</p></TooltipContent>
                            </Tooltip>
                        )}
                    </TooltipProvider>
                </div>

                <Button
                    variant="default"
                    className="w-full rounded-xl"
                    onClick={handleAddToCart}
                    disabled={isAddToCartDisabled}
                >
                    <ShoppingCart className="size-5"/>
                    <span>{t('ADD_TO_CART')}</span>
                </Button>
            </div>
        </>
    );
}

function ProductQuickView({storeContext, product, open, setOpen}: {
    storeContext: StoreContext,
    product: Product,
    open: boolean,
    setOpen: Dispatch<SetStateAction<boolean>>
}) {
    const t = useTranslations('PAGE.PRODUCT');

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogContent
                className="max-w-2xl lg:max-w-4xl p-0"
                onOpenAutoFocus={(e) => e.preventDefault()}
            >
                <div className="p-6 lg:p-8">
                    <ProductDetails storeContext={storeContext} p={product} t={t}/>
                </div>
            </DialogContent>
        </Dialog>
    );
}