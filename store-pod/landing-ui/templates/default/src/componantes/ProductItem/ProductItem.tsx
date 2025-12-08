'use client'
import {Link} from "@/i18n/navigation";
import {Product} from "@/types/product-groups";
import {StoreContext} from "@/types/store-context";
import {showToast} from "nextjs-toast-notify";
import {Dispatch, SetStateAction, useCallback, useState} from "react";
import {ProductDetails} from "@/componantes/ProductDetails/ProductDetails";
import {useTranslations} from "next-intl";
import {Dialog, DialogContent} from "@/components/ui/dialog";
import {toastDirection} from "@/services/direction-utils";
import {Button} from "@/components/ui/button";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/ui/tooltip";
import {ExternalLink, Eye, ShoppingCart} from "lucide-react";
import {getCartManager} from "@/services/cart-manager";
import Image from 'next/image';
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



 function ProductItemButtonGroup({storeContext, product}: {
    storeContext: StoreContext,
    product: Product
}) {
    const t = useTranslations('COMPONENTS.PRODUCT');
    const cartManager = getCartManager(storeContext);

    const [showQuickView, setShowQuickView] = useState(false);

    const isAddToCartDisabled = !product.available || product.quantity == null || product.quantity < 1;

    const addToCart = useCallback(async () => {
        cartManager.addProductToCart(product.sku, 1, (cart) => {
            if (cart) {
                showToast.success(t('SUCCESSFULLY_ADDED_PRODUCT_TO_CART'), {
                    duration: 3000,
                    progress: false,
                    position: toastDirection(storeContext.locale),
                    transition: "bounceIn",
                    sound: false,
                });
            }
        }, (err) => {
            showToast.error(t('FAILED_TO_ADD_PRODUCT_TO_CART'), {
                duration: 3000,
                progress: false,
                position: toastDirection(storeContext.locale),
                transition: "bounceIn",
                sound: false,
            });
        });
    }, [cartManager, storeContext.locale, product.sku, t]);

    return <>
        <ProductQuickVIew storeContext={storeContext} product={product} open={showQuickView}
                          setOpen={setShowQuickView}/>
        <TooltipProvider>
            <div
                className="absolute bottom-0 w-full flex items-center justify-center gap-2 py-2 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                <Tooltip>
                    <TooltipTrigger asChild>
                        <Button
                            variant={null}
                            size={null}
                            className="w-10 h-10 rounded-full bg-gradient-to-r from-secondary to-accent text-secondary-foreground hover:shadow-lg hover:shadow-secondary/50 transition-transform duration-300 hover:scale-110"
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
                            className="w-10 h-10 rounded-full bg-gradient-to-r from-green-500 to-primary text-primary-foreground transition-transform duration-300 hover:scale-110 hover:shadow-lg hover:shadow-green-500/50 disabled:opacity-50 disabled:scale-100 disabled:shadow-none"
                            onClick={addToCart}
                            disabled={isAddToCartDisabled}
                        >
                            <ShoppingCart className="size-6"/>
                            <span className="sr-only">{t('ADD_TO_CART')}</span>
                        </Button>
                    </TooltipTrigger>
                    <TooltipContent><p>{t('ADD_TO_CART')}</p></TooltipContent>
                </Tooltip>

                {product.description && (
                    <Tooltip>
                        <TooltipTrigger asChild>
                            <Button
                                variant={null}
                                size={null}
                                className="w-10 h-10 rounded-full bg-gradient-to-r from-blue-500 to-primary text-primary-foreground hover:shadow-lg hover:shadow-blue-500/50 transition-transform duration-300 hover:scale-110"
                                asChild
                            >
                                <Link href={`/product/${product.description.friendlyUrl}`}>
                                    <ExternalLink className="size-6"/>
                                    <span className="sr-only">{t('VIEW_DETAILS')}</span>
                                </Link>
                            </Button>
                        </TooltipTrigger>
                        <TooltipContent><p>{t('VIEW_DETAILS')}</p></TooltipContent>
                    </Tooltip>
                )}
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