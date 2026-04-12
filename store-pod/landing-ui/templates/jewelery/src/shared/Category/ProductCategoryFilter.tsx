'use client'
import {StoreContext} from "@/types/store-context";
import {Category} from "@/types/category";
import {useTranslations} from "next-intl";
import {Label} from "@/components/ui/label";
import {RadioGroup, RadioGroupItem} from "@/components/ui/radio-group";
import ProductGrid from "@/shared/ProductGrid/ProductGrid";
import {useProductCategoryFilter} from "@store-front/hooks/use-product-category-filter";

export const ProductCategoryFilter = ({storeContext, category}: { storeContext: StoreContext, category: Category }) => {
    const t = useTranslations('PAGE.CATEGORY');
    const {
        productGroup,
        manufacturers,
        selectedManufacturerId,
        handleManufacturerChange
    } = useProductCategoryFilter(storeContext, category);

    return (
        <div className="flex flex-col lg:flex-row gap-8 lg:gap-12">
            {/* Filter sidebar */}
            <aside className="w-full lg:w-48 shrink-0 lg:sticky lg:top-20 self-start">
                <div className="border border-border p-6">
                    <h3 className="text-[10px] tracking-[0.25em] uppercase font-medium text-foreground mb-5">
                        {t('FILTER_BY_MANUFACTURER')}
                    </h3>
                    <RadioGroup
                        value={selectedManufacturerId?.toString() ?? "all"}
                        onValueChange={(value) => {
                            handleManufacturerChange(value === "all" ? null : parseInt(value, 10));
                        }}
                        className="space-y-3"
                    >
                        <div className="flex items-center gap-3">
                            <RadioGroupItem value="all" id="manufacturer-all" className="text-primary"/>
                            <Label htmlFor="manufacturer-all" className="text-xs tracking-wider cursor-pointer text-foreground hover:text-primary transition-colors">
                                {t('ALL')}
                            </Label>
                        </div>
                        {manufacturers?.map((manufacturer) => (
                            <div key={manufacturer.id} className="flex items-center gap-3">
                                <RadioGroupItem
                                    value={manufacturer.id.toString()}
                                    id={`manufacturer-${manufacturer.id}`}
                                    className="text-primary"
                                />
                                <Label
                                    htmlFor={`manufacturer-${manufacturer.id}`}
                                    className="text-xs tracking-wider cursor-pointer text-foreground hover:text-primary transition-colors"
                                >
                                    {manufacturer.description.name}
                                </Label>
                            </div>
                        ))}
                    </RadioGroup>
                </div>
            </aside>

            {/* Product grid */}
            <div className="flex-1 min-w-0">
                <ProductGrid
                    storeContext={storeContext}
                    products={productGroup?.content ?? []}
                    className="grid-cols-2 sm:grid-cols-2 lg:grid-cols-3"
                    emptyStateMessage={t('NO_PRODUCTS_FOUND')}
                />
            </div>
        </div>
    );
};
