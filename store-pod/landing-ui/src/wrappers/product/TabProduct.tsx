import {SectionTitle} from "@/componants/section-title/SectionTitle";
import {Product} from "@/types/product-groups";
import {StoreContext} from "@/types/store-context";
import {ProductService} from "@/services/product-service";
import {TabProductContent} from "@/componants/product/TabProductContent";
import {getTranslations} from "next-intl/server";

export const TabProduct = async ({storeContext}: { storeContext: StoreContext }) => {
    const featuredItemsGroup = await ProductService.getFeaturedItemsProductGroup(storeContext)
    const t = await getTranslations('Product');
    return (
        <div
            className={"product-area pt-100 pb-100"}>
            <div className={"container"}>
                <SectionTitle titleText="Featured Products" positionClass="text-center"/>
                <div className="row">
                    <TabProductContent group={featuredItemsGroup} storeContext={storeContext} t={{
                        'SKU': t('SKU'),
                        'Add to cart': t('Add to cart'),
                        'Categories': t('Categories')
                    }}/>
                </div>
            </div>
        </div>
    );
};


