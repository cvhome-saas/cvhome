'use client'
import {Product} from "@/types/product-groups";
import {Link} from "@/navigation";
import {CartService} from "@/services/cart-service";
import {StoreContext} from "@/types/store-context";
import {Cart} from "@/types/cart";
import Cookies from "js-cookie";
import {useState} from 'react';
import {ProductModal} from "@/componants/product/sub-componants/ProductModal";

export const ProductGridActions = ({storeContext, product, t}: {
    storeContext: StoreContext,
    product: Product,
    t: { [key: string]: string }
}) => {
    const onClick = () => {
        const item: string | null = localStorage.getItem("store-ui-cart-data");
        let cartCode: string | undefined;
        if (item) {
            cartCode = (JSON.parse(item) as unknown as Cart).code
        } else {
            cartCode = undefined;
        }


        return CartService.addToCart(storeContext, cartCode, product.description.friendlyUrl, 1)
            .then(it => {
                return it.json() as unknown as Cart
            })
            .then(it => {
                if (it && it.code && it.products && it.products.length > 0) {
                    Cookies.set('store-ui-cart-id', it.code);
                    location.reload();
                }
                return it;
            })

    };
    const [show, setShow] = useState(false);


    return <div className="product-action-2">
        <Link title="Select options" href={`/product/${product.description.friendlyUrl}`}>
            <i className="fa fa-cog"></i>
        </Link>
        <button className="active" title="Add to cart" onClick={onClick}>
            <i className="fa fa-shopping-cart"></i>
        </button>
        <button title="Quick View" onClick={() => setShow(true)}>
            <i className="fa fa-eye"></i>
        </button>

        <ProductModal product={product} show={show} setShow={setShow} t={t}/>
    </div>
}