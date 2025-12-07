'use client'
import {Cart} from "@/types/cart";
import {Link} from "@/i18n/navigation";
import {CartService} from "@/services/cart-service";
import {StoreContext} from "@/types/store-context";
import Image from 'next/image';
import {useTranslations} from "next-intl";
import {Button} from "@/components/ui/button";
import React from "react";
import {Product} from "@store-front/types";
import {getCartManager} from "@/componantes/CartManager";

export const CartProductList = ({storeContext, cart, setCart}: {
    storeContext: StoreContext,
    cart: Cart | undefined,
    setCart: (cart: Cart | undefined) => void
}) => {
    const cartManager = getCartManager(storeContext);
    const removeItem = (sku: string) => {
        if (cart) {
            CartService.removeFromCartThenGetCart(storeContext, cart.code, sku).then((cart) => {
                cartManager.setCartData(cart)
            });
        }
    }

    return <div className="flow-root">
        <ul role="list" className="-my-6 divide-y divide-border">
            {cart && cart.products && cart.products.map((product) => (
                <CartProductItem key={product.id} product={product} onRemove={removeItem}/>
            ))}
        </ul>
    </div>;

}

const CartProductItem = ({product, onRemove}: { product: Product, onRemove: (sku: string) => void }) => {
    const t = useTranslations('COMPONENTS.CART');

    return (
        <li className="flex py-6">
            {product.description && (
                <div className="relative size-24 shrink-0 overflow-hidden rounded-md border border-border">
                    <Link prefetch={false} href={`/product/${product.description.friendlyUrl}`}
                          className="block size-full">
                        {product.images && product.images.length > 0 && (
                            <Image
                                alt={product.images[0].imageName}
                                src={product.images[0].imageUrl}
                                fill
                                style={{objectFit: 'cover'}}
                                sizes="96px"
                            />
                        )}
                    </Link>
                </div>
            )}

            <div className="ms-4 flex flex-1 flex-col">
                <div>
                    <div className="flex justify-between text-base font-medium text-foreground">
                        {product.description && (
                            <h3>
                                <Link prefetch={false} href={`/product/${product.description.friendlyUrl}`}>
                                    {product.description.name}
                                </Link>
                            </h3>
                        )}
                        <p className="ms-4">{product.finalPrice}</p>
                    </div>
                </div>

                <div className="flex items-center mt-4 w-full">
                    <span className="text-sm text-muted-foreground">{t('QTY')}: {product.quantity}</span>
                    <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        onClick={() => onRemove(product.sku)}
                        className="ms-auto text-destructive hover:text-destructive"
                    >
                        {t('REMOVE')}
                    </Button>
                </div>
            </div>
        </li>
    );
}