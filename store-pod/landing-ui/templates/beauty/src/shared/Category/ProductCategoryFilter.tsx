'use client'
import {StoreContext} from "@/types/store-context";
import {Category} from "@/types/category";
import {useTranslations} from "next-intl";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {RadioGroup, RadioGroupItem} from "@/components/ui/radio-group";
import {Label} from "@/components/ui/label";
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
        <div className="flex flex-col lg:flex-row gap-8">
            <Card className="w-full lg:w-1/4 sticky top-24 self-start rounded-3xl border-primary/10 shadow-xl shadow-primary/5 bg-card/50 backdrop-blur-sm overflow-hidden">
                <CardHeader className="bg-primary/5 border-b border-primary/5">
                    <CardTitle className="font-serif text-xl text-primary">{t('FILTER_BY_MANUFACTURER')}</CardTitle>
                </CardHeader>
                <CardContent className="pt-6">
                    <RadioGroup
                        value={selectedManufacturerId?.toString() ?? "all"}
                        onValueChange={(value) => {
                            handleManufacturerChange(value === "all" ? null : parseInt(value, 10));
                        }}
                        className="space-y-3"
                    >
                        <div className="flex items-center space-x-3 p-2 rounded-xl hover:bg-primary/5 transition-colors">
                            <RadioGroupItem value="all" id="manufacturer-all" className="border-primary/30 text-primary focus-visible:ring-primary/20"/>
                            <Label htmlFor="manufacturer-all" className="text-sm font-medium cursor-pointer text-foreground/80">{t('ALL')}</Label>
                        </div>
                        {manufacturers?.map((manufacturer) => (
                            <div key={manufacturer.id} className="flex items-center space-x-3 p-2 rounded-xl hover:bg-primary/5 transition-colors">
                                <RadioGroupItem value={manufacturer.id.toString()}
                                                id={`manufacturer-${manufacturer.id}`}
                                                className="border-primary/30 text-primary focus-visible:ring-primary/20"/>
                                <Label htmlFor={`manufacturer-${manufacturer.id}`} className="text-sm font-medium cursor-pointer text-foreground/80">
                                    {manufacturer.description.name}
                                </Label>
                            </div>
                        ))}
                    </RadioGroup>
                </CardContent>
                <div className="p-4 bg-primary/5 text-center">
                    <p className="text-[10px] uppercase tracking-[0.2em] text-muted-foreground">Curated for You</p>
                </div>
            </Card>
            <div className="w-full lg:w-3/4">
                <ProductGrid
                    storeContext={storeContext}
                    products={productGroup?.content ?? []}
                    className="grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 xl:grid-cols-3 gap-8"
                    emptyStateMessage={t('NO_PRODUCTS_FOUND')}
                />
            </div>
        </div>);
};