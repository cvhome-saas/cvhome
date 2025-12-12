import {useCallback, useEffect, useState} from "react";
import {Category} from "@store-front/types";
import {Manufacturer, ProductGroupPage} from "@store-front/types";
import {StoreContext} from "@store-front/types";
import {ProductCategory} from "@store-front/services/product-category";

export const useProductCategoryFilter = (storeContext: StoreContext, category: Category) => {
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

    const handleManufacturerChange = useCallback((manufacturerId: number | null) => {
        setSelectedManufacturerId(manufacturerId);
        ProductCategory.getProducts(storeContext, category.id, manufacturerId ?? undefined)
            .then(it => {
                setProductGroup(it);
            });
    }, [storeContext, category.id]);

    return {
        productGroup,
        manufacturers,
        selectedManufacturerId,
        handleManufacturerChange
    };
};