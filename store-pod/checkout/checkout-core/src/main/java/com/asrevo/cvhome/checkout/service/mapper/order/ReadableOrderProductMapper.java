package com.asrevo.cvhome.checkout.service.mapper.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProductAttribute;
import com.asrevo.cvhome.checkout.errors.PriceNotFormattableException;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderProduct;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderProductAttribute;
import com.asrevo.cvhome.checkout.model.product.ProductDetails;
import com.asrevo.cvhome.checkout.service.facade.product.ProductDetailsComposer;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.utils.PriceUtils;


@Component
public class ReadableOrderProductMapper implements Mapper<OrderProduct, ReadableOrderProduct> {

    final ProductDetailsComposer productDetailsComposer;

    private final ExternalMerchantStoreService externalMerchantStoreService;

    public ReadableOrderProductMapper(ProductDetailsComposer productDetailsComposer,
                                      ExternalMerchantStoreService externalMerchantStoreService) {
        this.productDetailsComposer = productDetailsComposer;
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    @Override
    public ReadableOrderProduct convert(OrderProduct source, StoreMerchantId store, LanguageCode language)
            throws PriceNotFormattableException {
        ReadableOrderProduct orderProduct = new ReadableOrderProduct();
        return this.merge(source, orderProduct, store, language);
    }

    @Override
    public ReadableOrderProduct merge(OrderProduct source, ReadableOrderProduct target, StoreMerchantId store,
                                      LanguageCode language) throws PriceNotFormattableException {

        target.setId(source.getId());
        target.setOrderedQuantity(source.getProductQuantity());
        try {
            target.setPrice(PriceUtils.getStoreFormatedAmountWithCurrency(externalMerchantStoreService.getStore(store),
                    source.getOneTimeCharge()));
        } catch (Exception e) {
            throw PriceNotFormattableException.of(source.getOneTimeCharge(), e);
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
            throw PriceNotFormattableException.of(subTotal, e);
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
            ProductDetails detailedProduct = productDetailsComposer.getDetailedProduct(store, source.getSku(),
                    language);
            target.setProduct(detailedProduct.product());
        }

        return target;
    }

}
