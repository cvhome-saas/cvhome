'use client'
import {Link} from "@/i18n/navigation";
import {Product} from "@/types/product-groups";
import {StoreContext} from "@/types/store-context";
import {Dispatch, SetStateAction, useCallback, useState} from "react";
import {ProductDetails} from "@/shared/ProductDetails/ProductDetails";
import {useTranslations} from "next-intl";
import {Dialog, DialogContent} from "@/components/ui/dialog";
import {Button} from "@/components/ui/button";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/ui/tooltip";
import {ExternalLink, Eye, ShoppingCart} from "lucide-react";
import Image from 'next/image';
import {Card, CardContent, CardFooter, CardHeader, CardTitle} from "@/components/ui/card";
import {Badge} from "@/components/ui/badge";
import {useCart} from "@store-front/hooks/use-cart";

export default function ProductItem({storeContext, product}: { storeContext: StoreContext, product: Product }) {

    const imageUrl = product.image?.imageUrl || '/placeholder-image.png';
    const t = useTranslations('COMPONENTS.PRODUCT');
    const imageAlt = product.description?.name || product.image?.imageName || 'Product image';
    const isAvailable = product.available && product.quantity > 0

    return (
        <Card
            className="group relative flex flex-col items-center overflow-hidden rounded-3xl border-none bg-card shadow-sm transition-all duration-500 hover:shadow-2xl">
            <CardContent className="w-full p-0">
                <div className="relative aspect-[4/5] overflow-hidden bg-secondary/10">
                    <Image
                        fill
                        style={{objectFit: 'cover'}}
                        className="transition-transform duration-1000 ease-out group-hover:scale-110"
                        src={imageUrl}
                        alt={imageAlt}
                        sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 25vw"
                    />
                    <div className="absolute inset-0 z-10 bg-primary/5 opacity-0 transition-opacity duration-500 group-hover:opacity-100" />
                    <div className="absolute inset-0 z-20">
                        <ProductItemButtonGroup storeContext={storeContext} product={product}/>
                    </div>
                </div>
            </CardContent>
            <CardHeader className="w-full space-y-1 px-4 py-6 text-center">
                {product.description && (
                    <CardTitle className="line-clamp-2 text-lg font-serif font-medium leading-tight text-foreground/90">
                        <Link prefetch={false} href={`/product/${product.description.friendlyUrl}`}
                              className="hover:text-primary transition-colors duration-300">
                            {product.description.name}
                        </Link>
                    </CardTitle>
                )}
                <div className="flex flex-col items-center gap-1">
                    <p className="text-xl font-bold text-primary/80">
                        {product.productPrice?.finalPrice || '$?.??'}
                    </p>
                    <div className="h-1 w-8 bg-accent/30 rounded-full my-1 transition-all duration-500 group-hover:w-16 group-hover:bg-accent/60" />
                    <span className="text-[10px] font-medium uppercase tracking-[0.2em] text-muted-foreground">
                        {isAvailable ? t('IN_STOCK') : t('OUT_OF_STOCK')}
                    </span>
                </div>
            </CardHeader>
        </Card>
    );
};


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

    return <>
        <ProductQuickVIew storeContext={storeContext} product={product} open={showQuickView}
                          setOpen={setShowQuickView}/>
        <TooltipProvider>
            <div
                className="absolute inset-0 flex items-center justify-center gap-3 opacity-0 group-hover:opacity-100 transition-all duration-500 scale-90 group-hover:scale-100">
                <Tooltip>
                    <TooltipTrigger asChild>
                        <Button
                            variant={null}
                            size={null}
                            className="w-12 h-12 rounded-full bg-white/90 text-primary hover:bg-white shadow-md hover:shadow-lg transition-transform duration-300 hover:scale-110"
                            onClick={() => setShowQuickView(true)}
                        >
                            <Eye className="size-6"/>
                            <span className="sr-only">{t('QUICK_VIEW')}</span>
                        </Button>
                    </TooltipTrigger>
                    <TooltipContent><p>{t('QUICK_VIEW')}</p></TooltipContent>
                </Tooltip>

                <Tooltip>
                    <TooltipTrigger asChild>
                        <Button
                            variant={null}
                            size={null}
                            className="w-12 h-12 rounded-full bg-primary/90 text-primary-foreground hover:bg-primary shadow-md hover:shadow-lg transition-transform duration-300 hover:scale-110 disabled:opacity-50 disabled:scale-100"
                            onClick={handleAddToCart}
                            disabled={isAddToCartDisabled}
                        >
                            <ShoppingCart className="size-6"/>
                            <span className="sr-only">{t('ADD_TO_CART')}</span>
                        </Button>
                    </TooltipTrigger>
                    <TooltipContent><p>{t('ADD_TO_CART')}</p></TooltipContent>
                </Tooltip>
            </div>
        </TooltipProvider>
    </>;
}

function ProductQuickVIew({storeContext, product, open, setOpen}: {
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
                onOpenAutoFocus={(e) => e.preventDefault()} // Prevents auto-focusing on the first element
            >
                {/* The close button is now part of DialogContent by default */}
                <div className="p-6 lg:p-8">
                    <ProductDetails storeContext={storeContext} p={product} t={t}/>
                </div>
            </DialogContent>
        </Dialog>
    );
}