import {Product} from "@/types/product-groups";
import ProductItem from "@/app/_themes/DEFAULT/componantes/ProductItem/ProductItem";
import {StoreContext} from "@/types/store-context";

export default function ProductGrid({storeContext, products}: {
    storeContext: StoreContext
    products: Product[]
}) {
    return <div
        className="grid grid-cols-1 gap-x-6 gap-y-10 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 xl:gap-x-8 border-border">
        {products.map((product) =>
            <ProductItem key={product.id} storeContext={storeContext} product={product}/>
        )}
    </div>;
}