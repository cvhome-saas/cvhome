'use client'
import {Product} from "@/types/product-groups";
import {ShoppingCartIcon} from "@heroicons/react/16/solid";
import {getCartCode, getMatchedProductsInCart, setCartData} from "@/utils/cart-utils";
import {CartService} from "@/services/cart-service";
import {emitter} from "next/client";
import {StoreContext} from "@/types/store-context";
import {showToast} from "nextjs-toast-notify";
import {ArrowTopRightOnSquareIcon, EyeIcon} from "@heroicons/react/24/solid";
import {Link} from "@/i18n/navigation";
import {useState} from "react";
import ProductQuickVIew from "@/app/_themes/DEFAULT/componantes/ProductItem/ProductQuickVIew";
import {isRtl} from "@/utils/direction-utils";
import {useTranslations} from "next-intl";

export default function ProductItemButtonGroup({storeContext, product}: {
    storeContext: StoreContext,
    product: Product
}) {
    const t = useTranslations('COMPONENTS.PRODUCT');

    const [showQuickView, setShowQuickView] = useState(false);
    const toastPosition = isRtl(storeContext.locale) ? "bottom-left" : "bottom-right";

    const isAddToCartDisabled = !product.available || product.quantity == null || product.quantity < 1;

    const updateCart = async () => {
        return await CartService.addToCart(storeContext, getCartCode(), product.sku, 1)
            .then(newCartData => {
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
                    showToast.error(t('FAILED_TO_ADD_PRODUCT_TO_CART'), {
                        duration: 3000,
                        progress: false,
                        position: toastPosition,
                        transition: "bounceIn",
                        sound: false,
                    });
                }
            });
    };

    const addToCart = async () => {
        const productInCart = getMatchedProductsInCart(product);
        if (!productInCart) {
            await updateCart();
        }
    };

    return <>
        <div
            className="absolute bottom-0 w-full flex items-center justify-center gap-4 py-2 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
            <ProductQuickVIew storeContext={storeContext} product={product} open={showQuickView}
                              setOpen={setShowQuickView}/>
            <button
                onClick={() => setShowQuickView(true)}
                aria-label="View Product"
                className="flex items-center justify-center w-10 h-10 rounded-full bg-gradient-to-r from-secondary to-accent text-neutral hover:shadow-lg hover:shadow-secondary/50 transition-transform duration-300 hover:scale-110"
            >
                <EyeIcon className="w-6 h-6"/>
            </button>
            <button
                onClick={addToCart}
                disabled={isAddToCartDisabled}
                aria-label="Add to Cart"
                className={`flex items-center justify-center w-10 h-10 rounded-full bg-gradient-to-r from-success to-primary text-neutral transition-transform duration-300 ${isAddToCartDisabled ? 'cursor-not-allowed opacity-50' : 'hover:shadow-lg hover:shadow-success/50 hover:scale-110'}`}
            >
                <ShoppingCartIcon className="w-6 h-6"/>
            </button>
            <Link
                href={"/product/" + product.description.friendlyUrl}
                aria-label="Go to Product Page"
                className="flex items-center justify-center w-10 h-10 rounded-full bg-gradient-to-r from-info to-primary text-neutral hover:shadow-lg hover:shadow-info/50 transition-transform duration-300 hover:scale-110"
            >
                <ArrowTopRightOnSquareIcon className="w-6 h-6"/>
            </Link>
        </div>
    </>;
}