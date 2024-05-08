import {StoreContext} from "@/types/store-context";
import {Product, ProductGroupPage} from "@/types/product-groups";
import {Link} from "@/navigation";
import {ProductGridActions} from "@/componants/product/ProductGridActions";

export const TabProductContent = ({storeContext, group, t}: {
    storeContext: StoreContext, group: ProductGroupPage,
    t: { [key: string]: string }
}) => {
    return group.products.map((product, i) => {
        return <ProductGrid product={product} storeContext={storeContext} t={t}/>
    })
}

const ProductGrid = ({storeContext, product, t}: {
    storeContext: StoreContext,
    product: Product,
    t: { [key: string]: string }
}) => {
    return <div className="col-xl-3 col-md-6 col-lg-4 col-sm-6 " key={product.id}>
        <div className="product-wrap-2 mb-25">
            <div className="product-img">
                <Link prefetch={false} href={`/product/${product.description.friendlyUrl}`}>
                    <img alt=""
                         src={product.images.length > 0 ? product.images[0].imageUrl : product.image.imageUrl}/>
                </Link>
                <ProductGridActions storeContext={storeContext} product={product} t={t}/>
            </div>
            <div className="product-content-2">
                <div className="title-price-wrap-2 ">
                    <h3>
                        <Link prefetch={false} href={`/product/${product.description.friendlyUrl}`}>
                            {product.description.name}
                        </Link>
                    </h3>
                    <div className="price-2">
                        <span>{product.finalPrice}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
}
