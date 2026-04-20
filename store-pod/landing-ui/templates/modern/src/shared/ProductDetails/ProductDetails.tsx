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

    const name = p.description?.name ?? "";
    const finalPrice = p.productPrice?.finalPrice;
    const originalPrice = p.productPrice?.originalPrice;
    const discounted = !!p.productPrice?.discounted;

    return (
        <section className="mx-auto w-full">
            <div className="grid grid-cols-1 gap-10 lg:grid-cols-12 lg:gap-12">
                <div className="lg:col-span-7">
                    <div className="rounded-2xl border border-border bg-background">
                        <ProductDetailsImageGallery product={p}/>
                    </div>
                </div>

                <aside className="lg:col-span-5">
                    <div className="lg:sticky lg:top-6">
                        <div className="rounded-2xl border border-border bg-background p-6">
                            <div className="flex items-start justify-between gap-4">
                                <div className="min-w-0">
                                    <h1 className="mt-2 text-2xl font-semibold tracking-tight text-foreground sm:text-3xl">
                                        {name}
                                    </h1>
                                </div>
                            </div>

                            {finalPrice && (
                                <div className="mt-5 flex flex-wrap items-end gap-3">
                                    <p className="text-3xl font-semibold tracking-tight text-foreground">
                                        {finalPrice}
                                    </p>
                                    {discounted && originalPrice && (
                                        <p className="text-sm text-muted-foreground line-through">
                                            {originalPrice}
                                        </p>
                                    )}
                                </div>
                            )}

                            <div className="mt-6">
                                <ProductDetailedActionBox storeContext={storeContext} product={p}/>
                            </div>
                        </div>
                        {p.description && (
                            <div className="mt-6 rounded-2xl border border-border bg-background p-2">
                                <Accordion type="single" collapsible defaultValue="description" className="w-full">
                                    <AccordionItem value="description" className="border-b-0">
                                        <AccordionTrigger className="px-4 text-sm font-medium">
                                            {t('PRODUCT_DESCRIPTION')}
                                        </AccordionTrigger>
                                        <AccordionContent className="px-4">
                                            <div
                                                className="prose prose-sm max-w-none dark:prose-invert text-muted-foreground"
                                                dangerouslySetInnerHTML={{__html: `${parseDescription(p.description)}`}}
                                            />
                                        </AccordionContent>
                                    </AccordionItem>
                                </Accordion>
                            </div>
                        )}
                    </div>
                </aside>
            </div>
        </section>
    );
};