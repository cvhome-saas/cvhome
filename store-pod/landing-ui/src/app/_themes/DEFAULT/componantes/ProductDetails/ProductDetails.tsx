import {Product} from "@/types/product-groups";
import {ProductDetailedAddToCart} from "@/app/_themes/DEFAULT/componantes/ProductDetails/ProductDetailedAddToCart";
import {parseDescription} from "@/utils/description-view-util";
import {ProductDetailsImageGallery} from "@/app/_themes/DEFAULT/componantes/ProductDetails/ProductDetailsImageGallery";
import {StoreContext} from "@/types/store-context";

export const ProductDetails = ({storeContext, p, t}: {
    storeContext: StoreContext,
    p: Product,
    t: (key: string) => string;
}) => {

    return (
        <div className="grid items-start grid-cols-1 lg:grid-cols-2 gap-8 max-lg:gap-12 max-sm:gap-8">

            <div className="w-full top-0">
                <div className="flex flex-row gap-2">
                    <ProductDetailsImageGallery product={p}/>
                </div>
            </div>


            <div className="w-full">
                <div>
                    {
                        p.description &&
                        <h3 className="text-lg sm:text-xl font-semibold text-foreground">{p.description.name}</h3>
                    }

                    {
                        p.productPrice &&
                        <div className="flex items-center flex-wrap gap-4 mt-6">
                            <h4 className="text-foreground text-2xl sm:text-3xl font-semibold">
                                {p.productPrice.finalPrice}
                            </h4>
                            {p.productPrice.discounted &&
                                <p className="text-neutral text-lg line-through">
                                    {p.productPrice.originalPrice}
                                </p>
                            }
                        </div>
                    }
                </div>


                <hr className="my-6 border-border"/>
                <div>
                    <ProductDetailedAddToCart storeContext={storeContext} product={p}/>
                </div>


                {p.description && (
                    <>
                        <hr className="my-6 border-border"/>
                        <div>
                            <h3 className="text-lg sm:text-xl font-semibold text-foreground">
                                {t('PRODUCT_DESCRIPTION')}
                            </h3>
                            <div className="mt-4">
                                <div className="transition-all">
                                    <div className="pb-4 px-4">
                                        <div
                                            className="text-sm text-neutral leading-relaxed"
                                            dangerouslySetInnerHTML={{__html: `${parseDescription(p.description)}`}}
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
};