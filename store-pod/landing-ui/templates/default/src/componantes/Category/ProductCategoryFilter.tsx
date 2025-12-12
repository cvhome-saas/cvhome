'use client'
import {StoreContext} from "@/types/store-context";
import {Category} from "@/types/category";
import {useTranslations} from "next-intl";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {RadioGroup, RadioGroupItem} from "@/components/ui/radio-group";
import {Label} from "@/components/ui/label";
import ProductGrid from "@/componantes/ProductGrid/ProductGrid";
import {useProductCategoryFilter} from "@/hooks/use-product-category-filter";

export const ProductCategoryFilter = ({storeContext, category}: { storeContext: StoreContext, category: Category }) => {
    const t = useTranslations('PAGE.CATEGORY');
    const {
        productGroup,
        manufacturers,
        selectedManufacturerId,
        handleManufacturerChange
    } = useProductCategoryFilter(storeContext, category);

    return (
        <div className="flex flex-col lg:flex-row gap-6">
            <Card className="w-full lg:w-1/5 sticky top-0 self-start">
                <CardHeader>
                    <CardTitle>{t('FILTER_BY_MANUFACTURER')}</CardTitle>
                </CardHeader>
                <CardContent>
                    <RadioGroup
                        value={selectedManufacturerId?.toString() ?? "all"}
                        onValueChange={(value) => {
                            handleManufacturerChange(value === "all" ? null : parseInt(value, 10));
                        }}
                        className="space-y-2"
                    >
                        <div className="flex items-center space-x-2">
                            <RadioGroupItem value="all" id="manufacturer-all"/>
                            <Label htmlFor="manufacturer-all">{t('ALL')}</Label>
                        </div>
                        {manufacturers?.map((manufacturer) => (
                            <div key={manufacturer.id} className="flex items-center space-x-2">
                                <RadioGroupItem value={manufacturer.id.toString()}
                                                id={`manufacturer-${manufacturer.id}`}/>
                                <Label htmlFor={`manufacturer-${manufacturer.id}`}>
                                    {manufacturer.description.name}
                                </Label>
                            </div>
                        ))}
                    </RadioGroup>
                </CardContent>
            </Card>
            <div className="w-full lg:w-4/5">
                <ProductGrid
                    storeContext={storeContext}
                    products={productGroup?.content ?? []}
                    className="grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-3"
                    emptyStateMessage={t('NO_PRODUCTS_FOUND')}
                />
            </div>
        </div>);
};