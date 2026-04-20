'use client'
import React, {useEffect, useState} from "react";
import {StoreContext} from "@/types/store-context";
import {ProductService} from "@/services/product-service";
import {ProductGroup} from "@/types/product-groups";
import {SectionTitle} from "@/shared/Common/SectionTitle";
import ProductSwiperGrid from "@/shared/ProductGrid/ProductSwiperGrid";
import {useTranslations} from "next-intl";

export const RelatedProducts = ({storeContext, productId}: {
    storeContext: StoreContext;
    productId: number;
}) => {
    const t = useTranslations('PAGE.PRODUCT');
    const [related, setRelated] = useState<ProductGroup | undefined>(undefined);

    useEffect(() => {
        ProductService.getRelatedProductGroup(storeContext, productId).then(setRelated);
    }, [productId]);

    if (!related?.products || !related.active || related.products.length === 0) {
        return null;
    }

    return (
        <div className="mx-auto max-w-2xl px-4 py-16 sm:px-6 sm:py-24 lg:max-w-7xl lg:px-8">
            <SectionTitle title={t('RELATED_PRODUCTS')}/>
            <ProductSwiperGrid storeContext={storeContext} products={related.products}/>
        </div>
    );
};
