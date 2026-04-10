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
        <div className="grid items-start grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-16 max-w-7xl mx-auto px-4 py-8">

            <div className="w-full top-24 lg:sticky self-start">
                <ProductDetailsImageGallery product={p}/>
            </div>


            <div className="w-full space-y-10 lg:pt-4">
                <div className="space-y-6">
                    <div className="space-y-2">
                        {p.description && (
                            <h1 className="text-4xl sm:text-5xl font-serif font-bold tracking-tight text-foreground leading-tight">
                                {p.description.name}
                            </h1>
                        )}
                        <div className="h-1.5 w-20 bg-primary/20 rounded-full" />
                    </div>

                    {p.productPrice && (
                        <div className="flex items-baseline flex-wrap gap-4">
                            <p className="text-primary text-4xl sm:text-5xl font-bold tracking-tighter">
                                {p.productPrice.finalPrice}
                            </p>
                            {p.productPrice.discounted && (
                                <p className="text-2xl text-muted-foreground line-through decoration-primary/20 font-light">
                                    {p.productPrice.originalPrice}
                                </p>
                            )}
                        </div>
                    )}
                </div>

                <div className="bg-secondary/20 p-8 rounded-3xl border border-primary/5 shadow-sm">
                    <ProductDetailedActionBox storeContext={storeContext} product={p}/>
                </div>

                {p.description && (
                    <Accordion type="single" collapsible defaultValue="item-1" className="w-full">
                        <AccordionItem value="item-1" className="border-b border-primary/10">
                            <AccordionTrigger className="text-xl font-serif font-semibold hover:text-primary transition-colors">
                                {t('PRODUCT_DESCRIPTION')}
                            </AccordionTrigger>
                            <AccordionContent>
                                <div
                                    className="prose prose-lg dark:prose-invert text-muted-foreground leading-relaxed font-light"
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