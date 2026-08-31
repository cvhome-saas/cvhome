package com.asrevo.cvhome.checkout.service.mapper.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProductOption;
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

/**
 * The one mapper an order line goes through on every read (confirmation, console detail, customer history).
 * The persisted snapshot — name, price, option/value labels — is the source of truth; the live catalog product
 * is attached on top for imagery and copy, and its absence degrades the line, never fails it. Callers that
 * render a whole order prefetch the composed details once and use the batch-aware overload.
 */
@Component
public class ReadableOrderProductMapper implements Mapper<OrderProduct, ReadableOrderProduct> {

    private final ProductDetailsComposer productDetailsComposer;

    private final ExternalMerchantStoreService externalMerchantStoreService;

    public ReadableOrderProductMapper(ProductDetailsComposer productDetailsComposer,
                                      ExternalMerchantStoreService externalMerchantStoreService) {
        this.productDetailsComposer = productDetailsComposer;
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    @Override
    public ReadableOrderProduct convert(OrderProduct source, StoreMerchantId store, LanguageCode language)
            throws PriceNotFormattableException {
        ProductDetails details = source.getSku() == null ? null
                : productDetailsComposer.getDetailedProducts(store, List.of(source.getSku()), language)
                .get(source.getSku());
        return convert(source, details, store);
    }

    @Override
    public ReadableOrderProduct merge(OrderProduct source, ReadableOrderProduct destination, StoreMerchantId store,
                                      LanguageCode language) throws PriceNotFormattableException {
        return convert(source, store, language);
    }

    /**
     * The batch-aware form: the caller prefetched the whole order's details in one composer call.
     */
    public ReadableOrderProduct convert(OrderProduct source, ProductDetails details, StoreMerchantId store)
            throws PriceNotFormattableException {
        ReadableOrderProduct target = new ReadableOrderProduct();
        target.setId(source.getId());
        target.setOrderedQuantity(source.getProductQuantity());
        target.setProductName(source.getProductName());
        target.setSku(source.getSku());
        target.setPrice(formatted(store, source.getOneTimeCharge()));
        BigDecimal subTotal = source.getOneTimeCharge().multiply(new BigDecimal(source.getProductQuantity()));
        target.setSubTotal(formatted(store, subTotal));
        target.setAttributes(attributes(source));
        if (details != null) {
            attachLiveProduct(target, details.product());
        }
        return target;
    }

    /**
     * The persisted option/value snapshot, rendered through the existing attributes vocabulary so no order view
     * needed a new DTO.
     */
    private static List<ReadableOrderProductAttribute> attributes(OrderProduct source) {
        List<ReadableOrderProductAttribute> attributes = new ArrayList<>();
        source.getOrderOptions().stream()
                .sorted(Comparator.comparing(OrderProductOption::getSortOrder,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(OrderProductOption::getOptionCode))
                .forEach(option -> {
                    ReadableOrderProductAttribute attribute = new ReadableOrderProductAttribute();
                    attribute.setAttributeName(option.getOptionName());
                    attribute.setAttributeValue(option.getValueName());
                    attributes.add(attribute);
                });
        return attributes;
    }

    private void attachLiveProduct(ReadableOrderProduct target, ReadableMinimalProduct product) {
        if (product == null) {
            return;
        }
        target.setProduct(product);
        ReadableImage defaultImage = null;
        for (ReadableImage image : product.getImages() == null ? List.<ReadableImage>of() : product.getImages()) {
            if (defaultImage == null || image.isDefaultImage()) {
                defaultImage = image;
            }
        }
        if (defaultImage != null) {
            target.setImage(defaultImage.getImageUrl());
        }
    }

    private String formatted(StoreMerchantId store, BigDecimal amount) throws PriceNotFormattableException {
        try {
            return PriceUtils.getStoreFormatedAmountWithCurrency(externalMerchantStoreService.getStore(store),
                    amount);
        } catch (Exception e) {
            throw PriceNotFormattableException.of(amount, e);
        }
    }
}
