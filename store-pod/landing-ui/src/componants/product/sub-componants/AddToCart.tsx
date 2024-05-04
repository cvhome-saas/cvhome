import {Product} from "@/types/product-groups";

export const AddToCart = ({product, t}: { product: Product, t: { [key: string]: string } }) => {
    return <>
        <div className="pro-details-size-color"></div>
        <div className="pro-details-quality">
            <div className="cart-plus-minus">
                <button className="dec qtybutton">-</button>
                <input className="cart-plus-minus-box" type="number"/>
                <button className="inc qtybutton">+</button>
            </div>
            <div className="pro-details-cart btn-hover">
                <button> {t['Add to cart']}</button>
            </div>
        </div>
    </>
}