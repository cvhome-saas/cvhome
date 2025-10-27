'use client'
import {StoreContext} from "@/types/store-context";
import {Category} from "@/types/category";
import ProductItem from "@/componantes/ProductItem/ProductItem";
import {Manufacturer, ProductGroupPage} from "@/types/product-groups";
import {ProductCategory} from "@/services/product-category";
import {useEffect, useState} from "react";
import {useTranslations} from "next-intl";

export const ProductCategoryFilter = ({storeContext, category}: { storeContext: StoreContext, category: Category }) => {
    const t = useTranslations('PAGE.CATEGORY');
    const [productGroup, setProductGroup] = useState<ProductGroupPage | undefined>();
    const [manufacturers, setManufacturers] = useState<Manufacturer[] | undefined>();
    const [selectedManufacturerId, setSelectedManufacturerId] = useState<number | null>(null);

    useEffect(() => {
        ProductCategory.getProducts(storeContext, category.id).then(it => {
            setProductGroup(it);
        });
        ProductCategory.getManufacturers(storeContext, category.id).then(it => {
            setManufacturers(it);
        })
    }, [storeContext, category.id]);

    const handleManufacturerChange = (manufacturerId: number | null) => {
        setSelectedManufacturerId(manufacturerId); // Update the selected ID state

        ProductCategory.getProducts(storeContext, category.id, manufacturerId ?? undefined)
            .then(it => {
                setProductGroup(it);
            });
    };

    return (
        <div className="flex flex-col lg:flex-row gap-6">
            <div
                className="w-full lg:w-1/5 p-6 border border-border rounded-lg shadow-sm sticky top-0 overflow-y-auto self-start"
                style={{paddingBottom: '1.5rem'}}>
                <h2 className="text-xl font-bold text-foreground mb-6">
                    {t('FILTER_BY_MANUFACTURER')}
                </h2>
                <div className="space-y-4">
                    <div className="flex items-center">
                        <input
                            type="radio"
                            id="manufacturer-all"
                            name="manufacturer"
                            value=""
                            checked={selectedManufacturerId === null}
                            onChange={() => handleManufacturerChange(null)}
                            className="size-4 text-primary border-border focus:ring-primary"
                        />
                        <label
                            htmlFor="manufacturer-all"
                            className="ms-3 text-sm font-medium text-foreground"
                        >
                            {t('ALL')}
                        </label>
                    </div>
                    {manufacturers &&
                        manufacturers.map((manufacturer) => (
                            <div key={manufacturer.id} className="flex items-center">
                                <input
                                    type="radio"
                                    id={`manufacturer-${manufacturer.id}`}
                                    name="manufacturer"
                                    value={manufacturer.id}
                                    checked={selectedManufacturerId === manufacturer.id}
                                    onChange={() => handleManufacturerChange(manufacturer.id)}
                                    className="size-4 text-primary border-border focus:ring-primary"
                                />
                                <label
                                    htmlFor={`manufacturer-${manufacturer.id}`}
                                    className="ms-3 text-sm font-medium text-foreground">
                                    {manufacturer.description.name}
                                </label>
                            </div>
                        ))}
                </div>
            </div>
            <div className="w-full lg:w-4/5 grid grid-cols-1 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                {productGroup?.content && productGroup.content.length > 0 ? (
                    productGroup.content.map((product) => (
                        <ProductItem key={product.id} storeContext={storeContext} product={product}/>
                    ))
                ) : (
                    <div className="col-span-full text-center text-muted-foreground py-10">
                        {t('NO_PRODUCTS_FOUND')}
                    </div>
                )}
            </div>
        </div>);
};