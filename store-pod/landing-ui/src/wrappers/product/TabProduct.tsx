import {SectionTitle} from "@/componants/section-title/SectionTitle";
import {Product, ProductGroupPage} from "@/types/product-groups";
import Image from "next/image";

export const TabProduct = async () => {
    const response: ProductGroupPage = await fetch(`http://localhost:8080/api/v1/products/group/FEATURED_ITEM?store=DEFAULT&lang=en`)
        .then((it: Response) => {
            return it.json() as unknown as ProductGroupPage
        })

    return (
        <div
            className={"product-area pt-100 pb-100"}>
            <div className={"container"}>
                <SectionTitle titleText="Featured Products" positionClass="text-center"/>
                <div className="row">
                    <TabProductContent group={response}/>
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
                <a href="/[local]/product/iphone">
                    <Image alt=""
                           src={product.images.length > 0 ? product.images[0].imageUrl : product.image.imageUrl}/>
                </a>
                <div className="product-action-2">
                    <a title="Select options" href="/[local]/product/iphone">
                        <i className="fa fa-cog"></i>
                    </a>
                    <button className="active" title="Add to cart">
                        <i className="fa fa-shopping-cart"></i>
                    </button>
                    <button title="Quick View">
                        <i className="fa fa-eye"></i>
                    </button>
                </div>
            </div>
            <div className="product-content-2">
                <div className="title-price-wrap-2 "><h3>
                    <a href="/[local]/product/iphone">{product.description.name}</a></h3>
                    <div className="price-2">
                        <span>{product.finalPrice}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
}