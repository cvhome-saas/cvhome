import {Product} from "@/types/product-groups";
import {ProductDetailedActionBox} from "@/shared/ProductDetails/ProductDetailedActionBox";
import {parseDescription} from "@/services/description-view-util";
import {ProductDetailsImageGallery} from "@/shared/ProductDetails/ProductDetailsImageGallery";
import {StoreContext} from "@/types/store-context";
import {Separator} from "@/components/ui/separator";
import {Accordion, AccordionContent, AccordionItem, AccordionTrigger,} from "@/components/ui/accordion";

export const ProductDetails = ({storeContext, p, t}: {
    storeContext: StoreContext,
    p: Product,
    t: (key: string) => string;
}) => {

    return (
        <div className="grid items-start grid-cols-1 lg:grid-cols-2 gap-8">

            <div className="w-full top-0 self-start">
                <ProductDetailsImageGallery product={p}/>
            </div>


            <div className="w-full space-y-6">
                <div className="space-y-4">
                    {
                        p.description &&
                        <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-foreground">{p.description.name}</h1>
                    }

                    {
                        p.productPrice &&
                        <div className="flex items-baseline flex-wrap gap-4">
                            <p className="text-foreground text-3xl sm:text-4xl font-bold">
                                {p.productPrice.finalPrice}
                            </p>
                            {p.productPrice.discounted &&
                                <p className="text-xl text-muted-foreground line-through">
                                    {p.productPrice.originalPrice}
                                </p>
                            }
                        </div>
                    }
                </div>

                <Separator/>
                <ProductDetailedActionBox storeContext={storeContext} product={p}/>

                {p.description && (
                    <Accordion type="single" collapsible defaultValue="item-1" className="w-full">
                        <AccordionItem value="item-1">
                            <AccordionTrigger className="text-lg font-semibold">
                                {t('PRODUCT_DESCRIPTION')}
                            </AccordionTrigger>
                            <AccordionContent>
                                <div
                                    className="prose prose-sm dark:prose-invert text-muted-foreground"
                                    dangerouslySetInnerHTML={{__html: `${parseDescription(p.description)}`}}
                                />
                            </AccordionContent>
                        </AccordionItem>
                    </Accordion>
                )}
            </div>
        </div>
    );
};