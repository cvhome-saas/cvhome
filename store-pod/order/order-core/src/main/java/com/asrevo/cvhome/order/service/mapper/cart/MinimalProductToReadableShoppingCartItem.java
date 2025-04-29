package com.asrevo.cvhome.order.service.mapper.cart;

import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.order.model.shoppingcart.ReadableShoppingCartItem;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MinimalProductToReadableShoppingCartItem
        implements Mapper<ShoppingCartItem, ReadableShoppingCartItem> {
    private final ExternalProductService externalProductService;

    public MinimalProductToReadableShoppingCartItem(ExternalProductService externalProductService) {
        this.externalProductService = externalProductService;
    }

    @Override
    public ReadableShoppingCartItem convert(
            ShoppingCartItem source, StoreMerchantId store, LanguageCode language) {
        ReadableShoppingCartItem destination = new ReadableShoppingCartItem();
        this.merge(source, destination, store, language);
        return destination;
    }

    @SneakyThrows
    @Override
    public ReadableShoppingCartItem merge(
            ShoppingCartItem item,
            ReadableShoppingCartItem destination,
            StoreMerchantId store,
            LanguageCode language) {
        ReadableMinimalProduct source =
                externalProductService.getMinimalProduct(store, item.getSku(), language);
        BeanUtils.copyProperties(destination, source);
        return destination;
    }
}
