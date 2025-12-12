'use client'
import React, {useMemo} from "react";
import {StoreContext} from "@/types/store-context";
import {Category} from "@/types/category";
import {useTranslations} from "next-intl";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {RadioGroup} from "@/components/ui/radio-group";
import ProductGrid from "@/shared/ProductGrid/ProductGrid";
import {useProductCategoryFilter} from "@store-front/hooks/use-product-category-filter";
import {cn} from "@/lib/utils";

type ChipProps = {
    id: string;
    label: string;
    value: string;
    selected: boolean;
    onSelect: (value: string) => void;
};

function FilterChip({id, label, value, selected, onSelect}: ChipProps) {
    return (
        <button
            id={id}
            type="button"
            onClick={() => onSelect(value)}
            aria-pressed={selected}
            className={cn(
                "inline-flex items-center justify-center rounded-full border px-3 py-1.5 text-xs font-semibold tracking-[0.12em] uppercase transition",
                selected
                    ? "border-primary bg-primary text-primary-foreground"
                    : "border-border bg-background text-foreground hover:bg-accent"
            )}
        >
            {label}
        </button>
    );
}

export const ProductCategoryFilter = ({storeContext, category}: { storeContext: StoreContext, category: Category }) => {
    const t = useTranslations('PAGE.CATEGORY');

    const {
        productGroup,
        manufacturers,
        selectedManufacturerId,
        handleManufacturerChange
    } = useProductCategoryFilter(storeContext, category);

    const selectedValue = useMemo(() => selectedManufacturerId?.toString() ?? "all", [selectedManufacturerId]);

    return (
        <div className="grid grid-cols-1 gap-8 lg:grid-cols-12">
            <div className="lg:col-span-3">
                <Card className="sticky top-6 rounded-2xl border border-border bg-background">
                    <CardHeader className="pb-1">
                        <CardTitle className="text-sm font-semibold tracking-[0.18em] uppercase">
                            {t('FILTER_BY_MANUFACTURER')}
                        </CardTitle>
                    </CardHeader>

                    <CardContent className="pt-0">
                        <RadioGroup
                            value={selectedValue}
                            onValueChange={(value) => {
                                handleManufacturerChange(value === "all" ? null : parseInt(value, 10));
                            }}
                            className="flex flex-wrap gap-2"
                        >
                            <FilterChip
                                id="manufacturer-all"
                                value="all"
                                label={t('ALL')}
                                selected={selectedValue === "all"}
                                onSelect={(value) => {
                                    handleManufacturerChange(value === "all" ? null : parseInt(value, 10));
                                }}
                            />

                            {manufacturers?.map((m) => {
                                const value = m.id.toString();
                                const label = m.description?.name ?? value;

                                return (
                                    <FilterChip
                                        key={m.id}
                                        id={`manufacturer-${m.id}`}
                                        value={value}
                                        label={label}
                                        selected={selectedValue === value}
                                        onSelect={(v) => handleManufacturerChange(parseInt(v, 10))}
                                    />
                                );
                            })}
                        </RadioGroup>
                    </CardContent>
                </Card>
            </div>
            <div className="lg:col-span-9">
                <ProductGrid
                    storeContext={storeContext}
                    products={productGroup?.content ?? []}
                    className="grid-cols-2 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-3"
                    emptyStateMessage={t('NO_PRODUCTS_FOUND')}
                />
            </div>
        </div>
    );
};