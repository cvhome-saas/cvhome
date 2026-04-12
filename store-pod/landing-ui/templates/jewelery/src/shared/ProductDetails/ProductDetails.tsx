import {Product} from "@/types/product-groups";
import {ProductDetailedActionBox} from "@/shared/ProductDetails/ProductDetailedActionBox";
import {parseDescription} from "@/services/description-view-util";
import {ProductDetailsImageGallery} from "@/shared/ProductDetails/ProductDetailsImageGallery";
import {StoreContext} from "@/types/store-context";
import {Accordion, AccordionContent, AccordionItem, AccordionTrigger} from "@/components/ui/accordion";

export const ProductDetails = ({storeContext, p, t}: {
    storeContext: StoreContext,
    p: Product,
    t: (key: string) => string;
}) => {
    return (
        <div className="grid items-start grid-cols-1 lg:grid-cols-2 gap-10 lg:gap-16">
            {/* Image gallery */}
            <div className="w-full self-start">
                <ProductDetailsImageGallery product={p}/>
            </div>

            {/* Product info */}
            <div className="w-full space-y-8">
                {/* Name */}
                {p.description && (
                    <h1 className="font-['Cormorant_Garamond',serif] text-3xl sm:text-4xl font-light tracking-wide text-foreground leading-tight">
                        {p.description.name}
                    </h1>
                )}

                {/* Price */}
                {p.productPrice && (
                    <div className="flex items-baseline gap-4">
                        <p className="font-['Jost',sans-serif] text-2xl font-medium tracking-wider text-foreground">
                            {p.productPrice.finalPrice}
                        </p>
                        {p.productPrice.discounted && (
                            <p className="text-base text-muted-foreground line-through tracking-wider">
                                {p.productPrice.originalPrice}
                            </p>
                        )}
                    </div>
                )}

                {/* Thin gold divider */}
                <div className="h-px bg-primary/30"/>

                {/* Add to cart */}
                <ProductDetailedActionBox storeContext={storeContext} product={p}/>

                {/* Description accordion */}
                {p.description && (
                    <Accordion type="single" collapsible defaultValue="description" className="w-full border-t border-border">
                        <AccordionItem value="description" className="border-b border-border">
                            <AccordionTrigger className="font-['Jost',sans-serif] text-xs tracking-[0.2em] uppercase font-medium py-4 hover:text-primary hover:no-underline">
                                {t('PRODUCT_DESCRIPTION')}
                            </AccordionTrigger>
                            <AccordionContent>
                                <div
                                    className="prose prose-sm text-muted-foreground font-light leading-relaxed pb-4"
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
