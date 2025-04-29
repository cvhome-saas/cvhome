'use client'
import {Cart} from "@/types/cart";
import {Link} from "@/i18n/navigation";
import {CartService} from "@/services/cart-service";
import {setCartData} from "@/utils/cart-utils";
import {emitter} from "next/client";
import {StoreContext} from "@/types/store-context";
import Image from 'next/image';
import {useTranslations} from "next-intl";

export const CartProductList = ({storeContext, cart, setCart}: {
    storeContext: StoreContext,
    cart: Cart | undefined,
    setCart: (cart: Cart | undefined) => void
}) => {
    const t = useTranslations('COMPONENTS.CART');
    const removeItem = (sku: string) => {
        if (cart) {
            CartService.removeFromCartThenGetCart(storeContext, cart.code, sku).then((cart) => {
                setCartData(cart)
                setCart(cart);
                emitter.emit("CART_STORAGE_CHANGED", "");
            });
        }
    }

    return <div className="flow-root">
        <ul role="list" className="-my-6 divide-y divide-border">
            {cart && cart.products && cart.products.map((product) => (
                <li key={product.id} className="flex py-6">
                    <div
                        className="relative size-24 shrink-0 overflow-hidden rounded-md border border-border">
                        <Link prefetch={false}
                              href={`/product/${product.description.friendlyUrl}`}
                              className="block size-full"
                        >
                            {
                                product.images && product.images.length > 0 &&
                                <Image alt={product.images[0].imageName}
                                       src={product.images[0].imageUrl}
                                       fill
                                       style={{objectFit: 'cover'}}
                                       sizes="96px"
                                />
                            }
                        </Link>
                    </div>

                    <div className="ms-4 flex flex-1 flex-col">
                        <div>
                            <div
                                className="flex justify-between text-base font-medium text-foreground">
                                <h3>
                                    <Link prefetch={false}
                                          href={`/product/${product.description.friendlyUrl}`}>{product.description.name}</Link>
                                </h3>
                                <p className="ms-4">{product.finalPrice}</p>
                            </div>
                        </div>

                        <div className="flex items-center mt-4 w-full">
                            <span className="mx-2">{t('QTY')}: {product.quantity}</span>
                            <button
                                type="button"
                                onClick={() => removeItem(product.sku)}
                                className="text-error hover:text-hover-error focus:text-focus-error px-3 py-1 rounded ms-auto"
                            >
                                {t('REMOVE')}
                            </button>
                        </div>
                    </div>
                </li>
            ))}
        </ul>
    </div>;

}