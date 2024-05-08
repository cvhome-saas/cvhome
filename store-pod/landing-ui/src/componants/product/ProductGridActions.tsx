'use client'
import {Product} from "@/types/product-groups";
import {Link} from "@/navigation";
import {StoreContext} from "@/types/store-context";
import {useState} from 'react';
import {ProductModal} from "@/componants/product/sub-componants/ProductModal";
import {AddToCart} from "@/componants/product/sub-componants/AddToCart";

export const ProductGridActions = ({storeContext, product, t}: {
    storeContext: StoreContext,
    product: Product,
    t: { [key: string]: string }
}) => {

    const [show, setShow] = useState(false);


    return <div className="product-action-2">
        <Link prefetch={false} title="Select options" href={`/product/${product.description.friendlyUrl}`}>
            <i className="fa fa-cog"></i>
        </Link>
        <AddToCart storeContext={storeContext} addToCartType={"MINI"} product={product} t={t}/>
        <button title="Quick View" onClick={() => setShow(true)}>
            <i className="fa fa-eye"></i>
        </button>
        <ProductModal storeContext={storeContext} product={product} show={show} setShow={setShow} t={t}/>
    </div>
}