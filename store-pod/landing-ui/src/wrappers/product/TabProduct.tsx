import {SectionTitle} from "@/componants/section-title/SectionTitle";
import {Product, ProductGroupPage} from "@/types/product-groups";
import {StoreContext} from "@/types/store-context";
import {Link} from "@/navigation";
import {ProductService} from "@/services/product-service";

export const TabProduct = async ({storeContext}: { storeContext: StoreContext }) => {
    const featuredItemsGroup = await ProductService.getFeaturedItemsProductGroup(storeContext)
    return (
        <div
            className={"product-area pt-100 pb-100"}>
            <div className={"container"}>
                <SectionTitle titleText="Featured Products" positionClass="text-center"/>
                <div className="row">
                    <TabProductContent group={featuredItemsGroup}/>
                </div>
            </div>
        </div>
    );
};


const TabProductContent = ({group}: { group: ProductGroupPage }) => {
    return group.products.map((product, i) => {
        return <ProductGrid key={product.id} product={product}/>
    })
}

const ProductGrid = ({key, product}: { key: number, product: Product }) => {
    return <div className="col-xl-3 col-md-6 col-lg-4 col-sm-6 " key={key}>
        <div className="product-wrap-2 mb-25  ">
            <div className="product-img">
                <Link href={`/product/${product.description.friendlyUrl}`}>
                    <img alt=""
                         src={product.images.length > 0 ? product.images[0].imageUrl : product.image.imageUrl}/>
                </Link>
                <div className="product-action-2">
                    <Link title="Select options" href={`/product/${product.description.friendlyUrl}`}>
                        <i className="fa fa-cog"></i>
                    </Link>
                    <button className="active" title="Add to cart">
                        <i className="fa fa-shopping-cart"></i>
                    </button>
                    <button title="Quick View">
                        <i className="fa fa-eye"></i>
                    </button>
                </div>
            </div>
            <div className="product-content-2">
                <div className="title-price-wrap-2 ">
                    <h3>
                        <Link href={`/product/${product.description.friendlyUrl}`}>
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