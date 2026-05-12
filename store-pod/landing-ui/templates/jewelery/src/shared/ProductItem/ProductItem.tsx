'use client'
import {Link} from "@/i18n/navigation";
import {Product} from "@/types/product-groups";
import {StoreContext} from "@/types/store-context";
import {Dispatch, SetStateAction, useCallback, useState} from "react";
import {ProductDetails} from "@/shared/ProductDetails/ProductDetails";
import {useTranslations} from "next-intl";
import {Dialog, DialogContent} from "@/components/ui/dialog";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/ui/tooltip";
import {ExternalLink, Eye, ShoppingCart} from "lucide-react";
import Image from 'next/image';
import {useCart} from "@store-front/hooks/use-cart";

export default function ProductItem({storeContext, product}: { storeContext: StoreContext, product: Product }) {
    const imageUrl = product.image?.imageUrl || '/placeholder.png';
    const t = useTranslations('COMPONENTS.PRODUCT');
    const imageAlt = product.description?.name || product.image?.imageName || 'Product image';
    const isAvailable = product.available && product.quantity > 0;

    return (
        <article className="group w-full flex flex-col">
            {/* Image container */}
            <div className="relative aspect-[3/4] overflow-hidden bg-secondary">
                <Image
                    fill
                    style={{objectFit: 'cover'}}
                    className="transition-transform duration-700 ease-out group-hover:scale-105"
                    src={imageUrl}
                    alt={imageAlt}
                    sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 25vw"
                />

                {/* Out of stock overlay */}
                {!isAvailable && (
                    <div className="absolute inset-0 bg-background/70 flex items-center justify-center">
                        <span
                            className="text-[10px] tracking-[0.2em] uppercase font-medium text-foreground border border-border px-3 py-1.5 bg-background">
                            {t('OUT_OF_STOCK')}
                        </span>
                    </div>
                )}

                {/* Action buttons — appear on hover */}
                <ProductItemButtonGroup storeContext={storeContext} product={product}/>
            </div>

            {/* Product info */}
            <div className="pt-4 pb-2 flex-1 flex flex-col">
                {product.description && (
                    <Link
                        prefetch={false}
                        href={`/product/${product.description.friendlyUrl}`}
                        className="font-['Cormorant_Garamond',serif] text-base font-light tracking-wide text-foreground hover:text-primary transition-colors duration-200 line-clamp-2 leading-snug mb-2"
                    >
                        {product.description.name}
                    </Link>
                )}

                <div className="mt-auto flex items-center justify-between">
                    <p className="font-['Jost',sans-serif] text-sm font-medium tracking-wider text-foreground">
                        {product.productPrice?.finalPrice || '—'}
                    </p>
                    {isAvailable && (
                        <span className="text-[9px] tracking-[0.15em] uppercase text-primary font-medium">
                            {t('IN_STOCK')}
                        </span>
                    )}
                </div>
            </div>
        </article>
    );
}

function ProductItemButtonGroup({storeContext, product}: {
    storeContext: StoreContext,
    product: Product
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
            <TooltipProvider>
                <div
                    className="absolute bottom-0 start-0 end-0 flex items-center justify-center gap-2 py-3 translate-y-full group-hover:translate-y-0 transition-transform duration-300 ease-out bg-background/90 backdrop-blur-sm">
                    <Tooltip>
                        <TooltipTrigger asChild>
                            <button
                                onClick={() => setShowQuickView(true)}
                                className="p-2 text-foreground hover:text-primary transition-colors duration-200"
                                aria-label={t('QUICK_VIEW')}
                            >
                                <Eye className="size-4"/>
                            </button>
                        </TooltipTrigger>
                        <TooltipContent><p className="text-xs tracking-wider">{t('QUICK_VIEW')}</p></TooltipContent>
                    </Tooltip>

                    <div className="w-px h-4 bg-border"/>

                    <Tooltip>
                        <TooltipTrigger asChild>
                            <button
                                onClick={handleAddToCart}
                                disabled={isAddToCartDisabled}
                                className="p-2 text-foreground hover:text-primary transition-colors duration-200 disabled:opacity-30 disabled:cursor-not-allowed"
                                aria-label={t('ADD_TO_CART')}
                            >
                                <ShoppingCart className="size-4"/>
                            </button>
                        </TooltipTrigger>
                        <TooltipContent><p className="text-xs tracking-wider">{t('ADD_TO_CART')}</p></TooltipContent>
                    </Tooltip>

                    {product.description && (
                        <>
                            <div className="w-px h-4 bg-border"/>
                            <Tooltip>
                                <TooltipTrigger asChild>
                                    <Link
                                        href={`/product/${product.description.friendlyUrl}`}
                                        className="p-2 text-foreground hover:text-primary transition-colors duration-200"
                                        aria-label={t('VIEW_DETAILS')}
                                    >
                                        <ExternalLink className="size-4"/>
                                    </Link>
                                </TooltipTrigger>
                                <TooltipContent><p className="text-xs tracking-wider">{t('VIEW_DETAILS')}</p>
                                </TooltipContent>
                            </Tooltip>
                        </>
                    )}
                </div>
            </TooltipProvider>
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
                className="max-w-2xl lg:max-w-4xl p-0 rounded-none"
                onOpenAutoFocus={(e) => e.preventDefault()}
            >
                <div className="p-6 lg:p-10">
                    <ProductDetails storeContext={storeContext} p={product} t={t}/>
                </div>
            </DialogContent>
        </Dialog>
    );
}
