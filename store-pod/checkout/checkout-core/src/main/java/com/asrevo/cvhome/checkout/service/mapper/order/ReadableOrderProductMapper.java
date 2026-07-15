package com.asrevo.cvhome.checkout.service.mapper.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.model.product.ProductDetails;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProductAttribute;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderProduct;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderProductAttribute;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.utils.PriceUtils;

import lombok.SneakyThrows;

@Component
public class ReadableOrderProductMapper implements Mapper<OrderProduct, ReadableOrderProduct> {

    final ExternalProductService externalProductService;

    private final ExternalMerchantStoreService externalMerchantStoreService;

    public ReadableOrderProductMapper(ExternalProductService externalProductService,
                                      ExternalMerchantStoreService externalMerchantStoreService) {
        this.externalProductService = externalProductService;
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    @Override
    public ReadableOrderProduct convert(OrderProduct source, StoreMerchantId store, LanguageCode language) {
        ReadableOrderProduct orderProduct = new ReadableOrderProduct();
        return this.merge(source, orderProduct, store, language);
    }

    @SneakyThrows
    @Override
    public ReadableOrderProduct merge(OrderProduct source, ReadableOrderProduct target, StoreMerchantId store,
                                      LanguageCode language) {

        target.setId(source.getId());
        target.setOrderedQuantity(source.getProductQuantity());
        try {
            target.setPrice(PriceUtils.getStoreFormatedAmountWithCurrency(externalMerchantStoreService.getStore(store),
                    source.getOneTimeCharge()));
        } catch (Exception e) {
            throw new ConversionRuntimeException("Cannot convert price", e);
        }
        target.setProductName(source.getProductName());
        target.setSku(source.getSku());

        // subtotal = price * quantity
        BigDecimal subTotal = source.getOneTimeCharge();
        subTotal = subTotal.multiply(new BigDecimal(source.getProductQuantity()));

        try {
            String subTotalPrice = PriceUtils
                    .getStoreFormatedAmountWithCurrency(externalMerchantStoreService.getStore(store), subTotal);
            target.setSubTotal(subTotalPrice);
        } catch (Exception e) {
            throw new ConversionRuntimeException("Cannot format price", e);
        }

        if (source.getOrderAttributes() != null) {
            List<ReadableOrderProductAttribute> attributes = new ArrayList<>();
            for (OrderProductAttribute attr : source.getOrderAttributes()) {
                ReadableOrderProductAttribute readableAttribute = new ReadableOrderProductAttribute();
                String price = PriceUtils.getStoreFormatedAmountWithCurrency(
                        externalMerchantStoreService.getStore(store), attr.getProductAttributePrice());
                readableAttribute.setAttributePrice(price);

                readableAttribute.setAttributeName(attr.getProductAttributeName());
                readableAttribute.setAttributeValue(attr.getProductAttributeValueName());
                attributes.add(readableAttribute);
            }
            target.setAttributes(attributes);
        }

        if (!StringUtils.isBlank(source.getSku())) {
            ProductDetails detailedProduct = externalProductService.getDetailedProduct(store, source.getSku(),
                    language);
            target.setProduct(detailedProduct.product());
        }

        return target;
    }

}
