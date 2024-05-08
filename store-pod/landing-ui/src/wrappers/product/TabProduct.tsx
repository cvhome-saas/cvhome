import {SectionTitle} from "@/componants/section-title/SectionTitle";
import {Product, ProductGroupPage} from "@/types/product-groups";
import {StoreContext} from "@/types/store-context";
import {Link} from "@/navigation";
import {ProductService} from "@/services/product-service";
import {ProductGridActions} from "@/componants/product/ProductGridActions";
import {useTranslations} from "next-intl";

export const TabProduct = async ({storeContext}: { storeContext: StoreContext }) => {
    const featuredItemsGroup = await ProductService.getFeaturedItemsProductGroup(storeContext)
    return (
        <div
            className={"product-area pt-100 pb-100"}>
            <div className={"container"}>
                <SectionTitle titleText="Featured Products" positionClass="text-center"/>
                <div className="row">
                    <TabProductContent group={featuredItemsGroup} storeContext={storeContext}/>
                </div>
            </div>
        </div>
    );
};


const TabProductContent = ({storeContext, group}: { storeContext: StoreContext, group: ProductGroupPage }) => {
    return group.products.map((product, i) => {
        return <ProductGrid key={product.id} product={product} storeContext={storeContext}/>
    })
}

const ProductGrid = ({storeContext, key, product}: {
    storeContext: StoreContext,
    key: number,
    product: Product
}) => {
    const t = useTranslations('Product');
    return <div className="col-xl-3 col-md-6 col-lg-4 col-sm-6 " key={key}>
        <div className="product-wrap-2 mb-25  ">
            <div className="product-img">
                <Link prefetch={false} href={`/product/${product.description.friendlyUrl}`}>
                    <img alt=""
                         src={product.images.length > 0 ? product.images[0].imageUrl : product.image.imageUrl}/>
                </Link>
                <ProductGridActions storeContext={storeContext} product={product}
                                    t={{
                                        'SKU': t('SKU'),
                                        'Add to cart': t('Add to cart'),
                                        'Categories': t('Categories')
                                    }}/>
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
