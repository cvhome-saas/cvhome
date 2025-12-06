'use client'
import {Product} from "@/types/product-groups";
import {getCartCode, setCartData} from "@/services/cart-utils";
import {CartService} from "@/services/cart-service";
import {emitter} from "next/client";
import {StoreContext} from "@/types/store-context";
import {showToast} from "nextjs-toast-notify";
import {Link} from "@/i18n/navigation";
import {useState, useCallback} from "react";
import ProductQuickVIew from "@/componantes/ProductItem/ProductQuickVIew";
import {isRtl} from "@/services/direction-utils";
import {useTranslations} from "next-intl";
import {Button} from "@/components/ui/button";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/ui/tooltip";
import {Eye, ExternalLink, ShoppingCart} from "lucide-react";

export default function ProductItemButtonGroup({storeContext, product}: {
    storeContext: StoreContext,
    product: Product
}) {
    const t = useTranslations('COMPONENTS.PRODUCT');

    const [showQuickView, setShowQuickView] = useState(false);
    const toastPosition = isRtl(storeContext.locale) ? "bottom-left" : "bottom-right";

    const isAddToCartDisabled = !product.available || product.quantity == null || product.quantity < 1;

    const addToCart = useCallback(async () => {
        try {
            const newCartData = await CartService.addToCart(storeContext, getCartCode(), product.sku, 1);
            if (newCartData) {
                setCartData(newCartData);
                emitter.emit("CART_STORAGE_CHANGED", "");
                showToast.success(t('SUCCESSFULLY_ADDED_PRODUCT_TO_CART'), {
                    duration: 3000,
                    progress: false,
                    position: toastPosition,
                    transition: "bounceIn",
                    sound: false,
                });
            } else {
                throw new Error("No cart data returned");
            }
        } catch (error) {
            console.error("Failed to add product to cart:", error);
            showToast.error(t('FAILED_TO_ADD_PRODUCT_TO_CART'), {
                duration: 3000,
                progress: false,
                position: toastPosition,
                transition: "bounceIn",
                sound: false,
            });
        }
    }, [storeContext, product.sku, t, toastPosition]);

    return <>
        <ProductQuickVIew storeContext={storeContext} product={product} open={showQuickView} setOpen={setShowQuickView}/>
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