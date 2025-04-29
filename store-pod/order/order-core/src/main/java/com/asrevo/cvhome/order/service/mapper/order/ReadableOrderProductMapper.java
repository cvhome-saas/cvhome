package com.asrevo.cvhome.order.service.mapper.order;

import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.order.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.order.entity.order.orderproduct.OrderProductAttribute;
import com.asrevo.cvhome.order.model.order.ReadableOrderProduct;
import com.asrevo.cvhome.order.model.order.ReadableOrderProductAttribute;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.PriceUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class ReadableOrderProductMapper implements Mapper<OrderProduct, ReadableOrderProduct> {

    final ExternalProductService productService;
    private final ExternalMerchantStoreService externalMerchantStoreService;

    public ReadableOrderProductMapper(
            ExternalProductService productService,
            ExternalMerchantStoreService externalMerchantStoreService) {
        this.productService = productService;
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    @Override
    public ReadableOrderProduct convert(
            OrderProduct source, StoreMerchantId store, LanguageCode language) {
        ReadableOrderProduct orderProduct = new ReadableOrderProduct();
        return this.merge(source, orderProduct, store, language);
    }

    @Override
    public ReadableOrderProduct merge(
            OrderProduct source,
            ReadableOrderProduct target,
            StoreMerchantId store,
            LanguageCode language) {

        Validate.notNull(source, "OrderProduct cannot be null");
        Validate.notNull(target, "ReadableOrderProduct cannot be null");
        Validate.notNull(store, "store cannot be null");
        Validate.notNull(language, "Language cannot be null");

        target.setId(source.getId());
        target.setOrderedQuantity(source.getProductQuantity());
        try {
            target.setPrice(
                    PriceUtils.getStoreFormatedAmountWithCurrency(
                            externalMerchantStoreService.getStore(store),
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
            String subTotalPrice =
                    PriceUtils.getStoreFormatedAmountWithCurrency(
                            externalMerchantStoreService.getStore(store), subTotal);
            target.setSubTotal(subTotalPrice);
        } catch (Exception e) {
            throw new ConversionRuntimeException("Cannot format price", e);
        }

        if (source.getOrderAttributes() != null) {
            List<ReadableOrderProductAttribute> attributes = new ArrayList<>();
            for (OrderProductAttribute attr : source.getOrderAttributes()) {
                ReadableOrderProductAttribute readableAttribute =
                        new ReadableOrderProductAttribute();
                String price =
                        PriceUtils.getStoreFormatedAmountWithCurrency(
                                externalMerchantStoreService.getStore(store),
                                attr.getProductAttributePrice());
                readableAttribute.setAttributePrice(price);

                readableAttribute.setAttributeName(attr.getProductAttributeName());
                readableAttribute.setAttributeValue(attr.getProductAttributeValueName());
                attributes.add(readableAttribute);
            }
            target.setAttributes(attributes);
        }

        if (!StringUtils.isBlank(source.getSku())) {
            target.setProduct(productService.getFullProduct(store, source.getSku(), language));
        }

        return target;
    }
}
