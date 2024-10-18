package com.asrevo.cvhome.store.service.mapper.order;

import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.store.core.entity.order.orderproduct.OrderProductAttribute;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProduct;
import com.asrevo.cvhome.store.core.model.order.ReadableOrderProduct;
import com.asrevo.cvhome.store.core.model.order.ReadableOrderProductAttribute;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.service.mapper.catalog.product.ReadableProductMapper;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class ReadableOrderProductMapper implements Mapper<OrderProduct, ReadableOrderProduct> {

    final PricingService pricingService;

    final ProductService productService;

    final ReadableProductMapper readableProductMapper;

    public ReadableOrderProductMapper(
            PricingService pricingService,
            ProductService productService,
            ReadableProductMapper readableProductMapper,
            ImageFilePath imageUtils) {
        this.pricingService = pricingService;
        this.productService = productService;
        this.readableProductMapper = readableProductMapper;
    }

    @Override
    public ReadableOrderProduct convert(
            OrderProduct source, MerchantStore store, Language language) {
        ReadableOrderProduct orderProduct = new ReadableOrderProduct();
        return this.merge(source, orderProduct, store, language);
    }

    @Override
    public ReadableOrderProduct merge(
            OrderProduct source,
            ReadableOrderProduct target,
            MerchantStore store,
            Language language) {

        Validate.notNull(source, "OrderProduct cannot be null");
        Validate.notNull(target, "ReadableOrderProduct cannot be null");
        Validate.notNull(store, "MerchantStore cannot be null");
        Validate.notNull(language, "Language cannot be null");

        target.setId(source.getId());
        target.setOrderedQuantity(source.getProductQuantity());
        try {
            target.setPrice(pricingService.getDisplayAmount(source.getOneTimeCharge(), store));
        } catch (Exception e) {
            throw new ConversionRuntimeException("Cannot convert price", e);
        }
        target.setProductName(source.getProductName());
        target.setSku(source.getSku());

        // subtotal = price * quantity
        BigDecimal subTotal = source.getOneTimeCharge();
        subTotal = subTotal.multiply(new BigDecimal(source.getProductQuantity()));

        try {
            String subTotalPrice = pricingService.getDisplayAmount(subTotal, store);
            target.setSubTotal(subTotalPrice);
        } catch (Exception e) {
            throw new ConversionRuntimeException("Cannot format price", e);
        }

        if (source.getOrderAttributes() != null) {
            List<ReadableOrderProductAttribute> attributes = new ArrayList<>();
            for (OrderProductAttribute attr : source.getOrderAttributes()) {
                ReadableOrderProductAttribute readableAttribute =
                        new ReadableOrderProductAttribute();
                try {
                    String price =
                            pricingService.getDisplayAmount(attr.getProductAttributePrice(), store);
                    readableAttribute.setAttributePrice(price);
                } catch (ServiceException e) {
                    throw new ConversionRuntimeException("Cannot format price", e);
                }

                readableAttribute.setAttributeName(attr.getProductAttributeName());
                readableAttribute.setAttributeValue(attr.getProductAttributeValueName());
                attributes.add(readableAttribute);
            }
            target.setAttributes(attributes);
        }

        String productSku = source.getSku();
        if (!StringUtils.isBlank(productSku)) {
            Product product = null;
            try {
                product = productService.getBySku(productSku, store, language);
            } catch (ServiceException e) {
                throw new ServiceRuntimeException(e);
            }
            if (product != null) {

                ReadableProduct productProxy =
                        readableProductMapper.convert(product, store, language);
                target.setProduct(productProxy);
            }
        }

        return target;
    }
}
